#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 显示帮助信息
function show_help {
    echo -e "${YELLOW}使用方法:${NC}"
    echo -e "  $0 -p <jar文件路径> [-b <备份目录>] [-n <应用名称>] [-port <端口>] [-profile <环境>]"
    echo -e ""
    echo -e "${YELLOW}参数:${NC}"
    echo -e "  -p, --path      ${NC}jar文件所在目录路径 (必填)"
    echo -e "  -b, --backup    ${NC}备份目录路径 (选填，默认为./backups)"
    echo -e "  -n, --name      ${NC}应用名称 (选填，默认为'java-app')"
    echo -e "  -port           ${NC}应用端口 (选填，默认为8080)"
    echo -e "  -profile        ${NC}SpringBoot环境配置 (选填，默认为'prod')"
    echo -e "  -h, --help      ${NC}显示帮助信息"
    exit 1
}

# 检查命令是否存在
function check_command_exists {
    command -v $1 >/dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo -e "${YELLOW}警告: 命令 '$1' 不存在，部分功能可能受限${NC}"
        return 1
    fi
    return 0
}

# 检查命令是否成功执行
function check_command {
    if [ $? -ne 0 ]; then
        echo -e "${RED}错误: $1${NC}"
        exit 1
    fi
}

# 停止正在运行的应用
function stop_running_app {
    local app_name=$1
    local port=$2
    
    echo -e "${YELLOW}检查是否有应用正在运行...${NC}"
    
    # 检查Docker命令是否存在
    if check_command_exists docker; then
        # 检查容器是否在运行
        if docker ps | grep -q "$app_name"; then
            echo -e "${YELLOW}发现正在运行的Docker容器，开始停止...${NC}"
            docker stop "$app_name" > /dev/null 2>&1
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ 已停止Docker容器: $app_name${NC}"
            else
                echo -e "${YELLOW}⚠ 无法停止Docker容器: $app_name${NC}"
            fi
        fi
    else
        echo -e "${YELLOW}跳过Docker容器检查${NC}"
    fi
    
    # 检查lsof命令是否存在
    if check_command_exists lsof; then
        # 检查端口是否被占用
        local pid=$(lsof -i :"$port" -t 2>/dev/null)
        if [ -n "$pid" ]; then
            echo -e "${YELLOW}端口 $port 被PID为 $pid 的进程占用，尝试终止进程...${NC}"
            kill -15 "$pid" 2>/dev/null
            
            # 等待进程终止
            local count=0
            while [ $count -lt 10 ] && lsof -i :"$port" -t > /dev/null 2>&1; do
                echo -e "${YELLOW}等待进程结束...${NC}"
                sleep 1
                count=$((count+1))
            done
            
            # 如果进程仍在运行，强制终止
            if lsof -i :"$port" -t > /dev/null 2>&1; then
                echo -e "${YELLOW}进程未响应，强制终止...${NC}"
                kill -9 "$pid" 2>/dev/null
                sleep 1
            fi
            
            if ! lsof -i :"$port" -t > /dev/null 2>&1; then
                echo -e "${GREEN}✓ 已释放端口 $port${NC}"
            else
                echo -e "${RED}⚠ 无法释放端口 $port，请手动检查${NC}"
            fi
        fi
    elif check_command_exists netstat; then
        # 如果没有lsof，尝试使用netstat
        echo -e "${YELLOW}使用netstat检查端口${NC}"
        local pid=$(netstat -tuln 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1)
        if [ -n "$pid" ] && [ "$pid" != "" ]; then
            echo -e "${YELLOW}端口 $port 被PID为 $pid 的进程占用，尝试终止进程...${NC}"
            kill -15 "$pid" 2>/dev/null
            sleep 5
            kill -9 "$pid" 2>/dev/null 2>&1
        fi
    else
        echo -e "${YELLOW}跳过端口检查，无法找到lsof或netstat命令${NC}"
    fi
}

