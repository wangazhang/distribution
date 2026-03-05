package com.hissp.distribution.module.infra.dal.mysql.image;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.infra.controller.admin.image.vo.folder.ImageFolderPageReqVO;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageFolderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图片文件夹 Mapper
 *
 * @author hissp
 */
@Mapper
public interface ImageFolderMapper extends BaseMapperX<ImageFolderDO> {

    default PageResult<ImageFolderDO> selectPage(ImageFolderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImageFolderDO>()
                .likeIfPresent(ImageFolderDO::getName, reqVO.getName())
                .eqIfPresent(ImageFolderDO::getParentId, reqVO.getParentId())
                .eqIfPresent(ImageFolderDO::getPermissionType, reqVO.getPermissionType())
                .eqIfPresent(ImageFolderDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ImageFolderDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(ImageFolderDO::getSortOrder)
                .orderByDesc(ImageFolderDO::getId));
    }

    default List<ImageFolderDO> selectListByParentId(@Param("parentId") Long parentId) {
        return selectList(new LambdaQueryWrapperX<ImageFolderDO>()
                .eqIfPresent(ImageFolderDO::getParentId, parentId)
                .orderByAsc(ImageFolderDO::getSortOrder)
                .orderByDesc(ImageFolderDO::getId));
    }

    default List<ImageFolderDO> selectTree() {
        return selectList(new LambdaQueryWrapperX<ImageFolderDO>()
                .orderByAsc(ImageFolderDO::getLevel)
                .orderByAsc(ImageFolderDO::getSortOrder)
                .orderByDesc(ImageFolderDO::getId));
    }

    default ImageFolderDO selectByPath(@Param("path") String path) {
        return selectOne(new LambdaQueryWrapperX<ImageFolderDO>()
                .eq(ImageFolderDO::getPath, path));
    }

    default List<ImageFolderDO> selectByPermissionType(@Param("permissionType") Integer permissionType, @Param("creator") String creator) {
        LambdaQueryWrapperX<ImageFolderDO> wrapper = new LambdaQueryWrapperX<ImageFolderDO>()
                .eq(ImageFolderDO::getPermissionType, permissionType);

        // 如果是隐私文件夹，只查询创建者自己的
        if (permissionType != null && permissionType == 2 && creator != null) {
            wrapper.eq(ImageFolderDO::getCreator, creator);
        }

        return selectList(wrapper.orderByAsc(ImageFolderDO::getLevel)
                .orderByAsc(ImageFolderDO::getSortOrder)
                .orderByDesc(ImageFolderDO::getId));
    }

    default long selectCountByParentId(@Param("parentId") Long parentId) {
        return selectCount(new LambdaQueryWrapperX<ImageFolderDO>()
                .eq(ImageFolderDO::getParentId, parentId));
    }

}