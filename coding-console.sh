#!/bin/bash

# Shell脚本用于设置和启动AI工作站
# 提供交互式菜单来执行特定任务

# --- 通用函数 ---

# 检查命令是否存在的函数
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 彩色输出函数
print_step() {
    echo -e "\n\033[1;34m📋 $1\033[0m"
}

print_success() {
    echo -e "\033[1;32m✅ $1\033[0m"
}

print_error() {
    echo -e "\033[1;31m❌ $1\033[0m"
}

print_warning() {
    echo -e "\033[1;33m⚠️  $1\033[0m"
}

# 多列显示菜单项（每行4列）
display_menu_items_columns() {
    local items=("$@")
    local columns=4
    local total=${#items[@]}
    local rows=$(( (total + columns - 1) / columns ))
    
    for ((row=0; row<rows; row++)); do
        local line_parts=()
        for ((col=0; col<columns; col++)); do
            local idx=$((row + col * rows))
            if [ $idx -lt $total ]; then
                # 格式化每个菜单项为固定宽度
                local item="${items[idx]}"
                # 计算需要的空格来对齐
                local padding=""
                local item_len=${#item}
                local max_len=28
                if [ $item_len -lt $max_len ]; then
                    local spaces=$((max_len - item_len))
                    padding=$(printf "%${spaces}s" "")
                fi
                line_parts+=("${item}${padding}")
            else
                line_parts+=("$(printf '%28s' '')")
            fi
        done
        echo "  $(printf '%s  ' "${line_parts[@]}")"
    done
}

# 多列显示MCP项（每行4列）
display_mcp_items_columns() {
    local items=("$@")
    local columns=4
    local total=${#items[@]}
    if [ $total -eq 0 ]; then
        return
    fi
    local rows=$(( (total + columns - 1) / columns ))
    
    for ((row=0; row<rows; row++)); do
        local line=""
        for ((col=0; col<columns; col++)); do
            local idx=$((row + col * rows))
            if [ $idx -lt $total ]; then
                local item="${items[idx]}"
                # MCP项需要更宽的显示空间
                local item_len=${#item}
                local max_len=32
                if [ $item_len -lt $max_len ]; then
                    local spaces=$((max_len - item_len))
                    line="${line}${item}$(printf "%${spaces}s" "")"
                else
                    # 如果超过最大长度，截断或保持原样
                    line="${line}${item:0:$max_len}"
                fi
            else
                line="${line}$(printf '%32s' '')"
            fi
            # 列之间添加两个空格分隔
            if [ $col -lt $((columns - 1)) ]; then
                line="${line}  "
            fi
        done
        echo "  ${line}"
    done
}

# --- 核心功能函数 ---

# --- MCP管理功能 ---

# MCP配置文件管理（使用正确的Claude Code配置位置）
init_mcp_config() {
    # Claude Code的正确配置文件位置
    MCP_GLOBAL_CONFIG="$HOME/.claude.json"
    MCP_PROJECT_CONFIG="./.mcp.json"

    # 设置默认使用全局配置
    MCP_CONFIG_FILE="$MCP_GLOBAL_CONFIG"

    # 检查全局配置文件是否存在，不存在则创建
    if [ ! -f "$MCP_GLOBAL_CONFIG" ]; then
        echo "正在创建Claude Code全局配置文件..."
        cat > "$MCP_GLOBAL_CONFIG" << 'EOF'
{
  "mcpServers": {}
}
EOF
        print_success "Claude Code配置文件已创建: $MCP_GLOBAL_CONFIG"
    else
        # 确保mcpServers字段存在
        if ! python3 -c "
import json
try:
    with open('$MCP_GLOBAL_CONFIG', 'r') as f:
        config = json.load(f)
    if 'mcpServers' not in config:
        config['mcpServers'] = {}
        with open('$MCP_GLOBAL_CONFIG', 'w') as f:
            json.dump(config, f, indent=2)
        print('已添加mcpServers字段到现有配置文件')
except Exception as e:
    print(f'Error: {e}')
    exit(1)
" 2>/dev/null; then
            print_success "已更新Claude Code配置文件结构"
        fi
    fi

    print_success "使用Claude Code配置文件: $MCP_CONFIG_FILE"
}

# 选择MCP配置作用域
choose_mcp_scope() {
    echo
    echo "请选择MCP配置作用域："
    echo "1. 项目配置 (./.mcp.json) - 仅当前项目可用，可版本控制"
    echo "2. 全局配置 (~/.claude.json) - 所有项目可用，推荐"

    echo
    read -p "请选择 [1-2] (默认: 2): " scope_choice
    scope_choice=${scope_choice:-2}

    case $scope_choice in
        2)
            MCP_CONFIG_FILE="./.mcp.json"
            if [ ! -f "$MCP_CONFIG_FILE" ]; then
                echo "正在创建项目级MCP配置文件..."
                cat > "$MCP_CONFIG_FILE" << 'EOF'
{
  "mcpServers": {}
}
EOF
                print_success "项目MCP配置文件已创建: $MCP_CONFIG_FILE"
            fi
            echo "✅ 使用项目级配置: $MCP_CONFIG_FILE"
            ;;
        *)
            MCP_CONFIG_FILE="$MCP_GLOBAL_CONFIG"
            echo "✅ 使用全局配置: $MCP_CONFIG_FILE"
            ;;
    esac
}

# 获取已安装的MCP服务器列表（从正确的Claude Code配置文件读取）
get_installed_mcps() {
    if [ -f "$MCP_CONFIG_FILE" ]; then
        echo "📍 读取配置文件: $MCP_CONFIG_FILE"
        python3 -c "
import json
try:
    with open('$MCP_CONFIG_FILE', 'r') as f:
        config = json.load(f)
    servers = config.get('mcpServers', {})
    if servers:
        print('\\n🔧 已安装的MCP服务器:')
        for name, details in servers.items():
            command = details.get('command', 'N/A')
            server_type = details.get('type', 'N/A')
            has_env = 'env' in details
            env_info = ' [有环境变量]' if has_env else ''
            print(f'  • {name}: {command} (类型: {server_type}){env_info}')
    else:
        print('\\n📝 暂无已安装的MCP服务器')
        print('💡 提示: 可通过主菜单选项 6 或 7 安装MCP服务器')
except Exception as e:
    print(f'❌ 读取MCP配置失败: {e}')
"
    else
        echo "📝 MCP配置文件不存在: $MCP_CONFIG_FILE"
        echo "💡 提示: 请先安装MCP服务器来创建配置文件"
    fi
}

# 从配置中删除MCP服务器（更新为使用正确的配置文件）
remove_mcp_from_config() {
    local mcp_name="$1"

    python3 -c "
import json
import sys

config_file = '$MCP_CONFIG_FILE'
mcp_name = '$mcp_name'

try:
    with open(config_file, 'r') as f:
        config = json.load(f)

    if 'mcpServers' in config and mcp_name in config['mcpServers']:
        del config['mcpServers'][mcp_name]

        with open(config_file, 'w') as f:
            json.dump(config, f, indent=2)

        print(f'✅ MCP服务器 \"{mcp_name}\" 已从Claude Code配置中移除')
    else:
        print(f'⚠️  MCP服务器 \"{mcp_name}\" 不存在于配置中')
except Exception as e:
    print(f'❌ 移除MCP配置失败: {e}')
    sys.exit(1)
"
}

# MCP配置验证函数
validate_mcp_config() {
    if [ ! -f "$MCP_CONFIG_FILE" ]; then
        print_error "MCP配置文件不存在: $MCP_CONFIG_FILE"
        return 1
    fi

    python3 -c "
import json

config_file = '$MCP_CONFIG_FILE'

try:
    with open(config_file, 'r') as f:
        config = json.load(f)

    # 检查配置文件格式
    if 'mcpServers' not in config:
        print('⚠️  配置文件缺少 mcpServers 字段')
        return

    servers = config['mcpServers']
    if not servers:
        print('✅ 配置文件格式正确，但暂无MCP服务器')
        return

    print('✅ 配置文件格式验证通过')
    print(f'📊 共配置了 {len(servers)} 个MCP服务器:')

    for name, details in servers.items():
        command = details.get('command', '未配置')
        server_type = details.get('type', '未指定')
        print(f'  • {name}: {command} (类型: {server_type})')

except json.JSONDecodeError as e:
    print(f'❌ JSON格式错误: {e}')
except Exception as e:
    print(f'❌ 验证失败: {e}')
"
}

# 添加MCP服务器到配置（支持环境变量，使用Claude Code标准格式）
add_mcp_to_config_with_env() {
    local mcp_name="$1"
    local mcp_command="$2"
    local mcp_args="$3"
    local env_key="$4"
    local env_value="$5"

    # 使用Python来更新JSON配置，确保使用正确的Claude Code格式
    python3 -c "
import json
import sys

config_file = '$MCP_CONFIG_FILE'
mcp_name = '$mcp_name'
mcp_command = '$mcp_command'
mcp_args = '$mcp_args'
env_key = '$env_key'
env_value = '$env_value'

try:
    # 读取现有配置
    try:
        with open(config_file, 'r') as f:
            config = json.load(f)
    except FileNotFoundError:
        config = {}

    # 确保mcpServers字段存在
    if 'mcpServers' not in config:
        config['mcpServers'] = {}

    # 构建符合Claude Code标准的MCP配置
    mcp_config = {
        'command': mcp_command
    }

    # 添加args参数（如果有）
    if mcp_args:
        # 处理参数：如果是单个参数直接添加，否则按空格分割
        if ' ' in mcp_args:
            mcp_config['args'] = mcp_args.split()
        else:
            mcp_config['args'] = [mcp_args]

    # 添加环境变量（如果有）
    if env_key and env_value:
        mcp_config['env'] = {env_key: env_value}

    # 添加标准的stdio类型标识
    mcp_config['type'] = 'stdio'

    # 将MCP配置添加到mcpServers
    config['mcpServers'][mcp_name] = mcp_config

    # 写入配置文件
    with open(config_file, 'w') as f:
        json.dump(config, f, indent=2)

    print(f'✅ MCP服务器 \"{mcp_name}\" 已添加到Claude Code配置')
except Exception as e:
    print(f'❌ 添加MCP配置失败: {e}')
    sys.exit(1)
"
}

# 添加MCP服务器到配置（标准版本，使用Claude Code格式）
add_mcp_to_config() {
    local mcp_name="$1"
    local mcp_command="$2"
    local mcp_args="$3"

    # 使用Python来更新JSON配置
    python3 -c "
import json
import sys

config_file = '$MCP_CONFIG_FILE'
mcp_name = '$mcp_name'
mcp_command = '$mcp_command'
mcp_args = '$mcp_args'

try:
    # 读取现有配置
    try:
        with open(config_file, 'r') as f:
            config = json.load(f)
    except FileNotFoundError:
        config = {}

    # 确保mcpServers字段存在
    if 'mcpServers' not in config:
        config['mcpServers'] = {}

    # 构建符合Claude Code标准的MCP配置
    mcp_config = {
        'command': mcp_command,
        'type': 'stdio'
    }

    # 添加args参数（如果有）
    if mcp_args:
        # 处理参数：如果是单个参数直接添加，否则按空格分割
        if ' ' in mcp_args:
            mcp_config['args'] = mcp_args.split()
        else:
            mcp_config['args'] = [mcp_args]

    # 将MCP配置添加到mcpServers
    config['mcpServers'][mcp_name] = mcp_config

    # 写入配置文件，保持良好的JSON格式
    with open(config_file, 'w') as f:
        json.dump(config, f, indent=2)

    print(f'✅ MCP服务器 \"{mcp_name}\" 已添加到Claude Code配置')
except Exception as e:
    print(f'❌ 添加MCP配置失败: {e}')
    sys.exit(1)
"
}

# 从配置中删除MCP服务器
remove_mcp_from_config() {
    local mcp_name="$1"

    python3 -c "
import json
import sys

config_file = '$MCP_CONFIG_FILE'
mcp_name = '$mcp_name'

try:
    with open(config_file, 'r') as f:
        config = json.load(f)

    if 'mcpServers' in config and mcp_name in config['mcpServers']:
        del config['mcpServers'][mcp_name]

        with open(config_file, 'w') as f:
            json.dump(config, f, indent=2)

        print(f'MCP服务器 \"{mcp_name}\" 已从配置中移除')
    else:
        print(f'MCP服务器 \"{mcp_name}\" 不存在于配置中')
except Exception as e:
    print(f'移除MCP配置失败: {e}')
    sys.exit(1)
"
}

# 通用：切到 Node.js 22 版本
use_node_22() {
    # 加载 nvm（nvm 通常是一个 shell 函数，需要 source）
    if ! command_exists nvm; then
        if [ -f "$HOME/.nvm/nvm.sh" ]; then
            source "$HOME/.nvm/nvm.sh"
        else
            print_error "nvm未安装或未加载，无法切换 Node 版本。请先安装 nvm 或在菜单中执行'切换Node.js版本'。"
            return 1
        fi
    fi

    echo "正在切换到 Node.js 22 版本..."
    if ! nvm use 22; then
        print_error "切换 Node.js 22 失败，请先在菜单中选择'切换Node.js版本'安装并切换。"
        return 1
    fi

    CURRENT_NODE_VERSION=$(node --version 2>/dev/null)
    print_success "当前Node.js版本：$CURRENT_NODE_VERSION"
}


# 1. 切换Node.js版本
switch_node_version() {
    print_step "功能1：切换Node.js版本"

    # 检查nvm
    if ! command_exists nvm; then
        if [ -f "$HOME/.nvm/nvm.sh" ]; then
            source "$HOME/.nvm/nvm.sh"
        else
            print_error "nvm未安装，请先安装nvm"
            return 1
        fi
    fi

    # 获取用户输入的版本号
    read -p "请输入Node版本号（默认为 22）：" NODE_VERSION_INPUT
    NODE_VERSION=${NODE_VERSION_INPUT:-22}

    echo "目标Node版本：$NODE_VERSION"

    # 检查并安装Node.js
    if nvm ls $NODE_VERSION --no-colors | grep -q "N/A"; then
      echo "Node.js $NODE_VERSION 版本未安装，正在安装..."
      nvm install $NODE_VERSION
    else
      print_success "Node.js $NODE_VERSION 版本已安装"
    fi

    # 使用Node.js
    echo "正在切换到Node.js $NODE_VERSION 版本..."
    nvm use $NODE_VERSION

    CURRENT_NODE_VERSION=$(node --version)
    print_success "当前Node.js版本：$CURRENT_NODE_VERSION"
}

# 2. 安装ccpm
install_ccpm() {
    print_step "功能2：安装ccpm"

    # 确保.claude目录存在，如果不存在则创建
    if [ ! -d ".claude" ]; then
        echo "正在创建 .claude 目录..."
        mkdir .claude
        print_success ".claude 目录已创建"
    fi

    read -p "是否需要为安装过程启用代理？(Y/n): " USE_PROXY

    # 使用git clone方式安装
    REPO_URL="https://github.com/automazeio/ccpm.git"
    TEMP_DIR="ccpm_temp_install"

    # 默认启用代理，除非用户输入 'n' 或 'N'
    if ! [[ "$USE_PROXY" == "n" || "$USE_PROXY" == "N" ]]; then
        print_warning "已启用代理进行安装"
        export https_proxy=http://127.0.0.1:7890
        export http_proxy=http://127.0.0.1:7890
        export all_proxy=socks5://127.0.0.1:7890
    else
        echo "不使用代理进行安装"
    fi

    echo "正在从 $REPO_URL 克隆仓库到临时目录..."
    # 克隆到临时目录
    git clone --depth 1 "$REPO_URL" "$TEMP_DIR"

    if [ $? -eq 0 ]; then
        echo "克隆成功。正在复制 .claude 文件..."
        # 将.claude目录的内容复制到项目的.claude目录
        cp -r "$TEMP_DIR/ccpm/." ".claude/"
        echo "文件复制完成。正在清理临时文件..."
        rm -rf "$TEMP_DIR"
        print_success "清理完成。"
    else
        print_error "错误：克隆仓库失败。"
        rm -rf "$TEMP_DIR" # 确保清理失败的克隆
    fi

    # 安装后取消代理设置，避免影响后续操作
    unset https_proxy http_proxy all_proxy

    # ccpm的安装脚本是在本地创建文件，而不是安装一个全局命令
    # 确保.claude/commands/pm目录存在，如果不存在则创建
    if [ ! -d ".claude/commands/pm" ]; then
        echo "正在创建 .claude/commands/pm 目录..."
        mkdir -p ".claude/commands/pm"
        print_success ".claude/commands/pm 目录已创建"
    fi

    # 验证安装
    if [ -d ".claude/commands/pm" ]; then
        print_success "ccpm文件已成功下载到 .claude/ 目录"
        print_warning "请根据文档运行 '/pm:init' 来完成初始化设置"
    else
        print_error "ccpm安装失败，无法创建 .claude/commands/pm 目录"
    fi
}

# 3. 启动ccr
start_ccr() {
    print_step "功能3：启动Claude Code Router (ccr)"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    if ! command_exists ccr; then
        print_error "未找到ccr命令。请确保ccpm已正确安装。"
        return 1
    fi

    # 清空 ~/.claude/settings.json 中 env 下的内容，确保ccr执行时环境干净
    if [ -f "$HOME/.claude/settings.json" ]; then
        print_step "清空 ~/.claude/settings.json 中 env 的内容"
        python3 -c "
import json
import os

settings_file = os.path.expanduser('~/.claude/settings.json')

try:
    # 读取现有配置
    with open(settings_file, 'r') as f:
        settings = json.load(f)

    # 清空 env 内容
    settings['env'] = {}

    # 写回文件
    with open(settings_file, 'w') as f:
        json.dump(settings, f, indent=2)

    print('✅ 已清空 ~/.claude/settings.json 中 env 的内容')
except Exception as e:
    print(f'⚠️  清空 env 内容时出错: {e}')
"
    else
        print_warning "未找到 ~/.claude/settings.json 文件"
    fi

    echo "🎯 正在启动Claude Code Router..."
    echo "按 Ctrl+C 停止服务器"
    ccr code --dangerously-skip-permissions
}

# Claude Code 安装函数（内部使用）
install_claude_code_internal() {
    echo "🎯 正在安装 Claude Code..."
    echo "📦 安装命令: npm install -g @anthropic-ai/claude-code"
    echo ""

    read -p "是否需要为安装过程启用代理？(Y/n): " USE_PROXY

    # 默认启用代理，除非用户输入 'n' 或 'N'
    if ! [[ "$USE_PROXY" == "n" || "$USE_PROXY" == "N" ]]; then
        print_warning "已启用代理进行安装"
        export https_proxy=http://127.0.0.1:7890
        export http_proxy=http://127.0.0.1:7890
        export all_proxy=socks5://127.0.0.1:7890
    else
        echo "不使用代理进行安装"
    fi

    echo ""
    echo "🔄 正在全局安装 Claude Code..."
    npm install -g @anthropic-ai/claude-code

    # 安装后取消代理设置
    unset https_proxy http_proxy all_proxy

    if [ $? -eq 0 ]; then
        print_success "Claude Code 安装完成！"

        if command_exists claude; then
            NEW_VERSION=$(claude --version 2>/dev/null || echo "未知版本")
            echo "安装版本: $NEW_VERSION"
        fi

        echo ""
        echo "💡 Claude Code 功能说明："
        echo "  • claude --version     - 查看版本信息"
        echo "  • claude --help        - 查看帮助信息"
        echo "  • claude               - 启动 Claude Code"
        echo ""
        echo "📖 更多信息: https://docs.anthropic.com/claude/docs/claude-code"
        return 0
    else
        print_error "Claude Code 安装失败，请检查网络连接或代理设置"
        return 1
    fi
}

# 6. 启动claude (自动检测、安装并升级)
start_claude() {
    print_step "功能6：启动Claude Code (自动检测、安装并升级)"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    # 检查是否安装了 claude，如果没有则自动安装
    if ! command_exists claude; then
        print_warning "未检测到 Claude Code，正在自动安装..."
        install_claude_code_internal
        # 重新检查是否安装成功
        if ! command_exists claude; then
            print_error "Claude Code 安装失败，无法启动"
            return 1
        fi
        print_success "Claude Code 安装成功，正在启动..."
    else
        # 已安装，检查是否需要升级
        CURRENT_VERSION=$(claude --version 2>/dev/null | grep -o 'claude-code version [^[:space:]]*' | sed 's/claude-code version //' || echo "未知版本")
        print_success "检测到 Claude Code 已安装 (版本: $CURRENT_VERSION)"

        read -p "是否检查并升级到最新版本？(y/N): " check_upgrade
        if [[ "$check_upgrade" == "y" || "$check_upgrade" == "Y" ]]; then
            print_step "正在检查最新版本..."

            read -p "是否需要为升级过程启用代理？(Y/n): " USE_PROXY
            # 默认启用代理，除非用户输入 'n' 或 'N'
            if ! [[ "$USE_PROXY" == "n" || "$USE_PROXY" == "N" ]]; then
                print_warning "已启用代理进行升级"
                export https_proxy=http://127.0.0.1:7890
                export http_proxy=http://127.0.0.1:7890
                export all_proxy=socks5://127.0.0.1:7890
            else
                echo "不使用代理进行升级"
            fi

            echo "🔄 正在升级 Claude Code 到最新版本..."
            npm install -g @anthropic-ai/claude-code@latest

            # 升级后取消代理设置
            unset https_proxy http_proxy all_proxy

            if [ $? -eq 0 ]; then
                NEW_VERSION=$(claude --version 2>/dev/null | grep -o 'claude-code version [^[:space:]]*' | sed 's/claude-code version //' || echo "最新版本")
                print_success "Claude Code 升级完成！新版本: $NEW_VERSION"
            else
                print_error "Claude Code 升级失败，将使用当前版本启动"
            fi
        fi
    fi

    # 检查是否安装了 ccs 工具
    if command_exists ccs; then
        print_step "检测到 ccs 工具，正在获取环境配置列表..."
        echo

        # 获取 ccs 配置列表并格式化显示为 4 列
        echo "📋 当前可用的 Claude Code 环境配置:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

        # 使用 Python 脚本解析 ccs ls 输出并格式化为 4 列
        python3 << 'PYTHON_SCRIPT'
import subprocess
import re
import sys
import os
import glob

def get_ccs_configs():
    """从 ~/.claude/ 目录读取所有 ccs 配置文件"""
    try:
        claude_dir = os.path.expanduser("~/.claude/")
        if not os.path.exists(claude_dir):
            print("⚠️  未找到 ~/.claude/ 目录")
            return [], None

        configs = []
        current_config = None

        # 查找两种命名格式的配置文件
        # 新格式: settings.json.<config-name>
        new_format_files = glob.glob(os.path.join(claude_dir, "settings.json.*"))
        # 传统格式: settings-<config-name>.json
        old_format_files = glob.glob(os.path.join(claude_dir, "settings-*.json"))

        # 解析新格式配置文件
        for filepath in new_format_files:
            basename = os.path.basename(filepath)
            # 提取配置名: settings.json.production -> production
            if basename.startswith("settings.json."):
                config_name = basename.replace("settings.json.", "")
                configs.append(config_name)

        # 解析传统格式配置文件
        for filepath in old_format_files:
            basename = os.path.basename(filepath)
            # 提取配置名: settings-production.json -> production
            if basename.startswith("settings-") and basename.endswith(".json"):
                config_name = basename.replace("settings-", "").replace(".json", "")
                configs.append(config_name)

        # 检测当前激活的配置
        # 方法1: 检查 settings.json 符号链接
        settings_file = os.path.join(claude_dir, "settings.json")
        if os.path.islink(settings_file):
            link_target = os.readlink(settings_file)
            target_basename = os.path.basename(link_target)
            # 从链接目标提取配置名
            if target_basename.startswith("settings.json."):
                current_config = target_basename.replace("settings.json.", "")
            elif target_basename.startswith("settings-") and target_basename.endswith(".json"):
                current_config = target_basename.replace("settings-", "").replace(".json", "")

        # 方法2: 如果不是符号链接，尝试读取 .ccs-current 文件（如果存在）
        if not current_config:
            ccs_current_file = os.path.join(claude_dir, ".ccs-current")
            if os.path.exists(ccs_current_file):
                try:
                    with open(ccs_current_file, 'r') as f:
                        current_config = f.read().strip()
                except:
                    pass

        # 方法3: 比较 settings.json 和各个配置文件的内容
        if not current_config and os.path.exists(settings_file) and not os.path.islink(settings_file):
            try:
                with open(settings_file, 'r') as f:
                    current_content = f.read()

                # 检查新格式文件
                for filepath in new_format_files:
                    try:
                        with open(filepath, 'r') as f:
                            if f.read() == current_content:
                                basename = os.path.basename(filepath)
                                current_config = basename.replace("settings.json.", "")
                                break
                    except:
                        continue

                # 如果还没找到，检查传统格式文件
                if not current_config:
                    for filepath in old_format_files:
                        try:
                            with open(filepath, 'r') as f:
                                if f.read() == current_content:
                                    basename = os.path.basename(filepath)
                                    current_config = basename.replace("settings-", "").replace(".json", "")
                                    break
                        except:
                            continue
            except:
                pass

        return sorted(set(configs)), current_config

    except Exception as e:
        print(f"⚠️  读取配置文件失败: {e}")
        return [], None

def display_configs_in_columns(configs, current_config, columns=4):
    """以多列格式显示配置"""
    if not configs:
        print("⚠️  没有可用的配置")
        print("💡 提示: 使用 'ccs add <config-name>' 创建新配置")
        return

    if current_config:
        print(f"共 {len(configs)} 个配置 (★ 表示当前使用: {current_config})")
    else:
        print(f"共 {len(configs)} 个配置 (未检测到当前配置)")
    print()

    # 准备显示数据
    config_items = []
    for idx, config_name in enumerate(configs, 1):
        is_current = (config_name == current_config)

        if is_current:
            # 当前配置：在序号前加 ★
            item = f"\033[1;32m★{idx:>2}. {config_name:<18}\033[0m"
        else:
            # 普通配置：序号前空格
            item = f" {idx:>2}. {config_name:<18}"

        config_items.append(item)

    # 计算行数（向上取整）
    total_configs = len(config_items)
    rows = (total_configs + columns - 1) // columns

    # 按行显示（列优先）
    for row in range(rows):
        line_items = []
        for col in range(columns):
            idx = row + col * rows
            if idx < total_configs:
                line_items.append(config_items[idx])

        # 打印一行
        print("  ".join(line_items))

# 主逻辑
configs, current_config = get_ccs_configs()
if configs:
    display_configs_in_columns(configs, current_config, columns=4)
else:
    print("未能获取配置列表")
    sys.exit(1)
PYTHON_SCRIPT

        echo
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo

        # 询问是否切换配置
        echo "💡 是否需要切换环境配置?"
        read -p "输入配置序号进行切换，直接回车跳过: " config_choice

        if [ -n "$config_choice" ] && [[ "$config_choice" =~ ^[0-9]+$ ]]; then
            # 获取配置列表
            config_list=$(ls ~/.claude/settings.json.* ~/.claude/settings-*.json 2>/dev/null | while read file; do
                basename "$file" | sed 's/settings\.json\.//' | sed 's/settings-//' | sed 's/\.json$//'
            done | sort | uniq)

            # 转换为数组
            config_array=($config_list)
            selected_index=$((config_choice - 1))

            if [ $selected_index -ge 0 ] && [ $selected_index -lt ${#config_array[@]} ]; then
                selected_config="${config_array[$selected_index]}"
                print_step "正在切换到配置: $selected_config"

                # 使用 ccs sw 命令切换配置
                if ccs sw "$selected_config"; then
                    print_success "环境配置切换成功！"
                    echo "⏳ 等待 2 秒使配置生效..."
                    sleep 2
                else
                    print_error "环境配置切换失败，将使用当前配置启动"
                fi
            else
                print_error "无效的配置序号"
            fi
        else
            print_success "将使用当前配置启动 Claude Code"
        fi
        echo
    else
        print_warning "未检测到 ccs 工具，跳过环境配置切换"
        echo "💡 提示: 可通过 npm 安装 ccs 工具"
        echo
    fi

    echo "🎯 正在启动 Claude..."
    export NODE_OPTIONS="--max-old-space-size=8192"
    echo "💡 内存设置: 8GB | 按 Ctrl+C 停止"
    claude --dangerously-skip-permissions
}

# 7. 启动codex (自动检测、安装并升级)
start_codex() {
    print_step "功能7：启动 Codex (自动检测、安装并升级)"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    # 检查是否安装了 codex，如果没有则自动安装
    if ! command_exists codex; then
        print_warning "未检测到 Codex CLI，正在自动安装..."

        read -p "是否需要为安装过程启用代理？(Y/n): " USE_PROXY

        # 默认启用代理，除非用户输入 'n' 或 'N'
        if ! [[ "$USE_PROXY" == "n" || "$USE_PROXY" == "N" ]]; then
            print_warning "已启用代理进行安装"
            export https_proxy=http://127.0.0.1:7890
            export http_proxy=http://127.0.0.1:7890
            export all_proxy=socks5://127.0.0.1:7890
        else
            echo "不使用代理进行安装"
        fi

        echo ""
        echo "🔄 正在全局安装 Codex CLI..."
        npm install -g @openai/codex

        # 安装后取消代理设置
        unset https_proxy http_proxy all_proxy

        if [ $? -eq 0 ]; then
            print_success "Codex CLI 安装完成！"
            if command_exists codex; then
                CODEX_VERSION=$(codex --version 2>/dev/null || echo "版本信息不可用")
                echo "安装版本: $CODEX_VERSION"
            fi
        else
            print_error "Codex CLI 安装失败，请检查网络连接或代理设置"
            return 1
        fi
    else
        # 已安装，检查是否需要升级
        CURRENT_VERSION=$(codex --version 2>/dev/null || echo "未知版本")
        print_success "检测到 Codex CLI 已安装 (版本: $CURRENT_VERSION)"

        read -p "是否检查并升级到最新版本？(y/N): " check_upgrade
        if [[ "$check_upgrade" == "y" || "$check_upgrade" == "Y" ]]; then
            print_step "正在检查最新版本..."

            read -p "是否需要为升级过程启用代理？(Y/n): " USE_PROXY
            # 默认启用代理，除非用户输入 'n' 或 'N'
            if ! [[ "$USE_PROXY" == "n" || "$USE_PROXY" == "N" ]]; then
                print_warning "已启用代理进行升级"
                export https_proxy=http://127.0.0.1:7890
                export http_proxy=http://127.0.0.1:7890
                export all_proxy=socks5://127.0.0.1:7890
            else
                echo "不使用代理进行升级"
            fi

            echo "🔄 正在升级 Codex CLI 到最新版本..."
            npm install -g @openai/codex@latest

            # 升级后取消代理设置
            unset https_proxy http_proxy all_proxy

            if [ $? -eq 0 ]; then
                NEW_VERSION=$(codex --version 2>/dev/null || echo "最新版本")
                print_success "Codex CLI 升级完成！新版本: $NEW_VERSION"
            else
                print_error "Codex CLI 升级失败，将使用当前版本启动"
            fi
        fi
    fi

    # 设置 Codex 工作目录，确保加载项目内的 prompts
    PROJECT_ROOT="$(pwd)"
    export CODEX_HOME="${PROJECT_ROOT}/.codex"

    GLOBAL_CODEX_HOME="$HOME/.codex"

    if [ ! -d "$CODEX_HOME" ]; then
        mkdir -p "$CODEX_HOME"
    fi

    # 复用全局凭据 / 配置，避免重新登录
    for file in auth.json config.toml; do
        if [ -f "$GLOBAL_CODEX_HOME/$file" ] && [ ! -f "$CODEX_HOME/$file" ]; then
            echo "🔑 检测到全局 Codex $file，已复制到项目目录"
            cp "$GLOBAL_CODEX_HOME/$file" "$CODEX_HOME/$file"
        fi
    done

    if [ ! -d "$CODEX_HOME/prompts" ]; then
        echo "⚠️  未检测到 $CODEX_HOME/prompts 目录，正在自动创建..."
        mkdir -p "$CODEX_HOME/prompts"
        echo "✅ 已创建 $CODEX_HOME/prompts，请确认自定义命令已就绪"
    fi

    echo "📁 已设置 CODEX_HOME=$CODEX_HOME"
    echo "🎯 正在启动 Codex..."
    export NODE_OPTIONS="--max-old-space-size=8192"
    echo "💡 内存设置: 8GB | 按 Ctrl+C 停止"
    codex resume --cd "$PROJECT_ROOT" --dangerously-bypass-approvals-and-sandbox
}

# 6. 安装 cc-statusline
install_cc_statusline() {
    print_step "功能6：安装 cc-statusline"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    if ! command_exists npx; then
        print_error "未找到 npx 命令，请确认 Node.js 已正确安装。"
        return 1
    fi

    echo "🎯 正在安装 cc-statusline..."
    echo "按 Ctrl+C 取消"
    npx @chongdashu/cc-statusline init
}

# 7. 安装 ccs
install_ccs() {
    print_step "功能7：安装 ccs (Claude Code Settings 管理工具)"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    if ! command_exists npm; then
        print_error "未找到 npm 命令，请确认 Node.js 已正确安装。"
        return 1
    fi

    echo "🎯 正在安装 ccs (Claude Code Settings)..."
    echo "📦 来源: https://github.com/wangazhang/ccs"
    echo ""

    read -p "是否需要为安装过程启用代理？(Y/n): " USE_PROXY

    # 默认启用代理，除非用户输入 'n' 或 'N'
    if ! [[ "$USE_PROXY" == "n" || "$USE_PROXY" == "N" ]]; then
        print_warning "已启用代理进行安装"
        export https_proxy=http://127.0.0.1:7890
        export http_proxy=http://127.0.0.1:7890
        export all_proxy=socks5://127.0.0.1:7890
    else
        echo "不使用代理进行安装"
    fi

    echo ""
    echo "🔄 正在全局安装 ccs..."
    npm install -g @wangazhang/ccs

    # 安装后取消代理设置
    unset https_proxy http_proxy all_proxy

    if [ $? -eq 0 ]; then
        print_success "ccs 安装完成！"

        if command_exists ccs; then
            CCS_VERSION=$(ccs --version 2>/dev/null || echo "未知版本")
            echo "安装版本: $CCS_VERSION"
        fi

        echo ""
        echo "💡 ccs 功能说明："
        echo "  • ccs ls         - 列出所有配置"
        echo "  • ccs add <name> - 添加新配置"
        echo "  • ccs sw <name>  - 切换配置"
        echo "  • ccs rm <name>  - 删除配置"
        echo "  • ccs --help     - 查看帮助"
        echo ""
        echo "📖 更多信息: https://github.com/wangazhang/ccs"
    else
        print_error "ccs 安装失败，请检查网络连接或代理设置"
        return 1
    fi
}



# 10. 一键安装常见MCP（集成配置作用域选择）
install_common_mcps() {
    print_step "功能10：一键安装常见MCP"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    # 初始化MCP配置并选择作用域
    init_mcp_config
    choose_mcp_scope

    # 获取常见MCP信息的函数
    get_common_mcp_info() {
        local mcp_name="$1"
        case "$mcp_name" in
            "filesystem") echo "npx -y @modelcontextprotocol/server-filesystem|$HOME/Documents| |文件系统访问 - 管理本地文件和目录" ;;
            "context7") echo "npx -y @upstash/context7-mcp| | |Context7文档检索 - 智能搜索和问答" ;;
            "figma") echo "npx -y figma-mcp| | |Figma设计工具 - 访问和管理设计文件" ;;
            "playwright") echo "npx -y @modelcontextprotocol/server-playwright| | |浏览器自动化 - 网页操作和测试" ;;
            *) echo "" ;;
        esac
    }

    echo "🎯 正在安装推荐的常见MCP服务器..."
    echo "📁 配置文件: $MCP_CONFIG_FILE"

    echo
    echo "即将安装以下常见推荐MCP服务器："
    common_mcp_names=("filesystem" "context7" "figma" "playwright")
    for mcp_name in "${common_mcp_names[@]}"; do
        mcp_info=$(get_common_mcp_info "$mcp_name")
        mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)
        echo "  • $mcp_name - $mcp_description"
    done
    echo

    read -p "确认安装这些推荐MCP到 $MCP_CONFIG_FILE？(Y/n): " confirm
    if [[ "$confirm" == "n" || "$confirm" == "N" ]]; then
        echo "安装已取消"
        return 0
    fi

    # 逐个安装MCP
    for mcp_name in "${common_mcp_names[@]}"; do
        echo
        print_step "安装 $mcp_name MCP..."

        # 解析MCP信息（使用新的分隔符格式）
        mcp_info=$(get_common_mcp_info "$mcp_name")
        mcp_command=$(echo "$mcp_info" | cut -d'|' -f1)
        mcp_args=$(echo "$mcp_info" | cut -d'|' -f2)
        mcp_config_type=$(echo "$mcp_info" | cut -d'|' -f3)
        mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)

        echo "描述: $mcp_description"

        # 处理需要特殊配置的MCP
        case "$mcp_name" in
            "filesystem")
                echo "⚠️  此MCP需要配置访问目录路径"
                mcp_default_path="$mcp_args"  # For filesystem, args field contains the default path
                read -p "请输入允许访问的目录路径 (默认: $mcp_default_path，回车使用默认): " user_path
                user_path=${user_path:-"$mcp_default_path"}
                final_args="$user_path"
                echo "✅ 将配置访问路径为: $user_path"
                ;;
            "figma")
                echo "⚠️  Figma MCP需要API Token才能正常工作"
                read -p "是否现在配置Figma API Token？(y/N，可稍后通过管理功能配置): " config_figma
                if [[ "$config_figma" == "y" || "$config_figma" == "Y" ]]; then
                    read -p "请输入Figma API Token: " figma_token
                    if [ -n "$figma_token" ]; then
                        # 为Figma MCP添加环境变量
                        add_mcp_to_config_with_env "$mcp_name" "$mcp_command" "$mcp_args" "FIGMA_ACCESS_TOKEN" "$figma_token"
                        print_success "$mcp_name MCP 安装成功（已配置API Token）"
                        continue
                    fi
                fi
                final_args="$mcp_args"
                echo "📝 提示: 可稍后通过主菜单 '8. 管理已安装MCP' 配置Figma API Token"
                ;;
            *)
                final_args="$mcp_args"
                ;;
        esac

        # 添加到配置
        add_mcp_to_config "$mcp_name" "$mcp_command" "$final_args"

        if [ $? -eq 0 ]; then
            print_success "$mcp_name MCP 安装成功"
        else
            print_error "$mcp_name MCP 安装失败"
        fi
    done

    echo
    print_success "常见推荐MCP安装完成！"
    echo
    echo "📋 安装总结:"
    echo "• filesystem - 可访问指定目录的文件"
    echo "• context7 - 智能文档检索和问答"
    echo "• figma - Figma设计文件访问（需API Token）"
    echo "• playwright - 浏览器自动化操作"
    echo
    echo "📁 配置位置: $MCP_CONFIG_FILE"
    echo "💡 提示: 重启Claude Code以使MCP配置生效"
    echo "🔍 可通过主菜单 '8. 管理已安装MCP' 来查看详情和进行配置调整"
}

