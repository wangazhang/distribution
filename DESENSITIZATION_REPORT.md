# 项目脱敏报告

## 1. 脱敏目标

用于开源发布前的全仓库脱敏，重点去除以下高风险信息：

- 真实密钥/口令/token（含配置、SQL、HTTP 请求示例）
- 私有仓库地址与凭据
- 可识别真实公网 IP、私有业务域名、私有 CDN 地址
- 真实邮箱、手机号等个人信息样例

## 2. 本次已处理内容

- 后端配置：`application.yaml`、`application-local.yaml`、`application-dev.yaml`、`application-test.yaml`、`application-prod.yaml`
  - 各类 `password/secret/api-key/client-secret` 已统一改为 `CHANGE_ME_*` 或环境变量引用。
  - 历史注释中的示例密码也已改为占位符。
- 前端环境变量：`yudao-ui-admin-vue3/.env*`、`distribution-mall-mini-vue3/.env`、`distribution-mall-mini-vue3/.e.*`
  - 私有 API/CDN 域名改为 `example.com` 体系或本地地址。
- Maven 私服配置：`src-claude-plus/backend/hissp-distribution/settings.xml`
  - 私服地址改为示例地址。
  - 账号密码改为环境变量：`MAVEN_REPO_USERNAME`、`MAVEN_REPO_PASSWORD`。
- 子模块地址：`.gitmodules`
  - 改为 GitHub 示例组织地址。
- HTTP 调试文件：`distribution-modules/**/*.http`
  - `Authorization` 全部改为 `{{token}}`/`{{appToken}}` 变量占位。
- 脚本与测试中的真实公网 IP、邮箱样例
  - `139.159.246.17` -> `203.0.113.17`
  - `192.168.9.225` -> `192.0.2.225`
  - `mp_html@126.com` -> `support@example.com`

## 3. 数据库/缓存/消息中间件账号密码

- `application-local.yaml`、`application-dev.yaml`、`application-test.yaml`、`application-prod.yaml`
  - 数据库用户名统一为 `CHANGE_ME_DB_USERNAME`
  - RabbitMQ 账号密码统一为 `CHANGE_ME_RABBITMQ_USERNAME` / `CHANGE_ME_RABBITMQ_PASSWORD`
- `src-claude-plus/devops/docker/docker-compose.yml`、`src-claude-plus/backend/hissp-distribution/sql/tools/docker-compose.yaml`
  - MySQL/PostgreSQL/Oracle/Kingbase/OpenGauss 默认口令改为 `CHANGE_ME_*`
- `src-claude-plus/devops/docker/docker.env`
  - 数据库账号与口令改为 `CHANGE_ME_DB_USERNAME` / `CHANGE_ME_MYSQL_ROOT_PASSWORD`
- `src-claude-plus/devops/docker/redis/conf/redis.conf`
  - `requirepass` 改为 `CHANGE_ME_REDIS_PASSWORD`
- `src-claude-plus/backend/hissp-distribution/sql/tools/oracle/1_create_user.sql`
  - Oracle 应用账号口令改为 `CHANGE_ME_ORACLE_APP_USER` / `CHANGE_ME_ORACLE_APP_PASSWORD`
- `src-claude-plus/backend/hissp-distribution/sql/tools/oracle/2_create_schema.sh`
  - Oracle 导入脚本账号口令改为 `CHANGE_ME_ORACLE_APP_USER` / `CHANGE_ME_ORACLE_APP_PASSWORD`

## 4. 验收扫描（已执行）

示例命令：

```bash
rg -n --hidden --glob '!.git' --glob '!**/node_modules/**' --glob '!**/dist/**' --glob '!**/target/**' \
"(saimeituo|distribution367|codeup\\.aliyun|repo-pqgoo|139\\.159\\.246\\.17|TVDBZ-TDILD|Distribution@2024|SYSDBA001|AKIA[0-9A-Z]{16}|BEGIN PRIVATE KEY)" \
src-claude-plus .gitmodules
```

```bash
rg -n -P "Authorization:\\s*(Bearer|Basic)\\s+(?!\\{\\{)[^\\s\\r\\n]+|(^|[?&])(token|refresh_token|access_token)=([A-Za-z0-9._-]{8,})" \
src-claude-plus/backend/hissp-distribution/distribution-modules/**/*.http
```

两类扫描均无高危命中。

## 5. 说明

- 仓库内仍存在部分第三方开源组件文档中的公共链接（例如 `aliyuncs.com` 文档示例地址），属于上游公开资料引用，不属于本项目私密信息。

