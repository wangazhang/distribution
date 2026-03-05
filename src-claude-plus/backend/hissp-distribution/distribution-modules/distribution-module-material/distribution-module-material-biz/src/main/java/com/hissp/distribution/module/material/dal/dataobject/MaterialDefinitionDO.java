package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 物料定义 DO
 */
@TableName("material_definition")
@KeySequence("material_definition_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDefinitionDO extends BaseDO {

    /**
     * 物料ID
     */
    @TableId
    private Long id;
    
    /**
     * 物料名称
     * 映射模式：不存储，从SPU实时获取
     * 快照模式：从SPU复制存储
     */
    private String name;

    /**
     * 物料编码
     */
    private String code;

    /**
     * 关联SPU ID
     * 成品物料关联商品SPU
     */
    private Long spuId;

    /**
     * 物料图片
     * 映射模式：不存储，从SPU实时获取
     * 快照模式：从SPU复制存储
     */
    private String image;

    /**
     * 物料描述
     * 映射模式：不存储，从SPU实时获取
     * 快照模式：从SPU复制存储
     */
    private String description;
    
    /**
     * 基础单位
     */
    private String baseUnit;
    
    /**
     * 物料类型（1:半成品 2:成品）
     *
     * 枚举 {@link com.hissp.distribution.module.material.enums.MaterialTypeEnum}
     */
    private Integer type;

    /**
     * SPU关联模式（1:映射 2:快照）
     * 仅当type=2(成品)时有效
     *
     * 枚举 {@link com.hissp.distribution.module.material.enums.MaterialLinkModeEnum}
     */
    private Integer linkMode;

    /**
     * 是否自动同步SPU信息
     * 映射模式下生效，快照模式忽略
     */
    private Boolean autoSync;

    /**
     * SPU信息快照（JSON格式）
     * 快照模式下存储SPU的名称、图片等信息
     */
    private String spuSnapshot;

    /**
     * 转化状态（0:不可转化 1:可转化 2:已转化）
     * 仅当type=1(半成品)时有效
     *
     * 枚举 {@link com.hissp.distribution.module.material.enums.MaterialConvertStatusEnum}
     */
    private Integer convertStatus;

    /**
     * 转化后的SPU ID
     * 半成品转化为成品后关联的SPU ID
     */
    private Long convertedSpuId;

    /**
     * 转化单价（单位：分）
     */
    private Integer convertPrice;

    /**
     * 是否支持出库
     */
    private Boolean supportOutbound;

    /**
     * 是否支持转化
     */
    private Boolean supportConvert;

    /**
     * 扩展属性
     */
    private String attrs; // TODO: typeHandler for JSON

    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;

}
