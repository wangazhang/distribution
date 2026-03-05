#!/bin/bash

# frpc启动脚本
# 用于连接到远程frps服务器，监听本地48080端口

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# frpc可执行文件路径
FRPC_PATH="$HOME/Documents/opt/frps/frp_0.63.0_darwin_arm64/frpc"

# 配置文件路径
CONFIG_PATH="$SCRIPT_DIR/frpc.toml"

# 日志文件路径
LOG_PATH="$SCRIPT_DIR/frpc.log"

# PID文件路径
PID_PATH="$SCRIPT_DIR/frpc.pid"

# 检查frpc可执行文件是否存在
check_frpc_exists() {
    if [ ! -f "$FRPC_PATH" ]; then
        echo -e "${RED}错误: frpc可执行文件不存在: $FRPC_PATH${NC}"
        echo -e "${YELLOW}请确认frpc安装路径是否正确${NC}"
        exit 1
    fi
    
    if [ ! -x "$FRPC_PATH" ]; then
        echo -e "${YELLOW}警告: frpc文件没有执行权限，正在添加执行权限...${NC}"
        chmod +x "$FRPC_PATH"
    fi
}

# 检查配置文件是否存在
check_config_exists() {
    if [ ! -f "$CONFIG_PATH" ]; then
        echo -e "${RED}错误: 配置文件不存在: $CONFIG_PATH${NC}"
        exit 1
    fi
}

# 检查frpc是否已经运行
check_frpc_running() {
    if [ -f "$PID_PATH" ]; then
        local pid=$(cat "$PID_PATH")
        if ps -p $pid > /dev/null 2>&1; then
            return 0  # 正在运行
        else
            rm -f "$PID_PATH"  # 清理无效的PID文件
            return 1  # 未运行
        fi
    fi
    return 1  # 未运行
}

# 启动frpc
start_frpc() {
    echo -e "${BLUE}正在检查frpc状态...${NC}"
    
    if check_frpc_running; then
        echo -e "${YELLOW}frpc已经在运行中 (PID: $(cat $PID_PATH))${NC}"
        return 0
    fi
    
    echo -e "${BLUE}正在启动frpc...${NC}"
    echo -e "${BLUE}服务器地址: 203.0.113.17:7500${NC}"
    echo -e "${BLUE}本地端口: 48080${NC}"
    echo -e "${BLUE}配置文件: $CONFIG_PATH${NC}"
    echo -e "${BLUE}日志文件: $LOG_PATH${NC}"
    
    # 启动frpc并将PID保存到文件
    nohup "$FRPC_PATH" -c "$CONFIG_PATH" > "$LOG_PATH" 2>&1 &
    local pid=$!
    echo $pid > "$PID_PATH"
    
    # 等待一秒检查是否启动成功
    sleep 1
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${GREEN}frpc启动成功! (PID: $pid)${NC}"
        echo -e "${GREEN}可以通过以下命令查看日志: tail -f $LOG_PATH${NC}"
        echo -e "${GREEN}本地服务48080端口已通过frp暴露到远程服务器${NC}"
    else
        echo -e "${RED}frpc启动失败，请检查日志: $LOG_PATH${NC}"
        rm -f "$PID_PATH"
        exit 1
    fi
}

# 停止frpc
stop_frpc() {
    echo -e "${BLUE}正在停止frpc...${NC}"
    
    if ! check_frpc_running; then
        echo -e "${YELLOW}frpc未运行${NC}"
        return 0
    fi
    
    local pid=$(cat "$PID_PATH")
    kill $pid
    
    # 等待进程结束
    local count=0
    while ps -p $pid > /dev/null 2>&1 && [ $count -lt 10 ]; do
        sleep 1
        count=$((count + 1))
    done
    
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${YELLOW}正常停止失败，强制终止进程...${NC}"
        kill -9 $pid
    fi
    
    rm -f "$PID_PATH"
    echo -e "${GREEN}frpc已停止${NC}"
}

# 重启frpc
restart_frpc() {
    echo -e "${BLUE}正在重启frpc...${NC}"
    stop_frpc
    sleep 2
    start_frpc
}

# 查看frpc状态
status_frpc() {
    if check_frpc_running; then
        local pid=$(cat "$PID_PATH")
        echo -e "${GREEN}frpc正在运行 (PID: $pid)${NC}"
        echo -e "${BLUE}配置信息:${NC}"
        echo -e "  服务器地址: 203.0.113.17:7500"
        echo -e "  本地端口: 48080"
        echo -e "  日志文件: $LOG_PATH"
    else
        echo -e "${YELLOW}frpc未运行${NC}"
    fi
}

# 查看日志
show_logs() {
    if [ -f "$LOG_PATH" ]; then
        echo -e "${BLUE}显示frpc日志 (按Ctrl+C退出):${NC}"
        tail -f "$LOG_PATH"
    else
        echo -e "${YELLOW}日志文件不存在: $LOG_PATH${NC}"
    fi
}

# 显示帮助信息
show_help() {
    echo -e "${BLUE}frpc管理脚本${NC}"
    echo
    echo "用法: $(basename $0) [命令]"
    echo
    echo "命令:"
    echo "  start    启动frpc"
    echo "  stop     停止frpc"
    echo "  restart  重启frpc"
    echo "  status   查看frpc状态"
    echo "  logs     查看frpc日志"
    echo "  help     显示帮助信息"
    echo
    echo "配置信息:"
    echo "  远程服务器: 203.0.113.17:7500"
    echo "  本地端口: 48080"
    echo "  frpc路径: $FRPC_PATH"
    echo "  配置文件: $CONFIG_PATH"
    echo
}

# 主函数
main() {
    # 检查必要文件
    check_frpc_exists
    check_config_exists
    
    case "${1:-start}" in
        start)
            start_frpc
            ;;
        stop)
            stop_frpc
            ;;
        restart)
            restart_frpc
            ;;
        status)
            status_frpc
            ;;
        logs)
            show_logs
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}未知命令: $1${NC}"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
