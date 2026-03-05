package com.hissp.distribution.module.mb.dal.dataobject.trade;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * mb订单表：记录代理用户的mb所有业务类型的订单信息 DO
 *
 * @author azhanga
 */
@TableName("mb_order")
@KeySequence("mb_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MbOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 业务类型：restock、materialConvert
     */
    private String bizType;
    /**
     * 代理用户ID
     */
    private Long agentUserId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 单价（单位：分）
     */
    private Integer unitPrice;
    /**
     * 总价（单位：分）
     */
    private Integer totalPrice;
    /**
     * 支付单ID
     */
    private String paymentId;
    /**
     * 订单状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败
     */
    private String status;
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

}
