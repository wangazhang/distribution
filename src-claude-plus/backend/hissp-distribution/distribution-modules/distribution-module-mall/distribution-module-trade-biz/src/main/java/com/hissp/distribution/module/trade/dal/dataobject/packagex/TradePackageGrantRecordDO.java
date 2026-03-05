package com.hissp.distribution.module.trade.dal.dataobject.packagex;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.Map;

@TableName(value = "trade_package_grant_record", autoResultMap = true)
@KeySequence("trade_package_grant_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradePackageGrantRecordDO extends BaseDO {

    @TableId
    private Long id;

    private Long userId;
    private Long orderId;
    private Long orderItemId;
    private Long packageId;
    // 1已发放 2已撤销
    private Integer grantStatus;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> details;
}

