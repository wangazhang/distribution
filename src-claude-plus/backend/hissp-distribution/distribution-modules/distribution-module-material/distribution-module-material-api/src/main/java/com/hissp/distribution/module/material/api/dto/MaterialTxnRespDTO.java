package com.hissp.distribution.module.material.api.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialTxnRespDTO {

    private Long id;

    private Long userId;

    private String nickname;

    private Long materialId;

    private String materialName;

    private Integer direction;

    private Integer quantity;

    private Integer balanceAfter;

    private String bizKey;

    private String bizType;

    private String reason;

    private LocalDateTime createTime;

}

