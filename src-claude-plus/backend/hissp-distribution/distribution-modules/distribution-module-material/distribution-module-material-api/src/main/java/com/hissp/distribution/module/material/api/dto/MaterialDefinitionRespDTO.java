package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物料定义响应 DTO
 */
@Data
public class MaterialDefinitionRespDTO {

    /**
     * 物料ID
     */
    private Long id;

    /**
     * 物料名称
     */
    private String name;

    /**
     * 物料编码
     */
    private String code;

    /**
     * 关联SPU ID
     */
    private Long spuId;

    /**
     * 物料图片
     */
    private String image;

    /**
     * 物料描述
     */
    private String description;

    /**
     * 物料类型：1原料 2产品 3工具
     */
    private Integer type;

    /**
     * 基础单位
     */
    private String baseUnit;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    /**
     * 是否支持出库
     */
    private Boolean supportOutbound;

    /**
     * 是否支持转化
     */
    private Boolean supportConvert;

    /**
     * 转化状态：0-不可转化 1-可转化 2-已转化
     */
    private Integer convertStatus;

    /**
     * 转化后的SPU ID
     */
    private Long convertedSpuId;

    /**
     * 转化单价（单位：分）
     */
    private Integer convertPrice;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
