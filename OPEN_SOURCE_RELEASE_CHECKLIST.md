# 开源发布检查清单

## 必做

- [ ] 确认仓库默认分支名称（建议 `main`）
- [ ] 再次执行 `DESENSITIZATION_REPORT.md` 中的脱敏扫描命令
- [ ] 人工抽查配置、SQL、脚本，确认无真实账号密码
- [ ] 配置仓库描述、Topics、README 首页信息
- [ ] 开启 Issue、PR、Discussions（按团队策略）

## 建议

- [ ] 配置分支保护（至少保护默认分支）
- [ ] 启用 Secret Scanning / Push Protection（平台支持时）
- [ ] 增加 CI（后端构建、前端构建、基础检查）
- [ ] 配置自动发布流程（Tag -> Release Notes）
- [ ] 补充 Roadmap（中长期规划）

## 需人工确认的占位信息

以下占位值在正式开源前建议替换为真实可用的项目联系方式：

- `SECURITY.md` 中的 `security@example.com`
- `CODE_OF_CONDUCT.md` 中的 `maintainer@example.com`
- `README.md` 中如果有组织名、域名、演示地址占位

