# 分销系统代码规范

## 1. 项目概述

这是一个前后端分离的分销系统，基于 Spring Boot 3.4.1 + Vue 3 技术栈构建，支持多端部署（管理后台、小程序商城）。

## 2. 技术栈

### 后端技术栈
- **框架**: Java 17 + Spring Boot 3.4.1
- **构建工具**: Maven 多模块架构
- **数据库**: MyBatis Plus + MySQL
- **缓存**: Redis
- **文档**: Swagger 3 (OpenAPI)
- **代码生成**: MapStruct、Lombok

### 前端技术栈
- **管理后台**: Vue 3 + TypeScript + Element Plus + Vite
- **小程序商城**: Uni-app + Vue 3 + JavaScript
- **状态管理**: Pinia
- **请求库**: Axios
- **包管理**: 管理端用 pnpm，小程序用 npm

## 3. 项目结构

### 3.1 后端项目结构

\`\`\`
src-claude-plus/backend/hissp-distribution/
├── distribution-framework/          # 框架层
│   ├── distribution-common/        # 公共工具类、异常处理、通用POJO
│   ├── distribution-spring-boot-starter-*  # 自动配置模块
│   └── distribution-mybatis/     # MyBatis 配置
├── distribution-dependencies/       # 统一依赖管理
├── distribution-server/           # 主应用服务器
└── distribution-modules/         # 业务模块
    ├── distribution-module-mall/      # 商城模块
    ├── distribution-module-crm/       # 客户关系管理
    ├── distribution-module-erp/       # 企业资源计划
    ├── distribution-module-system/    # 系统管理
    ├── distribution-module-infra/    # 基础设施
    └── distribution-module-bpm/     # 业务流程管理
\`\`\`

### 3.2 前端项目结构

\`\`\`
src-claude-plus/frontend/
├── yudao-ui-admin-vue3/        # 管理后台
│   ├── src/
│   │   ├── api/               # API 接口封装
│   │   ├── views/             # 页面组件
│   │   ├── components/        # 公共组件
│   │   ├── store/             # Pinia 状态管理
│   │   ├── router/            # 路由配置
│   │   ├── utils/             # 工具函数
│   │   └── types/             # TypeScript 类型定义
│   ├── package.json
│   └── vite.config.ts
└── distribution-mall-mini-vue3/  # 商城小程序
    ├── pages/                # 页面组件
    ├── sheep/               # 核心框架和工具
    │   ├── api/            # API 请求
    │   ├── components/     # 公共组件
    │   ├── store/          # Pinia 状态管理
    │   ├── util/           # 工具函数
    │   └── platform/       # 平台适配
    ├── static/               # 静态资源
    ├── uni_modules/          # uni-app 模块
    └── package.json
\`\`\`

## 4. 后端代码规范

### 4.1 分层架构

严格遵循三层架构模式：

**接入层 (Controller)**
\`\`\`java
@Tag(name = "管理后台 - 商品分类")
@RestController
@RequestMapping("/product/category")
@Validated
public class ProductCategoryController {
    @Resource
    private ProductCategoryService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建商品分类")
    @PreAuthorize("@ss.hasPermission('product:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody ProductCategorySaveReqVO createReqVO) {
        return success(categoryService.createCategory(createReqVO));
    }
}
\`\`\`

**逻辑层 (Service)**
\`\`\`java
@Service
@Validated
public class ProductCategoryServiceImpl implements ProductCategoryService {
    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Override
    public Long createCategory(ProductCategorySaveReqVO createReqVO) {
        // 业务校验
        validateParentProductCategory(createReqVO.getParentId());

        // 数据转换
        ProductCategoryDO category = BeanUtils.toBean(createReqVO, ProductCategoryDO.class);
        productCategoryMapper.insert(category);
        return category.getId();
    }
}
\`\`\`

**存储层 (Mapper)**
\`\`\`java
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategoryDO> {
    List<ProductCategoryDO> selectList(ProductCategoryListReqVO listReqVO);

    @Select("SELECT COUNT(*) FROM product_category WHERE parent_id = #{parentId} AND deleted = false")
    int selectCountByParentId(Long parentId);
}
\`\`\`

### 4.2 数据对象规范

**实体类 (DO)**
\`\`\`java
@TableName("product_category")
@KeySequence("product_category_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDO extends BaseDO {

    @TableId
    private Long id;

    @Schema(description = "父分类编号", example = "0")
    private Long parentId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    public static final Long PARENT_ID_NULL = 0L;
    public static final int CATEGORY_LEVEL = 2;
}
\`\`\`

**请求对象 (VO)**
\`\`\`java
@Schema(description = "管理后台 - 商品分类新增/更新 Request VO")
@Data
public class ProductCategorySaveReqVO {

    @Schema(description = "分类编号", example = "2")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "办公文具")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "父分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;
}
\`\`\`

### 4.3 API 接口规范

**URL 设计规则**
- 管理端: \`/admin-api/{模块名}/{资源名}\`
- 应用端: \`/app-api/{模块名}/{资源名}\`
- 路径使用小写连字符分隔: \`/product/category\`

**请求方法规范**
- 获取列表: \`GET /{resource}/page\`
- 获取详情: \`GET /{resource}/get?id={id}\`
- 创建资源: \`POST /{resource}/create\`
- 更新资源: \`PUT /{resource}/update\`
- 删除资源: \`DELETE /{resource}/delete?id={id}\`

**响应格式统一**
\`\`\`java
// 成功响应
CommonResult.success(data)

// 分页响应
CommonResult.success(PageResult<T>)

// 错误响应
CommonResult.error(ErrorCodeConstants.CATEGORY_NOT_EXISTS)
\`\`\`

### 4.4 命名规范

**类命名 (PascalCase)**
- Controller: \`*Controller\`
- Service: \`*Service\` / \`*ServiceImpl\`
- Mapper: \`*Mapper\`
- DO: \`*DO\` (数据对象)
- VO: \`*ReqVO\` / \`*RespVO\` / \`*SaveReqVO\`
- 枚举: \`*Enum\`

**方法命名 (camelCase)**
- 查询: \`getXxx\`, \`findXxx\`, \`queryXxx\`
- 插入: \`createXxx\`, \`saveXxx\`, \`insertXxx\`
- 更新: \`updateXxx\`
- 删除: \`deleteXxx\`, \`removeXxx\`
- 校验: \`validateXxx\`

**变量命名**
- 普通变量: camelCase
- 布尔变量: \`is*\`, \`has*\` 前缀
- 常量: 全大写+下划线分隔
- 包名: 小写字母，点分隔

## 5. 前端代码规范

### 5.1 Vue 3 组件规范

**管理后台组件 (Vue 3 + TypeScript)**
\`\`\`vue
<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="分类名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入分类名" />
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="分类名称" prop="name" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { CategoryApi, CategoryVO } from '@/api/mall/product/category'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'ProductCategory' })

const message = useMessage()
const loading = ref(true)
const list = ref<CategoryVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await CategoryApi.getCategoryPage(queryParams)
    list.value = data.list
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>
\`\`\`

**小程序组件 (Uni-app + JavaScript)**
\`\`\`vue
<template>
  <view class="category-list">
    <view v-for="item in categories" :key="item.id" class="category-item">
      <text>{{ item.name }}</text>
    </view>
  </view>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import CategoryApi from '@/sheep/api/product/category'

export default {
  setup() {
    const categories = ref([])

    const getCategories = async () => {
      const data = await CategoryApi.getCategoryList()
      categories.value = data
    }

    onMounted(() => {
      getCategories()
    })

    return {
      categories
    }
  }
}
</script>
\`\`\`

### 5.2 API 封装规范

**管理后台 (TypeScript)**
\`\`\`typescript
import request from '@/config/axios'

export interface CategoryVO {
  id?: number
  parentId?: number
  name: string
  picUrl: string
  sort: number
  status: number
}

export const CategoryApi = {
  createCategory: (data: CategoryVO) => {
    return request.post({ url: '/product/category/create', data })
  },

  getCategory: (id: number) => {
    return request.get({ url: \`/product/category/get?id=\${id}\` })
  },

  getCategoryPage: (params: any) => {
    return request.get({ url: '/product/category/page', params })
  }
}
\`\`\`

**小程序 (JavaScript)**
\`\`\`javascript
import request from '@/sheep/request'

const CategoryApi = {
  getCategoryList: () => {
    return request({
      url: '/product/category/list',
      method: 'GET'
    })
  },

  getCategoryListByIds: (ids) => {
    return request({
      url: '/product/category/list-by-ids',
      method: 'GET',
      params: { ids }
    })
  }
}

export default CategoryApi
\`\`\`

### 5.3 命名规范

**文件命名**
- 组件文件: PascalCase (\`UserProfile.vue\`)
- 页面文件: kebab-case (\`user-profile.vue\`)
- 工具函数: camelCase (\`formatDate.js\`)
- API 文件: camelCase (\`category.ts\`)
- 类型文件: camelCase (\`category.d.ts\`)

**变量命名**
- 变量: camelCase
- 布尔变量: \`is*\`, \`has*\` 前缀
- 私有变量: \`_*\` 前缀
- 常量: 全大写+下划线分隔
- CSS 类名: kebab-case (BEM 规范)

**组件命名**
- 基础组件: \`Base*\` 前缀
- 单例组件: \`The*\` 前缀
- 页面组件: PascalCase，与文件名一致

### 5.4 状态管理规范

**Pinia Store**
\`\`\`typescript
import { defineStore } from 'pinia'
import { CategoryApi } from '@/api/mall/product/category'

interface CategoryState {
  categories: CategoryVO[]
  currentCategory: CategoryVO | null
}

export const useCategoryStore = defineStore('category', {
  state: (): CategoryState => ({
    categories: [],
    currentCategory: null
  }),

  getters: {
    getCategoryById: (state) => (id: number) => {
      return state.categories.find(item => item.id === id)
    }
  },

  actions: {
    async fetchCategories() {
      const data = await CategoryApi.getCategoryList()
      this.categories = data
    },

    setCurrentCategory(category: CategoryVO) {
      this.currentCategory = category
    }
  }
})
\`\`\`

## 6. 数据库规范

### 6.1 表设计规范
- 表名: 小写字母，下划线分隔
- 主键: \`id\` (bigint)
- 必含字段: \`create_time\`, \`update_time\`
- 软删除: \`deleted\` 字段 (boolean)
- 字段命名: 小写字母，下划线分隔
- 索引命名: \`idx_字段名\`

### 6.2 SQL 规范示例
\`\`\`sql
CREATE TABLE \`product_category\` (
  \`id\` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
  \`parent_id\` bigint NOT NULL DEFAULT 0 COMMENT '父分类编号',
  \`name\` varchar(50) NOT NULL COMMENT '分类名称',
  \`pic_url\` varchar(255) NULL COMMENT '移动端分类图',
  \`sort\` int NOT NULL DEFAULT 0 COMMENT '分类排序',
  \`status\` tinyint NOT NULL DEFAULT 0 COMMENT '开启状态',
  \`create_time\` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  \`update_time\` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  \`deleted\` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (\`id\`),
  INDEX \`idx_parent_id\` (\`parent_id\`),
  INDEX \`idx_status\` (\`status\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
\`\`\`

## 7. 开发环境配置

### 7.1 开发命令

**后端 (Java/Maven)**
\`\`\`bash
cd src-claude-plus/backend/hissp-distribution
mvn clean install -DskipTests
mvn spring-boot:run
\`\`\`

**管理前端 (Vue 3)**
\`\`\`bash
cd src-claude-plus/frontend/yudao-ui-admin-vue3
pnpm install
pnpm dev              # 本地开发
pnpm build:prod        # 生产构建
pnpm lint:eslint       # ESLint 检查
pnpm lint:format       # Prettier 格式化
\`\`\`

**小程序前端 (Uni-app)**
\`\`\`bash
cd src-claude-plus/frontend/distribution-mall-mini-vue3
npm install
npm run dev:h5              # H5 开发
npm run dev:mp-weixin       # 微信小程序
npm run build:mp-weixin     # 微信小程序构建
npm run prettier             # 代码格式化
\`\`\`

### 7.2 环境要求
- **Java**: JDK 17+
- **Node.js**: 16+ (推荐 Node 22)
- **Maven**: 3.6+
- **pnpm**: 8.6.0+ (管理端)
- **npm**: 最新版本 (小程序端)

## 8. Git 提交规范

使用 Conventional Commits 规范：
- \`feat\`: 新功能
- \`fix\`: 修复 bug
- \`docs\`: 文档变更
- \`style\`: 代码格式变更
- \`refactor\`: 代码重构
- \`perf\`: 性能优化
- \`test\`: 测试相关
- \`chore\`: 构建过程或辅助工具变更

示例：\`feat(product): 添加商品分类管理功能\`

## 9. 关键原则

### 9.1 代码质量原则
1. **单一职责**: 每个函数、类、组件只负责一个功能
2. **开闭原则**: 对扩展开放，对修改封闭
3. **依赖倒置**: 依赖抽象而非具体实现
4. **接口隔离**: 不强迫依赖不使用的接口
5. **最小惊讶**: 代码行为符合直觉预期

### 9.2 安全原则
1. **输入验证**: 所有用户输入必须验证
2. **权限控制**: 使用 \`@PreAuthorize\` 注解控制接口权限
3. **SQL 注入防护**: 使用参数化查询
4. **敏感数据**: 不在日志中输出敏感信息

### 9.3 性能原则
1. **懒加载**: 组件和数据按需加载
2. **缓存策略**: 合理使用 Redis 缓存热点数据
3. **分页查询**: 大数据量必须分页
4. **索引优化**: 数据库查询走索引

## 10. 工具和配置

### 10.1 代码格式化
- **Java**: 使用 Lombok 和 MapStruct 注解
- **TypeScript**: 使用 Prettier + ESLint
- **JavaScript**: 使用 Prettier

### 10.2 IDE 配置
- 统一使用 UTF-8 编码
- 设置正确的缩进（2 空格）
- 配置 ESLint 和 Prettier 插件

### 10.3 构建优化
- **前端**: 使用 Vite 构建工具，支持 HMR
- **后端**: Maven 多模块构建，支持增量构建
- **小程序**: Uni-app 多端构建，支持平台差异化配置

---

本规范文档涵盖了项目的主要技术栈和开发规范，所有开发人员应严格遵循以确保代码质量和项目可维护性。