# 获取MCP信息的函数
get_mcp_info() {
    local mcp_name="$1"
    case "$mcp_name" in
        # 核心推荐MCP
        "filesystem") echo "npx -y @modelcontextprotocol/server-filesystem| |path|文件系统访问 - 管理本地文件和目录" ;;
        "context7") echo "npx -y @upstash/context7-mcp| | |Context7文档检索 - 智能搜索和问答" ;;
        "figma") echo "npx -y figma-mcp| |token|Figma设计工具 - 访问和管理设计文件" ;;
        "playwright") echo "npx -y @modelcontextprotocol/server-playwright| | |浏览器自动化 - 网页操作和测试" ;;

        # 开发工具MCP
        "git") echo "npx -y git-mcp| | |Git版本控制 - 代码版本管理" ;;
        "github") echo "npx -y @modelcontextprotocol/server-github| |token|GitHub集成 - 仓库和Issue管理" ;;
        "prisma") echo "npx -y prisma-mcp| |dburl|Prisma数据库ORM - 数据库查询和管理" ;;
        "repomix") echo "npx -y repomix-mcp| | |代码仓库分析 - 项目结构分析" ;;

        # 工作流程MCP
        "pipedream") echo "npx -y pipedream-mcp| |key|Pipedream工作流 - API集成和自动化" ;;
        "fastapi-mcp") echo "npx -y fastapi-mcp| | |FastAPI Web框架 - API开发工具" ;;
        "serena") echo "npx -y serena-mcp| | |Serena AI助手 - 智能对话助手" ;;

        # 生产力MCP
        "xmind") echo "npx -y xmind-generator-mcp| |path|Xmind思维导图 - 思维导图生成" ;;

        # 其他常用MCP
        "memory") echo "npx -y @modelcontextprotocol/server-memory| | |内存存储 - 临时数据存储" ;;
        "sequential-thinking") echo "npx -y @modelcontextprotocol/server-sequential-thinking| | |序列化思维 - 结构化推理" ;;
        "sqlite") echo "npx -y @modelcontextprotocol/server-sqlite| |dbpath|SQLite数据库 - 轻量级数据库" ;;
        "postgres") echo "npx -y @modelcontextprotocol/server-postgres| |dburl|PostgreSQL数据库 - 企业级数据库" ;;
        "brave-search") echo "npx -y @modelcontextprotocol/server-brave-search| |key|Brave搜索 - 网络搜索功能" ;;
        "gmail") echo "npx -y @modelcontextprotocol/server-gmail| |oauth|Gmail邮件 - 邮件读写功能" ;;
        "slack") echo "npx -y @modelcontextprotocol/server-slack| |token|Slack集成 - 团队协作工具" ;;
        *) echo "" ;;
    esac
}

