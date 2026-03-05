package com.hissp.distribution.module.infra.controller.admin.image;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.infra.controller.admin.image.vo.resource.*;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageFolderDO;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageResourceDO;
import com.hissp.distribution.module.infra.service.image.ImageFolderService;
import com.hissp.distribution.module.infra.service.image.ImageResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 图片资源")
@RestController
@RequestMapping("/infra/image-resource")
@Validated
public class ImageResourceController {

    @Resource
    private ImageResourceService imageResourceService;

    @Resource
    private ImageFolderService imageFolderService;

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:upload')")
    public CommonResult<Long> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "folderId", required = false) Long folderId,
                                         @RequestParam(value = "tags", required = false) String tags) {
        ImageResourceUploadReqVO uploadReqVO = new ImageResourceUploadReqVO();
        uploadReqVO.setFile(file);
        uploadReqVO.setName(name);
        uploadReqVO.setFolderId(folderId);
        uploadReqVO.setTags(tags);
        return success(imageResourceService.uploadImage(uploadReqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建图片资源记录（用于已上传的文件）")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:upload')")
    public CommonResult<Long> createImageResource(@Valid @RequestBody ImageResourceCreateReqVO createReqVO) {
        return success(imageResourceService.createImageResource(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新图片资源")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:update')")
    public CommonResult<Boolean> updateImageResource(@Valid @RequestBody ImageResourceUpdateReqVO updateReqVO) {
        imageResourceService.updateImageResource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除图片资源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:image-resource:delete')")
    public CommonResult<Boolean> deleteImageResource(@RequestParam("id") Long id) {
        imageResourceService.deleteImageResource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得图片资源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:query')")
    public CommonResult<ImageResourceRespVO> getImageResource(@RequestParam("id") Long id) {
        ImageResourceDO imageResource = imageResourceService.getImageResource(id);
        return success(BeanUtils.toBean(imageResource, ImageResourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得图片资源分页")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:query')")
    public CommonResult<PageResult<ImageResourceRespVO>> getImageResourcePage(@Valid ImageResourcePageReqVO pageReqVO) {
        PageResult<ImageResourceDO> pageResult = imageResourceService.getImageResourcePage(pageReqVO);

        // 手动处理响应数据，确保folderName和fileSizeDisplay不为null
        PageResult<ImageResourceRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());

        if (pageResult.getList() != null) {
            List<ImageResourceRespVO> respList = pageResult.getList().stream().map(resource -> {
                ImageResourceRespVO respVO = BeanUtils.toBean(resource, ImageResourceRespVO.class);

                // 设置文件大小友好显示
                if (resource.getFileSize() != null && respVO.getFileSizeDisplay() == null) {
                    respVO.setFileSizeDisplay(formatFileSize(resource.getFileSize()));
                }

                // 设置文件夹名称
                if (resource.getFolderId() != null && resource.getFolderId() > 0 && respVO.getFolderName() == null) {
                    ImageFolderDO folder = imageFolderService.getImageFolder(resource.getFolderId());
                    if (folder != null) {
                        respVO.setFolderName(folder.getName());
                    } else {
                        respVO.setFolderName("未知文件夹");
                    }
                } else if (resource.getFolderId() == null || resource.getFolderId() == 0) {
                    respVO.setFolderName("根目录");
                }

                return respVO;
            }).collect(Collectors.toList());

            result.setList(respList);
        } else {
            result.setList(List.of());
        }

        return success(result);
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

    @GetMapping("/select-list")
    @Operation(summary = "获得图片选择列表")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:query')")
    public CommonResult<List<ImageResourceRespVO>> getImageSelectList(
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        String currentUserId = SecurityFrameworkUtils.getLoginUserId().toString();
        List<ImageResourceRespVO> list = imageResourceService.getImageSelectList(folderId, keyword, currentUserId);
        return success(list);
    }

    @PostMapping("/batch-move")
    @Operation(summary = "批量移动图片到指定文件夹")
    @PreAuthorize("@ss.hasPermission('infra:image-resource:update')")
    public CommonResult<Boolean> batchMoveImageResource(@RequestBody ImageResourceBatchMoveReqVO batchMoveReqVO) {
        imageResourceService.batchMoveImageResource(batchMoveReqVO.getIds(), batchMoveReqVO.getTargetFolderId());
        return success(true);
    }

    @PostMapping("/increment-view-count")
    @Operation(summary = "增加图片查看次数")
    @Parameter(name = "id", description = "图片资源ID", required = true)
    public CommonResult<Boolean> incrementViewCount(@RequestParam("id") Long id) {
        imageResourceService.incrementViewCount(id);
        return success(true);
    }

    @PostMapping("/increment-download-count")
    @Operation(summary = "增加图片下载次数")
    @Parameter(name = "id", description = "图片资源ID", required = true)
    public CommonResult<Boolean> incrementDownloadCount(@RequestParam("id") Long id) {
        imageResourceService.incrementDownloadCount(id);
        return success(true);
    }

    @GetMapping("/list-by-folder")
    @Operation(summary = "根据文件夹ID获取图片列表")
    @Parameter(name = "folderId", description = "文件夹ID", required = true)
    @PreAuthorize("@ss.hasPermission('infra:image-resource:query')")
    public CommonResult<List<ImageResourceRespVO>> getImageResourceListByFolderId(@RequestParam("folderId") Long folderId) {
        List<ImageResourceDO> list = imageResourceService.getImageResourceListByFolderId(folderId);
        return success(BeanUtils.toBean(list, ImageResourceRespVO.class));
    }

}