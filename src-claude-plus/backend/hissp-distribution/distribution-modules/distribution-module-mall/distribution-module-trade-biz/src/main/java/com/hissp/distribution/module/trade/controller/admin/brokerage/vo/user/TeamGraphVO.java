package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销团队关系图谱 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 分销团队关系图谱 Response VO")
@Data
public class TeamGraphVO {

    @Schema(description = "团队根节点", requiredMode = Schema.RequiredMode.REQUIRED)
    private TeamGraphNodeVO rootNode;

    @Schema(description = "团队总人数", example = "100")
    private Integer totalCount;

    @Schema(description = "团队层级数", example = "5")
    private Integer levelCount;

    @Schema(description = "团队总佣金", example = "10000.00")
    private BigDecimal totalCommission;

    /**
     * 团队关系图谱节点 VO
     */
    @Schema(description = "团队关系图谱节点")
    @Data
    public static class TeamGraphNodeVO {

        @Schema(description = "节点唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "user_123")
        private String id;

        @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
        private String nickname;

        @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "代理等级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "总公司")
        private String levelName;

        @Schema(description = "累计佣金金额", example = "5000.00")
        private BigDecimal totalCommission;

        @Schema(description = "直接下级人数", example = "10")
        private Integer childCount;

        @Schema(description = "注册时间", example = "2023-01-01T00:00:00")
        private String createTime;

        @Schema(description = "子节点列表")
        private List<TeamGraphNodeVO> children;

    }

}
