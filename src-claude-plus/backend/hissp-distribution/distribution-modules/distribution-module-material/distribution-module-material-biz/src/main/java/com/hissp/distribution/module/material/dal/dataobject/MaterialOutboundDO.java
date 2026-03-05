package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 物料出库主 DO
 */
@TableName("material_outbound")
@KeySequence("material_outbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOutboundDO extends BaseDO {

    /**
     * 出库单ID
     */
    @TableId
    private Long id;
    
    /**
     * 出库单号
     */
    private String outboundNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 收货地址ID
     */
    private Long addressId;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人手机号
     */
    private String receiverMobile;
    
    /**
     * 收货人省份
     */
    private String receiverProvince;
    
    /**
     * 收货人城市
     */
    private String receiverCity;
    
    /**
     * 收货人区县
     */
    private String receiverDistrict;
    
    /**
     * 收货人详细地址
     */
    private String receiverDetailAddress;
    
    /**
     * 状态：0-待审核，1-已审核待发货，2-已发货，3-已完成，4-已取消
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 物流单号
     */
    private String logisticsCode;
    
    /**
     * 物流公司
     */
    private String logisticsCompany;
    
    /**
     * 审核时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 审核人ID
     */
    private Long approveUserId;
    
    /**
     * 发货时间
     */
    private LocalDateTime shipTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    
    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;
    
    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 用户昵称(非表字段,关联查询获取)
     */
    @TableField(exist = false)
    private String userNickname;

}