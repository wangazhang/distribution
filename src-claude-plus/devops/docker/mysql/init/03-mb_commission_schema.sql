-- 分佣策略/规则基础结构（首版）

DROP TABLE IF EXISTS `mb_commission_policy`;
CREATE TABLE `mb_commission_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `policy_code` varchar(64) NOT NULL COMMENT '策略英文编码，唯一',
  `display_name` varchar(128) NOT NULL COMMENT '策略展示名称',
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/ONLINE/OFFLINE',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `biz_type` varchar(64) NOT NULL COMMENT '业务类型，例如 restock',
  `applicable_level` varchar(64) DEFAULT NULL COMMENT '适用用户等级（可多值，逗号分隔）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_policy_code` (`policy_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣策略头表';

DROP TABLE IF EXISTS `mb_commission_policy_scope_rel`;
CREATE TABLE `mb_commission_policy_scope_rel` (
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

DROP TABLE IF EXISTS `mb_commission_rule_definition`;
CREATE TABLE `mb_commission_rule_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `policy_id` bigint NOT NULL COMMENT '所属策略',
  `rule_code` varchar(64) NOT NULL COMMENT '规则内部编码',
  `display_name` varchar(128) NOT NULL COMMENT '展示名称（客户可见）',
  `status` varchar(16) NOT NULL DEFAULT 'ONLINE' COMMENT '状态：DRAFT/ONLINE/OFFLINE',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级，数值越小优先级越高',
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

DROP TABLE IF EXISTS `mb_commission_rule_condition`;
CREATE TABLE `mb_commission_rule_condition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '关联的分佣规则ID',
  `condition_type` varchar(64) NOT NULL COMMENT '条件类型',
  `operator` varchar(16) NOT NULL COMMENT '运算符 EQ/IN/BETWEEN/ALWAYS/GE/LE 等',
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

DROP TABLE IF EXISTS `mb_commission_rule_action`;
CREATE TABLE `mb_commission_rule_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '关联的分佣规则ID',
  `action_type` varchar(64) NOT NULL COMMENT '动作类型 COMMISSION/CREDIT/COUPON 等',
  `amount_mode` varchar(16) DEFAULT NULL COMMENT '金额模式 FIXED/PERCENTAGE',
  `amount_value` decimal(18,4) DEFAULT NULL COMMENT '金额值',
  `cap_value` decimal(18,4) DEFAULT NULL COMMENT '封顶金额',
  `payload_json` json DEFAULT NULL COMMENT '扩展配置 JSON',
  `priority` int NOT NULL DEFAULT 1 COMMENT '执行顺序，越小越先执行',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_rule_action` (`rule_id`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣规则奖励动作';

DROP TABLE IF EXISTS `mb_commission_rule_material`;
CREATE TABLE `mb_commission_rule_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '关联的分佣规则ID',
  `policy_id` bigint NOT NULL COMMENT '策略ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `material_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物料名称',
  `material_code` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物料编码',
  `material_image` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物料图片',
  `material_unit` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物料单位',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '发放数量',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分佣规则物料奖励表';
