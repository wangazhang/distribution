package com.hissp.distribution.module.material.service.inbound;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundApproveReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundPageReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundRespVO;

/**
 * 物料入库 Service 接口
 */
public interface MaterialInboundService {

    /**
     * 创建物料入库申请
     *
     * @param createReqVO 创建信息
     * @param operatorId 操作人ID
     * @return 入库单ID
     */
    Long createInbound(MaterialInboundCreateReqVO createReqVO, Long operatorId);

    /**
     * 审核物料入库申请
     *
     * @param approveReqVO 审核信息
     * @param operatorId 操作人ID
     */
    void approveInbound(MaterialInboundApproveReqVO approveReqVO, Long operatorId);

    /**
     * 完成物料入库
     *
     * @param id 入库单ID
     * @param operatorId 操作人ID
     */
    void completeInbound(Long id, Long operatorId);

    /**
     * 取消物料入库
     *
     * @param id 入库单ID
     * @param operatorId 操作人ID
     */
    void cancelInbound(Long id, Long operatorId);

    /**
     * 获得物料入库分页
     *
     * @param pageReqVO 分页查询
     * @return 物料入库分页
     */
    PageResult<MaterialInboundRespVO> getInboundPage(MaterialInboundPageReqVO pageReqVO);

    /**
     * 获得物料入库详情
     *
     * @param id 入库单ID
     * @return 入库单详情
     */
    MaterialInboundRespVO getInbound(Long id);

}