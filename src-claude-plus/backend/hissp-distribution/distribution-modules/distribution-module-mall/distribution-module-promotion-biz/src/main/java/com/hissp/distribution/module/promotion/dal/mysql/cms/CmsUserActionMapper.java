package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsUserActionDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * CMS用户行为 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsUserActionMapper extends BaseMapperX<CmsUserActionDO> {

    /**
     * 根据用户ID和行为类型查询行为列表
     *
     * @param userId 用户ID
     * @param actionType 行为类型
     * @return 行为列表
     */
    default List<CmsUserActionDO> selectListByUserIdAndActionType(Long userId, String actionType) {
        return selectList(new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getUserId, userId)
                .eq(CmsUserActionDO::getActionType, actionType)
                .orderByDesc(CmsUserActionDO::getCreateTime));
    }

    /**
     * 根据文章ID和行为类型查询行为列表
     *
     * @param articleId 文章ID
     * @param actionType 行为类型
     * @return 行为列表
     */
    default List<CmsUserActionDO> selectListByArticleIdAndActionType(Long articleId, String actionType) {
        return selectList(new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getArticleId, articleId)
                .eq(CmsUserActionDO::getActionType, actionType)
                .orderByDesc(CmsUserActionDO::getCreateTime));
    }

    /**
     * 根据用户ID、文章ID和行为类型查询行为
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param actionType 行为类型
     * @return 用户行为
     */
    default CmsUserActionDO selectByUserIdAndArticleIdAndActionType(Long userId, Long articleId, String actionType) {
        return selectOne(new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getUserId, userId)
                .eq(CmsUserActionDO::getArticleId, articleId)
                .eq(CmsUserActionDO::getActionType, actionType));
    }

    /**
     * 根据文章ID、用户ID和行为类型查询行为（参数顺序不同的重载方法）
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param actionType 行为类型
     * @return 用户行为
     */
    default CmsUserActionDO selectByArticleIdAndUserIdAndType(Long articleId, Long userId, String actionType) {
        return selectByUserIdAndArticleIdAndActionType(userId, articleId, actionType);
    }

    /**
     * 根据用户ID分页查询用户的点赞或收藏记录
     *
     * @param pageParam 分页参数
     * @param userId 用户ID
     * @param actionType 行为类型
     * @return 分页结果
     */
    default PageResult<CmsUserActionDO> selectPageByUserId(PageParam pageParam, Long userId, String actionType) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getUserId, userId)
                .eq(CmsUserActionDO::getActionType, actionType)
                .orderByDesc(CmsUserActionDO::getCreateTime));
    }

    /**
     * 根据用户ID和文章ID删除行为记录（物理删除，避免唯一键冲突）
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param actionType 行为类型
     */
    @Delete("DELETE FROM cms_user_action WHERE user_id = #{userId} AND article_id = #{articleId} AND action_type = #{actionType}")
    void deleteByUserIdAndArticleIdAndActionTypePhysical(Long userId, Long articleId, String actionType);

    /**
     * 根据文章ID删除所有行为记录
     *
     * @param articleId 文章ID
     */
    default void deleteByArticleId(Long articleId) {
        delete(CmsUserActionDO::getArticleId, articleId);
    }

    /**
     * 根据用户ID和文章ID删除行为记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    default void deleteByUserIdAndArticleId(Long userId, Long articleId) {
        delete(new LambdaUpdateWrapper<CmsUserActionDO>()
                .eq(CmsUserActionDO::getUserId, userId)
                .eq(CmsUserActionDO::getArticleId, articleId));
    }

    /**
     * 统计用户某种行为的数量
     *
     * @param userId 用户ID
     * @param actionType 行为类型
     * @return 数量
     */
    default Long countByUserIdAndActionType(Long userId, String actionType) {
        return selectCount(new LambdaQueryWrapperX<CmsUserActionDO>()
                .eq(CmsUserActionDO::getUserId, userId)
                .eq(CmsUserActionDO::getActionType, actionType));
    }

}
