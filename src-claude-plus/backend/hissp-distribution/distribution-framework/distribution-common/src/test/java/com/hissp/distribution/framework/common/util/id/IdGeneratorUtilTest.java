package com.hissp.distribution.framework.common.util.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IdGeneratorUtil} 的单元测试
 */
public class IdGeneratorUtilTest {

    @Test
    public void testNextId() {
        long id = IdGeneratorUtil.nextId();
        System.out.println("生成的雪花算法ID: " + id);
        assertTrue(id > 0);
    }
    
    @Test
    public void testNextIdStr() {
        String id = IdGeneratorUtil.nextIdStr("TEST");
        System.out.println("生成的带前缀的雪花算法ID: " + id);
        assertTrue(id.startsWith("TEST"));
    }
    
    @Test
    public void testReplenishmentId() {
        String id = IdGeneratorUtil.nextReplenishmentId();
        System.out.println("生成的补货单ID: " + id);
        assertTrue(id.startsWith(IdGeneratorUtil.PREFIX_REPLENISHMENT));
    }
    
    @Test
    public void testMerchandiseId() {
        String id = IdGeneratorUtil.nextMerchandiseId();
        System.out.println("生成的商品单ID: " + id);
        assertTrue(id.startsWith(IdGeneratorUtil.PREFIX_MERCHANDISE));
    }
    
    @Test
    public void testConversionId() {
        String id = IdGeneratorUtil.nextConversionId();
        System.out.println("生成的转化单ID: " + id);
        assertTrue(id.startsWith(IdGeneratorUtil.PREFIX_CONVERSION));
    }
} 