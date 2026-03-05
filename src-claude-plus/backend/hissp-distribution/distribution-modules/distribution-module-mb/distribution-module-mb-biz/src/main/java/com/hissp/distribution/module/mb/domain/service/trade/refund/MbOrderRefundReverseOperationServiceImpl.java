package com.hissp.distribution.module.mb.domain.service.trade.refund;

import com.hissp.distribution.module.mb.domain.service.trade.refund.factory.RefundProcessorFactory;
import com.hissp.distribution.module.mb.domain.service.trade.refund.processor.AbstractRefundProcessor;
import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.trade.api.order.TradeOrderApi;
import com.hissp.distribution.module.trade.api.order.TradeOrderItemApi;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * MB业务退款逆操作服务实现类（使用模板设计模式重构）
 *
 * <p>本类已使用模板设计模式进行重构，提供更好的可维护性和可扩展性。</p>
 *
 * <h3>重构前后对比：</h3>
 * <ul>
 *   <li><strong>重构前</strong>：所有退款逻辑都在一个类中，通过 switch-case 分发到不同方法</li>
 *   <li><strong>重构后</strong>：使用模板模式，每种退款类型都有独立的处理器类</li>
 * </ul>
 *
 * <h3>模板设计模式架构：</h3>
 * <ul>
 *   <li><strong>抽象模板类</strong>：{@link com.hissp.distribution.module.mb.domain.service.trade.refund.processor.AbstractRefundProcessor}</li>
 *   <li><strong>具体实现类</strong>：各种退款类型的处理器（MaterialRestockRefundProcessor 等）</li>
 *   <li><strong>工厂类</strong>：{@link com.hissp.distribution.module.mb.domain.service.trade.refund.factory.RefundProcessorFactory}</li>
 *   <li><strong>主服务类</strong>：本类 - 负责退款类型识别和处理器调度</li>
 * </ul>
 *
 * <h3>主要职责：</h3>
 * <ul>
 *   <li>退款通知的统一入口处理</li>
 *   <li>退款类型识别和验证</li>
 *   <li>退款处理器的创建和调度</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * <h3>支持的退款类型：</h3>
 * <ul>
 *   <li><strong>物料补货退款</strong>：订单号前缀MR，回退物料库存和取消分佣</li>
 *   <li><strong>物料转化退款</strong>：订单号前缀MC，执行转化逆操作</li>
 *   <li><strong>MB普通产品退款</strong>：普通订单号，取消分佣和回退物料</li>
 *   <li><strong>MB代理商开通产品退款</strong>：代理商订单，回退等级、权限和物料奖励</li>
 * </ul>
 *
 * <h3>扩展说明：</h3>
 * <p>新增退款类型时，只需要：</p>
 * <ol>
 *   <li>在 MbRefundTypeEnum 中添加新的退款类型</li>
 *   <li>创建对应的处理器实现类</li>
 *   <li>在 RefundProcessorFactory 中添加创建逻辑</li>
 *   <li>无需修改本类的核心逻辑</li>
 * </ol>
 *
 * <h3>向后兼容性：</h3>
 * <p>重构后保持了原有的方法签名和返回值，确保对外接口不变。</p>
 *
 * @author system
 */
@Service
@Slf4j
public class MbOrderRefundReverseOperationServiceImpl implements MbOrderRefundReverseOperationService {

    @Resource
    private TradeOrderApi tradeOrderApi;

    @Resource
    private TradeOrderItemApi tradeOrderItemApi;

    @Resource
    private ProductSpuApi productSpuApi;

    @Resource
    private RefundProcessorFactory refundProcessorFactory;

