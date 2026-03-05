package com.hissp.distribution.module.mb.constants;

import lombok.Getter;

/**
 * #MB 各种配置和常量
 */
public class MbConstants {

    /**
     * 产品信息
     */
    public static class MbProductInfo {
        /**
         * 蛋白产品 spu_id & Material_id
         */
        public final static Long MB_MATERIAL_DANBAICHANPING_ID = 1L;
        /**
         * 蛋白原料 spu_id & Material_id
         */
        public final static Long MB_MATERIAL_DANBAIYUANLIAO_ID = 90000L;
        /// **
        // * 滚针spu_id & Material_id
        // */
        // public final static Long MB_MATERIAL_GUNZHEN_ID = 2L;
        /// **
        // * 蛋白原料转产品 spu_id & Material_id
        // */
        // public final static Long MB_MATERIAL_YL2PD_ID = 646L;
        /// **
        // * ECM面膜 spu_id & Material_id
        // */
        // public final static Long MB_MATERIAL_MIANMO_ID = 647L;
        /**
         * Mb普通产品的分类
         */
        public final static Long MB_NORMAL_GOODS_CATEGORY_ROOT_ID = 1L;
        /**
         * Mb代理阶跃产品的分类
         */
        public final static Long MB_AGENT_OBTAIN_GOODS_CATEGORY_ROOT_ID = 88L;
        /**
         * Mb代理补货产品的分类
         */
        public final static Long MB_AGENT_RESTOCK_GOODS_CATEGORY_ROOT_ID = 89L;

    }

    /**
     * 游客信息
     */
    public static class MbTouristLevelInfo {
        /**
         * 游客 level ID
         */
        public final static Long MB_TOURIST_LEVEL_ID = 1L;
        /**
         * 游客 level
         */
        public final static Integer MB_TOURIST_LEVEL = 1;

    }
    /**
     * VIP信息
     */
    public static class MbVIPLevelInfo {
        /**
         * VIP level ID
         */
        public final static Long MB_VIP_LEVEL_ID = 2L;
        /**
         * VIP level
         */
        public final static Integer MB_VIP_LEVEL = 2;

    }

    /**
     * 分公司信息
     */
    public static class MbBOSSLevelInfo {
        /**
         * 分公司 level ID
         */
        public final static Long MB_SUB_COM_LEVEL_ID = 4L;
        /**
         * 分公司 level
         */
        public final static Integer MB_SUB_COM_LEVEL = 4;
        /**
         * 原因 分公司 升级
         */
        public final static String MB_BOSS_LEVEL_REASON = "分公司事业直升";

    }

    /**
     * SVIP美学师信息
     */
    public static class MbSVIPLevelInfo {
        /**
         * SVIP美学师 SPU ID
         */
        public final static Long MB_SVIP_SPU_ID = 99999L;
        /**
         * SVIP美学师 product_category
         */
        public final static Long MB_SVIP_CATEGORY_ID = 87L;
        /**
         * SVIP美学师 level ID
         */
        public final static Long MB_SVIP_LEVEL_ID = 3L;
        /**
         * SVIP美学师 level
         */
        public final static Integer MB_SVIP_LEVEL = 3;
        /**
         * SVIP美学师 升级 原因
         */
        public final static String MB_SVIP_LEVEL_REASON = "SVIP美学师事业在线直升";

    }

    /**
     * 物料来源
     */
    @Getter
    public enum MbBalanceSourceTypeEnum {
        // 采购增加
        ADD_RESTOCK(1, "补货增加"), ADD_REWARD(11, "奖励增加"),
        // 转化增加
        ADD_CONVERT(12, "转化增加"),
        // 下级补货退款
        ADD_SUBORDINATE_REFUND(13, "下级退款退回"),
        // 下级购买减少
        DECREASE_SUBORDINATE_BUY(3, "下级购买减少"),
        // 转化减少
        DECREASE_CONVERT(4, "转化减少"),
        // 出库申请
        DECREASE_OUTBOUND_APPLY(5, "出库申请减少"),
        // 出库退回
        ADD_OUTBOUND_RETURN(13, "出库退回增加"),
        // 退款回退奖励
        DECREASE_REFUND_REWARD(14, "退款回退奖励减少"),
        // 补货退款减少
        DECREASE_RESTOCK_REFUND(15, "补货退款减少");

        private final Integer sourceTypeId;
        private final String name;

        MbBalanceSourceTypeEnum(Integer sourceType, String name) {
            this.sourceTypeId = sourceType;
            this.name = name;
        }
    }

    /**
     * 物料操作类型
     */
    @Getter
    public enum MbBalanceActionTypeEnum {

        // 余额操作类型 增加、减少
        INCREASE(1, "增加"), DECREASE(0, "减少");

        private final Integer actionType;
        private final String name;

        MbBalanceActionTypeEnum(Integer actionType, String name) {
            this.actionType = actionType;
            this.name = name;
        }

    }

    /**
     * 物料出库单状态
     */
    @Getter
    public enum MaterialOutboundStatusEnum {
        
        // 出库单状态
        PENDING(0, "待审核"),
        APPROVED(1, "已审核待发货"),
        SHIPPED(2, "已发货"),
        COMPLETED(3, "已完成"),
        CANCELED(4, "已取消");
        
        private final Integer status;
        private final String name;
        
        MaterialOutboundStatusEnum(Integer status, String name) {
            this.status = status;
            this.name = name;
        }
    }
    
    /**
     * 物料使用记录类型
     */
    @Getter
    public enum MaterialUseTypeEnum {
        
        // 物料使用类型
        OUTBOUND(0, "出库使用");
        
        private final Integer type;
        private final String name;
        
        MaterialUseTypeEnum(Integer type, String name) {
            this.type = type;
            this.name = name;
        }
    }

}
