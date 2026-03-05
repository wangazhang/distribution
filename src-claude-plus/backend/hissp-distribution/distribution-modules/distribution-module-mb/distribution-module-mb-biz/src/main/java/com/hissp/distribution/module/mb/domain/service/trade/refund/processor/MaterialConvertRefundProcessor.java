package com.hissp.distribution.module.mb.domain.service.trade.refund.processor;

import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbRefundRecordMapper;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbOrderMapper;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
import com.hissp.distribution.module.mb.enums.MbOrderStatusEnum;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物料转化退款处理器
 *
 * <p>处理物料转化订单（订单号前缀为MC）的退款逆操作，主要包括：</p>
 * <ul>
 *   <li>验证转化订单的有效性和状态</li>
 *   <li>回退原料物料库存（增加之前减少的原料）</li>
 *   <li>减少物料库存（回退之前增加的产品）</li>
 *   <li>更新订单状态</li>
 * </ul>
 *
 * <h3>处理逻辑说明：</h3>
 * <p>物料转化订单退款时需要执行以下逆操作：</p>
 * <ol>
 *   <li><strong>原料回退</strong>：增加用户在转化时消耗的原料物料库存</li>
 *   <li><strong>产品回退</strong>：减少用户在转化时获得的物料库存</li>
 *   <li><strong>状态更新</strong>：将订单状态更新为已退款</li>
 * </ol>
 *
 * <h3>转化逆操作示例：</h3>
 * <p>假设原始转化操作是：消耗10个原料A，获得5个物料B</p>
 * <p>那么退款逆操作就是：增加10个原料A，减少5个物料B</p>
 *
 * <h3>异常处理策略：</h3>
 * <ul>
 *   <li><strong>库存不足</strong>：当用户当前库存不足以回退时，记录错误但返回成功，避免阻塞退款流程</li>
 *   <li><strong>订单状态异常</strong>：如果订单状态不允许退款，直接返回失败</li>
 *   <li><strong>重复处理</strong>：如果订单已经处理过退款，直接返回成功</li>
 * </ul>
 *
 * @author system
 */
@Slf4j
@Component
public class MaterialConvertRefundProcessor extends AbstractRefundProcessor {

    private final MbOrderMapper mbOrderMapper;
    private final MaterialApi materialApi;

    /**
     * 构造函数 - 注入必要的依赖
     */
    public MaterialConvertRefundProcessor(
            MbRefundRecordMapper mbRefundRecordMapper,
            MbOrderMapper mbOrderMapper,
            MaterialApi materialApi) {
        super(mbRefundRecordMapper);
        this.mbOrderMapper = mbOrderMapper;
        this.materialApi = materialApi;
    }

    /**
     * 验证物料转化退款请求
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-验证通过，false-验证失败
     */
    @Override
    protected boolean validateRefundRequest(PayRefundNotifyReqDTO notifyReqDTO) {
        String merchantOrderId = notifyReqDTO.getMerchantOrderId();

        try {
            // 1. 检查订单号格式（必须以MC开头）
            if (!merchantOrderId.startsWith("MC")) {
                log.error("[MaterialConvertRefundProcessor][订单号格式错误，不是物料转化订单: {}]", merchantOrderId);
                return false;
            }

            // 2. 解析订单ID
            Long orderId = Long.parseLong(merchantOrderId.substring(2));

            // 3. 查询物料转化订单
            MbOrderDO order = mbOrderMapper.selectById(orderId);
            if (order == null) {
                log.error("[MaterialConvertRefundProcessor][物料转化订单不存在: orderId={}]", orderId);
                return false;
            }

            // 4. 检查订单状态
            MbOrderStatusEnum currentStatus = MbOrderStatusEnum.fromCode(order.getStatus());

            // 如果已经处理过退款，返回true（幂等性）
            if (currentStatus.isRefunded()) {
                log.info("[MaterialConvertRefundProcessor][物料转化订单已处理退款逆操作: orderId={}, status={}]",
                    orderId, currentStatus);
                return true;
            }

            // 检查是否可以退款
            if (!currentStatus.canRefund()) {
                log.warn("[MaterialConvertRefundProcessor][物料转化订单状态不允许退款: orderId={}, status={}]",
                    orderId, currentStatus);
                return false;
            }

            log.info("[MaterialConvertRefundProcessor][物料转化退款请求验证通过: orderId={}, userId={}, rawMaterialId={}, quantity={}]",
                orderId, order.getAgentUserId(), order.getProductId(), order.getQuantity());

            return true;

        } catch (NumberFormatException e) {
            log.error("[MaterialConvertRefundProcessor][解析订单ID失败: {}]", merchantOrderId, e);
            return false;
        } catch (Exception e) {
            log.error("[MaterialConvertRefundProcessor][验证退款请求异常: {}]", merchantOrderId, e);
            return false;
        }
    }

