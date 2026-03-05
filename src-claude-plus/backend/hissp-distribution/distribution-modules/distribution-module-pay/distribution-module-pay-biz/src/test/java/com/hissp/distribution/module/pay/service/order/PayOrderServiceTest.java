package com.hissp.distribution.module.pay.service.order;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.hissp.distribution.framework.test.core.ut.BaseDbAndRedisUnitTest;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.channel.PayChannelDO;
import com.hissp.distribution.module.pay.dal.dataobject.order.PayOrderDO;
import com.hissp.distribution.module.pay.dal.mysql.order.PayOrderMapper;
import com.hissp.distribution.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import com.hissp.distribution.module.pay.framework.pay.config.PayProperties;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.channel.PayChannelService;
import com.hissp.distribution.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.List;

import static com.hissp.distribution.framework.common.util.date.LocalDateTimeUtils.*;
import static com.hissp.distribution.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.hissp.distribution.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.hissp.distribution.framework.test.core.util.AssertUtils.assertServiceException;
import static com.hissp.distribution.framework.test.core.util.RandomUtils.*;
import static com.hissp.distribution.module.pay.enums.ErrorCodeConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link PayOrderServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Import({PayOrderServiceImpl.class, PayNoRedisDAO.class})
public class PayOrderServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayOrderServiceImpl orderService;

    @Resource
    private PayOrderMapper orderMapper;


    @MockBean
    private PayProperties properties;
    @MockBean
    private PayAppService appService;
    @MockBean
    private PayChannelService channelService;
    @MockBean
    private PayNotifyService notifyService;

    @BeforeEach
    public void setUp() {
        when(properties.getOrderNotifyUrl()).thenReturn("http://127.0.0.1");
    }

    @Test
    public void testGetOrder_id() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class);
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();

        // 调用
        PayOrderDO dbOrder = orderService.getOrder(id);
        // 断言
        assertPojoEquals(dbOrder, order);
    }

    @Test
    public void testGetOrder_appIdAndMerchantOrderId() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class);
        orderMapper.insert(order);
        // 准备参数
        Long appId = order.getAppId();
        String merchantOrderId = order.getMerchantOrderId();

        // 调用
        PayOrderDO dbOrder = orderService.getOrder(appId, merchantOrderId);
        // 断言
        assertPojoEquals(dbOrder, order);
    }

    @Test
    public void testGetOrderCountByAppId() {
        // mock 数据（PayOrderDO）
        PayOrderDO order01 = randomPojo(PayOrderDO.class);
        orderMapper.insert(order01);
        PayOrderDO order02 = randomPojo(PayOrderDO.class);
        orderMapper.insert(order02);
        // 准备参数
        Long appId = order01.getAppId();

        // 调用
        Long count = orderService.getOrderCountByAppId(appId);
        // 断言
        assertEquals(count, 1L);
    }

    @Test
    public void testGetOrderPage() {
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId("110");
            o.setChannelOrderNo("220");
            o.setNo("330");
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setCreateTime(buildTime(2018, 1, 15));
        });
        orderMapper.insert(dbOrder);
        // 测试 appId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantOrderId(randomString())));
        // 测试 channelOrderNo 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelOrderNo(randomString())));
        // 测试 no 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setNo(randomString())));
        // 测试 status 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())));
        // 测试 createTime 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setCreateTime(buildTime(2019, 1, 1))));
        // 准备参数
        PayOrderPageReqVO reqVO = new PayOrderPageReqVO();
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId("11");
        reqVO.setChannelOrderNo("22");
        reqVO.setNo("33");
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2018, 1, 10, 2018, 1, 30));

        // 调用
        PageResult<PayOrderDO> pageResult = orderService.getOrderPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbOrder, pageResult.getList().get(0));
    }

    @Test
    public void testGetOrderList() {
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId("110");
            o.setChannelOrderNo("220");
            o.setNo("330");
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setCreateTime(buildTime(2018, 1, 15));
        });
        orderMapper.insert(dbOrder);
        // 测试 appId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantOrderId(randomString())));
        // 测试 channelOrderNo 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelOrderNo(randomString())));
        // 测试 no 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setNo(randomString())));
        // 测试 status 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())));
        // 测试 createTime 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setCreateTime(buildTime(2019, 1, 1))));
        // 准备参数
        PayOrderExportReqVO reqVO = new PayOrderExportReqVO();
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId("11");
        reqVO.setChannelOrderNo("22");
        reqVO.setNo("33");
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2018, 1, 10, 2018, 1, 30));

        // 调用
        List<PayOrderDO> list = orderService.getOrderList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbOrder, list.get(0));
    }

    @Test
    public void testCreateOrder_success() {
        // mock 参数
        PayOrderCreateReqDTO reqDTO = randomPojo(PayOrderCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("10")
                        .setSubject(randomString()).setBody(randomString()));
        // mock 方法
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setOrderNotifyUrl("http://127.0.0.1"));
        when(appService.validPayApp(eq(reqDTO.getAppKey()))).thenReturn(app);

        // 调用
        Long orderId = orderService.createOrder(reqDTO);
        // 断言
        PayOrderDO order = orderMapper.selectById(orderId);
        assertPojoEquals(order, reqDTO);
        assertEquals(order.getAppId(), 1L);
        assertEquals(order.getNotifyUrl(), "http://127.0.0.1");
        assertEquals(order.getStatus(), PayOrderStatusEnum.WAITING.getStatus());
        assertEquals(order.getRefundPrice(), 0);
    }

    @Test
    public void testCreateOrder_exists() {
        // mock 参数
        PayOrderCreateReqDTO reqDTO = randomPojo(PayOrderCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("10"));
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class,  o -> o.setAppId(1L).setMerchantOrderId("10"));
        orderMapper.insert(dbOrder);
        // mock 方法
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setOrderNotifyUrl("http://127.0.0.1"));
        when(appService.validPayApp(eq(reqDTO.getAppKey()))).thenReturn(app);

        // 调用
        Long orderId = orderService.createOrder(reqDTO);
        // 断言
        PayOrderDO order = orderMapper.selectById(orderId);
        assertPojoEquals(dbOrder, order);
    }

    @Test
    public void testSubmitOrder_notFound() {
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class);
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_NOT_FOUND);
    }

    @Test
    public void testSubmitOrder_notWaiting() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.REFUND.getStatus()));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_STATUS_IS_NOT_WAITING);
    }

    @Test
    public void testSubmitOrder_isSuccess() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_STATUS_IS_SUCCESS);
    }

    @Test
    public void testSubmitOrder_expired() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setExpireTime(addTime(Duration.ofDays(-1))));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_IS_EXPIRED);
    }

    @Test
    public void testSubmitOrder_channelNotFound() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setAppId(1L).setExpireTime(addTime(Duration.ofDays(1))));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                .setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
        String userIp = randomString();
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(channelService.validPayChannel(eq(1L), eq(PayChannelEnum.ALIPAY_APP.getCode())))
                .thenReturn(channel);

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), CHANNEL_NOT_FOUND);
    }













    @Test
    public void testUpdateOrderRefundPrice_notFound() {
        // 准备参数
        Long id = randomLongId();
        Integer incrRefundPrice = randomInteger();

        // 调用，并断言异常
        assertServiceException(() -> orderService.updateOrderRefundPrice(id, incrRefundPrice),
                PAY_ORDER_NOT_FOUND);
    }

    @Test
    public void testUpdateOrderRefundPrice_waiting() {
        testUpdateOrderRefundPrice_waitingOrClosed(PayOrderStatusEnum.WAITING.getStatus());
    }

    @Test
    public void testUpdateOrderRefundPrice_closed() {
        testUpdateOrderRefundPrice_waitingOrClosed(PayOrderStatusEnum.CLOSED.getStatus());
    }

    private void testUpdateOrderRefundPrice_waitingOrClosed(Integer status) {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(status));
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();
        Integer incrRefundPrice = randomInteger();

        // 调用，并断言异常
        assertServiceException(() -> orderService.updateOrderRefundPrice(id, incrRefundPrice),
                PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
    }

    @Test
    public void testUpdateOrderRefundPrice_priceExceed() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setRefundPrice(1).setPrice(10));
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();
        Integer incrRefundPrice = 10;

        // 调用，并断言异常
        assertServiceException(() -> orderService.updateOrderRefundPrice(id, incrRefundPrice),
                REFUND_PRICE_EXCEED);
    }

    @Test
    public void testUpdateOrderRefundPrice_refund() {
        testUpdateOrderRefundPrice_refundOrSuccess(PayOrderStatusEnum.REFUND.getStatus());
    }

    @Test
    public void testUpdateOrderRefundPrice_success() {
        testUpdateOrderRefundPrice_refundOrSuccess(PayOrderStatusEnum.SUCCESS.getStatus());
    }

    private void testUpdateOrderRefundPrice_refundOrSuccess(Integer status) {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(status).setRefundPrice(1).setPrice(10));
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();
        Integer incrRefundPrice = 8;

        // 调用
        orderService.updateOrderRefundPrice(id, incrRefundPrice);
        // 断言
        order.setRefundPrice(9).setStatus(PayOrderStatusEnum.REFUND.getStatus());
        assertPojoEquals(order, orderMapper.selectOne(null),
                "updateTime", "updater");
    }





}
