#!/bin/bash
# 数据库配置批量部署脚本 - 结合多线程传输和密码重置功能

# 脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
TRANSFER_SCRIPT="$SCRIPT_DIR/file_transfer.sh"
RESET_PASSWORD_SCRIPT="$SCRIPT_DIR/reset_db_passwords.sh"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认值
DEFAULT_PASSWORD="CHANGE_ME_DB_PASSWORD"
DEFAULT_PORT=22
DEFAULT_WORKERS=5

# 显示帮助信息
function show_help {
    echo -e "${BLUE}数据库配置批量部署脚本${NC}"
    echo
    echo "该脚本用于将数据库配置文件批量传输到远程服务器，并可以同时修改密码"
    echo
    echo "用法: $(basename $0) [选项]"
    echo
    echo "选项:"
    echo "  -h, --help                显示帮助信息"
    echo "  -H, --host HOST           远程服务器地址（必需）"
    echo "  -P, --port PORT           SSH端口号，默认为$DEFAULT_PORT"
    echo "  -u, --user USER           SSH用户名（必需）"
    echo "  -p, --password PWD        SSH密码"
    echo "  -k, --keyfile FILE        SSH私钥文件路径"
    echo "  -s, --source DIR          本地源目录，包含要传输的配置文件（必需）"
    echo "  -r, --remote DIR          远程目标目录（必需）"
    echo "  -w, --workers NUM         工作线程数，默认为$DEFAULT_WORKERS"
    echo "  -d, --db-password PWD     设置所有数据库密码，默认为$DEFAULT_PASSWORD"
    echo "  --preserve-path           保留相对路径结构"
    echo "  --reset-only              只在本地重置密码，不传输到远程服务器"
    echo "  --transfer-only           只传输文件，不重置密码"
    echo "  --yes                     自动确认所有操作，不提示"
    echo
    echo "示例:"
    echo "  $(basename $0) -H 192.168.1.100 -u root -p password -s ./configs -r /opt/app/configs"
    echo "  $(basename $0) -H 192.168.1.100 -u root -k ~/.ssh/id_rsa -s ./configs -r /opt/app/configs -d custom_password"
    echo
    exit 0
}

# 检查依赖脚本
function check_dependencies {
    if [ ! -f "$TRANSFER_SCRIPT" ]; then
        echo -e "${RED}错误: 找不到文件传输脚本: $TRANSFER_SCRIPT${NC}"
        exit 1
    fi
    
    if [ ! -f "$RESET_PASSWORD_SCRIPT" ]; then
        echo -e "${RED}错误: 找不到密码重置脚本: $RESET_PASSWORD_SCRIPT${NC}"
        exit 1
    fi
    
    # 确保脚本有执行权限
    chmod +x "$TRANSFER_SCRIPT" "$RESET_PASSWORD_SCRIPT"
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

# 重置密码
function reset_passwords {
    echo -e "${BLUE}开始重置密码...${NC}"
    
    # 使用-y选项自动确认
    bash "$RESET_PASSWORD_SCRIPT" --yes
    
    echo -e "${GREEN}密码重置完成!${NC}"
}

# 传输文件
function transfer_files {
    echo -e "${BLUE}开始传输文件...${NC}"
    
    # 构建文件传输命令
    local cmd="$TRANSFER_SCRIPT -H $HOST -P $PORT -u $USER"
    
    if [ -n "$PASSWORD" ]; then
        cmd="$cmd -p $PASSWORD"
    elif [ -n "$KEYFILE" ]; then
        cmd="$cmd -k $KEYFILE"
    fi
    
    cmd="$cmd -d $SOURCE -r $REMOTE -w $WORKERS"
    
    if [ "$PRESERVE_PATH" = true ]; then
        cmd="$cmd --preserve-path"
    fi
    
    # 执行传输命令
    eval "$cmd"
    
    echo -e "${GREEN}文件传输完成!${NC}"
}

# 处理命令行参数
HOST=""
PORT=$DEFAULT_PORT
USER=""
PASSWORD=""
KEYFILE=""
SOURCE=""
REMOTE=""
WORKERS=$DEFAULT_WORKERS
DB_PASSWORD=$DEFAULT_PASSWORD
PRESERVE_PATH=false
RESET_ONLY=false
TRANSFER_ONLY=false
AUTO_CONFIRM=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -H|--host)
            HOST="$2"
            shift 2
            ;;
        -P|--port)
            PORT="$2"
            shift 2
            ;;
        -u|--user)
            USER="$2"
            shift 2
            ;;
        -p|--password)
            PASSWORD="$2"
            shift 2
            ;;
        -k|--keyfile)
            KEYFILE="$2"
            shift 2
            ;;
        -s|--source)
            SOURCE="$2"
            shift 2
            ;;
        -r|--remote)
            REMOTE="$2"
            shift 2
            ;;
        -w|--workers)
            WORKERS="$2"
            shift 2
            ;;
        -d|--db-password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        --preserve-path)
            PRESERVE_PATH=true
            shift
            ;;
        --reset-only)
            RESET_ONLY=true
            shift
            ;;
        --transfer-only)
            TRANSFER_ONLY=true
            shift
            ;;
        --yes)
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
echo -e "${BLUE}数据库配置批量部署脚本${NC}"
echo

