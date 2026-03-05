#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 创建测试环境
echo -e "${YELLOW}===== 创建测试环境 =====${NC}"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_DIR="${SCRIPT_DIR}/test-env"
TEST_APP_DIR="${TEST_DIR}/app"
TEST_JAR_NAME="distribution-server.jar"

# 获取项目根目录
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
SOURCE_JAR="${PROJECT_ROOT}/distribution-server/target/${TEST_JAR_NAME}"

# 检查源JAR文件是否存在
if [ ! -f "${SOURCE_JAR}" ]; then
    echo -e "${RED}错误: 源JAR文件不存在: ${SOURCE_JAR}${NC}"
    echo -e "${YELLOW}请先构建项目，确保JAR文件已生成${NC}"
    exit 1
fi

# 创建测试目录
echo -e "${YELLOW}创建测试目录...${NC}"
mkdir -p "${TEST_APP_DIR}"

# 复制实际的JAR文件到测试目录
echo -e "${YELLOW}复制JAR文件从 ${SOURCE_JAR} 到测试环境...${NC}"
cp "${SOURCE_JAR}" "${TEST_APP_DIR}/${TEST_JAR_NAME}"
if [ $? -ne 0 ]; then
    echo -e "${RED}复制JAR文件失败${NC}"
    exit 1
fi

# 确认测试环境准备完成
echo -e "${GREEN}测试环境准备完成${NC}"
echo -e "测试目录: ${TEST_APP_DIR}"
echo -e "测试JAR文件: ${TEST_APP_DIR}/${TEST_JAR_NAME}"
echo -e "JAR文件大小: $(du -h "${TEST_APP_DIR}/${TEST_JAR_NAME}" | cut -f1)"
echo -e ""

# 准备执行参数
DEPLOY_SCRIPT="${SCRIPT_DIR}/deploy.sh"
APP_NAME="distribution-server-deploy-test-app"
BACKUP_DIR="${TEST_DIR}/backups"
PORT="48080"
PROFILE="local"

# 显示即将执行的命令
echo -e "${YELLOW}===== 即将执行部署脚本 =====${NC}"
echo -e "部署脚本: ${DEPLOY_SCRIPT}"
echo -e "参数:"
echo -e "  -p ${TEST_APP_DIR}"
echo -e "  -n ${APP_NAME}"
echo -e "  -b ${BACKUP_DIR}"
echo -e "  -port ${PORT}"
echo -e "  -profile ${PROFILE}"
echo -e ""

# 询问用户是否继续
echo -e "${YELLOW}确认执行部署测试? (y/n)${NC}"
read -r response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo -e "${GREEN}开始执行部署测试...${NC}"
    # 设置执行权限
    chmod +x "${DEPLOY_SCRIPT}"
    # 执行部署脚本
    "${DEPLOY_SCRIPT}" -p "${TEST_APP_DIR}" -n "${APP_NAME}" -b "${BACKUP_DIR}" -port "${PORT}" -profile "${PROFILE}"
else
    echo -e "${RED}测试已取消${NC}"
    exit 0
fi 