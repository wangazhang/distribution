package com.hissp.distribution.module.promotion.convert.cms;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleSaveReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticleDetailRespVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticleRespVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppProductSimpleRespVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import com.hissp.distribution.module.promotion.service.cms.CmsSectionService;
import com.hissp.distribution.module.promotion.service.cms.CmsUserActionService;
import com.hissp.distribution.module.system.api.user.AdminUserApi;
import com.hissp.distribution.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CMS文章 Convert
 *
 * @author 芋道源码
 */
@Mapper
public abstract class CmsArticleConvert {

    public static final CmsArticleConvert INSTANCE = Mappers.getMapper(CmsArticleConvert.class);

    // CmsSectionService 属性，通过手动设置
    public CmsSectionService cmsSectionService;

    // AdminUserApi 属性，通过手动设置
    public AdminUserApi adminUserApi;

    // MemberUserApi 属性，通过手动设置
    public MemberUserApi memberUserApi;

    // CmsUserActionService 属性，通过手动设置
    public CmsUserActionService cmsUserActionService;

    // ProductSpuApi 属性，通过手动设置
    public ProductSpuApi productSpuApi;

    public abstract CmsArticleDO convert(CmsArticleSaveReqVO bean);

    public abstract CmsArticleRespVO convert(CmsArticleDO bean);

    public abstract List<CmsArticleRespVO> convertList(List<CmsArticleDO> list);

    public abstract PageResult<CmsArticleRespVO> convertPage(PageResult<CmsArticleDO> page);

    public abstract AppCmsArticleRespVO convertApp(CmsArticleDO bean);

    public abstract List<AppCmsArticleRespVO> convertAppList(List<CmsArticleDO> list);

    public abstract AppCmsArticleDetailRespVO convertAppDetail(CmsArticleDO bean);

    public abstract PageResult<AppCmsArticleRespVO> convertAppPage(PageResult<CmsArticleDO> page);

    /**
     * 获取板块类型
     */
    protected String getSectionType(Long sectionId) {
        if (sectionId == null || cmsSectionService == null) {
            return "article";
        }
        try {
            CmsSectionDO section = cmsSectionService.getSection(sectionId);
            return section != null ? section.getType() : "article";
        } catch (Exception e) {
            return "article";
        }
    }

    /**
     * 获取作者头像
     */
    protected String getAuthorAvatar(Long authorId, String authorType, String existingAvatar) {
        // 如果已有头像，直接返回
        if (StringUtils.hasText(existingAvatar)) {
            return existingAvatar;
        }

        // 如果作者是管理员类型，尝试从AdminUserApi获取头像
        if ("admin".equals(authorType) && authorId != null && adminUserApi != null) {
            try {
                AdminUserRespDTO adminUser = adminUserApi.getUser(authorId);
                if (adminUser != null && adminUser.getAvatar() != null && !adminUser.getAvatar().trim().isEmpty()) {
                    return adminUser.getAvatar();
                }
            } catch (Exception e) {
                // 获取失败时返回null，使用默认头像
            }
        }

        // 如果作者是前台用户类型，尝试从MemberUserApi获取头像
        if ("user".equals(authorType) && authorId != null && memberUserApi != null) {
            try {
                MemberUserRespDTO memberUser = memberUserApi.getUser(authorId);
                if (memberUser != null && StringUtils.hasText(memberUser.getAvatar())) {
                    return memberUser.getAvatar();
                }
            } catch (Exception e) {
                // 获取失败时返回null，使用默认头像
            }
        }

        return null;
    }

    /**
     * 转换后设置 sectionType、authorAvatar 和用户状态 - App列表
     */
    @AfterMapping
    protected void afterConvertApp(@MappingTarget AppCmsArticleRespVO target, CmsArticleDO source) {
        if (target != null && source != null) {
            target.setSectionType(getSectionType(source.getSectionId()));
            // 设置作者头像
            String avatar = getAuthorAvatar(source.getAuthorId(), source.getAuthorType(), source.getAuthorAvatar());
            target.setAuthorAvatar(avatar);

            // 设置用户点赞状态
            setUserActionStatus(target, source.getId());
            fillArticleFlags(target, source);
        }
    }

