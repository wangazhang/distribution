#!/bin/bash
# 多线程文件传输工具的Shell包装器

# 脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PYTHON_SCRIPT="$SCRIPT_DIR/multi_thread_file_transfer.py"

# 显示帮助信息
function show_help {
    echo "多线程文件传输工具"
    echo
    echo "用法: $(basename $0) [选项]"
    echo
    echo "选项:"
    echo "  -h, --help          显示帮助信息"
    echo "  -H, --host HOST     远程服务器地址（必需）"
    echo "  -P, --port PORT     SSH端口号，默认为22"
    echo "  -u, --user USER     SSH用户名（必需）"
    echo "  -p, --password PWD  SSH密码"
    echo "  -k, --keyfile FILE  SSH私钥文件路径"
    echo "  -r, --remote DIR    远程目标目录，默认为/tmp"
    echo "  -w, --workers NUM   工作线程数，默认为5"
    echo "  -f, --file FILE     要传输的文件路径（可多次指定）"
    echo "  -d, --dir DIR       要传输的目录路径（可多次指定）"
    echo "  --preserve-path     保留相对路径结构"
    echo
    echo "示例:"
    echo "  $(basename $0) -H 192.168.1.100 -u root -p password -f /path/to/file.txt -r /data"
    echo "  $(basename $0) -H 192.168.1.100 -u root -k ~/.ssh/id_rsa -d /path/to/dir -r /data --preserve-path"
    echo
    exit 0
}

# 检查Python脚本是否存在
if [ ! -f "$PYTHON_SCRIPT" ]; then
    echo "错误: 找不到Python脚本 '$PYTHON_SCRIPT'"
    exit 1
fi

# 检查Python是否可用
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到python3命令"
    exit 1
fi

# 检查依赖是否安装
if ! python3 -c "import paramiko, tqdm" &> /dev/null; then
    echo "正在安装所需依赖..."
    pip install paramiko tqdm || {
        echo "错误: 无法安装依赖"
        exit 1
    }
fi

# 参数数组
ARGS=()

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -H|--host)
            ARGS+=("--host" "$2")
            shift 2
            ;;
        -P|--port)
            ARGS+=("--port" "$2")
            shift 2
            ;;
        -u|--user)
            ARGS+=("--username" "$2")
            shift 2
            ;;
        -p|--password)
            ARGS+=("--password" "$2")
            shift 2
            ;;
        -k|--keyfile)
            ARGS+=("--key-file" "$2")
            shift 2
            ;;
        -r|--remote)
            ARGS+=("--remote-dir" "$2")
            shift 2
            ;;
        -w|--workers)
            ARGS+=("--workers" "$2")
            shift 2
            ;;
        -f|--file)
            # 收集所有连续的文件参数
            FILE_ARGS=()
            FILE_ARGS+=("$2")
            shift 2
            while [[ $# -gt 0 && ! $1 =~ ^- ]]; do
                FILE_ARGS+=("$1")
                shift
            done
            ARGS+=("--files" "${FILE_ARGS[@]}")
            ;;
        -d|--dir)
            # 收集所有连续的目录参数
            DIR_ARGS=()
            DIR_ARGS+=("$2")
            shift 2
            while [[ $# -gt 0 && ! $1 =~ ^- ]]; do
                DIR_ARGS+=("$1")
                shift
            done
            ARGS+=("--dirs" "${DIR_ARGS[@]}")
            ;;
        --preserve-path)
            ARGS+=("--preserve-path")
            shift
            ;;
        *)
            echo "错误: 未知选项 $1"
            show_help
            ;;
    esac
done

# 检查必要参数
if [[ ! " ${ARGS[@]} " =~ " --host " ]]; then
    echo "错误: 缺少必要参数 -H/--host"
    show_help
fi

if [[ ! " ${ARGS[@]} " =~ " --username " ]]; then
    echo "错误: 缺少必要参数 -u/--user"
    show_help
fi

if [[ ! " ${ARGS[@]} " =~ " --files " && ! " ${ARGS[@]} " =~ " --dirs " ]]; then
    echo "错误: 必须指定至少一个文件(-f)或目录(-d)"
    show_help
fi

if [[ ! " ${ARGS[@]} " =~ " --password " && ! " ${ARGS[@]} " =~ " --key-file " ]]; then
    echo "错误: 必须提供密码(-p)或私钥文件(-k)"
    show_help
fi

# 执行Python脚本
python3 "$PYTHON_SCRIPT" "${ARGS[@]}" 