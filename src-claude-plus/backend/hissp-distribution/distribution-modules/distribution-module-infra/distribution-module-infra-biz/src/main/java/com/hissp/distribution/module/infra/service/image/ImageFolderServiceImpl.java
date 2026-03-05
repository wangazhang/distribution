package com.hissp.distribution.module.infra.service.image;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.infra.controller.admin.image.vo.folder.*;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageFolderDO;
import com.hissp.distribution.module.infra.dal.mysql.image.ImageFolderMapper;
import com.hissp.distribution.module.infra.dal.mysql.image.ImageResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.*;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.infra.enums.ErrorCodeConstants.*;

/**
 * 图片文件夹 Service 实现类
 *
 * @author hissp
 */
@Service
@Validated
@Slf4j
public class ImageFolderServiceImpl implements ImageFolderService {

    @Resource
    private ImageFolderMapper imageFolderMapper;

    @Resource
    private ImageResourceMapper imageResourceMapper;

    @Override
    @Transactional
    public Long createImageFolder(@Valid ImageFolderCreateReqVO createReqVO) {
        // 校验父文件夹存在
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            validateImageFolderExists(createReqVO.getParentId());
        }

        // 构建路径
        String path = buildPath(createReqVO.getParentId(), createReqVO.getName());

        // 校验路径唯一性
        validatePathUnique(path, null);

        // 插入
        ImageFolderDO folder = BeanUtils.toBean(createReqVO, ImageFolderDO.class);
        folder.setLevel(calculateLevel(createReqVO.getParentId()));
        folder.setPath(path);
        if (folder.getSortOrder() == null) {
            folder.setSortOrder(0);
        }
        imageFolderMapper.insert(folder);

