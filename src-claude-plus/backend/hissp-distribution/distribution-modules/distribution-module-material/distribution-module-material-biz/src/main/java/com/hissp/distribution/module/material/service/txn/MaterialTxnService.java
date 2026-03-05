package com.hissp.distribution.module.material.service.txn;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnPageReqVO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnRespVO;

/**
 * 物料流水 Service 接口
 */
public interface MaterialTxnService {

    /**
     * 获得物料流水分页
     *
     * @param pageReqVO 分页查询
     * @return 物料流水分页
     */
    PageResult<MaterialTxnRespVO> getTxnPage(MaterialTxnPageReqVO pageReqVO);

    /**
     * 检查业务幂等性
     *
     * @param bizKey 业务key
     * @param materialId 物料ID
     * @param direction 方向值
     * @return 是否已存在
     */
    boolean checkIdempotency(String bizKey, Long materialId, Integer direction);

    /**
     * 创建物料流水记录
     *
     * @param act 物料操作DTO
     * @param balanceAfter 操作后余额
     */
    void createTxnRecord(MaterialActDTO act, Integer balanceAfter);

}

