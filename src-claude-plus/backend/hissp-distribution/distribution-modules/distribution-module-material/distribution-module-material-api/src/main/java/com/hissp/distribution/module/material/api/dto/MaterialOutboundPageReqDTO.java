package com.hissp.distribution.module.material.api.dto;

import com.hissp.distribution.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 物料出库分页查询请求 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialOutboundPageReqDTO extends PageParam {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 出库单号
     */
    private String outboundNo;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverMobile;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}