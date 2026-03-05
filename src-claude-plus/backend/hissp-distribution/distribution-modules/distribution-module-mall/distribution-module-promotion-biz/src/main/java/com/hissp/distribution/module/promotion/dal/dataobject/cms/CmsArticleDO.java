package com.hissp.distribution.module.promotion.dal.dataobject.cms;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CMS文章 DO
 *
 * @author 芋道源码
 */
@TableName(value = "cms_article", autoResultMap = true)
@KeySequence("cms_article_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库,可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsArticleDO extends BaseDO {

    /**
     * 文章ID
     */
    @TableId
    private Long id;

    /**
     * 所属板块ID
     *
     * 关联 {@link CmsSectionDO#getId()}
     */
    private Long sectionId;

    /**
     * 所属分类ID
     *
     * 关联 {@link CmsCategoryDO#getId()}
     */
    private Long categoryId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 封面图片(JSON数组)
     * 使用List<String>存储多张图片URL
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> coverImages;

    /**
     * 文章内容(富文本/Markdown)
     */
    private String content;

    /**
     * 内容类型
     * richtext-富文本, markdown-Markdown
     */
    private String contentType;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 作者类型
     * admin-管理员, user-用户
     */
    private String authorType;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否热门
     * 0-否, 1-是
     */
    private Integer isHot;

    /**
     * 是否官方
     * 0-否, 1-是
     */
    private Integer isOfficial;

    /**
     * 审核状态
     * pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 发布状态
     * 0-未发布, 1-已发布
     */
    private Integer publishStatus;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 是否支持点赞
     * 0-不支持, 1-支持
     */
    private Integer enableLike;

    /**
     * 初始点赞数(假数据,运营设置)
     */
    private Integer initialLikeCount;

    /**
     * 是否显示点赞数
     * 0-不显示, 1-显示
     */
    private Integer showLikeCount;

    /**
     * 是否支持收藏
     * 0-不支持, 1-支持
     */
    private Integer enableCollect;

    /**
     * 初始收藏数(假数据,运营设置)
     */
    private Integer initialCollectCount;

    /**
     * 是否显示收藏数
     * 0-不显示, 1-显示
     */
    private Integer showCollectCount;

    /**
     * 是否支持分享
     * 0-不支持, 1-支持
     */
    private Integer enableShare;

    /**
     * 初始分享数(假数据,运营设置)
     */
    private Integer initialShareCount;

    /**
     * 是否显示分享数
     * 0-不显示, 1-显示
     */
    private Integer showShareCount;

    /**
     * 是否支持下载
     * 0-不支持, 1-支持
     */
    private Integer enableDownload;

    /**
     * 是否支持报名(课程类型)
     * 0-不支持, 1-支持
     */
    private Integer enableRegister;

    /**
     * 是否允许移动端发布
     * 0-不允许, 1-允许
     */
    private Integer enableMobilePublish;

    /**
     * 关联商品ID列表(JSON数组)
     * 存储关联的SPU商品ID列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> productIds;

}
