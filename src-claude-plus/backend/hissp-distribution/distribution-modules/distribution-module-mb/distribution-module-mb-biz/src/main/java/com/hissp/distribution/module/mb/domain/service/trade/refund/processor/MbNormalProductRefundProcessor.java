//package com.hissp.distribution.module.mb.domain.service.trade.refund.processor;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.hissp.distribution.module.mb.dal.mysql.trade.MbRefundRecordMapper;
//import com.hissp.distribution.module.material.api.MaterialApi;
//import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
//import com.hissp.distribution.module.material.api.dto.MaterialTxnPageReqDTO;
//import com.hissp.distribution.module.material.api.dto.MaterialTxnRespDTO;
//import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
//import com.hissp.distribution.framework.common.pojo.PageResult;
//import com.hissp.distribution.module.mb.constants.MbConstants;
//import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
//import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
//import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
//import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
//import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
//import com.hissp.distribution.module.trade.api.order.TradeOrderApi;
//import com.hissp.distribution.module.trade.api.order.TradeOrderItemApi;
//import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
//import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * MB普通产品退款处理器
// *
// * <p>处理MB普通产品购买订单的退款逆操作，主要包括：</p>
// * <ul>
// *   <li>验证订单的有效性和产品类型</li>
// *   <li>取消相关的分佣记录</li>
// *   <li>回退上级代理商的物料库存（如果有物料扣减）</li>
// * </ul>
// *
// * <h3>处理逻辑说明：</h3>
// * <p>MB普通产品退款时需要执行以下逆操作：</p>
// * <ol>
// *   <li><strong>分佣取消</strong>：取消订单产生的所有分佣记录</li>
// *   <li><strong>物料库存回退</strong>：如果购买时扣减了上级代理的物料库存，需要回退</li>
// * </ol>
// *
// * <h3>物料库存回退逻辑：</h3>
// * <ul>
// *   <li><strong>代理商购买</strong>：代理商购买普通产品时不扣减上级物料，无需回退</li>
// *   <li><strong>普通用户购买</strong>：普通用户购买时扣减了上级代理的物料库存，需要回退给上级代理</li>
// * </ul>
// *
// * <h3>异常处理策略：</h3>
// * <ul>
// *   <li><strong>订单不存在</strong>：直接返回失败</li>
// *   <li><strong>产品类型不匹配</strong>：直接返回失败</li>
// *   <li><strong>物料回退失败</strong>：记录错误但不影响主流程</li>
// *   <li><strong>分佣取消失败</strong>：记录错误但不影响主流程</li>
// * </ul>
// *
// * @author system
// */
//@Slf4j
//@Component
//public class MbNormalProductRefundProcessor extends AbstractRefundProcessor {
//
//    private final TradeOrderApi tradeOrderApi;
//    private final TradeOrderItemApi tradeOrderItemApi;
//    private final ProductSpuApi productSpuApi;
//    private final BrokerageRecordApi brokerageRecordApi;
//    private final BrokerageUserApi brokerageUserApi;
//    private final MaterialApi materialApi;
//
//    /**
//     * 构造函数 - 注入必要的依赖
//     */
//    public MbNormalProductRefundProcessor(
//            MbRefundRecordMapper mbRefundRecordMapper,
//            TradeOrderApi tradeOrderApi,
//            TradeOrderItemApi tradeOrderItemApi,
//            ProductSpuApi productSpuApi,
//            BrokerageRecordApi brokerageRecordApi,
//            BrokerageUserApi brokerageUserApi,
//            MaterialApi materialApi) {
//        super(mbRefundRecordMapper);
//        this.tradeOrderApi = tradeOrderApi;
//        this.tradeOrderItemApi = tradeOrderItemApi;
//        this.productSpuApi = productSpuApi;
//        this.brokerageRecordApi = brokerageRecordApi;
//        this.brokerageUserApi = brokerageUserApi;
//        this.materialApi = materialApi;
//    }
//
//    /**
//     * 验证MB普通产品退款请求
//     *
//     * @param notifyReqDTO 退款通知请求数据
//     * @return true-验证通过，false-验证失败
//     */
//    @Override
//    protected boolean validateRefundRequest(PayRefundNotifyReqDTO notifyReqDTO) {
//        String merchantOrderId = notifyReqDTO.getMerchantOrderId();
//
//        try {
//            // 1. 解析订单ID
//            Long orderId = Long.parseLong(merchantOrderId);
//
//            // 2. 查询订单信息
//            TradeOrderRespDTO order = tradeOrderApi.getOrder(orderId);
//            if (order == null) {
//                log.error("[MbNormalProductRefundProcessor][订单不存在: orderId={}]", orderId);
//                return false;
//            }
//
//            // 3. 查询订单项
//            List<TradeOrderItemRespDTO> orderItems = tradeOrderItemApi.getOrderItemListByOrderId(orderId);
//            if (orderItems == null || orderItems.isEmpty()) {
//                log.error("[MbNormalProductRefundProcessor][订单项不存在: orderId={}]", orderId);
//                return false;
//            }
//
//            // 4. 验证是否为MB普通产品
//            List<Long> spuIds = orderItems.stream()
//                .map(TradeOrderItemRespDTO::getSpuId)
//                .toList();
//
//            boolean hasMbProduct = productSpuApi.isMbProduct(spuIds);
//            boolean hasCareerProduct = productSpuApi.isCareerProduct(spuIds);
//
//            if (!hasMbProduct || hasCareerProduct) {
//                log.error("[MbNormalProductRefundProcessor][不是MB普通产品订单: orderId={}, hasMbProduct={}, hasCareerProduct={}]",
//                    orderId, hasMbProduct, hasCareerProduct);
//                return false;
//            }
//
//            log.info("[MbNormalProductRefundProcessor][MB普通产品退款请求验证通过: orderId={}, userId={}, itemCount={}]",
//                orderId, order.getUserId(), orderItems.size());
//
//            return true;
//
//        } catch (NumberFormatException e) {
//            log.error("[MbNormalProductRefundProcessor][解析订单ID失败: {}]", merchantOrderId, e);
//            return false;
//        } catch (Exception e) {
//            log.error("[MbNormalProductRefundProcessor][验证退款请求异常: {}]", merchantOrderId, e);
//            return false;
//        }
//    }
//
//    /**
//     * 执行MB普通产品退款逆操作
//     *
//     * @param notifyReqDTO 退款通知请求数据
//     * @return true-执行成功，false-执行失败
//     */
//    @Override
//    protected boolean executeRefundReverse(PayRefundNotifyReqDTO notifyReqDTO) {
//        String merchantOrderId = notifyReqDTO.getMerchantOrderId();
//
//        try {
//            // 1. 解析订单ID
//            Long orderId = Long.parseLong(merchantOrderId);
//
//            // 2. 查询订单信息
//            TradeOrderRespDTO order = tradeOrderApi.getOrder(orderId);
//            if (order == null) {
//                log.error("[MbNormalProductRefundProcessor][订单不存在: orderId={}]", orderId);
//                return false;
//            }
//
//            log.info("[MbNormalProductRefundProcessor][开始执行MB普通产品退款逆操作: orderId={}, userId={}]",
//                orderId, order.getUserId());
//
//            // 3. 执行逆操作
//            return handleMbNormalProductRefundReverse(order, notifyReqDTO);
//
//        } catch (Exception e) {
//            log.error("[MbNormalProductRefundProcessor][执行MB普通产品退款逆操作异常: {}]", merchantOrderId, e);
//            throw e;
//        }
//    }
//
//    /**
//     * 获取退款类型
//     *
//     * @return MB普通产品退款类型
//     */
//    @Override
//    protected MbRefundTypeEnum getRefundType() {
//        return MbRefundTypeEnum.MB_NORMAL_PRODUCT;
//    }
//
//    /**
//     * 获取用户ID
//     *
//     * @param notifyReqDTO 退款通知请求数据
//     * @return 用户ID
//     */
//    @Override
//    protected Long getUserId(PayRefundNotifyReqDTO notifyReqDTO) {
//        try {
//            Long orderId = Long.parseLong(notifyReqDTO.getMerchantOrderId());
//            TradeOrderRespDTO order = tradeOrderApi.getOrder(orderId);
//            return order != null ? order.getUserId() : null;
//        } catch (Exception e) {
//            log.error("[MbNormalProductRefundProcessor][获取用户ID异常: merchantOrderId={}]",
//                notifyReqDTO.getMerchantOrderId(), e);
//            return null;
//        }
//    }
//
//    /**
//     * 处理MB普通产品退款逆操作的核心逻辑
//     *
//     * @param order 订单信息
//     * @param notifyReqDTO 退款通知请求数据
//     * @return true-处理成功，false-处理失败
//     */
//    private boolean handleMbNormalProductRefundReverse(TradeOrderRespDTO order, PayRefundNotifyReqDTO notifyReqDTO) {
//        try {
//            log.info("[handleMbNormalProductRefundReverse][开始处理MB普通产品购买退款逆操作: orderId={}, userId={}]",
//                order.getId(), order.getUserId());
//
//            // 1. 取消相关分佣记录
//            cancelBrokerageForOrder(order.getId());
//
//            // 2. 回退物料库存（如果有物料扣减）
//            // 查询订单项，判断是否需要回退物料库存
//            List<TradeOrderItemRespDTO> orderItems = tradeOrderItemApi.getOrderItemListByOrderId(order.getId());
//            if (orderItems != null && !orderItems.isEmpty()) {
//                // 回退上级代理商的物料库存
//                revertMaterialStockForNormalProduct(order, orderItems);
//            }
//
//            log.info("[handleMbNormalProductRefundReverse][MB普通产品购买退款逆操作成功: orderId={}]", order.getId());
//            return true;
//
//        } catch (Exception e) {
//            log.error("[handleMbNormalProductRefundReverse][MB普通产品购买退款逆操作异常: orderId={}]", order.getId(), e);
//            throw e;
//        }
//    }
//
//    /**
//     * 取消订单相关的分佣记录
//     *
//     * <p>分佣回退策略说明：</p>
//     * <ul>
//     *   <li>待结算状态(WAIT_SETTLEMENT)：从用户冻结余额中扣除</li>
//     *   <li>已结算状态(SETTLEMENT)：从用户可用余额中扣除</li>
//     *   <li>已取消状态(CANCEL)：跳过处理</li>
//     * </ul>
//     *
//     * @param orderId 订单ID
//     */
//    private void cancelBrokerageForOrder(Long orderId) {
//        try {
//            log.info("[cancelBrokerageForOrder][开始取消订单相关的分佣记录: orderId={}]", orderId);
//
//            // 直接使用订单ID作为bizId，分佣服务内部会根据bizId查找所有相关分佣记录并取消
//            brokerageRecordApi.cancelBrokerage(String.valueOf(orderId));
//
//            log.info("[cancelBrokerageForOrder][成功取消订单相关的所有分佣记录: orderId={}]", orderId);
//            log.info("[cancelBrokerageForOrder][分佣回退已根据记录状态自动处理: 待结算->减少冻结余额, 已结算->减少可用余额]");
//
//        } catch (Exception e) {
//            log.error("[cancelBrokerageForOrder][取消订单分佣异常: orderId={}]", orderId, e);
//            // 分佣取消失败不影响主流程，记录错误日志即可
//        }
//    }
//
//    /**
//     * 回退普通产品购买时扣减的物料库存
//     *
//     * <p>物料库存回退逻辑（修复后）：</p>
//     * <ul>
//     *   <li><strong>基于购买时的实际扣减记录</strong>：根据购买时是否实际扣减了上级代理的物料库存来判断是否需要回退</li>
//     *   <li><strong>查询物料变动记录</strong>：通过订单ID查询MaterialRecord表中的扣减记录</li>
//     *   <li><strong>精确回退</strong>：只有实际扣减了库存的情况下才进行回退，确保数据一致性</li>
//     * </ul>
//     *
//     * @param order 订单信息
//     * @param orderItems 订单项列表
//     */
//    private void revertMaterialStockForNormalProduct(TradeOrderRespDTO order, List<TradeOrderItemRespDTO> orderItems) {
//        try {
//            log.info("[revertMaterialStockForNormalProduct][开始回退物料库存: orderId={}, userId={}]",
//                order.getId(), order.getUserId());
//
//            // 1. 查询购买时的物料扣减记录，判断是否需要回退
//            List<MaterialTxnRespDTO> deductionRecords = queryMaterialDeductionRecords(order.getId());
//            if (deductionRecords.isEmpty()) {
//                log.info("[revertMaterialStockForNormalProduct][订单({})购买时未扣减任何上级物料库存，无需回退]", order.getId());
//                return;
//            }
//
//            log.info("[revertMaterialStockForNormalProduct][订单({})购买时扣减了{}条物料记录，需要进行回退]",
//                order.getId(), deductionRecords.size());
//
//            // 2. 按用户分组扣减记录，准备回退操作
//            Map<Long, List<MaterialTxnRespDTO>> recordsByUser = deductionRecords.stream()
//                .collect(Collectors.groupingBy(MaterialTxnRespDTO::getUserId));
//
//            // 3. 根据扣减记录进行精确回退
//            for (Map.Entry<Long, List<MaterialTxnRespDTO>> entry : recordsByUser.entrySet()) {
//                Long agentUserId = entry.getKey();
//                List<MaterialTxnRespDTO> userRecords = entry.getValue();
//
//                log.info("[revertMaterialStockForNormalProduct][开始回退用户({})的物料库存，共{}条记录]",
//                    agentUserId, userRecords.size());
//
//                // 准备批量回退操作
//                List<MaterialActDTO> revertActs = new ArrayList<>();
//                for (MaterialTxnRespDTO record : userRecords) {
//                    MaterialActDTO act = MaterialActDTO.builder()
//                        .userId(agentUserId)
//                        .materialId(record.getMaterialId())
//                        .quantity(record.getQuantity())
//                        .direction(MaterialActDirectionEnum.IN) // 入账操作（回退之前的扣减）
//                        .bizKey("refund_revert_" + order.getId() + "_" + record.getId())
//                        .bizType("REFUND_REVERT")
//                        .reason("普通产品退款-回退上级物料库存: 订单" + order.getNo() +
//                            ", 原扣减记录ID=" + record.getId() + ", 物料ID=" + record.getMaterialId())
//                        .build();
//                    revertActs.add(act);
//                }
//
//                try {
//                    // 批量执行物料回退
//                    materialApi.applyActs(revertActs);
//
//                    log.info("[revertMaterialStockForNormalProduct][成功回退用户({})的物料库存，共{}条记录]",
//                        agentUserId, revertActs.size());
//                } catch (Exception e) {
//                    log.error("[revertMaterialStockForNormalProduct][回退用户({})物料库存失败: 订单={}, 错误={}]",
//                        agentUserId, order.getId(), e.getMessage());
//                    // 单个用户回退失败不影响其他用户的回退，继续处理
//                }
//            }
//
//            log.info("[revertMaterialStockForNormalProduct][物料库存回退完成: orderId={}, 回退记录数={}]",
//                order.getId(), deductionRecords.size());
//
//        } catch (Exception e) {
//            log.error("[revertMaterialStockForNormalProduct][回退物料库存异常: orderId={}]", order.getId(), e);
//            // 物料库存回退失败不影响主流程，记录错误日志即可
//        }
//    }
//
//    /**
//     * 查询订单购买时的物料扣减记录
//     *
//     * <p>查询逻辑说明：</p>
//     * <ul>
//     *   <li>根据订单ID查询MaterialRecord表中的扣减记录</li>
//     *   <li>筛选条件：sourceId=订单ID，actionType=减少，sourceType=下级购买减少</li>
//     *   <li>返回所有匹配的扣减记录，用于后续的精确回退</li>
//     * </ul>
//     *
//     * @param orderId 订单ID
//     * @return 物料扣减记录列表
//     */
//    private List<MaterialTxnRespDTO> queryMaterialDeductionRecords(Long orderId) {
//        try {
//            log.info("[queryMaterialDeductionRecords][查询订单({})的物料扣减记录]", orderId);
//
//            // 构建查询条件：根据bizKey查询该订单相关的物料流水记录
//            // 这里需要根据实际的业务key命名规则来查询
//            MaterialTxnPageReqDTO pageReq = new MaterialTxnPageReqDTO();
//            pageReq.setBizKey(String.valueOf(orderId)); // 假设bizKey使用订单ID
//            pageReq.setPageSize(1000); // 设置足够大的页面大小，获取所有记录
//            pageReq.setPageNo(1);
//
//            PageResult<MaterialTxnRespDTO> pageResult = materialApi.getTxnPage(pageReq);
//            List<MaterialTxnRespDTO> allRecords = pageResult.getList();
//
//            // 筛选出扣减记录（direction = -1 表示回退/扣减）
//            List<MaterialTxnRespDTO> deductionRecords = allRecords.stream()
//                .filter(record -> record.getDirection() != null && record.getDirection() == -1)
//                .collect(Collectors.toList());
//
//            log.info("[queryMaterialDeductionRecords][订单({})查询到{}条物料扣减记录]", orderId, deductionRecords.size());
//
//            if (!deductionRecords.isEmpty()) {
//                for (MaterialTxnRespDTO record : deductionRecords) {
//                    log.debug("[queryMaterialDeductionRecords][扣减记录详情: recordId={}, userId={}, materialId={}, quantity={}, reason={}]",
//                        record.getId(), record.getUserId(), record.getMaterialId(), record.getQuantity(), record.getReason());
//                }
//            }
//
//            return deductionRecords;
//
//        } catch (Exception e) {
//            log.error("[queryMaterialDeductionRecords][查询订单({})物料扣减记录异常]", orderId, e);
//            // 查询失败时返回空列表，避免影响主流程
//            return List.of();
//        }
//    }
//
//    /**
//     * 后置处理钩子方法
//     *
//     * <p>MB普通产品退款的特殊后置处理逻辑</p>
//     *
//     * @param notifyReqDTO 退款通知请求数据
//     * @param success 处理是否成功
//     */
//    @Override
//    protected void postProcessing(PayRefundNotifyReqDTO notifyReqDTO, boolean success) {
//        super.postProcessing(notifyReqDTO, success);
//
//        if (success) {
//            log.info("[MbNormalProductRefundProcessor][MB普通产品退款后置处理: merchantOrderId={}, 分佣取消和物料回退已完成]",
//                notifyReqDTO.getMerchantOrderId());
//
//            // 这里可以添加特殊的后置处理逻辑，例如：
//            // 1. 发送退款成功通知
//            // 2. 更新销售统计数据
//            // 3. 记录退款审计日志
//        } else {
//            log.warn("[MbNormalProductRefundProcessor][MB普通产品退款后置处理: merchantOrderId={}, 退款逆操作失败，可能需要人工介入]",
//                notifyReqDTO.getMerchantOrderId());
//        }
//    }
//}
