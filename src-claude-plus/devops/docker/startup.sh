#!/bin/bash

# 颜色变量
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # 无颜色

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# 显示标题
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}      hissp系统 Docker 启动脚本       ${NC}"
echo -e "${GREEN}======================================${NC}"

# 加载环境变量
if [ -f "$SCRIPT_DIR/docker.env" ]; then
  # 安全地加载环境变量，避免执行命令
  while IFS='=' read -r key value || [[ -n "$key" ]]; do
    # 忽略注释和空行
    [[ $key =~ ^#.*$ ]] || [[ -z "$key" ]] && continue
    # 移除双引号和单引号
    value=${value//\"/}
    value=${value//\'/}
    # 导出环境变量
    export "$key=$value"
  done < "$SCRIPT_DIR/docker.env"
  echo -e "${BLUE}已加载环境变量配置${NC}"
else
  echo -e "${YELLOW}警告: docker.env 文件不存在，将使用默认环境变量${NC}"
fi

# 检查docker和docker-compose是否安装
if ! command -v docker &> /dev/null; then
  echo "错误: docker未安装，请先安装docker"
  exit 1
fi

if ! command -v docker-compose &> /dev/null; then
  echo "错误: docker-compose未安装，请先安装docker-compose"
  exit 1
fi

# 定义要搜索的基础目录（相对于 src-claude-plus）
BASE_UI_DIR="$PROJECT_ROOT/frontend"
# 搜索基础目录的上一级目录
PARENT_DIR="$PROJECT_ROOT/.."

# 复制MySQL初始化脚本
copy_mysql_init_scripts() {
  echo -e "${BLUE}正在复制MySQL初始化脚本...${NC}"
  
  # 清除已有初始化目录内容
  if [ -d "$SCRIPT_DIR/mysql/init" ]; then
    echo -e "${YELLOW}清除已有MySQL初始化目录...${NC}"
    rm -rf "$SCRIPT_DIR/mysql/init/"*
  else
    mkdir -p "$SCRIPT_DIR/mysql/init"
  fi
  
  # 复制项目中的SQL脚本到MySQL初始化目录
  SQL_DIR="$PROJECT_ROOT/backend/hissp-distribution/sql/mysql"
  if [ -d "$SQL_DIR" ]; then
    echo -e "${BLUE}复制SQL脚本 ${SQL_DIR} 到 mysql/init/${NC}"
    
    # 创建初始化顺序脚本
    echo "#!/bin/bash" > "$SCRIPT_DIR/mysql/init/00-init-order.sh"
    echo "echo '开始执行MySQL初始化脚本...'" >> "$SCRIPT_DIR/mysql/init/00-init-order.sh"
    echo "chmod +x /docker-entrypoint-initdb.d/*.sh" >> "$SCRIPT_DIR/mysql/init/00-init-order.sh"
    echo "echo '初始化完成!'" >> "$SCRIPT_DIR/mysql/init/00-init-order.sh"
    chmod +x "$SCRIPT_DIR/mysql/init/00-init-order.sh"
    
    # 复制基础脚本
    if [ -f "$SQL_DIR/ruoyi-vue-pro.sql" ]; then
      cp "$SQL_DIR/ruoyi-vue-pro.sql" "$SCRIPT_DIR/mysql/init/01-schema.sql"
      echo -e "${GREEN}已复制主数据库脚本${NC}"
    fi
    
    # 复制Quartz脚本
    if [ -f "$SQL_DIR/quartz.sql" ]; then
      cp "$SQL_DIR/quartz.sql" "$SCRIPT_DIR/mysql/init/02-quartz.sql"
      echo -e "${GREEN}已复制Quartz脚本${NC}"
    fi
    
    # 复制其他模块脚本
    local count=3
    for sql_file in "$SQL_DIR"/*.sql; do
      # 跳过已复制的基础脚本
      if [[ "$sql_file" != *"ruoyi-vue-pro.sql"* && "$sql_file" != *"quartz.sql"* ]]; then
        cp "$sql_file" "$SCRIPT_DIR/mysql/init/0$count-$(basename "$sql_file")"
        echo -e "${GREEN}已复制 $(basename "$sql_file")${NC}"
        count=$((count+1))
      fi
    done
  else
    echo -e "${YELLOW}警告: 未找到SQL脚本目录 ${SQL_DIR}${NC}"
  fi
}

# 清除数据目录
clean_data() {
  echo -e "${YELLOW}警告: 此操作将删除所有数据!${NC}"
  read -p "确定要继续吗? [y/N]: " confirm
  
  if [[ "$confirm" == "y" || "$confirm" == "Y" ]]; then
    echo -e "${BLUE}正在停止所有服务...${NC}"
    cd "$SCRIPT_DIR" && docker-compose down
    
    echo -e "${BLUE}正在清除MySQL数据...${NC}"
    rm -rf "$SCRIPT_DIR/mysql/data/"*
    
    echo -e "${BLUE}正在清除Redis数据...${NC}"
    rm -rf "$SCRIPT_DIR/redis/data/"*

    echo -e "${BLUE}正在清除RocketMQ数据...${NC}"
    rm -rf ~/storage/distribution/rocketmq/*

    # 重新复制MySQL初始化脚本
    copy_mysql_init_scripts
    
    echo -e "${GREEN}所有数据已清除!${NC}"
  else
    echo -e "${BLUE}操作已取消${NC}"
  fi
}

# 初始化RocketMQ Broker配置
init_rocketmq_broker_config() {
  echo -e "${BLUE}正在初始化RocketMQ Broker配置...${NC}"

  # 获取本机IP地址
  LOCAL_IP=""

  # 方法1: 使用hostname命令
  if [ -z "$LOCAL_IP" ]; then
    LOCAL_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
  fi

  # 方法2: 使用ip route命令
  if [ -z "$LOCAL_IP" ]; then
    LOCAL_IP=$(ip route get 1 2>/dev/null | awk '{print $7}' | head -1)
  fi

  # 方法3: 使用ifconfig命令
  if [ -z "$LOCAL_IP" ]; then
    LOCAL_IP=$(ifconfig 2>/dev/null | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -1 | awk '{print $2}' | cut -d: -f2)
  fi

  # 方法4: 使用网络连接方式获取IP
  if [ -z "$LOCAL_IP" ]; then
    LOCAL_IP=$(python3 -c "import socket; s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM); s.connect(('8.8.8.8', 80)); print(s.getsockname()[0]); s.close()" 2>/dev/null)
  fi

  # 验证IP地址格式
  if [[ ! $LOCAL_IP =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    echo -e "${RED}错误: 无法获取有效的本机IP地址${NC}"
    echo -e "${YELLOW}将使用默认IP: 192.0.2.225${NC}"
    return 1
  fi

  echo -e "${GREEN}检测到本机IP地址: $LOCAL_IP${NC}"

  # 备份并替换broker.conf
  BROKER_CONF="$SCRIPT_DIR/rocketmq/conf/broker.conf"
  if [ -f "$BROKER_CONF" ]; then
    # 备份原文件（只在第一次备份时创建）
    if [ ! -f "$BROKER_CONF.original" ]; then
      cp "$BROKER_CONF" "$BROKER_CONF.original"
      echo -e "${BLUE}已创建broker.conf原始备份${NC}"
    fi

    # 替换brokerIP1配置
    sed -i.tmp "s/brokerIP1=.*/brokerIP1=$LOCAL_IP/" "$BROKER_CONF"
    rm -f "$BROKER_CONF.tmp"

    # 验证替换是否成功
    if grep -q "brokerIP1=$LOCAL_IP" "$BROKER_CONF"; then
      echo -e "${GREEN}✓ 已更新RocketMQ Broker配置: brokerIP1=$LOCAL_IP${NC}"
    else
      echo -e "${RED}✗ 更新RocketMQ Broker配置失败${NC}"
      return 1
    fi
  else
    echo -e "${YELLOW}警告: 未找到broker.conf文件: $BROKER_CONF${NC}"
    return 1
  fi
}

# 显示当前Broker IP配置
show_current_broker_ip() {
  echo -e "${BLUE}======================================${NC}"
  echo -e "${BLUE}      当前RocketMQ Broker IP配置      ${NC}"
  echo -e "${BLUE}======================================${NC}"

  BROKER_CONF="$SCRIPT_DIR/rocketmq/conf/broker.conf"
  if [ -f "$BROKER_CONF" ]; then
    current_ip=$(grep "brokerIP1=" "$BROKER_CONF" | cut -d'=' -f2)
    echo -e "${GREEN}当前配置的brokerIP1: $current_ip${NC}"

    # 显示是否有备份文件
    if [ -f "$BROKER_CONF.original" ]; then
      original_ip=$(grep "brokerIP1=" "$BROKER_CONF.original" | cut -d'=' -f2)
      echo -e "${YELLOW}原始配置的brokerIP1: $original_ip${NC}"
    else
      echo -e "${YELLOW}未找到原始配置备份${NC}"
    fi
  else
    echo -e "${RED}错误: 未找到broker.conf文件${NC}"
  fi
  echo -e "${BLUE}======================================${NC}"
}

# 手动设置Broker IP
set_manual_broker_ip() {
  echo -e "${BLUE}======================================${NC}"
  echo -e "${BLUE}      手动设置RocketMQ Broker IP      ${NC}"
  echo -e "${BLUE}======================================${NC}"

  # 显示当前配置
  show_current_broker_ip

  echo -e "\n${YELLOW}请输入新的IP地址 (格式: xxx.xxx.xxx.xxx):${NC}"
  read -r manual_ip

  # 验证IP地址格式
  if [[ ! $manual_ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    echo -e "${RED}错误: IP地址格式不正确${NC}"
    return 1
  fi

  # 验证IP地址范围
  IFS='.' read -ra ADDR <<< "$manual_ip"
  for i in "${ADDR[@]}"; do
    if [ "$i" -gt 255 ]; then
      echo -e "${RED}错误: IP地址范围不正确 (0-255)${NC}"
      return 1
    fi
  done

  BROKER_CONF="$SCRIPT_DIR/rocketmq/conf/broker.conf"
  if [ -f "$BROKER_CONF" ]; then
    # 备份原文件（只在第一次备份时创建）
    if [ ! -f "$BROKER_CONF.original" ]; then
      cp "$BROKER_CONF" "$BROKER_CONF.original"
      echo -e "${BLUE}已创建broker.conf原始备份${NC}"
    fi

    # 替换brokerIP1配置
    sed -i.tmp "s/brokerIP1=.*/brokerIP1=$manual_ip/" "$BROKER_CONF"
    rm -f "$BROKER_CONF.tmp"

    # 验证替换是否成功
    if grep -q "brokerIP1=$manual_ip" "$BROKER_CONF"; then
      echo -e "${GREEN}✓ 已手动设置RocketMQ Broker IP: $manual_ip${NC}"
    else
      echo -e "${RED}✗ 设置RocketMQ Broker IP失败${NC}"
      return 1
    fi
  else
    echo -e "${RED}错误: 未找到broker.conf文件${NC}"
    return 1
  fi
}

# 恢复原始Broker配置
restore_original_broker_config() {
  echo -e "${BLUE}======================================${NC}"
  echo -e "${BLUE}      恢复原始RocketMQ Broker配置     ${NC}"
  echo -e "${BLUE}======================================${NC}"

  BROKER_CONF="$SCRIPT_DIR/rocketmq/conf/broker.conf"

  if [ -f "$BROKER_CONF.original" ]; then
    # 显示当前和原始配置
    current_ip=$(grep "brokerIP1=" "$BROKER_CONF" | cut -d'=' -f2)
    original_ip=$(grep "brokerIP1=" "$BROKER_CONF.original" | cut -d'=' -f2)

    echo -e "${YELLOW}当前配置: brokerIP1=$current_ip${NC}"
    echo -e "${YELLOW}原始配置: brokerIP1=$original_ip${NC}"

    echo -e "\n${YELLOW}确定要恢复到原始配置吗? [y/N]:${NC}"
    read -r confirm

    if [[ "$confirm" == "y" || "$confirm" == "Y" ]]; then
      cp "$BROKER_CONF.original" "$BROKER_CONF"
      echo -e "${GREEN}✓ 已恢复原始RocketMQ Broker配置${NC}"
    else
      echo -e "${BLUE}操作已取消${NC}"
    fi
  else
    echo -e "${RED}错误: 未找到原始配置备份文件${NC}"
    echo -e "${YELLOW}提示: 原始备份文件会在首次修改配置时自动创建${NC}"
  fi
}

# 管理Broker IP配置
manage_broker_ip_config() {
  while true; do
    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}    RocketMQ Broker IP 配置管理       ${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}请选择操作:${NC}"
    echo "1. 查看当前IP配置"
    echo "2. 自动检测并更新IP"
    echo "3. 手动设置IP地址"
    echo "4. 恢复原始配置"
    echo "0. 返回主菜单"

    read -p "请输入选项 [0-4]: " ip_choice

    case $ip_choice in
      1)
        show_current_broker_ip
        ;;
      2)
        init_rocketmq_broker_config
        ;;
      3)
        set_manual_broker_ip
        ;;
      4)
        restore_original_broker_config
        ;;
      0)
        echo -e "${BLUE}返回主菜单${NC}"
        break
        ;;
      *)
        echo -e "${YELLOW}无效的选项，请重新选择${NC}"
        ;;
    esac

    if [ "$ip_choice" != "0" ]; then
      echo -e "\n${YELLOW}按回车键继续...${NC}"
      read -r
    fi
  done
}

