-- 分佣规则引擎改造：新增规则定义/条件/动作表并迁移旧配置数据

START TRANSACTION;

-- 0. 备份旧规则表，便于回滚
CREATE TABLE IF NOT EXISTS `mb_commission_rule_legacy` LIKE `mb_commission_rule`;
INSERT INTO `mb_commission_rule_legacy`
SELECT * FROM `mb_commission_rule`
WHERE NOT EXISTS (
    SELECT 1 FROM `mb_commission_rule_legacy` legacy WHERE legacy.id = `mb_commission_rule`.id
);

-- 1. 建表（若不存在）
CREATE TABLE IF NOT EXISTS `mb_commission_rule_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `policy_id` bigint NOT NULL COMMENT '所属策略',
  `rule_code` varchar(64) NOT NULL COMMENT '规则内部编码',
  `display_name` varchar(128) NOT NULL COMMENT '展示名称（客户可见）',
  `status` varchar(16) NOT NULL DEFAULT 'ONLINE' COMMENT '状态：DRAFT/ONLINE/OFFLINE',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级，数值越小越高',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `effect_scope` json DEFAULT NULL COMMENT '命中范围摘要 JSON',
  `metadata` json DEFAULT NULL COMMENT '额外信息 JSON',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_policy_rule_code` (`policy_id`, `rule_code`),
  KEY `idx_policy_status_priority` (`policy_id`, `status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣规则定义';

CREATE TABLE IF NOT EXISTS `mb_commission_rule_condition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '关联的分佣规则ID',
  `condition_type` varchar(64) NOT NULL COMMENT '条件类型',
  `operator` varchar(16) NOT NULL COMMENT '运算符',
  `value_json` json NOT NULL COMMENT '条件值 JSON',
  `extra_json` json DEFAULT NULL COMMENT '补充信息 JSON',
  `priority` int NOT NULL DEFAULT 1 COMMENT '优先级，越小越先判断',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_rule_condition` (`rule_id`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣规则命中条件';

CREATE TABLE IF NOT EXISTS `mb_commission_rule_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '关联的分佣规则ID',
  `action_type` varchar(64) NOT NULL COMMENT '动作类型',
  `amount_mode` varchar(16) DEFAULT NULL COMMENT '金额模式',
  `amount_value` decimal(18,4) DEFAULT NULL COMMENT '金额值',
  `cap_value` decimal(18,4) DEFAULT NULL COMMENT '封顶金额',
  `payload_json` json DEFAULT NULL COMMENT '扩展配置 JSON',
  `priority` int NOT NULL DEFAULT 1 COMMENT '执行顺序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_rule_action` (`rule_id`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣规则奖励动作';

-- 2. 迁移主数据
INSERT INTO `mb_commission_rule_definition`
(`id`, `policy_id`, `rule_code`, `display_name`, `status`, `priority`, `version_no`,
 `effect_scope`, `metadata`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  mr.id,
  mr.policy_id,
  mr.inner_name,
  mr.display_name,
  IF(mr.enabled = b'1', 'ONLINE', 'OFFLINE'),
  mr.priority,
  1,
  JSON_OBJECT(
    'targetHierarchy', mr.target_hierarchy,
    'targetLevel', mr.target_level,
    'fundAccount', mr.fund_account,
    'amountSource', mr.amount_source
  ),
  JSON_OBJECT(
    'legacyAmountMode', mr.amount_mode,
    'legacyAmountValue', mr.amount_value
  ),
  mr.remark,
  mr.creator,
  mr.create_time,
  mr.updater,
  mr.update_time,
  mr.deleted
FROM `mb_commission_rule` mr
WHERE NOT EXISTS (
    SELECT 1 FROM `mb_commission_rule_definition` def WHERE def.id = mr.id
);

-- 3. 迁移条件
INSERT INTO `mb_commission_rule_condition`
(`rule_id`, `condition_type`, `operator`, `value_json`, `priority`)
SELECT
  mr.id,
  'TARGET_HIERARCHY',
  'EQ',
  JSON_OBJECT('values', JSON_ARRAY(mr.target_hierarchy)),
  10
FROM `mb_commission_rule` mr
WHERE mr.target_hierarchy IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `mb_commission_rule_condition` cond
    WHERE cond.rule_id = mr.id AND cond.condition_type = 'TARGET_HIERARCHY'
);

INSERT INTO `mb_commission_rule_condition`
(`rule_id`, `condition_type`, `operator`, `value_json`, `priority`)
SELECT
  mr.id,
  'TARGET_LEVEL',
  CASE WHEN mr.target_level = 'ALL' THEN 'ALWAYS' ELSE 'EQ' END,
  CASE WHEN mr.target_level = 'ALL'
       THEN JSON_OBJECT('always', TRUE)
       ELSE JSON_OBJECT('values', JSON_ARRAY(mr.target_level))
  END,
  20
FROM `mb_commission_rule` mr
WHERE NOT EXISTS (
    SELECT 1 FROM `mb_commission_rule_condition` cond
    WHERE cond.rule_id = mr.id AND cond.condition_type = 'TARGET_LEVEL'
);

INSERT INTO `mb_commission_rule_condition`
(`rule_id`, `condition_type`, `operator`, `value_json`, `priority`)
SELECT
  mr.id,
  'INVITE_ORDER',
  'ALWAYS',
  JSON_OBJECT('always', TRUE),
  30
FROM `mb_commission_rule` mr
WHERE NOT EXISTS (
    SELECT 1 FROM `mb_commission_rule_condition` cond
    WHERE cond.rule_id = mr.id AND cond.condition_type = 'INVITE_ORDER'
);

-- 4. 迁移动作
INSERT INTO `mb_commission_rule_action`
(`rule_id`, `action_type`, `amount_mode`, `amount_value`, `cap_value`, `payload_json`, `priority`)
SELECT
  mr.id,
  'COMMISSION',
  mr.amount_mode,
  mr.amount_value,
  NULL,
  JSON_OBJECT(
    'fundAccount', mr.fund_account,
    'amountSource', mr.amount_source,
    'bizCode', mr.biz_code,
    'fundBizCode', mr.fund_biz_code
  ),
  1
FROM `mb_commission_rule` mr
WHERE NOT EXISTS (
    SELECT 1 FROM `mb_commission_rule_action` act
    WHERE act.rule_id = mr.id AND act.action_type = 'COMMISSION'
);

-- 5. 更新物料外键
ALTER TABLE `mb_commission_rule_material`
  DROP FOREIGN KEY `fk_commission_rule_material_rule`;

COMMIT;
