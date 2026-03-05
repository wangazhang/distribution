# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个前后端分离的分销系统,由 Vue 3 前端和 Java Spring Boot 后端组成,基于 ruoyi-vue-pro 框架扩展了电商功能。

**技术栈:**
- **后端**: Java 17 + Spring Boot 3.4.1 + Maven 多模块架构
- **前端**: Vue 3 + TypeScript + Element Plus (管理端) / Uni-app (商城端)
- **架构**: 三层架构 (接入层、逻辑层、存储层)

## 项目结构

```
src-claude-plus/
├── backend/hissp-distribution/     # 后端项目
│   ├── distribution-framework/     # 核心框架模块
│   ├── distribution-dependencies/  # 依赖管理
│   ├── distribution-server/        # 应用服务主模块
│   └── distribution-modules/       # 业务模块
│       ├── distribution-module-mall/      # 商城模块
│       ├── distribution-module-member/    # 会员模块
│       ├── distribution-module-material/  # 物料模块
│       ├── distribution-module-system/    # 系统模块
│       └── ...                            # 其他业务模块
├── frontend/
│   ├── yudao-ui-admin-vue3/       # 管理后台 (Vue 3 + Element Plus)
│   └── distribution-mall-mini-vue3/  # 商城小程序 (Uni-app + Vue 3)
└── docs/                           # 项目文档
```

### 后端模块结构

**核心模块:**
- `distribution-server` - 主应用服务器入口
- `distribution-framework` - 框架层 (公共组件和工具类)
- `distribution-dependencies` - 统一依赖管理

**业务模块 (distribution-modules):**
- `distribution-module-mall` - 商城业务
- `distribution-module-member` - 会员管理
- `distribution-module-material` - 物料管理
- `distribution-module-crm` - 客户关系管理
- `distribution-module-erp` - 企业资源计划
- `distribution-module-bpm` - 业务流程管理
- `distribution-module-system` - 系统管理
- `distribution-module-infra` - 基础设施

### 前端模块结构

**管理后台 (yudao-ui-admin-vue3):**
- `/src/api` - API 接口封装
- `/src/views` - 页面组件
- `/src/components` - 公共组件
- `/src/store` - Pinia 状态管理
- `/src/router` - 路由配置
- `/src/utils` - 工具函数

**商城小程序 (distribution-mall-mini-vue3):**
- `/pages` - 页面组件
- `/sheep` - 核心业务逻辑和工具函数
  - `/sheep/api` - API 请求
  - `/sheep/components` - 公共组件
  - `/sheep/store` - Pinia 状态管理
  - `/sheep/util` - 工具函数
- `/yehu` - 定制化组件和功能
- `/static` - 静态资源
- `/uni_modules` - uni-app 模块

## 开发命令

### 后端 (Java/Maven)

```bash
# 进入后端目录
cd src-claude-plus/backend/hissp-distribution

# 构建所有模块
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests

# 运行测试
mvn test

# 运行主应用
cd distribution-server
mvn spring-boot:run
```

### 前端 - 管理后台

```bash
cd src-claude-plus/frontend/yudao-ui-admin-vue3

# 安装依赖 (使用 pnpm)
pnpm install

# 开发环境
pnpm dev              # 本地开发
pnpm dev-server       # 开发服务器模式

# 类型检查
pnpm ts:check

# 构建不同环境
pnpm build:local      # 本地
pnpm build:dev        # 开发
pnpm build:test       # 测试
pnpm build:stage      # 预发布
pnpm build:prod       # 生产

# 代码检查和格式化
pnpm lint:eslint      # ESLint
pnpm lint:format      # Prettier
pnpm lint:style       # StyleLint
```

### 前端 - 商城小程序

```bash
cd src-claude-plus/frontend/distribution-mall-mini-vue3

# 安装依赖 (使用 npm)
npm install

# 开发 - 不同平台
npm run dev:h5              # H5
npm run dev:mp-weixin       # 微信小程序
npm run dev:mp-alipay       # 支付宝小程序

# 构建 - 不同平台
npm run build:h5            # H5
npm run build:mp-weixin     # 微信小程序
npm run build:mp-alipay     # 支付宝小程序

# 代码格式化
npm run prettier
```

## 架构设计规范

### 后端三层架构

**1. 接入层 (Controller/Job/MQ Consumer/API)**
- 负责接收和处理外部请求
- Controller 只接收 VO 对象
- 负责参数校验和结果返回

**2. 逻辑层 (Service)**
- 负责业务逻辑处理和事务管理
- Service 可以调用其他 Service
- Service 禁止直接调用其他模块的 Mapper (必须通过该模块的 Service)

**3. 存储层 (Mapper/DAO/Redis/MQ Producer)**
- 负责数据存取
- Mapper 只处理 DO 对象,不包含业务逻辑

### 数据对象规范

- **VO (View Object)**: Controller 与前端交互,包括 ReqVO 和 RespVO
- **DTO (Data Transfer Object)**: 与外部系统交互,包括 ReqDTO 和 RespDTO
- **DO (Domain Object)**: 数据库实体对象,与数据库表结构对应
- **Message**: 消息队列传输对象

**转换规则:**
- 使用 MapStruct 进行对象转换,对象内字段不一致时，需要编写转化逻辑，避免数据转换错误
- Controller 接收 VO → 转换为 DO → Service 处理 → Mapper 操作 DO → 转换为 VO 返回

## API 接口规范

### URL 设计

**基础路径:**
- App 端: `/app-api/{模块名}`
- 管理后台: `/admin-api/{模块名}`

**资源路径:**
- 使用小写连字符分隔: `/app-api/trade/after-sale`

