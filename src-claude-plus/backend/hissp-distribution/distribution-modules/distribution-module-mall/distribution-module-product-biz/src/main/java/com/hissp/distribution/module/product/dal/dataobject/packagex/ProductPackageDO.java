package com.hissp.distribution.module.product.dal.dataobject.packagex;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/** 套包商品 DO */
@TableName("product_package")
@KeySequence("product_package_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackageDO extends BaseDO {

    @TableId
    private Long id;

    private String name;
    private Long spuId;
    /** 0:草稿 1:启用 2:禁用 */
    private Integer status;
    private String remark;
}

