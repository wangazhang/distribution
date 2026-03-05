package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsUserActionRespVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsUserActionDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleMapper;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsUserActionMapper;
import com.hissp.distribution.module.system.api.user.AdminUserApi;
import com.hissp.distribution.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CMS用户行为 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CmsUserActionServiceImpl implements CmsUserActionService {

    private static final String ACTION_TYPE_LIKE = "like";
    private static final String ACTION_TYPE_COLLECT = "collect";

    @Resource
    private CmsUserActionMapper cmsUserActionMapper;

    @Resource
    private CmsArticleMapper cmsArticleMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long userId, Long articleId) {
        // 检查是否已经点赞
        if (hasLiked(userId, articleId)) {
            return;
        }

        // 创建点赞记录
        CmsUserActionDO userAction = CmsUserActionDO.builder()
                .userId(userId)
                .articleId(articleId)
                .actionType(ACTION_TYPE_LIKE)
                .build();
        cmsUserActionMapper.insert(userAction);

        // 增加文章点赞数
        CmsArticleDO article = cmsArticleMapper.selectById(articleId);
        if (article != null) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(articleId)
                    .likeCount(article.getLikeCount() + 1)
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long userId, Long articleId) {
        // 检查是否已经点赞
        if (!hasLiked(userId, articleId)) {
            return;
        }

        // 删除点赞记录（使用物理删除避免唯一键冲突）
        cmsUserActionMapper.deleteByUserIdAndArticleIdAndActionTypePhysical(userId, articleId, "LIKE");

        // 减少文章点赞数
        CmsArticleDO article = cmsArticleMapper.selectById(articleId);
        if (article != null) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(articleId)
                    .likeCount(Math.max(0, article.getLikeCount() - 1))
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long userId, Long articleId) {
        // 检查是否已经收藏
        if (hasCollected(userId, articleId)) {
            return;
        }

        // 创建收藏记录
        CmsUserActionDO userAction = CmsUserActionDO.builder()
                .userId(userId)
                .articleId(articleId)
                .actionType(ACTION_TYPE_COLLECT)
                .build();
        cmsUserActionMapper.insert(userAction);

        // 增加文章收藏数
        CmsArticleDO article = cmsArticleMapper.selectById(articleId);
        if (article != null) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(articleId)
                    .collectCount(article.getCollectCount() + 1)
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long userId, Long articleId) {
        // 检查是否已经收藏
        if (!hasCollected(userId, articleId)) {
            return;
        }

        // 删除收藏记录（使用物理删除避免唯一键冲突）
        cmsUserActionMapper.deleteByUserIdAndArticleIdAndActionTypePhysical(userId, articleId, "COLLECT");

        // 减少文章收藏数
        CmsArticleDO article = cmsArticleMapper.selectById(articleId);
        if (article != null) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(articleId)
                    .collectCount(Math.max(0, article.getCollectCount() - 1))
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    public boolean hasLiked(Long userId, Long articleId) {
        CmsUserActionDO userAction = cmsUserActionMapper.selectByUserIdAndArticleIdAndActionType(userId, articleId, ACTION_TYPE_LIKE);
        return userAction != null;
    }

    @Override
    public boolean hasCollected(Long userId, Long articleId) {
        CmsUserActionDO userAction = cmsUserActionMapper.selectByUserIdAndArticleIdAndActionType(userId, articleId, ACTION_TYPE_COLLECT);
        return userAction != null;
    }

    @Override
    public PageResult<CmsUserActionDO> getLikeRecordPage(Integer pageNo, Integer pageSize, Long userId) {
        return cmsUserActionMapper.selectPageByUserId(new PageParam(pageNo, pageSize), userId, ACTION_TYPE_LIKE);
    }

    @Override
    public PageResult<CmsUserActionDO> getCollectRecordPage(Integer pageNo, Integer pageSize, Long userId) {
        return cmsUserActionMapper.selectPageByUserId(new PageParam(pageNo, pageSize), userId, ACTION_TYPE_COLLECT);
    }

    @Override
    public PageResult<CmsUserActionDO> getUserActionPage(Long userId, String actionType, PageParam pageParam) {
        return cmsUserActionMapper.selectPage(pageParam, new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getUserId, userId)
                .eq(CmsUserActionDO::getActionType, actionType)
                .orderByDesc(CmsUserActionDO::getCreateTime));
    }

    @Override
    public PageResult<AppCmsUserActionRespVO> getUserActionDetailPage(Long userId, String actionType, PageParam pageParam) {
        PageResult<CmsUserActionDO> pageResult = getUserActionPage(userId, actionType, pageParam);
        return buildActionDetailPage(pageResult);
    }

    @Override
    public PageResult<AppCmsUserActionRespVO> getReceivedActionDetailPage(Long userId, String actionType, PageParam pageParam) {
        List<CmsArticleDO> myArticles = cmsArticleMapper.selectList(new LambdaQueryWrapperX<CmsArticleDO>()
                .eq(CmsArticleDO::getAuthorType, "user")
                .eq(CmsArticleDO::getAuthorId, userId)
                .select(CmsArticleDO::getId));
        if (CollectionUtils.isEmpty(myArticles)) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        List<Long> articleIds = myArticles.stream()
                .map(CmsArticleDO::getId)
                .collect(Collectors.toList());

        PageResult<CmsUserActionDO> pageResult = cmsUserActionMapper.selectPage(pageParam, new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getActionType, actionType)
                .in(CmsUserActionDO::getArticleId, articleIds)
                .orderByDesc(CmsUserActionDO::getCreateTime));
        return buildActionDetailPage(pageResult);
    }

    @Override
    public boolean hasUserAction(Long userId, Long articleId, String actionType) {
        CmsUserActionDO userAction = cmsUserActionMapper.selectByUserIdAndArticleIdAndActionType(userId, articleId, actionType);
        return userAction != null;
    }

    private PageResult<AppCmsUserActionRespVO> buildActionDetailPage(PageResult<CmsUserActionDO> pageResult) {
        if (pageResult == null || CollectionUtils.isEmpty(pageResult.getList())) {
            long total = pageResult != null ? pageResult.getTotal() : 0L;
            return new PageResult<>(Collections.emptyList(), total);
        }

        List<CmsUserActionDO> records = pageResult.getList();
        List<Long> articleIds = records.stream()
                .map(CmsUserActionDO::getArticleId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, CmsArticleDO> articleMap = CollectionUtils.isEmpty(articleIds)
                ? Collections.emptyMap()
                : cmsArticleMapper.selectBatchIds(articleIds)
                .stream()
                .collect(Collectors.toMap(CmsArticleDO::getId, article -> article, (a, b) -> a));

        Set<Long> memberUserIds = records.stream()
                .map(CmsUserActionDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        articleMap.values().stream()
                .filter(article -> article != null && "user".equals(article.getAuthorType()) && article.getAuthorId() != null)
                .map(CmsArticleDO::getAuthorId)
                .forEach(memberUserIds::add);
        Map<Long, MemberUserRespDTO> memberMap = CollectionUtils.isEmpty(memberUserIds) || memberUserApi == null
                ? Collections.emptyMap()
                : memberUserApi.getUserMap(memberUserIds);
        if (memberMap == null) {
            memberMap = Collections.emptyMap();
        }
        final Map<Long, MemberUserRespDTO> memberMapFinal = memberMap;

        Set<Long> adminUserIds = articleMap.values().stream()
                .filter(article -> article != null && "admin".equals(article.getAuthorType()) && article.getAuthorId() != null)
                .map(CmsArticleDO::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> adminMap = CollectionUtils.isEmpty(adminUserIds) || adminUserApi == null
                ? Collections.emptyMap()
                : adminUserApi.getUserMap(adminUserIds);
        if (adminMap == null) {
            adminMap = Collections.emptyMap();
        }
        final Map<Long, AdminUserRespDTO> adminMapFinal = adminMap;

        List<AppCmsUserActionRespVO> voList = records.stream().map(action -> {
            AppCmsUserActionRespVO vo = new AppCmsUserActionRespVO();
            vo.setId(action.getId());
            vo.setActionType(action.getActionType());
            vo.setCreateTime(action.getCreateTime());
            vo.setActionUserId(action.getUserId());

            MemberUserRespDTO member = memberMapFinal.get(action.getUserId());
            if (member != null) {
                vo.setActionUserNickname(member.getNickname());
                vo.setActionUserAvatar(member.getAvatar());
            }

            vo.setArticleId(action.getArticleId());
            CmsArticleDO article = articleMap.get(action.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getTitle());
                vo.setArticleSubtitle(article.getSubtitle());
                vo.setArticleCoverImages(article.getCoverImages());
                vo.setArticleAuthorName(article.getAuthorName());
                String authorAvatar = article.getAuthorAvatar();
                if (!StringUtils.hasText(authorAvatar)) {
                    if ("admin".equals(article.getAuthorType())) {
                        AdminUserRespDTO admin = adminMapFinal.get(article.getAuthorId());
                        if (admin != null && StringUtils.hasText(admin.getAvatar())) {
                            authorAvatar = admin.getAvatar();
                        }
                    } else if ("user".equals(article.getAuthorType())) {
                        MemberUserRespDTO authorMember = memberMapFinal.get(article.getAuthorId());
                        if (authorMember != null && StringUtils.hasText(authorMember.getAvatar())) {
                            authorAvatar = authorMember.getAvatar();
                        }
                    }
                }
                vo.setArticleAuthorAvatar(authorAvatar);
                vo.setArticleIsOfficial(article.getIsOfficial());
                vo.setArticlePublishTime(article.getPublishTime());
                vo.setArticleLikeCount(article.getLikeCount());
                vo.setArticleCollectCount(article.getCollectCount());
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

}
