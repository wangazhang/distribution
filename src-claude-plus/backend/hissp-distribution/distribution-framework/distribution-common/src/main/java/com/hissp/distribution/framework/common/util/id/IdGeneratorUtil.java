package com.hissp.distribution.framework.common.util.id;

import cn.hutool.core.util.IdUtil;

/**
 * ID生成器工具类，基于Hutool的雪花算法
 * 
 * @author hissp
 */
public class IdGeneratorUtil {

    /**
     * 全局单例雪花算法ID生成器
     * 终端ID和数据中心ID均设置为1，可根据实际需求调整
     */
    private static final cn.hutool.core.lang.Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);
    
    /**
     * 生成雪花算法ID
     * 
     * @return 雪花算法ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }
    
    /**
     * 生成带前缀的雪花算法ID字符串
     * 
     * @param prefix 前缀，如RC、MC等
     * @return 带前缀的雪花算法ID字符串
     */
    public static String nextIdStr(String prefix) {
        return prefix + SNOWFLAKE.nextIdStr();
    }
    
    /**
     * 补货单ID前缀
     */
    public static final String PREFIX_REPLENISHMENT = "RC";
    
    /**
     * 商品单ID前缀
     */
    public static final String PREFIX_MERCHANDISE = "MC";
    
    /**
     * 转化单ID前缀
     */
    public static final String PREFIX_CONVERSION = "CC";
    
    /**
     * 生成补货单ID
     */
    public static String nextReplenishmentId() {
        return nextIdStr(PREFIX_REPLENISHMENT);
    }
    
    /**
     * 生成商品单ID
     */
    public static String nextMerchandiseId() {
        return nextIdStr(PREFIX_MERCHANDISE);
    }
    
    /**
     * 生成转化单ID
     */
    public static String nextConversionId() {
        return nextIdStr(PREFIX_CONVERSION);
    }
} 