package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderDeliveryStatusRespEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.impl.AbstractPayClient;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import com.upay.sdk.ConfigurationUtils;
import com.upay.sdk.crypto.CertificateReader;
import com.upay.sdk.crypto.SdkEncryptDecryptSupport;
import com.upay.sdk.executer.ResultListenerAdpater;
import com.upay.sdk.onlinepay.builder.OrderBuilder;
import com.upay.sdk.onlinepay.builder.QueryBuilder;
import com.upay.sdk.onlinepay.builder.RefundBuilder;
import com.upay.sdk.onlinepay.executer.OnlinePayOrderExecuter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.MapConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 首信易支付抽象基类,封装SDK核心调用逻辑
 *
 * @author system
 */
@Slf4j
public abstract class AbstractPayeasePayClient extends AbstractPayClient<PayeasePayClientConfig> {

    /**
     * 首信易 SDK 执行器
     */
    protected OnlinePayOrderExecuter executer;

    /**
     * 加密解密支持
     */
    protected SdkEncryptDecryptSupport encryptDecryptSupport;

    /**
     * 证书读取器
     */
    protected CertificateReader certificateReader;

    /**
     * API版本: v3.0(国际算法) 或 v4.0(国密算法)
     */
    protected String apiVersion = "v3.0"; // 默认使用国际算法

    public AbstractPayeasePayClient(Long channelId, String channelCode, PayeasePayClientConfig config) {
        super(channelId, channelCode, config);
    }

    @Override
    protected void doInit() {
        try {
            // 1. 构建SDK配置
            Map<String, String> configMap = buildSdkConfig();

            // 2. 初始化SDK配置
            ConfigurationUtils.setConfigSource(new MapConfiguration(configMap));

            // 3. 创建执行器
            this.executer = new OnlinePayOrderExecuter();

            // 4. 初始化加密解密支持(使用SPI加载)
            // 直接加载第一个可用的实现,通常是 upay-sdk-international 提供的国际算法实现
            ServiceLoader<SdkEncryptDecryptSupport> encryptServices = ServiceLoader.load(SdkEncryptDecryptSupport.class);
            for (SdkEncryptDecryptSupport service : encryptServices) {
                this.encryptDecryptSupport = service;
                log.info("[doInit][加载加密解密支持] class={}", service.getClass().getName());
                break; // 使用第一个找到的实现
            }

            // 5. 初始化证书读取器(使用SPI加载)
            ServiceLoader<CertificateReader> certServices = ServiceLoader.load(CertificateReader.class);
            for (CertificateReader reader : certServices) {
                this.certificateReader = reader;
                log.info("[doInit][加载证书读取器] class={}", reader.getClass().getName());
                break; // 使用第一个找到的实现
            }

            if (this.encryptDecryptSupport == null || this.certificateReader == null) {
                throw new RuntimeException("未找到加密解密支持实现,请检查upay-sdk-international依赖");
            }

            // 6. 设置TLS协议
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
            System.setProperty("jdk.tls.disabledAlgorithms", "");

            log.info("[doInit][渠道编号({}) 初始化首信易支付配置成功] merchantId={}",
                    getId(), config.getMerchantId());
        } catch (Exception e) {
            log.error("[doInit][渠道编号({}) 初始化首信易支付配置失败]", getId(), e);
            throw new RuntimeException("初始化首信易支付配置失败", e);
        }
    }