**操作示例:**
- 获取列表: `GET /app-api/trade/after-sale/page`
- 获取详情: `GET /app-api/trade/after-sale/get?id={id}`
- 创建资源: `POST /app-api/trade/after-sale/create`
- 更新资源: `PUT /app-api/trade/after-sale/update`
- 删除资源: `DELETE /app-api/trade/after-sale/cancel?id={id}`

### 请求参数

- **GET 请求**: 使用 URL 参数
- **POST/PUT 请求**: 使用 JSON 请求体
- **分页参数**: `pageNo` (页码,从1开始), `pageSize` (每页大小)

### 响应格式

**成功响应:**
```json
{
  "code": 200,
  "data": {},
  "msg": "操作成功"
}
```

**分页响应:**
```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "pageNo": 1,
    "pageSize": 10,
    "totalPages": 10
  },
  "msg": "操作成功"
}
```

**错误码:**
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

## 命名规范

### 后端命名

**类命名 (PascalCase):**
- Controller: `*Controller`
- Service: `*Service` / `*ServiceImpl`
- Mapper/Repository: `*Mapper` / `*Repository`

**方法命名 (camelCase):**
- 查询: `getXxx`, `findXxx`, `queryXxx`
- 插入: `createXxx`, `saveXxx`, `insertXxx`
- 更新: `updateXxx`
- 删除: `deleteXxx`, `removeXxx`

**变量命名:**
- 普通变量: camelCase
- 布尔变量: `is*`, `has*` 前缀
- 常量: 全大写下划线分隔

### 前端命名

**文件命名:**
- 组件文件: PascalCase (`UserProfile.vue`)
- 工具函数: camelCase (`formatDate.js`)
- 页面文件: kebab-case (`user-profile.vue`)

**组件命名:**
- 基础组件: `Base*` 前缀
- 单例组件: `The*` 前缀

**变量命名:**
- 变量: camelCase
- 布尔变量: `is*`, `has*` 前缀
- 私有变量: `_*` 前缀
- 常量: 全大写下划线分隔

## 前端开发规范

### 组件开发

1. 组件结构顺序: `<template>`, `<script>`, `<style>`
2. Props 需定义类型和默认值
3. 事件命名使用 kebab-case
4. 组件遵循单一职责原则

### 状态管理 (Pinia)

1. Store 按业务模块拆分
2. 定义清晰的 state、getters、actions
3. 异步操作放在 actions 中
4. 使用 pinia-plugin-persist-uni 持久化 (商城端)

### API 请求

**统一放在对应的 api 目录:**
- 管理端: `src/api`
- 商城端: `sheep/api`

**请求函数命名:** 动词 + 名词 (`getUserInfo`, `createOrder`)

**示例:**
```javascript
const AfterSaleApi = {
  getAfterSalePage: (params) => {
    return request({
      url: `/app-api/trade/after-sale/page`,
      method: 'GET',
      params,
    });
  },
  createAfterSale: (data) => {
    return request({
      url: `/app-api/trade/after-sale/create`,
      method: 'POST',
      data,
    });
  }
};
```

## 数据库规范

1. 表名: 小写字母,下划线分隔
2. 主键: `id` (bigint)
3. 必含字段: `create_time`, `update_time`
4. 软删除: `deleted` 字段
5. 字段命名: 小写字母,下划线分隔
6. 索引命名: `idx_字段名`

## 版本控制规范

### Git 工作流

- 主分支: `main`/`master` (生产环境)
- 开发分支: `develop` (开发代码)
- 功能分支: `feature/功能名称`
- 发布分支: `release/版本号`
- 修复分支: `hotfix/问题描述`

### 提交规范 (Conventional Commits)

**格式:** `<类型>[可选的作用域]: <描述>`

**类型:**
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档变更
- `style`: 代码格式变更
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变更

**示例:** `feat(user): 添加用户注册功能`

### 版本号管理

遵循语义化版本 (Semantic Versioning):
- 格式: `主版本号.次版本号.修订号`
- 主版本号: 不兼容的 API 修改
- 次版本号: 向下兼容的功能性新增
- 修订号: 向下兼容的问题修正
- 开发版本: `-SNAPSHOT` 后缀

## 环境设置

### 使用设置脚本

项目提供了完整的环境设置脚本:

```bash
# 运行交互式设置菜单
./src-claude-plus/setup-and-start.sh
```

**脚本功能:**
1. 切换 Node.js 版本 (默认 Node 22)
2. 安装 ccpm (Claude Code 包管理器)
3. 启动 ccr (Claude Code 路由器)
4. 启动 claude CLI
5. 安装 cc-statusline
6. 升级 Claude Code
7. 一键安装常见 MCP
8. 浏览 MCP 列表并安装
9. 管理已安装 MCP

### 包管理器

- 后端: Maven
- 管理前端: pnpm 8.6.0+
- 商城前端: npm

### 环境要求

- Java 17+
- Node.js 16+ (推荐 Node 22)
- Maven 3.6+
- pnpm 8.6.0+ (管理端)

## 重要提示

1. **始终使用中文与我对话**
2. 后端严格遵循三层架构和分层调用原则
3. 跨模块调用必须通过 Service,禁止直接调用 Mapper
4. 前后端接口严格遵循 RESTful 规范
5. 所有配置使用配置中心或环境变量,不允许硬编码
6. 代码提交前必须进行自我审查
7. 使用正确的包管理器: 后端用 Maven,管理前端用 pnpm,商城用 npm
8. 商城应用支持多平台部署 (H5、微信小程序、支付宝小程序等)
9. 前后端编码始终遵循DRY、KISS原则，并且代码要具备足够的可读性、简洁性、可测试性、灵活性、可扩展性
10. 要具备良好的代码风格，遵循阿里巴巴的Java开发规范
11. 结合需求与代码，综合运用合适的设计模式来解决需求，少量改动的需求直接修改；
