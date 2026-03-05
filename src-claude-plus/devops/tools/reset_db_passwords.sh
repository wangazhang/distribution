#!/bin/bash
# 数据库密码重置脚本 - 将所有数据库默认密码改为占位值（请按环境自行修改），包括 Redis

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 工作目录
WORK_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_DIR="$(dirname "$WORK_DIR")"
CONFIG_FILES=()

# 新密码
NEW_PASSWORD="CHANGE_ME_DB_PASSWORD"

# 显示帮助信息
function show_help {
    echo -e "${BLUE}数据库密码重置脚本${NC}"
    echo
    echo "将所有数据库默认密码全部改成${NEW_PASSWORD}，包括Redis、MySQL等"
    echo
    echo "用法: $(basename $0) [选项]"
    echo
    echo "选项:"
    echo "  -h, --help      显示帮助信息"
    echo "  -y, --yes       自动确认所有操作，不提示"
    echo
    exit 0
}

# 确认函数
function confirm {
    if [ "$AUTO_CONFIRM" = true ]; then
        return 0
    fi
    
    read -p "$1 (y/n): " choice
    case "$choice" in
        y|Y ) return 0;;
        * ) return 1;;
    esac
}

# 搜索配置文件
function find_config_files {
    echo -e "${BLUE}正在搜索配置文件...${NC}"
    
    # 应用配置文件
    local app_configs=($(find "$PROJECT_DIR" -type f -name "application*.yaml" -o -name "application*.yml" -o -name "application*.properties"))
    
    # Docker环境文件
    local docker_envs=($(find "$PROJECT_DIR" -type f -name "docker*.env"))
    
    # Docker Compose文件
    local docker_compose=($(find "$PROJECT_DIR" -type f -name "docker-compose*.yml" -o -name "docker-compose*.yaml"))
    
    # Redis配置文件
    local redis_configs=($(find "$PROJECT_DIR" -type f -name "redis*.conf"))
    
    CONFIG_FILES=("${app_configs[@]}" "${docker_envs[@]}" "${docker_compose[@]}" "${redis_configs[@]}")
    
    echo -e "${GREEN}找到 ${#CONFIG_FILES[@]} 个可能的配置文件${NC}"
    
    # 显示找到的文件
    if [ ${#CONFIG_FILES[@]} -gt 0 ]; then
        echo -e "${YELLOW}找到以下配置文件:${NC}"
        for file in "${CONFIG_FILES[@]}"; do
            echo "  - $file"
        done
        echo
    fi
}

# 修改密码
function update_passwords {
    echo -e "${BLUE}开始更新密码...${NC}"
    
    for file in "${CONFIG_FILES[@]}"; do
        echo -e "${YELLOW}处理文件: $file${NC}"
        
        # 文件扩展名
        ext="${file##*.}"
        filename=$(basename "$file")
        
        # 根据不同类型的文件采用不同的处理方式
        if [[ "$filename" == *redis*.conf ]]; then
            # Redis配置文件
            if grep -q "^requirepass" "$file"; then
                # 如果已经有密码行，则修改它
                sed -i'' -e "s/^requirepass .*/requirepass $NEW_PASSWORD/" "$file"
                echo -e "  ${GREEN}已更新 Redis 密码${NC}"
            else
                # 如果没有密码行，则添加一行
                echo "requirepass $NEW_PASSWORD" >> "$file"
                echo -e "  ${GREEN}已添加 Redis 密码${NC}"
            fi
            
        elif [[ "$ext" == "env" ]]; then
            # Docker环境文件
            if grep -q "MYSQL_ROOT_PASSWORD=" "$file"; then
                sed -i'' -e "s/MYSQL_ROOT_PASSWORD=.*/MYSQL_ROOT_PASSWORD=$NEW_PASSWORD/" "$file"
                echo -e "  ${GREEN}已更新 MySQL root 密码${NC}"
            fi
            
            if grep -q "REDIS_PASSWORD=" "$file"; then
                sed -i'' -e "s/REDIS_PASSWORD=.*/REDIS_PASSWORD=$NEW_PASSWORD/" "$file"
                echo -e "  ${GREEN}已更新 Redis 密码${NC}"
            fi
            
        elif [[ "$ext" == "yml" || "$ext" == "yaml" ]]; then
            # YAML配置文件
            
            # MySQL密码
            if grep -q "password:" "$file"; then
                # 对于MySQL/数据库密码，需要更谨慎，避免修改非数据库密码
                sed -i'' -e '/datasource/,/password:/s/password:.*/password: '"$NEW_PASSWORD"'/' "$file"
                echo -e "  ${GREEN}已更新数据库密码${NC}"
            fi
            
            # Redis密码
            if grep -q "redis:" "$file" && grep -q "password:" "$file"; then
                sed -i'' -e '/redis:/,/password:/s/password:.*/password: '"$NEW_PASSWORD"'/' "$file"
                echo -e "  ${GREEN}已更新 Redis 密码${NC}"
            fi
            
            # Docker Compose中的MySQL密码
            if grep -q "MYSQL_ROOT_PASSWORD:" "$file"; then
                sed -i'' -e "s/MYSQL_ROOT_PASSWORD:.*/MYSQL_ROOT_PASSWORD: \${MYSQL_ROOT_PASSWORD:-$NEW_PASSWORD}/" "$file"
                echo -e "  ${GREEN}已更新 Docker Compose 中的 MySQL 密码${NC}"
            fi
            
        elif [[ "$ext" == "properties" ]]; then
            # Properties文件
            
            # 数据库密码
            if grep -q "spring.datasource.*password" "$file"; then
                sed -i'' -e 's/\(spring\.datasource\..*password=\).*/\1'"$NEW_PASSWORD"'/' "$file"
                echo -e "  ${GREEN}已更新数据库密码${NC}"
            fi
            
            # Redis密码
            if grep -q "spring.redis.password" "$file"; then
                sed -i'' -e 's/\(spring\.redis\.password=\).*/\1'"$NEW_PASSWORD"'/' "$file"
                echo -e "  ${GREEN}已更新 Redis 密码${NC}"
            fi
        fi
    done
    
    echo -e "${GREEN}密码更新完成!${NC}"
}

# 处理命令行参数
AUTO_CONFIRM=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -y|--yes)
            AUTO_CONFIRM=true
            shift
            ;;
        *)
            echo -e "${RED}错误: 未知选项 $1${NC}"
            show_help
            ;;
    esac
done

# 主程序
echo -e "${BLUE}数据库密码重置脚本${NC}"
echo -e "${YELLOW}该脚本将把所有数据库默认密码改为: $NEW_PASSWORD${NC}"
echo

# 查找配置文件
find_config_files

if [ ${#CONFIG_FILES[@]} -eq 0 ]; then
    echo -e "${RED}错误: 未找到任何配置文件${NC}"
    exit 1
fi

# 确认操作
if confirm "确定要将所有数据库密码修改为 $NEW_PASSWORD 吗?"; then
    update_passwords
    
    echo -e "${GREEN}所有数据库密码已设置为 $NEW_PASSWORD${NC}"
    echo -e "${YELLOW}注意: 您可能需要重启相关服务才能使更改生效${NC}"
else
    echo -e "${YELLOW}操作已取消${NC}"
    exit 0
fi 
