<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      label-width="90px"
      class="-mb-15px"
    >
      <el-form-item label="商品" prop="productId">
        <div class="spu-select-container">
          <el-input
            v-model="queryProductName"
            placeholder="请选择商品"
            readonly
            class="!w-240px"
          >
            <template #suffix>
              <el-button type="primary" link size="small" @click="openQuerySpuSelect">
                选择商品
              </el-button>
            </template>
          </el-input>
          <el-button
            v-if="queryParams.productId"
            type="danger"
            link
            size="small"
            class="clear-btn"
            @click="clearQueryProduct"
          >
            清除
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="用户等级" prop="levelId">
        <MemberLevelSelect v-model="queryParams.levelId" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-160px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
        <el-button
          type="primary"
          @click="openDialog('create')"
          v-hasPermi="['mb:material-restock-price:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增价格
        </el-button>
        <el-button @click="handleExport" v-hasPermi="['mb:material-restock-price:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe :row-key="(row) => row.id">
      <el-table-column label="商品" prop="productName" min-width="160">
        <template #default="{ row }">
          <div class="flex items-center gap-x-6px">
            <span>{{ row.productName || `SPU#${row.productId}` }}</span>
            <el-tag v-if="!row.productName" type="info" size="small">名称缺失</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="商品ID" prop="productId" width="120" align="center" />
      <el-table-column label="用户等级" prop="levelName" min-width="140">
        <template #default="{ row }">
          {{ row.levelName || formatLevel(row.levelId) }}
        </template>
      </el-table-column>
      <el-table-column label="补货价格" prop="restockPrice" width="140" align="center">
        <template #default="{ row }">
          ￥{{ fenToYuan(row.restockPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="更新时间" prop="updateTime" width="180" align="center" />
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openDialog('update', row.id)"
            v-hasPermi="['mb:material-restock-price:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row)"
            v-hasPermi="['mb:material-restock-price:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="90px"
      v-loading="formLoading"
    >
      <el-form-item label="商品" prop="productId">
        <div class="spu-select-container">
          <el-input
            v-model="formProductName"
            placeholder="请选择商品"
            readonly
            class="!w-280px"
          >
            <template #suffix>
              <el-button type="primary" link size="small" @click="openFormSpuSelect">
                选择商品
              </el-button>
            </template>
          </el-input>
          <el-button
            v-if="formData.productId"
            type="danger"
            link
            size="small"
            class="clear-btn"
            @click="clearFormProduct"
          >
            清除
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="用户等级" prop="levelId">
        <MemberLevelSelect v-model="formData.levelId" />
      </el-form-item>
      <el-form-item label="补货价格" prop="restockPrice">
        <el-input
          v-model="formData.restockPrice"
          placeholder="请输入价格（元）"
          class="!w-200px"
        >
          <template #append>元</template>
        </el-input>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio-button :label="1">启用</el-radio-button>
          <el-radio-button :label="0">禁用</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
  <SpuSelect
    ref="querySpuSelectRef"
    :selectable-only="true"
    @confirm="onQuerySpuSelected"
  />
  <SpuSelect
    ref="formSpuSelectRef"
    :selectable-only="true"
    @confirm="onFormSpuSelected"
  />
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import MemberLevelSelect from '@/views/member/level/components/MemberLevelSelect.vue'
import type { MaterialRestockPriceVO } from '@/api/mb/materialRestockPrice'
import {
  createMaterialRestockPrice,
  deleteMaterialRestockPrice,
  exportMaterialRestockPrice,
  getMaterialRestockPrice,
  getMaterialRestockPricePage,
  updateMaterialRestockPrice
} from '@/api/mb/materialRestockPrice'
import { getSpu } from '@/api/mall/product/spu'
import { fenToYuan, yuanToFen } from '@/utils'
import { useMessage } from '@/hooks/web/useMessage'
import { SpuSelect } from '@/views/mall/promotion/components'

defineOptions({ name: 'MaterialRestockPrice' })

type DialogMode = 'create' | 'update'

const message = useMessage()

const loading = ref(false)
const list = ref<MaterialRestockPriceVO[]>([])
const total = ref(0)
const queryProductName = ref('')
const formProductName = ref('')
const querySpuSelectRef = ref<InstanceType<typeof SpuSelect> | null>(null)
const formSpuSelectRef = ref<InstanceType<typeof SpuSelect> | null>(null)
const queryFormRef = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  levelId: undefined as number | undefined,
  status: undefined as number | undefined
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<DialogMode>('create')
const formRef = ref()
const formLoading = ref(false)
const formData = reactive({
  id: undefined as number | undefined,
  productId: undefined as number | undefined,
  levelId: undefined as number | undefined,
  restockPrice: '' as string | number,
  status: 1
})

const formRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  levelId: [{ required: true, message: '请选择用户等级', trigger: 'change' }],
  restockPrice: [
    { required: true, message: '请输入补货价格', trigger: 'blur' },
    {
      validator: (_: any, value: any, callback: any) => {
        if (value === '' || value === null || value === undefined) {
          callback(new Error('请输入补货价格'))
          return
        }
        const num = Number(value)
        if (Number.isNaN(num) || num < 0) {
          callback(new Error('价格需为非负数字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getMaterialRestockPricePage(queryParams)
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryProductName.value = ''
  handleQuery()
}

const formatSpuLabel = (spuId?: number, name?: string) => {
  if (!spuId) return ''
  return name && name.trim().length > 0 ? name : `SPU#${spuId}`
}

const resolveSpuDisplayName = async (spuId: number, nameHint?: string) => {
  if (nameHint && nameHint.trim().length > 0) {
    return nameHint
  }
  try {
    const res = await getSpu(spuId)
    return formatSpuLabel(spuId, res?.name)
  } catch (error) {
    console.error('获取商品信息失败', error)
    return formatSpuLabel(spuId)
  }
}

const openQuerySpuSelect = () => {
  querySpuSelectRef.value?.open(
    queryParams.productId ? { spuId: queryParams.productId } : undefined
  )
}

const onQuerySpuSelected = async (spuId: number) => {
  queryParams.productId = spuId
  queryProductName.value = await resolveSpuDisplayName(spuId)
  queryParams.pageNo = 1
  await getList()
}

const clearQueryProduct = async () => {
  queryParams.productId = undefined
  queryProductName.value = ''
  queryParams.pageNo = 1
  await getList()
}

const openFormSpuSelect = () => {
  formSpuSelectRef.value?.open(
    formData.productId ? { spuId: formData.productId } : undefined
  )
}

const onFormSpuSelected = async (spuId: number) => {
  formData.productId = spuId
  formProductName.value = await resolveSpuDisplayName(spuId)
  await nextTick()
  formRef.value?.clearValidate('productId')
}

const clearFormProduct = async () => {
  formData.productId = undefined
  formProductName.value = ''
  await nextTick()
  formRef.value?.clearValidate('productId')
}

const openDialog = async (mode: DialogMode, id?: number) => {
  dialogMode.value = mode
  dialogTitle.value = mode === 'create' ? '新增补货价格' : '编辑补货价格'
  if (mode === 'create') {
    Object.assign(formData, {
      id: undefined,
      productId: undefined,
      levelId: undefined,
      restockPrice: '',
      status: 1
    })
    formProductName.value = ''
    dialogVisible.value = true
    await nextTick()
    formRef.value?.clearValidate()
    return
  }
  if (!id) return
  formLoading.value = true
  dialogVisible.value = true
  try {
    const res = await getMaterialRestockPrice(id)
    if (!res) {
      message.error('价格配置不存在或已删除')
      dialogVisible.value = false
      return
    }
    Object.assign(formData, {
      id: res.id,
      productId: res.productId,
      levelId: res.levelId,
      restockPrice: fenToYuan(res.restockPrice ?? 0),
      status: res.status ?? 1
    })
    formProductName.value = await resolveSpuDisplayName(res.productId!, res.productName)
    await nextTick()
    formRef.value?.clearValidate()
  } finally {
    formLoading.value = false
  }
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    formLoading.value = true
    try {
      const payload = {
        id: formData.id,
        productId: formData.productId!,
        levelId: formData.levelId!,
        restockPrice: yuanToFen(formData.restockPrice),
        status: formData.status
      }
      if (dialogMode.value === 'create') {
        await createMaterialRestockPrice(payload)
        message.success('新增成功')
      } else {
        await updateMaterialRestockPrice(payload)
        message.success('更新成功')
      }
      dialogVisible.value = false
      await getList()
    } finally {
      formLoading.value = false
    }
  })
}

const handleDelete = async (row: MaterialRestockPriceVO) => {
  await message.delConfirm('确认删除该价格配置吗？')
  await deleteMaterialRestockPrice(row.id!)
  message.success('删除成功')
  if (list.value.length === 1 && queryParams.pageNo > 1) {
    queryParams.pageNo -= 1
  }
  await getList()
}

const handleExport = async () => {
  await exportMaterialRestockPrice(queryParams as any)
  message.success('导出成功，正在下载中')
}

const formatLevel = (levelId?: number) => {
  if (!levelId) return '--'
  return `等级 ${levelId}`
}

onMounted(async () => {
  if (queryParams.productId) {
    queryProductName.value = await resolveSpuDisplayName(queryParams.productId)
  }
  await getList()
})
</script>

<style scoped lang="scss">
.spu-select-container {
  display: flex;
  align-items: center;
  gap: 8px;

  .clear-btn {
    flex-shrink: 0;
  }
}
</style>
