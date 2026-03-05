# RestockCommissionHandler 集成测试方案

## 🎯 **测试目标**

使用真实数据库和真实业务逻辑对RestockCommissionHandler补货分佣处理器进行完整的集成测试，验证实际的分佣流程。

## 🏗️ **测试架构**

### **测试类型对比**

| 测试类型 | 数据库 | 外部API | 业务逻辑 | 数据持久化 | 适用场景 |
|---------|--------|---------|----------|------------|----------|
| **单元测试** | Mock | Mock | 真实 | 否 | 快速验证逻辑 |
| **集成测试** | 真实 | Mock | 真实 | 是 | 完整流程验证 |

### **集成测试优势**

✅ **真实数据持久化**: 验证数据库操作的正确性  
✅ **完整业务流程**: 测试端到端的分佣处理  
✅ **真实数据验证**: 可以查看数据库中的实际结果  
✅ **边界条件测试**: 验证真实环境下的边界情况  

## 📁 **文件结构**

```
src/test/java/com/hissp/distribution/module/mb/
├── domain/service/commission/handler/
│   ├── RestockCommissionHandlerTest.java                    # 单元测试（Mock）
│   └── RestockCommissionHandlerIntegrationTest.java        # 集成测试（真实DB）
├── test/
│   ├── TestDataCleaner.java                               # 数据清理工具
│   ├── TestDataPreparer.java                              # 数据准备工具
│   └── DataCleanupApplication.java                        # 数据清理应用
└── scripts/
    └── run-integration-tests.sh                           # 集成测试执行脚本
```

## 🚀 **快速开始**

### **1. 环境准备**

确保以下服务正在运行：

```bash
# MySQL数据库
mysql -h localhost -P 3306 -u root -p

# Redis服务
redis-cli ping
```

### **2. 执行集成测试**

#### **方式一：使用脚本（推荐）**

```bash
# 执行完整的集成测试
./src/test/scripts/run-integration-tests.sh

# 仅清理测试数据
./src/test/scripts/run-integration-tests.sh --clean-only

# 跳过测试后数据清理（用于调试）
./src/test/scripts/run-integration-tests.sh --skip-cleanup
```

#### **方式二：使用Maven**

```bash
# 执行集成测试
mvn test -Dtest=RestockCommissionHandlerIntegrationTest -Dspring.profiles.active=integration-test

# 清理测试数据
mvn test-compile exec:java -Dexec.mainClass="com.hissp.distribution.module.mb.test.DataCleanupApplication"
```

#### **方式三：使用IDE**

1. 在IDE中打开 `RestockCommissionHandlerIntegrationTest.java`
2. 设置运行配置：`-Dspring.profiles.active=integration-test`
3. 运行测试类或单个测试方法

## 🧪 **测试场景**

### **1. 商品购买分佣场景**

```java
@Test
@DisplayName("商品购买分佣 - 普通用户购买商品的分佣计算")
public void testHandleRestockCommission_NormalUserPurchase()
```

- **测试数据**: 游客等级用户，100元订单
- **验证点**: 分佣规则查询、用户信息查询、分佣记录创建
- **预期结果**: 按5%比例计算分佣

### **2. VIP用户分佣场景**

```java
@Test
@DisplayName("商品购买分佣 - VIP用户购买商品的分佣差异")
public void testHandleRestockCommission_VIPUserPurchase()
```

- **测试数据**: VIP等级用户，150元订单
- **验证点**: VIP等级的特殊分佣规则
- **预期结果**: 按8%比例计算分佣

### **3. 多级分佣场景**

```java
@Test
@DisplayName("商品购买分佣 - 多级分佣链条的分佣分配")
public void testHandleRestockCommission_MultiLevelCommission()
```

- **测试数据**: SVIP用户，200元订单，完整分佣链条
- **验证点**: 一级、二级、分公司多级分佣
- **预期结果**: 多级分佣记录创建

### **4. 边界和异常场景**

```java
@Test
@DisplayName("边界和异常场景 - 分佣规则不存在的处理")
public void testHandleRestockCommission_NoCommissionRule()
```

- **测试数据**: 删除分佣规则后的订单
- **验证点**: 异常情况的优雅处理
- **预期结果**: 不抛异常，不创建分佣记录

## 📊 **数据管理**

### **测试数据ID范围**

| 数据类型 | ID范围 | 说明 |
|---------|--------|------|
| 测试订单 | 100000-199999 | UserMaterialOrderDO |
| 分佣规则 | 200000-299999 | RestockCommissionRuleDO |
| 测试用户 | 10001-10999 | Mock用户ID |

### **数据清理策略**

1. **自动清理**: 每个测试方法前后自动清理
2. **手动清理**: 使用DataCleanupApplication
3. **脚本清理**: 使用run-integration-tests.sh --clean-only

### **数据验证**

```java
// 验证订单数据
UserMaterialOrderDO updatedOrder = userMaterialOrderMapper.selectById(TEST_ORDER_ID_1);
assertNotNull(updatedOrder);

// 验证分佣规则
RestockCommissionRuleDO rule = restockCommissionRuleMapper.selectById(TEST_RULE_ID_1);
assertEquals(MbConstants.MbTouristLevelInfo.MB_TOURIST_LEVEL, rule.getLevel());
```

## 🔧 **配置说明**

### **数据库配置**

```yaml
# application-integration-test.yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/distribution_test
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
```

### **Mock配置**

```java
// Mock外部API，保持内部逻辑真实
@MockBean
private MemberUserApi memberUserApi;

@MockBean
private BrokerageUserApi brokerageUserApi;
```

## 📈 **测试报告**

### **执行结果示例**

```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

### **数据验证结果**

```
2025-07-27 11:00:00 INFO  - 清理测试订单数据: 3 条
2025-07-27 11:00:00 INFO  - 清理测试分佣规则数据: 3 条
2025-07-27 11:00:00 INFO  - ✓ 测试订单数据清理完成
2025-07-27 11:00:00 INFO  - ✓ 测试分佣规则数据清理完成
```

## 🚨 **注意事项**

### **环境要求**

1. **数据库**: 必须使用独立的测试数据库（distribution_test）
2. **Redis**: 使用独立的Redis数据库（database: 1）
3. **权限**: 确保有数据库读写权限

### **安全提醒**

⚠️ **重要**: 集成测试会在数据库中创建和删除真实数据  
⚠️ **警告**: 请确保使用测试数据库，不要在生产环境运行  
⚠️ **建议**: 运行前备份重要数据  

### **故障排除**

1. **连接失败**: 检查数据库和Redis服务状态
2. **权限错误**: 确认数据库用户权限
3. **数据冲突**: 运行数据清理工具
4. **端口占用**: 检查服务端口配置

## 🎉 **总结**

这个集成测试方案提供了：

✅ **真实环境测试**: 使用真实数据库验证完整流程  
✅ **自动化数据管理**: 自动准备和清理测试数据  
✅ **多场景覆盖**: 涵盖正常、异常、边界情况  
✅ **易于使用**: 提供脚本和文档支持  
✅ **安全可靠**: 独立测试环境，自动数据清理  

通过这个方案，您可以验证RestockCommissionHandler在真实环境下的完整分佣逻辑，确保业务功能的正确性和稳定性。