# 11. 浏览MCP列表并安装（集成配置作用域选择）
browse_mcp_list() {
    print_step "功能11：浏览MCP列表并安装"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    # 初始化MCP配置并选择作用域
    init_mcp_config
    choose_mcp_scope

    while true; do
        echo
        echo "=========================================="
        echo "         可用MCP服务器完整列表"
        echo "=========================================="
        echo "📁 配置将保存到: $MCP_CONFIG_FILE"
        echo
        echo "🌟 核心推荐MCP (基于使用频率):"
        echo "----------------------------------------"

        local counter=1
        local mcp_names=()

        # 先显示核心推荐MCP
        local mcp_items=()
        local core_mcps=("filesystem" "context7" "figma" "playwright")
        for mcp_name in "${core_mcps[@]}"; do
            mcp_info=$(get_mcp_info "$mcp_name")
            if [[ -n "$mcp_info" ]]; then
                mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)
                mcp_config_type=$(echo "$mcp_info" | cut -d'|' -f3)

                config_hint=""
                if [[ "$mcp_config_type" == "path" ]]; then
                    config_hint=" 📁"
                elif [[ "$mcp_config_type" == "token" ]]; then
                    config_hint=" 🔑"
                elif [[ "$mcp_config_type" == "key" ]]; then
                    config_hint=" 🔐"
                elif [[ "$mcp_config_type" == "dburl" || "$mcp_config_type" == "dbpath" ]]; then
                    config_hint=" 🗄️"
                fi

                mcp_items+=("${counter}. ${mcp_name} - ${mcp_description}${config_hint}")
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done
        display_mcp_items_columns "${mcp_items[@]}"

        echo
        echo "🛠️  开发工具MCP:"
        echo "----------------------------------------"
        mcp_items=()
        local dev_mcps=("git" "github" "prisma" "repomix")
        for mcp_name in "${dev_mcps[@]}"; do
            mcp_info=$(get_mcp_info "$mcp_name")
            if [[ -n "$mcp_info" ]]; then
                mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)
                mcp_config_type=$(echo "$mcp_info" | cut -d'|' -f3)

                config_hint=""
                if [[ "$mcp_config_type" == "token" ]]; then
                    config_hint=" 🔑"
                elif [[ "$mcp_config_type" == "dburl" ]]; then
                    config_hint=" 🗄️"
                fi

                mcp_items+=("${counter}. ${mcp_name} - ${mcp_description}${config_hint}")
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done
        display_mcp_items_columns "${mcp_items[@]}"

        echo
        echo "⚡ 工作流程MCP:"
        echo "----------------------------------------"
        mcp_items=()
        local workflow_mcps=("pipedream" "fastapi-mcp" "serena")
        for mcp_name in "${workflow_mcps[@]}"; do
            mcp_info=$(get_mcp_info "$mcp_name")
            if [[ -n "$mcp_info" ]]; then
                mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)
                mcp_config_type=$(echo "$mcp_info" | cut -d'|' -f3)

                config_hint=""
                if [[ "$mcp_config_type" == "key" ]]; then
                    config_hint=" 🔐"
                fi

                mcp_items+=("${counter}. ${mcp_name} - ${mcp_description}${config_hint}")
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done
        display_mcp_items_columns "${mcp_items[@]}"

        echo
        echo "📈 生产力工具MCP:"
        echo "----------------------------------------"
        mcp_items=()
        local productivity_mcps=("xmind")
        for mcp_name in "${productivity_mcps[@]}"; do
            mcp_info=$(get_mcp_info "$mcp_name")
            if [[ -n "$mcp_info" ]]; then
                mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)

                mcp_items+=("${counter}. ${mcp_name} - ${mcp_description} 📁")
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done
        display_mcp_items_columns "${mcp_items[@]}"

        echo
        echo "🔧 其他实用MCP:"
        echo "----------------------------------------"
        mcp_items=()
        local other_mcps=("memory" "sequential-thinking" "sqlite" "postgres" "brave-search" "gmail" "slack")
        for mcp_name in "${other_mcps[@]}"; do
            mcp_info=$(get_mcp_info "$mcp_name")
            if [[ -n "$mcp_info" ]]; then
                mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)
                mcp_config_type=$(echo "$mcp_info" | cut -d'|' -f3)

                config_hint=""
                if [[ "$mcp_config_type" == "dbpath" || "$mcp_config_type" == "dburl" ]]; then
                    config_hint=" 🗄️"
                elif [[ "$mcp_config_type" == "key" ]]; then
                    config_hint=" 🔐"
                elif [[ "$mcp_config_type" == "token" ]]; then
                    config_hint=" 🔑"
                elif [[ "$mcp_config_type" == "oauth" ]]; then
                    config_hint=" 🔐"
                fi

                mcp_items+=("${counter}. ${mcp_name} - ${mcp_description}${config_hint}")
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done
        display_mcp_items_columns "${mcp_items[@]}"

        echo
        echo "📋 图标说明: 📁=需要路径 🔑=需要Token 🔐=需要Key/OAuth 🗄️=需要数据库配置"
        echo "💡 重启Claude Code以使MCP配置生效"
        echo
        echo "0. 返回主菜单"
        echo

        read -p "请选择要安装的MCP (输入编号): " choice

        if [[ "$choice" == "0" ]]; then
            break
        elif [[ "$choice" =~ ^[1-9][0-9]*$ ]] && [ "$choice" -le "${#mcp_names[@]}" ]; then
            selected_mcp="${mcp_names[$((choice-1))]}"
            selected_mcp_info=$(get_mcp_info "$selected_mcp")
            install_single_mcp "$selected_mcp" "$selected_mcp_info"
        else
            print_error "无效选项，请输入正确的编号"
        fi
    done
}

