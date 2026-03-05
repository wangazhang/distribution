package com.hissp.distribution.module.mb.domain.service.mbdt.price;

import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPricePageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPriceSaveReqVO;
import jakarta.validation.*;
import com.hissp.distribution.module.mb.dal.dataobject.material.MaterialRestockPriceDO;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.mb.adapter.controller.app.mbdt.price.vo.AppMbPriceRespVO;

import java.util.List;

/**
 * 补货价格表：记录不同用户等级对应的商品补货价格 Service 接口
 *
 * @author azhanga
 */
public interface MaterialRestockPriceService {

    /**
     * 创建补货价格表：记录不同用户等级对应的商品补货价格
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaterialRestockPrice(@Valid MaterialRestockPriceSaveReqVO createReqVO);

    /**
     * 更新补货价格表：记录不同用户等级对应的商品补货价格
     *
     * @param updateReqVO 更新信息
     */
    void updateMaterialRestockPrice(@Valid MaterialRestockPriceSaveReqVO updateReqVO);

    /**
     * 删除补货价格表：记录不同用户等级对应的商品补货价格
     *
     * @param id 编号
     */
    void deleteMaterialRestockPrice(Long id);

    /**
     * 获得补货价格表：记录不同用户等级对应的商品补货价格
     *
     * @param id 编号
     * @return 补货价格表：记录不同用户等级对应的商品补货价格
     */
    MaterialRestockPriceDO getMaterialRestockPrice(Long id);

    /**
     * 获得补货价格表：记录不同用户等级对应的商品补货价格分页
     *
     * @param pageReqVO 分页查询
     * @return 补货价格表：记录不同用户等级对应的商品补货价格分页
     */
    PageResult<MaterialRestockPriceDO> getMaterialRestockPricePage(MaterialRestockPricePageReqVO pageReqVO);

    /**
     * 根据用户ID获取对应等级的补货价格列表
     * 
     * @param userId 用户ID
     * @return 补货价格列表
     */
    List<AppMbPriceRespVO> getMaterialRestockPriceListByUserId(Long userId);

    /**
     * 根据用户ID和产品ID获取补货价格
     * 
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 补货价格信息
     */
    AppMbPriceRespVO getMaterialRestockPriceByUserIdAndProductId(Long userId, Long productId);
}