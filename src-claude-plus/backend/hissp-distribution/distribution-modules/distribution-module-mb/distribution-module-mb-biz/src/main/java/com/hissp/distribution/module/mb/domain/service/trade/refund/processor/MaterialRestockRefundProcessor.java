package com.hissp.distribution.module.mb.domain.service.trade.refund.processor;

import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.api.commission.CommissionStrategyApi;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionBizTypeConstants;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateReqDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateRespDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionMaterialGrantDTO;
import com.hissp.distribution.module.mb.api.commission.util.CommissionMaterialGrantHelper;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbOrderMapper;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbRefundRecordMapper;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionVirtualAccounts;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler.RestockCommissionHandler;
import com.hissp.distribution.module.mb.enums.MbOrderStatusEnum;
import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 物料补货退款处理器
 *
 * <p>处理物料补货订单（订单号前缀为MR）的退款逆操作，主要包括：</p>
 * <ul>
 *   <li>验证补货订单的有效性和状态</li>
 *   <li>回退用户物料库存（减少之前增加的库存）</li>
 *   <li>取消相关的分佣记录</li>
 *   <li>更新订单状态</li>
 * </ul>
 *
 * <h3>处理逻辑说明：</h3>
 * <p>物料补货订单退款时需要执行以下逆操作：</p>
 * <ol>
 *   <li><strong>库存回退</strong>：减少用户在补货时增加的物料库存</li>
 *   <li><strong>分佣取消</strong>：取消补货订单产生的分佣记录</li>
 *   <li><strong>状态更新</strong>：将订单状态更新为已退款</li>
 * </ol>
 *
 * <h3>异常处理策略：</h3>
 * <ul>
 *   <li><strong>库存不足</strong>：当用户当前库存不足以回退时，仍然取消分佣记录，避免用户获得不当收益</li>
 *   <li><strong>订单状态异常</strong>：如果订单状态不允许退款，直接返回失败</li>
 *   <li><strong>重复处理</strong>：如果订单已经处理过退款，直接返回成功</li>
 * </ul>
 *
 * @author system
 */
@Slf4j
@Component
public class MaterialRestockRefundProcessor extends AbstractRefundProcessor {

    private final MbOrderMapper mbOrderMapper;
    private final MaterialApi materialApi;
    private final BrokerageRecordApi brokerageRecordApi;
    private final BrokerageUserApi brokerageUserApi;
    private final CommissionStrategyApi commissionStrategyApi;
    private final RestockCommissionHandler restockCommissionHandler;

    /**
     * 构造函数 - 注入必要的依赖
     */
    public MaterialRestockRefundProcessor(
            MbRefundRecordMapper mbRefundRecordMapper,
            MbOrderMapper mbOrderMapper,
            MaterialApi materialApi,
            BrokerageRecordApi brokerageRecordApi,
            BrokerageUserApi brokerageUserApi,
            CommissionStrategyApi commissionStrategyApi,
            RestockCommissionHandler restockCommissionHandler) {
        super(mbRefundRecordMapper);
        this.mbOrderMapper = mbOrderMapper;
        this.materialApi = materialApi;
        this.brokerageRecordApi = brokerageRecordApi;
        this.brokerageUserApi = brokerageUserApi;
        this.commissionStrategyApi = commissionStrategyApi;
        this.restockCommissionHandler = restockCommissionHandler;
    }

