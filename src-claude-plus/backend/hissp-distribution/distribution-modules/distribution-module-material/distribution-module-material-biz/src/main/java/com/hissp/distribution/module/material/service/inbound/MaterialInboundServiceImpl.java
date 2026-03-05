package com.hissp.distribution.module.material.service.inbound;

import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundApproveReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundPageReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundRespVO;
import com.hissp.distribution.module.material.convert.MaterialInboundConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialInboundDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialInboundMapper;
import com.hissp.distribution.module.material.enums.ErrorCodeConstants;
import com.hissp.distribution.module.material.service.balance.MaterialBalanceService;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import com.hissp.distribution.module.material.service.txn.MaterialTxnService;
import com.hissp.distribution.module.system.api.user.AdminUserApi;
import com.hissp.distribution.module.system.api.user.dto.AdminUserRespDTO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物料入库 Service 实现类
 */
@Service
@Validated
public class MaterialInboundServiceImpl implements MaterialInboundService {

    @Resource
    private MaterialInboundMapper inboundMapper;
    @Resource
    private MaterialDefinitionService definitionService;
    @Resource
    private MaterialBalanceService balanceService;
    @Resource
    private MaterialTxnService txnService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(MaterialInboundCreateReqVO createReqVO, Long operatorId) {
        // 1. 验证物料定义是否存在
        MaterialDefinitionDO definition = definitionService.getDefinition(createReqVO.getMaterialId());
        if (definition == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 2. 获取操作人信息
        AdminUserRespDTO operator = adminUserApi.getUser(operatorId);
        if (operator == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 3. 创建入库单
        MaterialInboundDO inbound = MaterialInboundConvert.INSTANCE.convert(createReqVO);
        inbound.setInboundNo(generateInboundNo());
        inbound.setApplicantId(operatorId);
        inbound.setApplicantName(operator.getNickname());
        inbound.setMaterialName(definition.getName());
        inbound.setTotalAmount(createReqVO.getQuantity() * createReqVO.getUnitPrice());
        inbound.setStatus(0); // 待审核
        
        inboundMapper.insert(inbound);
        return inbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveInbound(MaterialInboundApproveReqVO approveReqVO, Long operatorId) {
        // 1. 获取入库单
        MaterialInboundDO inbound = inboundMapper.selectById(approveReqVO.getId());
        if (inbound == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 2. 验证状态
        if (inbound.getStatus() != 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 3. 获取审核人信息
        AdminUserRespDTO approver = adminUserApi.getUser(operatorId);
        if (approver == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 4. 更新审核信息
        inbound.setStatus(approveReqVO.getApproved() ? 1 : 2); // 1-已审核 2-已拒绝
        inbound.setApproveUserId(operatorId);
        inbound.setApproveUserName(approver.getNickname());
        inbound.setApproveReason(approveReqVO.getApproveReason());
        inbound.setApproveTime(LocalDateTime.now());
        
        inboundMapper.updateById(inbound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeInbound(Long id, Long operatorId) {
        // 1. 获取入库单
        MaterialInboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 2. 验证状态
        if (inbound.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 3. 增加用户余额
        MaterialActDTO act = MaterialActDTO.builder()
                .userId(inbound.getApplicantId())
                .materialId(inbound.getMaterialId())
                .quantity(inbound.getQuantity())
                .direction(com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum.IN)
                .bizKey("INBOUND_" + inbound.getId())
                .bizType("INBOUND")
                .reason("入库完成：" + inbound.getReason())
                .build();

        // 获取或创建余额记录
        var balance = balanceService.createOrGetBalance(inbound.getApplicantId(), inbound.getMaterialId());
        Integer newBalance = balance.getAvailableBalance() + inbound.getQuantity();
        
        // 更新余额
        boolean updated = balanceService.updateBalanceWithOptimisticLock(balance, newBalance);
        if (!updated) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_BALANCE_NOT_EXISTS);
        }

        // 记录流水
        txnService.createTxnRecord(act, newBalance);

        // 4. 更新入库单状态
        inbound.setStatus(3); // 已完成
        inbound.setCompleteTime(LocalDateTime.now());
        inboundMapper.updateById(inbound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInbound(Long id, Long operatorId) {
        // 1. 获取入库单
        MaterialInboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 2. 验证状态（只有待审核和已审核状态可以取消）
        if (inbound.getStatus() != 0 && inbound.getStatus() != 1) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 3. 更新状态
        inbound.setStatus(4); // 已取消
        inboundMapper.updateById(inbound);
    }

    @Override
    public PageResult<MaterialInboundRespVO> getInboundPage(MaterialInboundPageReqVO pageReqVO) {
        PageResult<MaterialInboundDO> pageResult = inboundMapper.selectPage(pageReqVO);
        if (pageResult == null || CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        List<MaterialInboundRespVO> respList = MaterialInboundConvert.INSTANCE.convertList(pageResult.getList());
        
        // 设置状态描述
        respList.forEach(this::setStatusDesc);

        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public MaterialInboundRespVO getInbound(Long id) {
        MaterialInboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            return null;
        }
        
        MaterialInboundRespVO respVO = MaterialInboundConvert.INSTANCE.convert(inbound);
        setStatusDesc(respVO);
        return respVO;
    }

    private void setStatusDesc(MaterialInboundRespVO respVO) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "待审核");
        statusMap.put(1, "已审核");
        statusMap.put(2, "已拒绝");
        statusMap.put(3, "已完成");
        statusMap.put(4, "已取消");
        respVO.setStatusDesc(statusMap.getOrDefault(respVO.getStatus(), "未知"));
    }

    private String generateInboundNo() {
        return "INB" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 4);
    }

}