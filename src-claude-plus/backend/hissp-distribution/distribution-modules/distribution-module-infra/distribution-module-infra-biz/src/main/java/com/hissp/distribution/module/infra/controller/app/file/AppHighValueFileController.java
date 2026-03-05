package com.hissp.distribution.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.infra.controller.app.file.vo.AppHighValueFileUploadReqVO;
import com.hissp.distribution.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 高价值图片存储")
@RestController
@RequestMapping("/infra/high-value-pic")
@Validated
@Slf4j
public class AppHighValueFileController {

    @Resource
    private FileService fileService;

    @PostMapping("/card/upload")
    @Operation(summary = "上传身份证图片", description = "上传身份证正面或反面图片到指定路径")
    public CommonResult<String> uploadIdCard(@ModelAttribute @Valid AppHighValueFileUploadReqVO uploadReqVO) throws Exception {
        // 获取当前用户ID
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }

        // 构建高价值图片路径: /high-value-pic/card/{memberId}/
        String path = String.format("/high-value-pic/card/%d/", userId);

        // 如果指定了子路径，则追加
        if (uploadReqVO.getSubPath() != null && !uploadReqVO.getSubPath().trim().isEmpty()) {
            path += uploadReqVO.getSubPath().trim() + "/";
        }

        MultipartFile file = uploadReqVO.getFile();
        if (file == null) {
            return CommonResult.error(400, "上传文件不能为空");
        }

        // 检查文件类型（只能上传图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return CommonResult.error(400, "只能上传图片文件");
        }

        // 检查文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return CommonResult.error(400, "图片文件大小不能超过5MB");
        }

        try {
            // 直接使用FileService，让它生成完整的路径
            // 我们只需要传递目录前缀
            String directoryPrefix = String.format("high-value-pic/card/%d/%s", userId,
                uploadReqVO.getSubPath() != null ? uploadReqVO.getSubPath().trim() : "");
            String fileUrl = fileService.createFile(file.getOriginalFilename(), directoryPrefix, IoUtil.readBytes(file.getInputStream()));
            log.info("用户ID: {}, 上传身份证图片成功: {}", userId, fileUrl);
            return success(fileUrl);
        } catch (Exception e) {
            log.error("用户ID: {}, 上传身份证图片失败: {}", userId, e.getMessage(), e);
            return CommonResult.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/member/upload")
    @Operation(summary = "上传会员资料图片", description = "上传会员相关图片到指定路径")
    public CommonResult<String> uploadMemberImage(@ModelAttribute @Valid AppHighValueFileUploadReqVO uploadReqVO) throws Exception {
        // 获取当前用户ID
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }

        // 构建会员资料图片路径: /high-value-pic/member/{memberId}/
        String path = String.format("/high-value-pic/member/%d/", userId);

        // 如果指定了子路径，则追加
        if (uploadReqVO.getSubPath() != null && !uploadReqVO.getSubPath().trim().isEmpty()) {
            path += uploadReqVO.getSubPath().trim() + "/";
        }

        MultipartFile file = uploadReqVO.getFile();
        if (file == null) {
            return CommonResult.error(400, "上传文件不能为空");
        }

        // 检查文件类型（只能上传图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return CommonResult.error(400, "只能上传图片文件");
        }

        // 检查文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return CommonResult.error(400, "图片文件大小不能超过5MB");
        }

        try {
            // 直接使用FileService，让它生成完整的路径
            // 我们只需要传递目录前缀
            String directoryPrefix = String.format("high-value-pic/member/%d/%s", userId,
                uploadReqVO.getSubPath() != null ? uploadReqVO.getSubPath().trim() : "");
            String fileUrl = fileService.createFile(file.getOriginalFilename(), directoryPrefix, IoUtil.readBytes(file.getInputStream()));
            log.info("用户ID: {}, 上传会员资料图片成功: {}", userId, fileUrl);
            return success(fileUrl);
        } catch (Exception e) {
            log.error("用户ID: {}, 上传会员资料图片失败: {}", userId, e.getMessage(), e);
            return CommonResult.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    }