# 安装单个MCP的函数（更新为支持新格式和Claude Code标准）
install_single_mcp() {
    local mcp_name="$1"
    local mcp_info="$2"

    # 解析MCP信息（使用新的分隔符格式）
    local mcp_command=$(echo "$mcp_info" | cut -d'|' -f1)
    local mcp_args_base=$(echo "$mcp_info" | cut -d'|' -f2)
    local mcp_config_type=$(echo "$mcp_info" | cut -d'|' -f3)
    local mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)

    echo
    print_step "安装 $mcp_name MCP"
    echo "描述: $mcp_description"
    echo "命令: $mcp_command"
    echo "配置到: $MCP_CONFIG_FILE"
    echo

    local final_args="$mcp_args_base"
    local env_key=""
    local env_value=""

    # 根据配置类型处理特殊配置
    case "$mcp_config_type" in
        "path")
            echo "⚠️  此MCP需要配置目录路径"
            case "$mcp_name" in
                "filesystem")
                    read -p "请输入允许访问的目录路径 (默认: $HOME/Documents，回车跳过稍后配置): " user_path
                    user_path=${user_path:-"$HOME/Documents"}
                    final_args="$mcp_args_base $user_path"
                    ;;
                "xmind")
                    read -p "请输入Xmind文件输出路径 (默认: $HOME/xmind-output，回车跳过稍后配置): " output_path
                    output_path=${output_path:-"$HOME/xmind-output"}
                    env_key="outputPath"
                    env_value="$output_path"
                    final_args="$mcp_args_base"
                    ;;
                *)
                    read -p "请输入路径 (回车跳过稍后配置): " user_path
                    if [ -n "$user_path" ]; then
                        final_args="$mcp_args_base $user_path"
                    fi
                    ;;
            esac
            ;;
        "token")
            echo "⚠️  此MCP需要API Token才能正常工作"
            read -p "是否现在配置API Token？(y/N，回车跳过稍后配置): " config_now
            if [[ "$config_now" == "y" || "$config_now" == "Y" ]]; then
                case "$mcp_name" in
                    "figma")
                        read -p "请输入Figma API Token: " token_value
                        if [ -n "$token_value" ]; then
                            env_key="FIGMA_ACCESS_TOKEN"
                            env_value="$token_value"
                        fi
                        ;;
                    "github")
                        read -p "请输入GitHub Personal Access Token: " token_value
                        if [ -n "$token_value" ]; then
                            env_key="GITHUB_PERSONAL_ACCESS_TOKEN"
                            env_value="$token_value"
                        fi
                        ;;
                    "slack")
                        read -p "请输入Slack Bot Token: " token_value
                        if [ -n "$token_value" ]; then
                            env_key="SLACK_BOT_TOKEN"
                            env_value="$token_value"
                        fi
                        ;;
                    *)
                        read -p "请输入API Token: " token_value
                        if [ -n "$token_value" ]; then
                            env_key="API_TOKEN"
                            env_value="$token_value"
                        fi
                        ;;
                esac
            else
                echo "📝 提示: 可稍后通过主菜单 '8. 管理已安装MCP' 配置API Token"
            fi
            ;;
        "key")
            echo "⚠️  此MCP需要API Key才能正常工作"
            read -p "是否现在配置API Key？(y/N，回车跳过稍后配置): " config_now
            if [[ "$config_now" == "y" || "$config_now" == "Y" ]]; then
                case "$mcp_name" in
                    "brave-search")
                        read -p "请输入Brave Search API Key: " key_value
                        if [ -n "$key_value" ]; then
                            env_key="BRAVE_API_KEY"
                            env_value="$key_value"
                        fi
                        ;;
                    "pipedream")
                        read -p "请输入Pipedream API Key: " key_value
                        if [ -n "$key_value" ]; then
                            env_key="PIPEDREAM_API_KEY"
                            env_value="$key_value"
                        fi
                        ;;
                    *)
                        read -p "请输入API Key: " key_value
                        if [ -n "$key_value" ]; then
                            env_key="API_KEY"
                            env_value="$key_value"
                        fi
                        ;;
                esac
            else
                echo "📝 提示: 可稍后通过主菜单 '8. 管理已安装MCP' 配置API Key"
            fi
            ;;
        "dbpath"|"dburl")
            echo "⚠️  此MCP需要数据库配置"
            read -p "是否现在配置数据库？(y/N，回车跳过稍后配置): " config_db
            if [[ "$config_db" == "y" || "$config_db" == "Y" ]]; then
                case "$mcp_name" in
                    "sqlite")
                        read -p "请输入SQLite数据库文件路径: " db_path
                        if [ -n "$db_path" ]; then
                            final_args="$mcp_args_base --db-path=$db_path"
                        fi
                        ;;
                    "postgres")
                        read -p "请输入PostgreSQL连接字符串 (格式: postgresql://user:password@localhost/db): " db_url
                        if [ -n "$db_url" ]; then
                            final_args="$mcp_args_base $db_url"
                        fi
                        ;;
                    "prisma")
                        read -p "请输入数据库连接URL: " db_url
                        if [ -n "$db_url" ]; then
                            env_key="DATABASE_URL"
                            env_value="$db_url"
                        fi
                        ;;
                    *)
                        read -p "请输入数据库配置: " db_config
                        if [ -n "$db_config" ]; then
                            final_args="$mcp_args_base $db_config"
                        fi
                        ;;
                esac
            else
                echo "📝 提示: 可稍后通过主菜单 '8. 管理已安装MCP' 配置数据库连接"
            fi
            ;;
        "oauth")
            echo "⚠️  此MCP需要OAuth配置，建议稍后在管理界面中配置"
            echo "📝 提示: 可稍后通过主菜单 '8. 管理已安装MCP' 配置OAuth认证"
            ;;
    esac

    echo
    read -p "确认安装 '$mcp_name' MCP到 $MCP_CONFIG_FILE？(Y/n): " confirm
    if [[ "$confirm" != "n" && "$confirm" != "N" ]]; then
        # 根据是否有环境变量选择不同的添加方法
        if [ -n "$env_key" ] && [ -n "$env_value" ]; then
            add_mcp_to_config_with_env "$mcp_name" "$mcp_command" "$final_args" "$env_key" "$env_value"
        else
            add_mcp_to_config "$mcp_name" "$mcp_command" "$final_args"
        fi

        if [ $? -eq 0 ]; then
            print_success "$mcp_name MCP 安装成功"

            # 显示配置提示
            if [ -n "$env_key" ] && [ -n "$env_value" ]; then
                echo "✅ 已配置环境变量: $env_key"
            elif [ -n "$final_args" ] && [ "$final_args" != "$mcp_args_base" ]; then
                echo "✅ 已配置参数: $(echo $final_args | sed "s/$mcp_args_base //")"
            fi
            echo "💡 重启Claude Code以使MCP配置生效"
        else
            print_error "$mcp_name MCP 安装失败"
        fi
    else
        echo "安装已取消"
    fi
}

