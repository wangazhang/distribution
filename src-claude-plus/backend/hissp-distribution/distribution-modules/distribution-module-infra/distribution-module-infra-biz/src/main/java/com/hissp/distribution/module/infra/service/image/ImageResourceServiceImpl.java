package com.hissp.distribution.module.infra.service.image;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.infra.controller.admin.image.vo.resource.*;
import com.hissp.distribution.module.infra.dal.dataobject.file.FileDO;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageFolderDO;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageResourceDO;
import com.hissp.distribution.module.infra.dal.mysql.image.ImageFolderMapper;
import com.hissp.distribution.module.infra.dal.mysql.image.ImageResourceMapper;
import com.hissp.distribution.module.infra.service.file.FileService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.infra.enums.ErrorCodeConstants.*;

/**
 * 图片资源 Service 实现类
 *
 * @author hissp
 */
@Service
@Validated
@Slf4j
public class ImageResourceServiceImpl implements ImageResourceService {

    /**
     * 支持的图片格式
     */
    private static final List<String> SUPPORTED_IMAGE_FORMATS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    /**
     * 最大文件大小（10MB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    @Resource
    private ImageResourceMapper imageResourceMapper;

    @Resource
    private ImageFolderMapper imageFolderMapper;

    @Resource
    private ImageFolderService imageFolderService;

    @Resource
    private FileService fileService;

    @Override
    @Transactional
    public Long uploadImage(@Valid ImageResourceUploadReqVO uploadReqVO) {
        // 校验文件夹存在
        if (uploadReqVO.getFolderId() != null && uploadReqVO.getFolderId() > 0) {
            imageFolderService.validateImageFolderExists(uploadReqVO.getFolderId());
        }

        try {
            // 读取文件内容
            byte[] fileContent = IoUtil.readBytes(uploadReqVO.getFile().getInputStream());

            // 校验文件大小
            if (fileContent.length > MAX_FILE_SIZE) {
                throw exception(IMAGE_RESOURCE_SIZE_EXCEEDED);
            }

            // 校验文件格式
            String originalFilename = uploadReqVO.getFile().getOriginalFilename();
            if (originalFilename == null || !isSupportedImageFormat(originalFilename)) {
                throw exception(IMAGE_RESOURCE_FORMAT_NOT_SUPPORTED);
            }

            // 获取图片信息
            BufferedImage image = ImgUtil.read(new ByteArrayInputStream(fileContent));
            String format = getFileExtension(originalFilename);
            int width = image.getWidth();
            int height = image.getHeight();

            // 上传文件到文件系统
            String fileUrl = fileService.createFile(originalFilename, null, fileContent);

            // 根据路径获取文件记录
            FileDO fileDO = fileService.getFileByPath(fileUrl);

            // 创建图片资源记录
            ImageResourceDO imageResource = new ImageResourceDO();
            imageResource.setFileId(fileDO.getId());
            imageResource.setName(StrUtil.isBlank(uploadReqVO.getName()) ?
                    getFileNameWithoutExtension(originalFilename) : uploadReqVO.getName());
            imageResource.setOriginalName(originalFilename);
            imageResource.setFolderId(uploadReqVO.getFolderId() == null ? 0L : uploadReqVO.getFolderId());
            imageResource.setFileSize((long) fileContent.length);
            imageResource.setWidth(width);
            imageResource.setHeight(height);
            imageResource.setFormat(format);
            imageResource.setUrl(fileUrl);
            imageResource.setTags(uploadReqVO.getTags());
            imageResource.setDownloadCount(0);
            imageResource.setViewCount(0);

            imageResourceMapper.insert(imageResource);

            return imageResource.getId();

        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw exception(IMAGE_RESOURCE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public Long createImageResource(@Valid ImageResourceCreateReqVO createReqVO) {
        try {
            // 获取图片尺寸和格式信息
            BufferedImage image = ImageIO.read(new URL(createReqVO.getUrl()));
            Integer width = image != null ? image.getWidth() : 0;
            Integer height = image != null ? image.getHeight() : 0;

            // 从URL中提取文件格式
            String format = "jpg"; // 默认格式
            if (createReqVO.getFormat() != null) {
                format = createReqVO.getFormat().toLowerCase();
            } else if (createReqVO.getUrl().contains(".")) {
                String[] parts = createReqVO.getUrl().split("\\.");
                format = parts[parts.length - 1].toLowerCase();
            }

            // 根据URL查找文件记录，获取fileId
            FileDO file = fileService.getFileByPath(createReqVO.getUrl());
            if (file == null) {
                throw exception(FILE_NOT_EXISTS);
            }

            // 构建图片资源对象
            ImageResourceDO imageResource = BeanUtils.toBean(createReqVO, ImageResourceDO.class);
            imageResource.setWidth(width);
            imageResource.setHeight(height);
            imageResource.setFormat(format);
            imageResource.setFileId(file.getId());  // 设置文件ID

            // 设置文件大小（如果没有传入，使用文件记录中的大小）
            if (createReqVO.getFileSize() != null) {
                imageResource.setFileSize(createReqVO.getFileSize());
            } else {
                imageResource.setFileSize(Long.valueOf(file.getSize()));
            }

            // 设置原始文件名
            if (createReqVO.getOriginalName() == null) {
                imageResource.setOriginalName(createReqVO.getName());
            }

            // 设置初始统计
            imageResource.setDownloadCount(0);
            imageResource.setViewCount(0);

            imageResourceMapper.insert(imageResource);

            return imageResource.getId();

        } catch (Exception e) {
            log.error("创建图片资源失败", e);
            throw exception(IMAGE_RESOURCE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateImageResource(@Valid ImageResourceUpdateReqVO updateReqVO) {
        // 校验存在
        validateImageResourceExists(updateReqVO.getId());

        // 校验文件夹存在
        if (updateReqVO.getFolderId() != null && updateReqVO.getFolderId() > 0) {
            imageFolderService.validateImageFolderExists(updateReqVO.getFolderId());
        }

        // 更新
        ImageResourceDO updateObj = BeanUtils.toBean(updateReqVO, ImageResourceDO.class);
        if (updateObj.getFolderId() == null) {
            updateObj.setFolderId(0L);
        }
        imageResourceMapper.updateById(updateObj);
    }

    @Override
    @Transactional
    public void deleteImageResource(Long id) {
        // 校验存在
        validateImageResourceExists(id);

        ImageResourceDO imageResource = imageResourceMapper.selectById(id);

        // 删除文件记录
        if (imageResource.getFileId() != null) {
            try {
                fileService.deleteFile(imageResource.getFileId());
            } catch (Exception e) {
                log.warn("删除文件失败，文件ID: {}", imageResource.getFileId(), e);
            }
        }

        // 删除图片资源记录
        imageResourceMapper.deleteById(id);
    }

    @Override
    public ImageResourceDO getImageResource(Long id) {
        return imageResourceMapper.selectById(id);
    }

    @Override
    public PageResult<ImageResourceDO> getImageResourcePage(ImageResourcePageReqVO pageReqVO) {
        return imageResourceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ImageResourceRespVO> getImageSelectList(Long folderId, String keyword, String creator) {
        List<ImageResourceDO> imageResources = imageResourceMapper.selectListForSelect(folderId, keyword, creator);

        return imageResources.stream().map(resource -> {
            ImageResourceRespVO respVO = BeanUtils.toBean(resource, ImageResourceRespVO.class);

            // 设置文件大小友好显示
            respVO.setFileSizeDisplay(formatFileSize(resource.getFileSize()));

            // 设置文件夹名称
            if (resource.getFolderId() != null && resource.getFolderId() > 0) {
                ImageFolderDO folder = imageFolderMapper.selectById(resource.getFolderId());
                if (folder != null) {
                    respVO.setFolderName(folder.getName());
                }
            }

            return respVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void validateImageResourceExists(Long id) {
        if (getImageResource(id) == null) {
            throw exception(IMAGE_RESOURCE_NOT_EXISTS);
        }
    }

    @Override
    @Transactional
    public void batchMoveImageResource(List<Long> ids, Long targetFolderId) {
        // 校验目标文件夹存在
        if (targetFolderId != null && targetFolderId > 0) {
            imageFolderService.validateImageFolderExists(targetFolderId);
        }

        // 批量更新文件夹
        for (Long id : ids) {
            validateImageResourceExists(id);

            ImageResourceDO updateObj = new ImageResourceDO();
            updateObj.setId(id);
            updateObj.setFolderId(targetFolderId == null ? 0L : targetFolderId);
            imageResourceMapper.updateById(updateObj);
        }
    }

    @Override
    public void incrementViewCount(Long id) {
        imageResourceMapper.incrementViewCount(id);
    }

    @Override
    public void incrementDownloadCount(Long id) {
        imageResourceMapper.incrementDownloadCount(id);
    }

    @Override
    public List<ImageResourceDO> getImageResourceListByFolderId(Long folderId) {
        return imageResourceMapper.selectListByFolderId(folderId);
    }

    // ========== 私有方法 ==========

    /**
     * 判断是否为支持的图片格式
     */
    private boolean isSupportedImageFormat(String filename) {
        String extension = getFileExtension(filename);
        return SUPPORTED_IMAGE_FORMATS.contains(extension.toLowerCase());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    /**
     * 获取不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(Long fileSize) {
        if (fileSize == null) {
            return "0 B";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

}