    /**
     * 转换后设置 sectionType、authorAvatar 和关联商品 - App详情
     */
    @AfterMapping
    protected void afterConvertAppDetail(@MappingTarget AppCmsArticleDetailRespVO target, CmsArticleDO source) {
        if (target != null && source != null) {
            target.setSectionType(getSectionType(source.getSectionId()));
            // 设置作者头像
            String avatar = getAuthorAvatar(source.getAuthorId(), source.getAuthorType(), source.getAuthorAvatar());
            target.setAuthorAvatar(avatar);
            fillArticleFlags(target, source);
            // 设置关联商品信息
            target.setProducts(getProductList(source.getProductIds()));
        }
    }

    /**
     * 设置用户点赞和收藏状态
     */
    protected void setUserActionStatus(AppCmsArticleRespVO target, Long articleId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId != null && cmsUserActionService != null) {
            try {
                target.setUserLiked(cmsUserActionService.hasLiked(userId, articleId));
                target.setUserCollected(cmsUserActionService.hasCollected(userId, articleId));
            } catch (Exception e) {
                // 查询失败时默认为false
                target.setUserLiked(false);
                target.setUserCollected(false);
            }
        } else {
            // 用户未登录时默认为false
            target.setUserLiked(false);
            target.setUserCollected(false);
        }
    }

    private void fillArticleFlags(AppCmsArticleRespVO target, CmsArticleDO source) {
        target.setEnableLike(source.getEnableLike());
        target.setShowLikeCount(source.getShowLikeCount());
        target.setInitialLikeCount(source.getInitialLikeCount());
        target.setEnableCollect(source.getEnableCollect());
        target.setShowCollectCount(source.getShowCollectCount());
        target.setInitialCollectCount(source.getInitialCollectCount());
        target.setEnableShare(source.getEnableShare());
        target.setShowShareCount(source.getShowShareCount());
        target.setInitialShareCount(source.getInitialShareCount());
        target.setEnableDownload(source.getEnableDownload());
        target.setEnableRegister(source.getEnableRegister());
    }

    /**
     * 获取关联商品列表
     *
     * @param productIds 商品ID列表
     * @return 商品简单信息列表
     */
    protected List<AppProductSimpleRespVO> getProductList(List<Long> productIds) {
        // 如果没有关联商品，返回空列表（不是null）
        if (CollUtil.isEmpty(productIds) || productSpuApi == null) {
            return new ArrayList<>();
        }

        try {
            // 批量查询商品信息
            List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(productIds);
            if (CollUtil.isEmpty(spuList)) {
                return new ArrayList<>();
            }

            // 转换为简单VO
            return spuList.stream()
                    .map(this::convertToProductSimpleVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 查询失败时返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 将 ProductSpuRespDTO 转换为 AppProductSimpleRespVO
     *
     * @param spu 商品SPU DTO
     * @return 商品简单信息VO
     */
    protected AppProductSimpleRespVO convertToProductSimpleVO(ProductSpuRespDTO spu) {
        if (spu == null) {
            return null;
        }

        AppProductSimpleRespVO vo = new AppProductSimpleRespVO();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setPicUrl(spu.getPicUrl());
        vo.setPrice(spu.getPrice());
        vo.setMarketPrice(spu.getMarketPrice());
        // ProductSpuRespDTO 没有 salesCount 字段，设置为 0 或不设置
        vo.setSalesCount(0);
        vo.setStatus(spu.getStatus());
        // 根据业务需求设置是否为种草商品，这里默认为true（关联到CMS文章的商品认为是种草商品）
        vo.setIsRecommend(true);
        return vo;
    }

}
