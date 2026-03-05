package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CMS标签 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsTagMapper extends BaseMapperX<CmsTagDO> {

    /**
     * 根据标签名称查询标签
     *
     * @param name 标签名称
     * @return 标签
     */
    default CmsTagDO selectByName(String name) {
        return selectOne(CmsTagDO::getName, name);
    }

    /**
     * 根据状态查询标签列表
     *
     * @param status 状态
     * @return 标签列表
     */
    default List<CmsTagDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<CmsTagDO>()
                .eq(CmsTagDO::getStatus, status)
                .orderByAsc(CmsTagDO::getSort));
    }

    /**
     * 分页查询标签列表
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param name 标签名称(模糊查询)
     * @param status 状态
     * @return 分页结果
     */
    default PageResult<CmsTagDO> selectPage(Integer pageNo, Integer pageSize,
                                             String name, Integer status) {
        return selectPage(new PageParam(pageNo, pageSize), new LambdaQueryWrapperX<CmsTagDO>()
                .likeIfPresent(CmsTagDO::getName, name)
                .eqIfPresent(CmsTagDO::getStatus, status)
                .orderByAsc(CmsTagDO::getSort)
                .orderByDesc(CmsTagDO::getId));
    }

}