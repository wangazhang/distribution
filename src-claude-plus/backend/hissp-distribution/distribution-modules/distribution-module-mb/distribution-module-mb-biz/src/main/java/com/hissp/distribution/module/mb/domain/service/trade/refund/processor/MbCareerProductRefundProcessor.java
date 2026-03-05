//package com.hissp.distribution.module.mb.domain.service.trade.refund.processor;
//
//import com.hissp.distribution.framework.common.exception.ServiceException;
//import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
//import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
//import com.hissp.distribution.module.mb.dal.dataobject.levelbenefitmapping.LevelBenefitMappingDO;
//import com.hissp.distribution.module.mb.dal.mysql.trade.MbRefundRecordMapper;
//import com.hissp.distribution.module.material.api.MaterialApi;
//import com.hissp.distribution.module.mb.domain.service.mbdt.commission.BenefitConfigParser;
//import com.hissp.distribution.module.mb.domain.service.mbdt.commission.LevelBenefitMappingService;
//import com.hissp.distribution.module.mb.constants.MbConstants;
//import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
//import com.hissp.distribution.module.member.api.level.MemberLevelApi;
//import com.hissp.distribution.module.member.api.level.dto.MemberLevelReqDTO;
//import com.hissp.distribution.module.member.api.user.MemberUserApi;
//import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
//import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
//import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
//import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
//import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
//import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
//import com.hissp.distribution.module.trade.api.order.TradeOrderApi;
//import com.hissp.distribution.module.trade.api.order.TradeOrderItemApi;
//import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
//import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * MB代理商开通产品退款处理器
// *
// * <p>处理MB代理商开通产品订单的退款逆操作，主要包括：</p>
// * <ul>
// *   <li>验证订单的有效性和产品类型</li>
// *   <li>取消相关的分佣记录</li>
// *   <li>回退用户等级（从SVIP回退到普通等级）</li>
// *   <li>取消代理商相关的分销权限</li>
// *   <li>回退开通时给的物料奖励</li>
// * </ul>
// *
// * <h3>处理逻辑说明：</h3>
// * <p>代理商开通产品退款时需要执行以下逆操作：</p>
// * <ol>
// *   <li><strong>分佣取消</strong>：取消订单产生的所有分佣记录</li>
// *   <li><strong>等级回退</strong>：将用户等级从SVIP回退到默认等级</li>
// *   <li><strong>权限撤销</strong>：关闭用户的分销权限</li>
// *   <li><strong>物料回退</strong>：回退开通时给予的物料奖励</li>
// * </ol>
// *
// * <h3>物料奖励回退逻辑：</h3>
// * <p>根据SVIP等级的权益配置，回退开通时给予的物料奖励。包括：</p>
// * <ul>
// *   <li>自身权益中的物料奖励</li>
// *   <li>其他相关的物料权益</li>
// * </ul>
// *
// * <h3>异常处理策略：</h3>
// * <ul>
// *   <li><strong>订单不存在</strong>：直接返回失败</li>
// *   <li><strong>产品类型不匹配</strong>：直接返回失败</li>
// *   <li><strong>等级回退失败</strong>：记录错误但不影响主流程</li>
// *   <li><strong>权限撤销失败</strong>：记录错误但不影响主流程</li>
// *   <li><strong>物料回退失败</strong>：记录错误但不影响主流程</li>
// * </ul>
// *
// * @author system
// */
//@Slf4j
//@Component
//public class MbCareerProductRefundProcessor extends AbstractRefundProcessor {
//
//    private final TradeOrderApi tradeOrderApi;
//    private final TradeOrderItemApi tradeOrderItemApi;
//    private final ProductSpuApi productSpuApi;
//    private final BrokerageRecordApi brokerageRecordApi;
//    private final BrokerageUserApi brokerageUserApi;
//    private final MemberLevelApi memberLevelApi;
//    private final MemberUserApi memberUserApi;
//    private final LevelBenefitMappingService levelBenefitMappingService;
//    private final MaterialApi materialApi;
//
//    /**
//     * 构造函数 - 注入必要的依赖
//     */
//    public MbCareerProductRefundProcessor(
//            MbRefundRecordMapper mbRefundRecordMapper,
//            TradeOrderApi tradeOrderApi,
//            TradeOrderItemApi tradeOrderItemApi,
//            ProductSpuApi productSpuApi,
//            BrokerageRecordApi brokerageRecordApi,
//            BrokerageUserApi brokerageUserApi,
//            MemberLevelApi memberLevelApi,
//            MemberUserApi memberUserApi,
//            LevelBenefitMappingService levelBenefitMappingService,
//            MaterialApi materialApi) {
//        super(mbRefundRecordMapper);
//        this.tradeOrderApi = tradeOrderApi;
//        this.tradeOrderItemApi = tradeOrderItemApi;
//        this.productSpuApi = productSpuApi;
//        this.brokerageRecordApi = brokerageRecordApi;
//        this.brokerageUserApi = brokerageUserApi;
//        this.memberLevelApi = memberLevelApi;
//        this.memberUserApi = memberUserApi;
//        this.levelBenefitMappingService = levelBenefitMappingService;
//        this.materialApi = materialApi;
//    }
//
//    /**
//     * 验证MB代理商开通产品退款请求
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
//                log.error("[MbCareerProductRefundProcessor][订单不存在: orderId={}]", orderId);
//                return false;
//            }
//
//            // 3. 查询订单项
//            List<TradeOrderItemRespDTO> orderItems = tradeOrderItemApi.getOrderItemListByOrderId(orderId);
//            if (orderItems == null || orderItems.isEmpty()) {
//                log.error("[MbCareerProductRefundProcessor][订单项不存在: orderId={}]", orderId);
//                return false;
//            }
//
//            // 4. 验证是否为MB代理商开通产品
//            List<Long> spuIds = orderItems.stream()
//                .map(TradeOrderItemRespDTO::getSpuId)
//                .toList();
//
//            boolean hasCareerProduct = productSpuApi.isCareerProduct(spuIds);
//
//            if (!hasCareerProduct) {
//                log.error("[MbCareerProductRefundProcessor][不是MB代理商开通产品订单: orderId={}, hasCareerProduct={}]",
//                    orderId, hasCareerProduct);
//                return false;
//            }
//
//            log.info("[MbCareerProductRefundProcessor][MB代理商开通产品退款请求验证通过: orderId={}, userId={}, itemCount={}]",
//                orderId, order.getUserId(), orderItems.size());
//
//            return true;
//
//        } catch (NumberFormatException e) {
//            log.error("[MbCareerProductRefundProcessor][解析订单ID失败: {}]", merchantOrderId, e);
//            return false;
//        } catch (Exception e) {
//            log.error("[MbCareerProductRefundProcessor][验证退款请求异常: {}]", merchantOrderId, e);
//            return false;
//        }
//    }
//
//    /**
//     * 执行MB代理商开通产品退款逆操作
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
//                log.error("[MbCareerProductRefundProcessor][订单不存在: orderId={}]", orderId);
//                return false;
//            }
//
//            log.info("[MbCareerProductRefundProcessor][开始执行MB代理商开通产品退款逆操作: orderId={}, userId={}]",
//                orderId, order.getUserId());
//
//            // 3. 执行逆操作
//            return handleMbCareerProductRefundReverse(order, notifyReqDTO);
//
//        } catch (Exception e) {
//            log.error("[MbCareerProductRefundProcessor][执行MB代理商开通产品退款逆操作异常: {}]", merchantOrderId, e);
//            throw e;
//        }
//    }
//
//    /**
//     * 获取退款类型
//     *
//     * @return MB代理商开通产品退款类型
//     */
//    @Override
//    protected MbRefundTypeEnum getRefundType() {
//        return MbRefundTypeEnum.MB_CAREER_PRODUCT;
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
//            log.error("[MbCareerProductRefundProcessor][获取用户ID异常: merchantOrderId={}]",
//                notifyReqDTO.getMerchantOrderId(), e);
//            return null;
//        }
//    }
//
//    /**
//     * 处理MB代理商开通产品退款逆操作的核心逻辑
//     *
//     * @param order 订单信息
//     * @param notifyReqDTO 退款通知请求数据
//     * @return true-处理成功，false-处理失败
//     */
//    private boolean handleMbCareerProductRefundReverse(TradeOrderRespDTO order, PayRefundNotifyReqDTO notifyReqDTO) {
//        try {
//            log.info("[handleMbCareerProductRefundReverse][开始处理MB代理商开通退款逆操作: orderId={}, userId={}]",
//                order.getId(), order.getUserId());
//
//            Long userId = order.getUserId();
//            String bizId = String.valueOf(order.getId());
//
//            // 1. 取消相关分佣记录
//            cancelBrokerageForOrder(order.getId());
//
//            // 2. 回退用户等级
//            revertUserLevel(userId, bizId);
//
//            // 3. 取消代理商相关的分销权限
//            revokeAgentPermissions(userId, bizId);
//
//            // 4. 回退开通时给的物料奖励
//            revertCareerProductMaterialRewards(userId, bizId);
//
//            log.info("[handleMbCareerProductRefundReverse][MB代理商开通退款逆操作成功: orderId={}]", order.getId());
//            return true;
//
//        } catch (Exception e) {
//            log.error("[handleMbCareerProductRefundReverse][MB代理商开通退款逆操作异常: orderId={}]", order.getId(), e);
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
//     * 回退用户等级
//     *
//     * <p>将用户等级从SVIP回退到普通用户等级</p>
//     *
//     * @param userId 用户ID
//     * @param bizId 业务ID（订单ID）
//     */
//    private void revertUserLevel(Long userId, String bizId) {
//        try {
//            log.info("[revertUserLevel][开始回退用户等级: userId={}, bizId={}]", userId, bizId);
//
//            // 获取用户当前信息
//            MemberUserRespDTO userInfo = memberUserApi.getUser(userId);
//            if (userInfo == null) {
//                log.warn("[revertUserLevel][用户不存在: userId={}]", userId);
//                return;
//            }
//
//            // 检查用户当前是否为SVIP等级
//            if (!MbConstants.MbSVIPLevelInfo.MB_SVIP_LEVEL_ID.equals(userInfo.getLevelId())) {
//                log.warn("[revertUserLevel][用户当前等级不是SVIP，无需回退: userId={}, currentLevelId={}]",
//                    userId, userInfo.getLevelId());
//                return;
//            }
//
//            // 回退到默认等级（等级1）
//            Long defaultLevelId = 1L; // 假设1是默认等级ID
//            MemberLevelReqDTO levelChangeReq = new MemberLevelReqDTO()
//                .setUserId(userId)
//                .setLevelId(defaultLevelId)
//                .setReason("代理商开通退款-回退用户等级")
//                .setBizId(Long.parseLong(bizId))
//                .setEnableBrokerage(false); // 关闭分佣能力
//
//            memberLevelApi.updateUserLevel(levelChangeReq);
//
//            log.info("[revertUserLevel][成功回退用户等级: userId={}, 从等级{}回退到等级{}]",
//                userId, userInfo.getLevelId(), defaultLevelId);
//
//        } catch (Exception e) {
//            log.error("[revertUserLevel][回退用户等级异常: userId={}, bizId={}]", userId, bizId, e);
//            // 等级回退失败不影响主流程，记录错误日志即可
//        }
//    }
//
//    /**
//     * 取消代理商相关的分销权限
//     *
//     * @param userId 用户ID
//     * @param bizId 业务ID（订单ID）
//     */
//    private void revokeAgentPermissions(Long userId, String bizId) {
//        try {
//            log.info("[revokeAgentPermissions][开始取消代理商分销权限: userId={}, bizId={}]", userId, bizId);
//
//            // 检查用户是否为分销用户
//            BrokerageUserRespDTO brokerageUser = brokerageUserApi.getBrokerageUser(userId);
//            if (brokerageUser == null) {
//                log.info("[revokeAgentPermissions][用户不是分销用户，无需取消权限: userId={}]", userId);
//                return;
//            }
//
//            if (!brokerageUser.getBrokerageEnabled()) {
//                log.info("[revokeAgentPermissions][用户分销权限已关闭，无需操作: userId={}]", userId);
//                return;
//            }
//
//            // 关闭分销权限
//            brokerageUserApi.updateBrokerageUserEnabled(userId, false);
//
//            log.info("[revokeAgentPermissions][成功取消代理商分销权限: userId={}]", userId);
//
//        } catch (Exception e) {
//            log.error("[revokeAgentPermissions][取消代理商分销权限异常: userId={}, bizId={}]", userId, bizId, e);
//            // 权限取消失败不影响主流程，记录错误日志即可
//        }
//    }
//
//    /**
//     * 回退代理商开通时给的物料奖励
//     *
//     * @param userId 用户ID
//     * @param bizId 业务ID（订单ID）
//     */
//    private void revertCareerProductMaterialRewards(Long userId, String bizId) {
//        try {
//            log.info("[revertCareerProductMaterialRewards][开始回退代理商开通物料奖励: userId={}, bizId={}]", userId, bizId);
//
//            // 获取SVIP等级的权益配置
//            LevelBenefitMappingDO mappingConfig = levelBenefitMappingService.getLatestLevelBenefitMapping(
//                MbConstants.MbSVIPLevelInfo.MB_SVIP_LEVEL);
//
//            if (mappingConfig == null) {
//                log.warn("[revertCareerProductMaterialRewards][未找到SVIP等级权益配置: userId={}]", userId);
//                return;
//            }
//
//            // 解析权益配置
//            BenefitConfigParser.BenefitConfig benefitConfig =
//                BenefitConfigParser.parse(mappingConfig.getBenefitsGainedJson());
//
//            if (benefitConfig == null) {
//                log.warn("[revertCareerProductMaterialRewards][权益配置解析失败: userId={}]", userId);
//                return;
//            }
//
//            // 回退自身权益中的物料奖励
//            revertMaterialBenefits(userId, bizId, benefitConfig.getSelfBenefits(), "自身权益");
//
//            log.info("[revertCareerProductMaterialRewards][代理商开通物料奖励回退完成: userId={}]", userId);
//
//        } catch (Exception e) {
//            log.error("[revertCareerProductMaterialRewards][回退代理商开通物料奖励异常: userId={}, bizId={}]", userId, bizId, e);
//            // 物料回退失败不影响主流程，记录错误日志即可
//        }
//    }
//
//    /**
//     * 回退物料权益
//     *
//     * @param userId 用户ID
//     * @param bizId 业务ID
//     * @param benefitItems 权益项列表
//     * @param benefitType 权益类型描述
//     */
//    private void revertMaterialBenefits(Long userId, String bizId,
//                                      java.util.List<BenefitConfigParser.BenefitItem> benefitItems,
//                                      String benefitType) {
//        if (benefitItems == null || benefitItems.isEmpty()) {
//            log.info("[revertMaterialBenefits][{}没有物料权益需要回退: userId={}]", benefitType, userId);
//            return;
//        }
//
//        for (BenefitConfigParser.BenefitItem item : benefitItems) {
//            if (item.isProduct()) {
//                try {
//                    // 构建物料回退请求
//                    MaterialActDTO materialActDTO = MaterialActDTO.builder()
//                        .userId(userId)
//                        .materialId(Long.parseLong(item.getTypeId()))
//                        .quantity(item.getQuantity())
//                        .direction(MaterialActDirectionEnum.OUT)
//                        .bizKey("ORDER_REFUND_" + bizId + "_" + item.getTypeId())
//                        .bizType("REFUND_REVOKE")
//                        .reason("代理商开通退款-回退" + benefitType + "物料奖励: " + item.getDesc())
//                        .build();
//
//                    // 执行物料回退
//                    materialApi.applyActs(java.util.List.of(materialActDTO));
//
//                    log.info("[revertMaterialBenefits][成功回退{}物料奖励: userId={}, 物料ID={}, 数量={}]",
//                        benefitType, userId, item.getTypeId(), item.getQuantity());
//
//                } catch (ServiceException e) {
//                    log.error("[revertMaterialBenefits][回退{}物料奖励失败-库存不足: userId={}, 物料ID={}, 数量={}, 错误={}]",
//                        benefitType, userId, item.getTypeId(), item.getQuantity(), e.getMessage());
//                    // 库存不足时继续处理其他物料，不中断流程
//                } catch (Exception e) {
//                    log.error("[revertMaterialBenefits][回退{}物料奖励异常: userId={}, 物料ID={}, 数量={}]",
//                        benefitType, userId, item.getTypeId(), item.getQuantity(), e);
//                    // 单个物料回退失败不影响其他物料的回退
//                }
//            }
//        }
//    }
//
//    /**
//     * 后置处理钩子方法
//     *
//     * <p>MB代理商开通产品退款的特殊后置处理逻辑</p>
//     *
//     * @param notifyReqDTO 退款通知请求数据
//     * @param success 处理是否成功
//     */
//    @Override
//    protected void postProcessing(PayRefundNotifyReqDTO notifyReqDTO, boolean success) {
//        super.postProcessing(notifyReqDTO, success);
//
//        if (success) {
//            log.info("[MbCareerProductRefundProcessor][MB代理商开通产品退款后置处理: merchantOrderId={}, 等级回退、权限撤销、物料回退已完成]",
//                notifyReqDTO.getMerchantOrderId());
//
//            // 这里可以添加特殊的后置处理逻辑，例如：
//            // 1. 发送代理商权限撤销通知
//            // 2. 更新代理商统计数据
//            // 3. 记录代理商变更审计日志
//        } else {
//            log.warn("[MbCareerProductRefundProcessor][MB代理商开通产品退款后置处理: merchantOrderId={}, 退款逆操作失败，可能需要人工介入]",
//                notifyReqDTO.getMerchantOrderId());
//        }
//    }
//}
