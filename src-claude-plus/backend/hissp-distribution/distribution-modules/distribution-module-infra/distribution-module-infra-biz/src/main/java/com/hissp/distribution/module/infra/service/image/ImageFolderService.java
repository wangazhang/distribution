package com.hissp.distribution.module.infra.service.image;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.infra.controller.admin.image.vo.folder.*;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageFolderDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 图片文件夹 Service 接口
 *
 * @author hissp
 */
public interface ImageFolderService {

    /**
     * 创建图片文件夹
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createImageFolder(@Valid ImageFolderCreateReqVO createReqVO);

    /**
     * 更新图片文件夹
     *
     * @param updateReqVO 更新信息
     */
    void updateImageFolder(@Valid ImageFolderUpdateReqVO updateReqVO);

    /**
     * 删除图片文件夹
     *
     * @param id 编号
     */
    void deleteImageFolder(Long id);

    /**
     * 获得图片文件夹
     *
     * @param id 编号
     * @return 图片文件夹
     */
    ImageFolderDO getImageFolder(Long id);

    /**
     * 获得图片文件夹分页
     *
     * @param pageReqVO 分页查询
     * @return 图片文件夹分页
     */
    PageResult<ImageFolderDO> getImageFolderPage(ImageFolderPageReqVO pageReqVO);

    /**
     * 获得图片文件夹树
     *
     * @param permissionType 权限类型
     * @param creator 创建者
     * @return 图片文件夹树
     */
    List<ImageFolderRespVO> getImageFolderTree(Integer permissionType, String creator);

    /**
     * 校验图片文件夹是否存在
     *
     * @param id 编号
     */
    void validateImageFolderExists(Long id);

    /**
     * 移动文件夹
     *
     * @param id 文件夹ID
     * @param targetParentId 目标父文件夹ID
     */
    void moveImageFolder(Long id, Long targetParentId);

    /**
     * 获得子文件夹列表
     *
     * @param parentId 父文件夹ID
     * @return 子文件夹列表
     */
    List<ImageFolderDO> getChildrenFolderList(Long parentId);

}