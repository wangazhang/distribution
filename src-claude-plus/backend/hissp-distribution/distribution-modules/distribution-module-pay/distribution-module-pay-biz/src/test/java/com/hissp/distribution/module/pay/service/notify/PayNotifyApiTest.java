//package com.hissp.distribution.module.pay.service.notify;
//
//import com.hissp.distribution.module.pay.api.notify.PayNotifyApi;
//import com.hissp.distribution.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import jakarta.annotation.Resource;
//
///**
// * 支付通知API测试类
// * 演示如何使用PayNotifyApi注册监听器
// *
// * @author azhanga
// */
//@SpringBootTest
//@Slf4j
//public class PayNotifyApiTest {
//
//    @Resource
//    private PayNotifyApi payNotifyApi;
//
//    @Test
//    public void testAddListener() {
//        // 注册支付通知监听器
//        payNotifyApi.addListener(this::onPayOrderNotify);
//
//        // 可以注册多个监听器
//        payNotifyApi.addListener(notification -> {
//            log.info("另一个监听器收到支付通知: {}", notification);
//        });
//
//        log.info("成功注册支付通知监听器");
//    }
//
//    /**
//     * 处理支付订单通知
//     *
//     * @param notification 支付通知
//     */
//    private void onPayOrderNotify(PayOrderNotifyReqDTO notification) {
//        log.info("收到支付通知: {}", notification);
//
//        // 这里可以编写处理支付成功的业务逻辑
//        // 例如: 更新订单状态、增加用户积分等
//    }
//}