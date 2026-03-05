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
      <el-form-item label="分类名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入分类名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="所属板块" prop="sectionId">
        <el-select
          v-model="queryParams.sectionId"
          placeholder="请选择板块"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="section in sectionList"
            :key="section.id"
            :label="section.name"
            :value="section.id"
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
          v-hasPermi="['promotion:cms-category:create']"
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
      <el-table-column label="分类名称" align="center" prop="name" />
      <el-table-column label="所属板块" align="center" prop="sectionId" width="150">
        <template #default="scope">
          {{ getSectionName(scope.row.sectionId) }}
        </template>
      </el-table-column>
      <el-table-column label="图标" align="center" prop="icon" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.icon"
            class="h-40px max-w-40px"
            :src="scope.row.icon"
            :preview-src-list="[scope.row.icon]"
            preview-teleported
          />
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
            v-hasPermi="['promotion:cms-category:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['promotion:cms-category:delete']"
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
    width="600px"
    append-to-body
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="所属板块" prop="sectionId">
        <el-select
          v-model="formData.sectionId"
          placeholder="请选择板块"
          class="!w-100%"
        >
          <el-option
            v-for="section in sectionList"
            :key="section.id"
            :label="section.name"
            :value="section.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <UploadImg v-model="formData.icon" :limit="1" />
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
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as CmsCategoryApi from '@/api/mall/cms/category'
import * as CmsSectionApi from '@/api/mall/promotion/cmsSection'

/** CMS分类管理 */
defineOptions({ name: 'CmsCategory' })

const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  sectionId: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

// 板块列表
const sectionList = ref([])

/** 查询板块列表 */
const getSectionList = async () => {
  const data = await CmsSectionApi.getCmsSectionList()
  sectionList.value = data || []
}

/** 根据板块ID获取板块名称 */
const getSectionName = (sectionId: number) => {
  const section = sectionList.value.find((item: any) => item.id === sectionId)
  return section ? section.name : ''
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CmsCategoryApi.getCmsCategoryPage(queryParams)
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
const formData = ref({
  id: undefined,
  sectionId: undefined,
  name: '',
  icon: '',
  sort: 0,
  status: 0
})
const formRules = reactive({
  sectionId: [{ required: true, message: '所属板块不能为空', trigger: 'change' }],
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})

/** 打开弹窗 */
const openForm = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增分类' : '编辑分类'
  // 修改时，设置数据
  if (id) {
    const data = await CmsCategoryApi.getCmsCategory(id)
    formData.value = data
  } else {
    formData.value = {
      id: undefined,
      sectionId: undefined,
      name: '',
      icon: '',
      sort: 0,
      status: 0
    }
  }
}

/** 提交表单 */
const submitForm = async () => {
  // 校验表单
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  try {
    const data = formData.value
    if (data.id) {
      await CmsCategoryApi.updateCmsCategory(data)
      message.success('修改成功')
    } else {
      await CmsCategoryApi.createCmsCategory(data)
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
    await CmsCategoryApi.deleteCmsCategory(id)
    message.success('删除成功')
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(async () => {
  await getSectionList()
  await getList()
})
</script>
