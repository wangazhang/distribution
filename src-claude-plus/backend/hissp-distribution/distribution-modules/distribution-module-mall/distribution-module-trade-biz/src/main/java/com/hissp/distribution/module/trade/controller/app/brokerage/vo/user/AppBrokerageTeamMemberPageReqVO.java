package com.hissp.distribution.module.trade.controller.app.brokerage.vo.user;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 团队成员列表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppBrokerageTeamMemberPageReqVO extends PageParam {

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户等级", example = "0")
    private Integer level;

    @Schema(description = "类型：community-社群成员；direct-直接代理；indirect-间接代理", example = "community")
    private String type;

} 