package com.hissp.distribution.module.mb.domain.service.mbdt.price;

import com.hissp.distribution.framework.common.util.id.IdGeneratorUtil;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPricePageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPriceSaveReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.mbdt.price.vo.AppMbPriceRespVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.module.mb.dal.dataobject.material.MaterialRestockPriceDO;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;

import com.hissp.distribution.module.mb.dal.mysql.material.MaterialRestockPriceMapper;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.mb.enums.ErrorCodeConstants.MATERIAL_RESTOCK_PRICE_NOT_EXISTS;

/**
 * 补货价格表：记录不同用户等级对应的商品补货价格 Service 实现类
 *
 * @author azhanga
 */
@Service
@Validated
public class MaterialRestockPriceServiceImpl implements MaterialRestockPriceService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialRestockPriceServiceImpl.class);

    @Resource
    private MaterialRestockPriceMapper materialRestockPriceMapper;
    
    @Resource
    private MemberUserApi memberUserApi;
    
    @Resource
    private MemberLevelApi memberLevelApi;
    
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public Long createMaterialRestockPrice(MaterialRestockPriceSaveReqVO createReqVO) {
        // 插入
        MaterialRestockPriceDO materialRestockPrice = BeanUtils.toBean(createReqVO, MaterialRestockPriceDO.class);
        materialRestockPriceMapper.insert(materialRestockPrice.setId(IdGeneratorUtil.nextId()));
        // 返回
        return materialRestockPrice.getId();
    }

    @Override
    public void updateMaterialRestockPrice(MaterialRestockPriceSaveReqVO updateReqVO) {
        // 校验存在
        validateMaterialRestockPriceExists(updateReqVO.getId());
        // 更新
        MaterialRestockPriceDO updateObj = BeanUtils.toBean(updateReqVO, MaterialRestockPriceDO.class);
        materialRestockPriceMapper.updateById(updateObj);
    }

    @Override
    public void deleteMaterialRestockPrice(Long id) {
        // 校验存在
        validateMaterialRestockPriceExists(id);
        // 删除
        materialRestockPriceMapper.deleteById(id);
    }

    private void validateMaterialRestockPriceExists(Long id) {
        if (materialRestockPriceMapper.selectById(id) == null) {
            throw exception(MATERIAL_RESTOCK_PRICE_NOT_EXISTS);
        }
    }

    @Override
    public MaterialRestockPriceDO getMaterialRestockPrice(Long id) {
        return materialRestockPriceMapper.selectById(id);
    }

    @Override
    public PageResult<MaterialRestockPriceDO> getMaterialRestockPricePage(MaterialRestockPricePageReqVO pageReqVO) {
        return materialRestockPriceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AppMbPriceRespVO> getMaterialRestockPriceListByUserId(Long userId) {
        return List.of();
    }

    @Override
    public AppMbPriceRespVO getMaterialRestockPriceByUserIdAndProductId(Long userId, Long productId) {
        try {
            // 1. 获取用户信息，只包含用户等级ID
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            if (user == null) {
                logger.error("[getMaterialRestockPriceByUserIdAndProductId] 用户不存在, userId: {}", userId);
                return null;
            }
            
            // 2. 获取用户等级ID
            Long levelId = user.getLevelId();
            
            // 3. 查询用户等级详情（用于获取等级名称等信息）
            MemberLevelRespDTO levelInfo = memberLevelApi.getMemberLevel(levelId);
            if (levelInfo == null) {
                logger.error("[getMaterialRestockPriceByUserIdAndProductId] 用户等级不存在, levelId: {}", levelId);
                return null;
            }
            
            // 4. 根据用户等级ID和产品ID查询补货价格
            MaterialRestockPriceDO priceDO = materialRestockPriceMapper.selectByLevelIdAndProductId(levelId, productId);
            if (priceDO == null) {
                logger.warn("[getMaterialRestockPriceByUserIdAndProductId] 未找到补货价格记录, userId: {}, productId: {}, levelId: {}", 
                        userId, productId, levelId);
                throw exception(MATERIAL_RESTOCK_PRICE_NOT_EXISTS);
            }
            
            // 5. 转换为VO
            AppMbPriceRespVO respVO = BeanUtils.toBean(priceDO, AppMbPriceRespVO.class);
            
            // 6. 填充商品信息（名称和图片）
            ProductSpuRespDTO product = productSpuApi.getSpu(productId);
            if (product != null) {
                respVO.setProductName(product.getName());
                respVO.setProductImage(product.getPicUrl());
            }
            
            // 7. 填充用户等级名称
            respVO.setUserLevelName(levelInfo.getName()); // 直接使用等级信息中的名称
            
            return respVO;
        } catch (Exception e) {
            logger.error("[getMaterialRestockPriceByUserIdAndProductId] 获取补货价格异常", e);
            return null;
        }
    }
}