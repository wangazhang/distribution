package com.hissp.distribution.module.promotion.service.cms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.hissp.distribution.framework.common.enums.CommonStatusEnum;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionSaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsSectionConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsSectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_SECTION_NOT_EXISTS;

/**
 * CMS板块 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CmsSectionServiceImpl implements CmsSectionService {

    private static final List<String> SUPPORTED_CARD_BUTTONS =
            Arrays.asList("like", "collect", "download", "enroll", "share");

    @Resource
    private CmsSectionMapper cmsSectionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSection(CmsSectionSaveReqVO createReqVO) {
        // 插入
        CmsSectionDO section = CmsSectionConvert.INSTANCE.convert(createReqVO);
        mergeSectionConfig(section, null);
        cmsSectionMapper.insert(section);
        return section.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSection(CmsSectionSaveReqVO updateReqVO) {
        // 校验存在
        CmsSectionDO existing = cmsSectionMapper.selectById(updateReqVO.getId());
        if (existing == null) {
            throw exception(CMS_SECTION_NOT_EXISTS);
        }
        // 更新
        CmsSectionDO updateObj = CmsSectionConvert.INSTANCE.convert(updateReqVO);
        mergeSectionConfig(updateObj, existing);
        cmsSectionMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSection(Long id) {
        // 校验存在
        validateSectionExists(id);
        // 删除
        cmsSectionMapper.deleteById(id);
    }

    @Override
    public CmsSectionDO getSection(Long id) {
        return cmsSectionMapper.selectById(id);
    }

    @Override
    public PageResult<CmsSectionDO> getSectionPage(CmsSectionPageReqVO pageReqVO) {
        return cmsSectionMapper.selectPage(pageReqVO.getPageNo(), pageReqVO.getPageSize(),
                pageReqVO.getName(), pageReqVO.getType(), pageReqVO.getStatus());
    }

    @Override
    public List<CmsSectionDO> getSectionList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return cmsSectionMapper.selectBatchIds(ids);
    }

    @Override
    public List<CmsSectionDO> getSectionList() {
        return cmsSectionMapper.selectList();
    }

    @Override
    public void validateSectionExists(Long id) {
        if (cmsSectionMapper.selectById(id) == null) {
            throw exception(CMS_SECTION_NOT_EXISTS);
        }
    }

    @Override
    public List<CmsSectionDO> getEnabledSectionList() {
        return cmsSectionMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public boolean isAuditRequired(Long sectionId) {
        if (sectionId == null) {
            return true; // 默认需要审核
        }

        CmsSectionDO section = cmsSectionMapper.selectById(sectionId);
        if (section == null || section.getConfig() == null) {
            return true; // 默认需要审核
        }

        // 从config中读取审核配置，默认为需要审核
        Object requireAudit = section.getConfig().get("requireAudit");
        if (requireAudit instanceof Boolean) {
            return (Boolean) requireAudit;
        } else if (requireAudit instanceof String) {
            return "true".equals(requireAudit) || "1".equals(requireAudit);
        } else if (requireAudit instanceof Number) {
            return ((Number) requireAudit).intValue() != 0;
        }

        return true; // 默认需要审核
    }

    private void mergeSectionConfig(CmsSectionDO target, CmsSectionDO existing) {
        Map<String, Object> config = existing != null && existing.getConfig() != null
                ? new HashMap<>(existing.getConfig())
                : new HashMap<>();
        if (target.getConfig() != null) {
            config.putAll(target.getConfig());
        }
        ensureConfigDefaults(config);
        target.setConfig(config);
    }

    private void ensureConfigDefaults(Map<String, Object> config) {
        config.putIfAbsent("requireAudit", Boolean.TRUE);
        config.putIfAbsent("autoAuditEnabled", Boolean.FALSE);
        config.putIfAbsent("autoAuditDelayMinutes", 0);
        normalizeCardButtons(config);
    }

    @SuppressWarnings("unchecked")
    private void normalizeCardButtons(Map<String, Object> config) {
        Map<String, Object> cardButtons = new LinkedHashMap<>();

        Object existing = config.get("cardButtons");
        if (existing instanceof Map<?, ?> existingMap) {
            existingMap.forEach((key, value) -> {
                String buttonKey = String.valueOf(key);
                if (!SUPPORTED_CARD_BUTTONS.contains(buttonKey)) {
                    return;
                }
                Map<String, Object> normalized = normalizeSingleButton(value);
                if (normalized != null) {
                    cardButtons.put(buttonKey, normalized);
                }
            });
        }

        Object legacy = config.get("buttons");
        if (legacy instanceof Map<?, ?> legacyMap) {
            legacyMap.forEach((key, value) -> {
                String buttonKey = String.valueOf(key);
                if (!SUPPORTED_CARD_BUTTONS.contains(buttonKey)) {
                    return;
                }
                Map<String, Object> normalized = normalizeSingleButton(value);
                if (normalized != null) {
                    cardButtons.put(buttonKey, normalized);
                }
            });
            config.remove("buttons");
        }

        for (String key : SUPPORTED_CARD_BUTTONS) {
            cardButtons.computeIfAbsent(key, ignored -> createButtonConfig(Boolean.TRUE));
        }

        config.put("cardButtons", cardButtons);
    }

    private Map<String, Object> normalizeSingleButton(Object value) {
        boolean show = parseBoolean(value);
        return createButtonConfig(show);
    }

    private Map<String, Object> createButtonConfig(Boolean show) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("show", show != null ? show : Boolean.TRUE);
        return map;
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value instanceof String str) {
            String lower = str.trim().toLowerCase();
            return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower);
        }
        if (value instanceof Map<?, ?> map) {
            Object show = map.get("show");
            return parseBoolean(show);
        }
        return Boolean.TRUE;
    }

}
