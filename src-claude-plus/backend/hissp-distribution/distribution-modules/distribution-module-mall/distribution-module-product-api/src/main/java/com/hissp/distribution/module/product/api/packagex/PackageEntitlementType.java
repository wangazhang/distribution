package com.hissp.distribution.module.product.api.packagex;

/**
 * 套包权益类型编码
 * 采用 Long 数值编码，便于与数据库 item_id 保持一致
 */
public final class PackageEntitlementType {
    private PackageEntitlementType() {}
    // 开通分销资格
    public static final long OPEN_BROKERAGE = 1L;
    // 升级会员等级（extJson 需提供 levelId）
    public static final long SET_LEVEL = 2L;
}

