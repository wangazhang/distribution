#!/bin/bash

# Shell脚本用于设置和启动Claude Code Router
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

# 4. 启动claude
start_claude() {
    print_step "功能4：启动Claude CLI (claude)"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    if ! command_exists claude; then
        print_error "未找到claude命令。请确认已正确安装 Claude CLI。"
        return 1
    fi

    echo "🎯 正在启动 Claude..."
    echo "设置 Node.js 运行内存为 16GB..."
    export NODE_OPTIONS="--max-old-space-size=16384"
    print_success "Node.js 内存已设置为 16GB"
    echo "按 Ctrl+C 停止"
    claude --dangerously-skip-permissions
}

# 5. 安装 cc-statusline
install_cc_statusline() {
    print_step "功能5：安装 cc-statusline"

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

# 6. 升级 Claude Code
upgrade_claude_code() {
    print_step "功能6：升级 Claude Code"

    # 先切换到 Node.js 22 版本
    if ! use_node_22; then
        return 1
    fi

    if ! command_exists npm; then
        print_error "未找到 npm 命令，请确认 Node.js 已正确安装。"
        return 1
    fi

    echo "🎯 正在检查 Claude Code 版本..."

    # 检查是否安装了 claude
    if command_exists claude; then
        CURRENT_VERSION=$(claude --version 2>/dev/null || echo "未知版本")
        echo "当前版本: $CURRENT_VERSION"
    else
        echo "⚠️  未检测到 Claude Code，将执行全新安装"
    fi

    echo
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

    echo
    echo "🔄 正在升级 Claude Code 到最新版本..."
    npm install -g @anthropic-ai/claude-code@latest

    # 升级后取消代理设置
    unset https_proxy http_proxy all_proxy

    if [ $? -eq 0 ]; then
        print_success "Claude Code 升级完成！"
        if command_exists claude; then
            NEW_VERSION=$(claude --version 2>/dev/null || echo "未知版本")
            echo "新版本: $NEW_VERSION"
        fi
        echo
        echo "💡 提示: 使用 'claude --version' 查看版本信息"
        echo "💡 提示: 使用菜单选项 4 启动 Claude Code"
    else
        print_error "Claude Code 升级失败，请检查网络连接或代理设置"
        return 1
    fi
}

# 7. 一键安装常见MCP（集成配置作用域选择）
install_common_mcps() {
    print_step "功能7：一键安装常见MCP"

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

# 8. 浏览MCP列表并安装（集成配置作用域选择）
browse_mcp_list() {
    print_step "功能8：浏览MCP列表并安装"

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

                printf "%2d. %-20s - %s%s\n" "$counter" "$mcp_name" "$mcp_description" "$config_hint"
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done

        echo
        echo "🛠️  开发工具MCP:"
        echo "----------------------------------------"
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

                printf "%2d. %-20s - %s%s\n" "$counter" "$mcp_name" "$mcp_description" "$config_hint"
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done

        echo
        echo "⚡ 工作流程MCP:"
        echo "----------------------------------------"
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

                printf "%2d. %-20s - %s%s\n" "$counter" "$mcp_name" "$mcp_description" "$config_hint"
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done

        echo
        echo "📈 生产力工具MCP:"
        echo "----------------------------------------"
        local productivity_mcps=("xmind")
        for mcp_name in "${productivity_mcps[@]}"; do
            mcp_info=$(get_mcp_info "$mcp_name")
            if [[ -n "$mcp_info" ]]; then
                mcp_description=$(echo "$mcp_info" | cut -d'|' -f4)

                printf "%2d. %-20s - %s 📁\n" "$counter" "$mcp_name" "$mcp_description"
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done

        echo
        echo "🔧 其他实用MCP:"
        echo "----------------------------------------"
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

                printf "%2d. %-20s - %s%s\n" "$counter" "$mcp_name" "$mcp_description" "$config_hint"
                mcp_names+=("$mcp_name")
                ((counter++))
            fi
        done

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

# 9. 管理已安装的MCP（集成配置验证和作用域识别）
manage_installed_mcps() {
    print_step "功能9：管理已安装的MCP"

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
        echo "1. 查看已安装的MCP"
        echo "2. 验证MCP配置格式"
        echo "3. 查看MCP配置详情"
        echo "4. 删除MCP"
        echo "5. 导出MCP配置"
        echo "6. 切换配置作用域"
        echo "7. 返回主菜单"
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


# --- 主菜单 ---
main_menu() {
    while true; do
        echo -e "\n=================================="
        echo "  Claude Code Router 助手脚本"
        echo "=================================="
        echo "1. 切换Node.js版本"
        echo "2. 安装ccpm"
        echo "3. 启动ccr"
        echo "4. 启动claude"
        echo "5. 安装 cc-statusline"
        echo "6. 升级 Claude Code"
        echo
        echo "--- MCP管理功能 ---"
        echo "7. 一键安装常见MCP"
        echo "8. 浏览MCP列表并安装"
        echo "9. 管理已安装MCP"
        echo
        echo "10. 退出"
        read -p "请输入选项 [1-10]: " choice

        case $choice in
            1)
                switch_node_version
                ;;
            2)
                install_ccpm
                ;;
            3)
                start_ccr
                ;;
            4)
                start_claude
                ;;
            5)
                install_cc_statusline
                ;;
            6)
                upgrade_claude_code
                ;;
            7)
                install_common_mcps
                ;;
            8)
                browse_mcp_list
                ;;
            9)
                manage_installed_mcps
                ;;
            10)
                echo "脚本退出。"
                break
                ;;
            *)
                print_error "无效选项，请输入1-10之间的数字。"
                ;;
        esac
    done
}

# --- 脚本入口 ---
main_menu