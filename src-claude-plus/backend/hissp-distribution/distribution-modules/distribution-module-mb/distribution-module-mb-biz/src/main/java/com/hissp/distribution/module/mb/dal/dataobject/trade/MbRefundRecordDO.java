package com.hissp.distribution.module.mb.dal.dataobject.trade;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MB业务退款记录 DO
 *
 * @author system
 */
@TableName("mb_refund_record")
@KeySequence("mb_refund_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MbRefundRecordDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 商户订单号
     */
    private String merchantOrderId;

    /**
     * 支付退款ID
     */
    private Long payRefundId;

    /**
     * 退款类型
     * @see com.hissp.distribution.module.mb.enums.MbRefundTypeEnum
     */
    private String refundType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金额（分）
     */
    private Integer refundAmount;

    /**
     * 处理状态：0-待处理，1-处理中，2-处理成功，3-处理失败
     */
    private Integer status;

    /**
     * 处理开始时间
     */
    private LocalDateTime processStartTime;

    /**
     * 处理完成时间
     */
    private LocalDateTime processEndTime;

    /**
     * 逆操作详情（JSON格式）
     */
    private String reverseOperationDetails;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 备注
     */
    private String remark;
}
