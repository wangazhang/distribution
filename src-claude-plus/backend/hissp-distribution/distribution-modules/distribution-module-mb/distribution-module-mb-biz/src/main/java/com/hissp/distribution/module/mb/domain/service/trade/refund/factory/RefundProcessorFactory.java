package com.hissp.distribution.module.mb.domain.service.trade.refund.factory;

import com.hissp.distribution.module.mb.domain.service.trade.refund.processor.AbstractRefundProcessor;
import com.hissp.distribution.module.mb.domain.service.trade.refund.processor.MaterialConvertRefundProcessor;
import com.hissp.distribution.module.mb.domain.service.trade.refund.processor.MaterialRestockRefundProcessor;
import com.hissp.distribution.module.mb.enums.MbRefundTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 退款处理器工厂类
 * 
 * <p>使用工厂模式根据退款类型创建对应的退款处理器实例。</p>
 * 
 * <h3>工厂模式说明：</h3>
 * <ul>
 *   <li><strong>产品接口</strong>：{@link AbstractRefundProcessor} - 定义退款处理器的通用接口</li>
 *   <li><strong>具体产品</strong>：各种具体的退款处理器实现类</li>
 *   <li><strong>工厂类</strong>：本类 - 负责根据退款类型创建对应的处理器</li>
 * </ul>
 * 
 * <h3>支持的退款类型：</h3>
 * <ul>
 *   <li><strong>MATERIAL_RESTOCK</strong>：物料补货退款 - {@link MaterialRestockRefundProcessor}</li>
 *   <li><strong>MATERIAL_CONVERT</strong>：物料转化退款 - {@link MaterialConvertRefundProcessor}</li>
 * </ul>
 * 
 * <h3>扩展说明：</h3>
 * <p>新增退款类型时，只需要：</p>
 * <ol>
 *   <li>在 {@link MbRefundTypeEnum} 中添加新的退款类型枚举</li>
 *   <li>创建对应的处理器实现类，继承 {@link AbstractRefundProcessor}</li>
 *   <li>在本工厂类的 {@link #createProcessor(MbRefundTypeEnum)} 方法中添加对应的创建逻辑</li>
 * </ol>
 * 
 * <h3>Spring Bean 管理：</h3>
 * <p>工厂类通过 Spring 的 {@link ApplicationContext} 获取处理器实例，
 * 确保处理器的依赖注入正常工作。所有处理器都需要添加 {@code @Component} 注解。</p>
 * 
 * @author system
 */
@Slf4j
@Component
public class RefundProcessorFactory {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 根据退款类型创建对应的退款处理器
     * 
     * <p>使用 Spring 的 ApplicationContext 获取处理器实例，确保依赖注入正常工作。</p>
     * 
     * @param refundType 退款类型，不能为null
     * @return 对应的退款处理器实例
     * @throws IllegalArgumentException 当退款类型不支持时抛出
     */
    public AbstractRefundProcessor createProcessor(MbRefundTypeEnum refundType) {
        if (refundType == null) {
            throw new IllegalArgumentException("退款类型不能为null");
        }

        log.debug("[RefundProcessorFactory][创建退款处理器: refundType={}]", refundType);

        AbstractRefundProcessor processor = switch (refundType) {
            case MATERIAL_RESTOCK -> {
                log.debug("[RefundProcessorFactory][创建物料补货退款处理器]");
                yield applicationContext.getBean(MaterialRestockRefundProcessor.class);
            }
            case MATERIAL_CONVERT -> {
                log.debug("[RefundProcessorFactory][创建物料转化退款处理器]");
                yield applicationContext.getBean(MaterialConvertRefundProcessor.class);
            }
            default -> {
                log.error("[RefundProcessorFactory][不支持的退款类型: {}]", refundType);
                throw new IllegalArgumentException("不支持的退款类型: " + refundType);
            }
        };

        log.info("[RefundProcessorFactory][成功创建退款处理器: refundType={}, processorClass={}]",
            refundType, processor.getClass().getSimpleName());

        return processor;
    }

    /**
     * 检查是否支持指定的退款类型
     * 
     * @param refundType 退款类型
     * @return true-支持，false-不支持
     */
    public boolean isSupported(MbRefundTypeEnum refundType) {
        if (refundType == null) {
            return false;
        }

        return switch (refundType) {
            case MATERIAL_RESTOCK, MATERIAL_CONVERT, MB_NORMAL_PRODUCT, MB_CAREER_PRODUCT -> true;
            default -> false;
        };
    }

    /**
     * 获取所有支持的退款类型
     * 
     * @return 支持的退款类型数组
     */
    public MbRefundTypeEnum[] getSupportedRefundTypes() {
        return new MbRefundTypeEnum[]{
            MbRefundTypeEnum.MATERIAL_RESTOCK,
            MbRefundTypeEnum.MATERIAL_CONVERT,
            MbRefundTypeEnum.MB_NORMAL_PRODUCT,
            MbRefundTypeEnum.MB_CAREER_PRODUCT
        };
    }

    /**
     * 获取支持的退款类型数量
     * 
     * @return 支持的退款类型数量
     */
    public int getSupportedRefundTypeCount() {
        return getSupportedRefundTypes().length;
    }

    /**
     * 获取退款类型对应的处理器类名
     * 
     * @param refundType 退款类型
     * @return 处理器类名，如果不支持则返回null
     */
    public String getProcessorClassName(MbRefundTypeEnum refundType) {
        if (!isSupported(refundType)) {
            return null;
        }

        return switch (refundType) {
            case MATERIAL_RESTOCK -> MaterialRestockRefundProcessor.class.getSimpleName();
            case MATERIAL_CONVERT -> MaterialConvertRefundProcessor.class.getSimpleName();
            default -> null;
        };
    }

    /**
     * 打印工厂支持的所有退款类型信息
     * 
     * <p>用于调试和监控，输出工厂当前支持的所有退款类型及其对应的处理器。</p>
     */
    public void printSupportedRefundTypes() {
        log.info("[RefundProcessorFactory][支持的退款类型总数: {}]", getSupportedRefundTypeCount());
        
        for (MbRefundTypeEnum refundType : getSupportedRefundTypes()) {
            String processorClassName = getProcessorClassName(refundType);
            log.info("[RefundProcessorFactory][退款类型: {} -> 处理器: {}]", refundType, processorClassName);
        }
    }
}
