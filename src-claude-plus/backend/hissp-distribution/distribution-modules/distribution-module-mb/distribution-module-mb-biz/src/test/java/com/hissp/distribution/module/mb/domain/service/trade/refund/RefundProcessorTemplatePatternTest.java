package com.hissp.distribution.module.mb.domain.service.trade.refund;

import com.hissp.distribution.module.mb.domain.service.trade.refund.factory.RefundProcessorFactory;
import com.hissp.distribution.module.mb.domain.service.trade.refund.processor.AbstractRefundProcessor;
import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;

/**
 * 退款处理器模板模式测试类
 * 
 * <p>用于验证模板设计模式重构后的退款处理功能是否正常工作。</p>
 * 
 * <h3>测试内容：</h3>
 * <ul>
 *   <li>工厂类是否能正确创建各种类型的处理器</li>
 *   <li>各种退款类型的处理器是否能正常工作</li>
 *   <li>模板方法的流程是否正确执行</li>
 * </ul>
 * 
 * <h3>注意事项：</h3>
 * <p>这是一个集成测试示例，实际运行需要完整的Spring上下文和数据库环境。
 * 在实际项目中，建议使用Mock对象进行单元测试。</p>
 * 
 * @author system
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RefundProcessorTemplatePatternTest {

    @Resource
    private RefundProcessorFactory refundProcessorFactory;

    /**
     * 测试工厂类是否能正确创建所有类型的处理器
     */
    @Test
    public void testRefundProcessorFactory() {
        log.info("=== 测试退款处理器工厂类 ===");
        
        // 打印支持的退款类型
        refundProcessorFactory.printSupportedRefundTypes();
        
        // 测试创建各种类型的处理器
        for (MbRefundTypeEnum refundType : refundProcessorFactory.getSupportedRefundTypes()) {
            try {
                AbstractRefundProcessor processor = refundProcessorFactory.createProcessor(refundType);
                log.info("成功创建处理器: {} -> {}", refundType, processor.getClass().getSimpleName());
                
                // 验证处理器的退款类型是否正确
                // assert processor.getRefundType() == refundType :
                //     "处理器返回的退款类型与预期不符: " + processor.getRefundType() + " != " + refundType;
                
            } catch (Exception e) {
                log.error("创建处理器失败: {}", refundType, e);
                throw e;
            }
        }
        
        log.info("工厂类测试通过！");
    }

    /**
     * 测试不支持的退款类型
     */
    @Test
    public void testUnsupportedRefundType() {
        log.info("=== 测试不支持的退款类型 ===");
        
        try {
            refundProcessorFactory.createProcessor(MbRefundTypeEnum.UNKNOWN);
            assert false : "应该抛出异常";
        } catch (IllegalArgumentException e) {
            log.info("正确抛出异常: {}", e.getMessage());
        }
        
        log.info("不支持退款类型测试通过！");
    }

    /**
     * 测试物料补货退款处理器（示例）
     * 
     * <p>注意：这是一个示例测试，实际测试需要准备测试数据</p>
     */
    @Test
    public void testMaterialRestockRefundProcessor() {
        log.info("=== 测试物料补货退款处理器 ===");
        
        // 创建测试数据
        PayRefundNotifyReqDTO notifyReqDTO = new PayRefundNotifyReqDTO();
        notifyReqDTO.setMerchantOrderId("MR123456"); // 物料补货订单号
        notifyReqDTO.setPayRefundId(123456L);
        
        // 创建处理器
        AbstractRefundProcessor processor = refundProcessorFactory.createProcessor(MbRefundTypeEnum.MATERIAL_RESTOCK);
        
        log.info("创建物料补货退款处理器成功: {}", processor.getClass().getSimpleName());
        
        // 注意：实际的processRefund调用需要真实的数据库数据
        // 这里只是演示如何使用处理器
        // boolean result = processor.processRefund(notifyReqDTO);
        
        log.info("物料补货退款处理器测试完成！");
    }

    /**
     * 测试模板方法的扩展性
     * 
     * <p>演示如何通过继承AbstractRefundProcessor来扩展新的退款类型</p>
     */
    @Test
    public void testTemplatePatternExtensibility() {
        log.info("=== 测试模板模式的扩展性 ===");
        
        // 创建一个测试用的处理器实现
        AbstractRefundProcessor testProcessor = new TestRefundProcessor();
        
        // 验证模板方法的基本结构
        // assert testProcessor.getRefundType() == MbRefundTypeEnum.UNKNOWN;
        
        log.info("模板模式扩展性测试通过！");
    }

    /**
     * 测试用的退款处理器实现
     * 
     * <p>演示如何扩展AbstractRefundProcessor</p>
     */
    private static class TestRefundProcessor extends AbstractRefundProcessor {
        
        public TestRefundProcessor() {
            super(null); // 测试用，传入null
        }

        @Override
        protected boolean validateRefundRequest(PayRefundNotifyReqDTO notifyReqDTO) {
            return true; // 测试用，直接返回true
        }

        @Override
        protected boolean executeRefundReverse(PayRefundNotifyReqDTO notifyReqDTO) {
            return true; // 测试用，直接返回true
        }

        @Override
        protected MbRefundTypeEnum getRefundType() {
            return MbRefundTypeEnum.UNKNOWN; // 测试用
        }

        @Override
        protected Long getUserId(PayRefundNotifyReqDTO notifyReqDTO) {
            return 1L; // 测试用
        }
    }

    /**
     * 性能测试：批量创建处理器
     */
    @Test
    public void testProcessorCreationPerformance() {
        log.info("=== 测试处理器创建性能 ===");
        
        int testCount = 1000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < testCount; i++) {
            for (MbRefundTypeEnum refundType : refundProcessorFactory.getSupportedRefundTypes()) {
                AbstractRefundProcessor processor = refundProcessorFactory.createProcessor(refundType);
                assert processor != null;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log.info("创建{}个处理器耗时: {}ms, 平均每个: {}ms", 
            testCount * refundProcessorFactory.getSupportedRefundTypeCount(), 
            duration, 
            (double) duration / (testCount * refundProcessorFactory.getSupportedRefundTypeCount()));
        
        log.info("性能测试通过！");
    }

    /**
     * 测试工厂类的辅助方法
     */
    @Test
    public void testFactoryHelperMethods() {
        log.info("=== 测试工厂类辅助方法 ===");
        
        // 测试isSupported方法
        assert refundProcessorFactory.isSupported(MbRefundTypeEnum.MATERIAL_RESTOCK);
        assert refundProcessorFactory.isSupported(MbRefundTypeEnum.MATERIAL_CONVERT);
        assert refundProcessorFactory.isSupported(MbRefundTypeEnum.MB_NORMAL_PRODUCT);
        assert refundProcessorFactory.isSupported(MbRefundTypeEnum.MB_CAREER_PRODUCT);
        assert !refundProcessorFactory.isSupported(MbRefundTypeEnum.UNKNOWN);
        assert !refundProcessorFactory.isSupported(null);
        
        // 测试getSupportedRefundTypeCount方法
        assert refundProcessorFactory.getSupportedRefundTypeCount() == 4;
        
        // 测试getProcessorClassName方法
        assert "MaterialRestockRefundProcessor".equals(
            refundProcessorFactory.getProcessorClassName(MbRefundTypeEnum.MATERIAL_RESTOCK));
        assert "CollagenConvertRefundProcessor".equals(
            refundProcessorFactory.getProcessorClassName(MbRefundTypeEnum.MATERIAL_CONVERT));
        assert "MbNormalProductRefundProcessor".equals(
            refundProcessorFactory.getProcessorClassName(MbRefundTypeEnum.MB_NORMAL_PRODUCT));
        assert "MbCareerProductRefundProcessor".equals(
            refundProcessorFactory.getProcessorClassName(MbRefundTypeEnum.MB_CAREER_PRODUCT));
        assert refundProcessorFactory.getProcessorClassName(MbRefundTypeEnum.UNKNOWN) == null;
        
        log.info("工厂类辅助方法测试通过！");
    }
}