# 清理旧备份，保留至少5个备份且至少保留3个月内的备份
function clean_old_backups {
    local backup_dir=$1
    local backup_prefix=$2
    
    echo -e "${YELLOW}管理历史备份文件...${NC}"
    
    # 检查备份目录是否存在
    if [ ! -d "$backup_dir" ]; then
        echo -e "${YELLOW}备份目录不存在，跳过清理${NC}"
        return
    fi
    
    # 获取所有备份文件
    local all_backups=($(ls -t ${backup_dir}/${backup_prefix}*.tar.gz 2>/dev/null))
    local backup_count=${#all_backups[@]}
    
    if [ $backup_count -eq 0 ]; then
        echo -e "${YELLOW}没有找到历史备份文件${NC}"
        return
    fi
    
    echo -e "${YELLOW}发现 $backup_count 个历史备份文件${NC}"
    
    # 计算3个月前的时间戳
    local current_timestamp=$(date +%s)
    local three_months_seconds=$((90*24*60*60))  # 90天的秒数
    local three_months_ago=$((current_timestamp - three_months_seconds))
    
    # 尝试使用date命令计算3个月前，如果支持的话
    if date --version >/dev/null 2>&1; then
        # GNU date (Linux)
        three_months_ago=$(date -d "90 days ago" +%s 2>/dev/null)
    elif date -v-90d >/dev/null 2>&1; then
        # BSD date (macOS)
        three_months_ago=$(date -v-90d +%s 2>/dev/null)
    fi
    
    local files_to_delete=()
    local kept_count=0
    
    # 遍历所有备份文件
    for ((i=0; i<$backup_count; i++)); do
        local file=${all_backups[$i]}
        
        # 提取文件的时间戳
        local file_date=$(echo $file | grep -oE '[0-9]{8}_[0-9]{6}' | head -1)
        if [ -z "$file_date" ]; then
            echo -e "${YELLOW}警告: 无法从文件名解析日期: $file，将保留此文件${NC}"
            kept_count=$((kept_count + 1))
            continue
        fi
        
        local file_year=${file_date:0:4}
        local file_month=${file_date:4:2}
        local file_day=${file_date:6:2}
        local file_hour=${file_date:9:2}
        local file_min=${file_date:11:2}
        local file_sec=${file_date:13:2}
        
        # 计算文件时间戳
        local file_timestamp=""
        
        # 尝试不同的方法获取文件时间戳
        if date --version >/dev/null 2>&1; then
            # GNU date (Linux)
            file_timestamp=$(date -d "${file_year}-${file_month}-${file_day} ${file_hour}:${file_min}:${file_sec}" +%s 2>/dev/null)
        elif date -j -f "%Y%m%d_%H%M%S" "$file_date" >/dev/null 2>&1; then
            # BSD date (macOS)
            file_timestamp=$(date -j -f "%Y%m%d_%H%M%S" "$file_date" +%s 2>/dev/null)
        else
            # 手动计算大致时间戳（不精确但足够比较）
            # 从1970年到现在的月数
            local months_since_epoch=$(( (file_year - 1970) * 12 + file_month - 1 ))
            # 粗略估计天数（每月30天）
            local days_since_epoch=$(( months_since_epoch * 30 + file_day ))
            # 转换为秒
            file_timestamp=$(( days_since_epoch * 86400 + file_hour * 3600 + file_min * 60 + file_sec ))
        fi
        
        # 如果获取时间戳失败，跳过此文件
        if [ -z "$file_timestamp" ]; then
            echo -e "${YELLOW}警告: 无法计算文件时间戳: $file，将保留此文件${NC}"
            kept_count=$((kept_count + 1))
            continue
        fi
        
        # 保留规则：前5个最新备份或3个月内的备份
        if [ $i -lt 5 ] || [ $file_timestamp -gt $three_months_ago ]; then
            kept_count=$((kept_count + 1))
        else
            files_to_delete+=($file)
        fi
    done
    
    # 删除不符合保留条件的备份文件
    if [ ${#files_to_delete[@]} -gt 0 ]; then
        echo -e "${YELLOW}删除 ${#files_to_delete[@]} 个过期备份文件...${NC}"
        for file in "${files_to_delete[@]}"; do
            echo -e "${YELLOW}删除: $(basename $file)${NC}"
            rm -f "$file"
        done
    fi
    
    echo -e "${GREEN}✓ 备份管理完成，保留了 $kept_count 个备份文件${NC}"
}

# 解析参数
JAR_PATH=""
BACKUP_DIR="./backups"
APP_NAME="java-app"
PORT="8080"
PROFILE="prod"

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -p|--path) JAR_PATH="$2"; shift ;;
        -b|--backup) BACKUP_DIR="$2"; shift ;;
        -n|--name) APP_NAME="$2"; shift ;;
        -port) PORT="$2"; shift ;;
        -profile) PROFILE="$2"; shift ;;
        -h|--help) show_help ;;
        *) echo -e "${RED}未知参数: $1${NC}"; show_help ;;
    esac
    shift
done

# 检查必填参数
if [ -z "$JAR_PATH" ]; then
    echo -e "${RED}错误: 必须指定jar文件路径${NC}"
    show_help
fi

# 检查路径是否存在
if [ ! -d "$JAR_PATH" ]; then
    echo -e "${RED}错误: 指定的路径 '$JAR_PATH' 不存在或不是目录${NC}"
    exit 1
