package com.hissp.distribution.module.material.service.balance;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceAdjustReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalancePageReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceRespVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceStatRespVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialBalanceDO;

import java.util.List;

/**
 * 物料余额 Service 接口
 */
public interface MaterialBalanceService {

    /**
     * 获得物料余额分页
     *
     * @param pageReqVO 分页查询
     * @return 物料余额分页
     */
    PageResult<MaterialBalanceRespVO> getBalancePage(MaterialBalancePageReqVO pageReqVO);

    /**
     * 根据用户ID和物料ID获取余额
     *
     * @param userId 用户ID
     * @param materialId 物料ID
     * @return 物料余额DO
     */
    MaterialBalanceDO getBalanceByUserIdAndMaterialId(Long userId, Long materialId);

    /**
     * 批量获取用户的多个物料余额
     *
     * @param userId 用户ID
     * @param materialIds 物料ID列表
     * @return 物料余额DO列表
     */
    List<MaterialBalanceDO> getBalancesByUserIdAndMaterialIds(Long userId, List<Long> materialIds);

    /**
     * 创建或获取物料余额记录
     *
     * @param userId 用户ID
     * @param materialId 物料ID
     * @return 物料余额DO
     */
    MaterialBalanceDO createOrGetBalance(Long userId, Long materialId);

    /**
     * 使用乐观锁更新余额
     *
     * @param balance 当前余额对象
     * @param newAvailableBalance 新的可用余额
     * @return 更新是否成功
     */
    boolean updateBalanceWithOptimisticLock(MaterialBalanceDO balance, Integer newAvailableBalance);

    /**
     * 调整物料余额
     *
     * @param adjustReqVO 调整请求参数
     * @param operatorId 操作人ID
     */
    void adjustBalance(MaterialBalanceAdjustReqVO adjustReqVO, Long operatorId);

    /**
     * 获取物料余额统计信息
     *
     * @return 统计信息
     */
    MaterialBalanceStatRespVO getBalanceStatistics();

}

