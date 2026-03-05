package com.hissp.distribution.module.promotion.convert.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionSaveReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsSectionRespVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CMS板块 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsSectionConvert {

    CmsSectionConvert INSTANCE = Mappers.getMapper(CmsSectionConvert.class);

    CmsSectionDO convert(CmsSectionSaveReqVO bean);

    CmsSectionRespVO convert(CmsSectionDO bean);

    List<CmsSectionRespVO> convertList(List<CmsSectionDO> list);

    PageResult<CmsSectionRespVO> convertPage(PageResult<CmsSectionDO> page);

    AppCmsSectionRespVO convertApp(CmsSectionDO bean);

    List<AppCmsSectionRespVO> convertAppList(List<CmsSectionDO> list);

    @AfterMapping
    default void fillConfig(CmsSectionSaveReqVO source, @MappingTarget CmsSectionDO target) {
        if (source == null || target == null) {
            return;
        }
        Map<String, Object> config = target.getConfig() != null ? new HashMap<>(target.getConfig()) : new HashMap<>();
        putIfNotNull(config, "requireAudit", source.getRequireAudit());
        putIfNotNull(config, "autoAuditEnabled", source.getAutoAuditEnabled());
        putIfNotNull(config, "autoAuditDelayMinutes", source.getAutoAuditDelayMinutes());
        target.setConfig(config);
    }

    @AfterMapping
    default void fillExtraFields(CmsSectionDO source, @MappingTarget CmsSectionRespVO target) {
        if (source == null || target == null) {
            return;
        }
        Map<String, Object> config = source.getConfig();
        if (config == null) {
            return;
        }
        Boolean requireAudit = parseBoolean(config.get("requireAudit"));
        Boolean autoAuditEnabled = parseBoolean(config.get("autoAuditEnabled"));
        Integer autoAuditDelayMinutes = parseInteger(config.get("autoAuditDelayMinutes"));

        target.setRequireAudit(requireAudit != null ? requireAudit : Boolean.TRUE);
        target.setAutoAuditEnabled(autoAuditEnabled != null ? autoAuditEnabled : Boolean.FALSE);
        target.setAutoAuditDelayMinutes(autoAuditDelayMinutes != null ? autoAuditDelayMinutes : 0);
    }

    private static void putIfNotNull(Map<String, Object> config, String key, Object value) {
        if (value != null) {
            config.put(key, value);
        }
    }

    private static Boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            String str = ((String) value).trim().toLowerCase();
            if ("true".equals(str) || "1".equals(str) || "yes".equals(str)) {
                return true;
            }
            if ("false".equals(str) || "0".equals(str) || "no".equals(str)) {
                return false;
            }
        }
        return null;
    }

    private static Integer parseInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
