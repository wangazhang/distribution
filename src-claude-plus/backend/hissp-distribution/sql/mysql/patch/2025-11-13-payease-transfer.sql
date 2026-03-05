-- 首信易提现扩展字段：补齐 pay_transfer 表的收款人信息
ALTER TABLE `pay_transfer`
    ADD COLUMN `receiver_id` VARCHAR(64) NULL COMMENT '收款方标识（如首信易子商户号）' AFTER `user_name`,
    ADD COLUMN `receiver_type` VARCHAR(32) NULL COMMENT '收款方类型（MERCHANT_ACC 等）' AFTER `receiver_id`,
    ADD COLUMN `currency` VARCHAR(16) DEFAULT 'CNY' COMMENT '币种' AFTER `receiver_type`;

-- 备注列如果不存在可以手工补齐；若已有则忽略执行
ALTER TABLE `pay_transfer`
    ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '转账备注' AFTER `currency`;
