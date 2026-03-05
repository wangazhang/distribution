package com.hissp.distribution.module.material.service.outbound;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.MaterialOutboundCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.MaterialOutboundPageReqVO;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.MaterialOutboundRespVO;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.MaterialOutboundUpdateReqVO;
import com.hissp.distribution.module.material.controller.app.outbound.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 物料出库 Service 接口
 * 
 * 迁移自 MB 域的物料出库功能，统一到物料域
 */
public interface MaterialOutboundService {

    /**
     * 创建物料出库申请（管理后台）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutbound(@Valid MaterialOutboundCreateReqVO createReqVO);

    /**
     * 更新物料出库
     *
     * @param updateReqVO 更新信息
     */
    void updateOutbound(@Valid MaterialOutboundUpdateReqVO updateReqVO);

    /**
     * 删除物料出库
     *
     * @param id 编号
     */
    void deleteOutbound(Long id);

    /**
     * 获得物料出库
     *
     * @param id 编号
     * @return 物料出库
     */
    MaterialOutboundRespVO getOutbound(Long id);

    /**
     * 获得物料出库分页
     *
     * @param pageReqVO 分页查询
     * @return 物料出库分页
     */
    PageResult<MaterialOutboundRespVO> getOutboundPage(MaterialOutboundPageReqVO pageReqVO);

    /**
     * 审核物料出库申请
     *
     * @param id 出库单ID
     * @param approveUserId 审核人ID
     * @param approved 是否通过
     * @param reason 拒绝原因（审核不通过时必填）
     */
    void approveOutbound(Long id, Long approveUserId, Boolean approved, String reason);
    
    /**
     * 批量审批通过物料出库申请
     *
     * @param ids 出库单ID列表
     * @param approveUserId 审核人ID
     */
    void batchApproveOutbound(List<Long> ids, Long approveUserId);
    
    /**
     * 发货
     *
     * @param id 出库单ID
     * @param logisticsCompany 物流公司
     * @param logisticsCode 物流单号
     */
    void shipOutbound(Long id, String logisticsCompany, String logisticsCode);
    
    /**
     * 确认收货
     *
     * @param id 出库单ID
     */
    void completeOutbound(Long id);
    
    /**
     * 取消出库申请
     *
     * @param id 出库单ID
     * @param cancelReason 取消原因
     */
    void cancelOutbound(Long id, String cancelReason);
    
    // ========== APP端接口 ==========
    
    /**
     * 创建APP端物料出库申请
     *
     * @param createReqVO APP端物料出库创建请求
     * @return 出库单创建响应，包含ID和单号
     */
    AppMaterialOutboundCreateRespVO createAppOutbound(@Valid AppMaterialOutboundCreateReqVO createReqVO);
    
    /**
     * 获取APP端物料出库分页列表
     *
     * @param pageReqVO 分页请求
     * @return APP端物料出库分页结果
     */
    PageResult<AppMaterialOutboundRespVO> getAppOutboundPage(AppMaterialOutboundPageReqVO pageReqVO);
    
    /**
     * 获取APP端物料出库详情
     *
     * @param id 出库单ID
     * @param userId 用户ID，用于校验是否有权限查看
     * @return APP端物料出库详情
     */
    AppMaterialOutboundDetailRespVO getAppOutboundDetail(Long id, Long userId);
    
    /**
     * APP端确认收货
     *
     * @param id 出库单ID
     * @param userId 用户ID，用于校验是否有权限操作
     */
    void appCompleteOutbound(Long id, Long userId);
    
    /**
     * APP端取消出库申请
     *
     * @param id 出库单ID
     * @param cancelReason 取消原因
     * @param userId 用户ID，用于校验是否有权限操作
     */
    void appCancelOutbound(Long id, String cancelReason, Long userId);

    /**
     * 导出待发货物料出库单模板
     *
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    void exportShippingTemplate(HttpServletResponse response) throws IOException;

    /**
     * 批量导入物料出库发货信息
     *
     * @param file Excel文件
     * @return 导入结果（成功数、失败数、错误信息）
     * @throws IOException IO异常
     */
    Map<String, Object> importShipping(MultipartFile file) throws IOException;
}