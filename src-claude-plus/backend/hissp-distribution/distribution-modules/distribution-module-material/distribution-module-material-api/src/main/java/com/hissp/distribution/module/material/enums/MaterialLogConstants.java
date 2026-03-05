package com.hissp.distribution.module.material.enums;

/**
 * 物料操作日志常量
 */
public interface MaterialLogConstants {

    // ========== 操作模块类型 ==========
    String MATERIAL_DEFINITION_TYPE = "MATERIAL_DEFINITION";
    String MATERIAL_CONVERT_TYPE = "MATERIAL_CONVERT";

    // ========== 物料定义操作子类型 ==========
    String CREATE_SEMI_FINISHED = "CREATE_SEMI_FINISHED";
    String CREATE_FINISHED_MAPPING = "CREATE_FINISHED_MAPPING";
    String CREATE_FINISHED_SNAPSHOT = "CREATE_FINISHED_SNAPSHOT";
    String UPDATE_DEFINITION = "UPDATE_DEFINITION";
    String SYNC_SPU_INFO = "SYNC_SPU_INFO";
    String DELETE_DEFINITION = "DELETE_DEFINITION";

    // ========== 物料转化操作子类型 ==========
    String SEMI_TO_FINISHED = "SEMI_TO_FINISHED";
    String ADMIN_CONVERT = "ADMIN_CONVERT";
    String MOBILE_CONVERT = "MOBILE_CONVERT";
    String CONVERT_ROLLBACK = "CONVERT_ROLLBACK";

    // ========== 操作动作模板 ==========
    interface Actions {
        String CREATE_SEMI_FINISHED = "创建半成品物料：{{#materialName}}，编码：{{#materialCode}}";
        String CREATE_FINISHED_MAPPING = "创建成品物料：{{#materialName}}，映射SPU：{{#spuName}}（ID：{{#spuId}}）";
        String CREATE_FINISHED_SNAPSHOT = "创建成品物料：{{#materialName}}，快照SPU：{{#spuName}}（ID：{{#spuId}}）";
        String UPDATE_DEFINITION = "修改物料定义：{{#materialName}}";
        String SYNC_SPU_INFO = "同步SPU信息：物料{{#materialName}}从SPU{{#spuName}}更新信息";
        String DELETE_DEFINITION = "删除物料定义：{{#materialName}}（ID：{{#materialId}}）";

        String SEMI_TO_FINISHED_MOBILE = "半成品转化：{{#materialName}}转化为成品SPU{{#targetSpuName}}，支付金额：{{#amount}}元";
        String SEMI_TO_FINISHED_ADMIN = "管理员转化：{{#materialName}}转化为成品SPU{{#targetSpuName}}，原因：{{#reason}}";
        String CONVERT_ROLLBACK = "转化回退：物料{{#materialName}}从成品状态回退为半成品";
    }
}