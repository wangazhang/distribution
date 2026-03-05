## 首信易分账/提现集成说明

1. **新增支付渠道**
   - 在【支付管理 -> 渠道管理】中新建 `payease_account` 渠道，类型选择“账户服务渠道”，填入首信易证书配置（商户号、私钥、公钥、partnerId）。
   - 仍可保留 `payease_wx_*`、`payease_alipay` 等支付渠道，不受影响。

2. **配置自动提现打款渠道**
   - 在业务配置文件中设置 `distribution.trade.order.withdraw-transfer-channel-code=payease_account`，若不设置默认仍走原来的 `wx_lite` 渠道。
   - 佣金提现审核通过后会根据该配置自动调用 PayTransferApi，走首信易账户间转账接口。
   - 在【支付管理 → 应用信息】中为目标应用配置“账户服务渠道”（`payease_account`），系统会自动在实名/提现流程中选择该应用绑定的渠道；若未指定应用，则回退到配置项 `distribution.pay.default-account-app-key` 指定的应用。
   - 账户入网/申报的回调地址可通过 `distribution.pay.account-declaration-notify-url` 统一配置（为空则复用 `order-notify-url`），方便后续接入其他渠道时共用。

3. **提现账户字段映射**
   - `trade_brokerage_withdraw.accountNo` 需存放首信易子商户号（或钱包号），系统将其作为 `receiverId`。
   - 可根据实际需要将 `bankName` 映射为 `receiverType`（目前默认 `MERCHANT_ACC`，如需钱包请在变更后扩展）。

4. **数据库变更**
   - 新增 `pay_transfer.remark/receiver_id/receiver_type/currency/depute_mark` 五个字段，请执行 `sql/mysql/ruoyi-vue-pro.sql` 中的相应变更或编写增量迁移脚本。

5. **通知地址**
   - 首信易后台需将账户间转账、提现回调指向 `/pay/notify/transfer/{channelId}`，已经支持 header 中的 `encryptKey`。

6. **常见问题**
   - 如果 `accountNo` 为空会抛出 `BROKERAGE_WITHDRAW_ACCOUNT_NOT_EXISTS`，请补齐子商户号。
   - 如果未配置 `payease_account` 渠道，仍会回退到旧渠道类型，需要注意渠道与转账类型匹配。
