package com.hissp.distribution.module.mb.constants;

/**
 * MB订单业务类型常量
 *
 * @author azhanga
 */
public class MbOrderBizType {
    
    /**
     * 补货业务类型
     */
    public static final String RESTOCK = "restock";
    
    /**
     * 物料转化业务类型
     */
    public static final String MATERIAL_CONVERT = "materialConvert";

    /**
     * @deprecated 请使用 {@link #MATERIAL_CONVERT}
     */
    @Deprecated
    public static final String COLLAGEN_CONVERT = "collagenConvert";
    
}
