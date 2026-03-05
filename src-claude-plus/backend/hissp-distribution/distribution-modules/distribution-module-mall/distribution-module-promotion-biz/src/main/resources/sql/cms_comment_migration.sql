-- 为CMS文章表添加comment_count字段
ALTER TABLE `cms_article`
ADD COLUMN `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数' AFTER `share_count`;

-- 为CMS文章评论表添加like_count字段(如果表已经存在)
ALTER TABLE `promotion_cms_article_comment`
ADD COLUMN `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数量' AFTER `reply_count`;