        return folder.getId();
    }

    @Override
    @Transactional
    public void updateImageFolder(@Valid ImageFolderUpdateReqVO updateReqVO) {
        // 校验存在
        validateImageFolderExists(updateReqVO.getId());

        // 校验父文件夹存在（不能设置自己为父文件夹）
        if (updateReqVO.getParentId() != null && updateReqVO.getParentId() > 0) {
            if (updateReqVO.getParentId().equals(updateReqVO.getId())) {
                throw exception(IMAGE_FOLDER_PARENT_CANNOT_BE_SELF);
            }
            validateImageFolderExists(updateReqVO.getParentId());

            // 校验不能将自己的子文件夹设置为父文件夹
            validateNotChildFolder(updateReqVO.getId(), updateReqVO.getParentId());
        }

        // 构建新路径
        String newPath = buildPath(updateReqVO.getParentId(), updateReqVO.getName());

        // 校验路径唯一性
        validatePathUnique(newPath, updateReqVO.getId());

        // 更新
        ImageFolderDO updateObj = BeanUtils.toBean(updateReqVO, ImageFolderDO.class);
        updateObj.setLevel(calculateLevel(updateReqVO.getParentId()));
        updateObj.setPath(newPath);
        imageFolderMapper.updateById(updateObj);

        // 更新所有子文件夹的路径和层级
        updateChildrenPathAndLevel(updateReqVO.getId(), newPath, updateObj.getLevel());
    }

    @Override
    @Transactional
    public void deleteImageFolder(Long id) {
        // 校验存在
        validateImageFolderExists(id);

        // 校验是否有子文件夹
        long childrenCount = imageFolderMapper.selectCountByParentId(id);
        if (childrenCount > 0) {
            throw exception(IMAGE_FOLDER_HAS_CHILDREN);
        }

        // 校验是否有图片
        long imageCount = imageResourceMapper.selectCountByFolderId(id);
        if (imageCount > 0) {
            throw exception(IMAGE_FOLDER_HAS_IMAGES);
        }

        // 删除
        imageFolderMapper.deleteById(id);
    }

    private ImageFolderDO getImageFolderDO(Long id) {
        return imageFolderMapper.selectById(id);
    }

    @Override
    public ImageFolderDO getImageFolder(Long id) {
        return getImageFolderDO(id);
    }

    @Override
    public PageResult<ImageFolderDO> getImageFolderPage(ImageFolderPageReqVO pageReqVO) {
        return imageFolderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ImageFolderRespVO> getImageFolderTree(Integer permissionType, String creator) {
        // 查询所有可见的文件夹
        List<ImageFolderDO> folders;
        if (permissionType != null) {
            folders = imageFolderMapper.selectByPermissionType(permissionType, creator);
        } else {
            folders = imageFolderMapper.selectTree();
        }

        // 统计每个文件夹的图片数量和子文件夹数量
        Map<Long, Integer> imageCountMap = new HashMap<>();
        Map<Long, Integer> childrenCountMap = new HashMap<>();

        for (ImageFolderDO folder : folders) {
            // 统计图片数量
            long imageCount = imageResourceMapper.selectCountByFolderId(folder.getId());
            imageCountMap.put(folder.getId(), (int) imageCount);

            // 统计子文件夹数量
            long childrenCount = imageFolderMapper.selectCountByParentId(folder.getId());
            childrenCountMap.put(folder.getId(), (int) childrenCount);
        }

        // 转换为响应VO并构建树形结构
        List<ImageFolderRespVO> respVOs = BeanUtils.toBean(folders, ImageFolderRespVO.class);

        // 设置统计数据
        for (ImageFolderRespVO respVO : respVOs) {
            respVO.setImageCount(imageCountMap.get(respVO.getId()));
            respVO.setChildrenCount(childrenCountMap.get(respVO.getId()));
            respVO.setPermissionTypeName(getPermissionTypeName(respVO.getPermissionType()));
        }

        // 构建树形结构
        return buildTree(respVOs, 0L);
    }

    @Override
    public void validateImageFolderExists(Long id) {
        if (getImageFolderDO(id) == null) {
            throw exception(IMAGE_FOLDER_NOT_EXISTS);
        }
    }

    @Override
    @Transactional
    public void moveImageFolder(Long id, Long targetParentId) {
        // 校验存在
        validateImageFolderExists(id);

        if (targetParentId != null && targetParentId > 0) {
            validateImageFolderExists(targetParentId);
            validateNotChildFolder(id, targetParentId);
        }

        ImageFolderDO folder = getImageFolderDO(id);
        String newPath = buildPath(targetParentId, folder.getName());

        validatePathUnique(newPath, id);

        // 更新父文件夹和路径
        folder.setParentId(targetParentId);
        folder.setPath(newPath);
        folder.setLevel(calculateLevel(targetParentId));
        imageFolderMapper.updateById(folder);

        // 更新子文件夹
        updateChildrenPathAndLevel(id, newPath, folder.getLevel());
    }

    @Override
    public List<ImageFolderDO> getChildrenFolderList(Long parentId) {
        return imageFolderMapper.selectListByParentId(parentId);
    }

    // ========== 私有方法 ==========

    /**
     * 构建文件夹路径
     */
    private String buildPath(Long parentId, String name) {
        if (parentId == null || parentId == 0) {
            return "/" + name;
        }

        ImageFolderDO parentFolder = getImageFolderDO(parentId);
        if (parentFolder == null) {
            return "/" + name;
        }

        return parentFolder.getPath().equals("/") ?
                "/" + name : parentFolder.getPath() + "/" + name;
    }

    /**
     * 计算文件夹层级
     */
    private Integer calculateLevel(Long parentId) {
        if (parentId == null || parentId == 0) {
            return 1;
        }

        ImageFolderDO parentFolder = getImageFolderDO(parentId);
        if (parentFolder == null) {
            return 1;
        }

        return parentFolder.getLevel() + 1;
    }

    /**
     * 校验路径唯一性
     */
    private void validatePathUnique(String path, Long excludeId) {
        ImageFolderDO existingFolder = imageFolderMapper.selectByPath(path);
        if (existingFolder != null && !existingFolder.getId().equals(excludeId)) {
            throw exception(IMAGE_FOLDER_PATH_DUPLICATE);
        }
    }

    /**
     * 校验不是子文件夹（防止循环引用）
     */
    private void validateNotChildFolder(Long folderId, Long targetParentId) {
        List<ImageFolderDO> children = getChildrenFolderList(folderId);
        for (ImageFolderDO child : children) {
            if (child.getId().equals(targetParentId)) {
                throw exception(IMAGE_FOLDER_PARENT_CANNOT_BE_CHILD);
            }
            validateNotChildFolder(child.getId(), targetParentId);
        }
    }

    /**
     * 更新子文件夹的路径和层级
     */
    private void updateChildrenPathAndLevel(Long parentId, String parentPath, Integer parentLevel) {
        List<ImageFolderDO> children = getChildrenFolderList(parentId);
        for (ImageFolderDO child : children) {
            String oldPath = child.getPath();
            String newPath = parentPath.equals("/") ?
                    "/" + child.getName() : parentPath + "/" + child.getName();

            child.setPath(newPath);
            child.setLevel(parentLevel + 1);
            imageFolderMapper.updateById(child);

            // 递归更新子文件夹
            updateChildrenPathAndLevel(child.getId(), newPath, child.getLevel());
        }
    }

    /**
     * 构建树形结构
     */
    private List<ImageFolderRespVO> buildTree(List<ImageFolderRespVO> allFolders, Long parentId) {
        List<ImageFolderRespVO> result = new ArrayList<>();

        for (ImageFolderRespVO folder : allFolders) {
            if ((parentId == 0 && folder.getParentId() == null) ||
                (folder.getParentId() != null && folder.getParentId().equals(parentId))) {
                List<ImageFolderRespVO> children = buildTree(allFolders, folder.getId());
                folder.setChildren(children.isEmpty() ? null : children);
                result.add(folder);
            }
        }

        return result;
    }

    /**
     * 获取权限类型名称
     */
    private String getPermissionTypeName(Integer permissionType) {
        if (permissionType == null) {
            return "";
        }
        switch (permissionType) {
            case 1:
                return "共享";
            case 2:
                return "隐私";
            default:
                return "";
        }
    }

}