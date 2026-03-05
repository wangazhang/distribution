package com.hissp.distribution.module.material.dal.dataobject;

import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 物料入库 DO
 */
@TableName("material_inbound")
@KeySequence("material_inbound_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialInboundDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    
    /**
     * 入库单号
     */
    private String inboundNo;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 物料ID
     */
    private Long materialId;
    
    /**
     * 物料名称
     */
    private String materialName;
    
    /**
     * 入库数量
     */
    private Integer quantity;
    
    /**
     * 单价（分）
     */
    private Integer unitPrice;
    
    /**
     * 总金额（分）
     */
    private Integer totalAmount;
    
    /**
     * 供应商
     */
    private String supplier;
    
    /**
     * 入库原因
     */
    private String reason;
    
    /**
     * 状态
     * 0-待审核 1-已审核 2-已拒绝 3-已完成 4-已取消
     */
    private Integer status;
    
    /**
     * 审核人ID
     */
    private Long approveUserId;
    
    /**
     * 审核人姓名
     */
    private String approveUserName;
    
    /**
     * 审核意见
     */
    private String approveReason;
    
    /**
     * 审核时间
     */
    private java.time.LocalDateTime approveTime;
    
    /**
     * 完成时间
     */
    private java.time.LocalDateTime completeTime;

}