    /**
     * 处理退款通知（主入口方法）- 使用模板设计模式重构
     *
     * <p>重构说明：</p>
     * <ul>
     *   <li>使用模板设计模式，将通用流程抽象到 {@link AbstractRefundProcessor} 中</li>
     *   <li>通过 {@link RefundProcessorFactory} 根据退款类型创建对应的处理器</li>
     *   <li>每种退款类型都有独立的处理器实现，便于维护和扩展</li>
     * </ul>
     *
     * <p>事务边界：此方法是整个退款处理流程的事务边界，所有内部方法都参与此事务</p>
     * <ul>
     *   <li>任何步骤失败都会导致整个退款处理回滚</li>
     *   <li>包括退款记录状态更新、订单状态更新、物料库存回退、分佣取消等操作</li>
     * </ul>
     *
     * <p>模板方法流程：</p>
     * <ol>
     *   <li>识别退款类型</li>
     *   <li>创建对应的退款处理器</li>
     *   <li>调用处理器的模板方法执行完整的退款处理流程</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefundNotification(PayRefundNotifyReqDTO notifyReqDTO) {
        log.info("[processRefundNotification][开始处理退款通知（使用模板模式）: {}]", notifyReqDTO);

        try {
            // 1. 识别退款类型
            MbRefundTypeEnum refundType = identifyRefundType(notifyReqDTO);

            log.info("[processRefundNotification][退款类型识别结果: merchantOrderId={}, refundType={}]",
                notifyReqDTO.getMerchantOrderId(), refundType);

            // 2. 如果不是MB相关退款，跳过处理
            if (refundType == MbRefundTypeEnum.UNKNOWN) {
                log.warn("[processRefundNotification][不是MB相关退款，跳过处理: {}]", notifyReqDTO.getMerchantOrderId());
                return false;
            }

            // 3. 检查是否支持该退款类型
            if (!refundProcessorFactory.isSupported(refundType)) {
                log.error("[processRefundNotification][不支持的退款类型: merchantOrderId={}, refundType={}]",
                    notifyReqDTO.getMerchantOrderId(), refundType);
                return false;
            }

            // 4. 创建对应的退款处理器
            AbstractRefundProcessor processor = refundProcessorFactory.createProcessor(refundType);

            log.info("[processRefundNotification][创建退款处理器成功: merchantOrderId={}, refundType={}, processorClass={}]",
                notifyReqDTO.getMerchantOrderId(), refundType, processor.getClass().getSimpleName());

            // 5. 使用模板方法执行退款处理
            boolean result = processor.processRefund(notifyReqDTO);

            if (result) {
                log.info("[processRefundNotification][退款处理成功: merchantOrderId={}, refundType={}]",
                    notifyReqDTO.getMerchantOrderId(), refundType);
            } else {
                log.warn("[processRefundNotification][退款处理失败: merchantOrderId={}, refundType={}]",
                    notifyReqDTO.getMerchantOrderId(), refundType);
            }

            return result;

        } catch (Exception e) {
            log.error("[processRefundNotification][处理退款通知异常: {}]", notifyReqDTO, e);
            throw e;
        }
    }

    // ==================== 已删除的老代码 ====================
    // 以下方法已经通过模板设计模式重构，移动到各个具体的处理器类中：
    // - handleMaterialRestockRefundReverse() -> MaterialRestockRefundProcessor
    // - handleMaterialConvertRefundReverse() -> CollagenConvertRefundProcessor
    // - handleMbNormalProductRefundReverse() -> MbNormalProductRefundProcessor
    // - handleMbCareerProductRefundReverse() -> MbCareerProductRefundProcessor
    // - executeRestockRefundReverse() -> MaterialRestockRefundProcessor
    // - executeCollagenConvertRefundReverse() -> CollagenConvertRefundProcessor
    // - revertMaterialStockForNormalProduct() -> MbNormalProductRefundProcessor
    // - cancelBrokerageForOrder() -> 各个处理器类中
    // - revertUserLevel() -> MbCareerProductRefundProcessor
    // - revokeAgentPermissions() -> MbCareerProductRefundProcessor
    // - revertCareerProductMaterialRewards() -> MbCareerProductRefundProcessor
    // - revertMaterialBenefits() -> MbCareerProductRefundProcessor
    // - validateUserMaterialBalance() -> MaterialRestockRefundProcessor
    // - 退款记录管理相关方法 -> AbstractRefundProcessor

    // ==================== 退款类型识别功能（模板模式重构后保留的通用方法） ====================

    /**
     * 识别退款类型
     */
    private MbRefundTypeEnum identifyRefundType(PayRefundNotifyReqDTO notifyReqDTO) {
        String merchantOrderId = notifyReqDTO.getMerchantOrderId();

        if (merchantOrderId == null || merchantOrderId.trim().isEmpty()) {
            return MbRefundTypeEnum.UNKNOWN;
        }

        // 1. 首先通过订单号前缀判断
        MbRefundTypeEnum typeByPrefix = MbRefundTypeEnum.getRefundType(merchantOrderId);
        if (typeByPrefix != MbRefundTypeEnum.UNKNOWN) {
            return typeByPrefix;
        }

        // 2. 如果前缀无法判断，通过订单信息判断
        try {
            Long orderId = Long.parseLong(merchantOrderId);
            return identifyRefundTypeByOrder(orderId);
        } catch (NumberFormatException e) {
            log.warn("[identifyRefundType][无法解析订单ID: {}]", merchantOrderId);
            return MbRefundTypeEnum.UNKNOWN;
        }
    }

    /**
     * 通过订单信息识别退款类型
     */
    private MbRefundTypeEnum identifyRefundTypeByOrder(Long orderId) {
        try {
            // 查询订单信息
            TradeOrderRespDTO order = tradeOrderApi.getOrder(orderId);
            if (order == null) {
                log.warn("[identifyRefundTypeByOrder][订单不存在: orderId={}]", orderId);
                return MbRefundTypeEnum.UNKNOWN;
            }

            // 查询订单项
            List<TradeOrderItemRespDTO> orderItems = tradeOrderItemApi.getOrderItemListByOrderId(orderId);
            if (orderItems == null || orderItems.isEmpty()) {
                log.warn("[identifyRefundTypeByOrder][订单项不存在: orderId={}]", orderId);
                return MbRefundTypeEnum.UNKNOWN;
            }

            // 获取所有SPU ID
            List<Long> spuIds = orderItems.stream()
                .map(TradeOrderItemRespDTO::getSpuId)
                .toList();

            // 通过ProductSpuApi判断产品类型
            boolean hasCareerProduct = productSpuApi.isCareerProduct(spuIds);
            boolean hasMbProduct = productSpuApi.isMbProduct(spuIds);

            if (hasCareerProduct) {
                return MbRefundTypeEnum.MB_CAREER_PRODUCT;
            } else if (hasMbProduct) {
                return MbRefundTypeEnum.MB_NORMAL_PRODUCT;
            } else {
                return MbRefundTypeEnum.UNKNOWN;
            }

        } catch (Exception e) {
            log.error("[identifyRefundTypeByOrder][识别退款类型异常: orderId={}]", orderId, e);
            return MbRefundTypeEnum.UNKNOWN;
        }
    }
}
