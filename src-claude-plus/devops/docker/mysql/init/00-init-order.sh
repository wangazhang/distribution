#!/bin/bash
echo '开始执行MySQL初始化脚本...'
chmod +x /docker-entrypoint-initdb.d/*.sh
echo '初始化完成!'
