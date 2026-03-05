package com.hissp.distribution.module.mb.dal.mysql.trade;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderPageReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderPageReqVO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

/**
 * mb订单表：记录代理用户的mb所有业务类型的订单信息 Mapper
 *
 * @author azhanga
 */
@Mapper
public interface MbOrderMapper extends BaseMapperX<MbOrderDO> {

    default PageResult<MbOrderDO> selectPage(MbOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MbOrderDO>()
                .eqIfPresent(MbOrderDO::getId, reqVO.getOrderId())
                .eqIfPresent(MbOrderDO::getProductId, reqVO.getProductId())
                .inIfPresent(MbOrderDO::getProductId, reqVO.getProductIds())
                .eqIfPresent(MbOrderDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MbOrderDO::getAgentUserId, reqVO.getAgentUserId())
                .inIfPresent(MbOrderDO::getAgentUserId, reqVO.getAgentUserIds())
                .eqIfPresent(MbOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MbOrderDO::getPaymentId, reqVO.getPaymentId())
                .betweenIfPresent(MbOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MbOrderDO::getId));
    }

    default PageResult<MbOrderDO> selectPage(AppMbOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MbOrderDO>()
                .eqIfPresent(MbOrderDO::getAgentUserId, reqVO.getAgentUserId()) // 必须按用户ID过滤
                .eqIfPresent(MbOrderDO::getProductId, reqVO.getProductId())
                .eqIfPresent(MbOrderDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MbOrderDO::getStatus, reqVO.getStatus()) // 支持状态筛选
                .eqIfPresent(MbOrderDO::getPaymentId, reqVO.getPaymentId())
                .betweenIfPresent(MbOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MbOrderDO::getId));
    }

    /**
     * 根据支付单ID查询订单
     *
     * @param paymentId 支付单ID
     * @return 订单
     */
    default MbOrderDO selectByPaymentId(String paymentId) {
        return selectOne(new LambdaQueryWrapperX<MbOrderDO>()
                .eq(MbOrderDO::getPaymentId, paymentId));
    }

    /**
     * 根据主键与状态更新订单，避免并发覆盖
     *
     * @param id 订单编号
     * @param status 当前状态
     * @param updateObj 更新数据
     * @return 受影响行数
     */
    default int updateByIdAndStatus(Long id, String status, MbOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<MbOrderDO>()
                .eq(MbOrderDO::getId, id)
                .eq(MbOrderDO::getStatus, status));
    }

}
