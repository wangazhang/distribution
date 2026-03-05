package com.hissp.distribution.module.mb.domain.service.trade;

import java.util.List;
import java.util.Map;

import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderDetailRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderSaveReqVO;
import jakarta.validation.*;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderCreateReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderRespVO;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.framework.common.pojo.PageResult;

/**
 * mb订单表：记录代理用户的mb所有业务类型的订单信息 Service 接口
 *
 * @author azhanga
 */
public interface MbOrderService {

    /**
     * 创建mb订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMbOrder(@Valid MbOrderSaveReqVO createReqVO);

    /**
     * 更新mb订单
     *
     * @param updateReqVO 更新信息
     */
    void updateMbOrder(@Valid MbOrderSaveReqVO updateReqVO);

    /**
     * 删除mb订单
     *
     * @param id 编号
     */
    void deleteMbOrder(Long id);

    /**
     * 获得mb订单
     *
     * @param id 编号
     * @return mb订单
     */
    MbOrderDO getMbOrder(Long id);

    /**
     * 获得mb订单分页
     *
     * @param pageReqVO 分页查询
     * @return mb订单分页
     */
    PageResult<MbOrderDO> getMbOrderPage(MbOrderPageReqVO pageReqVO);

    /**
     * 获得管理后台订单分页
     *
     * @param pageReqVO 分页查询
     * @return 订单分页
     */
    PageResult<MbOrderRespVO> getAdminOrderPage(MbOrderPageReqVO pageReqVO);

    /**
     * 获得管理后台订单详情
     *
     * @param id 订单编号
     * @return 订单详情
     */
    MbOrderDetailRespVO getAdminOrderDetail(Long id);

    /**
     * 创建用户补货订单
     *
     * @param createReqVO 创建请求
     * @return 订单ID
     */
    Long createUserMaterialRestockOrder(@Valid AppMbOrderCreateReqVO createReqVO);

    /**
     * 获取用户订单分页列表
     *
     * @param pageReqVO 分页请求
     * @return 订单分页结果
     */
    PageResult<AppMbOrderRespVO> getUserMaterialOrderPage(AppMbOrderPageReqVO pageReqVO);

    /**
     * 获取用户订单详情
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    AppMbOrderRespVO getUserMaterialOrderDetail(Long orderId, Long userId);

    /**
     * 获取订单状态列表
     *
     * @return 状态列表
     */
    List<Map<String, Object>> getOrderStatusList();

    /**
     * 管理后台手动更新订单状态
     *
     * @param orderId 订单编号
     * @param status 目标状态
     */
    void adminUpdateOrderStatus(Long orderId, String status);

    /**
     * 重新触发虚拟发货
     *
     * @param orderId 订单编号
     */
    void retryVirtualDelivery(Long orderId);

    /**
     * 【用户】确认收货
     *
     * @param userId 用户编号
     * @param orderId 订单编号
     */
    void receiveOrderByMember(Long userId, Long orderId);

    /**
     * 补偿校验渠道确认收货
     *
     * @param orderId 订单编号
     * @return 是否同步为已收货
     */
    boolean checkOrderReceiveStatus(Long orderId);

    /**
     * 支付系统回调的渠道妥投结果处理
     *
     * @param orderId       订单编号
     * @param payOrderId    支付单编号
     * @param deliveryStatus 渠道妥投状态
     */
    void processOrderPaidNotify(Long orderId, Long payOrderId, String deliveryStatus);

    /**
     * 管理后台发起退款
     *
     * @param orderId 订单编号
     * @param reason 退款原因
     */
    void adminRefundOrder(Long orderId, String reason, String password);

    /**
     * 是否启用后台退款密码
     *
     * @return true 表示需要密码
     */
    boolean isAdminRefundPasswordEnabled();
}
