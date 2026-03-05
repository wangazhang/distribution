package com.hissp.distribution.module.pay.service.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.common.annotations.VisibleForTesting;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.date.DateUtils;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.framework.pay.core.enums.notify.PayNotifyStatusEnum;
import com.hissp.distribution.framework.tenant.core.util.TenantUtils;
import com.hissp.distribution.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import com.hissp.distribution.module.pay.api.notify.dto.PayTransferNotifyReqDTO;
import com.hissp.distribution.module.pay.controller.admin.notify.vo.PayNotifyTaskPageReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.notify.PayNotifyLogDO;
import com.hissp.distribution.module.pay.dal.dataobject.notify.PayNotifyTaskDO;
import com.hissp.distribution.module.pay.dal.dataobject.order.PayOrderDO;
import com.hissp.distribution.module.pay.dal.dataobject.refund.PayRefundDO;
import com.hissp.distribution.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.hissp.distribution.module.pay.dal.mysql.notify.PayNotifyLogMapper;
import com.hissp.distribution.module.pay.dal.mysql.notify.PayNotifyTaskMapper;
import com.hissp.distribution.module.pay.dal.redis.notify.PayNotifyLockRedisDAO;
import com.hissp.distribution.module.pay.enums.notify.PayNotifyTypeEnum;
import com.hissp.distribution.module.pay.service.order.PayOrderService;
import com.hissp.distribution.module.pay.service.refund.PayRefundService;
import com.hissp.distribution.module.pay.service.transfer.PayTransferService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.hissp.distribution.framework.common.util.date.LocalDateTimeUtils.addTime;
import static com.hissp.distribution.module.pay.framework.job.config.PayJobConfiguration.NOTIFY_THREAD_POOL_TASK_EXECUTOR;

@Service
@Valid
@Slf4j
public class PayNotifyServiceImpl implements PayNotifyService {

    public static final int NOTIFY_TIMEOUT = 120;
    public static final long NOTIFY_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

