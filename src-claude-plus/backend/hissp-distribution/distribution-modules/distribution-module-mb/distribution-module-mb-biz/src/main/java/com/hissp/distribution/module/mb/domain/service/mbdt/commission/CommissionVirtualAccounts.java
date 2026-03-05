package com.hissp.distribution.module.mb.domain.service.mbdt.commission;

/**
 * 分佣虚拟账号常量。
 * <p>
 * 目前仅维护总部出资虚拟成员，后续若有更多虚拟主体，可统一放置于此，避免散落的硬编码。
 */
public final class CommissionVirtualAccounts {

    /**
     * 平台中转虚拟成员 ID。
     * <p>
     * 采用 0 作为平台账号，便于和真实代理 ID 区分。
     */
    public static final long PLATFORM_VIRTUAL_USER_ID = 0L;

    /**
     * 总部资金池虚拟成员 ID。
     */
    public static final long HQ_VIRTUAL_USER_ID = 9000000000000000L;

    /**
     * 平台层级标识。
     */
    public static final String HIERARCHY_PLATFORM = "PLATFORM";

    /**
     * 总部层级标识。
     */
    public static final String HIERARCHY_HQ = "HQ";

    private CommissionVirtualAccounts() {
        // utility class
    }
}
