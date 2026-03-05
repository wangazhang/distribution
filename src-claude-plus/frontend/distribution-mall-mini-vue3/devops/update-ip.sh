#!/bin/bash

# 日志函数
log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log "开始执行IP更新脚本..."

# 获取项目根目录路径
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

log "项目根目录: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

# 查找所有可能的.env文件
ENV_FILES=(.env .env.local .env.development .env.development.local)
FOUND_ENV_FILE=""

log "查找环境配置文件..."
for file in "${ENV_FILES[@]}"; do
  if [ -f "$file" ]; then
    log "找到环境文件: $file"
    FOUND_ENV_FILE="$file"
    break
  fi
done

# 如果没有找到.env文件，创建一个
if [ -z "$FOUND_ENV_FILE" ]; then
  FOUND_ENV_FILE=".env"
  log "未找到环境文件，将创建新文件: $FOUND_ENV_FILE"
  touch "$FOUND_ENV_FILE"
fi

# 智能获取本机IP地址的函数
get_local_ip() {
  local ip_address=""

  # 优先尝试的网卡列表
  local priority_interfaces=("en0" "eth0" "wlan0" "en1" "eth1")

  log "尝试从优先网卡获取IP地址..." >&2
  for interface in "${priority_interfaces[@]}"; do
    if ifconfig "$interface" >/dev/null 2>&1; then
      # 获取该网卡的所有IP地址
      local interface_ips=$(ifconfig "$interface" | grep "inet " | grep -v "127.0.0.1" | awk '{print $2}')

      # 优先选择192.168.1.x网段的IP
      for ip in $interface_ips; do
        if [[ "$ip" =~ ^192\.168\.1\. ]]; then
          log "从网卡 $interface 获取到优先IP地址: $ip" >&2
          echo "$ip"
          return 0
        fi
      done

      # 如果没有192.168.1.x，选择第一个可用IP
      ip_address=$(echo "$interface_ips" | head -n 1)
      if [ -n "$ip_address" ]; then
        log "从网卡 $interface 获取到IP地址: $ip_address" >&2
        echo "$ip_address"
        return 0
      fi
    fi
  done

  log "优先网卡未找到有效IP，尝试从所有网卡获取..." >&2

  # 获取所有网卡的IP地址，过滤掉不合适的
  local all_ips=$(ifconfig | grep "inet " | grep -v "127.0.0.1" | awk '{print $2}')

  for ip in $all_ips; do
    # 过滤掉链路本地地址 (169.254.x.x)
    if [[ "$ip" =~ ^169\.254\. ]]; then
      continue
    fi

    # 优先选择私有网络地址
    if [[ "$ip" =~ ^192\.168\. ]] || [[ "$ip" =~ ^10\. ]] || [[ "$ip" =~ ^172\.(1[6-9]|2[0-9]|3[0-1])\. ]]; then
      log "选择私有网络IP地址: $ip" >&2
      echo "$ip"
      return 0
    fi

    # 如果没有私有网络地址，记录第一个可用的公网IP
    if [ -z "$ip_address" ]; then
      ip_address="$ip"
    fi
  done

  # 如果有可用的公网IP，使用它
  if [ -n "$ip_address" ]; then
    log "使用公网IP地址: $ip_address" >&2
    echo "$ip_address"
    return 0
  fi

  # 如果都没有找到，返回空
  return 1
}

# 获取本机IP地址
log "开始获取本机IP地址..."
IP_ADDRESS=$(get_local_ip)

if [ -z "$IP_ADDRESS" ]; then
  log "错误：未能获取任何有效的IP地址，脚本终止"
  exit 1
fi

log "获取到IP地址: $IP_ADDRESS"

# 定义固定端口
PORT="48080"
NEW_URL="http://${IP_ADDRESS}:${PORT}"

log "新URL值: $NEW_URL"

# 检查文件中是否已存在SHOPRO_DEV_BASE_URL配置
if grep -q "SHOPRO_DEV_BASE_URL" "$FOUND_ENV_FILE"; then
  log "更新环境变量SHOPRO_DEV_BASE_URL..."
  # 获取当前的URL值（仅获取未被注释的行）
  CURRENT_URL=$(grep "SHOPRO_DEV_BASE_URL" "$FOUND_ENV_FILE" | grep -v "^#" | cut -d"=" -f2)
  
  if [ -n "$CURRENT_URL" ]; then
    log "当前URL值: $CURRENT_URL"
    
    # 直接替换为新URL（仅替换未被注释的行）
    sed -i '' -E "s|^(SHOPRO_DEV_BASE_URL=).*|\1${NEW_URL}|" "$FOUND_ENV_FILE"
  else
    log "未找到有效的SHOPRO_DEV_BASE_URL值，将添加新配置"
    echo "SHOPRO_DEV_BASE_URL=${NEW_URL}" >> "$FOUND_ENV_FILE"
  fi
else
  log "添加SHOPRO_DEV_BASE_URL配置..."
  echo "SHOPRO_DEV_BASE_URL=${NEW_URL}" >> "$FOUND_ENV_FILE"
fi

# 检查文件中是否已存在SHOPRO_BASE_URL配置
if grep -q "SHOPRO_BASE_URL" "$FOUND_ENV_FILE"; then
  log "更新环境变量SHOPRO_BASE_URL..."
  # 获取当前的URL值（仅获取未被注释的行）
  CURRENT_BASE_URL=$(grep "SHOPRO_BASE_URL" "$FOUND_ENV_FILE" | grep -v "^#" | cut -d"=" -f2)
  
  if [ -n "$CURRENT_BASE_URL" ]; then
    log "当前BASE URL值: $CURRENT_BASE_URL"
    
    # 直接替换为新URL（仅替换未被注释的行）
    sed -i '' -E "s|^(SHOPRO_BASE_URL=).*|\1${NEW_URL}|" "$FOUND_ENV_FILE"
  else
    log "未找到有效的SHOPRO_BASE_URL值，将添加新配置"
    echo "SHOPRO_BASE_URL=${NEW_URL}" >> "$FOUND_ENV_FILE"
  fi
else
  log "添加SHOPRO_BASE_URL配置..."
  echo "SHOPRO_BASE_URL=${NEW_URL}" >> "$FOUND_ENV_FILE"
fi

log "操作完成！SHOPRO_DEV_BASE_URL已更新为：${NEW_URL}"
log "配置文件位置: $(realpath "$FOUND_ENV_FILE")"
log "如需手动运行开发服务器，请执行: npm run dev" 