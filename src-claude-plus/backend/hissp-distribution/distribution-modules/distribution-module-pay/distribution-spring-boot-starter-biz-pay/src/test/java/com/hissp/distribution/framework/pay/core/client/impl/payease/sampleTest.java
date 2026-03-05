package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import lombok.Data;

public class sampleTest {
    public static void main(String[] args) {
        A a = new A();
        a.setAa("aaaa");
        a.setBb("BBBB");

        B b = new B();
        b.setCc("C");
        b.setDd("D");

        JSONObject entries = JSONUtil.createObj();
        entries.putAll(JSONUtil.parseObj(a));
        entries.putAll(JSONUtil.parseObj(b));
        System.out.println(entries.get("aa"));

    }

    @Data
    static class A {
        public String aa;
        public String bb;
    }

    @Data
    static class B {
        public String cc;
        public String dd;
    }

}
