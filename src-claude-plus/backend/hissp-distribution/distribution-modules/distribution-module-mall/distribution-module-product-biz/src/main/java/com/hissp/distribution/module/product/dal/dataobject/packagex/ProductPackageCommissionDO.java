package com.hissp.distribution.module.product.dal.dataobject.packagex;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/** 套包分佣规则 DO */
@TableName("product_package_commission")
@KeySequence("product_package_commission_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackageCommissionDO extends BaseDO {

    @TableId
    private Long id;

    private Long packageId;
    /** 1: 上级, 2: 上上级 */
    private Integer level;
    /** 1: 固定金额(分), 2: 固定比例(%) */
    private Integer commissionType;
    /** 金额(分)或百分比 */
    private java.math.BigDecimal commissionValue;
    /** 1:订单项实付, 2:自定义基准 */
    private Integer baseType;
    /** 自定义基准(分) */
    private Integer baseAmount;
}

