<template>
  <el-dialog :title="dialogTitle" v-model="dialogVisible" width="560px" append-to-body>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <el-form-item label="策略编码" prop="policyCode">
        <el-input v-model="formData.policyCode" :disabled="isUpdate" placeholder="请输入策略编码" />
      </el-form-item>
      <el-form-item label="展示名称" prop="displayName">
        <el-input v-model="formData.displayName" placeholder="请输入策略名称" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="formData.bizType" placeholder="请选择业务类型">
          <el-option
            v-for="item in bizTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="版本号" prop="versionNo">
        <el-input-number v-model="formData.versionNo" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
      <el-card shadow="never" class="scope-card">
        <template #header>
          <span>适用范围配置</span>
        </template>
        <div class="scope-card__grid">
          <el-form-item label="适用等级" prop="applicableLevel" class="scope-card__item">
            <div class="scope-field">
              <div class="scope-field__summary">
                <template v-if="levelPreview.length">
                  <el-tag
                    v-for="item in levelPreview"
                    :key="item.value"
                    size="small"
                    effect="plain"
                    closable
                    @close="removeLevel(item.value)"
                  >
                    {{ item.label }}
                  </el-tag>
                </template>
                <span v-else class="scope-field__placeholder">未限制</span>
              </div>
              <div class="scope-field__actions">
                <el-button type="primary" size="small" @click="openLevelDialog">选择等级</el-button>
                <el-button size="small" @click="handleClearLevel" :disabled="!levelPreview.length">
                  清除
                </el-button>
              </div>
              <p class="scope-card__hint">不选择表示对全部等级生效，可多选。</p>
            </div>
          </el-form-item>
          <el-form-item label="适用商品" prop="applicableProductIds" class="scope-card__item">
            <div class="scope-field">
              <div class="scope-field__summary">
                <template v-if="selectedProducts.length">
                  <el-tag
                    v-for="item in selectedProducts"
                    :key="item.id"
                    type="info"
                    size="small"
                    effect="plain"
                    closable
                    @close="removeProduct(item.id)"
                  >
                    {{ item.name ?? `SPU-${item.id}` }}（ID：{{ item.id }}）
                  </el-tag>
                </template>
                <span v-else class="scope-field__placeholder">未限制</span>
              </div>
              <div class="scope-field__actions">
                <el-button type="primary" size="small" @click="openProductDialog">选择商品</el-button>
                <el-button size="small" @click="handleClearProduct" :disabled="!selectedProducts.length">
                  清除
                </el-button>
              </div>
              <p class="scope-card__hint">留空默认匹配所有商品。</p>
            </div>
          </el-form-item>
          <el-form-item label="适用套包" prop="applicablePackageIds" class="scope-card__item">
            <div class="scope-field">
              <div class="scope-field__summary">
                <template v-if="selectedPackages.length">
                  <el-tag
                    v-for="item in selectedPackages"
                    :key="item.id"
                    type="info"
                    size="small"
                    effect="plain"
                    closable
                    @close="removePackageAndSync(item.id)"
                  >
                    {{ item.name ?? `套包-${item.id}` }}（ID：{{ item.id }}）
                  </el-tag>
                </template>
                <span v-else class="scope-field__placeholder">未限制</span>
              </div>
              <div class="scope-field__actions">
                <el-button type="primary" size="small" @click="openPackageDialog">选择套包</el-button>
                <el-button size="small" @click="handleClearPackage" :disabled="!selectedPackages.length">
                  清除
                </el-button>
              </div>
              <p class="scope-card__hint">留空默认匹配所有套包。</p>
            </div>
          </el-form-item>
        </div>
      </el-card>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="levelDialogVisible" title="选择适用等级" width="480px" append-to-body>
    <div v-if="levelOptions.length" class="level-dialog__body">
      <el-checkbox-group v-model="levelDialogSelection">
        <el-checkbox v-for="item in levelOptions" :key="item.value" :label="item.value">
          {{ item.label }}
        </el-checkbox>
      </el-checkbox-group>
    </div>
    <el-empty v-else description="暂无可选等级" />
    <template #footer>
      <el-button @click="levelDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirmLevelDialog">确 定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="packageDialogVisible" title="选择套包" width="720px" append-to-body>
    <div class="package-dialog__toolbar">
      <el-input
        v-model="packageQuery.name"
        placeholder="请输入套包名称"
        clearable
        class="package-dialog__search"
        @keyup.enter="loadPackageList"
      />
      <div class="package-dialog__actions">
        <el-button @click="loadPackageList">搜索</el-button>
        <el-button @click="resetPackageQuery">重置</el-button>
      </div>
    </div>
    <el-table
      ref="packageTableRef"
      v-loading="packageDialogLoading"
      :data="packageDialogList"
      row-key="id"
      height="360px"
      @selection-change="onPackageSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="100" align="center" />
      <el-table-column prop="name" label="套包名称" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else-if="row.status === 0" type="info">草稿</el-tag>
          <el-tag v-else type="warning">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="220" :show-overflow-tooltip="true" />
    </el-table>
    <div class="package-dialog__pagination">
      <el-pagination
        v-model:current-page="packagePagination.pageNo"
        v-model:page-size="packagePagination.pageSize"
        :total="packagePagination.total"
        layout="prev, pager, next, jumper, ->, total"
        background
        @size-change="handlePackagePageSizeChange"
        @current-change="handlePackagePageChange"
      />
    </div>
    <template #footer>
      <el-button @click="packageDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirmPackageDialog">确 定</el-button>
    </template>
  </el-dialog>

  <SpuTableSelect ref="spuTableSelectRef" :multiple="true" @change="onProductsSelected" />
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, nextTick, reactive, ref, watch } from 'vue'
import * as PolicyApi from '@/api/mb/commission/policy'
import * as ProductSpuApi from '@/api/mall/product/spu'
import * as ProductPackageApi from '@/api/mall/product/package'
import type { PackageRespVO } from '@/api/mall/product/package'
import * as MemberLevelApi from '@/api/member/level'
import SpuTableSelect from '@/views/mall/product/spu/components/SpuTableSelect.vue'

