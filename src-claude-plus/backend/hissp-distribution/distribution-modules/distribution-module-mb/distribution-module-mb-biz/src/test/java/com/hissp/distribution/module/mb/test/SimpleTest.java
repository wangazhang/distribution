package com.hissp.distribution.module.mb.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单测试类，用于验证测试环境是否正常
 * 
 * @author test
 */
@DisplayName("简单测试验证")
public class SimpleTest {

    @Test
    @DisplayName("基础断言测试")
    public void testBasicAssertions() {
        // 基础断言测试
        assertTrue(true, "这个测试应该通过");
        assertEquals(2, 1 + 1, "1 + 1 应该等于 2");
        assertNotNull("Hello World", "字符串不应该为null");
    }

    @Test
    @DisplayName("字符串操作测试")
    public void testStringOperations() {
        String str = "Hello World";
        
        assertNotNull(str);
        assertEquals(11, str.length());
        assertTrue(str.contains("World"));
        assertTrue(str.startsWith("Hello"));
        assertTrue(str.endsWith("World"));
    }

    @Test
    @DisplayName("数学运算测试")
    public void testMathOperations() {
        assertEquals(4, 2 + 2);
        assertEquals(0, 2 - 2);
        assertEquals(4, 2 * 2);
        assertEquals(1, 2 / 2);
    }
}
