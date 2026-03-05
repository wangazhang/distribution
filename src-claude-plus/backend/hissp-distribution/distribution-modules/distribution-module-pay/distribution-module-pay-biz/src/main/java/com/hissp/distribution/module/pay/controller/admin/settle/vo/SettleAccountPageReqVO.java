package com.hissp.distribution.module.pay.controller.admin.settle.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SettleAccountPageReqVO extends PageParam {

    @Schema(description = "姓名")
    private String signedName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态")
    private Integer status;
}
