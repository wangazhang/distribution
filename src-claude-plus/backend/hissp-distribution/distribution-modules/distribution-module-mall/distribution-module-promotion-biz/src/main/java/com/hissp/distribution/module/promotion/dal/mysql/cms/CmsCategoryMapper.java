package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CMS分类 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsCategoryMapper extends BaseMapperX<CmsCategoryDO> {

    /**
     * 根据板块ID查询分类列表
     *
     * @param sectionId 板块ID
     * @return 分类列表
     */
    default List<CmsCategoryDO> selectListBySectionId(Long sectionId) {
        return selectList(new LambdaQueryWrapperX<CmsCategoryDO>()
                .eq(CmsCategoryDO::getSectionId, sectionId)
                .orderByAsc(CmsCategoryDO::getSort));
    }

    /**
     * 根据板块ID和状态查询分类列表
     *
     * @param sectionId 板块ID
     * @param status 状态
     * @return 分类列表
     */
    default List<CmsCategoryDO> selectListBySectionIdAndStatus(Long sectionId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CmsCategoryDO>()
                .eq(CmsCategoryDO::getSectionId, sectionId)
                .eq(CmsCategoryDO::getStatus, status)
                .orderByAsc(CmsCategoryDO::getSort));
    }

    /**
     * 分页查询分类列表
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param sectionId 板块ID
     * @param name 分类名称(模糊查询)
     * @param status 状态
     * @return 分页结果
     */
    default PageResult<CmsCategoryDO> selectPage(Integer pageNo, Integer pageSize,
                                                   Long sectionId, String name, Integer status) {
        return selectPage(new PageParam(pageNo, pageSize), new LambdaQueryWrapperX<CmsCategoryDO>()
                .eqIfPresent(CmsCategoryDO::getSectionId, sectionId)
                .likeIfPresent(CmsCategoryDO::getName, name)
                .eqIfPresent(CmsCategoryDO::getStatus, status)
                .orderByAsc(CmsCategoryDO::getSort)
                .orderByDesc(CmsCategoryDO::getId));
    }

}