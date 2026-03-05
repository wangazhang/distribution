package com.hissp.distribution.module.promotion.dal.dataobject.cms;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.List;

/**
 * CMS文章评论 DO
 *
 * @author distribution
 */
@TableName(value = "promotion_cms_article_comment", autoResultMap = true)
@KeySequence("promotion_cms_article_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsArticleCommentDO extends BaseDO {

    /**
     * 默认匿名昵称
     */
    public static final String NICKNAME_ANONYMOUS = "匿名用户";

    /**
     * 评论编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 文章编号
     * 关联 CmsArticleDO 的 id 编号
     */
    private Long articleId;

    /**
     * 评论者用户编号
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 评论者昵称
     */
    private String userNickname;

    /**
     * 评论者头像
     */
    private String userAvatar;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 父评论ID
     * 0 表示一级评论(直接评论文章)
     * 非0 表示二级评论(回复某条评论)
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片地址数组，JSON 格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;

    /**
     * 是否可见
     * true: 显示
     * false: 隐藏
     */
    private Boolean visible;

    /**
     * 回复数量
     * 记录该评论收到的回复数
     */
    private Integer replyCount;

    /**
     * 点赞数量
     */
    private Integer likeCount;

    /**
     * 是否已读
     * 针对被评论者/被回复者
     * true: 已读
     * false: 未读
     */
    private Boolean readStatus;

    /**
     * 文章作者ID
     * 冗余字段,用于快速查询收到的评论
     * 关联 CmsArticleDO 的 authorId
     */
    private Long articleAuthorId;

}
