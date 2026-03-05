package com.hissp.distribution.module.mb.enums;

import com.hissp.distribution.framework.common.exception.ErrorCode;

// TODO 待办：请将下面的错误码复制到 distribution-module-mb-api 模块的 ErrorCodeConstants 类中。注意，请给"TODO 补充编号"设置一个错误码编号！！！
// ========== 物料维度表，存储物料的基本信息 TODO 补充编号 ==========
public interface ErrorCodeConstants {
    ErrorCode MATERIAL_NOT_EXISTS = new ErrorCode(1000, "物料维度表，存储物料的基本信息不存在");
    ErrorCode MATERIAL_DETAIL_NOT_EXISTS = new ErrorCode(1001, "物料明细事实表，记录物料的获得和使用明细信息不存在");
    ErrorCode USER_MATERIAL_BALANCE_NOT_EXISTS = new ErrorCode(10002, "用户物料余额事实表，记录用户每种物料的余额信息不存在");
    ErrorCode USER_MATERIAL_BALANCE_UPDATE_CONFLICT = new ErrorCode(10003, "用户物料余额更新冲突，请稍后重试");
    ErrorCode USER_MATERIAL_BALANCE_PARAM_ERROR = new ErrorCode(10004, "用户物料余额更新参数错误");
    ErrorCode USER_MATERIAL_BALANCE_NOT_ENOUGH = new ErrorCode(10005, "用户物料余额不足");
    ErrorCode MATERIAL_REFILL_MAP_NOT_EXISTS = new ErrorCode(10006, "补货关系不存在");
    ErrorCode MATERIAL_RESTOCK_PRICE_NOT_EXISTS = new ErrorCode(10007, "补货价格表：记录不同用户等级对应的商品补货价格不存在");
    ErrorCode USER_MATERIAL_ORDER_NOT_EXISTS = new ErrorCode(10008, "补货订单：补货订单不存在");
    ErrorCode RESTOCK_COMMISSION_RULE_NOT_EXISTS = new ErrorCode(10009, "补货佣金规则不存在");
    ErrorCode COMMISSION_POLICY_NOT_EXISTS = new ErrorCode(12001, "分佣策略不存在");
    ErrorCode COMMISSION_RULE_NOT_EXISTS = new ErrorCode(12002, "分佣规则不存在");
    ErrorCode LEVEL_BENEFIT_MAPPING_NOT_EXISTS = new ErrorCode(10010, "会员等级-权益映射不存在");

    ErrorCode MATERIAL_OUTBOUND_ITEM_NOT_EXISTS = new ErrorCode(10011, "物料出库明细不存在");
    ErrorCode MATERIAL_OUTBOUND_NOT_EXISTS = new ErrorCode(10012, "物料出库单不存在");
    ErrorCode MATERIAL_OUTBOUND_STATUS_ERROR = new ErrorCode(10013, "物料出库单状态不正确");
    ErrorCode MATERIAL_OUTBOUND_ADDRESS_NOT_EXISTS = new ErrorCode(10014, "出库单重地址不存在");

    ErrorCode MATERIAL_OUTBOUND_NO_PERMISSION = new ErrorCode(10015, "没有权限操作该物料出库单");
    
    // 添加物料出库相关的错误码
    ErrorCode MATERIAL_OUTBOUND_ITEMS_EMPTY = new ErrorCode(10016, "物料出库明细不能为空");
   ErrorCode MATERIAL_OUTBOUND_INVALID_QUANTITY = new ErrorCode(10017, "物料出库数量无效");
   ErrorCode USER_MATERIAL_BALANCE_NOT_FOUND = new ErrorCode(10018, "用户物料库存不存在");
    ErrorCode MATERIAL_OUTBOUND_INVALID_MATERIAL = new ErrorCode(10019, "包含不允许出库的物料");
    ErrorCode MATERIAL_DEFINITION_NOT_EXISTS = new ErrorCode(10020, "物料定义不存在");
    ErrorCode MATERIAL_NOT_SUPPORT_CONVERT = new ErrorCode(10021, "物料不支持转化");
    ErrorCode MATERIAL_CONVERT_TARGET_NOT_CONFIGURED = new ErrorCode(10022, "物料未配置转化成品");
    ErrorCode MATERIAL_CONVERT_PRICE_NOT_CONFIGURED = new ErrorCode(10028, "物料未配置转化价格");
    ErrorCode MATERIAL_NOT_SUPPORT_RESTOCK = new ErrorCode(10023, "物料不支持补货");
    ErrorCode MB_ORDER_STATUS_INVALID = new ErrorCode(10024, "订单状态不允许该操作");
    ErrorCode MB_ORDER_STATUS_TRANSITION_INVALID = new ErrorCode(10025, "订单状态不允许流转到目标状态");
    ErrorCode MB_ORDER_PAYMENT_NOT_EXISTS = new ErrorCode(10026, "订单支付信息不存在，无法执行该操作");
    ErrorCode MB_ORDER_PAYMENT_CHANNEL_UNSUPPORTED = new ErrorCode(10027, "支付渠道不支持虚拟发货");
    ErrorCode FUNDING_USER_BALANCE_NOT_ENOUGH = new ErrorCode(10029, "团队库存不足，请先补充货源");
    ErrorCode MB_ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(10030, "订单暂未发货，无法确认收货");
    ErrorCode MB_ORDER_REFUND_PASSWORD_INVALID = new ErrorCode(10031, "退款密码错误");
}
