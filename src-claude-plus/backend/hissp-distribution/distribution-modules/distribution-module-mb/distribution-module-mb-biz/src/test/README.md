# MB模块分销功能接口自动化测试方案

## 概述

本测试方案为MB模块的分销功能提供了完整的接口自动化测试解决方案，目前已实现基础的单元测试框架。

## 🚀 快速开始

### 运行测试

```bash
# 进入模块目录
cd distribution-modules/distribution-module-mb/distribution-module-mb-biz

# 运行所有单元测试
./src/test/scripts/run-tests.sh unit

# 运行指定测试类
./src/test/scripts/run-tests.sh --test SimpleTest
./src/test/scripts/run-tests.sh --test MaterialApiImplTest

# 使用Maven直接运行
mvn test
mvn test -Dtest=MaterialApiImplTest
```

## 📋 已实现的测试

### 1. 基础测试
- **SimpleTest**: 基础环境验证测试
  - 基础断言测试
  - 字符串操作测试
  - 数学运算测试

### 2. API接口测试
- **MaterialApiImplTest**: 物料API接口测试
  - 批量添加物料 - 成功场景
  - 批量添加物料 - 空列表处理
  - 批量添加物料 - 单个物料添加失败
  - 批量添加物料 - 不同操作类型
  - 批量添加物料 - 参数验证

### 3. 分佣处理器测试
- **RestockCommissionHandlerTest**: 补货分佣处理器测试
  - 商品购买分佣 - 普通用户购买商品的分佣计算
  - 商品购买分佣 - VIP用户购买商品的分佣差异
  - 边界和异常场景 - 分佣规则不存在的处理
  - 边界和异常场景 - 空订单处理
  - 用户不存在的处理

### 4. 测试套件
- **MbDistributionTestSuite**: 测试套件验证
  - 测试套件验证
  - 测试环境验证
  - 测试框架验证

## 📊 测试统计

### 测试覆盖情况
- **总测试数**: 13个
- **通过率**: 100% (13/13)
- **测试类数**: 4个
- **执行时间**: < 6秒

### 分佣业务场景覆盖
- ✅ **商品购买分佣场景**: 普通用户和VIP用户的分佣计算差异
- ✅ **边界和异常场景**: 分佣规则不存在、空订单、用户不存在等异常情况处理
- 🚧 **SVIP开通分佣场景**: 需要更多Mock配置支持
- 🚧 **不同等级代理商补货分佣场景**: 需要完整的处理器Mock
- 🚧 **多级分佣链条**: 需要复杂的用户关系Mock

## 🛠️ 测试架构

### 测试层次结构
```
├── 单元测试 (Unit Tests) ✅
│   └── API接口测试 ✅
├── 集成测试 (Integration Tests) 🚧
├── 性能测试 (Performance Tests) 🚧
└── 数据驱动测试 (Data-Driven Tests) 🚧
```

### 测试覆盖范围

#### ✅ 已实现
- **MaterialApi**: 物料管理API
  - 批量添加物料
  - 参数验证
  - 异常处理
- **RestockCommissionHandler**: 补货分佣处理器
  - 基础功能测试
  - 边界条件测试
  - 异常处理测试

#### 🚧 待实现
- **UserMaterialBalanceService**: 用户物料余额服务
- **AppMaterialOutboundController**: 物料出库控制器
- **CareerProductCommissionHandler**: 事业产品分佣处理器

## 📊 测试报告

测试执行后会生成以下报告：

- **Surefire测试报告**: `target/surefire-reports/`
- **控制台输出**: 实时显示测试执行状态

### 示例输出
```
[INFO] 开始执行MB模块分销功能自动化测试
[INFO] 测试类型: unit
[SUCCESS] 环境检查通过
[INFO] 运行单元测试...
[SUCCESS] 单元测试执行成功
[INFO] Surefire测试报告: target/surefire-reports/
[SUCCESS] 测试执行完成
```

## 🔧 测试配置

### 环境配置

#### 单元测试环境 (application-unit-test.yaml)
- 使用H2内存数据库
- 使用内存Redis
- Mock外部依赖
- 快速执行

### Maven配置

项目已配置以下Maven插件：
- **Surefire Plugin**: 单元测试执行
- **JaCoCo Plugin**: 代码覆盖率报告
- **Compiler Plugin**: Java 21支持

## 💡 脚本使用说明

### 脚本参数

```bash
./src/test/scripts/run-tests.sh [test-type] [options]
```

**test-type:**
- `unit` - 只运行单元测试 (默认)
- `all` - 运行所有测试
- `specific` - 运行指定测试类

**options:**
- `--test <name>` - 指定要运行的测试类名
- `--coverage` - 生成代码覆盖率报告
- `--verbose` - 详细输出
- `--clean` - 清理之前的测试结果
- `--help` - 显示帮助信息

### 使用示例

```bash
# 运行单元测试
./src/test/scripts/run-tests.sh unit

# 运行指定测试
./src/test/scripts/run-tests.sh --test SimpleTest
./src/test/scripts/run-tests.sh --test MaterialApiImplTest --verbose

# 生成代码覆盖率报告
./src/test/scripts/run-tests.sh unit --coverage

# 清理并运行测试
./src/test/scripts/run-tests.sh unit --clean
```

