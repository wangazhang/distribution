package com.hissp.distribution.module.trade.dal.redis;

/**
 * 交易 Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 交易序号的缓存
     *
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String TRADE_NO = "trade_no:";

    /**
     * 交易序号的缓存
     *
     * KEY 格式：express_track:{code-logisticsNo-receiverMobile}
     * VALUE 数据格式 String, 物流信息集合
     */
    String EXPRESS_TRACK = "express_track";

    /**
     * 团队总览的缓存
     *
     * KEY 格式：brokerage_team_overview:{userId}
     * VALUE 数据格式：String 团队总览信息
     */
    String BROKERAGE_TEAM_OVERVIEW = "brokerage_team_overview";

    /**
     * 团队成员分页的缓存
     *
     * KEY 格式：brokerage_team_member:{userId}:{type}:{level}:{nickname}:{pageNo}:{pageSize}
     * VALUE 数据格式：String 团队成员分页信息
     */
    String BROKERAGE_TEAM_MEMBER_PAGE = "brokerage_team_member";

}
