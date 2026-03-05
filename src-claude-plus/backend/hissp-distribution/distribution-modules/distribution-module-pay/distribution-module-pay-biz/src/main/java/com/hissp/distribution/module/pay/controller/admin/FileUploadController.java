//package com.hissp.distribution.module.pay.controller.admin;
//
//import com.hissp.distribution.framework.common.pojo.CommonResult;
//import com.hissp.distribution.module.infra.controller.admin.file.vo.file.FileUploadReqVO;
//import com.hissp.distribution.module.infra.service.file.FileService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
//
//@Tag(name = "管理后台 - 文件上传(支付模块)")
//@RestController
//@RequestMapping("/admin-api/pay")
//@Validated
//@Slf4j
//public class FileUploadController {
//
//    @Resource
//    private FileService fileService;
//
//    @PostMapping("/ftp/proxy/upload")
//    @Operation(summary = "FTP代理上传文件", description = "用于小程序端文件上传，兼容首信易支付需求")
//    public CommonResult<String> uploadFile(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "category", required = false) String category
//    ) throws IOException {
//        log.info("开始上传文件: fileName={}, size={}, category={}",
//                file.getOriginalFilename(), file.getSize(), category);
//
//        try {
//            // 构建文件路径，使用category作为子目录
//            String path = category != null ? category : "payease";
//
//            // 上传文件
//            String fileUrl = fileService.createFile(file.getOriginalFilename(), path, file.getBytes());
//
//            log.info("文件上传成功: {}", fileUrl);
//            return success(fileUrl);
//
//        } catch (Exception e) {
//            log.error("文件上传失败: {}", e.getMessage(), e);
//            return CommonResult.error(500, "文件上传失败: " + e.getMessage());
//        }
//    }
//
//    @PostMapping("/file/upload")
//    @Operation(summary = "通用文件上传", description = "用于支付模块通用文件上传")
//    public CommonResult<String> uploadPayFile(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "path", required = false) String path
//    ) throws IOException {
//        log.info("开始上传支付模块文件: fileName={}, size={}, path={}",
//                file.getOriginalFilename(), file.getSize(), path);
//
//        try {
//            // 如果没有指定路径，使用默认路径
//            String filePath = path != null ? path : "pay";
//
//            // 上传文件
//            String fileUrl = fileService.createFile(file.getOriginalFilename(), filePath, file.getBytes());
//
//            log.info("文件上传成功: {}", fileUrl);
//            return success(fileUrl);
//
//        } catch (Exception e) {
//            log.error("文件上传失败: {}", e.getMessage(), e);
//            return CommonResult.error(500, "文件上传失败: " + e.getMessage());
//        }
//    }
//}