type ProductSpu = ProductSpuApi.Spu
type SelectOption = { label: string; value: string }

type CommissionPolicyFormModel = Omit<
  PolicyApi.CommissionPolicyVO,
  'applicableLevel' | 'applicableProductIds' | 'applicablePackageIds'
> & {
  applicableLevel?: string
  applicableProductIds: number[]
  applicablePackageIds: number[]
}

const bizTypeOptions = [
  { value: 'restock', label: '补货订单（restock）' },
  { value: 'mall_goods_order', label: '商城商品订单（mall_goods_order）' },
  { value: 'package_order', label: '套包订单（package_order）' }
]

const emits = defineEmits<{ (e: 'success'): void }>()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isUpdate = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<CommissionPolicyFormModel>({
  policyCode: '',
  displayName: '',
  bizType: bizTypeOptions[0].value,
  versionNo: 1,
  applicableLevel: '',
  applicableProductIds: [],
  applicablePackageIds: [],
  remark: undefined
})

const levelOptions = ref<SelectOption[]>([])
const levelLoading = ref(false)
const selectedLevelValues = ref<string[]>([])
const levelDialogVisible = ref(false)
const levelDialogSelection = ref<string[]>([])

const selectedProducts = ref<ProductSpu[]>([])
const selectedPackages = ref<PackageRespVO[]>([])
const dialogSelectedPackages = ref<PackageRespVO[]>([])

const spuTableSelectRef = ref<InstanceType<typeof SpuTableSelect> | null>(null)
const packageDialogVisible = ref(false)
const packageDialogLoading = ref(false)
const packageDialogList = ref<PackageRespVO[]>([])
const packageTableRef = ref<any>(null)
const packagePagination = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const packageQuery = reactive({ name: '' })

const formRules: FormRules = {
  policyCode: [{ required: true, message: '策略编码不能为空', trigger: 'blur' }],
  displayName: [{ required: true, message: '展示名称不能为空', trigger: 'blur' }],
  bizType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
  versionNo: [{ required: true, message: '版本号不能为空', trigger: 'blur' }]
}

const levelPreview = computed(() =>
  selectedLevelValues.value.map((value) => {
    const option = levelOptions.value.find((item) => item.value === value)
    return {
      value,
      label: option ? option.label : `等级值：${value}`
    }
  })
)

watch(selectedLevelValues, (values) => {
  formData.applicableLevel = values.join(',')
})

watch(
  selectedProducts,
  (list) => {
    formData.applicableProductIds = list
      .map((item) => item.id)
      .filter((id): id is number => id !== undefined && id !== null)
  },
  { deep: true }
)

watch(
  selectedPackages,
  (list) => {
    formData.applicablePackageIds = list
      .map((item) => item.id)
      .filter((id): id is number => id !== undefined && id !== null)
  },
  { deep: true }
)

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: undefined,
    policyCode: '',
    displayName: '',
    bizType: bizTypeOptions[0].value,
    versionNo: 1,
    applicableLevel: '',
    applicableProductIds: [],
    applicablePackageIds: [],
    remark: undefined
  })
  selectedLevelValues.value = []
  levelDialogSelection.value = []
  selectedProducts.value = []
  selectedPackages.value = []
  dialogSelectedPackages.value = []
  packagePagination.pageNo = 1
  packagePagination.pageSize = 10
  packagePagination.total = 0
  packageQuery.name = ''
}

const parseLevelValues = (value?: string | null) => {
  if (!value) {
    return []
  }
  return value
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
}

