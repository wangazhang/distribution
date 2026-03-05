<template>
  <Dialog v-model="visible" :title="dialogTitle" width="900px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="套包名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入套包名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="formData.status" placeholder="请选择">
              <el-option label="草稿" :value="0" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="关联SPU" prop="spuId">
            <div class="spu-select-group">
              <el-input
                v-model="selectedPackageSpuName"
                class="spu-select-input"
                placeholder="请选择关联商品"
                readonly
              >
                <template #suffix>
                  <el-button type="primary" link @click="openSpuSelectForPackage">选择商品</el-button>
                </template>
              </el-input>
              <el-button v-if="formData.spuId" type="danger" link @click="clearPackageSpu">清除</el-button>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 物料配置 -->
      <el-divider>物料配置</el-divider>
      <el-button type="primary" link @click="addItemRow"><Icon icon="ep:plus" class="mr-5px" /> 添加物料</el-button>
      <el-table :data="formData.items" class="mt-10px" border>
        <el-table-column label="#" type="index" width="50" />
        <el-table-column label="类型" width="140">
          <template #default="{ row }">
            <el-select v-model="row.itemType" placeholder="类型" @change="onItemTypeChange(row)">
              <el-option label="商品" :value="1" />
              <el-option label="权益" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="标识/商品">
          <template #default="{ row }">
            <!-- 商品类：选择 SPU -->
            <div v-if="row.itemType === 1" class="spu-select-group">
              <el-input
                v-model="row.itemName"
                class="spu-select-input"
                placeholder="请选择商品"
                readonly
              >
                <template #suffix>
                  <el-button type="primary" link @click="openSpuSelectForItem(row)">选择商品</el-button>
                </template>
              </el-input>
              <el-button v-if="row.itemId" type="danger" link @click="clearItemSpu(row)">清除</el-button>
            </div>
            <!-- 权益类：选择权益编码 -->
            <el-select v-else v-model="row.itemId" placeholder="选择权益">
              <el-option label="开通分销资格" :value="1" />
              <el-option label="升级会员等级" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.itemQuantity" :min="1" />
          </template>
        </el-table-column>
        <el-table-column label="扩展参数">
          <template #default="{ row }">
            <!-- SET_LEVEL 需要 levelId 下拉 -->
            <div v-if="row.itemType === 2 && row.itemId === 2" class="flex items-center">
              <span class="mr-8px">等级：</span>
              <el-select v-model="row.extJson.levelId" placeholder="请选择等级" style="width: 220px">
                <el-option v-for="lv in levelOptions" :key="lv.id" :label="lv.name + '（' + lv.id + '）'" :value="lv.id" />
              </el-select>
            </div>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeItemRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分佣配置 -->
      <el-divider>分佣配置</el-divider>
      <el-button type="primary" link @click="addCommissionRow"><Icon icon="ep:plus" class="mr-5px" /> 添加分佣规则</el-button>
      <el-table :data="formData.commissions" class="mt-10px" border>
        <el-table-column label="#" type="index" width="50" />
        <el-table-column label="层级" width="120">
          <template #default="{ row }">
            <el-select v-model="row.level" placeholder="层级">
              <el-option label="上级(L1)" :value="1" />
              <el-option label="上上级(L2)" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="150">
          <template #default="{ row }">
            <el-select v-model="row.commissionType" placeholder="类型">
              <el-option label="固定金额(分)" :value="1" />
              <el-option label="固定比例(%)" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数值">
          <template #default="{ row }">
            <el-input v-model.number="row.commissionValue" placeholder="金额(分)或比例(%)" />
          </template>
        </el-table-column>
        <el-table-column label="基准类型" width="150">
          <template #default="{ row }">
            <el-select v-model="row.baseType" placeholder="基准类型">
              <el-option label="订单项实付" :value="1" />
              <el-option label="自定义基准" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="基准价(分)" width="220">
          <template #default="{ row }">
            <el-input v-model.number="row.baseAmount" :disabled="row.baseType !== 2" placeholder="自定义基准时必填" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeCommissionRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
  <SpuSelect
    ref="spuSelectRef"
    :selectable-only="spuSelectSelectableOnly"
    @confirm="handleSpuSelected"
  />
</template>

<script setup lang="ts">
import { reactive, ref, toRaw, computed } from 'vue'
import { createPackage, updatePackage, getPackage } from '@/api/mall/product/package'
import { getSpu, getSpuDetailList } from '@/api/mall/product/spu'
import { getSimpleLevelList } from '@/api/member/level'
import { useMessage } from '@/hooks/web/useMessage'
import { SpuSelect } from '@/views/mall/promotion/components'

const message = useMessage()

const visible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()

const levelOptions = ref<any[]>([])
const selectedPackageSpuName = ref('')
const spuSelectRef = ref<InstanceType<typeof SpuSelect> | null>(null)
const spuSelectContext = ref<{ type: 'package' | 'item'; row?: any } | null>(null)
const spuSelectSelectableOnly = computed(() => spuSelectContext.value?.type === 'item')

const emptyForm = () => ({
  id: undefined,
  name: '',
  spuId: undefined as any,
  status: 0,
  remark: '',
  items: [] as any[],
  commissions: [] as any[]
})

const formData = reactive<any>(emptyForm())

