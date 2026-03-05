package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseDeclarationResult;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseModifyBankCardDTO;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseSubMerchantDeclareDTO;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import com.upay.sdk.exception.RequestException;
import com.upay.sdk.executer.ResultListenerAdpater;
import com.upay.sdk.serviceprovider.v3_0.declaration.builder.DeclarationBuilder;
import com.upay.sdk.serviceprovider.v3_0.declaration.builder.ModifyBankCardBuilder;
import com.upay.sdk.serviceprovider.v3_0.declaration.builder.QueryProgressBuilder;
import com.upay.sdk.serviceprovider.v3_0.declaration.executer.DeclarationExecuter;
import com.upay.sdk.serviceprovider.v3_0.declaration.executer.ModifyBankCardExecuter;
import com.upay.sdk.serviceprovider.v3_0.declaration.executer.QueryProgressExecuter;
import com.upay.sdk.transferaccount.builder.OrderBuilder;
import com.upay.sdk.transferaccount.builder.QueryBuilder;
import com.upay.sdk.transferaccount.executer.TransferAccountOrderExecuter;
import com.upay.sdk.wallet.builder.WithdrawBuilder;
import com.upay.sdk.wallet.builder.WithdrawQueryBuilder;
import com.upay.sdk.wallet.executer.WithdrawExecuter;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 首信易账户服务客户端：负责子商户入网与查询
 */
@Slf4j
public class PayeaseAccountPayClient extends AbstractPayeasePayClient {

    private final DeclarationExecuter declarationExecuter = new DeclarationExecuter();
    private final QueryProgressExecuter queryProgressExecuter = new QueryProgressExecuter();
    private final ModifyBankCardExecuter modifyBankCardExecuter = new ModifyBankCardExecuter();
    private final TransferAccountOrderExecuter transferAccountExecuter = new TransferAccountOrderExecuter();
    private final WithdrawExecuter withdrawExecuter = new WithdrawExecuter();

    private static final String WITHDRAW_REQUEST_SUFFIX = "-WD";

    public PayeaseAccountPayClient(Long channelId, PayeasePayClientConfig config) {
        super(channelId, PayChannelEnum.PAYEASE_ACCOUNT.getCode(), config);
    }

    /**
     * 子商户报单
     */
    public PayeaseDeclarationResult declareSubMerchant(PayeaseSubMerchantDeclareDTO dto) {
        DeclarationBuilder builder = PayeaseDeclarationMapper.build(dto, config.getMerchantId());
        CompletableFuture<PayeaseDeclarationResult> future = new CompletableFuture<>();
        try {
            declarationExecuter.declare(builder, new ResultListenerAdpater() {
                @Override
                public void success(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, true));
                }

                @Override
                public void failure(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, false));
                }