const ensureLevelOptions = async () => {
  if (levelOptions.value.length > 0 || levelLoading.value) {
    return
  }
  levelLoading.value = true
  try {
    const list = await MemberLevelApi.getLevelList({ status: 0 })
    levelOptions.value =
      list?.map((item: any) => {
        const levelValue = item.level ?? item.value ?? item.id
        const labelParts: string[] = []
        if (item.name) {
          labelParts.push(item.name)
        }
        if (levelValue !== undefined && levelValue !== null) {
          labelParts.push(`等级值：${levelValue}`)
        }
        if (item.id !== undefined && item.id !== null) {
          labelParts.push(`ID：${item.id}`)
        }
        return {
          label: labelParts.length > 0 ? labelParts.join(' ｜ ') : `等级 ${levelValue}`,
          value: String(levelValue)
        }
      }) ?? []
  } catch (error) {
    console.error('加载会员等级失败', error)
  } finally {
    levelLoading.value = false
  }
}

const openLevelDialog = async () => {
  await ensureLevelOptions()
  levelDialogSelection.value = [...selectedLevelValues.value]
  levelDialogVisible.value = true
}

const confirmLevelDialog = () => {
  selectedLevelValues.value = Array.from(new Set(levelDialogSelection.value))
  levelDialogVisible.value = false
}

const removeLevel = (value: string) => {
  selectedLevelValues.value = selectedLevelValues.value.filter((item) => item !== value)
  levelDialogSelection.value = levelDialogSelection.value.filter((item) => item !== value)
}

const handleClearLevel = () => {
  selectedLevelValues.value = []
  levelDialogSelection.value = []
}

const deduplicateSpuList = (list: ProductSpu[]) => {
  const map = new Map<number, ProductSpu>()
  list.forEach((item) => {
    if (item && item.id != null) {
      map.set(item.id, item)
    }
  })
  return Array.from(map.values())
}

const openProductDialog = () => {
  spuTableSelectRef.value?.open(selectedProducts.value)
}

const onProductsSelected = (spus: ProductSpu | ProductSpu[] | any) => {
  const array = Array.isArray(spus) ? spus : [spus]
  selectedProducts.value = deduplicateSpuList(array)
}

const removeProduct = (id?: number) => {
  if (id == null) return
  selectedProducts.value = selectedProducts.value.filter((item) => item.id !== id)
}

const handleClearProduct = () => {
  selectedProducts.value = []
}

const loadProductsByIds = async (ids: number[]) => {
  if (!ids || ids.length === 0) {
    selectedProducts.value = []
    return
  }
  try {
    const list = await ProductSpuApi.getSpuDetailList(ids)
    const map = new Map<number, ProductSpu>()
    list?.forEach((item) => {
      if (item.id != null) {
        map.set(item.id, item)
      }
    })
    const ordered = ids
      .map((id) => map.get(id) ?? ({ id, name: `SPU-${id}` } as ProductSpu))
      .filter((item) => item.id != null)
    selectedProducts.value = deduplicateSpuList(ordered)
  } catch (error) {
    console.error('加载商品信息失败', error)
    selectedProducts.value = ids.map((id) => ({ id, name: `SPU-${id}` } as ProductSpu))
  }
}

const removePackage = (id?: number) => {
  if (id == null) return
  selectedPackages.value = selectedPackages.value.filter((item) => item.id !== id)
  dialogSelectedPackages.value = dialogSelectedPackages.value.filter((item) => item.id !== id)
}

const handleClearPackage = () => {
  selectedPackages.value = []
  dialogSelectedPackages.value = []
  syncPackageSelection()
}

const syncPackageSelection = async () => {
  if (!packageDialogVisible.value) {
    return
  }
  await nextTick()
  const table = packageTableRef.value
  if (!table) return
  table.clearSelection()
  const selectedIds = new Set(dialogSelectedPackages.value.map((item) => item.id))
  packageDialogList.value.forEach((row) => {
    if (row.id != null && selectedIds.has(row.id)) {
      table.toggleRowSelection(row, true)
    }
  })
}

const loadPackagesByIds = async (ids: number[]) => {
  if (!ids || ids.length === 0) {
    selectedPackages.value = []
    dialogSelectedPackages.value = []
    return
  }
  const result: PackageRespVO[] = []
  for (const id of ids) {
    if (id == null) continue
    try {
      const pkg = await ProductPackageApi.getPackage(id)
      if (pkg) {
        result.push(pkg)
      } else {
        result.push({ id, name: `套包-${id}` } as PackageRespVO)
      }
    } catch (error) {
      console.error('加载套包信息失败', error)
      result.push({ id, name: `套包-${id}` } as PackageRespVO)
    }
  }
  selectedPackages.value = result.map((item) => ({ ...item }))
  dialogSelectedPackages.value = result.map((item) => ({ ...item }))
}