const formRules = {
  name: [{ required: true, message: '请输入套包名称', trigger: 'blur' }],
  spuId: [{ required: true, message: '请选择关联SPU', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const normalizeSpuId = (value: unknown): number | undefined => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const num = Number(value)
  return Number.isNaN(num) ? undefined : num
}

const addItemRow = () => {
  formData.items.push({
    itemType: 1,
    itemId: undefined,
    itemName: '',
    itemQuantity: 1,
    extJson: {}
  })
}
const removeItemRow = (idx: number) => {
  formData.items.splice(idx, 1)
}
const onItemTypeChange = (row: any) => {
  row.itemId = undefined
  row.itemName = ''
  row.extJson = {}
}

const addCommissionRow = () => {
  formData.commissions.push({ level: 1, commissionType: 2, commissionValue: 10.0, baseType: 1, baseAmount: 0 })
}
const removeCommissionRow = (idx: number) => {
  formData.commissions.splice(idx, 1)
}

const openSpuSelectForPackage = () => {
  spuSelectContext.value = { type: 'package' }
  const spuId = normalizeSpuId(formData.spuId)
  spuSelectRef.value?.open({ spuId })
}
const openSpuSelectForItem = (row: any) => {
  spuSelectContext.value = { type: 'item', row }
  const spuId = normalizeSpuId(row.itemId)
  spuSelectRef.value?.open({ spuId })
}
const clearPackageSpu = () => {
  formData.spuId = undefined
  selectedPackageSpuName.value = ''
  formRef.value?.validateField?.('spuId')
}
const clearItemSpu = (row: any) => {
  row.itemId = undefined
  row.itemName = ''
}

const handleSpuSelected = async (spuId: number) => {
  if (!spuSelectContext.value) {
    return
  }
  const assignSpuInfo = (name: string) => {
    if (spuSelectContext.value?.type === 'package') {
      formData.spuId = spuId
      selectedPackageSpuName.value = name
      formRef.value?.validateField?.('spuId')
    } else if (spuSelectContext.value?.type === 'item' && spuSelectContext.value.row) {
      spuSelectContext.value.row.itemId = spuId
      spuSelectContext.value.row.itemName = name
    }
  }
  try {
    const spu = await getSpu(spuId)
    assignSpuInfo(spu?.name ?? `商品ID: ${spuId}`)
  } catch (error) {
    console.error('加载商品信息失败', error)
    assignSpuInfo(`商品ID: ${spuId}`)
  } finally {
    spuSelectContext.value = null
  }
}

const open = async (type: 'create' | 'update', id?: number) => {
  visible.value = true
  dialogTitle.value = type === 'create' ? '新增套包' : '编辑套包'
  Object.assign(formData, emptyForm())
  selectedPackageSpuName.value = ''
  spuSelectContext.value = null
  await loadInitialOptions()
  if (type === 'update' && id) {
    const pkg = await getPackage(id)
    Object.assign(formData, pkg)
    if (!Array.isArray(formData.items)) {
      formData.items = []
    }
    formData.items.forEach((it: any) => {
      it.extJson = it.extJson || {}
      it.itemName = ''
    })
    await loadSelectedSpus()
  }
}

const loadInitialOptions = async () => {
  const levels = await getSimpleLevelList()
  levelOptions.value = levels || []
}

const loadSelectedSpus = async () => {
  const spuIdsToLoad = new Set<number>()
  if (formData.spuId) {
    spuIdsToLoad.add(formData.spuId)
  }
  formData.items.forEach((item: any) => {
    if (item.itemType === 1 && item.itemId) {
      spuIdsToLoad.add(item.itemId)
    }
  })

  if (spuIdsToLoad.size === 0) {
    selectedPackageSpuName.value = ''
    formData.items.forEach((item: any) => {
      if (item.itemType === 1) {
        item.itemName = ''
      }
    })
    return
  }

  try {
    const spuList = await getSpuDetailList(Array.from(spuIdsToLoad))
    const loadedSpus = spuList || []
    const nameMap = new Map<number, string>()
    loadedSpus.forEach((spu: any) => {
      if (spu?.id) {
        nameMap.set(spu.id, spu.name)
      }
    })

    if (formData.spuId) {
      selectedPackageSpuName.value = nameMap.get(formData.spuId) ?? `商品ID: ${formData.spuId}`
    }
    formData.items.forEach((item: any) => {
      if (item.itemType === 1 && item.itemId) {
        item.itemName = nameMap.get(item.itemId) ?? `商品ID: ${item.itemId}`
      } else if (item.itemType === 1) {
        item.itemName = ''
      }
    })
  } catch (error) {
    console.error('加载商品信息失败', error)
  }
}

const buildSubmitData = () => {
  const rawForm = toRaw(formData)
  return {
    ...rawForm,
    items: rawForm.items.map((item: any) => {
      const { itemName, ...rest } = item
      return { ...rest }
    })
  }
}

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    const submitData = buildSubmitData()
    if (submitData.id) {
      await updatePackage(submitData)
    } else {
      await createPackage(submitData)
    }
    message.success('保存成功')
    visible.value = false
    emit('success')
  } finally {
    submitLoading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.mt-10px {
  margin-top: 10px;
}

.spu-select-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spu-select-input {
  flex: 1;
}
</style>