# 管理frp客户端连接
manage_frpc_connection() {
  # frpc脚本路径
  FRPC_SCRIPT="$PROJECT_ROOT/devops/tools/frpc/start_frpc.sh"

  # 检查frpc脚本是否存在
  if [ ! -f "$FRPC_SCRIPT" ]; then
    echo -e "${RED}错误: frpc管理脚本不存在: $FRPC_SCRIPT${NC}"
    echo -e "${YELLOW}请确认frpc工具是否已正确安装${NC}"
    return 1
  fi

  # 检查脚本执行权限
  if [ ! -x "$FRPC_SCRIPT" ]; then
    echo -e "${YELLOW}正在为frpc脚本添加执行权限...${NC}"
    chmod +x "$FRPC_SCRIPT"
  fi

  while true; do
    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}        frp 客户端连接管理            ${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}请选择操作:${NC}"
    echo "1. 启动 frp 客户端连接"
    echo "2. 停止 frp 客户端连接"
    echo "3. 重启 frp 客户端连接"
    echo "4. 查看 frp 客户端状态"
    echo "5. 查看 frp 客户端日志"
    echo "0. 返回主菜单"

    read -p "请输入选项 [0-5]: " frpc_choice

    case $frpc_choice in
      1)
        echo -e "${BLUE}正在启动 frp 客户端连接...${NC}"
        "$FRPC_SCRIPT" start
        ;;
      2)
        echo -e "${BLUE}正在停止 frp 客户端连接...${NC}"
        "$FRPC_SCRIPT" stop
        ;;
      3)
        echo -e "${BLUE}正在重启 frp 客户端连接...${NC}"
        "$FRPC_SCRIPT" restart
        ;;
      4)
        echo -e "${BLUE}查看 frp 客户端状态:${NC}"
        "$FRPC_SCRIPT" status
        ;;
      5)
        echo -e "${BLUE}查看 frp 客户端日志:${NC}"
        echo -e "${YELLOW}提示: 按 Ctrl+C 退出日志查看${NC}"
        echo -e "${YELLOW}按回车键继续...${NC}"
        read -r
        "$FRPC_SCRIPT" logs
        ;;
      0)
        echo -e "${BLUE}返回主菜单${NC}"
        break
        ;;
      *)
        echo -e "${YELLOW}无效的选项，请重新选择${NC}"
        ;;
    esac

    if [ "$frpc_choice" != "0" ] && [ "$frpc_choice" != "5" ]; then
      echo -e "\n${YELLOW}按回车键继续...${NC}"
      read -r
    fi
  done
}

