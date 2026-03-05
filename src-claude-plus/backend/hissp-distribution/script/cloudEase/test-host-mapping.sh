#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${GREEN}=== 宿主机服务连接测试 ===${NC}"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装${NC}"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装${NC}"
    exit 1
fi

# 检测操作系统类型
echo -e "${YELLOW}步骤1: 检测操作系统类型...${NC}"
OS_TYPE="unknown"
if [[ "$(uname)" == "Darwin" ]]; then
    OS_TYPE="macOS"
    echo -e "${GREEN}✓ 检测到macOS系统${NC}"
elif [[ "$(uname)" == "Linux" ]]; then
    OS_TYPE="Linux"
    echo -e "${GREEN}✓ 检测到Linux系统${NC}"
else
    echo -e "${YELLOW}⚠ 未能识别的操作系统类型，将使用默认配置${NC}"
fi

# 创建测试容器
echo -e "${YELLOW}步骤2: 创建测试容器...${NC}"
TEST_CONTAINER_NAME="host-mapping-test"

# 停止并删除已存在的测试容器
docker rm -f "$TEST_CONTAINER_NAME" &> /dev/null

# 启动测试容器
echo -e "${YELLOW}启动测试容器...${NC}"
docker run --name "$TEST_CONTAINER_NAME" -d \
    -e MYSQL_HOST=host.docker.internal \
    -e REDIS_HOST=host.docker.internal \
    alpine:latest sleep 300

if [ $? -ne 0 ]; then
    echo -e "${RED}错误: 无法启动测试容器${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 测试容器已启动${NC}"

# 测试环境变量
echo -e "${YELLOW}步骤3: 测试环境变量...${NC}"
echo -e "${YELLOW}检查环境变量:${NC}"
docker exec "$TEST_CONTAINER_NAME" env | grep -E "MYSQL_HOST|REDIS_HOST"
echo ""

# 测试连接
echo -e "${YELLOW}步骤4: 测试连接宿主机...${NC}"

# 安装ping工具
echo -e "${YELLOW}安装测试工具...${NC}"
docker exec "$TEST_CONTAINER_NAME" apk add --no-cache iputils curl &> /dev/null

# 测试连接host.docker.internal
echo -e "${YELLOW}测试连接host.docker.internal:${NC}"
docker exec "$TEST_CONTAINER_NAME" ping -c 2 host.docker.internal || echo -e "${YELLOW}注意: 在某些Linux系统上可能需要额外配置${NC}"
echo ""

# 测试端口连接
echo -e "${YELLOW}步骤5: 测试端口连接...${NC}"

# 检查MySQL端口
echo -e "${YELLOW}测试MySQL端口连接 (3306):${NC}"
docker exec "$TEST_CONTAINER_NAME" timeout 2 bash -c "echo > /dev/tcp/host.docker.internal/3306" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ MySQL端口连接成功${NC}"
else
    echo -e "${YELLOW}⚠ MySQL端口连接失败，请确认宿主机上MySQL服务是否运行在3306端口${NC}"
fi

# 检查Redis端口
echo -e "${YELLOW}测试Redis端口连接 (6379):${NC}"
docker exec "$TEST_CONTAINER_NAME" timeout 2 bash -c "echo > /dev/tcp/host.docker.internal/6379" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Redis端口连接成功${NC}"
else
    echo -e "${YELLOW}⚠ Redis端口连接失败，请确认宿主机上Redis服务是否运行在6379端口${NC}"
fi

# 清理测试容器
echo -e "${YELLOW}步骤6: 清理测试容器...${NC}"
docker rm -f "$TEST_CONTAINER_NAME" &> /dev/null
echo -e "${GREEN}✓ 测试容器已清理${NC}"

echo -e "${GREEN}=== 测试完成 ===${NC}"
echo -e "${GREEN}操作系统: $OS_TYPE${NC}"
echo -e "${GREEN}宿主机连接测试完成!${NC}"
echo -e ""
echo -e "${YELLOW}注意事项:${NC}"
echo -e "1. 在macOS和Windows上，host.docker.internal特性默认可用"
echo -e "2. 在Linux系统上，可能需要在docker run命令中添加 --add-host=host.docker.internal:host-gateway 参数"
echo -e "3. 确保应用程序使用环境变量连接服务，例如:"
echo -e "   spring.datasource.url=jdbc:mysql://\${MYSQL_HOST:localhost}:3306/your_database"
echo -e "   spring.redis.host=\${REDIS_HOST:localhost}" 