fi

echo -e "${GREEN}=== 开始部署SpringBoot应用: $APP_NAME ===${NC}"

# 生成时间戳
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="${APP_NAME}_${TIMESTAMP}"
BACKUP_FILE="${BACKUP_DIR}/${BACKUP_NAME}.tar.gz"

# 创建备份目录（如果不存在）
echo -e "${YELLOW}步骤1: 创建备份目录...${NC}"
mkdir -p "$BACKUP_DIR"
check_command "创建备份目录失败"
echo -e "${GREEN}✓ 备份目录已创建: $BACKUP_DIR${NC}"

# 查找jar文件
echo -e "${YELLOW}步骤2: 查找SpringBoot应用jar文件...${NC}"
JAR_FILES=($JAR_PATH/*.jar)
if [ ${#JAR_FILES[@]} -eq 0 ]; then
    echo -e "${RED}错误: 指定目录下没有找到jar文件${NC}"
    exit 1
elif [ ${#JAR_FILES[@]} -gt 1 ]; then
    echo -e "${YELLOW}警告: 发现多个jar文件, 将使用第一个: ${JAR_FILES[0]}${NC}"
    MAIN_JAR=${JAR_FILES[0]}
else
    MAIN_JAR=${JAR_FILES[0]}
fi
MAIN_JAR_NAME=$(basename "$MAIN_JAR")
echo -e "${GREEN}✓ 已找到SpringBoot应用jar文件: $MAIN_JAR_NAME${NC}"

# 创建压缩备份
echo -e "${YELLOW}步骤3: 创建压缩备份...${NC}"
# 仅备份最终产出的主Jar，避免打包整个target目录导致长时间阻塞
tar -czf "$BACKUP_FILE" -C "$JAR_PATH" "$MAIN_JAR_NAME"
check_command "创建压缩备份失败"
echo -e "${GREEN}✓ 备份文件已创建: $BACKUP_FILE${NC}"

# 清理旧备份
clean_old_backups "$BACKUP_DIR" "${APP_NAME}_"

# 检查docker-compose.yml是否存在
echo -e "${YELLOW}步骤4: 检查Docker Compose配置...${NC}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ ! -f "${SCRIPT_DIR}/docker-compose.yml" ]; then
    echo -e "${RED}错误: 在 ${SCRIPT_DIR} 目录下没有找到docker-compose.yml文件${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose配置文件检查通过${NC}"

# 检查Docker和Docker Compose是否安装
echo -e "${YELLOW}步骤5: 检查Docker环境...${NC}"
if ! check_command_exists docker; then
    echo -e "${RED}错误: Docker未安装，请先安装Docker${NC}"
    exit 1
fi
if ! check_command_exists docker-compose; then
    echo -e "${RED}错误: Docker Compose未安装，请先安装Docker Compose${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker环境检查通过${NC}"

# 设置环境变量供docker-compose使用
export JAR_PATH="$JAR_PATH"
export JAR_FILE="$MAIN_JAR_NAME"
export APP_NAME="$APP_NAME"
export PORT="$PORT"
export SPRING_PROFILES="$PROFILE"

# 检测操作系统类型
echo -e "${YELLOW}步骤6: 检测操作系统类型...${NC}"
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

# 停止正在运行的应用
echo -e "${YELLOW}步骤7: 停止正在运行的应用...${NC}"
stop_running_app "$APP_NAME" "$PORT"

# 启动Docker容器
echo -e "${YELLOW}步骤8: 启动SpringBoot应用...${NC}"
cd "$SCRIPT_DIR"
docker-compose down 2>/dev/null
sleep 2
docker-compose up -d
check_command "启动应用失败"
echo -e "${GREEN}✓ SpringBoot应用已成功启动${NC}"

echo -e "${GREEN}=== 部署完成 ===${NC}"
echo -e "${YELLOW}SpringBoot应用信息:${NC}"
echo -e "  名称: ${APP_NAME}"
echo -e "  端口: ${PORT}"
echo -e "  环境: ${PROFILE}"
echo -e "  JAR文件: ${MAIN_JAR_NAME}"
echo -e "  备份文件: $BACKUP_FILE"
echo -e "  操作系统: ${OS_TYPE}"
# echo -e "  服务连接:"
# echo -e "    - MySQL: host.docker.internal (Docker内部访问宿主机)"
# echo -e "    - Redis: host.docker.internal (Docker内部访问宿主机)"
# echo -e ""
echo -e "${GREEN}可以通过以下命令查看日志:${NC}"
echo -e "  docker-compose logs -f ${APP_NAME}" 
