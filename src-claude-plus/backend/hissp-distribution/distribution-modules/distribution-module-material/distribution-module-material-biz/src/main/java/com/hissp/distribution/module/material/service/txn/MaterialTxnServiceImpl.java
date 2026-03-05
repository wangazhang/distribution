package com.hissp.distribution.module.material.service.txn;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnPageReqVO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnRespVO;
import com.hissp.distribution.module.material.convert.MaterialConvert;
import com.hissp.distribution.module.material.convert.MaterialTxnConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialTxnDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialTxnMapper;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 物料流水 Service 实现类
 */
@Service
@Validated
public class MaterialTxnServiceImpl implements MaterialTxnService {

    @Resource
    private MaterialTxnMapper txnMapper;
    @Resource
    private MaterialDefinitionService definitionService;
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public PageResult<MaterialTxnRespVO> getTxnPage(MaterialTxnPageReqVO pageReqVO) {
        PageResult<MaterialTxnDO> pageResult = txnMapper.selectPage(pageReqVO);
        if (pageResult == null || CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        // Enrich with user and material names
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(CollectionUtils.convertSet(pageResult.getList(), MaterialTxnDO::getUserId));
        Map<Long, MaterialDefinitionDO> definitionMap = definitionService.getDefinitionMap(CollectionUtils.convertSet(pageResult.getList(), MaterialTxnDO::getMaterialId));

        List<MaterialTxnRespVO> respList = CollectionUtils.convertList(pageResult.getList(), txn -> {
            MaterialTxnRespVO respVO = MaterialTxnConvert.INSTANCE.convert(txn);
            if (userMap.containsKey(txn.getUserId())) {
                respVO.setNickname(userMap.get(txn.getUserId()).getNickname());
            }
            if (definitionMap.containsKey(txn.getMaterialId())) {
                respVO.setMaterialName(definitionMap.get(txn.getMaterialId()).getName());
            }
            return respVO;
        });

        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public boolean checkIdempotency(String bizKey, Long materialId, Integer direction) {
        long count = txnMapper.selectCount(new com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX<MaterialTxnDO>()
                .eq(MaterialTxnDO::getBizKey, bizKey)
                .eq(MaterialTxnDO::getMaterialId, materialId)
                .eq(MaterialTxnDO::getDirection, direction)
        );
        return count > 0;
    }

    @Override
    public void createTxnRecord(MaterialActDTO act, Integer balanceAfter) {
        MaterialTxnDO txn = MaterialConvert.INSTANCE.convert(act);
        txn.setBalanceAfter(balanceAfter == null ? 0 : balanceAfter);
        txnMapper.insert(txn);
    }
}

