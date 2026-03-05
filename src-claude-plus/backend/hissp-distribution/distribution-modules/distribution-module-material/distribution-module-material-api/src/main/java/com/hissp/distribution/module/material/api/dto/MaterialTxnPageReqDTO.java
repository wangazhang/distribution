package com.hissp.distribution.module.material.api.dto;

import com.hissp.distribution.framework.common.pojo.PageParam;

import lombok.Data;


@Data
public class MaterialTxnPageReqDTO extends PageParam {


    private Long userId;


    private Long materialId;


    private String bizKey;

}

