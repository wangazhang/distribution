# 汇付天下支付模块

本模块实现了基于汇付天下SDK的支付功能，支持多种支付方式，如微信小程序支付、微信扫码支付、支付宝支付等。

## 模块结构

- `HuiPayClientConfig` - 汇付天下支付配置类
- `AbstractHuiPayClient` - 汇付天下支付抽象客户端，实现了通用的支付逻辑
- `HuiWxLitePayClient` - 汇付天下微信小程序支付客户端实现

## 使用方法

### 1. 配置支付渠道

在系统中添加一个汇付天下支付渠道，配置示例：

```json
{
  "huifuId": "商户号",
  "productId": "产品号",
  "systemId": "系统ID",
  "publicKey": "公钥",
  "privateKey": "私钥",
  "signType": "RSA",
  "logLevel": "INFO"
}
```

### 2. 发起支付

```java
// 构建支付请求
PayOrderUnifiedReqDTO reqDTO = new PayOrderUnifiedReqDTO();
reqDTO.setOutTradeNo("订单号");
reqDTO.setSubject("商品标题");
reqDTO.setBody("商品描述");
reqDTO.setPrice(100); // 金额，单位：分
reqDTO.setUserIp("用户IP");
reqDTO.setNotifyUrl("回调地址");
reqDTO.setExpireTime(LocalDateTime.now().plusHours(2)); // 过期时间

// 微信小程序支付需要传递openid
Map<String, String> channelExtras = new HashMap<>();
channelExtras.put("openid", "用户openid");
reqDTO.setChannelExtras(channelExtras);

// 调用支付接口
PayClient payClient = payClientFactory.getPayClient(渠道编号);
PayOrderRespDTO respDTO = payClient.unifiedOrder(reqDTO);

// 返回支付参数给前端
return respDTO.getDisplayContent();
```

### 3. 处理支付回调

```java
@PostMapping("/notify/huifu/wx-lite")
public String notifyHuifuWxLite(@RequestParam Map<String, String> params,
                             @RequestBody String body) {
    // 获取支付客户端
    PayClient payClient = payClientFactory.getPayClient(渠道编号);
    // 解析通知
    PayOrderRespDTO respDTO = payClient.parseOrderNotify(params, body);
    
    // 处理支付结果
    if (PayOrderStatusRespEnum.SUCCESS.getStatus().equals(respDTO.getStatus())) {
        // 支付成功，更新订单状态
    }
    
    // 返回成功
    return "success";
}
```

### 4. 发起退款

```java
// 构建退款请求
PayRefundUnifiedReqDTO reqDTO = new PayRefundUnifiedReqDTO();
reqDTO.setOutTradeNo("原订单号");
reqDTO.setOutRefundNo("退款单号");
reqDTO.setPayPrice(200); // 原支付金额，单位：分
reqDTO.setRefundPrice(100); // 退款金额，单位：分
reqDTO.setReason("用户申请退款"); // 退款原因
reqDTO.setNotifyUrl("退款回调地址");

// 调用退款接口
PayClient payClient = payClientFactory.getPayClient(渠道编号);
PayRefundRespDTO respDTO = payClient.unifiedRefund(reqDTO);

// 处理退款结果
if (PayRefundStatusRespEnum.isSuccess(respDTO.getStatus())) {
    // 退款成功
} else if (PayRefundStatusRespEnum.isProcess(respDTO.getStatus())) {
    // 退款处理中，等待回调通知
} else {
    // 退款失败
}
```

### 5. 处理退款回调

```java
@PostMapping("/notify/huifu/refund")
public String notifyHuifuRefund(@RequestParam Map<String, String> params,
                               @RequestBody String body) {
    // 获取支付客户端
    PayClient payClient = payClientFactory.getPayClient(渠道编号);
    // 解析退款通知
    PayRefundRespDTO respDTO = payClient.parseRefundNotify(params, body);

    // 处理退款结果
    if (PayRefundStatusRespEnum.isSuccess(respDTO.getStatus())) {
        // 退款成功，更新退款订单状态
    } else if (PayRefundStatusRespEnum.isFailure(respDTO.getStatus())) {
        // 退款失败，更新退款订单状态
    }

    // 返回成功
    return "success";
}
```

### 6. 查询退款结果

```java
// 查询退款结果
PayClient payClient = payClientFactory.getPayClient(渠道编号);
PayRefundRespDTO respDTO = payClient.getRefund("原订单号", "退款单号");

// 处理查询结果
if (PayRefundStatusRespEnum.isSuccess(respDTO.getStatus())) {
    // 退款成功
    LocalDateTime successTime = respDTO.getSuccessTime();
    String channelRefundNo = respDTO.getChannelRefundNo();
} else if (PayRefundStatusRespEnum.isFailure(respDTO.getStatus())) {
    // 退款失败
    String errorCode = respDTO.getChannelErrorCode();
    String errorMsg = respDTO.getChannelErrorMsg();
}
```

## 退款功能特性

### 支持的退款方式
- **统一退款接口**：通过 `unifiedRefund` 方法发起退款申请
- **异步退款通知**：支持汇付天下的退款回调通知
- **退款查询**：支持主动查询退款结果

### 退款状态说明
- `PROCESS`：退款处理中，等待汇付天下处理
- `SUCCESS`：退款成功
- `FAILURE`：退款失败

### 退款金额处理
- 支持部分退款和全额退款
- 金额单位为分，自动转换为汇付要求的元格式
- 退款金额不能超过原支付金额

### 退款回调数据格式
汇付天下的退款回调数据为URL编码格式，包含以下关键字段：
- `resp_code`：响应码
- `resp_desc`：响应描述
- `resp_data`：退款详细数据（JSON格式，URL编码）

退款详细数据包含：
- `trans_stat`：退款状态（S:成功, F:失败, P:处理中, I:初始）
- `mer_ord_id`：退款单号
- `hf_seq_id`：汇付流水号
- `ord_amt`：退款金额
- `end_time`：退款完成时间
- `huifu_id`：商户号

## 扩展其他支付方式

如需扩展其他支付方式，可参考 `HuiWxLitePayClient` 的实现，创建对应的支付客户端类，并在 `PayClientFactoryImpl` 中注册。

例如，要实现汇付天下微信扫码支付：

1. 创建 `HuiWxNativePayClient` 类，继承 `AbstractHuiPayClient`
2. 实现相关方法，如 `doUnifiedOrder`、`doParseOrderNotify` 等
3. 在 `PayClientFactoryImpl` 中注册该客户端

```java
// 注册支付客户端
clientClass.put(HUIFU_WX_NATIVE, HuiWxNativePayClient.class);
``` 