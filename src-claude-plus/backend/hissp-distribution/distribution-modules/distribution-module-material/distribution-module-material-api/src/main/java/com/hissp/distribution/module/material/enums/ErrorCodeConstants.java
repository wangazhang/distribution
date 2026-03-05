package com.hissp.distribution.module.material.enums;

import com.hissp.distribution.framework.common.exception.ErrorCode;

/**
 * Material 错误码枚举类
 * 
 * material 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 物料定义相关 1-003-001-000 ==========
    ErrorCode MATERIAL_DEFINITION_NOT_EXISTS = new ErrorCode(1_003_001_000, "物料定义不存在");
    ErrorCode MATERIAL_DEFINITION_CODE_DUPLICATE = new ErrorCode(1_003_001_001, "物料编码已存在");
    ErrorCode MATERIAL_DEFINITION_NAME_DUPLICATE = new ErrorCode(1_003_001_002, "物料名称已存在");
    ErrorCode MATERIAL_DEFINITION_SPU_LINKED = new ErrorCode(1_003_001_003, "SPU已被其他物料关联");
    ErrorCode MATERIAL_DEFINITION_TYPE_NOT_ALLOW = new ErrorCode(1_003_001_004, "物料类型不允许此操作");

    // ========== 物料余额相关 1-003-002-000 ==========
    ErrorCode MATERIAL_BALANCE_NOT_EXISTS = new ErrorCode(1_003_002_000, "物料余额不存在");
    ErrorCode MATERIAL_BALANCE_NOT_ENOUGH = new ErrorCode(1_003_002_001, "物料余额不足");
    ErrorCode MATERIAL_BALANCE_NEGATIVE = new ErrorCode(1_003_002_002, "物料余额不能为负数");

    // ========== 物料出库相关 1-003-003-000 ==========
    ErrorCode MATERIAL_OUTBOUND_NOT_EXISTS = new ErrorCode(1_003_003_000, "物料出库单不存在");
    ErrorCode MATERIAL_OUTBOUND_STATUS_NOT_ALLOW = new ErrorCode(1_003_003_001, "物料出库单状态不允许操作");
    ErrorCode MATERIAL_OUTBOUND_ITEM_NOT_EXISTS = new ErrorCode(1_003_003_002, "物料出库明细不存在");

    // ========== 物料转化相关 1-003-004-000 ==========
    ErrorCode MATERIAL_CONVERT_RULE_NOT_EXISTS = new ErrorCode(1_003_004_000, "物料转化规则不存在");
    ErrorCode MATERIAL_CONVERT_RULE_EXISTS = new ErrorCode(1_003_004_001, "物料转化规则已存在");
    ErrorCode MATERIAL_CONVERT_RULE_HAS_RECORDS = new ErrorCode(1_003_004_002, "物料转化规则存在关联记录，无法删除");
    ErrorCode MATERIAL_NOT_SUPPORT_CONVERT = new ErrorCode(1_003_004_003, "物料不支持转化");
    ErrorCode MATERIAL_CONVERT_TARGET_NOT_CONFIGURED = new ErrorCode(1_003_004_004, "未配置物料转化目标");
    ErrorCode MATERIAL_CONVERT_PRICE_INVALID = new ErrorCode(1_003_004_005, "物料转化单价无效");

}