const openPackageDialog = async () => {
  packageDialogVisible.value = true
  dialogSelectedPackages.value = selectedPackages.value.map((item) => ({ ...item }))
  packagePagination.pageNo = 1
  await loadPackageList()
}

const loadPackageList = async () => {
  packageDialogLoading.value = true
  try {
    const page = await ProductPackageApi.getPackagePage({
      pageNo: packagePagination.pageNo,
      pageSize: packagePagination.pageSize,
      name: packageQuery.name || undefined
    })
    packageDialogList.value = page?.list ?? []
    packagePagination.total = page?.total ?? 0
    await syncPackageSelection()
  } catch (error) {
    console.error('加载套包列表失败', error)
  } finally {
    packageDialogLoading.value = false
  }
}

const onPackageSelectionChange = (selection: PackageRespVO[]) => {
  const currentIds = new Set(packageDialogList.value.map((item) => item.id))
  const retained = dialogSelectedPackages.value.filter((item) => !currentIds.has(item.id))
  const merged = [...retained]
  selection.forEach((item) => {
    if (item.id != null && !merged.some((pkg) => pkg.id === item.id)) {
      merged.push(item)
    }
  })
  dialogSelectedPackages.value = merged
}

const confirmPackageDialog = () => {
  selectedPackages.value = dialogSelectedPackages.value.map((item) => ({ ...item }))
  packageDialogVisible.value = false
}

const handlePackagePageChange = async (page: number) => {
  packagePagination.pageNo = page
  await loadPackageList()
}

const handlePackagePageSizeChange = async (size: number) => {
  packagePagination.pageSize = size
  packagePagination.pageNo = 1
  await loadPackageList()
}

const resetPackageQuery = async () => {
  packageQuery.name = ''
  packagePagination.pageNo = 1
  await loadPackageList()
}

const normalizeApplicableLevel = (value?: string) => {
  if (value == null) return null
  const trimmed = value.trim()
  return trimmed.length > 0 ? trimmed : null
}

const open = async (type: 'create' | 'update', id?: number) => {
  await ensureLevelOptions()
  resetForm()
  dialogVisible.value = true
  isUpdate.value = type === 'update'
  dialogTitle.value = type === 'create' ? '新增策略' : '编辑策略'
  if (type === 'update' && id) {
    const data = await PolicyApi.getCommissionPolicy(id)
    Object.assign(formData, {
      ...data,
      applicableLevel: data.applicableLevel ?? '',
      applicableProductIds: Array.isArray(data.applicableProductIds)
        ? data.applicableProductIds.filter((item): item is number => item !== null && item !== undefined)
        : [],
      applicablePackageIds: Array.isArray(data.applicablePackageIds)
        ? data.applicablePackageIds.filter((item): item is number => item !== null && item !== undefined)
        : []
    })
    await Promise.all([
      loadProductsByIds(formData.applicableProductIds),
      loadPackagesByIds(formData.applicablePackageIds)
    ])
  }
  selectedLevelValues.value = parseLevelValues(formData.applicableLevel)
  levelDialogSelection.value = [...selectedLevelValues.value]
}

defineExpose({ open })

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitLoading.value = true
  try {
    const payload: PolicyApi.CommissionPolicyVO = {
      ...formData,
      applicableLevel: normalizeApplicableLevel(formData.applicableLevel),
      applicableProductIds:
        formData.applicableProductIds.length > 0 ? [...formData.applicableProductIds] : [],
      applicablePackageIds:
        formData.applicablePackageIds.length > 0 ? [...formData.applicablePackageIds] : []
    }
    if (isUpdate.value) {
      await PolicyApi.updateCommissionPolicy(payload)
    } else {
      await PolicyApi.createCommissionPolicy(payload)
    }
    message.success(isUpdate.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    emits('success')
  } finally {
    submitLoading.value = false
  }
}

const removePackageAndSync = (id?: number) => {
  removePackage(id)
  syncPackageSelection()
}
</script>

<style scoped>
.scope-card {
  margin-top: 12px;
}

.scope-card__grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scope-card__item {
  margin-bottom: 0;
}

.scope-field {
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 8px;
}

.scope-field__summary {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 32px;
}

.scope-field__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.scope-field__placeholder {
  color: var(--el-text-color-placeholder);
}

.scope-card__hint {
  margin-top: 2px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--el-text-color-secondary);
}

.level-dialog__body {
  display: flex;
  max-height: 360px;
  padding: 8px;
  overflow-y: auto;
  flex-direction: column;
  gap: 8px;
}

.package-dialog__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.package-dialog__search {
  flex: 1;
}

.package-dialog__actions {
  display: flex;
  gap: 8px;
}

.package-dialog__pagination {
  display: flex;
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