# 12. 管理已安装的MCP（集成配置验证和作用域识别）
manage_installed_mcps() {
    print_step "功能12：管理已安装的MCP"

    # 初始化配置并检查配置文件
    init_mcp_config

    while true; do
        echo
        echo "=================================="
        echo "       MCP管理菜单"
        echo "=================================="
        echo "📁 全局配置: $MCP_GLOBAL_CONFIG"
        if [ -f "./.mcp.json" ]; then
            echo "📁 项目配置: ./.mcp.json"
        fi
        echo
        local mcp_menu_items=("1. 查看已安装的MCP" "2. 验证MCP配置格式" "3. 查看MCP配置详情" "4. 删除MCP" "5. 导出MCP配置" "6. 切换配置作用域" "7. 返回主菜单")
        display_menu_items_columns "${mcp_menu_items[@]}"
        echo

        read -p "请选择操作 [1-7]: " choice

        case $choice in
            1)
                echo
                print_step "已安装的MCP列表"

                # 显示全局配置的MCP
                MCP_CONFIG_FILE="$MCP_GLOBAL_CONFIG"
                if [ -f "$MCP_GLOBAL_CONFIG" ]; then
                    echo "🌍 全局配置 ($MCP_GLOBAL_CONFIG):"
                    get_installed_mcps
                else
                    echo "🌍 全局配置: 不存在"
                fi

                echo

                # 显示项目配置的MCP
                if [ -f "./.mcp.json" ]; then
                    MCP_CONFIG_FILE="./.mcp.json"
                    echo "📂 项目配置 (./.mcp.json):"
                    get_installed_mcps
                else
                    echo "📂 项目配置: 不存在"
                fi
                ;;
            2)
                echo
                print_step "验证MCP配置格式"

                # 验证全局配置
                echo "🌍 验证全局配置:"
                MCP_CONFIG_FILE="$MCP_GLOBAL_CONFIG"
                validate_mcp_config

                echo

                # 验证项目配置
                if [ -f "./.mcp.json" ]; then
                    echo "📂 验证项目配置:"
                    MCP_CONFIG_FILE="./.mcp.json"
                    validate_mcp_config
                else
                    echo "📂 项目配置: 不存在，跳过验证"
                fi
                ;;
            3)
                echo
                print_step "MCP配置详情"

                echo "请选择要查看的配置:"
                echo "1. 全局配置 ($MCP_GLOBAL_CONFIG)"
                if [ -f "./.mcp.json" ]; then
                    echo "2. 项目配置 (./.mcp.json)"
                fi
                echo

                read -p "请选择 [1-2]: " config_choice

                case $config_choice in
                    1)
                        if [ -f "$MCP_GLOBAL_CONFIG" ]; then
                            echo "🌍 全局配置文件内容："
                            echo "配置文件位置: $MCP_GLOBAL_CONFIG"
                            echo "详细配置内容："
                            cat "$MCP_GLOBAL_CONFIG" | python3 -m json.tool
                        else
                            echo "🌍 全局配置文件不存在"
                        fi
                        ;;
                    2)
                        if [ -f "./.mcp.json" ]; then
                            echo "📂 项目配置文件内容："
                            echo "配置文件位置: ./.mcp.json"
                            echo "详细配置内容："
                            cat "./.mcp.json" | python3 -m json.tool
                        else
                            echo "📂 项目配置文件不存在"
                        fi
                        ;;
                    *)
                        print_error "无效选项"
                        ;;
                esac
                ;;
            4)
                echo
                print_step "删除MCP"

                # 选择配置作用域
                echo "请选择要删除MCP的配置:"
                echo "1. 全局配置 ($MCP_GLOBAL_CONFIG)"
                if [ -f "./.mcp.json" ]; then
                    echo "2. 项目配置 (./.mcp.json)"
                fi
                echo

                read -p "请选择 [1-2]: " delete_scope

                case $delete_scope in
                    1)
                        MCP_CONFIG_FILE="$MCP_GLOBAL_CONFIG"
                        echo "📋 全局配置中的MCP:"
                        ;;
                    2)
                        if [ -f "./.mcp.json" ]; then
                            MCP_CONFIG_FILE="./.mcp.json"
                            echo "📋 项目配置中的MCP:"
                        else
                            print_error "项目配置文件不存在"
                            continue
                        fi
                        ;;
                    *)
                        print_error "无效选项"
                        continue
                        ;;
                esac

                get_installed_mcps
                echo
                read -p "请输入要删除的MCP名称: " mcp_to_delete
                if [ -n "$mcp_to_delete" ]; then
                    read -p "确认删除 '$mcp_to_delete' 从 $MCP_CONFIG_FILE？(y/N): " confirm_delete
                    if [[ "$confirm_delete" == "y" || "$confirm_delete" == "Y" ]]; then
                        remove_mcp_from_config "$mcp_to_delete"
                        echo "💡 重启Claude Code以使MCP配置更改生效"
                    else
                        echo "删除已取消"
                    fi
                fi
                ;;
            5)
                echo
                print_step "导出MCP配置"

                timestamp=$(date +%Y%m%d_%H%M%S)

                # 导出全局配置
                if [ -f "$MCP_GLOBAL_CONFIG" ]; then
                    backup_file="claude_global_mcp_backup_${timestamp}.json"
                    cp "$MCP_GLOBAL_CONFIG" "$backup_file"
                    print_success "全局MCP配置已导出到: $backup_file"
                fi

                # 导出项目配置
                if [ -f "./.mcp.json" ]; then
                    backup_file="project_mcp_backup_${timestamp}.json"
                    cp "./.mcp.json" "$backup_file"
                    print_success "项目MCP配置已导出到: $backup_file"
                fi

                if [ ! -f "$MCP_GLOBAL_CONFIG" ] && [ ! -f "./.mcp.json" ]; then
                    print_error "没有找到MCP配置文件"
                fi
                ;;
            6)
                echo
                print_step "切换配置作用域"
                choose_mcp_scope
                echo "✅ 配置作用域已更新为: $MCP_CONFIG_FILE"
                ;;
            7)
                break
                ;;
            *)
                print_error "无效选项，请输入1-7之间的数字"
                ;;
        esac
    done
}


