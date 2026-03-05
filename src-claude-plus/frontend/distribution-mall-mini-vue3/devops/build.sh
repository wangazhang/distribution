#!/bin/bash

# 日志函数
log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# 检查参数
if [ "$#" -ne 1 ]; then
  log "错误：请提供环境参数（local、test 或 prod）"
  exit 1
fi

ENV_TYPE="$1"

if [ "$ENV_TYPE" != "local" ] && [ "$ENV_TYPE" != "test" ] && [ "$ENV_TYPE" != "prod" ]; then
  log "错误：无效的环境参数，必须是 local、test 或 prod"
  exit 1
fi

log "开始执行环境配置替换脚本..."

# 获取项目根目录路径
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

log "项目根目录: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

# 根据环境选择配置文件
if [ "$ENV_TYPE" == "local" ]; then
  ENV_FILE=".e.local"
elif [ "$ENV_TYPE" == "test" ]; then
  ENV_FILE=".e.test"
else
  ENV_FILE=".e.prod"
fi

# 检查环境配置文件是否存在
if [ ! -f "$ENV_FILE" ]; then
  log "错误：未找到环境文件: $ENV_FILE"
  exit 1
fi

# 复制找到的环境文件到.env
cp "$ENV_FILE" .env
log "已将 $ENV_FILE 复制到 .env"

# 如果使用的是.e.local，执行update-ip.sh
if [ "$ENV_FILE" == ".e.local" ]; then
  log "使用.e.local，执行update-ip.sh替换IP..."
  ./devops/update-ip.sh
fi

log "操作完成！" 