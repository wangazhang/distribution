package com.hissp.distribution.module.mb.domain.service.trade.refund.processor;

import com.hissp.distribution.module.mb.dal.dataobject.trade.MbRefundRecordDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbRefundRecordMapper;
import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 退款处理抽象模板类
 *
 * <p>使用模板设计模式定义退款处理的通用流程，具体的业务逻辑由子类实现。</p>
 *
 * <h3>模板方法模式说明：</h3>
 * <ul>
 *   <li><strong>模板方法</strong>：{@link #processRefund(PayRefundNotifyReqDTO)} - 定义完整的退款处理流程</li>
 *   <li><strong>抽象方法</strong>：由子类必须实现的核心业务逻辑方法</li>
 *   <li><strong>钩子方法</strong>：子类可选择性重写的扩展点方法</li>
 *   <li><strong>通用方法</strong>：所有子类共享的公共逻辑方法</li>
 * </ul>
 *
 * <h3>处理流程步骤：</h3>
 * <ol>
 *   <li>幂等性检查 - 避免重复处理</li>
 *   <li>验证退款请求 - 子类实现具体验证逻辑</li>
 *   <li>获取用户ID - 子类实现用户ID获取逻辑</li>
 *   <li>创建退款记录 - 记录处理状态</li>
 *   <li>更新状态为处理中</li>
 *   <li>执行退款逆操作 - 子类实现核心业务逻辑</li>
 *   <li>更新处理结果状态</li>
 *   <li>执行后置处理 - 子类可选的扩展处理</li>
 * </ol>
 *
 * @author system
 */
@Slf4j
public abstract class AbstractRefundProcessor {

    protected final MbRefundRecordMapper mbRefundRecordMapper;

    /**
     * 构造函数 - 注入必要的依赖
     *
     * @param mbRefundRecordMapper 退款记录数据访问对象
     */
    protected AbstractRefundProcessor(MbRefundRecordMapper mbRefundRecordMapper) {
        this.mbRefundRecordMapper = mbRefundRecordMapper;
    }

    /**
     * 模板方法：处理退款通知的完整流程
     *
     * <p>这是模板方法模式的核心方法，定义了退款处理的标准流程。
     * 该方法是final的，不允许子类重写，确保流程的一致性。</p>
     *
     * <p>事务边界：此方法应该在事务环境中调用，所有步骤都参与同一个事务。</p>
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-处理成功，false-处理失败
     */
    public final boolean processRefund(PayRefundNotifyReqDTO notifyReqDTO) {
        log.info("[{}][开始处理退款: merchantOrderId={}]",
            getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId());

        try {
            // 步骤1：幂等性检查（通用逻辑）
            if (isAlreadyProcessed(notifyReqDTO)) {
                log.info("[{}][退款已处理过，跳过: merchantOrderId={}]",
                    getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId());
                return true;
            }

            // 步骤2：验证退款请求（子类实现）
            if (!validateRefundRequest(notifyReqDTO)) {
                log.warn("[{}][退款请求验证失败: merchantOrderId={}]",
                    getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId());
                return false;
            }

            // 步骤3：获取用户ID（子类实现）
            Long userId = getUserId(notifyReqDTO);
            if (userId == null) {
                log.error("[{}][无法获取用户ID: merchantOrderId={}]",
                    getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId());
                return false;
            }

            // 步骤4：创建退款记录（通用逻辑）
            Long recordId = createRefundRecord(notifyReqDTO, getRefundType(), userId);

            try {
                // 步骤5：更新状态为处理中（通用逻辑）
                updateRefundRecordStatusToProcessing(recordId);

                // 步骤6：执行退款逆操作（子类实现）
                boolean result = executeRefundReverse(notifyReqDTO);

                if (result) {
                    // 步骤7a：处理成功
                    updateRefundRecordStatusToSuccess(recordId, "退款逆操作处理成功");

                    // 步骤8a：后置处理（钩子方法）
                    postProcessing(notifyReqDTO, true);

                    log.info("[{}][退款处理成功: merchantOrderId={}]",
                        getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId());
                    return true;
                } else {
                    // 步骤7b：处理失败
                    updateRefundRecordStatusToFailed(recordId, "退款逆操作处理失败");

                    // 步骤8b：后置处理（钩子方法）
                    postProcessing(notifyReqDTO, false);

                    log.warn("[{}][退款处理失败: merchantOrderId={}]",
                        getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId());
                    return false;
                }

            } catch (Exception e) {
                // 步骤7c：异常处理
                updateRefundRecordStatusToFailed(recordId, e.getMessage());
                log.error("[{}][退款处理异常: merchantOrderId={}]",
                    getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId(), e);
                throw e;
            }

        } catch (Exception e) {
            log.error("[{}][退款处理流程异常: merchantOrderId={}]",
                getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId(), e);
            throw e;
        }
    }

    // ==================== 抽象方法（子类必须实现） ====================

    /**
     * 验证退款请求
     *
     * <p>子类需要实现具体的验证逻辑，例如：</p>
     * <ul>
     *   <li>检查订单ID格式和前缀</li>
     *   <li>验证订单是否存在</li>
     *   <li>检查订单状态是否允许退款</li>
     *   <li>验证产品类型是否匹配</li>
     * </ul>
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-验证通过，false-验证失败
     */
    protected abstract boolean validateRefundRequest(PayRefundNotifyReqDTO notifyReqDTO);

    /**
     * 执行退款逆操作
     *
     * <p>这是模板方法的核心步骤，子类需要实现具体的业务逻辑，例如：</p>
     * <ul>
     *   <li>物料库存回退</li>
     *   <li>分佣记录取消</li>
     *   <li>用户等级回退</li>
     *   <li>权限撤销</li>
     * </ul>
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-执行成功，false-执行失败
     */
    protected abstract boolean executeRefundReverse(PayRefundNotifyReqDTO notifyReqDTO);

    /**
     * 获取退款类型
     *
     * @return 当前处理器对应的退款类型
     */
    protected abstract MbRefundTypeEnum getRefundType();

    /**
     * 获取用户ID
     *
     * <p>不同类型的退款，用户ID的获取方式可能不同：</p>
     * <ul>
     *   <li>MB内部订单：从物料订单中获取代理用户ID</li>
     *   <li>Trade模块订单：从订单中获取用户ID</li>
     * </ul>
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return 用户ID，获取失败返回null
     */
    protected abstract Long getUserId(PayRefundNotifyReqDTO notifyReqDTO);

    // ==================== 钩子方法（子类可选择性重写） ====================

    /**
     * 后置处理钩子方法
     *
     * <p>在退款处理完成后调用，子类可以重写此方法来实现额外的处理逻辑，
     * 例如发送通知、记录审计日志等。</p>
     *
     * <p>默认实现为空，子类可根据需要重写。</p>
     *
     * @param notifyReqDTO 退款通知请求数据
     * @param success 处理是否成功
     */
    protected void postProcessing(PayRefundNotifyReqDTO notifyReqDTO, boolean success) {
        // 默认空实现，子类可选择性重写
        log.debug("[{}][后置处理: merchantOrderId={}, success={}]",
            getClass().getSimpleName(), notifyReqDTO.getMerchantOrderId(), success);
    }

    // ==================== 通用方法（所有子类共享） ====================

    /**
     * 检查是否已处理过该退款（幂等性检查）
     *
     * @param notifyReqDTO 退款通知请求数据
     * @return true-已处理过，false-未处理过
     */
    protected boolean isAlreadyProcessed(PayRefundNotifyReqDTO notifyReqDTO) {
        // 优先通过支付退款ID查询
        if (notifyReqDTO.getPayRefundId() != null) {
            MbRefundRecordDO record = mbRefundRecordMapper.selectByPayRefundId(notifyReqDTO.getPayRefundId());
            if (record != null && record.getStatus() == 2) { // 处理成功
                return true;
            }
        }

        // 通过商户订单号查询
        if (notifyReqDTO.getMerchantOrderId() != null) {
            MbRefundRecordDO record = mbRefundRecordMapper.selectByMerchantOrderId(notifyReqDTO.getMerchantOrderId());
            if (record != null && record.getStatus() == 2) { // 处理成功
                return true;
            }
        }

        return false;
    }

    /**
     * 创建退款记录
     *
     * @param notifyReqDTO 退款通知请求数据
     * @param refundType 退款类型
     * @param userId 用户ID
     * @return 退款记录ID
     */
    protected Long createRefundRecord(PayRefundNotifyReqDTO notifyReqDTO, MbRefundTypeEnum refundType, Long userId) {
        MbRefundRecordDO record = MbRefundRecordDO.builder()
            .merchantOrderId(notifyReqDTO.getMerchantOrderId())
            .payRefundId(notifyReqDTO.getPayRefundId())
            .refundType(refundType.getType())
            .userId(userId)
            .refundAmount(0) // 退款金额需要从退款单中获取
            .status(0) // 待处理
            .retryCount(0)
            .build();

        mbRefundRecordMapper.insert(record);

        log.info("[{}][创建退款记录: id={}, merchantOrderId={}, refundType={}]",
            getClass().getSimpleName(), record.getId(), notifyReqDTO.getMerchantOrderId(), refundType);

        return record.getId();
    }

    /**
     * 更新退款记录状态为处理中
     *
     * @param id 退款记录ID
     */
    protected void updateRefundRecordStatusToProcessing(Long id) {
        MbRefundRecordDO updateObj = new MbRefundRecordDO();
        updateObj.setId(id);
        updateObj.setStatus(1); // 处理中
        updateObj.setProcessStartTime(LocalDateTime.now());

        mbRefundRecordMapper.updateById(updateObj);

        log.info("[{}][更新退款记录状态为处理中: id={}]", getClass().getSimpleName(), id);
    }

    /**
     * 更新退款记录状态为处理成功
     *
     * @param id 退款记录ID
     * @param reverseOperationDetails 逆操作详情
     */
    protected void updateRefundRecordStatusToSuccess(Long id, String reverseOperationDetails) {
        MbRefundRecordDO updateObj = new MbRefundRecordDO();
        updateObj.setId(id);
        updateObj.setStatus(2); // 处理成功
        updateObj.setProcessEndTime(LocalDateTime.now());
        updateObj.setReverseOperationDetails(reverseOperationDetails);

        mbRefundRecordMapper.updateById(updateObj);

        log.info("[{}][更新退款记录状态为处理成功: id={}]", getClass().getSimpleName(), id);
    }

    /**
     * 更新退款记录状态为处理失败
     *
     * @param id 退款记录ID
     * @param errorMessage 错误信息
     */
    protected void updateRefundRecordStatusToFailed(Long id, String errorMessage) {
        MbRefundRecordDO updateObj = new MbRefundRecordDO();
        updateObj.setId(id);
        updateObj.setStatus(3); // 处理失败
        updateObj.setProcessEndTime(LocalDateTime.now());
        updateObj.setErrorMessage(errorMessage);

        mbRefundRecordMapper.updateById(updateObj);

        log.info("[{}][更新退款记录状态为处理失败: id={}, error={}]",
            getClass().getSimpleName(), id, errorMessage);
    }
}
