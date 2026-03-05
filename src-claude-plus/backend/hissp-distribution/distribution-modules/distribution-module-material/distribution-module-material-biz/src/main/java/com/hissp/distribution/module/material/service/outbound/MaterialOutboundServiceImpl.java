package com.hissp.distribution.module.material.service.outbound;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.framework.common.util.number.NumberUtils;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.api.enums.MaterialOutboundStatusEnum;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.*;
import com.hissp.distribution.module.material.controller.app.outbound.vo.*;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialOutboundDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialOutboundItemDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialOutboundItemMapper;
import com.hissp.distribution.module.material.dal.mysql.MaterialOutboundMapper;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.material.enums.ErrorCodeConstants.*;

/**
 * 物料出库 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MaterialOutboundServiceImpl implements MaterialOutboundService {

    @Resource
    private MaterialOutboundMapper outboundMapper;
    
    @Resource
    private MaterialOutboundItemMapper outboundItemMapper;
    
    @Resource
    private MaterialDefinitionService materialDefinitionService;

    @Resource
    private MaterialApi materialApi;

    @Resource
    private MemberUserApi memberUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutbound(@Valid MaterialOutboundCreateReqVO createReqVO) {
        // 1. 验证物料定义存在且支持出库
        validateMaterialsForOutbound(createReqVO.getItems());
        
        // 2. 生成出库单号
        String outboundNo = generateOutboundNo();
        
        // 3. 创建出库主记录
        MaterialOutboundDO outbound = buildOutboundDO(createReqVO, outboundNo);
        outboundMapper.insert(outbound);
        
        // 4. 创建出库明细记录
        List<MaterialOutboundItemDO> items = buildOutboundItems(outbound.getId(), createReqVO.getItems());
        items.forEach(outboundItemMapper::insert);
        
        log.info("[MaterialOutboundService][创建物料出库申请成功] outboundId={}, outboundNo={}, userId={}", 
                outbound.getId(), outboundNo, createReqVO.getUserId());
        
        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOutbound(@Valid MaterialOutboundUpdateReqVO updateReqVO) {
        // 1. 验证出库单存在
        MaterialOutboundDO existingOutbound = validateOutboundExists(updateReqVO.getId());
        
        // 2. 只有待审核状态的出库单才能修改
        if (!isStatusPending(existingOutbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 3. 验证物料定义存在且支持出库
        validateMaterialsForOutbound(updateReqVO.getItems());
        
        // 4. 更新出库主记录
        MaterialOutboundDO updateObj = buildOutboundDO(updateReqVO, existingOutbound.getOutboundNo());
        updateObj.setId(updateReqVO.getId());
        outboundMapper.updateById(updateObj);
        
        // 5. 删除原有明细，重新创建
        outboundItemMapper.deleteByOutboundId(updateReqVO.getId());
        List<MaterialOutboundItemDO> items = buildOutboundItems(updateReqVO.getId(), updateReqVO.getItems());
        items.forEach(outboundItemMapper::insert);
        
        log.info("[MaterialOutboundService][更新物料出库申请成功] outboundId={}, userId={}", 
                updateReqVO.getId(), updateReqVO.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutbound(Long id) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 只有待审核状态的出库单才能删除
        if (!isStatusPending(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 3. 删除出库单和明细
        outboundMapper.deleteById(id);
        outboundItemMapper.deleteByOutboundId(id);
        
        log.info("[MaterialOutboundService][删除物料出库申请成功] outboundId={}", id);
    }

    @Override
    public MaterialOutboundRespVO getOutbound(Long id) {
        MaterialOutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            return null;
        }
        return convertToRespVO(outbound);
    }

    @Override
    public PageResult<MaterialOutboundRespVO> getOutboundPage(MaterialOutboundPageReqVO pageReqVO) {
        PageResult<MaterialOutboundDO> pageResult = outboundMapper.selectPage(pageReqVO);
        return convertToRespVOPage(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveOutbound(Long id, Long approveUserId, Boolean approved, String reason) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 只有待审核状态的出库单才能审核
        if (!isStatusPending(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 3. 如果审核通过，需要扣减用户物料余额
        if (Boolean.TRUE.equals(approved)) {
            List<MaterialOutboundItemDO> items = outboundItemMapper.selectListByOutboundId(id);
            List<MaterialActDTO> acts = items.stream()
                    .map(item -> MaterialActDTO.builder()
                            .userId(outbound.getUserId())
                            .materialId(item.getMaterialId())
                            .quantity(item.getQuantity())
                            .direction(MaterialActDirectionEnum.OUT)
                            .bizKey("OUTBOUND_" + id + "_" + item.getMaterialId())
                            .bizType("OUTBOUND")
                            .reason("物料出库扣减")
                            .build())
                    .collect(Collectors.toList());
            
            materialApi.applyActs(acts);
            
            // 更新状态为已审核
            outbound.setStatus(MaterialOutboundStatusEnum.APPROVED.getStatus());
            outbound.setApproveTime(LocalDateTime.now());
            outbound.setApproveUserId(approveUserId);
        } else {
            // 审核拒绝
            outbound.setStatus(MaterialOutboundStatusEnum.REJECTED.getStatus());
            outbound.setApproveTime(LocalDateTime.now());
            outbound.setApproveUserId(approveUserId);
            outbound.setCancelReason(reason);
        }
        
        outboundMapper.updateById(outbound);
        
        log.info("[MaterialOutboundService][审核物料出库申请] outboundId={}, approved={}, approveUserId={}", 
                id, approved, approveUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchApproveOutbound(List<Long> ids, Long approveUserId) {
        for (Long id : ids) {
            approveOutbound(id, approveUserId, true, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOutbound(Long id, String logisticsCompany, String logisticsCode) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 只有已审核状态的出库单才能发货
        if (!isStatusApproved(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 3. 更新发货信息
        outbound.setStatus(MaterialOutboundStatusEnum.SHIPPED.getStatus());
        outbound.setLogisticsCompany(logisticsCompany);
        outbound.setLogisticsCode(logisticsCode);
        outbound.setShipTime(LocalDateTime.now());
        
        outboundMapper.updateById(outbound);
        
        log.info("[MaterialOutboundService][发货物料出库单] outboundId={}, logisticsCompany={}, logisticsCode={}", 
                id, logisticsCompany, logisticsCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOutbound(Long id) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 只有已发货状态的出库单才能确认收货
        if (!isStatusShipped(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 3. 更新状态为已完成
        outbound.setStatus(MaterialOutboundStatusEnum.COMPLETED.getStatus());
        outbound.setCompleteTime(LocalDateTime.now());
        
        outboundMapper.updateById(outbound);
        
        log.info("[MaterialOutboundService][确认收货物料出库单] outboundId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutbound(Long id, String cancelReason) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 已完成的出库单不能取消
        if (isStatusCompleted(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 3. 如果出库单已审核，需要回退用户物料余额
        if (isStatusApproved(outbound.getStatus()) || isStatusShipped(outbound.getStatus())) {
            List<MaterialOutboundItemDO> items = outboundItemMapper.selectListByOutboundId(id);
            List<MaterialActDTO> acts = items.stream()
                    .map(item -> MaterialActDTO.builder()
                            .userId(outbound.getUserId())
                            .materialId(item.getMaterialId())
                            .quantity(item.getQuantity())
                            .direction(MaterialActDirectionEnum.IN)
                            .bizKey("OUTBOUND_CANCEL_" + id + "_" + item.getMaterialId())
                            .bizType("OUTBOUND_CANCEL")
                            .reason("物料出库取消回退")
                            .build())
                    .collect(Collectors.toList());
            
            materialApi.applyActs(acts);
        }
        
        // 4. 更新状态为已取消
        outbound.setStatus(MaterialOutboundStatusEnum.CANCELLED.getStatus());
        outbound.setCancelTime(LocalDateTime.now());
        outbound.setCancelReason(cancelReason);
        
        outboundMapper.updateById(outbound);
        
        log.info("[MaterialOutboundService][取消物料出库单] outboundId={}, reason={}", id, cancelReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appCancelOutbound(Long id, String cancelReason, Long userId) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 验证出库单属于当前用户
        if (!userId.equals(outbound.getUserId())) {
            throw exception(MATERIAL_OUTBOUND_NOT_EXISTS);
        }
        
        // 3. 已完成的出库单不能取消
        if (isStatusCompleted(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 4. 如果出库单已审核，需要回退用户物料余额
        if (isStatusApproved(outbound.getStatus()) || isStatusShipped(outbound.getStatus())) {
            List<MaterialOutboundItemDO> items = outboundItemMapper.selectListByOutboundId(id);
            List<MaterialActDTO> acts = items.stream()
                    .map(item -> MaterialActDTO.builder()
                            .userId(outbound.getUserId())
                            .materialId(item.getMaterialId())
                            .quantity(item.getQuantity())
                            .direction(MaterialActDirectionEnum.IN)
                            .bizKey("OUTBOUND_APP_CANCEL_" + id + "_" + item.getMaterialId())
                            .bizType("OUTBOUND_APP_CANCEL")
                            .reason(cancelReason)
                            .build())
                    .collect(Collectors.toList());
            
            materialApi.applyActs(acts);
        }
        
        // 5. 更新状态为已取消
        outbound.setStatus(MaterialOutboundStatusEnum.CANCELLED.getStatus());
        outbound.setCancelTime(LocalDateTime.now());
        outbound.setCancelReason(cancelReason);
        
        outboundMapper.updateById(outbound);
        
        log.info("[MaterialOutboundService][用户取消物料出库单] outboundId={}, userId={}, reason={}", id, userId, cancelReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appCompleteOutbound(Long id, Long userId) {
        // 1. 验证出库单存在
        MaterialOutboundDO outbound = validateOutboundExists(id);
        
        // 2. 验证出库单属于当前用户
        if (!userId.equals(outbound.getUserId())) {
            throw exception(MATERIAL_OUTBOUND_NOT_EXISTS);
        }
        
        // 3. 只有已发货状态的出库单才能确认收货
        if (!isStatusShipped(outbound.getStatus())) {
            throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW);
        }
        
        // 4. 更新状态为已完成
        outbound.setStatus(MaterialOutboundStatusEnum.COMPLETED.getStatus());
        outbound.setCompleteTime(LocalDateTime.now());
        
        outboundMapper.updateById(outbound);
        
        log.info("[MaterialOutboundService][用户确认收货物料出库单] outboundId={}, userId={}", id, userId);
    }

    // ========== APP端接口实现 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppMaterialOutboundCreateRespVO createAppOutbound(AppMaterialOutboundCreateReqVO createReqVO) {
        // 1. 验证物料定义存在且支持出库
        validateAppMaterialsForOutbound(createReqVO.getItems());

        // 2. 生成出库单号
        String outboundNo = generateOutboundNo();

        // 3. 创建出库主记录
        MaterialOutboundDO outbound = buildAppOutboundDO(createReqVO, outboundNo);
        outboundMapper.insert(outbound);

        // 4. 创建出库明细记录
        List<MaterialOutboundItemDO> items = buildAppOutboundItems(outbound.getId(), createReqVO.getItems());
        items.forEach(outboundItemMapper::insert);

        log.info("[MaterialOutboundService][APP端创建物料出库申请成功] outboundId={}, outboundNo={}, userId={}",
                outbound.getId(), outboundNo, createReqVO.getUserId());

        // 5. 构建响应VO
        AppMaterialOutboundCreateRespVO respVO = new AppMaterialOutboundCreateRespVO();
        respVO.setId(outbound.getId());
        respVO.setOutboundNo(outboundNo);
        respVO.setStatus(outbound.getStatus());
        respVO.setStatusName(getStatusName(outbound.getStatus()));

        return respVO;
    }

    @Override
    public PageResult<AppMaterialOutboundRespVO> getAppOutboundPage(AppMaterialOutboundPageReqVO pageReqVO) {
        PageResult<MaterialOutboundDO> pageResult = outboundMapper.selectAppPage(pageReqVO);
        return convertToAppRespVOPage(pageResult);
    }

    @Override
    public AppMaterialOutboundDetailRespVO getAppOutboundDetail(Long id, Long userId) {
        // 查询出库单
        MaterialOutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw exception(MATERIAL_OUTBOUND_NOT_EXISTS);
        }

        // 校验用户权限(只能查看自己的出库单)
        if (!outbound.getUserId().equals(userId)) {
            throw exception(MATERIAL_OUTBOUND_NOT_EXISTS, "无权限查看此出库单");
        }

        // 转换为详情VO
        return convertToAppDetailRespVO(outbound);
    }

    // ========== 私有方法 ==========

    private void validateMaterialsForOutbound(List<MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO> items) {
        List<Long> materialIds = CollectionUtils.convertList(items,
                MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO::getMaterialId);
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

        for (MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO item : items) {
            MaterialDefinitionDO material = materialMap.get(item.getMaterialId());
            if (material == null) {
                log.error("[MaterialOutboundService][物料定义不存在] materialId={}", item.getMaterialId());
                throw exception(MATERIAL_DEFINITION_NOT_EXISTS);
            }

            // 检查物料状态是否启用
            if (!Integer.valueOf(1).equals(material.getStatus())) {
                log.error("[MaterialOutboundService][物料状态不可用] materialId={}, status={}",
                        item.getMaterialId(), material.getStatus());
                throw exception(MATERIAL_DEFINITION_NOT_EXISTS, "物料状态不可用");
            }

            // 检查物料是否支持出库
            if (!Boolean.TRUE.equals(material.getSupportOutbound())) {
                log.error("[MaterialOutboundService][物料不支持出库] materialId={}, supportOutbound={}",
                        item.getMaterialId(), material.getSupportOutbound());
                throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW, "物料不支持出库");
            }
        }
    }

    private String generateOutboundNo() {
        // 生成出库单号：OUT + 年月日 + 序号
        return "OUT" + System.currentTimeMillis();
    }

    private MaterialOutboundDO buildOutboundDO(MaterialOutboundCreateReqVO createReqVO, String outboundNo) {
        return MaterialOutboundDO.builder()
                .outboundNo(outboundNo)
                .userId(createReqVO.getUserId())
                .addressId(createReqVO.getAddressId())
                .receiverName(createReqVO.getReceiverName())
                .receiverMobile(createReqVO.getReceiverMobile())
                .receiverProvince(createReqVO.getReceiverProvince())
                .receiverCity(createReqVO.getReceiverCity())
                .receiverDistrict(createReqVO.getReceiverDistrict())
                .receiverDetailAddress(createReqVO.getReceiverDetailAddress())
                .status(MaterialOutboundStatusEnum.PENDING.getStatus())
                .remark(createReqVO.getRemark())
                .build();
    }

    private List<MaterialOutboundItemDO> buildOutboundItems(Long outboundId,
            List<MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO> itemReqVOs) {

        // 获取物料信息
        List<Long> materialIds = CollectionUtils.convertList(itemReqVOs,
                MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO::getMaterialId);
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

        return itemReqVOs.stream()
                .map(itemReqVO -> {
                    MaterialDefinitionDO material = materialMap.get(itemReqVO.getMaterialId());
                    return MaterialOutboundItemDO.builder()
                            .outboundId(outboundId)
                            .materialId(itemReqVO.getMaterialId())
                            .quantity(itemReqVO.getQuantity())
                            .unit(material.getBaseUnit()) // 使用物料的基础单位
                            .materialName(material.getName()) // 非表字段,用于查询时展示
                            .materialCode(material.getCode()) // 非表字段,用于查询时展示
                            .baseUnit(material.getBaseUnit()) // 非表字段,用于查询时展示
                            .build();
                })
                .collect(Collectors.toList());
    }

    private MaterialOutboundDO validateOutboundExists(Long id) {
        MaterialOutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw exception(MATERIAL_OUTBOUND_NOT_EXISTS);
        }
        return outbound;
    }

    private boolean isStatusPending(Integer status) {
        return MaterialOutboundStatusEnum.PENDING.getStatus().equals(status);
    }

    private boolean isStatusApproved(Integer status) {
        return MaterialOutboundStatusEnum.APPROVED.getStatus().equals(status);
    }

    private boolean isStatusShipped(Integer status) {
        return MaterialOutboundStatusEnum.SHIPPED.getStatus().equals(status);
    }

    private boolean isStatusCompleted(Integer status) {
        return MaterialOutboundStatusEnum.COMPLETED.getStatus().equals(status);
    }

    private PageResult<MaterialOutboundRespVO> convertToRespVOPage(PageResult<MaterialOutboundDO> pageResult) {
        if (pageResult == null || pageResult.getList() == null) {
            return PageResult.empty();
        }

        // 批量查询用户信息
        List<Long> userIds = pageResult.getList().stream()
                .map(MaterialOutboundDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);

        List<MaterialOutboundRespVO> respVOList = pageResult.getList().stream()
                .map(outbound -> {
                    MaterialOutboundRespVO vo = convertToRespVO(outbound);
                    // 填充用户昵称
                    MemberUserRespDTO user = userMap.get(outbound.getUserId());
                    if (user != null) {
                        vo.setUserNickname(user.getNickname());
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(respVOList, pageResult.getTotal());
    }

    private PageResult<AppMaterialOutboundRespVO> convertToAppRespVOPage(PageResult<MaterialOutboundDO> pageResult) {
        if (pageResult == null || pageResult.getList() == null) {
            return PageResult.empty();
        }

        List<AppMaterialOutboundRespVO> respVOList = pageResult.getList().stream()
                .map(this::convertToAppRespVO)
                .collect(Collectors.toList());

        return new PageResult<>(respVOList, pageResult.getTotal());
    }

    private AppMaterialOutboundRespVO convertToAppRespVO(MaterialOutboundDO outbound) {
        if (outbound == null) {
            return null;
        }

        AppMaterialOutboundRespVO respVO = new AppMaterialOutboundRespVO();
        respVO.setId(outbound.getId());
        respVO.setOutboundNo(outbound.getOutboundNo());
        respVO.setUserId(outbound.getUserId());
        respVO.setStatus(outbound.getStatus());
        respVO.setStatusName(getStatusName(outbound.getStatus()));
        respVO.setReceiverName(outbound.getReceiverName());
        respVO.setReceiverMobile(outbound.getReceiverMobile());
        respVO.setReceiverProvince(outbound.getReceiverProvince());
        respVO.setReceiverCity(outbound.getReceiverCity());
        respVO.setReceiverDistrict(outbound.getReceiverDistrict());
        respVO.setReceiverDetailAddress(outbound.getReceiverDetailAddress());
        respVO.setRemark(outbound.getRemark());
        respVO.setLogisticsCompany(outbound.getLogisticsCompany());
        respVO.setTrackingNumber(outbound.getLogisticsCode());
        respVO.setShippedTime(outbound.getShipTime());
        respVO.setReceivedTime(outbound.getCompleteTime());
        respVO.setCreateTime(outbound.getCreateTime());
        respVO.setUpdateTime(outbound.getUpdateTime());

        // 填充物料统计信息
        fillMaterialSummary(outbound.getId(), respVO);

        // TODO: 填充用户昵称等附加信息
        // respVO.setNickname(getUserNickname(outbound.getUserId()));

        return respVO;
    }

    /**
     * 填充物料统计信息(列表展示用)
     */
    private void fillMaterialSummary(Long outboundId, AppMaterialOutboundRespVO respVO) {
        // 查询出库明细
        List<MaterialOutboundItemDO> items = outboundItemMapper.selectList(
                MaterialOutboundItemDO::getOutboundId, outboundId);

        if (org.springframework.util.CollectionUtils.isEmpty(items)) {
            respVO.setItemCount(0);
            respVO.setTotalQuantity(0);
            respVO.setMaterialImages(new java.util.ArrayList<>());
            respVO.setMaterialNames(new java.util.ArrayList<>());
            return;
        }

        // 统计物料种类数和总数量
        respVO.setItemCount(items.size());
        respVO.setTotalQuantity(items.stream()
                .mapToInt(MaterialOutboundItemDO::getQuantity)
                .sum());

        // 获取物料详情
        List<Long> materialIds = CollectionUtils.convertList(items, MaterialOutboundItemDO::getMaterialId);
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

        // 收集物料图片(最多3张)
        List<String> materialImages = items.stream()
                .limit(3)
                .map(item -> materialMap.get(item.getMaterialId()))
                .filter(material -> material != null && material.getImage() != null)
                .map(MaterialDefinitionDO::getImage)
                .collect(Collectors.toList());
        respVO.setMaterialImages(materialImages);

        // 收集物料名称(最多3个)
        List<String> materialNames = items.stream()
                .limit(3)
                .map(item -> materialMap.get(item.getMaterialId()))
                .filter(material -> material != null && material.getName() != null)
                .map(MaterialDefinitionDO::getName)
                .collect(Collectors.toList());
        respVO.setMaterialNames(materialNames);
    }

    /**
     * 转换为APP端详情VO
     */
    private AppMaterialOutboundDetailRespVO convertToAppDetailRespVO(MaterialOutboundDO outbound) {
        if (outbound == null) {
            return null;
        }

        AppMaterialOutboundDetailRespVO respVO = new AppMaterialOutboundDetailRespVO();
        respVO.setId(outbound.getId());
        respVO.setOutboundNo(outbound.getOutboundNo());
        respVO.setUserId(outbound.getUserId());
        respVO.setStatus(outbound.getStatus());
        respVO.setStatusName(getStatusName(outbound.getStatus()));
        respVO.setReceiverName(outbound.getReceiverName());
        respVO.setReceiverMobile(outbound.getReceiverMobile());
        respVO.setReceiverProvince(outbound.getReceiverProvince());
        respVO.setReceiverCity(outbound.getReceiverCity());
        respVO.setReceiverDistrict(outbound.getReceiverDistrict());
        respVO.setReceiverDetailAddress(outbound.getReceiverDetailAddress());

        // 拼接完整地址
        String fullAddress = String.format("%s%s%s%s",
                outbound.getReceiverProvince() == null ? "" : outbound.getReceiverProvince(),
                outbound.getReceiverCity() == null ? "" : outbound.getReceiverCity(),
                outbound.getReceiverDistrict() == null ? "" : outbound.getReceiverDistrict(),
                outbound.getReceiverDetailAddress() == null ? "" : outbound.getReceiverDetailAddress());
        respVO.setFullAddress(fullAddress);

        respVO.setRemark(outbound.getRemark());
        respVO.setLogisticsCompany(outbound.getLogisticsCompany());
        respVO.setTrackingNumber(outbound.getLogisticsCode());
        respVO.setShippedTime(outbound.getShipTime());
        respVO.setReceivedTime(outbound.getCompleteTime());
        respVO.setApproveTime(outbound.getApproveTime());
        respVO.setShipTime(outbound.getShipTime());
        respVO.setCompleteTime(outbound.getCompleteTime());
        respVO.setCancelTime(outbound.getCancelTime());
        respVO.setCancelReason(outbound.getCancelReason());
        // TODO: MaterialOutboundDO 缺少 approveRemark 字段
        // respVO.setApproveRemark(outbound.getApproveRemark());
        respVO.setCreateTime(outbound.getCreateTime());
        respVO.setUpdateTime(outbound.getUpdateTime());

        // 查询出库明细
        List<MaterialOutboundItemDO> items = outboundItemMapper.selectList(
                MaterialOutboundItemDO::getOutboundId, outbound.getId());

        if (!org.springframework.util.CollectionUtils.isEmpty(items)) {
            // 获取物料详情
            List<Long> materialIds = CollectionUtils.convertList(items, MaterialOutboundItemDO::getMaterialId);
            Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

            // 转换明细列表
            List<AppMaterialOutboundDetailRespVO.AppMaterialOutboundItemRespVO> itemVOList = items.stream()
                    .map(item -> {
                        MaterialDefinitionDO material = materialMap.get(item.getMaterialId());

                        AppMaterialOutboundDetailRespVO.AppMaterialOutboundItemRespVO itemVO =
                                new AppMaterialOutboundDetailRespVO.AppMaterialOutboundItemRespVO();
                        itemVO.setId(item.getId());
                        itemVO.setMaterialId(item.getMaterialId());
                        itemVO.setMaterialName(material != null ? material.getName() : "");
                        itemVO.setMaterialImage(material != null ? material.getImage() : null);
                        itemVO.setMaterialUnit(item.getUnit());
                        itemVO.setQuantity(item.getQuantity());
                        // TODO: 物料规格字段待补充
                        // itemVO.setMaterialSpec(material != null ? material.getSpec() : "");

                        return itemVO;
                    })
                    .collect(Collectors.toList());
            respVO.setItems(itemVOList);
        } else {
            respVO.setItems(new java.util.ArrayList<>());
        }

        // TODO: 填充用户昵称
        // respVO.setNickname(getUserNickname(outbound.getUserId()));

        return respVO;
    }

    private String getStatusName(Integer status) {
        if (status == null) {
            return "";
        }

        for (MaterialOutboundStatusEnum statusEnum : MaterialOutboundStatusEnum.values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    private void validateAppMaterialsForOutbound(List<AppMaterialOutboundCreateReqVO.AppMaterialOutboundItemCreateReqVO> items) {
        List<Long> materialIds = CollectionUtils.convertList(items,
                AppMaterialOutboundCreateReqVO.AppMaterialOutboundItemCreateReqVO::getMaterialId);
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

        for (AppMaterialOutboundCreateReqVO.AppMaterialOutboundItemCreateReqVO item : items) {
            MaterialDefinitionDO material = materialMap.get(item.getMaterialId());
            if (material == null) {
                log.error("[MaterialOutboundService][物料定义不存在] materialId={}", item.getMaterialId());
                throw exception(MATERIAL_DEFINITION_NOT_EXISTS);
            }

            // 检查物料状态是否启用
            if (!Integer.valueOf(1).equals(material.getStatus())) {
                log.error("[MaterialOutboundService][物料状态不可用] materialId={}, status={}",
                        item.getMaterialId(), material.getStatus());
                throw exception(MATERIAL_DEFINITION_NOT_EXISTS, "物料状态不可用");
            }

            // 检查物料是否支持出库
            if (!Boolean.TRUE.equals(material.getSupportOutbound())) {
                log.error("[MaterialOutboundService][物料不支持出库] materialId={}, supportOutbound={}",
                        item.getMaterialId(), material.getSupportOutbound());
                throw exception(MATERIAL_OUTBOUND_STATUS_NOT_ALLOW, "物料不支持出库");
            }
        }
    }

    private MaterialOutboundDO buildAppOutboundDO(AppMaterialOutboundCreateReqVO createReqVO, String outboundNo) {
        return MaterialOutboundDO.builder()
                .outboundNo(outboundNo)
                .userId(createReqVO.getUserId())
                .addressId(createReqVO.getAddressId())
                .receiverName(createReqVO.getReceiverName())
                .receiverMobile(createReqVO.getReceiverMobile())
                .receiverProvince(createReqVO.getReceiverProvince())
                .receiverCity(createReqVO.getReceiverCity())
                .receiverDistrict(createReqVO.getReceiverDistrict())
                .receiverDetailAddress(createReqVO.getReceiverDetailAddress())
                .status(MaterialOutboundStatusEnum.PENDING.getStatus())
                .remark(createReqVO.getRemark())
                .build();
    }

    private List<MaterialOutboundItemDO> buildAppOutboundItems(Long outboundId,
            List<AppMaterialOutboundCreateReqVO.AppMaterialOutboundItemCreateReqVO> itemReqVOs) {

        // 获取物料信息
        List<Long> materialIds = CollectionUtils.convertList(itemReqVOs,
                AppMaterialOutboundCreateReqVO.AppMaterialOutboundItemCreateReqVO::getMaterialId);
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

        return itemReqVOs.stream()
                .map(itemReqVO -> {
                    MaterialDefinitionDO material = materialMap.get(itemReqVO.getMaterialId());
                    return MaterialOutboundItemDO.builder()
                            .outboundId(outboundId)
                            .materialId(itemReqVO.getMaterialId())
                            .quantity(itemReqVO.getQuantity())
                            .unit(material.getBaseUnit()) // 使用物料的基础单位
                            .materialName(material.getName()) // 非表字段,用于查询时展示
                            .materialCode(material.getCode()) // 非表字段,用于查询时展示
                            .baseUnit(material.getBaseUnit()) // 非表字段,用于查询时展示
                            .build();
                })
                .collect(Collectors.toList());
    }

    private MaterialOutboundRespVO convertToRespVO(MaterialOutboundDO outbound) {
        if (outbound == null) {
            return null;
        }

        MaterialOutboundRespVO respVO = new MaterialOutboundRespVO();
        respVO.setId(outbound.getId());
        respVO.setOutboundNo(outbound.getOutboundNo());
        respVO.setUserId(outbound.getUserId());
        respVO.setAddressId(outbound.getAddressId());
        respVO.setReceiverName(outbound.getReceiverName());
        respVO.setReceiverMobile(outbound.getReceiverMobile());
        respVO.setReceiverProvince(outbound.getReceiverProvince());
        respVO.setReceiverCity(outbound.getReceiverCity());
        respVO.setReceiverDistrict(outbound.getReceiverDistrict());
        respVO.setReceiverDetailAddress(outbound.getReceiverDetailAddress());
        respVO.setStatus(outbound.getStatus());
        respVO.setStatusName(getStatusName(outbound.getStatus()));
        respVO.setRemark(outbound.getRemark());
        respVO.setLogisticsCode(outbound.getLogisticsCode());
        respVO.setLogisticsCompany(outbound.getLogisticsCompany());
        respVO.setApproveTime(outbound.getApproveTime());
        respVO.setApproveUserId(outbound.getApproveUserId());
        respVO.setShipTime(outbound.getShipTime());
        respVO.setCompleteTime(outbound.getCompleteTime());
        respVO.setCancelTime(outbound.getCancelTime());
        respVO.setCancelReason(outbound.getCancelReason());
        respVO.setCreateTime(outbound.getCreateTime());
        respVO.setCreateBy(outbound.getCreator());

        // 填充出库明细列表
        List<MaterialOutboundItemDO> items = outboundItemMapper.selectList(
                MaterialOutboundItemDO::getOutboundId, outbound.getId());

        if (!org.springframework.util.CollectionUtils.isEmpty(items)) {
            // 获取物料详情
            List<Long> materialIds = CollectionUtils.convertList(items, MaterialOutboundItemDO::getMaterialId);
            Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);

            List<MaterialOutboundRespVO.MaterialOutboundItemRespVO> itemVOList = items.stream()
                    .map(item -> {
                        MaterialDefinitionDO material = materialMap.get(item.getMaterialId());

                        MaterialOutboundRespVO.MaterialOutboundItemRespVO itemVO =
                                new MaterialOutboundRespVO.MaterialOutboundItemRespVO();
                        itemVO.setId(item.getId());
                        itemVO.setMaterialId(item.getMaterialId());
                        itemVO.setMaterialName(material != null ? material.getName() : "");
                        itemVO.setMaterialCode(material != null ? material.getCode() : "");
                        itemVO.setQuantity(item.getQuantity());
                        itemVO.setBaseUnit(item.getUnit());

                        return itemVO;
                    })
                    .collect(Collectors.toList());
            respVO.setItems(itemVOList);
        } else {
            respVO.setItems(new java.util.ArrayList<>());
        }

        // TODO: 填充用户昵称、审核人姓名等附加信息
        // respVO.setUserNickname(getUserNickname(outbound.getUserId()));
        // respVO.setApproveUserName(getUserName(outbound.getApproveUserId()));

        return respVO;
    }

    @Override
    public void exportShippingTemplate(HttpServletResponse response) throws IOException {
        // 查询所有"已审核待发货"状态的出库单
        MaterialOutboundPageReqVO query = new MaterialOutboundPageReqVO();
        query.setStatus(MaterialOutboundStatusEnum.APPROVED.getStatus());
        query.setPageNo(1);
        query.setPageSize(10000); // 一次性查询大量数据

        PageResult<MaterialOutboundDO> pageResult = outboundMapper.selectPage(query);
        List<MaterialOutboundDO> outbounds = pageResult.getList();

        log.info("[MaterialOutboundService][导出待发货模板] 查询到{}条待发货记录", outbounds == null ? 0 : outbounds.size());

        // 创建Excel工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("待发货出库单");

        // 创建表头样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 创建表头
        HSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"出库单ID", "出库单号", "收货人", "联系方式", "收货地址", "物流公司", "物流单号"};
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 添加物流公司说明行(第二行)
        HSSFRow remarkRow = sheet.createRow(1);
        HSSFCellStyle remarkStyle = workbook.createCellStyle();
        HSSFFont remarkFont = workbook.createFont();
        remarkFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        remarkFont.setItalic(true);
        remarkStyle.setFont(remarkFont);

        HSSFCell remarkCell = remarkRow.createCell(5);
        remarkCell.setCellValue("请从下拉列表中选择物流公司");
        remarkCell.setCellStyle(remarkStyle);
        // 合并单元格(物流公司和物流单号列)
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 5, 6));

        // 定义物流公司下拉选项
        String[] logisticsCompanies = {
            "顺丰速运",
            "中通快递",
            "圆通速递",
            "申通快递",
            "韵达速递",
            "百世汇通",
            "天天快递",
            "德邦快递",
            "京东物流",
            "邮政EMS"
        };

        // 创建数据验证约束对象
        HSSFDataValidation dataValidation = null;
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        // 设置下拉列表的值
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(logisticsCompanies);
        // 设置数据验证的范围：物流公司列(第5列,从第3行到第10000行)
        org.apache.poi.ss.util.CellRangeAddressList addressList = new org.apache.poi.ss.util.CellRangeAddressList(2, 9999, 5, 5);
        dataValidation = (HSSFDataValidation) validationHelper.createValidation(constraint, addressList);
        // 设置提示信息
        dataValidation.createPromptBox("物流公司选择", "请从下拉列表中选择物流公司");
        dataValidation.setShowPromptBox(true);
        // 设置错误提示
        dataValidation.createErrorBox("输入错误", "请从下拉列表中选择有效的物流公司");
        dataValidation.setShowErrorBox(true);
        // 应用数据验证
        sheet.addValidationData(dataValidation);

        // 填充数据(从第三行开始)
        int rowNum = 2;
        for (MaterialOutboundDO outbound : outbounds) {
            HSSFRow row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(outbound.getId());
            row.createCell(1).setCellValue(outbound.getOutboundNo());
            row.createCell(2).setCellValue(outbound.getReceiverName());
            row.createCell(3).setCellValue(outbound.getReceiverMobile());

            // 拼接完整地址
            String fullAddress = String.format("%s%s%s%s",
                    outbound.getReceiverProvince() == null ? "" : outbound.getReceiverProvince(),
                    outbound.getReceiverCity() == null ? "" : outbound.getReceiverCity(),
                    outbound.getReceiverDistrict() == null ? "" : outbound.getReceiverDistrict(),
                    outbound.getReceiverDetailAddress() == null ? "" : outbound.getReceiverDetailAddress());
            row.createCell(4).setCellValue(fullAddress);

            row.createCell(5).setCellValue(""); // 物流公司（待填写）
            row.createCell(6).setCellValue(""); // 物流单号（待填写）
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // 额外增加一点宽度
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2000);
        }

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("物料出库待发货模板_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xls");

        // 输出Excel
        try (OutputStream out = response.getOutputStream()) {
            workbook.write(out);
            out.flush();
        } finally {
            workbook.close();
        }

        log.info("[MaterialOutboundService][导出待发货模板] 导出{}条记录", outbounds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importShipping(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 跳过表头和说明行,从第三行开始读取数据(索引从0开始,第三行是索引2)
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    // 读取出库单ID
                    Cell idCell = row.getCell(0);
                    if (idCell == null || idCell.getCellType() == CellType.BLANK) {
                        continue; // 跳过空行
                    }

                    Long outboundId;
                    if (idCell.getCellType() == CellType.NUMERIC) {
                        outboundId = (long) idCell.getNumericCellValue();
                    } else {
                        outboundId = Long.parseLong(idCell.getStringCellValue().trim());
                    }

                    // 读取物流公司 - 兼容字符串和数字类型
                    Cell logisticsCompanyCell = row.getCell(5);
                    String logisticsCompany = "";
                    if (logisticsCompanyCell != null && logisticsCompanyCell.getCellType() != CellType.BLANK) {
                        if (logisticsCompanyCell.getCellType() == CellType.NUMERIC) {
                            logisticsCompany = String.valueOf((long) logisticsCompanyCell.getNumericCellValue());
                        } else {
                            logisticsCompany = logisticsCompanyCell.getStringCellValue().trim();
                        }
                    }

                    // 读取物流单号 - 兼容字符串和数字类型
                    Cell logisticsCodeCell = row.getCell(6);
                    String logisticsCode = "";
                    if (logisticsCodeCell != null && logisticsCodeCell.getCellType() != CellType.BLANK) {
                        if (logisticsCodeCell.getCellType() == CellType.NUMERIC) {
                            // 数字类型,转为字符串(保留完整数字,不使用科学计数法)
                            logisticsCode = String.format("%.0f", logisticsCodeCell.getNumericCellValue());
                        } else {
                            logisticsCode = logisticsCodeCell.getStringCellValue().trim();
                        }
                    }

                    // 校验必填字段
                    if (logisticsCompany.isEmpty() || logisticsCode.isEmpty()) {
                        errors.add(String.format("第%d行：物流公司或物流单号不能为空", i + 1));
                        failureCount++;
                        continue;
                    }

                    // 校验出库单存在且状态正确
                    MaterialOutboundDO outbound = outboundMapper.selectById(outboundId);
                    if (outbound == null) {
                        errors.add(String.format("第%d行：出库单ID[%d]不存在", i + 1, outboundId));
                        failureCount++;
                        continue;
                    }

                    if (!isStatusApproved(outbound.getStatus())) {
                        errors.add(String.format("第%d行：出库单[%s]状态不是\"已审核待发货\"，当前状态：%s",
                                i + 1, outbound.getOutboundNo(), getStatusName(outbound.getStatus())));
                        failureCount++;
                        continue;
                    }

                    // 调用发货方法
                    shipOutbound(outboundId, logisticsCompany, logisticsCode);
                    successCount++;

                } catch (Exception e) {
                    errors.add(String.format("第%d行：处理失败 - %s", i + 1, e.getMessage()));
                    failureCount++;
                    log.error("[MaterialOutboundService][导入发货信息] 第{}行处理失败", i + 1, e);
                }
            }

        } catch (Exception e) {
            log.error("[MaterialOutboundService][导入发货信息] Excel解析失败", e);
            throw new IOException("Excel解析失败: " + e.getMessage(), e);
        }

        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);

        log.info("[MaterialOutboundService][导入发货信息] 成功:{}, 失败:{}", successCount, failureCount);
        return result;
    }

}