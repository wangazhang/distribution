# SpringBoot应用Docker部署工具

这是一个用于将SpringBoot JAR应用以Docker容器方式部署的工具集。

## 功能特点

- 自动备份部署文件并创建带时间戳的压缩包
- 智能备份管理（保留至少5个备份且至少保留3个月）
- 智能停止已运行的应用，避免部署冲突
- 使用Docker和Docker Compose进行容器化部署
- 支持Java 21运行环境
- 支持SpringBoot配置文件环境切换
- 自动配置容器与宿主机服务（MySQL、Redis）的连接
- 健康检查和自动重启功能
- 灵活的参数配置

## 系统要求

- Linux或macOS操作系统
- Docker及Docker Compose已安装
- 拥有对指定目录的读写权限

## 文件说明

- `deploy.sh`: 部署脚本
- `docker-compose.yml`: Docker Compose配置文件

## 使用方法

### 1. 赋予脚本执行权限

```bash
chmod +x deploy.sh
```

### 2. 基本用法

```bash
./deploy.sh -p /path/to/jar/directory
```

### 3. 高级用法

```bash
./deploy.sh -p /path/to/jar/directory -n my-application -b /path/to/backups -port 48080 -profile dev
```

### 4. 参数说明

| 参数 | 说明 | 是否必填 | 默认值 |
|------|------|---------|--------|
| -p, --path | JAR文件所在的目录 | 是 | - |
| -b, --backup | 备份目录路径 | 否 | ./backups |
| -n, --name | 应用名称 | 否 | java-app |
| -port | 应用端口 | 否 | 8080 |
| -profile | SpringBoot环境配置 | 否 | prod |
| -h, --help | 显示帮助信息 | 否 | - |

## 部署流程

脚本执行以下步骤进行部署：

1. 创建带时间戳的备份压缩包
2. 管理历史备份文件（保留最新5个和3个月内的备份）
3. 查找并确认JAR文件
4. 检查Docker Compose配置文件
5. 检测操作系统类型
6. 停止已运行的同名应用（检查容器和端口占用）
7. 启动新的Docker容器运行应用

## 备份策略

脚本会自动创建带时间戳的压缩备份包，并遵循以下备份管理策略：

1. 所有备份文件保存在指定的备份目录中
2. 每个备份都是一个带有时间戳的tar.gz格式压缩包
3. 自动清理过期备份，但保证：
   - 至少保留5个最新备份
   - 至少保留最近3个月内的所有备份
4. 备份文件命名格式：`应用名称_YYYYMMDD_HHMMSS.tar.gz`

## 宿主机服务连接

部署的容器会自动配置与宿主机服务的连接：

- 在macOS和Windows上，使用Docker的`host.docker.internal`特性自动连接宿主机
- 应用可以通过环境变量`MYSQL_HOST`和`REDIS_HOST`访问宿主机上的MySQL和Redis服务
- 无需手动配置IP地址，适用于不同的开发和部署环境

## 示例

### 部署Spring Boot应用

```bash
./deploy.sh -p /opt/myapp -n spring-service -port 8081 -profile test
```

### 指定自定义备份目录

```bash
./deploy.sh -p /opt/myapp -b /data/backups -n api-service -port 9000
```

### 查看应用日志

```bash
docker-compose logs -f java-app
```

## 注意事项

1. 如果指定目录中有多个JAR文件，脚本将使用找到的第一个JAR文件
2. 确保Docker和Docker Compose已正确安装并配置
3. 端口冲突可能导致容器启动失败（脚本会尝试自动解决冲突）
4. docker-compose.yml文件应与部署脚本在同一目录
5. SpringBoot应用需要支持actuator健康检查功能(/actuator/health端点)
6. 备份目录需要有足够的存储空间
7. 脚本会自动检测并停止已运行的应用，需要确保有足够的权限
8. 宿主机上应已配置并启动MySQL和Redis服务
9. 在Linux系统上，可能需要额外配置以允许容器访问宿主机服务

## 故障排除

1. 如果容器无法启动，请检查日志：
   ```bash
   docker-compose logs
   ```

2. 如果端口被占用，可以使用不同端口：
   ```bash
   ./deploy.sh -p /path/to/jar -port 9090
   ```

3. 如果应用需要特定的SpringBoot配置文件，可以使用profile参数：
   ```bash
   ./deploy.sh -p /path/to/jar -profile dev
   ```

4. 如果需要调整JVM参数，可以修改docker-compose.yml中的JAVA_TOOL_OPTIONS环境变量

5. 如果自动停止应用失败，可能需要手动停止：
   ```bash
   docker stop <容器名称>
   ```

6. 如果容器无法连接宿主机服务，可以检查应用的连接配置是否正确使用环境变量：
   ```
   spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/your_database
   spring.redis.host=${REDIS_HOST:localhost}
   ```
