#!/bin/bash
# 用途：本地下载文件并上传至服务器
# 使用方法：./download_and_upload.sh URL 服务器用户名@服务器IP [服务器目标路径]

# 参数检查
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "用法: $0 <下载URL> <服务器用户名@服务器IP> [服务器目标路径]"
    exit 1
fi

DOWNLOAD_URL="$1"
SERVER="$2"
REMOTE_PATH="${3:-/tmp/}"  # 默认上传到服务器/tmp目录

# 提取文件名
FILENAME=$(basename "$DOWNLOAD_URL")

# 下载文件
echo "正在下载 $DOWNLOAD_URL ..."
curl -o "$FILENAME" -L "$DOWNLOAD_URL" || { echo "下载失败"; exit 1; }

# 上传文件
echo "正在上传 $FILENAME 到 $SERVER:$REMOTE_PATH ..."
scp "$FILENAME" "$SERVER:$REMOTE_PATH" || { echo "上传失败"; exit 1; }

echo "操作完成！文件已上传至 $SERVER:$REMOTE_PATH$FILENAME"