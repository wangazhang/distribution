package com.hissp.distribution.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "用户 App - 团队成员 Response VO")
@Data
public class AppBrokerageTeamMemberRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小王")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.example.com/xxx.jpg")
    private String avatar;
    
    @Schema(description = "手机号", example = "15601691300")
    private String phone;
    
    @Schema(description = "邀请人", example = "张三")
    private String inviter;

    @Schema(description = "会员等级编号", example = "1024")
    private Long levelId;

    @Schema(description = "会员等级名称", example = "金牌代理")
    private String levelName;

    @Schema(description = "消费金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer consumption;

    @Schema(description = "佣金收益，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer revenue;

    @Schema(description = "社群人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer communityCount;

    @Schema(description = "注册时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime registerTime;
}