# 检查依赖脚本
check_dependencies

# 检查必要参数
if [ "$RESET_ONLY" = false ] && [ -z "$HOST" ]; then
    echo -e "${RED}错误: 缺少必要参数 -H/--host${NC}"
    show_help
fi

if [ "$RESET_ONLY" = false ] && [ -z "$USER" ]; then
    echo -e "${RED}错误: 缺少必要参数 -u/--user${NC}"
    show_help
fi

if [ "$RESET_ONLY" = false ] && [ -z "$SOURCE" ]; then
    echo -e "${RED}错误: 缺少必要参数 -s/--source${NC}"
    show_help
fi

if [ "$RESET_ONLY" = false ] && [ -z "$REMOTE" ]; then
    echo -e "${RED}错误: 缺少必要参数 -r/--remote${NC}"
    show_help
fi

if [ "$RESET_ONLY" = false ] && [ -z "$PASSWORD" ] && [ -z "$KEYFILE" ]; then
    echo -e "${RED}错误: 必须提供SSH密码(-p)或私钥文件(-k)${NC}"
    show_help
fi

# 检查源目录是否存在
if [ "$RESET_ONLY" = false ] && [ ! -d "$SOURCE" ]; then
    echo -e "${RED}错误: 源目录不存在: $SOURCE${NC}"
    exit 1
fi

# 检查是否同时设置了互斥选项
if [ "$RESET_ONLY" = true ] && [ "$TRANSFER_ONLY" = true ]; then
    echo -e "${RED}错误: 不能同时指定 --reset-only 和 --transfer-only${NC}"
    show_help
fi

# 确认显示配置信息
if [ "$RESET_ONLY" = false ]; then
    echo -e "${YELLOW}配置信息:${NC}"
    echo "  远程服务器: $HOST:$PORT"
    echo "  SSH用户名: $USER"
    if [ -n "$PASSWORD" ]; then
        echo "  认证方式: 密码"
    else
        echo "  认证方式: 私钥文件 ($KEYFILE)"
    fi
    echo "  源目录: $SOURCE"
    echo "  目标目录: $REMOTE"
    echo "  工作线程数: $WORKERS"
fi

if [ "$TRANSFER_ONLY" = false ]; then
    echo "  数据库密码: $DB_PASSWORD"
fi

echo

# 执行操作
if [ "$RESET_ONLY" = true ]; then
    # 只重置密码
    if confirm "确定要重置本地配置文件中的数据库密码为 $DB_PASSWORD 吗?"; then
        reset_passwords
    else
        echo -e "${YELLOW}操作已取消${NC}"
        exit 0
    fi
elif [ "$TRANSFER_ONLY" = true ]; then
    # 只传输文件
    if confirm "确定要将配置文件从 $SOURCE 传输到 $HOST:$REMOTE 吗?"; then
        transfer_files
    else
        echo -e "${YELLOW}操作已取消${NC}"
        exit 0
    fi
else
    # 重置密码并传输文件
    if confirm "确定要重置密码并将配置文件传输到远程服务器吗?"; then
        reset_passwords
        transfer_files
        echo -e "${GREEN}所有操作已完成!${NC}"
    else
        echo -e "${YELLOW}操作已取消${NC}"
        exit 0
    fi
fi 
