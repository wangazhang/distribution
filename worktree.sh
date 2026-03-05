#!/bin/bash

# Git Worktree 管理脚本
# 支持主工程和子工程的 worktree 生命周期管理
# 作者: Claude Code
# 版本: 1.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="$(basename "$SCRIPT_DIR")"
PARENT_DIR="$(dirname "$SCRIPT_DIR")"

# 日志函数
log_info() {
    echo -e "${BLUE}[信息]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[警告]${NC} $1"
}

log_error() {
    echo -e "${RED}[错误]${NC} $1"
}

# 检查是否在Git仓库中
check_git_repo() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        log_error "当前目录不是Git仓库"
        exit 1
    fi
}

# 获取子模块列表
get_submodules() {
    if [ -f ".gitmodules" ]; then
        git config --file .gitmodules --get-regexp path | awk '{ print $2 }'
    fi
}

# 检查worktree名称是否有效
validate_worktree_name() {
    local name="$1"
    if [[ -z "$name" ]]; then
        log_error "Worktree名称不能为空"
        return 1
    fi
    if [[ "$name" =~ [^a-zA-Z0-9_-] ]]; then
        log_error "Worktree名称只能包含字母、数字、下划线和连字符"
        return 1
    fi
    return 0
}

# 创建worktree
create_worktree() {
    local worktree_name="$1"
    local worktree_path="${PARENT_DIR}/${PROJECT_NAME}-${worktree_name}"
    local branch_name="worktree-${worktree_name}"

    if ! validate_worktree_name "$worktree_name"; then
        return 1
    fi

    # 检查目标目录是否已存在
    if [ -d "$worktree_path" ]; then
        log_error "目标目录已存在: $worktree_path"
        return 1
    fi

    # 检查分支是否已存在
    if git show-ref --verify --quiet refs/heads/"$branch_name"; then
        log_warning "分支 '$branch_name' 已存在，将使用现有分支"
    else
        log_info "创建新分支: $branch_name"
    fi

    log_info "创建主工程 worktree: $worktree_path"

    # 创建主工程的worktree
    if git worktree add "$worktree_path" -b "$branch_name" 2>/dev/null || git worktree add "$worktree_path" "$branch_name" 2>/dev/null; then
        log_success "主工程 worktree 创建成功"
    else
        log_error "主工程 worktree 创建失败"
        return 1
    fi

    # 处理子模块
    local submodules=($(get_submodules))
    if [ ${#submodules[@]} -gt 0 ]; then
        log_info "发现 ${#submodules[@]} 个子模块，开始创建子模块 worktree..."

        # 切换到主工程worktree目录
        cd "$worktree_path"

        # 为每个子模块在原位置创建worktree
        for submodule in "${submodules[@]}"; do
            local sub_branch_name="worktree-${worktree_name}"
            local submodule_worktree_path="${worktree_path}/${submodule}"
            local submodule_repo_path="${SCRIPT_DIR}/${submodule}"
            local submodule_dir="$(dirname "$submodule")"

            log_info "处理子模块: $submodule"
            log_info "为子模块创建 worktree: $submodule_worktree_path"

            # 确保子模块的父目录存在
            if [ ! -d "$submodule_dir" ]; then
                mkdir -p "$submodule_dir"
            fi

            # 检查子模块原始仓库是否存在
            if [ -d "$submodule_repo_path" ]; then
                cd "$submodule_repo_path"

                # 检查是否是Git仓库
                if [ -d ".git" ] || [ -f ".git" ]; then
                    # 检查子模块分支是否存在
                    if git show-ref --verify --quiet refs/heads/"$sub_branch_name" 2>/dev/null; then
                        log_info "子模块分支 '$sub_branch_name' 已存在，将使用现有分支"
                        if git worktree add "$submodule_worktree_path" "$sub_branch_name" 2>/dev/null; then
                            log_success "子模块 worktree 创建成功（使用现有分支）: $submodule"
                        else
                            log_warning "无法为子模块创建worktree: $submodule"
                            continue
                        fi
                    else
                        log_info "为子模块创建新分支和worktree: $sub_branch_name"
                        if git worktree add "$submodule_worktree_path" -b "$sub_branch_name" 2>/dev/null; then
                            log_success "子模块 worktree 创建成功（新分支）: $submodule"
                        else
                            log_warning "无法为子模块创建worktree: $submodule"
                            continue
                        fi
                    fi
                else
                    log_warning "子模块 '$submodule' 不是有效的Git仓库"
                    cd "$worktree_path"
                    continue
                fi

                # 回到主工程worktree目录
                cd "$worktree_path"
            else
                log_warning "子模块原始仓库不存在: $submodule_repo_path"
                # 如果子模块原始仓库不存在，使用传统的子模块初始化
                git submodule update --init "$submodule" 2>/dev/null || log_warning "无法初始化子模块: $submodule"
            fi
        done
    else
        log_info "未发现子模块"
    fi

    # 返回原始目录
    cd "$SCRIPT_DIR"

    log_success "Worktree '$worktree_name' 创建完成"
    log_info "路径: $worktree_path"
    log_info "结构说明："
    log_info "  - 主工程: $worktree_path"
    if [ ${#submodules[@]} -gt 0 ]; then
        log_info "  - 子模块 worktree (在原位置):"
        for submodule in "${submodules[@]}"; do
            local submodule_worktree_path="${worktree_path}/${submodule}"
            if [ -d "$submodule_worktree_path" ]; then
                log_info "    └── $submodule: worktree-${worktree_name}"
            else
                log_info "    └── $submodule: 使用普通子模块模式"
            fi
        done
    fi
    echo
}

# 删除worktree
delete_worktree() {
    local input_name="$1"
    local force_mode="${2:-false}"
    local worktree_name=""
    local worktree_path=""
    local branch_name=""

    if ! validate_worktree_name "$input_name"; then
        return 1
    fi

    # 智能识别输入格式
    if [[ "$input_name" == *"/"* ]]; then
        # 如果输入包含路径分隔符，假设是完整路径
        worktree_path="$input_name"
        if [[ "$worktree_path" == *"${PROJECT_NAME}-"* ]]; then
            worktree_name="${worktree_path##*${PROJECT_NAME}-}"
        else
            log_error "无法从路径中提取 worktree 名称: $worktree_path"
            return 1
        fi
    else
        # 否则假设是worktree名称
        worktree_name="$input_name"
        worktree_path="${PARENT_DIR}/${PROJECT_NAME}-${worktree_name}"
    fi

    branch_name="worktree-${worktree_name}"

    # 检查worktree是否存在
    if ! git worktree list | grep -q "$worktree_path"; then
        log_error "Worktree '$worktree_name' (路径: $worktree_path) 不存在"
        log_info "可用的 worktree 列表："
        git worktree list | grep -v "$(pwd)" | while read -r path branch rest; do
            if [[ "$path" == *"${PROJECT_NAME}-"* ]]; then
                local available_name="${path##*${PROJECT_NAME}-}"
                echo "  - $available_name"
            fi
        done
        return 1
    fi

    log_info "删除 worktree: $worktree_name (路径: $worktree_path)"

    # 获取子模块列表（从当前主项目）
    local submodules=($(get_submodules))

    # 如果worktree目录存在，处理子模块分支删除
    if [ -d "$worktree_path" ]; then
        log_info "处理子模块 worktree 删除..."
        for submodule in "${submodules[@]}"; do
            local submodule_worktree_path="${worktree_path}/${submodule}"
            local submodule_repo_path="${SCRIPT_DIR}/${submodule}"
            local sub_branch_name="worktree-${worktree_name}"

            log_info "处理子模块: $submodule"

            # 检查子模块worktree是否存在
            if [ -d "$submodule_worktree_path" ]; then
                # 先在原始仓库中删除worktree
                if [ -d "$submodule_repo_path" ]; then
                    cd "$submodule_repo_path"
                    if [ -d ".git" ] || [ -f ".git" ]; then
                        # 删除子模块的worktree
                        if git worktree remove "$submodule_worktree_path" --force 2>/dev/null; then
                            log_success "子模块 worktree 删除成功: $submodule"
                        else
                            log_warning "无法删除子模块 worktree: $submodule"
                        fi

                        # 删除子模块的worktree分支
                        if git show-ref --verify --quiet refs/heads/"$sub_branch_name" 2>/dev/null; then
                            log_info "删除子模块分支: $sub_branch_name"
                            if git branch -D "$sub_branch_name" 2>/dev/null; then
                                log_success "子模块分支删除成功: $sub_branch_name"
                            else
                                log_warning "无法删除子模块分支: $sub_branch_name"
                            fi
                        fi

                        # 清理子模块的worktree记录
                        git worktree prune 2>/dev/null || true
                    fi
                    cd "$SCRIPT_DIR"
                fi
            fi
        done
    fi

    # 尝试正常删除主工程的worktree
    log_info "删除主工程 worktree..."
    local delete_success=false

    if git worktree remove "$worktree_path" 2>/dev/null; then
        log_success "Worktree 删除成功"
        delete_success=true
    elif git worktree remove "$worktree_path" --force 2>/dev/null; then
        log_success "Worktree 强制删除成功"
        delete_success=true
    else
        log_warning "无法使用 git worktree remove 删除，尝试手动删除..."

        # 手动删除目录
        if [ -d "$worktree_path" ]; then
            # 确保没有进程占用目录
            if command -v lsof >/dev/null 2>&1; then
                local processes=$(lsof +D "$worktree_path" 2>/dev/null | wc -l)
                if [ "$processes" -gt 0 ]; then
                    log_warning "检测到有进程正在使用 worktree 目录"
                    if [[ "$force_mode" == "true" ]]; then
                        log_info "强制模式：继续删除"
                    else
                        echo -n "是否强制删除? (y/N): "
                        read -r force_confirm
                        if [[ ! "$force_confirm" =~ ^[Yy]$ ]]; then
                            log_info "删除操作已取消"
                            return 1
                        fi
                    fi
                fi
            fi

            # 更改权限并删除
            chmod -R 755 "$worktree_path" 2>/dev/null || true
            if rm -rf "$worktree_path" 2>/dev/null; then
                log_success "目录手动删除成功"
                delete_success=true
            else
                log_error "手动删除目录失败: $worktree_path"
            fi
        fi
    fi

    # 删除主工程的分支
    if git show-ref --verify --quiet refs/heads/"$branch_name" 2>/dev/null; then
        log_info "删除主工程分支: $branch_name"
        if git branch -D "$branch_name" 2>/dev/null; then
            log_success "主工程分支删除成功"
        else
            log_warning "无法删除主工程分支: $branch_name"
        fi
    fi

    # 清理可能遗留的worktree记录
    log_info "清理 worktree 记录..."
    git worktree prune 2>/dev/null || true

    if [[ "$delete_success" == "true" ]]; then
        log_success "Worktree '$worktree_name' 删除完成"
    else
        log_error "Worktree '$worktree_name' 删除失败"

        # 提供手动清理建议
        echo
        log_info "手动清理步骤："
        log_info "1. 删除目录: sudo rm -rf '$worktree_path'"
        log_info "2. 清理Git记录: git worktree prune"
        log_info "3. 手动删除Git记录: rm -rf .git/worktrees/$(basename "$worktree_path")"
        log_info "4. 删除分支: git branch -D '$branch_name'"
        echo

        # 提供强制删除选项
        if [[ "$force_mode" != "true" ]]; then
            echo -n "是否尝试强制删除? (y/N): "
            read -r force_retry
            if [[ "$force_retry" =~ ^[Yy]$ ]]; then
                log_info "尝试强制删除..."
                delete_worktree "$input_name" "true"
            fi
        fi
    fi
}

# 列出所有worktree
list_worktrees() {
    log_info "当前所有 Worktree："
    echo

    local worktree_list=$(git worktree list)
    echo "$worktree_list"
    echo

    # 显示可用的worktree名称（用于删除）
    log_info "可删除的 Worktree 名称："
    git worktree list | grep -v "$(pwd)" | while read -r path branch rest; do
        if [[ "$path" == *"${PROJECT_NAME}-"* ]]; then
            local worktree_name="${path##*${PROJECT_NAME}-}"
            echo "  - $worktree_name (完整路径: $path)"

            # 检查是否有对应的子模块worktree
            local submodules=($(get_submodules))
            for submodule in "${submodules[@]}"; do
                local submodule_worktree_path="${path}/${submodule}"
                if [ -d "$submodule_worktree_path" ]; then
                    echo "    └── 子模块 $submodule: worktree-${worktree_name}"
                fi
            done
        fi
    done
    echo

    # 统计信息
    local total_count=$(echo "$worktree_list" | wc -l)
    log_info "总共 $total_count 个 worktree"
    echo
}

# 交互式菜单
interactive_menu() {
    while true; do
        echo
        echo "=========================================="
        echo "           Git Worktree 管理工具"
        echo "=========================================="
        echo "1. 创建新的 Worktree"
        echo "2. 删除 Worktree"
        echo "3. 查看所有 Worktree"
        echo "4. 退出"
        echo "=========================================="
        echo -n "请选择操作 (1-4): "

        read -r choice

        case $choice in
            1)
                echo
                echo -n "请输入 Worktree 名称: "
                read -r worktree_name
                if [ -n "$worktree_name" ]; then
                    create_worktree "$worktree_name"
                else
                    log_error "名称不能为空"
                fi
                ;;
            2)
                echo
                list_worktrees
                echo -n "请输入要删除的 Worktree 名称 (如: feature-01): "
                read -r worktree_name
                if [ -n "$worktree_name" ]; then
                    echo -n "确认删除 '$worktree_name'? (y/N): "
                    read -r confirm
                    if [[ "$confirm" =~ ^[Yy]$ ]]; then
                        delete_worktree "$worktree_name"
                    else
                        log_info "删除操作已取消"
                    fi
                else
                    log_error "名称不能为空"
                fi
                ;;
            3)
                echo
                list_worktrees
                ;;
            4)
                log_info "再见！"
                exit 0
                ;;
            *)
                log_error "无效选择，请输入 1-4"
                ;;
        esac
    done
}

