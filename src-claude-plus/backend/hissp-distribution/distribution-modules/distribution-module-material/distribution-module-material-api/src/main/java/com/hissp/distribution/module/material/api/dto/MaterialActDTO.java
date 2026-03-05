package com.hissp.distribution.module.material.api.dto;

import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 物料动作 DTO
 * 代表一次物料的入账或回退
 */
@Data
@Builder
public class MaterialActDTO implements Serializable {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 物料ID
     */
    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    /**
     * 数量（正数）
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须为正数")
    private Integer quantity;

    /**
     * 方向（IN:入账, OUT:回退）
     */
    @NotNull(message = "方向不能为空")
    private MaterialActDirectionEnum direction;

    /**
     * 业务唯一标识（幂等键）
     * 建议使用：订单项ID
     */
    @NotBlank(message = "业务唯一标识不能为空")
    private String bizKey;

    /**
     * 业务类型（来源）
     * 例如：ORDER_GRANT, REFUND_REVOKE
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 备注/原因
     */
    private String reason;

    /**
     * 是否允许出现负库存（特殊场景，如总部资金池）
     */
    private boolean allowNegative;

}
