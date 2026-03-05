-- 调整分佣策略表结构，支持配置多个商品/套包
CREATE TABLE IF NOT EXISTS `mb_commission_policy_scope_rel` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `policy_id` bigint NOT NULL COMMENT '策略ID',
    `scope_type` tinyint NOT NULL COMMENT '作用类型：1-商品 2-套包',
    `target_id` bigint NOT NULL COMMENT '关联ID（商品SPU或套包ID）',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_policy_scope_rel_policy` (`policy_id`, `scope_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣策略作用范围表';

-- 迁移历史数据（若存在）
INSERT INTO `mb_commission_policy_scope_rel` (`policy_id`, `scope_type`, `target_id`)
SELECT `id`, 1, `applicable_product_id`
FROM `mb_commission_policy`
WHERE `applicable_product_id` IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `mb_commission_policy_scope_rel`
      WHERE `policy_id` = `mb_commission_policy`.`id`
        AND `scope_type` = 1
        AND `target_id` = `mb_commission_policy`.`applicable_product_id`
  );

INSERT INTO `mb_commission_policy_scope_rel` (`policy_id`, `scope_type`, `target_id`)
SELECT `id`, 2, `applicable_package_id`
FROM `mb_commission_policy`
WHERE `applicable_package_id` IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `mb_commission_policy_scope_rel`
      WHERE `policy_id` = `mb_commission_policy`.`id`
        AND `scope_type` = 2
        AND `target_id` = `mb_commission_policy`.`applicable_package_id`
  );

ALTER TABLE `mb_commission_policy`
    DROP COLUMN `applicable_product_id`,
    DROP COLUMN `applicable_package_id`;

DROP TABLE IF EXISTS `mb_commission_policy_product_rel`;
DROP TABLE IF EXISTS `mb_commission_policy_package_rel`;
