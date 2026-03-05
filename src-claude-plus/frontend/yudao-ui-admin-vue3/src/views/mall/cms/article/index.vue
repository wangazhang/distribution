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
      <el-form-item label="文章标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入文章标题"
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
      <el-form-item label="所属分类" prop="categoryId">
        <el-select
          v-model="queryParams.categoryId"
          placeholder="请选择分类"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="category in categoryList"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select
          v-model="queryParams.auditStatus"
          placeholder="请选择审核状态"
          clearable
          class="!w-240px"
        >
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select
          v-model="queryParams.publishStatus"
          placeholder="请选择发布状态"
          clearable
          class="!w-240px"
        >
          <el-option label="未发布" :value="0" />
          <el-option label="已发布" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['promotion:cms-article:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleBatchAudit"
          :disabled="multipleSelection.length === 0"
          v-hasPermi="['promotion:cms-article:audit']"
        >
          <Icon icon="ep:check" class="mr-5px" /> 批量审核
        </el-button>
        <el-button
          type="warning"
          plain
          @click="handleBatchPublish"
          :disabled="multipleSelection.length === 0"
          v-hasPermi="['promotion:cms-article:publish']"
        >
          <Icon icon="ep:upload" class="mr-5px" /> 批量发布
        </el-button>
        <el-button
          type="info"
          plain
          @click="handleBatchUnpublish"
          :disabled="multipleSelection.length === 0"
          v-hasPermi="['promotion:cms-article:publish']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 批量下架
        </el-button>
        <el-button
          type="danger"
          plain
          @click="handleBatchDelete"
          :disabled="multipleSelection.length === 0"
          v-hasPermi="['promotion:cms-article:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="文章标题" align="center" prop="title" min-width="200" show-overflow-tooltip />
      <el-table-column label="板块" align="center" prop="sectionId" width="120">
        <template #default="scope">{{ getSectionName(scope.row.sectionId) }}</template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="categoryId" width="120">
        <template #default="scope">{{ getCategoryName(scope.row.categoryId) }}</template>
      </el-table-column>
      <el-table-column label="作者" align="center" prop="authorName" width="120" />
      <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.auditStatus === 'pending'" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 'approved'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 'rejected'" type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布状态" align="center" prop="publishStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.publishStatus === 1" type="success">已发布</el-tag>
          <el-tag v-else type="info">未发布</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="浏览数" align="center" prop="viewCount" width="80" />
      <el-table-column label="点赞数" align="center" prop="likeCount" width="80" />
      <el-table-column
        label="发布时间"
        align="center"
        prop="publishTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['promotion:cms-article:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="handleAudit(scope.row)"
            v-if="scope.row.auditStatus === 'pending'"
            v-hasPermi="['promotion:cms-article:audit']"
          >
            审核
          </el-button>
          <el-button
            link
            type="warning"
            @click="handlePublish(scope.row.id)"
            v-if="scope.row.publishStatus === 0 && scope.row.auditStatus === 'approved'"
            v-hasPermi="['promotion:cms-article:publish']"
          >
            发布
          </el-button>
          <el-button
            link
            type="info"
            @click="handleUnpublish(scope.row.id)"
            v-if="scope.row.publishStatus === 1"
            v-hasPermi="['promotion:cms-article:publish']"
          >
            下架
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['promotion:cms-article:delete']"
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

  <!-- 文章表单弹窗 -->
  <el-dialog
    v-model="formDialogVisible"
    :title="formDialogTitle"
    width="80%"
    append-to-body
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="所属板块" prop="sectionId">
            <el-select
              v-model="formData.sectionId"
              placeholder="请选择板块"
              @change="handleSectionChange"
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
        </el-col>
        <el-col :span="24">
          <el-form-item label="所属分类" prop="categoryId">
            <el-select
              v-model="formData.categoryId"
              placeholder="请选择分类"
              class="!w-100%"
              :disabled="!formData.sectionId"
            >
              <el-option
                v-for="category in filteredCategoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="文章标题" prop="title">
            <el-input
              v-model="formData.title"
              placeholder="请输入文章标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="副标题" prop="subtitle">
            <el-input
              v-model="formData.subtitle"
              placeholder="请输入副标题(可选)"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="封面图片" prop="coverImages">
            <UploadImgs v-model="formData.coverImages" :limit="9" />
            <div class="form-item-tip">最多上传9张图片</div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="内容类型" prop="contentType">
            <el-radio-group v-model="formData.contentType">
              <el-radio value="richtext">富文本</el-radio>
              <el-radio value="markdown">Markdown</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="文章内容" prop="content">
            <!-- 富文本编辑器 -->
            <Editor
              v-if="formData.contentType === 'richtext'"
              v-model="formData.content"
              :height="500"
            />
            <!-- Markdown编辑器 -->
            <el-input
              v-else
              v-model="formData.content"
              type="textarea"
              :rows="20"
              placeholder="请输入Markdown格式内容"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="关联标签" prop="tagIds">
            <el-select
              v-model="formData.tagIds"
              multiple
              placeholder="请选择标签"
              class="!w-100%"
            >
              <el-option
                v-for="tag in tagList"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="热门标记" prop="isHot">
            <el-switch v-model="formData.isHot" :active-value="1" :inactive-value="0" />
            <span class="ml-10px text-gray-500">标记为热门文章</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="官方标记" prop="isOfficial">
            <el-switch v-model="formData.isOfficial" :active-value="1" :inactive-value="0" />
            <span class="ml-10px text-gray-500">标记为官方文章</span>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="handleFormClose">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
    </template>
  </el-dialog>

  <!-- 审核对话框 -->
  <el-dialog title="审核文章" v-model="auditDialogVisible" width="500px" append-to-body>
    <el-form ref="auditFormRef" :model="auditFormData" :rules="auditFormRules" label-width="100px">
      <el-form-item label="审核结果" prop="auditStatus">
        <el-radio-group v-model="auditFormData.auditStatus">
          <el-radio value="approved">通过</el-radio>
          <el-radio value="rejected">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审核备注" prop="auditRemark">
        <el-input
          v-model="auditFormData.auditRemark"
          type="textarea"
          :rows="3"
          placeholder="请输入审核备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="auditDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitAudit">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as CmsArticleApi from '@/api/mall/promotion/cmsArticle'
import * as CmsSectionApi from '@/api/mall/promotion/cmsSection'
import * as CmsCategoryApi from '@/api/mall/promotion/cmsCategory'
import * as CmsTagApi from '@/api/mall/promotion/cmsTag'
import { Editor } from '@/components/Editor'
import { UploadImgs } from '@/components/UploadFile'

/** CMS文章管理 */
defineOptions({ name: 'CmsArticle' })

const message = useMessage() // 消息弹窗
const { push } = useRouter() // 路由

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const multipleSelection = ref([]) // 多选数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: undefined,
  sectionId: undefined,
  categoryId: undefined,
  auditStatus: undefined,
  publishStatus: undefined
})
const queryFormRef = ref() // 搜索的表单

