package com.hissp.distribution.module.product.controller.admin.packagex.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import lombok.Data;

import java.util.Date;

@Data
public class ProductPackagePageReqVO extends PageParam {
    private String name;
    private Long spuId;
    // 0: 草稿 1: 启用 2: 禁用
    private Integer status;
    // 创建时间范围（开始、结束）
    private Date[] createTime;
}

