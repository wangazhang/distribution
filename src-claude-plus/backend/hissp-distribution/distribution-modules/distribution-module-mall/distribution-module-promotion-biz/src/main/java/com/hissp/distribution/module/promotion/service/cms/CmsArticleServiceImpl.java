package com.hissp.distribution.module.promotion.service.cms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleAuditReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticlePageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleSaveReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticlePageReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticlePublishReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsArticleConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleTagDO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsUserActionDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleMapper;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleTagMapper;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsUserActionMapper;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_ARTICLE_ALREADY_PUBLISHED;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_ARTICLE_AUDIT_STATUS_NOT_PENDING;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_ARTICLE_NOT_APPROVED;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_ARTICLE_NOT_EXISTS;

/**
 * CMS文章 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CmsArticleServiceImpl implements CmsArticleService {

    @Resource
    private CmsArticleMapper cmsArticleMapper;

    @Resource
    private CmsArticleTagMapper cmsArticleTagMapper;
    @Resource
    private CmsUserActionMapper cmsUserActionMapper;

    @Resource
    private com.hissp.distribution.module.member.api.user.MemberUserApi memberUserApi;

    @Resource
    private CmsSectionService cmsSectionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(CmsArticleSaveReqVO createReqVO) {
        // 插入文章
        CmsArticleDO article = CmsArticleConvert.INSTANCE.convert(createReqVO);
        // 初始化统计数据
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setShareCount(0);
        // 初始化状态
        article.setAuditStatus("pending"); // 待审核
        article.setPublishStatus(0); // 未发布
        article.setIsHot(0);
        article.setIsOfficial(0);

        // 设置作者信息
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId != null) {
            article.setAuthorId(userId);
            article.setAuthorType("admin");
            article.setAuthorName("系统管理员");
        }

        cmsArticleMapper.insert(article);

        // 保存文章标签关联
        saveArticleTags(article.getId(), createReqVO.getTagIds());

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishUserArticle(AppCmsArticlePublishReqVO publishReqVO) {
        // 获取当前登录用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 转换为DO对象
        CmsArticleDO article = new CmsArticleDO();
        article.setSectionId(publishReqVO.getSectionId());
        article.setCategoryId(publishReqVO.getCategoryId());
        article.setTitle(publishReqVO.getTitle());
        article.setSubtitle(publishReqVO.getSubtitle());
        article.setCoverImages(publishReqVO.getCoverImages());
        article.setContent(publishReqVO.getContent());
        article.setContentType(publishReqVO.getContentType());
        article.setProductIds(publishReqVO.getProductIds());

        // 设置统计数据
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setShareCount(0);

        // 根据板块配置决定是否需要审核
        boolean requireAudit = cmsSectionService.isAuditRequired(publishReqVO.getSectionId());

        if (requireAudit) {
            // 需要审核
            article.setAuditStatus("pending"); // 待审核
            article.setPublishStatus(0); // 未发布
        } else {
            // 不需要审核，直接发布
            article.setAuditStatus("approved"); // 已通过审核
            article.setPublishStatus(1); // 已发布
            article.setPublishTime(LocalDateTime.now());
        }

        article.setIsHot(0);
        article.setIsOfficial(0);

        // 设置作者信息
        article.setAuthorId(userId);
        article.setAuthorType("user");
        // 从会员 API 获取用户信息
       MemberUserRespDTO memberUser = memberUserApi.getUser(userId);
        if (memberUser != null) {
            article.setAuthorName(memberUser.getNickname() != null ? memberUser.getNickname() : "用户");
            article.setAuthorAvatar(memberUser.getAvatar());
        } else {
            article.setAuthorName("用户");
        }

        // 插入文章
        cmsArticleMapper.insert(article);

        // 保存文章标签关联
        saveArticleTags(article.getId(), publishReqVO.getTagIds());

        // TODO: 保存商品关联关系（如果有商品关联表的话）
        // saveArticleProducts(article.getId(), publishReqVO.getProductIds());

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(CmsArticleSaveReqVO updateReqVO) {
        // 校验存在
        validateArticleExists(updateReqVO.getId());

        // 校验文章状态,已发布的文章不能修改
        CmsArticleDO article = cmsArticleMapper.selectById(updateReqVO.getId());
        if (article.getPublishStatus() == 1) {
            throw exception(CMS_ARTICLE_ALREADY_PUBLISHED);
        }

        // 更新文章
        CmsArticleDO updateObj = CmsArticleConvert.INSTANCE.convert(updateReqVO);
        cmsArticleMapper.updateById(updateObj);

        // 更新文章标签关联
        saveArticleTags(updateReqVO.getId(), updateReqVO.getTagIds());
    }

    /**
     * 保存文章标签关联
     *
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        // 先物理清除历史关联，避免软删除记录占用唯一索引导致分类切换报错
        cmsArticleTagMapper.deleteAllByArticleId(articleId);

        // 添加新的关联
        if (CollUtil.isNotEmpty(tagIds)) {
            List<CmsArticleTagDO> articleTagList = tagIds.stream()
                    .map(tagId -> CmsArticleTagDO.builder()
                            .articleId(articleId)
                            .tagId(tagId)
                            .build())
                    .collect(Collectors.toList());
            articleTagList.forEach(cmsArticleTagMapper::insert);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        // 校验存在
        validateArticleExists(id);

        // 删除文章
        cmsArticleMapper.deleteById(id);

        // 同步清理标签关联，防止遗留逻辑删除记录
        cmsArticleTagMapper.deleteAllByArticleId(id);
    }

    @Override
    public CmsArticleDO getArticle(Long id) {
        return cmsArticleMapper.selectById(id);
    }

    /**
     * 获取文章详情（包含标签信息）
     *
     * @param id 文章ID
     * @return 文章详情（包含标签信息）
     */
    @Override
    public CmsArticleRespVO getArticleWithTags(Long id) {
        CmsArticleDO article = cmsArticleMapper.selectById(id);
        if (article == null) {
            return null;
        }

        // 查询文章标签关联
        List<CmsArticleTagDO> articleTagList = cmsArticleTagMapper.selectListByArticleId(id);
        List<Long> tagIds = articleTagList.stream()
                .map(CmsArticleTagDO::getTagId)
                .collect(Collectors.toList());

        // 转换为VO并设置标签信息
        CmsArticleRespVO respVO = CmsArticleConvert.INSTANCE.convert(article);
        respVO.setTagIds(tagIds);
        return respVO;
    }

    /**
     * 获取文章分页列表（包含标签信息）
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果（包含标签信息）
     */
    @Override
    public PageResult<CmsArticleRespVO> getArticlePageWithTags(CmsArticlePageReqVO pageReqVO) {
        // 先获取文章列表
        PageResult<CmsArticleDO> pageResult = getArticlePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(ListUtil.empty(), pageResult.getTotal());
        }

        // 查询所有文章的标签关联
        List<Long> articleIds = pageResult.getList().stream()
                .map(CmsArticleDO::getId)
                .collect(Collectors.toList());

        // 批量查询标签关联
        List<CmsArticleTagDO> articleTagList = cmsArticleTagMapper.selectList(
                new LambdaQueryWrapperX<CmsArticleTagDO>()
                        .in(CmsArticleTagDO::getArticleId, articleIds)
        );

        // 按文章ID分组标签
        java.util.Map<Long, List<Long>> articleTagMap = articleTagList.stream()
                .collect(Collectors.groupingBy(
                        CmsArticleTagDO::getArticleId,
                        Collectors.mapping(CmsArticleTagDO::getTagId, Collectors.toList())
                ));

        // 转换为包含标签的VO列表
        List<CmsArticleRespVO> respVOList = pageResult.getList().stream()
                .map(article -> {
                    CmsArticleRespVO respVO = CmsArticleConvert.INSTANCE.convert(article);
                    respVO.setTagIds(articleTagMap.getOrDefault(article.getId(), ListUtil.empty()));
                    return respVO;
                })
                .collect(Collectors.toList());

        return new PageResult<>(respVOList, pageResult.getTotal());
    }

    @Override
    public PageResult<CmsArticleDO> getArticlePage(CmsArticlePageReqVO pageReqVO) {
        // 如果指定了标签ID,需要先查询该标签关联的文章ID列表
        if (CollUtil.isNotEmpty(pageReqVO.getTagIds())) {
            // 查询所有标签关联的文章ID列表（并集关系）
            java.util.Set<Long> allArticleIds = new java.util.HashSet<>();
            for (Long tagId : pageReqVO.getTagIds()) {
                List<CmsArticleTagDO> articleTagList = cmsArticleTagMapper.selectListByTagId(tagId);
                if (CollUtil.isNotEmpty(articleTagList)) {
                    List<Long> tagArticleIds = articleTagList.stream()
                            .map(CmsArticleTagDO::getArticleId)
                            .collect(Collectors.toList());
                    allArticleIds.addAll(tagArticleIds); // 取并集（文章包含任意一个标签即可）
                }
            }

            if (CollUtil.isEmpty(allArticleIds)) {
                return PageResult.empty();
            }

            // 使用文章ID列表进行查询
            return cmsArticleMapper.selectPageByIds(
                    new PageParam(pageReqVO.getPageNo(), pageReqVO.getPageSize()),
                    allArticleIds,
                    pageReqVO.getSectionId(), pageReqVO.getCategoryId(),
                    pageReqVO.getTitle(), pageReqVO.getAuthorType(), pageReqVO.getAuthorId(),
                    pageReqVO.getAuditStatus(), pageReqVO.getPublishStatus(),
                    pageReqVO.getIsHot(), pageReqVO.getIsOfficial(),
                    pageReqVO.getCreateTime()
            );
        }

        // 未指定标签，直接查询
        return cmsArticleMapper.selectPage(
                new PageParam(pageReqVO.getPageNo(), pageReqVO.getPageSize()),
                pageReqVO.getSectionId(), pageReqVO.getCategoryId(),
                pageReqVO.getTitle(), pageReqVO.getAuthorType(), pageReqVO.getAuthorId(),
                pageReqVO.getAuditStatus(), pageReqVO.getPublishStatus(),
                pageReqVO.getIsHot(), pageReqVO.getIsOfficial(),
                pageReqVO.getCreateTime()
        );
    }

    @Override
    public PageResult<CmsArticleDO> getPublishedArticlePage(AppCmsArticlePageReqVO pageReqVO) {
        // 如果指定了标签ID,需要先查询该标签关联的文章ID列表
        if (pageReqVO.getTagId() != null) {
            // 查询标签关联的文章
            List<CmsArticleTagDO> articleTagList = cmsArticleTagMapper.selectListByTagId(pageReqVO.getTagId());
            if (CollUtil.isEmpty(articleTagList)) {
                return PageResult.empty();
            }

            // 获取文章ID列表
            List<Long> articleIds = articleTagList.stream()
                    .map(CmsArticleTagDO::getArticleId)
                    .collect(Collectors.toList());

            // 查询已发布的文章
            PageResult<CmsArticleDO> pageResult = cmsArticleMapper.selectPublishedPage(
                    pageReqVO.getPageNo(), pageReqVO.getPageSize(),
                    pageReqVO.getSectionId(), pageReqVO.getCategoryId(),
                    pageReqVO.getIsHot(), pageReqVO.getIsOfficial()
            );

            // 过滤出在articleIds中的文章
            List<CmsArticleDO> filteredList = pageResult.getList().stream()
                    .filter(article -> articleIds.contains(article.getId()))
                    .collect(Collectors.toList());

            return new PageResult<>(filteredList, pageResult.getTotal());
        }

        // 未指定标签,直接查询
        return cmsArticleMapper.selectPublishedPage(
                pageReqVO.getPageNo(), pageReqVO.getPageSize(),
                pageReqVO.getSectionId(), pageReqVO.getCategoryId(),
                pageReqVO.getIsHot(), pageReqVO.getIsOfficial()
        );
    }

    @Override
    public List<CmsArticleDO> getArticleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return cmsArticleMapper.selectBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditArticle(CmsArticleAuditReqVO auditReqVO) {
        // 校验存在
        validateArticleExists(auditReqVO.getId());

        // 校验审核状态
        CmsArticleDO article = cmsArticleMapper.selectById(auditReqVO.getId());
        if (!"pending".equals(article.getAuditStatus())) {
            throw exception(CMS_ARTICLE_AUDIT_STATUS_NOT_PENDING);
        }

        // 更新审核状态
        CmsArticleDO updateObj = CmsArticleDO.builder()
                .id(auditReqVO.getId())
                .auditStatus(auditReqVO.getAuditStatus())
                .auditRemark(auditReqVO.getAuditRemark())
                .build();
        cmsArticleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long id) {
        // 校验存在
        validateArticleExists(id);

        // 校验审核状态
        CmsArticleDO article = cmsArticleMapper.selectById(id);
        if (!"approved".equals(article.getAuditStatus())) {
            throw exception(CMS_ARTICLE_NOT_APPROVED);
        }

        // 更新发布状态
        CmsArticleDO updateObj = CmsArticleDO.builder()
                .id(id)
                .publishStatus(1)
                .publishTime(LocalDateTime.now())
                .build();
        cmsArticleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishArticle(Long id) {
        // 校验存在
        validateArticleExists(id);

        // 更新发布状态
        CmsArticleDO updateObj = CmsArticleDO.builder()
                .id(id)
                .publishStatus(0)
                .build();
        cmsArticleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        // 校验存在
        CmsArticleDO article = cmsArticleMapper.selectById(id);
        if (article == null) {
            throw exception(CMS_ARTICLE_NOT_EXISTS);
        }

        // 增加浏览数
        CmsArticleDO updateObj = CmsArticleDO.builder()
                .id(id)
                .viewCount(article.getViewCount() + 1)
                .build();
        cmsArticleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementShareCount(Long id) {
        // 校验存在
        CmsArticleDO article = cmsArticleMapper.selectById(id);
        if (article == null) {
            throw exception(CMS_ARTICLE_NOT_EXISTS);
        }

        // 增加分享数
        CmsArticleDO updateObj = CmsArticleDO.builder()
                .id(id)
                .shareCount(article.getShareCount() + 1)
                .build();
        cmsArticleMapper.updateById(updateObj);
    }

    @Override
    public void validateArticleExists(Long id) {
        if (cmsArticleMapper.selectById(id) == null) {
            throw exception(CMS_ARTICLE_NOT_EXISTS);
        }
    }

    @Override
    public List<CmsArticleDO> getHotArticleList(Long sectionId, Integer limit) {
        return cmsArticleMapper.selectHotList(sectionId, limit);
    }

    @Override
    public List<CmsArticleDO> getHotArticleList(Integer limit) {
        return getHotArticleList(null, limit);
    }

    @Override
    public List<CmsArticleDO> getRecommendArticleList(Long sectionId, Integer limit) {
        return cmsArticleMapper.selectRecommendList(sectionId, limit);
    }

    @Override
    public List<CmsArticleDO> getRecommendArticleList(Integer limit) {
        return getRecommendArticleList(null, limit);
    }

    @Override
    public PageResult<CmsArticleDO> getAppArticlePage(AppCmsArticlePageReqVO pageVO) {
        // 如果指定了作者ID，说明是查询"我的文章"，需要支持不同状态的文章
        if (pageVO.getAuthorId() != null) {
            return cmsArticleMapper.selectPageByAuthor(
                    pageVO.getPageNo(),
                    pageVO.getPageSize(),
                    pageVO.getSectionId(),
                    pageVO.getCategoryId(),
                    pageVO.getTitle(),
                    pageVO.getAuthorId(),
                    pageVO.getAuthorType(), // 作者类型筛选
                    pageVO.getAuditStatus(), // 审核状态筛选
                    pageVO.getPublishStatus() // 发布状态筛选
            );
        }

        // 否则使用原有方法（只返回已发布且已审核的文章）
        return cmsArticleMapper.selectPublishedPage(
                pageVO.getPageNo(),
                pageVO.getPageSize(),
                pageVO.getSectionId(),
                pageVO.getCategoryId(),
                pageVO.getTitle(),
                pageVO.getIsHot(),
                pageVO.getIsOfficial()
        );
    }

    @Override
    public CmsArticleDO getAppArticleDetail(Long id) {
        CmsArticleDO article = cmsArticleMapper.selectById(id);
        // 只返回已发布且已审核的文章
        if (article == null) {
            throw exception(CMS_ARTICLE_NOT_EXISTS);
        }
        if (article.getPublishStatus() != 1 || !"approved".equals(article.getAuditStatus())) {
            throw exception(CMS_ARTICLE_NOT_APPROVED);
        }
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteArticle(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量删除文章
        cmsArticleMapper.deleteBatchIds(ids);
        // TODO: 后续可以添加批量删除关联数据的优化
        // 目前由数据库级联删除处理关联数据，或者后续添加专门的批量删除方法
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditArticle(Collection<Long> ids, String auditStatus, String auditRemark) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(id)
                    .auditStatus(auditStatus)
                    .auditRemark(auditRemark)
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublishArticle(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(id)
                    .publishStatus(1)
                    .publishTime(LocalDateTime.now())
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUnpublishArticle(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            CmsArticleDO updateObj = CmsArticleDO.builder()
                    .id(id)
                    .publishStatus(0)
                    .build();
            cmsArticleMapper.updateById(updateObj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long articleId, Long userId) {
        // 校验文章存在
        validateArticleExists(articleId);

        try {
            // 尝试插入点赞记录（利用数据库唯一键防止并发问题）
            CmsUserActionDO newAction = CmsUserActionDO.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .actionType("LIKE")
                    .build();
            cmsUserActionMapper.insert(newAction);

            // 插入成功，说明之前没有点赞记录，增加文章点赞数
            CmsArticleDO article = cmsArticleMapper.selectById(articleId);
            if (article != null) {
                CmsArticleDO updateObj = CmsArticleDO.builder()
                        .id(articleId)
                        .likeCount(article.getLikeCount() + 1)
                        .build();
                cmsArticleMapper.updateById(updateObj);
            }
            return true;

        } catch (Exception e) {
            // 插入失败，说明已有点赞记录（可能是已点赞或并发冲突）
            // 查询当前状态
            CmsUserActionDO userAction = cmsUserActionMapper.selectByArticleIdAndUserIdAndType(
                    articleId, userId, "LIKE"
            );

            if (userAction == null) {
                // 这种情况可能是由于并发导致的数据不一致，尝试重新查询
                try {
                    Thread.sleep(10); // 短暂等待
                    userAction = cmsUserActionMapper.selectByArticleIdAndUserIdAndType(
                            articleId, userId, "LIKE"
                    );
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            if (userAction == null) {
                // 仍然查不到记录，可能是数据异常，直接返回未点赞状态
                return false;
            }

            // 记录存在，执行取消点赞操作（使用物理删除避免唯一键冲突）
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
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long articleId, Long userId) {
        // 校验文章存在
        validateArticleExists(articleId);

        try {
            // 尝试插入收藏记录（利用数据库唯一键防止并发问题）
            CmsUserActionDO newAction = CmsUserActionDO.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .actionType("COLLECT")
                    .build();
            cmsUserActionMapper.insert(newAction);

            // 插入成功，说明之前没有收藏记录，增加文章收藏数
            CmsArticleDO article = cmsArticleMapper.selectById(articleId);
            if (article != null) {
                CmsArticleDO updateObj = CmsArticleDO.builder()
                        .id(articleId)
                        .collectCount(article.getCollectCount() + 1)
                        .build();
                cmsArticleMapper.updateById(updateObj);
            }
            return true;

        } catch (Exception e) {
            // 插入失败，说明已有收藏记录（可能是已收藏或并发冲突）
            // 查询当前状态
            CmsUserActionDO userAction = cmsUserActionMapper.selectByArticleIdAndUserIdAndType(
                    articleId, userId, "COLLECT"
            );

            if (userAction == null) {
                // 这种情况可能是由于并发导致的数据不一致，尝试重新查询
                try {
                    Thread.sleep(10); // 短暂等待
                    userAction = cmsUserActionMapper.selectByArticleIdAndUserIdAndType(
                            articleId, userId, "COLLECT"
                    );
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            if (userAction == null) {
                // 仍然查不到记录，可能是数据异常，直接返回未收藏状态
                return false;
            }

            // 记录存在，执行取消收藏操作（使用物理删除避免唯一键冲突）
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
            return false;
        }
    }

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        CmsUserActionDO userAction = cmsUserActionMapper.selectByArticleIdAndUserIdAndType(
                articleId, userId, "LIKE"
        );
        return userAction != null;
    }

    @Override
    public boolean isCollected(Long articleId, Long userId) {
        CmsUserActionDO userAction = cmsUserActionMapper.selectByArticleIdAndUserIdAndType(
                articleId, userId, "COLLECT"
        );
        return userAction != null;
    }

}
