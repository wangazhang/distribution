package com.hissp.distribution.module.material.api.dto;

import com.hissp.distribution.framework.common.pojo.PageParam;

import lombok.Data;


@Data
public class MaterialBalancePageReqDTO extends PageParam {


    private Long userId;


    private Long materialId;

}

