package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端 - CMS 用户行为响应 VO
 *
 * @author
 */
@Data
public class AppCmsUserActionRespVO {

    /**
     * 行为记录编号
     */
    private Long id;

    /**
     * 行为类型 like / collect
     */
    private String actionType;

    /**
     * 行为时间
     */
    private LocalDateTime createTime;

    /**
     * 执行动作的用户编号
     */
    private Long actionUserId;

    /**
     * 执行动作的用户昵称
     */
    private String actionUserNickname;

    /**
     * 执行动作的用户头像
     */
    private String actionUserAvatar;

    /**
     * 文章编号
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章副标题
     */
    private String articleSubtitle;

    /**
     * 文章封面图
     */
    private List<String> articleCoverImages;

    /**
     * 文章作者昵称
     */
    private String articleAuthorName;

    /**
     * 文章作者头像
     */
    private String articleAuthorAvatar;

    /**
     * 文章是否官方
     */
    private Integer articleIsOfficial;

    /**
     * 文章发布时间
     */
    private LocalDateTime articlePublishTime;

    /**
     * 文章当前点赞数量
     */
    private Integer articleLikeCount;

    /**
     * 文章当前收藏数量
     */
    private Integer articleCollectCount;

}
