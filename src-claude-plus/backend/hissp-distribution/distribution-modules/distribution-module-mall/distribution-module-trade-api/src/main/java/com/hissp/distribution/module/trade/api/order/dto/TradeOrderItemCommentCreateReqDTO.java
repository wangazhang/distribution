package com.hissp.distribution.module.trade.api.order.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 交易订单项评论创建 Request DTO
 * 用于API层的订单项评论创建操作
 * 
 * @author system
 */
@Data
public class TradeOrderItemCommentCreateReqDTO {

    /**
     * 是否匿名评论
     */
    @NotNull(message = "是否匿名不能为空")
    private Boolean anonymous;

    /**
     * 交易订单项编号
     */
    @NotNull(message = "交易订单项编号不能为空")
    private Long orderItemId;

    /**
     * 描述星级 1-5 分
     */
    @NotNull(message = "描述星级不能为空")
    private Integer descriptionScores;

    /**
     * 服务星级 1-5 分
     */
    @NotNull(message = "服务星级不能为空")
    private Integer benefitScores;

    /**
     * 评论内容
     */
    @NotNull(message = "评论内容不能为空")
    private String content;

    /**
     * 评论图片地址数组，最多上传 9 张
     */
    @Size(max = 9, message = "评论图片地址数组长度不能超过 9 张")
    private List<String> picUrls;

    /**
     * 评论人ID
     * 用于记录是谁创建的评论
     */
    private Long userId;
}