    /**
     * 验证物料补货退款请求
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-验证通过，false-验证失败
     */
    @Override
    protected boolean validateRefundRequest(PayRefundNotifyReqDTO notifyReqDTO) {
        String merchantOrderId = notifyReqDTO.getMerchantOrderId();

        try {
            // 1. 检查订单号格式（必须以MR开头）
            if (!merchantOrderId.startsWith("MR")) {
                log.error("[MaterialRestockRefundProcessor][订单号格式错误，不是物料补货订单: {}]", merchantOrderId);
                return false;
            }

            // 2. 解析订单ID
            Long orderId = Long.parseLong(merchantOrderId.substring(2));

            // 3. 查询物料补货订单
            MbOrderDO order = mbOrderMapper.selectById(orderId);
            if (order == null) {
                log.error("[MaterialRestockRefundProcessor][物料补货订单不存在: orderId={}]", orderId);
                return false;
            }

            // 4. 检查订单状态
            MbOrderStatusEnum currentStatus = MbOrderStatusEnum.fromCode(order.getStatus());

            // 如果已经处理过退款，返回true（幂等性）
            if (currentStatus.isRefunded()) {
                log.info("[MaterialRestockRefundProcessor][物料补货订单已处理退款逆操作: orderId={}, status={}]",
                    orderId, currentStatus);
                return true;
            }

            // 检查是否可以退款
            if (!currentStatus.canRefund()) {
                log.warn("[MaterialRestockRefundProcessor][物料补货订单状态不允许退款: orderId={}, status={}]",
                    orderId, currentStatus);
                return false;
            }

            log.info("[MaterialRestockRefundProcessor][物料补货退款请求验证通过: orderId={}, userId={}, productSpuId={}, quantity={}]",
                orderId, order.getAgentUserId(), order.getProductId(), order.getQuantity());

            return true;

        } catch (NumberFormatException e) {
            log.error("[MaterialRestockRefundProcessor][解析订单ID失败: {}]", merchantOrderId, e);
            return false;
        } catch (Exception e) {
            log.error("[MaterialRestockRefundProcessor][验证退款请求异常: {}]", merchantOrderId, e);
            return false;
        }
    }

    /**
     * 执行物料补货退款逆操作
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-执行成功，false-执行失败
     */
    @Override
    protected boolean executeRefundReverse(PayRefundNotifyReqDTO notifyReqDTO) {
        String merchantOrderId = notifyReqDTO.getMerchantOrderId();

        try {
            // 1. 解析订单ID
            Long orderId = Long.parseLong(merchantOrderId.substring(2));

            // 2. 查询物料补货订单
            MbOrderDO order = mbOrderMapper.selectById(orderId);
            if (order == null) {
                log.error("[MaterialRestockRefundProcessor][物料补货订单不存在: orderId={}]", orderId);
                return false;
            }

            // 3. 更新订单状态为退款中
            order.setStatus(MbOrderStatusEnum.REFUNDING.getCode());
            mbOrderMapper.updateById(order);

            log.info("[MaterialRestockRefundProcessor][开始执行物料补货退款逆操作: orderId={}, userId={}, productSpuId={}, quantity={}]",
                orderId, order.getAgentUserId(), order.getProductId(), order.getQuantity());

            // 4. 执行逆操作
            boolean result = executeRestockRefundReverse(order);

            // 5. 更新最终状态
            if (result) {
                order.setStatus(MbOrderStatusEnum.REFUNDED.getCode());
                mbOrderMapper.updateById(order);
                log.info("[MaterialRestockRefundProcessor][物料补货退款逆操作成功: orderId={}]", orderId);
            } else {
                order.setStatus(MbOrderStatusEnum.REFUND_FAILED.getCode());
                mbOrderMapper.updateById(order);
                log.error("[MaterialRestockRefundProcessor][物料补货退款逆操作失败: orderId={}]", orderId);
            }

            return result;

        } catch (Exception e) {
            log.error("[MaterialRestockRefundProcessor][执行物料补货退款逆操作异常: {}]", merchantOrderId, e);
            throw e;
        }
    }

    /**
     * 获取退款类型
     *
     * @return 物料补货退款类型
     */
    @Override
    protected MbRefundTypeEnum getRefundType() {
        return MbRefundTypeEnum.MATERIAL_RESTOCK;
    }

    /**
     * 获取用户ID
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return 代理用户ID
     */
    @Override
    protected Long getUserId(PayRefundNotifyReqDTO notifyReqDTO) {
        try {
            String merchantOrderId = notifyReqDTO.getMerchantOrderId();
            Long orderId = Long.parseLong(merchantOrderId.substring(2));
            MbOrderDO order = mbOrderMapper.selectById(orderId);
            return order != null ? order.getAgentUserId() : null;
        } catch (Exception e) {
            log.error("[MaterialRestockRefundProcessor][获取用户ID异常: merchantOrderId={}]",
                notifyReqDTO.getMerchantOrderId(), e);
            return null;
        }
    }