# 显示帮助信息
show_help() {
    echo "Git Worktree 管理脚本"
    echo
    echo "用法:"
    echo "  $0                    # 启动交互式菜单"
    echo "  $0 add <name>         # 创建新的 worktree"
    echo "  $0 del <name>         # 删除指定的 worktree"
    echo "  $0 del-force <name>   # 强制删除指定的 worktree"
    echo "  $0 list               # 列出所有 worktree"
    echo "  $0 help               # 显示帮助信息"
    echo
    echo "示例:"
    echo "  $0 add feature-01     # 创建名为 feature-01 的 worktree"
    echo "  $0 del feature-01     # 删除名为 feature-01 的 worktree"
    echo "  $0 del-force feature-01 # 强制删除名为 feature-01 的 worktree"
    echo
    echo "注意:"
    echo "  - Worktree 将创建在与当前项目同级的目录中"
    echo "  - 会自动处理所有 Git 子模块"
    echo "  - 分支名称格式: worktree-<name>"
    echo "  - 强制删除会跳过确认步骤并尝试清理所有相关文件"
}

# 主程序
main() {
    # 检查Git仓库
    check_git_repo

    # 解析命令行参数
    case "${1:-}" in
        "add")
            if [ -z "${2:-}" ]; then
                log_error "请提供 worktree 名称"
                echo "用法: $0 add <name>"
                exit 1
            fi
            create_worktree "$2"
            ;;
        "del"|"delete")
            if [ -z "${2:-}" ]; then
                log_error "请提供 worktree 名称"
                echo "用法: $0 del <name>"
                exit 1
            fi
            delete_worktree "$2"
            ;;
        "del-force"|"delete-force")
            if [ -z "${2:-}" ]; then
                log_error "请提供 worktree 名称"
                echo "用法: $0 del-force <name>"
                exit 1
            fi
            delete_worktree "$2" "true"
            ;;
        "list"|"ls")
            list_worktrees
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        "")
            interactive_menu
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口
main "$@"