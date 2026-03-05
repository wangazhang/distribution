package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CMS板块 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsSectionMapper extends BaseMapperX<CmsSectionDO> {

    /**
     * 根据板块类型查询板块列表
     *
     * @param type 板块类型
     * @return 板块列表
     */
    default List<CmsSectionDO> selectListByType(String type) {
        return selectList(CmsSectionDO::getType, type);
    }

    /**
     * 根据状态查询板块列表
     *
     * @param status 状态
     * @return 板块列表
     */
    default List<CmsSectionDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<CmsSectionDO>()
                .eq(CmsSectionDO::getStatus, status)
                .orderByAsc(CmsSectionDO::getSort));
    }

    /**
     * 查询所有板块列表
     *
     * @return 板块列表
     */
    default List<CmsSectionDO> selectList() {
        return selectList(new LambdaQueryWrapperX<CmsSectionDO>()
                .orderByAsc(CmsSectionDO::getSort));
    }

    /**
     * 分页查询板块列表
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param name 板块名称(模糊查询)
     * @param type 板块类型
     * @param status 状态
     * @return 分页结果
     */
    default PageResult<CmsSectionDO> selectPage(Integer pageNo, Integer pageSize,
                                                String name, String type, Integer status) {
        return selectPage(new PageParam(pageNo, pageSize), new LambdaQueryWrapperX<CmsSectionDO>()
                .likeIfPresent(CmsSectionDO::getName, name)
                .eqIfPresent(CmsSectionDO::getType, type)
                .eqIfPresent(CmsSectionDO::getStatus, status)
                .orderByAsc(CmsSectionDO::getSort)
                .orderByDesc(CmsSectionDO::getId));
    }

}