                @Override
                public void pending(JSONObject jsonObject) {
                    future.complete(PayeaseDeclarationResult.pending(jsonObject, dto.getRequestId()));
                }
            });
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            future.completeExceptionally(ex);
            log.error("payease declare failed", ex);
            //throw new RuntimeException("payease declare failed", ex);
            return PayeaseDeclarationResult.builder()
                    .success(false)
                    .errorCode("UNKNOWN")
                    .errorMessage("UNKNOWN")
                    .requestId(dto.getRequestId())
                    .subMerchantId(dto.getSubMerchantId())
                    .raw(JSONObject.parseObject(ex.getMessage()))
                    .build();
        }
    }

    /**
     * 修改结算卡信息
     */
    public PayeaseDeclarationResult modifyBankCard(PayeaseModifyBankCardDTO dto) {
        ModifyBankCardBuilder builder = PayeaseModifyBankCardMapper.build(dto, config.getMerchantId());
        CompletableFuture<PayeaseDeclarationResult> future = new CompletableFuture<>();
        try {
            modifyBankCardExecuter.modify(builder, new ResultListenerAdpater() {
                @Override
                public void success(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, true));
                }

                @Override
                public void failure(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, false));
                }

                @Override
                public void pending(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, false));
                }
            });
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            future.completeExceptionally(ex);
            throw new RuntimeException("payease modify bank card failed", ex);
        }
    }

    /**
     * 报单进度查询
     */
    public PayeaseDeclarationResult querySubMerchant(String requestId, String subMerchantId) {
        QueryProgressBuilder builder = new QueryProgressBuilder()
                .setMerchantId(config.getMerchantId())
                .setRequestId(requestId)
                .setSubMerchantId(subMerchantId);
        CompletableFuture<PayeaseDeclarationResult> future = new CompletableFuture<>();
        try {
            queryProgressExecuter.query(builder, new ResultListenerAdpater() {
                @Override
                public void success(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, true));
                }

                @Override
                public void failure(JSONObject jsonObject) {
                    future.complete(parseResult(jsonObject, false));
                }

                @Override
                public void pending(JSONObject jsonObject) {
                    future.complete(PayeaseDeclarationResult.pending(jsonObject, requestId));
                }
            });
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            future.completeExceptionally(ex);
            throw new RuntimeException("payease query failed", ex);
        }
    }

    private PayeaseDeclarationResult parseResult(JSONObject jsonObject, boolean success) {
        return PayeaseDeclarationResult.builder()
                .success(success && isSuccessStatus(jsonObject))
                .status(readFirst(jsonObject, "status", "progress"))
                .errorCode(readFirst(jsonObject, "code", "errorCode"))
                .errorMessage(readFirst(jsonObject, "errorMessage", "message", "msg"))
                .requestId(readFirst(jsonObject, "requestId"))
                .subMerchantId(readFirst(jsonObject, "subMerchantId", "subMerchantCode"))
                .reviewStatus(readFirst(jsonObject, "subMerchantReviewStatus"))
                .reviewRemark(readFirst(jsonObject, "subMerchantReviewRemarks"))
                .postReviewStatus(readFirst(jsonObject, "postReviewStatus"))
                .postReviewRemark(readFirst(jsonObject, "postReviewRemark"))
                .electronicContractingUrl(readFirst(jsonObject, "electronicContractingUrl"))
                .certificateSupplementUrl(readFirst(jsonObject, "certificateSupplementUrl"))
                .raw(jsonObject)
                .build();
    }

    private boolean isSuccessStatus(JSONObject jsonObject) {
        String status = readFirst(jsonObject, "status", "progress");
        return StrUtil.equalsAnyIgnoreCase(status, "SUCCESS", "SUCCEED");
    }

    private String readFirst(JSONObject jsonObject, String... keys) {
        for (String key : keys) {
            String value = jsonObject.getString(key);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 解析首信易入网回调
     */
    public PayeaseDeclarationResult parseDeclarationNotify(Map<String, String> headers,
                                                           Map<String, String> params,
                                                           String body) {
        JSONObject jsonObject = resolveNotifyPayload(headers, params, body, "declaration");
        return parseResult(jsonObject, true);
    }

    private JSONObject toJsonObject(String body, Map<String, String> params) {
        if (StrUtil.isNotBlank(body)) {
            String trimmed = body.trim();
            if (trimmed.startsWith("{")) {
                try {
                    return JSONObject.parseObject(trimmed);
                } catch (Exception ignored) {
                }
            }
            JSONObject obj = new JSONObject();
            String[] pairs = trimmed.split("&");
            for (String pair : pairs) {
                if (StrUtil.isBlank(pair)) {
                    continue;
                }
                int idx = pair.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                obj.put(key, value);
            }
            if (!obj.isEmpty()) {
                return obj;
            }
        }
        if (params == null || params.isEmpty()) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        params.forEach((k, v) -> jsonObject.put(k, v));
        return jsonObject;
    }

    // ==================== 以下接口暂未使用，抛出 UnsupportedOperationException，防止误用 ====================

    @Override
    protected PayOrderInnerRespDTO doUnifiedOrder(PayOrderUnifiedInnerReqDTO reqDTO) {
        throw new UnsupportedOperationException("payease_account 不支持统一下单");
    }

    @Override
    protected PayOrderInnerRespDTO parseOrderResponse(JSONObject jsonObject, PayOrderUnifiedInnerReqDTO reqDTO) {
        throw new UnsupportedOperationException("payease_account 不支持解析支付响应");
    }

    @Override
    protected PayOrderInnerRespDTO doParseOrderNotify(java.util.Map<String, String> headers,
                                                      java.util.Map<String, String> params,
                                                      String body) {
        throw new UnsupportedOperationException("payease_account 不支持支付通知解析");
    }

    @Override
    protected PayOrderInnerRespDTO doGetOrder(String outTradeNo) {
        throw new UnsupportedOperationException("payease_account 不支持查询支付单");
    }

    @Override
    protected PayRefundInnerRespDTO doUnifiedRefund(PayRefundUnifiedInnerReqDTO reqDTO) {
        throw new UnsupportedOperationException("payease_account 不支持退款");
    }

    @Override
    protected PayRefundInnerRespDTO doParseRefundNotify(java.util.Map<String, String> headers,
                                                        java.util.Map<String, String> params,
                                                        String body) {
        throw new UnsupportedOperationException("payease_account 不支持退款通知");
    }

    @Override
    protected PayRefundInnerRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        throw new UnsupportedOperationException("payease_account 不支持查询退款");
    }

    @Override
    protected PayTransferInnerRespDTO doUnifiedTransfer(PayTransferUnifiedInnerReqDTO reqDTO) {
        PayeaseTransferExtras extras = PayeaseTransferExtras.from(reqDTO);
        OrderBuilder transferBuilder = PayeaseTransferMapper.buildTransferOrder(config, reqDTO, extras);
        PayeaseStageResult transferStage = submitTransferOrder(reqDTO.getOutTransferNo(), transferBuilder);
        if (transferStage.isFailure()) {
            return buildClosedResp(reqDTO.getOutTransferNo(), transferStage, null);
        }
        if (!transferStage.isSuccess()) {
            return buildPendingResp(reqDTO.getOutTransferNo(), transferStage);
        }
        String withdrawRequestId = buildWithdrawRequestId(reqDTO.getOutTransferNo());
        WithdrawBuilder withdrawBuilder =
                PayeaseTransferMapper.buildWithdrawOrder4Transfer(config, withdrawRequestId, reqDTO);
        PayeaseStageResult withdrawStage = submitWithdrawOrder(withdrawRequestId, withdrawBuilder);
        return buildFinalResp(reqDTO.getOutTransferNo(), transferStage, withdrawStage);
    }

    @Override
    protected PayTransferInnerRespDTO doParseTransferNotify(Map<String, String> headers,
                                                            Map<String, String> params,
                                                            String body) {
        JSONObject jsonObject = resolveNotifyPayload(headers, params, body, "transfer");
        PayeaseStageResult stageResult = PayeaseStageResult.from(Stage.WITHDRAW, StageState.fromStatus(
                jsonObject != null ? jsonObject.getString("status") : null), jsonObject, this::parseDateTime);
        String outTransferNo = extractOutTransferNo(stageResult.getRequestId());
        if (StrUtil.isBlank(outTransferNo)) {
            log.warn("[doParseTransferNotify][缺少 requestId 原文={}]", jsonObject);
            throw new IllegalArgumentException("首信易回调缺少 requestId");
        }
        if (stageResult.isSuccess()) {
            return PayTransferInnerRespDTO.successOf(stageResult.getChannelTransferNo(),
                    stageResult.getSuccessTime(), outTransferNo, stageResult.getRaw());
        }
        if (stageResult.isFailure()) {
            return PayTransferInnerRespDTO.closedOf(stageResult.getErrorCode(), stageResult.getErrorMessage(),
                    outTransferNo, stageResult.getRaw());
        }
        return PayTransferInnerRespDTO.dealingOf(stageResult.getChannelTransferNo(), outTransferNo,
                stageResult.getRaw());
    }

    /**
     * 统一处理首信易回调的解密与容错解析。
     */
    private JSONObject resolveNotifyPayload(Map<String, String> headers,
                                            Map<String, String> params,
                                            String body,
                                            String scene) {
        Map<String, String> safeHeaders = headers != null ? headers : Collections.emptyMap();
        Map<String, String> safeParams = params != null ? params : Collections.emptyMap();
        try {
            return decryptNotifyData(safeHeaders, safeParams, body);
        } catch (Exception ex) {
            log.warn("[resolveNotifyPayload][{} decrypt failed, fallback plain] {}", scene, ex.getMessage());
            JSONObject fallback = toJsonObject(body, safeParams);
            if (fallback == null) {
                throw new IllegalArgumentException("payease " + scene + " notify payload is blank");
            }
            return fallback;
        }
    }

    @Override
    protected PayTransferInnerRespDTO doGetTransfer(String outTransferNo, PayTransferTypeEnum type) {
        PayeaseStageResult transferStage = queryTransferStage(outTransferNo);
        if (transferStage.isFailure()) {
            return buildClosedResp(outTransferNo, transferStage, null);
        }
        if (!transferStage.isSuccess()) {
            return buildPendingResp(outTransferNo, transferStage);
        }
        String withdrawRequestId = buildWithdrawRequestId(outTransferNo);
        PayeaseStageResult withdrawStage = queryWithdrawStage4Transfer(withdrawRequestId,transferStage);
        if (withdrawStage.isFailure()) {
            return buildClosedResp(outTransferNo, transferStage, withdrawStage);
        }
        return buildFinalResp(outTransferNo, transferStage, withdrawStage);
    }

    private PayeaseStageResult submitTransferOrder(String requestId, OrderBuilder builder) {
        CompletableFuture<PayeaseStageResult> future = new CompletableFuture<>();
        try {
            transferAccountExecuter.bothOrder(builder, new PayeaseResultListener(future, Stage.TRANSFER, this::parseDateTime));
            return future.get(30, TimeUnit.SECONDS);
        } catch (RequestException e1){
            log.error("[submitTransferOrder][requestId={}] 调用首信易转账接口异常]", requestId, e1);
            return PayeaseStageResult.failure(Stage.TRANSFER, requestId, "RequestException",
                    e1.getMessage(), e1.getResponseData());
        } catch (Exception ex) {
            log.error("[submitTransferOrder][requestId={}] 调用首信易转账接口异常", requestId, ex);
            return PayeaseStageResult.failure(Stage.TRANSFER, requestId, "SDK_ERROR",
                    ex.getMessage(), null);
        }
    }

    private PayeaseStageResult submitWithdrawOrder(String requestId, WithdrawBuilder builder) {
        CompletableFuture<PayeaseStageResult> future = new CompletableFuture<>();
        try {
            withdrawExecuter.bothWithdraw(builder, new PayeaseResultListener(future, Stage.WITHDRAW, this::parseDateTime));
            return future.get(30, TimeUnit.SECONDS);
        }catch (RequestException e1){
            log.error("[submitWithdrawOrder][requestId={}] 调用首信易提现接口异常", requestId, e1);
            return PayeaseStageResult.failure(Stage.WITHDRAW, requestId, "RequestException",e1.getMessage(),e1.getResponseData());
        }
        catch (Exception ex) {
            log.error("[submitWithdrawOrder][requestId={}] 调用首信易提现接口异常", requestId, ex);
            return PayeaseStageResult.failure(Stage.WITHDRAW, requestId, "SDK_ERROR",
                    ex.getMessage(), null);
        }
    }

    private PayeaseStageResult queryTransferStage(String requestId) {
        CompletableFuture<PayeaseStageResult> future = new CompletableFuture<>();
        try {
            QueryBuilder builder = PayeaseTransferMapper.buildTransferQuery(config, requestId);
            transferAccountExecuter.bothQuery(builder, new PayeaseResultListener(future, Stage.TRANSFER, this::parseDateTime));

            return future.get(30, TimeUnit.SECONDS);
        }catch (RequestException e1){
            future.completeExceptionally(e1);
            log.error("[queryTransferStage][requestId={}] 获取首信易转账状态异常", requestId, e1);
            return PayeaseStageResult.failure(Stage.TRANSFER, requestId, "RequestException",
                    e1.getMessage(), e1.getResponseData());
        }
        catch (Exception ex) {
            future.completeExceptionally(ex);
            log.error("[queryTransferStage][requestId={}] 查询首信易转账状态异常", requestId, ex);
            return PayeaseStageResult.failure(Stage.TRANSFER, requestId, "SDK_ERROR", ex.getMessage(), JSONObject.parseObject(ex.getMessage()));
        }
    }

    private PayeaseStageResult queryWithdrawStage4Transfer(String requestId, PayeaseStageResult transferStage) {
        CompletableFuture<PayeaseStageResult> future = new CompletableFuture<>();
        try {
            WithdrawQueryBuilder builder =
                    PayeaseTransferMapper.buildWithdrawQuery4Transfer(config, requestId,String.valueOf(transferStage.getRaw().get("receiverId")));
            withdrawExecuter.bothWithdrawQuery(builder, new PayeaseResultListener(future, Stage.WITHDRAW, this::parseDateTime));
            return future.get(30, TimeUnit.SECONDS);
        }catch (RequestException e1){
            future.completeExceptionally(e1);
            log.error("[queryWithdrawStage][requestId={}] 获取首信易提现状态异常", requestId, e1);
            return PayeaseStageResult.failure(Stage.WITHDRAW, requestId, "RequestException",e1.getMessage(),e1.getResponseData());
        }catch (Exception ex) {
            future.completeExceptionally(ex);
            log.error("[queryWithdrawStage][requestId={}] 查询首信易提现状态异常", requestId, ex);
            return PayeaseStageResult.failure(Stage.WITHDRAW, requestId, "SDK_ERROR", ex.getMessage(), null);
        }
    }

    private PayTransferInnerRespDTO buildPendingResp(String outTransferNo, PayeaseStageResult transferStage) {
        return PayTransferInnerRespDTO.dealingOf(
                StrUtil.blankToDefault(transferStage.getChannelTransferNo(), transferStage.getRequestId()),
                outTransferNo,
                buildRawData(transferStage, null));
    }

    private PayTransferInnerRespDTO buildClosedResp(String outTransferNo,
                                                    PayeaseStageResult transferStage,
                                                    PayeaseStageResult withdrawStage) {
        PayeaseStageResult failed = withdrawStage != null ? withdrawStage : transferStage;
        return PayTransferInnerRespDTO.closedOf(StrUtil.blankToDefault(failed.getErrorCode(), "CHANNEL_ERROR"),
                StrUtil.blankToDefault(failed.getErrorMessage(), "渠道处理失败"),
                outTransferNo,
                buildRawData(transferStage, withdrawStage));
    }

    private PayTransferInnerRespDTO buildFinalResp(String outTransferNo,
                                                   PayeaseStageResult transferStage,
                                                   PayeaseStageResult withdrawStage) {
        if (withdrawStage == null || withdrawStage.getState() == StageState.PENDING) {
            return PayTransferInnerRespDTO.dealingOf(
                    withdrawStage != null ? withdrawStage.getChannelTransferNo() : transferStage.getChannelTransferNo(),
                    outTransferNo,
                    buildRawData(transferStage, withdrawStage));
        }
        if (withdrawStage.isFailure()) {
            return buildClosedResp(outTransferNo, transferStage, withdrawStage);
        }
        return PayTransferInnerRespDTO.successOf(
                withdrawStage.getChannelTransferNo(),
                withdrawStage.getSuccessTime(),
                outTransferNo,
                buildRawData(transferStage, withdrawStage));
    }

    private Map<String, Object> buildRawData(PayeaseStageResult transferStage, PayeaseStageResult withdrawStage) {
        Map<String, Object> raw = new HashMap<>(2);
        if (transferStage != null) {
            raw.put("transfer", transferStage.getRaw());
        }
        if (withdrawStage != null) {
            raw.put("withdraw", withdrawStage.getRaw());
        }
        return raw;
    }

    private String buildWithdrawRequestId(String outTransferNo) {
        return outTransferNo + WITHDRAW_REQUEST_SUFFIX;
    }

    private String extractOutTransferNo(String requestId) {
        if (StrUtil.isBlank(requestId)) {
            return null;
        }
        if (requestId.endsWith(WITHDRAW_REQUEST_SUFFIX)) {
            return StrUtil.removeSuffix(requestId, WITHDRAW_REQUEST_SUFFIX);
        }
        return requestId;
    }

    private static final class PayeaseResultListener extends ResultListenerAdpater {

        private final CompletableFuture<PayeaseStageResult> future;
        private final Stage stage;
        private final java.util.function.Function<String, LocalDateTime> timeParser;

        private PayeaseResultListener(CompletableFuture<PayeaseStageResult> future, Stage stage,
                                      java.util.function.Function<String, LocalDateTime> timeParser) {
            this.future = future;
            this.stage = stage;
            this.timeParser = timeParser;
        }

        @Override
        public void success(JSONObject jsonObject) {
            future.complete(PayeaseStageResult.success(stage, jsonObject, timeParser));
        }

        @Override
        public void failure(JSONObject jsonObject) {
            future.complete(PayeaseStageResult.failure(stage, jsonObject, timeParser));
        }

        @Override
        public void pending(JSONObject jsonObject) {
            future.complete(PayeaseStageResult.pending(stage, jsonObject, timeParser));
        }
    }

    private enum Stage {
        TRANSFER, WITHDRAW
    }

    private enum StageState {
        SUCCESS, PENDING, FAILURE;

        static StageState fromStatus(String status) {
            if (StrUtil.equalsIgnoreCase("SUCCESS", status)) {
                return SUCCESS;
            }
            if (StrUtil.equalsIgnoreCase("FAILED", status)
                    || StrUtil.equalsIgnoreCase("ERROR", status)
                    || StrUtil.equalsIgnoreCase("CANCEL", status)) {
                return FAILURE;
            }
            return PENDING;
        }
    }

    /**
     * 封装渠道返回的阶段性结果，便于统一转换为 {@link PayTransferInnerRespDTO}。
     */
    private static final class PayeaseStageResult {
        private final Stage stage;
        private final StageState state;
        private final String requestId;
        private final String channelTransferNo;
        private final LocalDateTime successTime;
        private final String errorCode;
        private final String errorMessage;
        private final JSONObject raw;

        private PayeaseStageResult(Stage stage, StageState state, String requestId,
                                   String channelTransferNo, LocalDateTime successTime,
                                   String errorCode, String errorMessage, JSONObject raw) {
            this.stage = stage;
            this.state = state;
            this.requestId = requestId;
            this.channelTransferNo = channelTransferNo;
            this.successTime = successTime;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.raw = raw;
        }

        static PayeaseStageResult success(Stage stage, JSONObject json,
                                          java.util.function.Function<String, LocalDateTime> timeParser) {
            return from(stage, StageState.SUCCESS, json, timeParser);
        }

        static PayeaseStageResult pending(Stage stage, JSONObject json,
                                          java.util.function.Function<String, LocalDateTime> timeParser) {
            return from(stage, StageState.PENDING, json, timeParser);
        }

        static PayeaseStageResult failure(Stage stage, JSONObject json,
                                          java.util.function.Function<String, LocalDateTime> timeParser) {
            return from(stage, StageState.FAILURE, json, timeParser);
        }

        static PayeaseStageResult failure(Stage stage, String requestId,
                                          String errorCode, String errorMessage, JSONObject raw) {
            return new PayeaseStageResult(stage, StageState.FAILURE, requestId,
                    null, null, errorCode, errorMessage, raw);
        }

        private static PayeaseStageResult from(Stage stage, StageState state, JSONObject json) {
            return from(stage, state, json, null);
        }

        static PayeaseStageResult from(Stage stage, StageState state, JSONObject json,
                                       java.util.function.Function<String, LocalDateTime> timeParser) {
            if (json == null) {
                return new PayeaseStageResult(stage, state, null, null,
                        null, null, null, new JSONObject());
            }
            String requestId = json.getString("requestId");
            String serialNumber = StrUtil.firstNonBlank(json.getString("serialNumber"),
                    json.getString("orderId"), json.getString("withdrawId"));
            String completeDateTime = json.getString("completeDateTime");
            String errorCode = StrUtil.blankToDefault(json.getString("code"), json.getString("errorCode"));
            String errorMessage = StrUtil.blankToDefault(json.getString("errorMessage"), json.getString("message"));
            LocalDateTime successTime = timeParser != null ? timeParser.apply(completeDateTime) : null;
            return new PayeaseStageResult(stage, state, requestId, serialNumber, successTime,
                    errorCode, errorMessage, json);
        }

        boolean isSuccess() {
            return StageState.SUCCESS == state;
        }

        boolean isFailure() {
            return StageState.FAILURE == state;
        }

        StageState getState() {
            return state;
        }

        String getRequestId() {
            return requestId;
        }

        String getChannelTransferNo() {
            return channelTransferNo;
        }

        LocalDateTime getSuccessTime() {
            return successTime;
        }

        String getErrorCode() {
            return errorCode;
        }

        String getErrorMessage() {
            return errorMessage;
        }

        JSONObject getRaw() {
            return raw;
        }
    }
}