    /**
     * 执行物料补货订单退款逆操作的核心逻辑
     *
     * <p>操作步骤：</p>
     * <ol>
     *   <li>验证用户当前物料库存是否足够回退（可选验证）</li>
     *   <li>减少用户物料库存（回退之前增加的库存）</li>
     *   <li>取消相关分佣记录</li>
     * </ol>
     *
     * @param order 物料补货订单
     * @return true-执行成功，false-执行失败
     */
    private boolean executeRestockRefundReverse(MbOrderDO order) {
        try {
            log.info("[executeRestockRefundReverse][开始执行补货退款逆操作: orderId={}, userId={}, productSpuId={}, quantity={}]",
                order.getId(), order.getAgentUserId(), order.getProductId(), order.getQuantity());

            // 先归还策略发放的物料，避免与后续库存回退产生时间差
            revertStrategyMaterialGrants(order);

            MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
            if (definition == null) {
                log.warn("[executeRestockRefundReverse][未找到补货物料定义，跳过库存回退: orderId={}, productSpuId={}]", order.getId(), order.getProductId());
                cancelBrokerageForOrder(order.getId());
                return true;
            }

            // 1. 验证用户当前物料库存（可选验证，根据业务需求决定）
            validateUserMaterialBalance(order);

            // 2. 减少用户物料库存（回退之前增加的库存），并归还出资方库存
            List<MaterialActDTO> acts = new ArrayList<>();
            acts.add(MaterialActDTO.builder()
                .userId(order.getAgentUserId())
                .materialId(definition.getId())
                .quantity(order.getQuantity())
                .direction(MaterialActDirectionEnum.OUT) // 减少操作
                .bizKey("RESTOCK_REFUND_" + order.getId())
                .bizType("RESTOCK_REFUND")
                .reason("物料补货订单退款-回退库存: 订单" + order.getId() + ", 物料定义" + definition.getId())
                .build());

            Long fundingUserId = resolveFundingUserId(order, definition.getId(), order.getQuantity());
            if (fundingUserId != null && !Objects.equals(fundingUserId, order.getAgentUserId())) {
                acts.add(MaterialActDTO.builder()
                        .userId(fundingUserId)
                        .materialId(definition.getId())
                        .quantity(order.getQuantity())
                        .direction(MaterialActDirectionEnum.IN)
                        .bizKey("RESTOCK_REFUND_" + order.getId() + "_FUND")
                        .bizType("RESTOCK_REFUND_FUND")
                        .reason("物料补货订单退款-归还出资库存: 订单" + order.getId())
                        .build());
            }

            materialApi.applyActs(acts);

            log.info("[executeRestockRefundReverse][成功回退物料库存: orderId={}, userId={}, materialDefinitionId={}, quantity={}, fundingUserId={}]",
                order.getId(), order.getAgentUserId(), definition.getId(), order.getQuantity(), fundingUserId);

            // 3. 取消相关分佣记录
            cancelBrokerageForOrder(order.getId());

            log.info("[executeRestockRefundReverse][补货退款逆操作执行成功: orderId={}]", order.getId());
            return true;

        } catch (ServiceException e) {
            log.error("[executeRestockRefundReverse][物料库存不足，无法完全回退: orderId={}, error={}]", order.getId(), e.getMessage());

            // 库存不足时的处理策略：
            // 1. 仍然取消分佣记录（避免用户获得不当收益）
            // 2. 记录详细的异常信息供后续人工处理
            // 3. 返回成功以避免阻塞退款流程
            try {
                cancelBrokerageForOrder(order.getId());
                log.warn("[executeRestockRefundReverse][库存不足但已取消分佣记录，建议人工核查: orderId={}, userId={}, productSpuId={}, 需要回退数量={}, 错误信息={}]",
                    order.getId(), order.getAgentUserId(), order.getProductId(), order.getQuantity(), e.getMessage());
            } catch (Exception ex) {
                log.error("[executeRestockRefundReverse][取消分佣记录也失败: orderId={}]", order.getId(), ex);
            }

            return true; // 返回成功，避免阻塞退款流程
        } catch (Exception e) {
            log.error("[executeRestockRefundReverse][执行物料补货订单退款逆操作异常: orderId={}]", order.getId(), e);
            throw e;
        }
    }

