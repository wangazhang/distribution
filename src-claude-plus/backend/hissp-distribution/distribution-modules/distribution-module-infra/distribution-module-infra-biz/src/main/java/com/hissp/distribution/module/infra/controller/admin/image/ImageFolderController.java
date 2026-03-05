package com.hissp.distribution.module.infra.controller.admin.image;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.infra.controller.admin.image.vo.folder.*;
import com.hissp.distribution.module.infra.dal.dataobject.image.ImageFolderDO;
import com.hissp.distribution.module.infra.service.image.ImageFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 图片文件夹")
@RestController
@RequestMapping("/infra/image-folder")
@Validated
public class ImageFolderController {

    @Resource
    private ImageFolderService imageFolderService;

    @PostMapping("/create")
    @Operation(summary = "创建图片文件夹")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:create')")
    public CommonResult<Long> createImageFolder(@Valid @RequestBody ImageFolderCreateReqVO createReqVO) {
        return success(imageFolderService.createImageFolder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新图片文件夹")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:update')")
    public CommonResult<Boolean> updateImageFolder(@Valid @RequestBody ImageFolderUpdateReqVO updateReqVO) {
        imageFolderService.updateImageFolder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除图片文件夹")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:image-folder:delete')")
    public CommonResult<Boolean> deleteImageFolder(@RequestParam("id") Long id) {
        imageFolderService.deleteImageFolder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得图片文件夹")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:query')")
    public CommonResult<ImageFolderRespVO> getImageFolder(@RequestParam("id") Long id) {
        ImageFolderDO imageFolder = imageFolderService.getImageFolder(id);
        return success(BeanUtils.toBean(imageFolder, ImageFolderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得图片文件夹分页")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:query')")
    public CommonResult<PageResult<ImageFolderRespVO>> getImageFolderPage(@Valid ImageFolderPageReqVO pageReqVO) {
        PageResult<ImageFolderDO> pageResult = imageFolderService.getImageFolderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImageFolderRespVO.class));
    }

    @GetMapping("/tree")
    @Operation(summary = "获得图片文件夹树")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:query')")
    public CommonResult<List<ImageFolderRespVO>> getImageFolderTree(
            @RequestParam(value = "permissionType", required = false) Integer permissionType) {
        String currentUserId = SecurityFrameworkUtils.getLoginUserId().toString();
        List<ImageFolderRespVO> tree = imageFolderService.getImageFolderTree(permissionType, currentUserId);
        return success(tree);
    }

    @PostMapping("/move")
    @Operation(summary = "移动文件夹")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:update')")
    public CommonResult<Boolean> moveImageFolder(@RequestBody ImageFolderMoveReqVO moveReqVO) {
        imageFolderService.moveImageFolder(moveReqVO.getId(), moveReqVO.getTargetParentId());
        return success(true);
    }

    @GetMapping("/children")
    @Operation(summary = "获得子文件夹列表")
    @Parameter(name = "parentId", description = "父文件夹ID")
    @PreAuthorize("@ss.hasPermission('infra:image-folder:query')")
    public CommonResult<List<ImageFolderRespVO>> getChildrenFolderList(
            @RequestParam(value = "parentId", required = false) Long parentId) {
        List<ImageFolderDO> children = imageFolderService.getChildrenFolderList(parentId);
        return success(BeanUtils.toBean(children, ImageFolderRespVO.class));
    }

}