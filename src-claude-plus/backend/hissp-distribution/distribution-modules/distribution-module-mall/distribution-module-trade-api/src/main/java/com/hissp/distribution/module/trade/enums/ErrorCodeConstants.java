package com.hissp.distribution.module.trade.enums;

import com.hissp.distribution.framework.common.exception.ErrorCode;

/**
 * Trade 错误码枚举类
 * trade 系统，使用 1-011-000-000 段
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ErrorCodeConstants {

    // ========== Order 模块 1-011-000-000 ==========
    ErrorCode ORDER_ITEM_NOT_FOUND = new ErrorCode(1_011_000_010, "交易订单项不存在");
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1_011_000_011, "交易订单不存在");
    ErrorCode ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL = new ErrorCode(1_011_000_012, "交易订单项更新售后状态失败，请重试");
    ErrorCode ORDER_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(1_011_000_013, "交易订单更新支付状态失败，订单不是【未支付】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(1_011_000_014, "交易订单更新支付状态失败，支付单编号不匹配");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_015, "交易订单更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(1_011_000_016, "交易订单更新支付状态失败，支付单金额不匹配");
    ErrorCode ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED = new ErrorCode(1_011_000_017, "交易订单发货失败，订单不是【待发货】状态");
    ErrorCode ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(1_011_000_018, "交易订单收货失败，订单不是【待收货】状态");
    ErrorCode ORDER_COMMENT_FAIL_STATUS_NOT_COMPLETED = new ErrorCode(1_011_000_019, "创建交易订单项的评价失败，订单不是【已完成】状态");
    ErrorCode ORDER_COMMENT_STATUS_NOT_FALSE = new ErrorCode(1_011_000_020, "创建交易订单项的评价失败，订单已评价");
    ErrorCode ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE = new ErrorCode(1_011_000_021, "交易订单发货失败，订单已退款或部分退款");
    ErrorCode ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_022, "交易订单发货失败，拼团未成功");
    ErrorCode ORDER_DELIVERY_FAIL_BARGAIN_RECORD_STATUS_NOT_SUCCESS = new ErrorCode(1_011_000_023, "交易订单发货失败，砍价未成功");
    ErrorCode ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS = new ErrorCode(1_011_000_024, "交易订单发货失败，发货类型不是快递");
    ErrorCode ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID = new ErrorCode(1_011_000_025, "交易订单取消失败，订单不是【待支付】状态");
    ErrorCode ORDER_UPDATE_PRICE_FAIL_PAID = new ErrorCode(1_011_000_026, "支付订单调价失败，原因：支付订单已付款,不能调价");
    ErrorCode ORDER_UPDATE_PRICE_FAIL_ALREADY = new ErrorCode(1_011_000_027, "支付订单调价失败，原因：已经修改过价格");
    ErrorCode ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR = new ErrorCode(1_011_000_028, "支付订单调价失败，原因：调整后支付价格不能小于 0.01 元");
    ErrorCode ORDER_DELETE_FAIL_STATUS_NOT_CANCEL = new ErrorCode(1_011_000_029, "交易订单删除失败，订单不是【已取消】状态");
    ErrorCode ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP = new ErrorCode(1_011_000_030, "交易订单自提失败，收货方式不是【用户自提】");
    ErrorCode ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(1_011_000_031, "交易订单修改收货地址失败，原因：订单不是【待发货】状态");
    ErrorCode ORDER_CREATE_FAIL_EXIST_UNPAID = new ErrorCode(1_011_000_032, "交易订单创建失败，原因：存在未付款订单");
    ErrorCode ORDER_CANCEL_PAID_FAIL = new ErrorCode(1_011_000_033, "交易订单取消支付失败，原因：订单不是【{}】状态");
    ErrorCode ORDER_PICK_UP_FAIL_NOT_VERIFY_USER = new ErrorCode(1_011_000_034, "交易订单自提失败，原因：你没有核销该门店订单的权限");
    ErrorCode ORDER_CREATE_FAIL_INSUFFICIENT_USER_POINTS = new ErrorCode(1_011_000_035, "交易订单创建失败，原因：用户积分不足");
    ErrorCode ORDER_CREATE_FAIL_PRIVILEGE_USER_LEVEL_BEYOND = new ErrorCode(1_011_000_036, "交易订单创建失败，原因：资格过高，无法开通");
    ErrorCode ORDER_CREATE_FAIL_PRIVILEGE_INVITER_LEVEL_LOWER = new ErrorCode(1_011_000_037, "交易订单创建失败，原因：上级等级过低，无法开通");
    ErrorCode ORDER_CHANNEL_SHIPPING_WAITING = new ErrorCode(1_011_000_038, "支付成功未满一分钟，暂无法推送渠道发货");
    ErrorCode ORDER_CHANNEL_SHIPPING_ALREADY = new ErrorCode(1_011_000_039, "渠道已存在发货记录，无需重复推送");
    ErrorCode ORDER_REFUND_FAIL_STATUS_NOT_PAID = new ErrorCode(1_011_000_040, "交易订单未支付，无法退款");
    ErrorCode ORDER_REFUND_FAIL_ALREADY = new ErrorCode(1_011_000_041, "交易订单已发起退款，请勿重复操作");
    ErrorCode ORDER_REFUND_FAIL_PAYMENT_NOT_FOUND = new ErrorCode(1_011_000_042, "交易订单缺少支付信息，无法退款");
    ErrorCode ORDER_REFUND_FAIL_PRICE_INVALID = new ErrorCode(1_011_000_043, "交易订单退款金额异常");
    ErrorCode ORDER_REFUND_FAIL_PASSWORD_INVALID = new ErrorCode(1_011_000_044, "退款密码错误");

    // ========== After Sale 模块 1-011-000-100 ==========
    ErrorCode AFTER_SALE_NOT_FOUND = new ErrorCode(1_011_000_100, "售后单不存在");
    ErrorCode AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR = new ErrorCode(1_011_000_101, "申请退款金额错误");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED = new ErrorCode(1_011_000_102, "订单已关闭，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID = new ErrorCode(1_011_000_103, "订单未支付，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED = new ErrorCode(1_011_000_104, "订单未发货，无法申请【退货退款】售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED = new ErrorCode(1_011_000_105, "订单项已申请售后，无法重复申请");
    ErrorCode AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY = new ErrorCode(1_011_000_106, "审批失败，售后状态不处于审批中");
    ErrorCode AFTER_SALE_UPDATE_STATUS_FAIL = new ErrorCode(1_011_000_107, "操作售后单失败，请刷新后重试");
    ErrorCode AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE = new ErrorCode(1_011_000_108, "退货失败，售后单状态不处于【待买家退货】");
    ErrorCode AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY = new ErrorCode(1_011_000_109, "确认收货失败，售后单状态不处于【待确认收货】");
    ErrorCode AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND = new ErrorCode(1_011_000_110, "退款失败，售后单状态不是【待退款】");
    ErrorCode AFTER_SALE_CANCEL_FAIL_STATUS_NOT_APPLY_OR_AGREE_OR_BUYER_DELIVERY =
            new ErrorCode(1_011_000_111, "取消售后单失败，售后单状态不是【待审核】或【卖家同意】或【商家待收货】");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_COMBINATION_IN_PROGRESS = new ErrorCode(1_011_000_112, "订单拼团中，无法申请售后");
    ErrorCode AFTER_SALE_REFUND_FAIL_PRICE_ERROR = new ErrorCode(1_011_000_113, "退款金额错误");
    ErrorCode AFTER_SALE_REFUND_FAIL_PRICE_EXCEEDS_PAY_PRICE = new ErrorCode(1_011_000_114, "退款金额不能超过订单项的支付金额");
    ErrorCode AFTER_SALE_REFUND_FAIL_ORDER_NOT_PAID = new ErrorCode(1_011_000_115, "退款失败，订单未支付");
    ErrorCode AFTER_SALE_REFUND_FAIL_RESOURCE_NOT_ENOUGH = new ErrorCode(1_011_000_116, "退款失败，用户物料资源不足");
    // ========== Material 模块 1-011-010-000 ==========
    ErrorCode MATERIAL_DEFINITION_NOT_EXISTS = new ErrorCode(1_011_010_000, "物料定义不存在");

    ErrorCode AFTER_SALE_REFUND_FAIL_COMMISSION_ALREADY_SETTLED = new ErrorCode(1_011_000_117, "退款失败，已产生已结算佣金，不支持自动退款");

    // ========== Cart 模块 1-011-002-000 ==========
    ErrorCode CARD_ITEM_NOT_FOUND = new ErrorCode(1_011_002_000, "购物车项不存在");

    // ========== Price 相关 1-011-003-000 ============
    ErrorCode PRICE_CALCULATE_PAY_PRICE_ILLEGAL = new ErrorCode(1_011_003_000, "支付价格计算异常，原因：价格小于等于 0");
    ErrorCode PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND = new ErrorCode(1_011_003_001, "计算快递运费异常，找不到对应的运费模板");
    ErrorCode PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER = new ErrorCode(1_011_003_002, "参与秒杀、拼团、砍价的营销商品，无法使用优惠劵");
    ErrorCode PRICE_CALCULATE_SECKILL_TOTAL_LIMIT_COUNT = new ErrorCode(1_011_003_003, "参与秒杀的商品，超过了秒杀总限购数量");
    ErrorCode PRICE_CALCULATE_POINT_TOTAL_LIMIT_COUNT = new ErrorCode(1_011_003_004, "参与积分活动的商品，超过了积分活动商品总限购数量");
    ErrorCode PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL = new ErrorCode(1_011_003_005, "计算快递运费异常，配送方式不匹配");
    ErrorCode PRICE_CALCULATE_COUPON_CAN_NOT_USE = new ErrorCode(1_011_003_006, "该优惠劵无法使用，原因：{}」");

    // ========== 物流 Express 模块 1-011-004-000 ==========
    ErrorCode EXPRESS_NOT_EXISTS = new ErrorCode(1_011_004_000, "快递公司不存在");
    ErrorCode EXPRESS_CODE_DUPLICATE = new ErrorCode(1_011_004_001, "已经存在该编码的快递公司");
    ErrorCode EXPRESS_CLIENT_NOT_PROVIDE = new ErrorCode(1_011_004_002, "需要接入快递服务商，比如【快递100】");
    ErrorCode EXPRESS_STATUS_NOT_ENABLE = new ErrorCode(1_011_004_003, "快递公司未启用");

    ErrorCode EXPRESS_API_QUERY_ERROR = new ErrorCode(1_011_004_101, "快递查询接口异常");
    ErrorCode EXPRESS_API_QUERY_FAILED = new ErrorCode(1_011_004_102, "快递查询返回失败，原因：{}");

    // ========== 物流 Template 模块 1-011-005-000 ==========
    ErrorCode EXPRESS_TEMPLATE_NAME_DUPLICATE = new ErrorCode(1_011_005_000, "已经存在该运费模板名");
    ErrorCode EXPRESS_TEMPLATE_NOT_EXISTS = new ErrorCode(1_011_005_001, "运费模板不存在");

    // ==========  物流 PICK_UP 模块 1-011-006-000 ==========
    ErrorCode PICK_UP_STORE_NOT_EXISTS = new ErrorCode(1_011_006_000, "自提门店不存在");
    ErrorCode PICK_UP_STORE_STAFF_NOT_EXISTS = new ErrorCode(1_011_006_000, "自提门店店员不存在");

    // ========== 分销用户 模块 1-011-007-000 ==========
    ErrorCode BROKERAGE_USER_NOT_EXISTS = new ErrorCode(1_011_007_000, "分销用户不存在");
    ErrorCode BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH = new ErrorCode(1_011_007_001, "用户冻结佣金({})数量不足");
    ErrorCode BROKERAGE_BIND_SELF = new ErrorCode(1_011_007_002, "不能绑定自己");
    ErrorCode BROKERAGE_BIND_USER_NOT_ENABLED = new ErrorCode(1_011_007_003, "绑定用户没有推广资格");
    ErrorCode BROKERAGE_BIND_CONDITION_ADMIN = new ErrorCode(1_011_007_004, "仅可在后台绑定推广员");
    ErrorCode BROKERAGE_BIND_MODE_REGISTER = new ErrorCode(1_011_007_005, "只有在注册时可以绑定");
    ErrorCode BROKERAGE_BIND_OVERRIDE = new ErrorCode(1_011_007_006, "已绑定了推广人");
    ErrorCode BROKERAGE_BIND_LOOP = new ErrorCode(1_011_007_007, "下级不能绑定自己的上级");
    ErrorCode BROKERAGE_USER_LEVEL_NOT_SUPPORT = new ErrorCode(1_011_007_008, "目前只支持 level 小于等于 2");
    ErrorCode BROKERAGE_CREATE_USER_EXISTS = new ErrorCode(1_011_007_009, "分销用户已存在");
    ErrorCode BROKERAGE_RECORD_NOT_EXISTS = new ErrorCode(1_011_007_010, "佣金记录不存在");

    // ========== 分销提现 模块 1-011-008-000 ==========
    ErrorCode BROKERAGE_WITHDRAW_NOT_EXISTS = new ErrorCode(1_011_008_000, "佣金提现记录不存在");
    ErrorCode BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING = new ErrorCode(1_011_008_001, "佣金提现记录状态不是审核中");
    ErrorCode BROKERAGE_WITHDRAW_MIN_PRICE = new ErrorCode(1_011_008_002, "提现金额不能低于 {} 元");
    ErrorCode BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH = new ErrorCode(1_011_008_003, "您当前最多可提现 {} 元");
    ErrorCode BROKERAGE_WITHDRAW_STATUS_NOT_FINANCE_PAYING = new ErrorCode(1_011_008_004, "佣金提现记录状态不是财务打款中");
    ErrorCode BROKERAGE_WITHDRAW_ACCOUNT_REQUIRED = new ErrorCode(1_011_008_005, "请先完善提现信息并通过审核");
    ErrorCode BROKERAGE_WITHDRAW_CHANNEL_PAY_UNSUPPORTED = new ErrorCode(1_011_008_006, "当前提现类型不支持渠道打款");
    ErrorCode BROKERAGE_WITHDRAW_CHANNEL_PAY_STATUS_INVALID = new ErrorCode(1_011_008_007, "当前状态不允许渠道打款");
    ErrorCode BROKERAGE_WITHDRAW_CHANNEL_TRANSFER_NOT_FOUND = new ErrorCode(1_011_008_008, "暂无渠道打款记录");
    ErrorCode BROKERAGE_WITHDRAW_CHANNEL_PASSWORD_INVALID = new ErrorCode(1_011_008_009, "打款口令错误");

    // ========== 分销提现账户 模块 1-011-009-000 ==========
    ErrorCode BROKERAGE_WITHDRAW_ACCOUNT_NOT_EXISTS = new ErrorCode(1_011_009_000, "佣金提现账户不存在");

}