    /**
     * 执行物料转化退款逆操作
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

            // 2. 查询物料转化订单
            MbOrderDO order = mbOrderMapper.selectById(orderId);
            if (order == null) {
                log.error("[MaterialConvertRefundProcessor][物料转化订单不存在: orderId={}]", orderId);
                return false;
            }

            // 3. 更新订单状态为退款中
            order.setStatus(MbOrderStatusEnum.REFUNDING.getCode());
            mbOrderMapper.updateById(order);

            log.info("[MaterialConvertRefundProcessor][开始执行物料转化退款逆操作: orderId={}, userId={}, rawMaterialId={}, quantity={}]",
                orderId, order.getAgentUserId(), order.getProductId(), order.getQuantity());

            // 4. 执行逆操作
            boolean result = executeMaterialConvertRefundReverse(order);

            // 5. 更新最终状态
            if (result) {
                order.setStatus(MbOrderStatusEnum.REFUNDED.getCode());
                mbOrderMapper.updateById(order);
                log.info("[MaterialConvertRefundProcessor][物料转化退款逆操作成功: orderId={}]", orderId);
            } else {
                order.setStatus(MbOrderStatusEnum.REFUND_FAILED.getCode());
                mbOrderMapper.updateById(order);
                log.error("[MaterialConvertRefundProcessor][物料转化退款逆操作失败: orderId={}]", orderId);
            }

            return result;

        } catch (Exception e) {
            log.error("[MaterialConvertRefundProcessor][执行物料转化退款逆操作异常: {}]", merchantOrderId, e);
            throw e;
        }
    }

    /**
     * 获取退款类型
     *
     * @return 物料转化退款类型
     */
    @Override
    protected MbRefundTypeEnum getRefundType() {
        return MbRefundTypeEnum.MATERIAL_CONVERT;
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
            log.error("[MaterialConvertRefundProcessor][获取用户ID异常: merchantOrderId={}]",
                notifyReqDTO.getMerchantOrderId(), e);
            return null;
        }
    }

    /**
     * 执行物料转化订单退款逆操作的核心逻辑
     *
     * <p>转化逆操作步骤：</p>
     * <ol>
     *   <li><strong>增加原料物料库存</strong>：回退之前减少的原料</li>
     *   <li><strong>减少物料库存</strong>：回退之前增加的产品</li>
     * </ol>
     *
     * <p>操作示例：</p>
     * <ul>
     *   <li>原始转化：消耗10个原料A → 获得5个物料B</li>
     *   <li>退款逆操作：增加10个原料A，减少5个物料B</li>
     * </ul>
     *
     * @param order 物料转化订单
     * @return true-执行成功，false-执行失败
     */
    private boolean executeMaterialConvertRefundReverse(MbOrderDO order) {
        try {
            log.info("[executeMaterialConvertRefundReverse][开始执行物料转化退款逆操作: orderId={}, userId={}, rawMaterialId={}, quantity={}]",
                order.getId(), order.getAgentUserId(), order.getProductId(), order.getQuantity());

            MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
            Long productMaterialId = definition != null ? definition.getConvertedSpuId() : null;
            if (productMaterialId == null) {
                log.warn("[executeMaterialConvertRefundReverse][物料({})未配置转化目标，跳过产品库存回退]", order.getProductId());
            }

            // 1. 增加原料物料库存（回退之前减少的原料）
            MaterialActDTO increaseRawMaterialDTO = MaterialActDTO.builder()
                .userId(order.getAgentUserId())
                .materialId(order.getProductId()) // 原料物料ID
                .quantity(order.getQuantity())
                .direction(MaterialActDirectionEnum.IN) // 增加操作
                .bizKey("MATERIAL_CONVERT_REFUND_RAW_" + order.getId())
                .bizType("MATERIAL_CONVERT_REFUND")
                .reason("物料转化订单退款-回退原料: 订单" + order.getId() + ", 原料物料" + order.getProductId())
                .build();

            MaterialActDTO decreaseProductDTO = null;
            if (productMaterialId != null) {
                decreaseProductDTO = MaterialActDTO.builder()
                        .userId(order.getAgentUserId())
                        .materialId(productMaterialId)
                        .quantity(order.getQuantity())
                        .direction(MaterialActDirectionEnum.OUT)
                        .bizKey("MATERIAL_CONVERT_REFUND_PRODUCT_" + order.getId())
                        .bizType("MATERIAL_CONVERT_REFUND")
                        .reason("物料转化订单退款-回退产品: 订单" + order.getId() + ", 产品物料" + productMaterialId)
                        .build();
            }

            if (decreaseProductDTO != null) {
                materialApi.applyActs(java.util.List.of(increaseRawMaterialDTO, decreaseProductDTO));
            } else {
                materialApi.applyActs(java.util.List.of(increaseRawMaterialDTO));
            }

            log.info("[executeMaterialConvertRefundReverse][成功回退原料物料库存: orderId={}, userId={}, rawMaterialId={}, quantity={}]",
                order.getId(), order.getAgentUserId(), order.getProductId(), order.getQuantity());
            if (productMaterialId != null) {
                log.info("[executeMaterialConvertRefundReverse][成功回退物料库存: orderId={}, userId={}, productId={}, quantity={}]",
                        order.getId(), order.getAgentUserId(), productMaterialId, order.getQuantity());
            }

            log.info("[executeMaterialConvertRefundReverse][物料转化退款逆操作执行成功: orderId={}]", order.getId());
            return true;

        } catch (ServiceException e) {
            log.error("[executeMaterialConvertRefundReverse][物料库存不足，无法回退: orderId={}, error={}]", order.getId(), e.getMessage());

            // 库存不足时的处理策略：
            // 1. 记录详细的异常信息供后续人工处理
            // 2. 返回成功以避免阻塞退款流程
            log.warn("[executeMaterialConvertRefundReverse][库存不足但继续处理，建议人工核查: orderId={}, userId={}, rawMaterialId={}, 需要回退数量={}, 错误信息={}]",
                order.getId(), order.getAgentUserId(), order.getProductId(), order.getQuantity(), e.getMessage());

            return true; // 返回成功，避免阻塞退款流程
        } catch (Exception e) {
            log.error("[executeMaterialConvertRefundReverse][执行物料转化订单退款逆操作异常: orderId={}]", order.getId(), e);
            throw e;
        }
    }

    /**
     * 后置处理钩子方法
     *
     * <p>物料转化退款的特殊后置处理逻辑</p>
     *
     * @param notifyReqDTO 退款通知请求数据
     * @param success 处理是否成功
     */
    @Override
    protected void postProcessing(PayRefundNotifyReqDTO notifyReqDTO, boolean success) {
        super.postProcessing(notifyReqDTO, success);

        if (success) {
            log.info("[MaterialConvertRefundProcessor][物料转化退款后置处理: merchantOrderId={}, 转化逆操作已完成]",
                notifyReqDTO.getMerchantOrderId());

            // 这里可以添加特殊的后置处理逻辑，例如：
            // 1. 发送转化退款通知
            // 2. 更新转化统计数据
            // 3. 记录转化审计日志
        } else {
            log.warn("[MaterialConvertRefundProcessor][物料转化退款后置处理: merchantOrderId={}, 转化逆操作失败，可能需要人工介入]",
                notifyReqDTO.getMerchantOrderId());
        }
    }
}