    @Resource
    @Lazy
    private PayOrderService orderService;
    @Resource
    @Lazy
    private PayRefundService refundService;
    @Resource
    @Lazy
    private PayTransferService transferService;
    @Resource
    private PayNotifyTaskMapper notifyTaskMapper;
    @Resource
    private PayNotifyLogMapper notifyLogMapper;
    @Resource(name = NOTIFY_THREAD_POOL_TASK_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private PayNotifyLockRedisDAO notifyLockCoreRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPayNotifyTask(Integer type, Long dataId, Map<String, Object> extParams) {
        PayNotifyTaskDO task = new PayNotifyTaskDO().setType(type).setDataId(dataId);
        task.setStatus(PayNotifyStatusEnum.WAITING.getStatus()).setNextNotifyTime(LocalDateTime.now())
                .setNotifyTimes(0).setMaxNotifyTimes(PayNotifyTaskDO.NOTIFY_FREQUENCY.length + 1);
        if (Objects.equals(task.getType(), PayNotifyTypeEnum.ORDER.getType())) {
            PayOrderDO order = orderService.getOrder(task.getDataId());
            task.setAppId(order.getAppId()).
                    setMerchantOrderId(order.getMerchantOrderId()).setNotifyUrl(order.getNotifyUrl());
        } else if (Objects.equals(task.getType(), PayNotifyTypeEnum.REFUND.getType())) {
            PayRefundDO refundDO = refundService.getRefund(task.getDataId());
            task.setAppId(refundDO.getAppId())
                    .setMerchantOrderId(refundDO.getMerchantOrderId()).setNotifyUrl(refundDO.getNotifyUrl());
        } else if (Objects.equals(task.getType(), PayNotifyTypeEnum.TRANSFER.getType())) {
            PayTransferDO transfer = transferService.getTransfer(task.getDataId());
            task.setAppId(transfer.getAppId()).setMerchantTransferId(transfer.getMerchantTransferId())
                    .setNotifyUrl(transfer.getNotifyUrl());
        }
        Map<String, Object> extras = (extParams == null || extParams.isEmpty()) ? null
                : Collections.unmodifiableMap(new HashMap<>(extParams));
        task.setExtParams(extras);

        notifyTaskMapper.insert(task);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executeNotify(task);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPayNotifyTask(Integer type, Long dataId) {
        createPayNotifyTask(type, dataId, null);
    }

    @Override
    public int executeNotify() throws InterruptedException {
        List<PayNotifyTaskDO> tasks = notifyTaskMapper.selectListByNotify();
        if (CollUtil.isEmpty(tasks)) {
            return 0;
        }

        CountDownLatch latch = new CountDownLatch(tasks.size());
        tasks.forEach(task -> threadPoolTaskExecutor.execute(() -> {
            try {
                executeNotify(task);
            } finally {
                latch.countDown();
            }
        }));
        awaitExecuteNotify(latch);
        return tasks.size();
    }

    private void awaitExecuteNotify(CountDownLatch latch) throws InterruptedException {
        long size = latch.getCount();
        for (int i = 0; i < NOTIFY_TIMEOUT; i++) {
            if (latch.await(1L, TimeUnit.SECONDS)) {
                return;
            }
            log.info("[awaitExecuteNotify][任务处理中， 总任务数({}) 剩余任务数({})]", size, latch.getCount());
        }
        log.error("[awaitExecuteNotify][任务未处理完，总任务数({}) 剩余任务数({})]", size, latch.getCount());
    }

    public void executeNotify(PayNotifyTaskDO task) {
        notifyLockCoreRedisDAO.lock(task.getId(), NOTIFY_TIMEOUT_MILLIS, () -> {
            PayNotifyTaskDO dbTask = notifyTaskMapper.selectById(task.getId());
            if (ObjectUtil.notEqual(task.getNotifyTimes(), dbTask.getNotifyTimes())) {
                log.warn("[executeNotifySync][task({}) 任务被忽略，原因是它的通知不是第 ({}) 次，可能是因为并发执行了]",
                        JsonUtils.toJsonString(task), dbTask.getNotifyTimes());
                return;
            }
            getSelf().executeNotify0(dbTask);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeNotify0(PayNotifyTaskDO task) {
        CommonResult<?> invokeResult = null;
        Throwable invokeException = null;
        try {
            invokeResult = executeNotifyInvoke(task);
        } catch (Throwable e) {
            invokeException = e;
        }

        Integer newStatus = processNotifyResult(task, invokeResult, invokeException);

        String response = invokeException != null ? ExceptionUtil.getRootCauseMessage(invokeException) :
                JsonUtils.toJsonString(invokeResult);
        notifyLogMapper.insert(PayNotifyLogDO.builder().taskId(task.getId())
                .notifyTimes(task.getNotifyTimes() + 1).status(newStatus).response(response).build());
    }

    private CommonResult<?> executeNotifyInvoke(PayNotifyTaskDO task) {
        Object request;
        if (Objects.equals(task.getType(), PayNotifyTypeEnum.ORDER.getType())) {

            request = PayOrderNotifyReqDTO.builder().merchantOrderId(task.getMerchantOrderId())
                    .payOrderId(task.getDataId()).deliveryStatus(task.getExtParams() != null ?
                            String.valueOf(task.getExtParams().get("deliveryStatus")) : null).build();
        } else if (Objects.equals(task.getType(), PayNotifyTypeEnum.REFUND.getType())) {
            request = PayRefundNotifyReqDTO.builder().merchantOrderId(task.getMerchantOrderId())
                    .payRefundId(task.getDataId()).build();
        } else if (Objects.equals(task.getType(), PayNotifyTypeEnum.TRANSFER.getType())) {
            request = new PayTransferNotifyReqDTO().setMerchantTransferId(task.getMerchantTransferId())
                    .setPayTransferId(task.getDataId());
        } else {
            throw new RuntimeException("未知的通知任务类型：" + JsonUtils.toJsonString(task));
        }
        Map<String, String> headers = new HashMap<>();
        TenantUtils.addTenantHeader(headers, task.getTenantId());

        try (HttpResponse response = HttpUtil.createPost(task.getNotifyUrl())
                .body(JsonUtils.toJsonString(request)).addHeaders(headers)
                .timeout((int) NOTIFY_TIMEOUT_MILLIS).execute()) {
            return JsonUtils.parseObject(response.body(), CommonResult.class);
        }
    }

    @VisibleForTesting
    Integer processNotifyResult(PayNotifyTaskDO task, CommonResult<?> invokeResult, Throwable invokeException) {
        PayNotifyTaskDO updateTask = new PayNotifyTaskDO()
                .setId(task.getId())
                .setLastExecuteTime(LocalDateTime.now())
                .setNotifyTimes(task.getNotifyTimes() + 1);

        if (invokeResult != null && invokeResult.isSuccess()) {
            updateTask.setStatus(PayNotifyStatusEnum.SUCCESS.getStatus());
            notifyTaskMapper.updateById(updateTask);
            return updateTask.getStatus();
        }

        if (updateTask.getNotifyTimes() >= PayNotifyTaskDO.NOTIFY_FREQUENCY.length) {
            updateTask.setStatus(PayNotifyStatusEnum.FAILURE.getStatus());
            notifyTaskMapper.updateById(updateTask);
            return updateTask.getStatus();
        }
        updateTask.setNextNotifyTime(addTime(Duration.ofSeconds(PayNotifyTaskDO.NOTIFY_FREQUENCY[updateTask.getNotifyTimes()])));
        updateTask.setStatus(invokeException != null ? PayNotifyStatusEnum.REQUEST_FAILURE.getStatus()
                : PayNotifyStatusEnum.REQUEST_SUCCESS.getStatus());
        notifyTaskMapper.updateById(updateTask);
        return updateTask.getStatus();
    }

    @Override
    public PayNotifyTaskDO getNotifyTask(Long id) {
        return notifyTaskMapper.selectById(id);
    }

    @Override
    public PageResult<PayNotifyTaskDO> getNotifyTaskPage(PayNotifyTaskPageReqVO pageReqVO) {
        return notifyTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayNotifyLogDO> getNotifyLogList(Long taskId) {
        return notifyLogMapper.selectListByTaskId(taskId);
    }

    private PayNotifyServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }
}
