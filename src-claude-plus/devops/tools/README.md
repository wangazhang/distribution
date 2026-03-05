# 多线程文件传输工具

这是一个使用Python实现的多线程文件传输工具，可以将本地文件或目录传输到远程服务器。

## 功能特点

- 多线程并行传输，提高传输效率
- 支持通过密码或SSH密钥认证
- 自动创建远程目录结构
- 支持传输单个文件或整个目录
- 可选是否保留目录结构
- 实时显示传输进度条

## 依赖安装

使用前请先安装所需依赖：

```bash
pip install paramiko tqdm
```

## 使用方法

### 基本用法

```bash
# 使用密码认证传输单个文件
python multi_thread_file_transfer.py -H 远程服务器地址 -u 用户名 -p 密码 -f 本地文件路径 -r 远程目录路径

# 使用SSH密钥传输整个目录
python multi_thread_file_transfer.py -H 远程服务器地址 -u 用户名 -k 私钥文件路径 -d 本地目录路径 -r 远程目录路径
```

### 参数说明

| 参数 | 说明 |
|------|------|
| `-H, --host` | 远程服务器地址（必需） |
| `-P, --port` | SSH端口号，默认为22 |
| `-u, --username` | SSH用户名（必需） |
| `-p, --password` | SSH密码 |
| `-k, --key-file` | SSH私钥文件路径 |
| `-r, --remote-dir` | 远程目标目录，默认为/tmp |
| `-w, --workers` | 工作线程数，默认为5 |
| `-f, --files` | 要传输的文件路径列表 |
| `-d, --dirs` | 要传输的目录路径列表 |
| `--preserve-path` | 保留相对路径结构 |

**注意**: 必须提供密码或私钥文件中的一个进行认证，并且至少指定一个文件或目录进行传输。

### 示例

```bash
# 传输单个文件到远程服务器的/data目录
python multi_thread_file_transfer.py -H 192.168.1.100 -u root -p password -f /path/to/local/file.txt -r /data

# 使用10个线程传输多个文件
python multi_thread_file_transfer.py -H 192.168.1.100 -u root -p password -f /path/to/file1.txt /path/to/file2.txt -r /data -w 10

# 传输整个目录到远程服务器，保留目录结构
python multi_thread_file_transfer.py -H 192.168.1.100 -u root -k ~/.ssh/id_rsa -d /path/to/local/dir -r /data --preserve-path
```

## 数据库密码重置工具

此脚本用于将所有数据库默认密码修改为统一密码（CHANGE_ME_DB_PASSWORD），包括MySQL和Redis等。

### 功能特点

- 自动搜索项目中的配置文件
- 支持多种配置文件格式（YAML, Properties, Env, Redis配置等）
- 同时修改多种数据库密码（MySQL, Redis等）
- 交互式操作，支持自动确认模式

### 使用方法

```bash
# 基本用法
./reset_db_passwords.sh

# 自动确认，不提示
./reset_db_passwords.sh --yes

# 显示帮助信息
./reset_db_passwords.sh --help
```

## 数据库配置批量部署工具

此脚本结合了多线程文件传输和密码重置功能，可以一键完成数据库配置的修改和部署。

### 功能特点

- 同时支持密码重置和文件传输
- 支持仅重置密码或仅传输文件
- 多线程传输提高效率
- 支持SSH密码或密钥认证
- 自动确认模式简化操作

### 使用方法

```bash
# 基本用法（重置密码并传输）
./db_config_deploy.sh -H 192.168.1.100 -u root -p password -s ./configs -r /opt/app/configs

# 仅重置密码，不传输
./db_config_deploy.sh --reset-only

# 仅传输文件，不重置密码
./db_config_deploy.sh -H 192.168.1.100 -u root -p password -s ./configs -r /opt/app/configs --transfer-only

# 设置自定义数据库密码
./db_config_deploy.sh -H 192.168.1.100 -u root -p password -s ./configs -r /opt/app/configs -d mypassword
```

### 参数说明

| 参数 | 说明 |
|------|------|
| `-H, --host` | 远程服务器地址 |
| `-P, --port` | SSH端口号，默认为22 |
| `-u, --user` | SSH用户名 |
| `-p, --password` | SSH密码 |
| `-k, --keyfile` | SSH私钥文件路径 |
| `-s, --source` | 本地源目录，包含要传输的配置文件 |
| `-r, --remote` | 远程目标目录 |
| `-w, --workers` | 工作线程数，默认为5 |
| `-d, --db-password` | 设置所有数据库密码，默认为CHANGE_ME_DB_PASSWORD |
| `--preserve-path` | 保留相对路径结构 |
| `--reset-only` | 只在本地重置密码，不传输到远程服务器 |
| `--transfer-only` | 只传输文件，不重置密码 |
| `--yes` | 自动确认所有操作，不提示 |

## 注意事项

1. 确保远程服务器的SSH服务正常运行
2. 使用私钥认证时，确保私钥文件权限正确（通常为600）
3. 传输大量小文件时，建议适当增加线程数以提高效率
4. 传输大文件时，可能需要较长时间，请耐心等待
5. 修改数据库密码后，需要重启相关服务才能生效 