// 板块和分类列表
const sectionList = ref([])
const categoryList = ref([])
const filteredCategoryList = ref([])
const tagList = ref([])

// 表单弹窗相关
const formDialogVisible = ref(false)
const formDialogTitle = ref('新增文章')
const formLoading = ref(false)
const formRef = ref()

// 表单数据
const formData = ref({
  id: undefined,
  sectionId: undefined,
  categoryId: undefined,
  title: '',
  subtitle: '',
  coverImages: [],
  content: '',
  contentType: 'richtext',
  tagIds: [],
  isHot: 0,
  isOfficial: 0
})

// 表单校验规则
const formRules = reactive({
  sectionId: [{ required: true, message: '所属板块不能为空', trigger: 'change' }],
  categoryId: [{ required: true, message: '所属分类不能为空', trigger: 'change' }],
  title: [{ required: true, message: '文章标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '文章内容不能为空', trigger: 'blur' }],
  contentType: [{ required: true, message: '内容类型不能为空', trigger: 'change' }]
})

/** 查询板块列表 */
const getSectionList = async () => {
  const data = await CmsSectionApi.getCmsSectionList()
  sectionList.value = data
}

/** 查询分类列表 */
const getCategoryList = async () => {
  const data = await CmsCategoryApi.getCmsCategoryList()
  categoryList.value = data
}

/** 根据板块ID获取板块名称 */
const getSectionName = (sectionId: number) => {
  const section = sectionList.value.find((item: any) => item.id === sectionId)
  return section ? section.name : ''
}

/** 根据分类ID获取分类名称 */
const getCategoryName = (categoryId: number) => {
  const category = categoryList.value.find((item: any) => item.id === categoryId)
  return category ? category.name : ''
}

/** 查询标签列表 */
const getTagList = async () => {
  const data = await CmsTagApi.getCmsTagList()
  tagList.value = data
}

/** 板块切换时加载分类 */
const handleSectionChange = async (sectionId: number) => {
  formData.value.categoryId = undefined
  if (sectionId) {
    // 获取所有分类，然后过滤出对应板块的分类
    const data = await CmsCategoryApi.getCmsCategoryList()
    filteredCategoryList.value = data.filter((category: any) => category.sectionId === sectionId)
  } else {
    filteredCategoryList.value = []
  }
}

/** 获取文章详情 */
const getArticleDetail = async (id: number) => {
  formLoading.value = true
  try {
    const data = await CmsArticleApi.getCmsArticle(id)
    formData.value = data
    // 加载对应板块的分类
    if (data.sectionId) {
      await handleSectionChange(data.sectionId)
    }
  } finally {
    formLoading.value = false
  }
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CmsArticleApi.getCmsArticlePage(queryParams)
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

/** 打开表单弹窗 */
const openForm = async (type: string, id?: number) => {
  formDialogVisible.value = true
  await nextTick()
  formRef.value?.resetFields()

  if (type === 'create') {
    formDialogTitle.value = '新增文章'
    formData.value = {
      id: undefined,
      sectionId: undefined,
      categoryId: undefined,
      title: '',
      subtitle: '',
      coverImages: [],
      content: '',
      contentType: 'richtext',
      tagIds: [],
      isHot: 0,
      isOfficial: 0
    }
    filteredCategoryList.value = []
  } else {
    formDialogTitle.value = '编辑文章'
    await getArticleDetail(id!)
  }
}

/** 关闭表单弹窗 */
const handleFormClose = () => {
  formDialogVisible.value = false
  formRef.value?.resetFields()
}

/** 提交表单 */
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  formLoading.value = true
  try {
    const data = { ...formData.value }
    if (data.id) {
      await CmsArticleApi.updateCmsArticle(data)
      message.success('修改成功')
    } else {
      await CmsArticleApi.createCmsArticle(data)
      message.success('新增成功')
    }
    formDialogVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

/** 审核对话框 */
const auditDialogVisible = ref(false)
const auditFormRef = ref()
const auditFormData = ref({
  id: undefined,
  auditStatus: 'approved',
  auditRemark: ''
})
const auditFormRules = reactive({
  auditStatus: [{ required: true, message: '审核结果不能为空', trigger: 'change' }]
})

/** 打开审核对话框 */
const handleAudit = (row: any) => {
  auditDialogVisible.value = true
  auditFormData.value = {
    id: row.id,
    auditStatus: 'approved',
    auditRemark: ''
  }
}

/** 提交审核 */
const submitAudit = async () => {
  if (!auditFormRef.value) return
  const valid = await auditFormRef.value.validate()
  if (!valid) return

  try {
    if (auditFormData.value.id) {
      // 单个审核
      await CmsArticleApi.auditCmsArticle(auditFormData.value)
      message.success('审核成功')
    } else {
      // 批量审核
      const ids = multipleSelection.value.map((item: any) => item.id)
      await CmsArticleApi.batchAuditCmsArticle({
        ids,
        auditStatus: auditFormData.value.auditStatus,
        auditRemark: auditFormData.value.auditRemark
      })
      message.success('批量审核成功')
    }
    auditDialogVisible.value = false
    await getList()
  } catch {}
}

/** 发布文章 */
const handlePublish = async (id: number) => {
  try {
    await message.confirm('确认发布该文章吗?')
    await CmsArticleApi.publishCmsArticle(id)
    message.success('发布成功')
    await getList()
  } catch {}
}

/** 下架文章 */
const handleUnpublish = async (id: number) => {
  try {
    await message.confirm('确认下架该文章吗?')
    await CmsArticleApi.unpublishCmsArticle(id)
    message.success('下架成功')
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await CmsArticleApi.deleteCmsArticle(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 多选变化 */
const handleSelectionChange = (selection: any[]) => {
  multipleSelection.value = selection
}

/** 批量审核 */
const handleBatchAudit = async () => {
  if (multipleSelection.value.length === 0) {
    message.warning('请选择要审核的文章')
    return
  }

  auditDialogVisible.value = true
  auditFormData.value = {
    id: undefined, // 批量审核时不需要单个ID
    auditStatus: 'approved',
    auditRemark: ''
  }
}

/** 批量发布 */
const handleBatchPublish = async () => {
  if (multipleSelection.value.length === 0) {
    message.warning('请选择要发布的文章')
    return
  }

  try {
    await message.confirm(`确认批量发布选中的 ${multipleSelection.value.length} 篇文章吗?`)
    const ids = multipleSelection.value.map((item: any) => item.id)
    await CmsArticleApi.batchPublishCmsArticle(ids)
    message.success('批量发布成功')
    await getList()
  } catch {}
}

/** 批量下架 */
const handleBatchUnpublish = async () => {
  if (multipleSelection.value.length === 0) {
    message.warning('请选择要下架的文章')
    return
  }

  try {
    await message.confirm(`确认批量下架选中的 ${multipleSelection.value.length} 篇文章吗?`)
    const ids = multipleSelection.value.map((item: any) => item.id)
    await CmsArticleApi.batchUnpublishCmsArticle(ids)
    message.success('批量下架成功')
    await getList()
  } catch {}
}

/** 批量删除 */
const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) {
    message.warning('请选择要删除的文章')
    return
  }

  try {
    await message.delConfirm(`确认批量删除选中的 ${multipleSelection.value.length} 篇文章吗?`)
    const ids = multipleSelection.value.map((item: any) => item.id)
    await CmsArticleApi.batchDeleteCmsArticle(ids)
    message.success('批量删除成功')
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(async () => {
  await getSectionList()
  await getCategoryList()
  await getTagList()
  await getList()
})
</script>
