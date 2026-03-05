package com.hissp.distribution.module.mb.test;

import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.constants.MbConstants;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;

/**
 * MB模块测试基础类
 * 
 * @author test
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("unit-test")
public abstract class BaseMbUnitTest {

    protected static final Random RANDOM = new Random();
    
    // Mock外部依赖的API
    @Mock
    protected MemberUserApi memberUserApi;
    
    @Mock
    protected MemberLevelApi memberLevelApi;
    
    @Mock
    protected ProductSpuApi productSpuApi;
    
    @Mock
    protected BrokerageUserApi brokerageUserApi;
    
    @Mock
    protected BrokerageRecordApi brokerageRecordApi;

    /**
     * 创建测试用的物料操作请求DTO
     */
    protected MaterialActDTO createTestMaterialActDTO() {
        return MaterialActDTO.builder()
                .userId(generateRandomUserId())
                .materialId(generateRandomMaterialId())
                .quantity(RANDOM.nextInt(100) + 1)
                .direction(MaterialActDirectionEnum.IN)
                .bizKey("test_source_" + System.currentTimeMillis())
                .bizType("ADD_RESTOCK")
                .reason("测试添加物料")
                .build();
    }

    /**
     * 生成随机用户ID
     */
    protected Long generateRandomUserId() {
        return 10000L + RANDOM.nextInt(90000);
    }

    /**
     * 生成随机物料ID
     */
    protected Long generateRandomMaterialId() {
        return 1000L + RANDOM.nextInt(9000);
    }

    /**
     * 生成随机等级ID
     */
    protected Long generateRandomLevelId() {
        return 1L + RANDOM.nextInt(10);
    }

    /**
     * 生成随机订单ID
     */
    protected String generateRandomOrderId() {
        return "ORDER_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(1000);
    }

    /**
     * 生成随机金额（分）
     */
    protected Integer generateRandomAmount() {
        return RANDOM.nextInt(100000) + 1000; // 1000分到100000分
    }

    /**
     * 生成随机数量
     */
    protected Integer generateRandomQuantity() {
        return RANDOM.nextInt(100) + 1; // 1到100
    }

    /**
     * 生成随机等级值
     */
    protected Integer generateRandomLevel() {
        return RANDOM.nextInt(10) + 1; // 1到10
    }

    /**
     * 生成随机价格
     */
    protected Integer generateRandomPrice() {
        return RANDOM.nextInt(10000) + 100; // 100到10100
    }

    /**
     * 生成随机业务ID
     */
    protected String generateRandomBizId() {
        return "BIZ_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(1000);
    }
}