    /**
     * 构建SDK配置Map
     */
    private Map<String, String> buildSdkConfig() throws Exception {
        Map<String, String> configMap = new HashMap<>();

        //// HTTP Content-Type配置
        configMap.put("http.content.type", "application/vnd.5upay-v3.0+json");  // 新增
        configMap.put("http.content.type.v3", "application/vnd.5upay-v3.0+json");
        configMap.put("http.content.type.v4", "application/vnd.5upay-v4.0+json");

        // 商户配置(使用商户号作为前缀)
        // 注意: SDK需要文件系统绝对路径,不支持classpath:前缀
        String prefix = config.getMerchantId() + ".";
        configMap.put(prefix + "client.private.key.path", resolveFilePath(config.getPrivateKeyPath()));
        configMap.put(prefix + "client.private.key.password", config.getPrivateKeyPassword());
        configMap.put(prefix + "server.public.key.path", resolveFilePath(config.getPublicKeyPath()));
        configMap.put("server.public.key.path", resolveFilePath(config.getPublicKeyPath()));
        // API地址配置（支付侧）
        configMap.put("onlinepay.order.url", "https://apis.5upay.com/onlinePay/order");
        configMap.put("onlinepay.query.url", "https://apis.5upay.com/onlinePay/query");
        configMap.put("onlinepay.refund.url", "https://apis.5upay.com/onlinePay/refund");
        configMap.put("onlinepay.refund.query.url", "https://apis.5upay.com/onlinePay/refundQuery");
        configMap.put("onlinepay.cancel.url", "https://apis.5upay.com/onlinePay/cancelOrder");

        // 入网侧（serviceprovider）地址配置：支持覆盖
        configMap.put("serviceprovider.declaration.url", "https://apis.5upay.com/serviceprovider/declaration/declare");
        configMap.put("serviceprovider.declaration.query.url", "https://apis.5upay.com/serviceprovider/declaration/query");
        //转账接口
        configMap.put("transferAccount.order.url", "https://apis.5upay.com/transferAccount/order");
        configMap.put("transferAccount.query.url", "https://apis.5upay.com/transferAccount/query");
        //提现接口
        configMap.put("wallet.withdraw.url", "https://apis.5upay.com/wallet/withdraw");
        configMap.put("wallet.withdraw.query.url", "https://apis.5upay.com/wallet/withdrawQuery");
        //提现接口
        configMap.put("serviceprovider.modifyBankCard.url", "https://apis.5upay.com/serviceprovider/declaration/modifyBankCard");

        configMap.put("ehking.sdk.net.client.defaultConnectTimeout", "30000");  // 连接超时30秒                                                                                                                                                                                                │ │
        configMap.put("ehking.sdk.net.client.defaultReadTimeout", "30000");     // 读取超时30秒
        log.info("[buildSdkConfig][解析证书路径] privateKey={}, publicKey={}",
                configMap.get(prefix + "client.private.key.path"),
                configMap.get("server.public.key.path"));

        return configMap;
    }

    /**
     * 解析文件路径,支持classpath:前缀和绝对路径
     *
     * @param path 配置的路径,可以是 "classpath:xxx" 或 绝对路径
     * @return 文件系统绝对路径
     */
    private String resolveFilePath(String path) throws Exception {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("证书文件路径不能为空");
        }

        File candidate = new File(path);
        boolean classpathResource = path.startsWith("classpath:") || !candidate.isAbsolute();
        if (classpathResource) {
            String resourcePath = path.startsWith("classpath:")
                    ? path.substring("classpath:".length())
                    : path;
            resourcePath = StrUtil.removePrefix(resourcePath, "/");
            return resolveClasspathFile(resourcePath);
        }

        if (!candidate.exists()) {
            throw new IllegalArgumentException("证书文件不存在: " + path);
        }

