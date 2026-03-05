<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="板块名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入板块名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="板块类型" prop="type">
        <el-select
          v-model="queryParams.type"
          placeholder="请选择板块类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="item in SECTION_TYPES"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['promotion:cms-section:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="板块名称" align="center" prop="name" />
      <el-table-column label="板块类型" align="center" prop="type" width="120">
        <template #default="scope">
          {{ getSectionTypeLabel(scope.row.type) }}
        </template>
      </el-table-column>
      <el-table-column label="排版样式" align="center" prop="layoutStyle" width="120" />
      <el-table-column label="封面展示" align="center" prop="coverDisplayType" width="100">
        <template #default="scope">
          {{ getCoverDisplayLabel(scope.row.coverDisplayType) }}
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['promotion:cms-section:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['promotion:cms-section:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <el-dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="700px"
    append-to-body
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
    >
      <el-form-item label="板块名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入板块名称" />
      </el-form-item>
      <el-form-item label="板块类型" prop="type">
        <el-select
          v-model="formData.type"
          placeholder="请选择板块类型"
          @change="handleTypeChange"
          class="!w-100%"
        >
          <el-option
            v-for="item in SECTION_TYPES"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="排版样式" prop="layoutStyle">
        <el-select v-model="formData.layoutStyle" placeholder="请选择排版样式" class="!w-100%">
          <el-option label="文章样式" value="article_style" />
          <el-option label="动态样式" value="dynamic_style" />
          <el-option label="课程样式" value="course_style" />
          <el-option label="自定义样式" value="custom_style" />
        </el-select>
      </el-form-item>
      <el-form-item label="封面展示" prop="coverDisplayType">
        <el-radio-group v-model="formData.coverDisplayType">
          <el-radio value="single">单图</el-radio>
          <el-radio value="grid">栅格</el-radio>
          <el-radio value="carousel">轮播</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="功能按钮">
        <el-checkbox-group v-model="configButtons">
          <el-checkbox value="like">点赞</el-checkbox>
          <el-checkbox value="collect">收藏</el-checkbox>
          <el-checkbox value="download">下载</el-checkbox>
          <el-checkbox value="enroll">报名</el-checkbox>
          <el-checkbox value="share">分享</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="用户内容生产">
        <el-switch v-model="ugcEnabled" />
        <span class="ml-10px text-gray-500">开启后允许用户在此板块发布内容</span>
      </el-form-item>
      <el-form-item v-if="ugcEnabled" label="自动审核">
        <el-switch v-model="autoAuditEnabled" />
        <span class="ml-10px text-gray-500">开启后系统会在设置时间后自动审核通过</span>
      </el-form-item>
      <el-form-item
        v-if="ugcEnabled && autoAuditEnabled"
        label="自动审核时间"
        prop="autoAuditDelayMinutes"
      >
        <div class="flex items-center gap-10px">
          <el-input-number
            v-model="autoAuditDelayMinutes"
            :min="1"
            :max="1440"
            controls-position="right"
          />
          <span class="text-gray-500">分钟后自动通过</span>
        </div>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" controls-position="right" :min="0" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as CmsSectionApi from '@/api/mall/promotion/cmsSection'

/** CMS板块管理 */
defineOptions({ name: 'CmsSection' })

const message = useMessage() // 消息弹窗

// 板块类型选项
const SECTION_TYPES = [
  { label: '文章类', value: 'article' },
  { label: '动态类', value: 'dynamic' },
  { label: '课程类', value: 'course' },
  { label: '自定义', value: 'custom' }
]

// 封面显示类型映射
const COVER_DISPLAY_MAP: Record<string, string> = {
  single: '单图',
  grid: '栅格',
  carousel: '轮播'
}

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  type: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 获取板块类型标签 */
const getSectionTypeLabel = (type: string) => {
  const item = SECTION_TYPES.find(t => t.value === type)
  return item ? item.label : type
}

/** 获取封面显示类型标签 */
const getCoverDisplayLabel = (type: string) => {
  return COVER_DISPLAY_MAP[type] || type
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CmsSectionApi.getCmsSectionPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formRef = ref() // 表单 Ref

const BUTTON_KEYS = ['like', 'collect', 'download', 'enroll', 'share']

// 配置按钮和UGC开关
const configButtons = ref<string[]>([])
const ugcEnabled = ref(false)
const autoAuditEnabled = ref(false)
const autoAuditDelayMinutes = ref(30)

const formData = ref({
  id: undefined,
  name: '',
  type: '',
  layoutStyle: '',
  coverDisplayType: 'single',
  config: {},
  requireAudit: false,
  autoAuditEnabled: false,
  autoAuditDelayMinutes: 0,
  sort: 0,
  status: 0
})

const formRules = reactive({
  name: [{ required: true, message: '板块名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '板块类型不能为空', trigger: 'change' }],
  layoutStyle: [{ required: true, message: '排版样式不能为空', trigger: 'change' }],
  coverDisplayType: [{ required: true, message: '封面展示类型不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }],
  autoAuditDelayMinutes: [
    {
      validator: (_rule: any, value: number, callback: (error?: Error) => void) => {
        if (ugcEnabled.value && autoAuditEnabled.value) {
          if (!value || value <= 0) {
            callback(new Error('请输入大于 0 的分钟数'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
})

watch(ugcEnabled, (value) => {
  if (!value) {
    autoAuditEnabled.value = false
    autoAuditDelayMinutes.value = 0
    formData.value.requireAudit = false
  } else {
    formData.value.requireAudit = true
    if (autoAuditEnabled.value && autoAuditDelayMinutes.value <= 0) {
      autoAuditDelayMinutes.value = 30
    }
  }
})

watch(autoAuditEnabled, (value) => {
  formData.value.autoAuditEnabled = value
  if (value) {
    formData.value.requireAudit = true
    if (autoAuditDelayMinutes.value <= 0) {
      autoAuditDelayMinutes.value = 30
    }
  } else {
    autoAuditDelayMinutes.value = 0
  }
})

watch(autoAuditDelayMinutes, (value) => {
  formData.value.autoAuditDelayMinutes = value || 0
})

const normalizeButtonFlag = (value: any): boolean => {
  if (value === undefined || value === null) {
    return false
  }
  if (typeof value === 'object') {
    return normalizeButtonFlag((value as any).show)
  }
  if (typeof value === 'string') {
    const lower = value.trim().toLowerCase()
    return lower === 'true' || lower === '1' || lower === 'yes'
  }
  if (typeof value === 'number') {
    return value !== 0
  }
  return !!value
}

const extractButtonSelection = (
  cardButtons?: Record<string, any>,
  legacyButtons?: Record<string, any>
): string[] => {
  const selection: string[] = []
  BUTTON_KEYS.forEach((key) => {
    let candidate = cardButtons?.[key]
    if (candidate === undefined && legacyButtons?.[key] !== undefined) {
      candidate = legacyButtons?.[key]
    }
    if (normalizeButtonFlag(candidate)) {
      selection.push(key)
    }
  })
  return selection
}

// 板块类型默认配置
const DEFAULT_TYPE_CONFIG: Record<string, any> = {
  article: {
    layoutStyle: 'article_style',
    cardButtons: { like: true, collect: true, download: true, enroll: false, share: true },
    ugc_enabled: false,
    autoAuditEnabled: false,
    autoAuditDelayMinutes: 30
  },
  dynamic: {
    layoutStyle: 'dynamic_style',
    cardButtons: { like: true, collect: true, download: false, enroll: false, share: true },
    ugc_enabled: true,
    autoAuditEnabled: true,
    autoAuditDelayMinutes: 30
  },
  course: {
    layoutStyle: 'course_style',
    cardButtons: { like: true, collect: true, download: false, enroll: true, share: true },
    ugc_enabled: false,
    autoAuditEnabled: false,
    autoAuditDelayMinutes: 30
  },
  custom: {
    layoutStyle: 'custom_style',
    cardButtons: { like: false, collect: false, download: false, enroll: false, share: false },
    ugc_enabled: false,
    autoAuditEnabled: false,
    autoAuditDelayMinutes: 30
  }
}

/** 板块类型切换 */
const handleTypeChange = (type: string) => {
  const config = DEFAULT_TYPE_CONFIG[type]
  if (config) {
    formData.value.layoutStyle = config.layoutStyle
    // 更新按钮配置
    configButtons.value = extractButtonSelection(config.cardButtons, config.buttons)
    ugcEnabled.value = config.ugc_enabled
    autoAuditEnabled.value = config.autoAuditEnabled ?? false
    autoAuditDelayMinutes.value =
      config.autoAuditDelayMinutes ?? (autoAuditEnabled.value ? 30 : 0)
    formData.value.requireAudit = ugcEnabled.value ? true : false
    formData.value.autoAuditEnabled = autoAuditEnabled.value
    formData.value.autoAuditDelayMinutes = autoAuditDelayMinutes.value
  }
}

/** 更新config对象 */
const updateConfig = () => {
  const cardButtons: Record<string, { show: boolean }> = {}
  BUTTON_KEYS.forEach((key) => {
    cardButtons[key] = { show: configButtons.value.includes(key) }
  })
  const nextConfig = {
    ...(formData.value.config || {}),
    cardButtons,
    ugc_enabled: ugcEnabled.value,
    requireAudit: ugcEnabled.value ? true : false,
    autoAuditEnabled: ugcEnabled.value ? autoAuditEnabled.value : false,
    autoAuditDelayMinutes:
      ugcEnabled.value && autoAuditEnabled.value ? autoAuditDelayMinutes.value : 0
  }
  delete (nextConfig as any).buttons

  formData.value.config = nextConfig
  formData.value.requireAudit = ugcEnabled.value ? true : false
  formData.value.autoAuditEnabled = ugcEnabled.value ? autoAuditEnabled.value : false
  formData.value.autoAuditDelayMinutes =
    ugcEnabled.value && autoAuditEnabled.value ? autoAuditDelayMinutes.value : 0
}

/** 打开弹窗 */
const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增板块' : '编辑板块'
  // 修改时，设置数据
  if (id) {
    const data = await CmsSectionApi.getCmsSection(id)
    formData.value = data
    // 解析config到UI状态
    if (data.config) {
      configButtons.value = extractButtonSelection(data.config.cardButtons, data.config.buttons)
      ugcEnabled.value = data.config.ugc_enabled || false
      autoAuditEnabled.value =
        (data.autoAuditEnabled ?? data.config.autoAuditEnabled) || false
      const delay =
        data.autoAuditDelayMinutes ?? data.config.autoAuditDelayMinutes ?? 0
      autoAuditDelayMinutes.value = delay > 0 ? delay : autoAuditEnabled.value ? 30 : 0
    }
    formData.value.requireAudit =
      data.requireAudit ?? data.config?.requireAudit ?? (ugcEnabled.value ? true : false)
    formData.value.autoAuditEnabled = autoAuditEnabled.value
    formData.value.autoAuditDelayMinutes = autoAuditDelayMinutes.value
  } else {
    formData.value = {
      id: undefined,
      name: '',
      type: '',
      layoutStyle: '',
      coverDisplayType: 'single',
      config: {},
      requireAudit: false,
      autoAuditEnabled: false,
      autoAuditDelayMinutes: 0,
      sort: 0,
      status: 0
    }
    configButtons.value = []
    ugcEnabled.value = false
    autoAuditEnabled.value = false
    autoAuditDelayMinutes.value = 0
    formData.value.requireAudit = false
  }
}

/** 提交表单 */
const submitForm = async () => {
  // 校验表单
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 更新config
  updateConfig()

  // 提交请求
  try {
    const data = formData.value
    if (data.id) {
      await CmsSectionApi.updateCmsSection(data)
      message.success('修改成功')
    } else {
      await CmsSectionApi.createCmsSection(data)
      message.success('新增成功')
    }
    dialogVisible.value = false
    // 刷新列表
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await CmsSectionApi.deleteCmsSection(id)
    message.success('删除成功')
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
