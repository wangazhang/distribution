package com.hissp.distribution.module.infra.dal.mysql.image;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.infra.controller.admin.image.vo.resource.ImageResourcePageReqVO;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageResourceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图片资源 Mapper
 *
 * @author hissp
 */
@Mapper
public interface ImageResourceMapper extends BaseMapperX<ImageResourceDO> {

    default PageResult<ImageResourceDO> selectPage(ImageResourcePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImageResourceDO>()
                .likeIfPresent(ImageResourceDO::getName, reqVO.getName())
                .likeIfPresent(ImageResourceDO::getOriginalName, reqVO.getOriginalName())
                .eqIfPresent(ImageResourceDO::getFolderId, reqVO.getFolderId())
                .eqIfPresent(ImageResourceDO::getFormat, reqVO.getFormat())
                .likeIfPresent(ImageResourceDO::getTags, reqVO.getTags())
                .eqIfPresent(ImageResourceDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(ImageResourceDO::getFileSize, reqVO.getFileSize())
                .betweenIfPresent(ImageResourceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImageResourceDO::getCreateTime));
    }

    default List<ImageResourceDO> selectListByFolderId(@Param("folderId") Long folderId) {
        return selectList(new LambdaQueryWrapperX<ImageResourceDO>()
                .eqIfPresent(ImageResourceDO::getFolderId, folderId)
                .orderByDesc(ImageResourceDO::getCreateTime));
    }

    default List<ImageResourceDO> selectListForSelect(@Param("folderId") Long folderId,
                                                    @Param("keyword") String keyword,
                                                    @Param("creator") String creator) {
        LambdaQueryWrapperX<ImageResourceDO> wrapper = new LambdaQueryWrapperX<ImageResourceDO>()
                .eqIfPresent(ImageResourceDO::getFolderId, folderId)
                .likeIfPresent(ImageResourceDO::getName, keyword)
                .likeIfPresent(ImageResourceDO::getTags, keyword);

        // 如果指定了创建者，只能看到自己的图片（隐私文件夹）
        if (creator != null) {
            wrapper.and(w -> w.eq(ImageResourceDO::getCreator, creator)
                    .or().apply("folder_id IN (SELECT id FROM infra_image_folder WHERE permission_type = 1)"));
        }

        return selectList(wrapper.orderByDesc(ImageResourceDO::getCreateTime));
    }

    default long selectCountByFolderId(@Param("folderId") Long folderId) {
        return selectCount(new LambdaQueryWrapperX<ImageResourceDO>()
                .eq(ImageResourceDO::getFolderId, folderId));
    }

    default void incrementViewCount(@Param("id") Long id) {
        // 这里可以写原生的SQL来更新查看次数
        // 暂时用简单的更新方式
        ImageResourceDO resource = selectById(id);
        if (resource != null) {
            resource.setViewCount((resource.getViewCount() == null ? 0 : resource.getViewCount()) + 1);
            updateById(resource);
        }
    }

    default void incrementDownloadCount(@Param("id") Long id) {
        ImageResourceDO resource = selectById(id);
        if (resource != null) {
            resource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
            updateById(resource);
        }
    }

}