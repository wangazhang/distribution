-- 银行卡管理相关字典配置
-- 创建时间：2025-01-22

-- ----------------------------
-- 银行卡绑定状态字典
-- ----------------------------
DELETE FROM `system_dict_type` WHERE `type` = 'pay_bankcard_bind_status';
INSERT INTO `system_dict_type` (`id`, `type`, `name`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
(1001, 'pay_bankcard_bind_status', '银行卡绑定状态', 0, '银行卡绑定状态', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0);

DELETE FROM `system_dict_data` WHERE `dict_type` = 'pay_bankcard_bind_status';
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
(10001, 1, '申请中', '0', 'pay_bankcard_bind_status', 0, 'warning', '', '银行卡绑定申请中', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0),
(10002, 2, '绑定成功', '1', 'pay_bankcard_bind_status', 0, 'success', '', '银行卡绑定成功', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0),
(10003, 3, '绑定失败', '2', 'pay_bankcard_bind_status', 0, 'danger', '', '银行卡绑定失败', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0),
(10004, 4, '已解绑', '3', 'pay_bankcard_bind_status', 0, 'info', '', '银行卡已解绑', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0);

-- ----------------------------
-- 银行卡入网状态字典
-- ----------------------------
DELETE FROM `system_dict_type` WHERE `type` = 'pay_bankcard_network_status';
INSERT INTO `system_dict_type` (`id`, `type`, `name`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
(1002, 'pay_bankcard_network_status', '银行卡入网状态', 0, '银行卡入网状态', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0);

DELETE FROM `system_dict_data` WHERE `dict_type` = 'pay_bankcard_network_status';
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
(10011, 1, '未申请', '0', 'pay_bankcard_network_status', 0, 'info', '', '银行卡入网未申请', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0),
(10012, 2, '申请中', '1', 'pay_bankcard_network_status', 0, 'warning', '', '银行卡入网申请中', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0),
(10013, 3, '入网成功', '2', 'pay_bankcard_network_status', 0, 'success', '', '银行卡入网成功', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0),
(10014, 4, '入网失败', '3', 'pay_bankcard_network_status', 0, 'danger', '', '银行卡入网失败', '1', '2025-01-22 00:00:00', '1', '2025-01-22 00:00:00', b'0', 0);