# 13. 初始化项目骨架
init_project_skeleton() {
    print_step "功能13：初始化项目骨架"

    echo "🏗️  即将创建标准项目结构..."
    echo
    echo "将创建以下目录结构："
    echo "backend/      - 后端代码目录"
    echo "docs/         - 文档目录"
    echo "frontend/     - 前端代码目录"
    echo

    # 检查是否已存在这些目录
    existing_dirs=()
    if [ -d "backend" ]; then
        existing_dirs+=("backend")
    fi
    if [ -d "docs" ]; then
        existing_dirs+=("docs")
    fi
    if [ -d "frontend" ]; then
        existing_dirs+=("frontend")
    fi

    # 如果有已存在的目录，提示用户
    if [ ${#existing_dirs[@]} -gt 0 ]; then
        print_warning "检测到以下目录已存在: ${existing_dirs[*]}"
        read -p "是否继续？这可能会覆盖现有目录的内容 (y/N): " confirm_overwrite
        if [[ "$confirm_overwrite" != "y" && "$confirm_overwrite" != "Y" ]]; then
            echo "操作已取消"
            return 0
        fi
    fi

    echo
    read -p "确认创建项目骨架？(Y/n): " confirm_init
    if [[ "$confirm_init" == "n" || "$confirm_init" == "N" ]]; then
        echo "操作已取消"
        return 0
    fi

    # 创建目录结构
    echo "正在创建目录结构..."

    # 创建 backend 目录和基础文件
    mkdir -p backend/{src,tests,config,docs}
    if [ ! -f "backend/README.md" ]; then
        cat > backend/README.md << 'EOF'
# Backend 后端项目

## 项目结构
- `src/` - 源代码目录
- `tests/` - 测试文件目录
- `config/` - 配置文件目录
- `docs/` - 后端相关文档

## 开发指南
请在此添加后端项目的具体说明和开发指南。
EOF
    fi

    # 创建 frontend 目录和基础文件
    mkdir -p frontend/{src,public,tests,docs,assets}
    if [ ! -f "frontend/README.md" ]; then
        cat > frontend/README.md << 'EOF'
# Frontend 前端项目

## 项目结构
- `src/` - 源代码目录
- `public/` - 静态资源目录
- `tests/` - 测试文件目录
- `docs/` - 前端相关文档
- `assets/` - 资源文件目录

## 开发指南
请在此添加前端项目的具体说明和开发指南。
EOF
    fi

    # 创建 docs 目录和基础文件
    mkdir -p docs/{api,design,requirements}
    if [ ! -f "docs/README.md" ]; then
        cat > docs/README.md << 'EOF'
# 项目文档

## 文档结构
- `api/` - API 接口文档
- `design/` - 设计文档
- `requirements/` - 需求文档

## 文档规范
请在此添加项目的文档规范和编写指南。
EOF
    fi

    # 创建项目根目录的基础文件
    if [ ! -f "README.md" ]; then
        cat > README.md << 'EOF'
# 项目名称

## 项目简介
这是一个使用 Claude Code Router 脚手架创建的项目。

## 项目结构
```
.
├── backend/          # 后端代码
├── frontend/         # 前端代码
├── docs/            # 项目文档
└── README.md        # 项目说明
```

## 快速开始
请参考各个目录下的 README.md 文件了解具体的开发指南。

## 开发规范
本项目遵循 CLAUDE.md 中定义的开发规范和指导原则。
EOF
    fi

    print_success "项目骨架创建完成！"
    echo
    echo "📁 创建的目录结构："
    tree -L 2 2>/dev/null || find . -maxdepth 2 -type d | sort
    echo
    echo "📝 下一步操作建议："
    echo "1. 在各个目录中添加项目特定的文件"
    echo "2. 更新根目录的 README.md 文件"
    echo "3. 开始编写代码和文档"
    echo
    echo "💡 AI助手现在可以通过 CLAUDE.md 了解这个项目结构！"

    # 更新 CLAUDE.md 文件
    update_claude_md_with_structure
}

# 更新 CLAUDE.md 文件，添加项目结构指导
update_claude_md_with_structure() {
    echo
    print_step "更新 CLAUDE.md 项目结构指导"

    # 读取现有的 CLAUDE.md 内容
    existing_content=""
    if [ -f "CLAUDE.md" ]; then
        existing_content=$(cat "CLAUDE.md")
    fi

    # 创建新的 CLAUDE.md 内容
    cat > CLAUDE.md << 'EOF'
# CLAUDE.md

> Think carefully and implement the most concise solution that changes as little code as possible.

## 项目结构

本项目采用标准的前后端分离架构：

```
.
├── backend/          # 后端代码目录
│   ├── src/         # 源代码
│   ├── tests/       # 测试文件
│   ├── config/      # 配置文件
│   └── docs/        # 后端文档
├── frontend/         # 前端代码目录
│   ├── src/         # 源代码
│   ├── public/      # 静态资源
│   ├── tests/       # 测试文件
│   ├── docs/        # 前端文档
│   └── assets/      # 资源文件
├── docs/            # 项目文档目录
│   ├── api/         # API 文档
│   ├── design/      # 设计文档
│   └── requirements/ # 需求文档
└── CLAUDE.md         # AI 助手指导文件
```

## AI 开发指导原则

### 目录使用规范
- **backend/**: 所有后端相关代码，包括 API、数据库逻辑、服务层等
- **frontend/**: 所有前端相关代码，包括 UI 组件、页面、样式等
- **docs/**: 所有项目文档，包括技术文档、用户文档、设计文档等

### 开发工作流
1. 新功能开发时，优先在对应目录下创建合适的文件结构
2. 后端 API 变更需要同步更新 docs/api/ 目录下的接口文档
3. 前端组件开发需要遵循 frontend/src/ 目录的现有结构
4. 所有测试文件统一放置在对应的 tests/ 目录中

### 代码组织原则
- 保持模块化和可维护性
- 遵循各目录下的 README.md 指导
- 优先选择项目已有的技术栈和模式
- 确保代码风格与现有代码保持一致

## Project-Specific Instructions
Add your project-specific instructions here.

## Testing

Always run tests before committing:
- `npm test` or equivalent for your stack

## Code Style

Follow existing patterns in the codebase.

## Behavioral Guidelines

- Talk me in Chinese
EOF

    print_success "CLAUDE.md 已更新，现在 AI 助手可以理解项目结构！"
}

# --- 主菜单 ---
main_menu() {
    while true; do
        echo -e "\n=================================="
        echo "       🚀 AI工作站 🚀"
        echo "=================================="
        echo "🔧 环境设置:"
        local env_items=("1. 切换Node.js版本" "2. 安装ccpm" "3. 安装ccs" "4. 安装 cc-statusline")
        display_menu_items_columns "${env_items[@]}"
        echo
        echo "🚀 启动服务 (自动检测并升级):"
        local start_items=("5. 启动ccr" "6. 启动claude" "7. 启动codex")
        display_menu_items_columns "${start_items[@]}"
        echo
        echo "🛠️  MCP工具:"
        local mcp_items=("8. 一键安装常见MCP" "9. 浏览并安装MCP" "10. 管理已安装MCP")
        display_menu_items_columns "${mcp_items[@]}"
        echo
        echo "📁 项目管理:"
        local project_items=("11. 初始化项目骨架")
        display_menu_items_columns "${project_items[@]}"
        echo
        echo "0. 退出"
        read -p "请选择操作 [0-11]: " choice

        case $choice in
            1)
                switch_node_version
                ;;
            2)
                install_ccpm
                ;;
            3)
                install_ccs
                ;;
            4)
                install_cc_statusline
                ;;
            5)
                start_ccr
                ;;
            6)
                start_claude
                ;;
            7)
                start_codex
                ;;
            8)
                install_common_mcps
                ;;
            9)
                browse_mcp_list
                ;;
            10)
                manage_installed_mcps
                ;;
            11)
                init_project_skeleton
                ;;
            0)
                echo "👋 AI工作站已退出，再见！"
                break
                ;;
            *)
                print_error "无效选项，请输入0-11之间的数字。"
                ;;
        esac
    done
}

# --- 脚本入口 ---
main_menu