    /**
     * 取消订单相关的分佣记录
     *
     * <p>分佣回退策略说明：</p>
     * <ul>
     *   <li>待结算状态(WAIT_SETTLEMENT)：从用户冻结余额中扣除</li>
     *   <li>已结算状态(SETTLEMENT)：从用户可用余额中扣除</li>
     *   <li>已取消状态(CANCEL)：跳过处理</li>
     * </ul>
     *
     * @param orderId 订单ID
     */
    private void cancelBrokerageForOrder(Long orderId) {
        try {
            log.info("[cancelBrokerageForOrder][开始取消订单相关的分佣记录: orderId={}]", orderId);

            // 直接使用订单ID作为bizId，分佣服务内部会根据bizId查找所有相关分佣记录并取消
            brokerageRecordApi.cancelBrokerage(String.valueOf(orderId));

            log.info("[cancelBrokerageForOrder][成功取消订单相关的所有分佣记录: orderId={}]", orderId);
            log.info("[cancelBrokerageForOrder][分佣回退已根据记录状态自动处理: 待结算->减少冻结余额, 已结算->减少可用余额]");

        } catch (Exception e) {
            log.error("[cancelBrokerageForOrder][取消订单分佣异常: orderId={}]", orderId, e);
            // 分佣取消失败不影响主流程，记录错误日志即可
        }
    }

    /**
     * 验证用户物料库存是否足够回退
     *
     * @param order 补货订单
     */
    private void validateUserMaterialBalance(MbOrderDO order) {
        log.debug("[validateUserMaterialBalance][验证用户物料库存: orderId={}, userId={}, productSpuId={}, 需要回退数量={}]",
            order.getId(), order.getAgentUserId(), order.getProductId(), order.getQuantity());
    }

    private Long resolveFundingUserId(MbOrderDO order, Long materialDefinitionId, Integer quantity) {
        BrokerageUserRespDTO bossUser = brokerageUserApi.getFirstBossBrokerageUser(order.getAgentUserId());
        Long candidate = null;
        if (bossUser != null && Boolean.TRUE.equals(bossUser.getBrokerageEnabled())) {
            candidate = bossUser.getId();
        }
        if (candidate != null && Objects.equals(candidate, order.getAgentUserId())) {
            candidate = null;
        }
        if (candidate != null) {
            MaterialBalanceRespDTO balance = materialApi.getBalance(candidate, materialDefinitionId);
            int available = balance != null ? Optional.ofNullable(balance.getAvailableBalance()).orElse(0) : 0;
            if (available >= Optional.ofNullable(quantity).orElse(0)) {
                return candidate;
            }
            log.warn("[resolveFundingUserId][退款归还时出资方库存不足，使用总部资金池: orderId={} sponsor={} need={} available={}]",
                    order.getId(), candidate, quantity, available);
        }
        // 总部资金池可透支，直接归还至总部虚拟账号。
        log.info("[resolveFundingUserId][退款归还落入总部资金池，可能出现负库存: orderId={} need={}]", order.getId(), quantity);
        return CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID;
    }

    /**
     * 依据策略重新计算物料奖励并回退，确保退款时物料与佣金同步撤销。
     */
    private void revertStrategyMaterialGrants(MbOrderDO order) {
        try {
            String bizType = order.getBizType();
            if (bizType == null || bizType.isEmpty()) {
                bizType = CommissionBizTypeConstants.RESTOCK;
            }
            CommissionCalculateReqDTO reqDTO = restockCommissionHandler.buildCommissionRequest(order, bizType);
            CommissionCalculateRespDTO respDTO = commissionStrategyApi.calculateCommission(reqDTO);
            List<MaterialActDTO> acts = CommissionMaterialGrantHelper.buildActs(
                respDTO.getMaterialGrants(),
                MaterialActDirectionEnum.OUT,
                order.getId() != null ? "REFUND-" + order.getId() : "REFUND",
                "补货分佣物料退回");
            if (!acts.isEmpty()) {
                materialApi.applyActs(acts);
            }
        } catch (Exception ex) {
            log.error("[revertStrategyMaterialGrants][补货退款回退物料失败: orderId={}]", order.getId(), ex);
        }
    }
}