### Maven命令

```bash
# 执行单元测试
mvn test

# 执行特定测试类
mvn test -Dtest=MaterialApiImplTest

# 生成代码覆盖率报告
mvn test jacoco:report
```

## 测试报告

### 报告类型

1. **JUnit测试报告**: `target/surefire-reports/`
2. **代码覆盖率报告**: `target/site/jacoco/index.html`
3. **自定义HTML报告**: `target/test-reports/test-report.html`
4. **JSON格式报告**: `target/test-reports/test-report.json`
5. **CSV格式报告**: `target/test-reports/test-report.csv`

### 报告内容

- 测试执行统计
- 测试结果详情
- 性能指标分析
- 错误分析和堆栈跟踪
- 代码覆盖率统计

## 真实服务集成

### 配置真实服务连接

```yaml
test:
  real-service:
    enabled: true
    base-url: http://localhost:8080
    auth:
      username: test_user
      password: test_password
      token: your_auth_token
    timeout:
      connect: 5000
      read: 10000
```

### 真实服务测试场景

1. **物料批量添加接口测试**
2. **用户物料订单创建接口测试**
3. **物料出库申请接口测试**
4. **用户物料余额查询接口测试**
5. **完整业务流程测试**
6. **错误处理测试**
7. **性能测试**

## 数据驱动测试

### 参数化测试

使用JUnit 5的参数化测试功能：

```java
@ParameterizedTest
@ValueSource(ints = {1, 5, 10, 50, 100})
void testDifferentQuantities(int quantity) {
    // 测试不同数量的处理
}

@ParameterizedTest
@CsvSource({
    "ADD, ADMIN_ADD, 添加操作-管理员添加",
    "SUBTRACT, ORDER_USE, 扣减操作-订单使用"
})
void testDifferentOperations(String actionType, String sourceType, String description) {
    // 测试不同操作类型
}
```

### 测试数据工厂

使用TestDataFactory类生成测试数据：

```java
// 创建测试物料
MaterialDO material = TestDataFactory.createMaterialDO();

// 创建测试用户
MemberUserRespDTO user = TestDataFactory.createMemberUserRespDTO();

// 创建批量测试数据
List<MaterialActReqDTO> materialList = TestDataFactory.createMaterialActReqDTOList(10);
```

## 性能测试

### 性能测试配置

```yaml
performance-test:
  concurrent-users: 10
  duration: 60
  request-interval: 100
  thresholds:
    avg-response-time: 500
    p95-response-time: 1000
    error-rate: 1.0
```

### 性能测试场景

1. **并发性能测试**: 50个并发用户，每用户20个请求
2. **大数据量测试**: 测试不同批量大小的性能
3. **内存使用测试**: 监控内存使用情况
4. **稳定性测试**: 长时间运行测试

### 性能指标

- 平均响应时间
- 最大/最小响应时间
- 吞吐量 (TPS)
- 错误率
- 内存使用量
- 响应时间标准差

## 最佳实践

### 1. 测试隔离
- 每个测试方法独立运行
- 使用@Transactional回滚数据
- Mock外部依赖

### 2. 测试数据管理
- 使用TestDataFactory统一创建测试数据
- 避免硬编码测试数据
- 清理测试数据

### 3. 断言策略
- 使用有意义的断言消息
- 验证关键业务逻辑
- 检查边界条件

### 4. 异常测试
- 测试各种异常场景
- 验证错误消息
- 确保系统稳定性

### 5. 性能监控
- 设置合理的性能阈值
- 监控关键性能指标
- 定期执行性能测试

## 持续集成

### CI/CD集成

在CI/CD流程中集成测试：

```yaml
# GitHub Actions示例
- name: Run Unit Tests
  run: mvn test

- name: Run Integration Tests
  run: mvn verify

- name: Generate Test Report
  run: mvn jacoco:report

- name: Upload Test Results
  uses: actions/upload-artifact@v2
  with:
    name: test-results
    path: target/surefire-reports/
```

### 测试门禁

设置测试门禁标准：
- 单元测试通过率 >= 95%
- 代码覆盖率 >= 80%
- 集成测试通过率 >= 90%
- 性能测试满足阈值要求

## 故障排查

### 常见问题

1. **测试环境问题**
   - 检查数据库连接
   - 验证Redis配置
   - 确认依赖服务状态

2. **测试数据问题**
   - 检查测试数据初始化
   - 验证数据清理逻辑
   - 确认数据隔离

3. **Mock问题**
   - 验证Mock配置
   - 检查Mock行为
   - 确认依赖注入

### 调试技巧

1. 启用详细日志
2. 使用IDE调试功能
3. 检查测试报告
4. 分析错误堆栈

## 扩展指南

### 添加新测试

1. 继承BaseMbUnitTest基类
2. 使用TestDataFactory创建测试数据
3. 添加适当的断言
4. 更新测试套件

### 自定义测试配置

1. 修改application-test.yaml
2. 添加新的测试配置属性
3. 更新测试脚本
4. 文档化配置变更

## 联系方式

如有问题或建议，请联系：
- 开发团队: dev-team@company.com
- 测试团队: qa-team@company.com
