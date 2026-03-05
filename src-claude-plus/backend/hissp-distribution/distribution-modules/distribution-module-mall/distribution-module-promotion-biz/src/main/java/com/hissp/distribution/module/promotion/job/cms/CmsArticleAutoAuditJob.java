package com.hissp.distribution.module.promotion.job.cms;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleMapper;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.enums.SectionTypeEnum;
import com.hissp.distribution.module.promotion.service.cms.CmsSectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CMS 动态文章自动审核任务
 *
 * @author
 */
@Slf4j
@Component
public class CmsArticleAutoAuditJob {

    private static final String CONFIG_AUTO_AUDIT_ENABLED = "autoAuditEnabled";
    private static final String CONFIG_AUTO_AUDIT_DELAY = "autoAuditDelayMinutes";

    @Resource
    private CmsSectionService cmsSectionService;
    @Resource
    private CmsArticleMapper cmsArticleMapper;

    /**
     * 每分钟检查一次，自动审核配置的动态文章
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void autoApprovePendingArticles() {
        List<CmsSectionDO> sections = cmsSectionService.getEnabledSectionList();
        if (CollUtil.isEmpty(sections)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (CmsSectionDO section : sections) {
            if (section.getConfig() == null) {
                continue;
            }
            String sectionType = section.getType();
            if (sectionType == null || !SectionTypeEnum.DYNAMIC.getType().equalsIgnoreCase(sectionType)) {
                continue;
            }
            if (!cmsSectionService.isAuditRequired(section.getId())) {
                continue;
            }

            Integer delayMinutes = resolveAutoAuditDelay(section.getConfig());
            if (delayMinutes == null || delayMinutes <= 0) {
                continue;
            }

            LocalDateTime deadline = now.minusMinutes(delayMinutes);
            List<CmsArticleDO> pendingList = cmsArticleMapper.selectPendingListBefore(section.getId(), deadline);
            if (CollUtil.isEmpty(pendingList)) {
                continue;
            }

            List<Long> ids = pendingList.stream().map(CmsArticleDO::getId).collect(Collectors.toList());
            int updated = cmsArticleMapper.updateAutoApprove(ids, now, "系统自动审核通过");

            if (updated > 0) {
                log.info("[autoApprovePendingArticles][sectionId={}, delayMinutes={}, updated={}]", section.getId(),
                        delayMinutes, updated);
            }
        }
    }

    private Integer resolveAutoAuditDelay(Map<String, Object> config) {
        if (config == null) {
            return null;
        }

        boolean enabled = parseBoolean(config.get(CONFIG_AUTO_AUDIT_ENABLED));
        Integer delayMinutes = parseInteger(config.get(CONFIG_AUTO_AUDIT_DELAY));

        // 如果没有显式开启，但配置了延迟时间，也视为开启
        if (!enabled && (delayMinutes == null || delayMinutes <= 0)) {
            return 0;
        }

        return delayMinutes != null ? delayMinutes : 0;
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String str = ((String) value).trim().toLowerCase();
            return "true".equals(str) || "1".equals(str) || "yes".equals(str);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return false;
    }

    private Integer parseInteger(Object value) {
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
