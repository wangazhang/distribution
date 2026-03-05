# 贡献指南

感谢你参与本项目贡献。为保证协作效率，请先阅读本指南。

## 提交前准备

- Fork 并创建分支：`feat/*`、`fix/*`、`docs/*`
- 先同步主分支最新代码
- 确保本地可编译、可启动

## 开发规范

- 后端遵循阿里 Java 开发规范，保持类职责单一
- 前后端遵循 DRY/KISS 原则
- 小改动直接修改，复杂改动请拆分为多个原子提交
- 不提交真实密钥、密码、Token、证书等敏感信息
- 配置中统一使用 `CHANGE_ME_*` 或环境变量

## Commit 规范

使用 Conventional Commits：

- `feat: ...`
- `fix: ...`
- `docs: ...`
- `refactor: ...`
- `test: ...`

示例：

```text
feat: add order export API
fix: correct redis timeout handling in cache layer
docs: update open source setup instructions
```

## Pull Request 要求

PR 描述请至少包含：

- 变更背景与目标
- 主要改动点
- 影响范围（后端/前端/数据库/配置）
- 测试说明（如何验证）
- UI 变更请附截图

## 脱敏与安全

- 涉及配置、SQL、脚本改动时，必须二次检查敏感信息
- 参考 `DESENSITIZATION_REPORT.md` 中的扫描命令进行自检
- 安全问题请走 `SECURITY.md` 的私下披露流程