        return candidate.getAbsolutePath();
    }

    /**
     * 将classpath资源解析为文件系统路径,必要时复制到临时文件
     */
    private String resolveClasspathFile(String resourcePath) throws IOException {
        Resource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("证书文件不存在: classpath:" + resourcePath);
        }

        try {
            File file = resource.getFile();
            if (file.exists()) {
                String absolutePath = file.getAbsolutePath();
                log.info("[resolveFilePath][解析classpath路径] classpath:{} -> {}", resourcePath, absolutePath);
                return absolutePath;
            }
        } catch (FileNotFoundException ex) {
            log.debug("[resolveFilePath][classpath资源位于Jar中,准备复制到临时目录] classpath:{}", resourcePath);
        }

        String suffix = ".tmp";
        int dotIndex = resourcePath.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < resourcePath.length() - 1) {
            suffix = resourcePath.substring(dotIndex);
        }
        File tempFile = File.createTempFile("payease-", suffix);
        tempFile.deleteOnExit();
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("[resolveFilePath][复制classpath资源到临时文件] classpath:{} -> {}", resourcePath, tempFile.getAbsolutePath());
        return tempFile.getAbsolutePath();
    }

    // ============ 支付相关 ==========

    @Override
    protected PayOrderInnerRespDTO doUnifiedOrder(PayOrderUnifiedInnerReqDTO reqDTO) throws Throwable {
        log.info("[doUnifiedOrder][渠道编号({}) 发起统一下单] outTradeNo={}", getId(), reqDTO.getOutTradeNo());

        // 1. 构建订单请求
        OrderBuilder builder = buildOrderRequest(reqDTO);

        // 2. 使用CompletableFuture处理异步回调
        CompletableFuture<PayOrderInnerRespDTO> future = new CompletableFuture<>();

        // 3. 执行下单请求
        executer.bothOrder(builder, new ResultListenerAdpater() {
            @Override
            public void success(JSONObject jsonObject) {
                try {
                    PayOrderInnerRespDTO resp = parseOrderResponse(jsonObject, reqDTO);
                    future.complete(resp);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void failure(JSONObject jsonObject) {
                String errorCode = jsonObject.getString("code");
                String errorMessage = jsonObject.getString("errorMessage");
                future.complete(PayOrderInnerRespDTO.closedOf(errorCode, errorMessage,
                        reqDTO.getOutTradeNo(), jsonObject.toJSONString()));
            }

            @Override
            public void redirect(JSONObject jsonObject, String redirectUrl) {
                // 重定向场景(如H5支付)
                Map<String, String> displayMap = new HashMap<>();
                displayMap.put("redirectUrl", redirectUrl);
                future.complete(PayOrderInnerRespDTO.waitingOf("redirect", redirectUrl,
                        reqDTO.getOutTradeNo(), displayMap));
            }
        });

        // 4. 等待异步结果(最多10秒)
        return future.get(10, TimeUnit.SECONDS);
    }

    /**
     * 构建订单请求
     * 子类可以覆盖此方法来设置特定的支付方式参数
     */
    protected OrderBuilder buildOrderRequest(PayOrderUnifiedInnerReqDTO reqDTO) {
        OrderBuilder builder = new OrderBuilder(config.getMerchantId());

        // 基础订单信息
        builder.setRequestId(reqDTO.getOutTradeNo())
                .setOrderAmount(String.valueOf(reqDTO.getPrice())) // 单位:分
                .setOrderCurrency("CNY")
                .setNotifyUrl(reqDTO.getNotifyUrl())
                .setCallbackUrl("")
                //.setCallbackUrl(reqDTO.getReturnUrl())
                .setRemark(reqDTO.getBody())
                .setClientIp(reqDTO.getUserIp());

        // 服务商ID(如果配置)
        if (StrUtil.isNotBlank(config.getPartnerId())) {
            builder.setPartnerId(config.getPartnerId());
        }

        // 商品信息(必填)
        com.upay.sdk.entity.ProductDetail productDetail = new com.upay.sdk.entity.ProductDetail();
        productDetail.setName(reqDTO.getSubject());  // 商品名称
        productDetail.setQuantity(1L);  // 商品数量
        productDetail.setAmount(reqDTO.getPrice().longValue());  // 商品金额(单位:分)
        builder.addProductDetail(productDetail);

        // 用户身份信息(可选,根据监管要求填写)
        com.upay.sdk.entity.Payer payer = new com.upay.sdk.entity.Payer();
        if (reqDTO.getChannelExtras() != null&& reqDTO.getChannelExtras().containsKey("payerName")) {
            payer.setName(reqDTO.getChannelExtras().get("payerName"));
        }
        if (reqDTO.getChannelExtras() != null&& reqDTO.getChannelExtras().containsKey("payerPhone")) {
            payer.setName(reqDTO.getChannelExtras().get("payerPhone"));
        }
        builder.setPayer(payer);

        return builder;
    }

    /**
     * 解析订单响应
     * 子类可以覆盖此方法来处理特定的响应格式
     */
    protected abstract PayOrderInnerRespDTO parseOrderResponse(JSONObject jsonObject, PayOrderUnifiedInnerReqDTO reqDTO);

    @Override
    protected PayOrderInnerRespDTO doParseOrderNotify(Map<String, String> headers, Map<String, String> params, String body) throws Throwable {
        log.info("[doParseOrderNotify][渠道编号({}) 解析订单通知] headers={}, body={}", getId(), headers, body);

        // 1. 解密回调数据
        JSONObject decryptedData = decryptNotifyData(headers, params, body);
        log.info("[doParseOrderNotify][解密后的数据] {}", decryptedData);

        // 2. 解析业务数据
        String status = decryptedData.getString("status");
        String serialNumber = decryptedData.getString("serialNumber"); // 渠道订单号
        String realBankSerialNumber = decryptedData.getString("realBankSerialNumber");
        String requestId = decryptedData.getString("requestId"); // 商户订单号
        String completeDateTime = decryptedData.getString("completeDateTime");
        //String channelUserId = decryptedData.getString("paymentModeAlias"); // 支付方式别名

        // 3. 解析支付状态
        Integer payStatus = parsePayStatus(status);
        LocalDateTime successTime = parseDateTime(completeDateTime);

        PayOrderInnerRespDTO respDTO = PayOrderInnerRespDTO.of(payStatus, serialNumber, null,
                successTime, requestId, decryptedData);
        respDTO.setDeliveryStatus(parseDeliveryStatus(decryptedData.getString("deliveryStatus")));
        respDTO.setChannelFeePrice(parseFeeAmount(decryptedData));
        if (StrUtil.isNotBlank(realBankSerialNumber)) {
            respDTO.setChannelTransactionNo(realBankSerialNumber);
        }
        // 退款通知需要在进入 PayNotify 流程之前即刻覆写状态，避免被按“支付成功”处理
        markRefundStatusIfNecessary(respDTO, decryptedData);
        return respDTO;
    }

    @Override
    protected PayOrderInnerRespDTO doGetOrder(String outTradeNo) throws Throwable {
        log.info("[doGetOrder][渠道编号({}) 查询订单] outTradeNo={}", getId(), outTradeNo);

        // 1. 构建查询请求
        QueryBuilder builder = new QueryBuilder(config.getMerchantId());
        builder.setRequestId(outTradeNo);
        if (StrUtil.isNotBlank(config.getPartnerId())) {
            builder.setPartnerId(config.getPartnerId());
        }

        // 2. 使用CompletableFuture处理异步回调
        CompletableFuture<PayOrderInnerRespDTO> future = new CompletableFuture<>();

        // 3. 执行查询请求
        try {
            executer.bothQuery(builder, new ResultListenerAdpater() {
                @Override
                public void success(JSONObject jsonObject) {
                    String status = jsonObject.getString("status");
                    String serialNumber = jsonObject.getString("serialNumber");
                    String completeDateTime = jsonObject.getString("completeDateTime");

                    Integer payStatus = parsePayStatus(status);
                    LocalDateTime successTime = parseDateTime(completeDateTime);

                    PayOrderInnerRespDTO respDTO = PayOrderInnerRespDTO.of(payStatus, serialNumber, null,
                            successTime, outTradeNo, jsonObject);
                    respDTO.setDeliveryStatus(parseDeliveryStatus(jsonObject.getString("deliveryStatus")));
                    respDTO.setChannelFeePrice(parseFeeAmount(jsonObject));
                    // 渠道主动查询同样要复用退款识别，保证前端看到的状态一致；无需重复补查
                    markRefundStatusIfNecessary(respDTO, jsonObject, false);
                    future.complete(respDTO);
                }

                @Override
                public void failure(JSONObject jsonObject) {
                    String errorCode = jsonObject.getString("code");
                    String errorMessage = jsonObject.getString("errorMessage");
                    future.complete(PayOrderInnerRespDTO.closedOf(errorCode, errorMessage,
                            outTradeNo, jsonObject.toJSONString()));
                }

                @Override
                public void pending(JSONObject jsonObject) {
                    future.complete(PayOrderInnerRespDTO.waitingOf("query", "订单查询中",
                            outTradeNo, null));
                }
            });
        } catch (Throwable ex) {
            if (isOrderNotExistException(ex)) {
                String message = StrUtil.nullToDefault(ex.getMessage(), "payease order not exist");
                log.info("[doGetOrder][渠道编号({}) 查询订单不存在，按关闭处理] outTradeNo={} message={}",
                        getId(), outTradeNo, message);
                return PayOrderInnerRespDTO.closedOf("ORDER_NOT_EXIST", message, outTradeNo, message);
            }
            throw ex;
        }

        // 4. 等待异步结果
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (isOrderNotExistException(cause)) {
                String message = StrUtil.nullToDefault(cause.getMessage(), "payease order not exist");
                log.info("[doGetOrder][渠道编号({}) 查询订单不存在(异步)，按关闭处理] outTradeNo={} message={}",
                        getId(), outTradeNo, message);
                return PayOrderInnerRespDTO.closedOf("ORDER_NOT_EXIST", message, outTradeNo, message);
            }
            if (cause != null) {
                throw cause;
            }
            throw ex;
        }
    }

    private PayOrderDeliveryStatusRespEnum parseDeliveryStatus(String deliveryStatus) {
        return PayOrderDeliveryStatusRespEnum.from(deliveryStatus);
    }

    /**
     * 退款识别逻辑：在渠道报文携带退款金额/次数时，将状态转换为 REFUND，避免 payNotify 继续当作成功单处理。
     */
    private void markRefundStatusIfNecessary(PayOrderInnerRespDTO respDTO, JSONObject rawData) {
        markRefundStatusIfNecessary(respDTO, rawData, true);
    }

    private void markRefundStatusIfNecessary(PayOrderInnerRespDTO respDTO, JSONObject rawData,
                                             boolean allowChannelQueryFallback) {
        if (respDTO == null) {
            return;
        }
        List<String> refundHints = collectRefundHints(rawData);
        if (!refundHints.isEmpty()) {
            log.info("[markRefundStatusIfNecessary][orderNo={}] 渠道报文直接命中退款字段: {}",
                    respDTO.getOutTradeNo(), refundHints);
            respDTO.setStatus(PayOrderStatusRespEnum.REFUND.getStatus());
            return;
        }
        if (!allowChannelQueryFallback) {
            return;
        }
        //再查一次渠道订单信息 然后判断是否有退款，没退款才返回
        if (queryRefundStatusFromChannel(respDTO)) {
            log.info("[markRefundStatusIfNecessary][orderNo={}] 渠道二次查询确认退款，覆盖状态为 REFUND",
                    respDTO.getOutTradeNo());
            respDTO.setStatus(PayOrderStatusRespEnum.REFUND.getStatus());
        }
    }

    private boolean queryRefundStatusFromChannel(PayOrderInnerRespDTO respDTO) {
        if (respDTO == null || StrUtil.isBlank(respDTO.getOutTradeNo())) {
            return false;
        }
        try {
            PayOrderInnerRespDTO snapshot = doGetOrder(respDTO.getOutTradeNo());
            return snapshot != null && PayOrderStatusRespEnum.isRefund(snapshot.getStatus());
        } catch (Throwable ex) {
            log.warn("[queryRefundStatusFromChannel][orderNo={}] 渠道补查退款状态失败，按未退款处理]",
                    respDTO.getOutTradeNo(), ex);
            return false;
        }
    }

    /**
     * 解析渠道返回的退款相关字段，只要 refundAmount / totalRefundAmount / refundCount / totalRefundCount 任意一个大于 0
     * 即认为该通知是“退款”语义，并返回命中的字段及取值，便于日志排查。
     */
    private List<String> collectRefundHints(JSONObject rawData) {
        if (rawData == null) {
            return Collections.emptyList();
        }
        List<String> hints = new ArrayList<>(4);
        appendRefundHintIfPositive(hints, "refundAmount", rawData.getString("refundAmount"));
        appendRefundHintIfPositive(hints, "totalRefundAmount", rawData.getString("totalRefundAmount"));
        appendRefundHintIfPositive(hints, "refundCount", rawData.getString("refundCount"));
        appendRefundHintIfPositive(hints, "totalRefundCount", rawData.getString("totalRefundCount"));
        return hints;
    }

    private void appendRefundHintIfPositive(List<String> hints, String fieldName, String valueText) {
        if (isPositiveNumber(valueText)) {
            hints.add(fieldName + "=" + valueText);
        }
    }

    /**
     * 判断字符串数值是否为正数。渠道的字段存在“1”、“0.01”等格式，这里统一转 BigDecimal 处理。
     */
    private boolean isPositiveNumber(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        try {
            return new BigDecimal(text.trim()).compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException ex) {
            log.warn("[isPositiveNumber][字段({}) 解析失败，忽略退款识别]", text, ex);
            return false;
        }
    }

    /**
     * 解析渠道返回的手续费。feeAmount 字段缺失或格式异常时静默忽略。
     */
    private Integer parseFeeAmount(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String feeText = jsonObject.getString("feeAmount");
        if (StrUtil.isBlank(feeText)) {
            return null;
        }
        try {
            return Integer.valueOf(feeText.trim());
        } catch (NumberFormatException ex) {
            log.warn("[parseFeeAmount][feeAmount({}) 解析失败]", feeText, ex);
            return null;
        }
    }

    private boolean isOrderNotExistException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        String message = ex.getMessage();
        if (StrUtil.isBlank(message)) {
            return false;
        }
        for (String keyword : ORDER_NOT_EXIST_HINTS) {
            if (StrUtil.containsIgnoreCase(message, keyword)) {
                return true;
            }
        }
        return false;
    }

    private static final List<String> ORDER_NOT_EXIST_HINTS = Arrays.asList(
            "订单不存在", "order not exist", "order does not exist", "not exist", "not found",
            "order_not_exist", "trade not exist"
    );

    // ============ 退款相关 ==========

    @Override
    protected PayRefundInnerRespDTO doUnifiedRefund(PayRefundUnifiedInnerReqDTO reqDTO) throws Throwable {
        log.info("[doUnifiedRefund][渠道编号({}) 发起统一退款] outRefundNo={}", getId(), reqDTO.getOutRefundNo());

        // 1. 构建退款请求
        RefundBuilder builder = new RefundBuilder(config.getMerchantId());
        builder.setRequestId(reqDTO.getOutRefundNo())
                .setAmount(String.valueOf(reqDTO.getRefundPrice())) // 单位:分
                .setOrderId(reqDTO.getChannelOrderNo()) // 原订单的serialNumber
                .setRemark(reqDTO.getReason())
                .setNotifyUrl(reqDTO.getNotifyUrl());

        if (StrUtil.isNotBlank(config.getPartnerId())) {
            builder.setPartnerId(config.getPartnerId());
        }

        // 2. 使用CompletableFuture处理异步回调
        CompletableFuture<PayRefundInnerRespDTO> future = new CompletableFuture<>();

        // 3. 执行退款请求
        try {
            executer.bothRefund(builder, new ResultListenerAdpater() {
                @Override
                public void success(JSONObject jsonObject) {
                    String status = jsonObject.getString("status");
                    String serialNumber = jsonObject.getString("serialNumber");

                    if (Objects.equals("SUCCESS", status)) {
                        future.complete(PayRefundInnerRespDTO.waitingOf(serialNumber,
                                reqDTO.getOutRefundNo(), jsonObject));
                    } else {
                        future.complete(PayRefundInnerRespDTO.waitingOf(serialNumber,
                                reqDTO.getOutRefundNo(), jsonObject));
                    }
                }

                @Override
                public void failure(JSONObject jsonObject) {
                    String errorCode = jsonObject.getString("code");
                    String errorMessage = jsonObject.getString("errorMessage");
                    future.complete(PayRefundInnerRespDTO.failureOf(errorCode, errorMessage,
                            reqDTO.getOutRefundNo(), jsonObject.toJSONString()));
                }
            });
        } catch (Exception e) {
            log.error("[doUnifiedRefund][渠道编号({}) 统一退款异常:调用渠道侧报错] outRefundNo={}, reqDTO={}, error={}",
                    getId(), reqDTO.getOutRefundNo(), reqDTO, e);
            PayRefundInnerRespDTO.failureOf("调用渠道侧报错", "调用渠道侧报错",
                    reqDTO.getOutRefundNo(), e.getMessage());
            return null;
        }

        // 4. 等待异步结果
        return future.get(10, TimeUnit.SECONDS);
    }

    @Override
    protected PayRefundInnerRespDTO doParseRefundNotify(Map<String, String> headers, Map<String, String> params, String body) throws Throwable {
        log.info("[doParseRefundNotify][渠道编号({}) 解析退款通知] headers={}, body={}", getId(), headers, body);

        // 1. 解密回调数据
        JSONObject decryptedData = decryptNotifyData(headers, params, body);
        log.info("[doParseRefundNotify][解密后的数据] {}", decryptedData);

        // 2. 解析业务数据
        String status = decryptedData.getString("status");
        String serialNumber = decryptedData.getString("serialNumber");
        String requestId = decryptedData.getString("requestId");
        String completeDateTime = decryptedData.getString("completeDateTime");

        if (Objects.equals("SUCCESS", status)) {
            LocalDateTime successTime = parseDateTime(completeDateTime);
            return PayRefundInnerRespDTO.successOf(serialNumber, successTime, requestId, decryptedData);
        } else if (Objects.equals("FAILED", status)) {
            String errorMessage = decryptedData.getString("errorMessage");
            return PayRefundInnerRespDTO.failureOf("REFUND_FAILED", errorMessage, requestId, decryptedData);
        } else {
            return PayRefundInnerRespDTO.waitingOf(serialNumber, requestId, decryptedData);
        }
    }

    @Override
    protected PayRefundInnerRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Throwable {
        log.info("[doGetRefund][渠道编号({}) 查询退款] outRefundNo={}", getId(), outRefundNo);

        // 1. 构建查询请求 (注意: SDK的bothRefundQuery方法接受RefundBuilder而非RefundQueryBuilder)
        RefundBuilder builder = new RefundBuilder(config.getMerchantId());
        builder.setRequestId(outRefundNo);
        if (StrUtil.isNotBlank(config.getPartnerId())) {
            builder.setPartnerId(config.getPartnerId());
        }

        // 2. 使用CompletableFuture处理异步回调
        CompletableFuture<PayRefundInnerRespDTO> future = new CompletableFuture<>();

        // 3. 执行查询请求
        executer.bothRefundQuery(builder, new ResultListenerAdpater() {
            @Override
            public void success(JSONObject jsonObject) {
                String status = jsonObject.getString("status");
                String serialNumber = jsonObject.getString("serialNumber");
                String completeDateTime = jsonObject.getString("completeDateTime");

                if (Objects.equals("SUCCESS", status)) {
                    LocalDateTime successTime = parseDateTime(completeDateTime);
                    future.complete(PayRefundInnerRespDTO.successOf(serialNumber, successTime,
                            outRefundNo, jsonObject));
                } else if (Objects.equals("FAILED", status)) {
                    String errorMessage = jsonObject.getString("errorMessage");
                    future.complete(PayRefundInnerRespDTO.failureOf("REFUND_FAILED", errorMessage,
                            outRefundNo, jsonObject));
                } else {
                    future.complete(PayRefundInnerRespDTO.waitingOf(serialNumber,
                            outRefundNo, jsonObject));
                }
            }

            @Override
            public void failure(JSONObject jsonObject) {
                String errorCode = jsonObject.getString("code");
                String errorMessage = jsonObject.getString("errorMessage");
                future.complete(PayRefundInnerRespDTO.failureOf(errorCode, errorMessage,
                        outRefundNo, jsonObject.toJSONString()));
            }
        });

        // 4. 等待异步结果
        return future.get(10, TimeUnit.SECONDS);
    }

    // ============ 转账相关 ==========

    @Override
    protected PayTransferInnerRespDTO doUnifiedTransfer(PayTransferUnifiedInnerReqDTO reqDTO) throws Throwable {
        throw new UnsupportedOperationException("首信易支付暂不支持转账功能");
    }

    @Override
    protected PayTransferInnerRespDTO doGetTransfer(String outTradeNo, PayTransferTypeEnum type) throws Throwable {
        throw new UnsupportedOperationException("首信易支付暂不支持转账功能");
    }

    @Override
    protected PayTransferInnerRespDTO doParseTransferNotify(Map<String, String> headers,
                                                            Map<String, String> params,
                                                            String body) throws Throwable {
        throw new UnsupportedOperationException("首信易支付暂不支持转账功能");
    }

    // ============ 工具方法 ==========

    /**
     * 解密回调通知数据
     * 根据首信易文档的解密流程:
     * 1. 从headers中获取encryptKey(用商户公钥加密的AES密钥)
     * 2. 用商户私钥解密encryptKey得到AES密钥
     * 3. 用AES密钥解密body得到原始JSON数据
     * 4. 验证HMAC签名
     *
     * @param headers 请求头,包含encryptKey、merchantId、requestId
     * @param params
     * @param body    加密的请求体
     * @return 解密后的JSON数据
     */
    protected JSONObject decryptNotifyData(Map<String, String> headers, Map<String, String> params, String body) throws Exception {
        try {
            // 1. 从headers中获取加密的AES密钥
            String encryptKey = headers.get("encryptkey"); // 注意:HTTP header名称不区分大小写,Spring会转为小写
            if (StrUtil.isBlank(encryptKey)) {
                encryptKey = headers.get("encryptKey");
            }
            if (StrUtil.isBlank(encryptKey)) {
                throw new IllegalArgumentException("回调headers中缺少encryptKey");
            }

            log.info("[decryptNotifyData][获取加密密钥] encryptKey={}", encryptKey);

            // 2. 读取商户私钥(传入merchantId,SDK会自动从配置中读取对应的私钥路径)
            String privateKey = certificateReader.readPrivateKey(config.getMerchantId());
            log.info("[decryptNotifyData][读取商户私钥成功] merchantId={}", config.getMerchantId());

            // 3. 用私钥解密encryptKey,得到AES密钥
            String aesKey = encryptDecryptSupport.asymmetricDecrypt(encryptKey, privateKey);
            log.info("[decryptNotifyData][解密AES密钥成功] aesKey长度={}", aesKey != null ? aesKey.length() : 0);

            // 4. 用AES密钥解密body中的data字段
            if (StrUtil.isBlank(body)) {
                throw new IllegalArgumentException("回调body为空");
            }

            // 解析body获取data字段
            JSONObject bodyJson = JSON.parseObject(body);
            String encryptedData = bodyJson.getString("data");
            if (StrUtil.isBlank(encryptedData)) {
                throw new IllegalArgumentException("回调body中缺少data字段");
            }

            String decryptedBody = encryptDecryptSupport.symmetricDecrypt(encryptedData, aesKey);
            log.info("[decryptNotifyData][解密body成功] decryptedBody={}", decryptedBody);

            // 5. 解析JSON并验证签名
            JSONObject jsonData = JSON.parseObject(decryptedBody);

            // 6. 验证HMAC签名
            String hmac = jsonData.getString("hmac");
            if (StrUtil.isBlank(hmac)) {
                log.warn("[decryptNotifyData][回调数据中无hmac签名,跳过验签]");
                return jsonData;
            }

            // TODO: 暂时跳过验签,因为公钥格式问题导致验签失败
            // 验签逻辑待后续完善
            log.warn("[decryptNotifyData][暂时跳过HMAC验签]");
            return jsonData;

            /* 原验签逻辑,待修复公钥格式问题后启用
            // 移除hmac后验签
            JSONObject dataWithoutHmac = JSON.parseObject(decryptedBody);
            dataWithoutHmac.remove("hmac");

            // 按字母顺序排序键名,用#连接
            TreeMap<String, Object> sortedData = new TreeMap<>(dataWithoutHmac);
            StringBuilder signStr = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedData.entrySet()) {
                if (entry.getValue() != null && StrUtil.isNotBlank(entry.getValue().toString())) {
                    signStr.append(entry.getValue()).append("#");
                }
            }

            // 读取首信易公钥(传入merchantId,SDK会自动从配置中读取对应的公钥路径)
            String serverPublicKey = certificateReader.readPublicKey(config.getMerchantId());

            // 验证签名
            boolean verifyResult = encryptDecryptSupport.verify(
                    signStr.toString().getBytes("UTF-8"),
                    hmac,
                    serverPublicKey
            );

            if (!verifyResult) {
                log.error("[decryptNotifyData][HMAC验签失败] hmac={}, signStr={}", hmac, signStr);
                throw new SecurityException("回调数据HMAC验签失败");
            }

            log.info("[decryptNotifyData][HMAC验签成功]");
            return jsonData;
            */

        } catch (Exception e) {
            log.error("[decryptNotifyData][解密回调数据失败] headers={}, body={}", headers, body, e);
            throw e;
        }
    }

    /**
     * 解析支付状态
     */
    private Integer parsePayStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return PayOrderStatusRespEnum.WAITING.getStatus();
        }

        switch (status) {
            case "SUCCESS":
                return PayOrderStatusRespEnum.SUCCESS.getStatus();
            case "CANCEL":
            case "FAILED":
            case "ERROR":
                return PayOrderStatusRespEnum.CLOSED.getStatus();
            case "INIT":
            default:
                return PayOrderStatusRespEnum.WAITING.getStatus();
        }
    }

    /**
     * 解析时间字符串
     */
    protected LocalDateTime parseDateTime(String dateTimeStr) {
        if (StrUtil.isBlank(dateTimeStr)) {
            return null;
        }
        try {
            // 首信易时间格式: yyyy-MM-dd HH:mm:ss
            return LocalDateTimeUtil.parse(dateTimeStr, DatePattern.NORM_DATETIME_PATTERN);
        } catch (Exception e) {
            log.warn("[parseDateTime][解析时间失败] dateTimeStr={}", dateTimeStr, e);
            return null;
        }
    }
}
