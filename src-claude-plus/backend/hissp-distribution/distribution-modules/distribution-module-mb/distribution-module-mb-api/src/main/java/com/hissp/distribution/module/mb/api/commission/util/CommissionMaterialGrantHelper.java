package com.hissp.distribution.module.mb.api.commission.util;

import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionMaterialGrantDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 帮助类：将策略返回的物料奖励 DTO 转换为物料流水动作，方便发放与回退共用一套逻辑。
 */
public final class CommissionMaterialGrantHelper {

    private static final String DEFAULT_BIZ_TYPE = "MB_COMMISSION_MATERIAL_GRANT";

    private CommissionMaterialGrantHelper() {
    }

    public static List<MaterialActDTO> buildActs(List<CommissionMaterialGrantDTO> grants,
        MaterialActDirectionEnum direction, String bizKeySuffix, String defaultReason) {
        if (grants == null || grants.isEmpty()) {
            return Collections.emptyList();
        }
        List<MaterialActDTO> acts = new ArrayList<>(grants.size());
        for (CommissionMaterialGrantDTO grant : grants) {
            if (grant == null || grant.getUserId() == null || grant.getMaterialId() == null
                || grant.getQuantity() == null || grant.getQuantity() <= 0) {
                continue;
            }
            acts.add(MaterialActDTO.builder()
                .userId(grant.getUserId())
                .materialId(grant.getMaterialId())
                .quantity(grant.getQuantity())
                .direction(direction)
                .bizKey(handleBizKey(grant, bizKeySuffix))
                .bizType(hasText(grant.getBizType()) ? grant.getBizType() : DEFAULT_BIZ_TYPE)
                .reason(hasText(grant.getReason()) ? grant.getReason() : defaultReason)
                .build());
        }
        return acts;
    }

    private static String handleBizKey(CommissionMaterialGrantDTO grant, String bizKeySuffix) {
        String baseKey = hasText(grant.getBizKey())
            ? grant.getBizKey()
            : String.format("MB-COMMISSION:%s:%s", grant.getRuleId(), grant.getMaterialId());
        if (!hasText(bizKeySuffix)) {
            return baseKey;
        }
        return baseKey + ":" + bizKeySuffix;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