# 动态检测前端项目
detect_frontend_projects() {
  echo -e "${BLUE}正在检测前端项目...${NC}"
  
  # 清空数组
  frontend_paths=()
  frontend_names=()
  
  # 搜索所有可能的前端项目目录
  potential_dirs=()
  
  # 检查基础UI目录是否存在
  if [ -d "$BASE_UI_DIR" ]; then
    # 在基础UI目录中查找
    for dir in "$BASE_UI_DIR"/*; do
      if [ -d "$dir" ]; then
        potential_dirs+=("$dir")
      fi
    done
  fi
  
  # 在上级目录中查找名称包含ui或front的目录
  for dir in "$PARENT_DIR"/*; do
    if [ -d "$dir" ] && [[ "$(basename "$dir")" =~ ui|front|web ]]; then
      potential_dirs+=("$dir")
    fi
  done
  
  # 检查每个潜在目录是否是前端项目
  for dir in "${potential_dirs[@]}"; do
    if [ -f "$dir/package.json" ]; then
      # 尝试从package.json中获取项目名称
      if command -v jq &> /dev/null; then
        project_name=$(jq -r '.name' "$dir/package.json" 2>/dev/null)
      else
        # 如果没有jq，用grep简单提取名称
        project_name=$(grep -m 1 '"name"' "$dir/package.json" | cut -d '"' -f 4 2>/dev/null)
      fi
      
      # 如果无法获取名称，则使用目录名
      if [ -z "$project_name" ] || [ "$project_name" == "null" ]; then
        project_name=$(basename "$dir")
      fi
      
      # 添加到数组
      frontend_paths+=("$dir")
      frontend_names+=("$project_name")
      
      echo -e "${GREEN}已检测到前端项目: $project_name${NC}"
    fi
  done
  
  # 如果没有找到前端项目，提供默认值
  if [ ${#frontend_paths[@]} -eq 0 ]; then
    echo -e "${YELLOW}警告: 未检测到前端项目，将使用默认配置${NC}"
    frontend_paths=("$PROJECT_ROOT/frontend/yudao-ui-admin-vue3")
    frontend_names=("distribution管理系统(Vue3版)")
  fi
}

# 构建指定的服务
build_services() {
  services=$1
  echo -e "${BLUE}正在构建服务: ${services}${NC}"
  cd "$SCRIPT_DIR" && docker-compose build ${services}
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}服务构建成功!${NC}"
  else
    echo -e "${YELLOW}服务构建失败，请检查日志${NC}"
    exit 1
  fi
}

# 启动指定的服务
start_services() {
  services=$1
  echo -e "${BLUE}正在启动服务: ${services}${NC}"
  cd "$SCRIPT_DIR" && docker-compose up -d ${services}
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}服务启动成功!${NC}"
  else
    echo -e "${YELLOW}服务启动失败，请检查日志${NC}"
    exit 1
  fi
}

# 停止所有服务
stop_all() {
  echo -e "${BLUE}正在停止所有服务...${NC}"
  cd "$SCRIPT_DIR" && docker-compose down
  echo -e "${GREEN}所有服务已停止${NC}"
}

# 查看服务状态
check_status() {
  echo -e "${BLUE}当前服务状态:${NC}"
  cd "$SCRIPT_DIR" && docker-compose ps
}

# 选择前端项目并启动所有服务
start_all_with_frontend() {
  # 复制MySQL初始化脚本
  copy_mysql_init_scripts

  # 先检测前端项目
  detect_frontend_projects

  if [ ${#frontend_paths[@]} -eq 0 ]; then
    echo -e "${YELLOW}错误: 未检测到有效的前端项目${NC}"
    return
  fi

  echo -e "${BLUE}请选择要启动的前端项目:${NC}"

  # 显示所有前端项目
  for i in "${!frontend_names[@]}"; do
    echo -e "$((i+1)). ${frontend_names[$i]} (${frontend_paths[$i]})"
  done
  echo -e "0. 返回主菜单"

  read -p "请输入选项 [0-${#frontend_names[@]}]: " frontend_choice

  if [ "$frontend_choice" = "0" ]; then
    return
  fi

  # 检查输入是否有效
  if [ "$frontend_choice" -ge 1 ] && [ "$frontend_choice" -le "${#frontend_names[@]}" ]; then
    index=$((frontend_choice-1))
    path="${frontend_paths[$index]}"
    name="${frontend_names[$index]}"

    echo -e "${GREEN}已选择: $name${NC}"

    # 设置环境变量
    export FRONTEND_PROJECT_PATH=$path

    # 初始化RocketMQ配置
    init_rocketmq_broker_config

    # 构建并启动所有服务
    echo -e "${BLUE}正在构建并启动所有服务...${NC}"
    build_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server admin"
    start_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server admin"

    if [ $? -eq 0 ]; then
      echo -e "${GREEN}所有服务启动成功!${NC}"
      echo -e "${GREEN}前端访问地址: http://localhost:8080${NC}"
      echo -e "${GREEN}后端API地址: http://distribution-server:48080${NC}"
      echo -e "${GREEN}RocketMQ控制台: http://localhost:48081${NC}"
    else
      echo -e "${YELLOW}服务启动失败，请检查日志${NC}"
    fi
  else
    echo -e "${YELLOW}无效的选项，请重新选择${NC}"
    start_all_with_frontend
  fi
}

# 自动选择前端项目并启动所有服务（用于批量模式）
start_all_with_frontend_auto() {
  # 复制MySQL初始化脚本
  copy_mysql_init_scripts

  # 先检测前端项目
  detect_frontend_projects

  if [ ${#frontend_paths[@]} -eq 0 ]; then
    echo -e "${YELLOW}错误: 未检测到有效的前端项目${NC}"
    return 1
  fi

  # 自动选择第一个前端项目
  path="${frontend_paths[0]}"
  name="${frontend_names[0]}"

  echo -e "${GREEN}批量模式: 自动选择前端项目: $name${NC}"

  # 设置环境变量
  export FRONTEND_PROJECT_PATH=$path

  # 初始化RocketMQ配置
  init_rocketmq_broker_config

  # 构建并启动所有服务
  echo -e "${BLUE}正在构建并启动所有服务...${NC}"
  build_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server admin"
  start_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server admin"

  if [ $? -eq 0 ]; then
    echo -e "${GREEN}所有服务启动成功!${NC}"
    echo -e "${GREEN}前端访问地址: http://localhost:8080${NC}"
    echo -e "${GREEN}后端API地址: http://distribution-server:48080${NC}"
    echo -e "${GREEN}RocketMQ控制台: http://localhost:48081${NC}"
    return 0
  else
    echo -e "${YELLOW}服务启动失败，请检查日志${NC}"
    return 1
  fi
}

# 自动清除数据（用于批量模式）
clean_data_auto() {
  echo -e "${YELLOW}批量模式: 自动清除所有数据${NC}"

  echo -e "${BLUE}正在停止所有服务...${NC}"
  cd "$SCRIPT_DIR" && docker-compose down

  echo -e "${BLUE}正在清除MySQL数据...${NC}"
  rm -rf "$SCRIPT_DIR/mysql/data/"*

  echo -e "${BLUE}正在清除Redis数据...${NC}"
  rm -rf "$SCRIPT_DIR/redis/data/"*

  echo -e "${BLUE}正在清除RocketMQ数据...${NC}"
  rm -rf ~/storage/distribution/rocketmq/*

  # 重新复制MySQL初始化脚本
  copy_mysql_init_scripts

  echo -e "${GREEN}所有数据已清除!${NC}"
  return 0
}

# 执行frp客户端操作
execute_frpc_action() {
  local action=$1

  # frpc脚本路径
  FRPC_SCRIPT="$PROJECT_ROOT/devops/tools/frpc/start_frpc.sh"

  # 检查frpc脚本是否存在
  if [ ! -f "$FRPC_SCRIPT" ]; then
    echo -e "${RED}错误: frpc管理脚本不存在: $FRPC_SCRIPT${NC}"
    echo -e "${YELLOW}请确认frpc工具是否已正确安装${NC}"
    return 1
  fi

  # 检查脚本执行权限
  if [ ! -x "$FRPC_SCRIPT" ]; then
    echo -e "${YELLOW}正在为frpc脚本添加执行权限...${NC}"
    chmod +x "$FRPC_SCRIPT"
  fi

  case $action in
    start)
      echo -e "${BLUE}正在启动 frp 客户端连接...${NC}"
      "$FRPC_SCRIPT" start
      ;;
    stop)
      echo -e "${BLUE}正在停止 frp 客户端连接...${NC}"
      "$FRPC_SCRIPT" stop
      ;;
    restart)
      echo -e "${BLUE}正在重启 frp 客户端连接...${NC}"
      "$FRPC_SCRIPT" restart
      ;;
    status)
      echo -e "${BLUE}查看 frp 客户端状态:${NC}"
      "$FRPC_SCRIPT" status
      ;;
    logs)
      echo -e "${BLUE}查看 frp 客户端日志:${NC}"
      "$FRPC_SCRIPT" logs
      ;;
    *)
      echo -e "${RED}错误: 无效的frp操作: $action${NC}"
      return 1
      ;;
  esac

  return $?
}

# 执行上下文相关的操作
execute_contextual_operation() {
  local operation=$1
  local context="${operation%%:*}"
  local choice="${operation##*:}"

  case $context in
    "main")
      case $choice in
        1) start_all_with_frontend_auto ;;
        2)
          copy_mysql_init_scripts
          init_rocketmq_broker_config
          build_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console"
          start_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console"
          echo -e "${GREEN}基础设施启动成功!${NC}"
          echo -e "${GREEN}RocketMQ控制台: http://localhost:48081${NC}"
          ;;
        3)
          copy_mysql_init_scripts
          init_rocketmq_broker_config
          build_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server"
          start_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server"
          echo -e "${GREEN}基础设施和后端启动成功!${NC}"
          echo -e "${GREEN}后端API地址: http://localhost:48080${NC}"
          echo -e "${GREEN}RocketMQ控制台: http://localhost:48081${NC}"
          ;;
        4) check_status ;;
        5) stop_all ;;
        6) clean_data_auto ;;
        7) echo -e "${BLUE}进入RocketMQ Broker IP配置管理菜单${NC}" ;;
        8) echo -e "${BLUE}进入frp客户端连接管理菜单${NC}" ;;
        0) echo -e "${YELLOW}批量模式: 跳过退出操作${NC}" ;;
        *) echo -e "${RED}主菜单无效选项: $choice${NC}"; return 1 ;;
      esac
      ;;
    "rocketmq")
      case $choice in
        1) show_current_broker_ip ;;
        2) init_rocketmq_broker_config ;;
        3) echo -e "${YELLOW}批量模式: 跳过手动设置IP（需要交互输入）${NC}" ;;
        4) restore_original_broker_config ;;
        0) echo -e "${BLUE}返回主菜单${NC}" ;;
        *) echo -e "${RED}RocketMQ菜单无效选项: $choice${NC}"; return 1 ;;
      esac
      ;;
    "frp")
      case $choice in
        1) execute_frpc_action "start" ;;
        2) execute_frpc_action "stop" ;;
        3) execute_frpc_action "restart" ;;
        4) execute_frpc_action "status" ;;
        5) execute_frpc_action "logs" ;;
        0) echo -e "${BLUE}返回主菜单${NC}" ;;
        *) echo -e "${RED}frp菜单无效选项: $choice${NC}"; return 1 ;;
      esac
      ;;
    "error")
      echo -e "${RED}错误操作: $choice${NC}"
      return 1
      ;;
    *)
      echo -e "${RED}未知上下文: $context${NC}"
      return 1
      ;;
  esac

  return 0
}

# 执行单个选择的操作
execute_single_choice() {
  local choice=$1
  local batch_mode=${2:-false}

  case $choice in
    1)
      if [ "$batch_mode" = "true" ]; then
        # 批量模式下自动选择第一个前端项目
        start_all_with_frontend_auto
      else
        start_all_with_frontend
      fi
      ;;
    2)
      # 复制MySQL初始化脚本
      copy_mysql_init_scripts
      # 初始化RocketMQ配置
      init_rocketmq_broker_config
      # 启动基础设施
      build_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console"
      start_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console"
      echo -e "${GREEN}基础设施启动成功!${NC}"
      echo -e "${GREEN}RocketMQ控制台: http://localhost:48081${NC}"
      ;;
    3)
      # 复制MySQL初始化脚本
      copy_mysql_init_scripts
      # 初始化RocketMQ配置
      init_rocketmq_broker_config
      # 启动基础设施和后端
      build_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server"
      start_services "mysql redis rocketmq-nameserver rocketmq-broker rocketmq-console server"
      echo -e "${GREEN}基础设施和后端启动成功!${NC}"
      echo -e "${GREEN}后端API地址: http://localhost:48080${NC}"
      echo -e "${GREEN}RocketMQ控制台: http://localhost:48081${NC}"
      ;;
    4)
      check_status
      ;;
    5)
      stop_all
      ;;
    6)
      if [ "$batch_mode" = "true" ]; then
        # 批量模式下自动确认清除数据
        clean_data_auto
      else
        clean_data
      fi
      ;;
    7)
      if [ "$batch_mode" = "true" ]; then
        # 批量模式下自动初始化RocketMQ配置
        init_rocketmq_broker_config
      else
        manage_broker_ip_config
      fi
      ;;
    8)
      if [ "$batch_mode" = "true" ]; then
        echo -e "${YELLOW}批量模式下跳过frp客户端管理（需要交互操作）${NC}"
      else
        manage_frpc_connection
      fi
      ;;
    0)
      if [ "$batch_mode" = "true" ]; then
        # 批量模式下忽略退出操作
        echo -e "${YELLOW}批量模式: 跳过退出操作${NC}"
        return 0
      else
        echo -e "${GREEN}感谢使用，再见!${NC}"
        exit 0
      fi
      ;;
    *)
      echo -e "${YELLOW}无效的选项: $choice${NC}"
      return 1
      ;;
  esac

  return 0
}

# 上下文相关的批量命令解析和执行
parse_and_execute_batch() {
  local input=$1
  local auto_confirm=${2:-false}

  # 将输入按"-"分割成数组
  IFS='-' read -ra choices <<< "$input"

  # 状态定义
  local MAIN_MENU="main"
  local ROCKETMQ_MENU="rocketmq"
  local FRP_MENU="frp"

  # 初始状态
  local current_state="$MAIN_MENU"

  # 解析操作序列
  local operations=()
  local descriptions=()

  for choice in "${choices[@]}"; do
    case $current_state in
      "$MAIN_MENU")
        case $choice in
          1)
            operations+=("main:1")
            descriptions+=("启动所有服务 (MySQL, Redis, RocketMQ, 后端, 前端)")
            ;;
          2)
            operations+=("main:2")
            descriptions+=("仅启动基础设施 (MySQL, Redis, RocketMQ)")
            ;;
          3)
            operations+=("main:3")
            descriptions+=("启动基础设施和后端 (MySQL, Redis, RocketMQ, 后端)")
            ;;
          4)
            operations+=("main:4")
            descriptions+=("查看服务状态")
            ;;
          5)
            operations+=("main:5")
            descriptions+=("停止所有服务")
            ;;
          6)
            operations+=("main:6")
            descriptions+=("清除所有数据 (MySQL, Redis, RocketMQ)")
            ;;
          7)
            operations+=("main:7")
            descriptions+=("进入RocketMQ Broker IP配置管理")
            current_state="$ROCKETMQ_MENU"
            ;;
          8)
            operations+=("main:8")
            descriptions+=("进入frp客户端连接管理")
            current_state="$FRP_MENU"
            ;;
          0)
            operations+=("main:0")
            descriptions+=("跳过退出操作（批量模式）")
            ;;
          *)
            operations+=("error:$choice")
            descriptions+=("${RED}主菜单无效选项: $choice${NC}")
            ;;
        esac
        ;;
      "$ROCKETMQ_MENU")
        case $choice in
          1)
            operations+=("rocketmq:1")
            descriptions+=("查看当前RocketMQ Broker IP配置")
            ;;
          2)
            operations+=("rocketmq:2")
            descriptions+=("自动检测并更新RocketMQ Broker IP")
            ;;
          3)
            operations+=("rocketmq:3")
            descriptions+=("手动设置RocketMQ Broker IP地址")
            ;;
          4)
            operations+=("rocketmq:4")
            descriptions+=("恢复原始RocketMQ Broker配置")
            ;;
          0)
            operations+=("rocketmq:0")
            descriptions+=("返回主菜单")
            current_state="$MAIN_MENU"
            ;;
          *)
            operations+=("error:$choice")
            descriptions+=("${RED}RocketMQ菜单无效选项: $choice${NC}")
            ;;
        esac
        ;;
      "$FRP_MENU")
        case $choice in
          1)
            operations+=("frp:1")
            descriptions+=("启动 frp 客户端连接")
            ;;
          2)
            operations+=("frp:2")
            descriptions+=("停止 frp 客户端连接")
            ;;
          3)
            operations+=("frp:3")
            descriptions+=("重启 frp 客户端连接")
            ;;
          4)
            operations+=("frp:4")
            descriptions+=("查看 frp 客户端状态")
            ;;
          5)
            operations+=("frp:5")
            descriptions+=("查看 frp 客户端日志")
            ;;
          0)
            operations+=("frp:0")
            descriptions+=("返回主菜单")
            current_state="$MAIN_MENU"
            ;;
          *)
            operations+=("error:$choice")
            descriptions+=("${RED}frp菜单无效选项: $choice${NC}")
            ;;
        esac
        ;;
    esac
  done

  echo -e "${BLUE}======================================${NC}"
  echo -e "${BLUE}        上下文批量执行模式            ${NC}"
  echo -e "${BLUE}======================================${NC}"
  echo -e "${YELLOW}将按顺序执行以下操作:${NC}"

  # 显示将要执行的操作
  for i in "${!descriptions[@]}"; do
    echo -e "$((i+1)). ${descriptions[$i]}"
  done

  if [ "$auto_confirm" = "true" ]; then
    echo -e "\n${BLUE}命令行模式: 自动开始执行...${NC}"
  else
    echo -e "\n${YELLOW}按回车键开始执行，或输入 'n' 取消:${NC}"
    read -r confirm

    if [[ "$confirm" == "n" || "$confirm" == "N" ]]; then
      echo -e "${BLUE}批量执行已取消${NC}"
      return 0
    fi
  fi

  echo -e "${BLUE}开始上下文批量执行...${NC}"

  # 执行操作序列
  for i in "${!operations[@]}"; do
    operation="${operations[$i]}"
    description="${descriptions[$i]}"

    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}执行步骤 $((i+1))/${#operations[@]}: $description${NC}"
    echo -e "${BLUE}======================================${NC}"

    if execute_contextual_operation "$operation"; then
      echo -e "${GREEN}✓ 步骤 $((i+1)) 执行成功${NC}"
    else
      echo -e "${RED}✗ 步骤 $((i+1)) 执行失败${NC}"
    fi

    # 在步骤之间添加短暂延迟
    if [ $((i+1)) -lt ${#operations[@]} ]; then
      echo -e "${YELLOW}等待 2 秒后继续下一步...${NC}"
      sleep 2
    fi
  done

  echo -e "\n${GREEN}======================================${NC}"
  echo -e "${GREEN}        上下文批量执行完成            ${NC}"
  echo -e "${GREEN}======================================${NC}"
}

# 主菜单
show_menu() {
  echo -e "\n${BLUE}请选择操作:${NC}"
  echo "1. 启动所有服务 (MySQL, Redis, RocketMQ, 后端, 前端)"
  echo "2. 仅启动基础设施 (MySQL, Redis, RocketMQ)"
  echo "3. 启动基础设施和后端 (MySQL, Redis, RocketMQ, 后端)"
  echo "4. 查看服务状态"
  echo "5. 停止所有服务"
  echo "6. 清除所有数据 (MySQL, Redis, RocketMQ)"
  echo "7. 管理RocketMQ Broker IP配置"
  echo "8. 管理frp客户端连接"
  echo "0. 退出"
  echo -e "${YELLOW}提示: 支持上下文相关的拆解拼装执行${NC}"
  echo -e "${YELLOW}示例: 8-3-0-7-2-0-2 (进入frp菜单->重启->返回->进入RocketMQ菜单->自动配置->返回->启动基础设施)${NC}"
  echo -e "${YELLOW}RocketMQ子菜单: 1(查看IP) 2(自动配置) 3(手动设置) 4(恢复) 0(返回)${NC}"
  echo -e "${YELLOW}frp子菜单: 1(启动) 2(停止) 3(重启) 4(状态) 5(日志) 0(返回)${NC}"

  read -p "请输入选项或上下文批量选项: " choice

  # 检查是否为批量输入
  if [[ "$choice" == *"-"* ]]; then
    parse_and_execute_batch "$choice" "false"
  else
    # 单个选项处理
    execute_single_choice "$choice" "false"
  fi

  # 如果不是退出命令，继续显示菜单
  if [ "$choice" != "0" ]; then
    show_menu
  fi
}

# 添加执行权限
chmod +x "$(realpath "$0")"

# 进入脚本目录
cd "$SCRIPT_DIR"

# 显示帮助信息
show_help() {
  echo -e "${GREEN}======================================${NC}"
  echo -e "${GREEN}      hissp系统 Docker 启动脚本       ${NC}"
  echo -e "${GREEN}======================================${NC}"
  echo -e "${BLUE}使用方法:${NC}"
  echo -e "  $0                    # 交互式菜单模式"
  echo -e "  $0 [选项序列]         # 命令行批量执行模式"
  echo -e "  $0 --help|-h          # 显示此帮助信息"
  echo
  echo -e "${BLUE}选项序列语法:${NC}"
  echo -e "  单个选项: 1, 2, 3, 4, 5, 6, 7, 8, 0"
  echo -e "  批量选项: 用'-'分隔的数字序列，支持上下文相关解析"
  echo
  echo -e "${BLUE}示例:${NC}"
  echo -e "  $0 2                  # 启动基础设施"
  echo -e "  $0 8-3-0              # 进入frp菜单->重启frp->返回主菜单"
  echo -e "  $0 7-2-0-2            # 进入RocketMQ菜单->自动配置IP->返回->启动基础设施"
  echo -e "  $0 8-3-0-7-2-0-2-4    # 复杂操作序列"
  echo
  echo -e "${BLUE}主菜单选项:${NC}"
  echo -e "  1 - 启动所有服务 (MySQL, Redis, RocketMQ, 后端, 前端)"
  echo -e "  2 - 仅启动基础设施 (MySQL, Redis, RocketMQ)"
  echo -e "  3 - 启动基础设施和后端 (MySQL, Redis, RocketMQ, 后端)"
  echo -e "  4 - 查看服务状态"
  echo -e "  5 - 停止所有服务"
  echo -e "  6 - 清除所有数据 (MySQL, Redis, RocketMQ)"
  echo -e "  7 - 进入RocketMQ Broker IP配置管理菜单"
  echo -e "  8 - 进入frp客户端连接管理菜单"
  echo -e "  0 - 退出（批量模式下跳过）"
  echo
  echo -e "${BLUE}RocketMQ子菜单选项（在选择7后）:${NC}"
  echo -e "  1 - 查看当前IP配置"
  echo -e "  2 - 自动检测并更新IP"
  echo -e "  3 - 手动设置IP地址（批量模式下跳过）"
  echo -e "  4 - 恢复原始配置"
  echo -e "  0 - 返回主菜单"
  echo
  echo -e "${BLUE}frp子菜单选项（在选择8后）:${NC}"
  echo -e "  1 - 启动frp客户端"
  echo -e "  2 - 停止frp客户端"
  echo -e "  3 - 重启frp客户端"
  echo -e "  4 - 查看frp状态"
  echo -e "  5 - 查看frp日志"
  echo -e "  0 - 返回主菜单"
  echo
}

# 检查是否有命令行参数
if [ $# -gt 0 ]; then
  # 检查是否为帮助参数
  if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_help
    exit 0
  fi

  # 有命令行参数，直接执行批量操作
  batch_input="$1"
  echo -e "${GREEN}======================================${NC}"
  echo -e "${GREEN}      hissp系统 Docker 启动脚本       ${NC}"
  echo -e "${GREEN}======================================${NC}"
  echo -e "${BLUE}命令行模式: 执行批量操作 '$batch_input'${NC}"

  # 检查是否为批量输入
  if [[ "$batch_input" == *"-"* ]]; then
    parse_and_execute_batch "$batch_input" "true"
  else
    # 单个选项处理
    execute_single_choice "$batch_input" "true"
  fi

  echo -e "${GREEN}命令行批量执行完成!${NC}"
else
  # 没有命令行参数，显示交互式菜单
  show_menu
fi
