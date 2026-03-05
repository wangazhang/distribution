package com.hissp.distribution.module.infra.service.image;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.infra.controller.admin.image.vo.resource.*;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageResourceDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 图片资源 Service 接口
 *
 * @author hissp
 */
public interface ImageResourceService {

    /**
     * 上传图片
     *
     * @param uploadReqVO 上传信息
     * @return 图片资源ID
     */
    Long uploadImage(@Valid ImageResourceUploadReqVO uploadReqVO);

    /**
     * 创建图片资源记录（用于已上传的文件）
     *
     * @param createReqVO 创建信息
     * @return 图片资源ID
     */
    Long createImageResource(@Valid ImageResourceCreateReqVO createReqVO);

    /**
     * 更新图片资源
     *
     * @param updateReqVO 更新信息
     */
    void updateImageResource(@Valid ImageResourceUpdateReqVO updateReqVO);

    /**
     * 删除图片资源
     *
     * @param id 编号
     */
    void deleteImageResource(Long id);

    /**
     * 获得图片资源
     *
     * @param id 编号
     * @return 图片资源
     */
    ImageResourceDO getImageResource(Long id);

    /**
     * 获得图片资源分页
     *
     * @param pageReqVO 分页查询
     * @return 图片资源分页
     */
    PageResult<ImageResourceDO> getImageResourcePage(ImageResourcePageReqVO pageReqVO);

    /**
     * 获得图片选择列表
     *
     * @param folderId 文件夹ID
     * @param keyword 关键词
     * @param creator 创建者
     * @return 图片选择列表
     */
    List<ImageResourceRespVO> getImageSelectList(Long folderId, String keyword, String creator);

    /**
     * 校验图片资源是否存在
     *
     * @param id 编号
     */
    void validateImageResourceExists(Long id);

    /**
     * 批量移动图片到指定文件夹
     *
     * @param ids 图片资源ID列表
     * @param targetFolderId 目标文件夹ID
     */
    void batchMoveImageResource(List<Long> ids, Long targetFolderId);

    /**
     * 增加图片查看次数
     *
     * @param id 图片资源ID
     */
    void incrementViewCount(Long id);

    /**
     * 增加图片下载次数
     *
     * @param id 图片资源ID
     */
    void incrementDownloadCount(Long id);

    /**
     * 根据文件夹ID获取图片列表
     *
     * @param folderId 文件夹ID
     * @return 图片列表
     */
    List<ImageResourceDO> getImageResourceListByFolderId(Long folderId);

}