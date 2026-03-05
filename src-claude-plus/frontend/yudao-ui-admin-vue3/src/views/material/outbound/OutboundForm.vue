<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="申请用户" prop="userId">
            <el-select
              v-model="formData.userId"
              placeholder="请选择用户"
              filterable
              remote
              reserve-keyword
              :remote-method="searchUsers"
              :loading="userSearchLoading"
              @change="handleUserChange"
              style="width: 100%"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="`${user.nickname || user.name || '未命名'} (${user.mobile || user.id})`"
                :value="user.id"
              >
                <span>{{ user.nickname || user.name || '未命名' }}</span>
                <span style=" margin-left: 8px;color: var(--el-text-color-secondary)">
                  {{ user.mobile || user.id }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收货地址" prop="addressId">
            <el-select
              v-model="formData.addressId"
              placeholder="请选择收货地址"
              filterable
              clearable
              :disabled="!formData.userId"
              @change="handleAddressChange"
              style="width: 100%"
            >
              <el-option
                v-for="addr in addressList"
                :key="addr.id"
                :label="`${addr.name} ${addr.mobile} - ${getAddressText(addr)}`"
                :value="addr.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">收货人信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="收货人姓名" prop="receiverName">
            <el-input v-model="formData.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="receiverMobile">
            <el-input v-model="formData.receiverMobile" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="省/市/区" prop="areaCode">
            <el-cascader
              v-model="formData.areaCode"
              :options="areaOptions"
              :props="areaProps"
              placeholder="请选择省市区"
              @change="handleAreaChange"
              style="width: 100%"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="详细地址" prop="receiverDetailAddress">
            <el-input v-model="formData.receiverDetailAddress" placeholder="请输入详细地址" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">出库物料</el-divider>

      <!-- 用户物料余额展示 -->
      <el-alert
        v-if="formData.userId && userMaterialBalances.length > 0"
        type="info"
        :closable="false"
        class="mb-3"
      >
        <template #title>
          <div class="flex items-center justify-between">
            <span>用户持有物料</span>
            <el-button
              link
              type="primary"
              @click="refreshUserMaterialBalances"
              :loading="balanceLoading"
            >
              <Icon icon="ep:refresh" class="mr-5px" /> 刷新
            </el-button>
          </div>
        </template>
        <el-scrollbar max-height="120px">
          <div class="grid grid-cols-3 gap-2">
            <div
              v-for="balance in userMaterialBalances"
              :key="balance.materialId"
              class="text-sm"
            >
              <span class="font-medium">{{ balance.materialName }}</span>:
              <span class="text-primary">{{ balance.balance }}</span>
              <span class="text-gray-500 ml-1">{{ balance.unit || '个' }}</span>
            </div>
          </div>
        </el-scrollbar>
      </el-alert>

      <el-form-item label="出库明细" prop="items">
        <el-table :data="formData.items" border style="width: 100%">
          <el-table-column label="物料" min-width="200">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.materialId"
                placeholder="请选择物料"
                filterable
                remote
                reserve-keyword
                :remote-method="searchMaterials"
                :loading="materialSearchLoading"
                @change="handleMaterialSelectChange($index)"
                style="width: 100%"
              >
                <el-option
                  v-for="material in materialList"
                  :key="material.id"
                  :label="`${material.name} (${material.code})`"
                  :value="material.id"
                >
                  <div style="display: flex; justify-content: space-between">
                    <span>{{ material.name }}</span>
                    <span style="color: var(--el-text-color-secondary)">{{ material.code }}</span>
                  </div>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="100">
            <template #default="{ row }">
              <span>{{ row.baseUnit || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可用余额" width="100">
            <template #default="{ row }">
              <span v-if="row.materialId" :class="{ 'text-danger': getAvailableBalance(row.materialId) < row.quantity }">
                {{ getAvailableBalance(row.materialId) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="出库数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                placeholder="数量"
                :min="1"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeItem($index)">
                <Icon icon="ep:delete" />
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button
          type="primary"
          plain
          @click="addItem"
          class="mt-2"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 添加物料
        </el-button>
      </el-form-item>
      
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          placeholder="请输入备注"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OutboundApi } from '@/api/material/outbound/index'
import { DefinitionApi } from '@/api/material/definition'
import { getUserListByNickname } from '@/api/member/user'
import { getAddressList } from '@/api/member/address'
import { getAreaTree } from '@/utils/area'
import { MaterialBalanceApi } from '@/api/material/balance'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型：create - 新增；update - 修改

// 用户选择相关
const userList = ref([])
const userSearchLoading = ref(false)

// 地址选择相关
const addressList = ref<any[]>([])

// 物料选择相关
const materialList = ref([])
const materialSearchLoading = ref(false)

// 用户物料余额
const userMaterialBalances = ref<any[]>([])
const balanceLoading = ref(false)

// 省市区数据
const areaOptions = ref<any[]>([])
const areaProps = {
  value: 'code',
  label: 'name',
  children: 'children'
}

const formData = ref({
  id: undefined,
  userId: undefined,
  addressId: undefined,
  receiverName: undefined,
  receiverMobile: undefined,
  areaCode: [] as string[],
  receiverProvince: undefined as string | undefined,
  receiverCity: undefined as string | undefined,
  receiverDistrict: undefined as string | undefined,
  receiverDetailAddress: undefined,
  remark: undefined,
  items: [
    {
      materialId: undefined,
      materialName: '',
      baseUnit: '',
      quantity: 1
    }
  ]
})

const formRules = reactive({
  userId: [{ required: true, message: '申请用户不能为空', trigger: 'change' }],
  receiverName: [{ required: true, message: '收货人姓名不能为空', trigger: 'blur' }],
  receiverMobile: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }],
  areaCode: [{ required: true, message: '请选择省市区', trigger: 'change' }],
  receiverDetailAddress: [{ required: true, message: '详细地址不能为空', trigger: 'blur' }],
  items: [
    {
      validator: (rule: any, value: any, callback: any) => {
        if (!value || value.length === 0) {
          callback(new Error('至少需要添加一个物料'))
          return
        }
        for (let i = 0; i < value.length; i++) {
          const item = value[i]
          if (!item.materialId) {
            callback(new Error(`第${i + 1}行物料ID不能为空`))
            return
          }
          if (!item.quantity || item.quantity <= 0) {
            callback(new Error(`第${i + 1}行数量必须大于0`))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
})

const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  // 加载省市区数据
  loadAreaData()

  // 修改时,设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await OutboundApi.getMaterialOutbound(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法,用于打开弹窗

/** 加载省市区数据 */
const loadAreaData = async () => {
  try {
    const data = await getAreaTree()
    console.log('省市区数据加载成功:', data)
    areaOptions.value = data
  } catch (e) {
    console.error('加载省市区数据失败', e)
    areaOptions.value = []
  }
}

/** 搜索用户 */
const searchUsers = async (query: string) => {
  if (!query) {
    userList.value = []
    return
  }

  userSearchLoading.value = true
  try {
    const data = await getUserListByNickname(query)
    userList.value = data || []
  } catch (e) {
    console.error('搜索用户失败', e)
    userList.value = []
  } finally {
    userSearchLoading.value = false
  }
}

/** 用户变更 - 加载地址列表和物料余额 */
const handleUserChange = async (userId: number) => {
  if (!userId) {
    addressList.value = []
    formData.value.addressId = undefined
    userMaterialBalances.value = []
    return
  }

  try {
    // 加载地址列表
    const data = await getAddressList({ userId })
    addressList.value = data || []

    // 加载用户物料余额
    await loadUserMaterialBalances(userId)
  } catch (e) {
    console.error('加载用户数据失败', e)
    addressList.value = []
    userMaterialBalances.value = []
  }
}

/** 加载用户物料余额 */
const loadUserMaterialBalances = async (userId: number) => {
  balanceLoading.value = true
  try {
    const result = await MaterialBalanceApi.getUserMaterials(userId)
    userMaterialBalances.value = result.list || []
    console.log('用户物料余额:', userMaterialBalances.value)
  } catch (e) {
    console.error('加载用户物料余额失败', e)
    userMaterialBalances.value = []
  } finally {
    balanceLoading.value = false
  }
}

/** 刷新用户物料余额 */
const refreshUserMaterialBalances = async () => {
  if (!formData.value.userId) {
    message.warning('请先选择用户')
    return
  }
  await loadUserMaterialBalances(formData.value.userId)
  message.success('刷新成功')
}

/** 获取物料的可用余额 */
const getAvailableBalance = (materialId: number) => {
  if (!materialId || !userMaterialBalances.value.length) return 0
  const balance = userMaterialBalances.value.find(b => b.materialId === materialId)
  return balance ? balance.balance : 0
}

/** 校验物料余额是否充足 */
const validateMaterialBalances = (): boolean => {
  for (const item of formData.value.items) {
    if (item.materialId) {
      const availableBalance = getAvailableBalance(item.materialId)
      if (item.quantity > availableBalance) {
        message.error(`物料"${item.materialName}"出库数量(${item.quantity})超过可用余额(${availableBalance})`)
        return false
      }
    }
  }
  return true
}

/** 地址变更 - 自动填充收货信息 */
const handleAddressChange = (addressId: number) => {
  if (!addressId) return

  const address = addressList.value.find(a => a.id === addressId)
  if (address) {
    formData.value.receiverName = address.name
    formData.value.receiverMobile = address.mobile
    formData.value.receiverDetailAddress = address.detailAddress

    // 根据 areaId 查找对应的省市区路径
    if (address.areaId) {
      const areaPath = findAreaPath(areaOptions.value, String(address.areaId))
      if (areaPath && areaPath.length === 3) {
        formData.value.areaCode = areaPath
        // 触发省市区变更,提取名称
        handleAreaChange(areaPath)
      }
    }
  }
}

/** 根据区域ID查找省市区路径 */
const findAreaPath = (areas: any[], targetCode: string, currentPath: string[] = []): string[] | null => {
  for (const area of areas) {
    const newPath = [...currentPath, area.code]

    // 如果找到目标code
    if (area.code === targetCode) {
      return newPath
    }

    // 递归查找子节点
    if (area.children && area.children.length > 0) {
      const result = findAreaPath(area.children, targetCode, newPath)
      if (result) {
        return result
      }
    }
  }
  return null
}

/** 获取地址文本 */
const getAddressText = (address: any) => {
  return address.detailAddress || ''
}

/** 省市区变更 */
const handleAreaChange = (value: any) => {
  if (value && value.length === 3) {
    // 从级联选择器的值中提取省市区名称
    const findAreaName = (options: any[], code: string): string => {
      for (const opt of options) {
        if (opt.code === code) return opt.name
        if (opt.children) {
          const name = findAreaName(opt.children, code)
          if (name) return name
        }
      }
      return ''
    }

    formData.value.receiverProvince = findAreaName(areaOptions.value, value[0])
    formData.value.receiverCity = findAreaName(areaOptions.value, value[1])
    formData.value.receiverDistrict = findAreaName(areaOptions.value, value[2])
  }
}

/** 搜索物料 */
const searchMaterials = async (query: string) => {
  if (!query) {
    materialList.value = []
    return
  }

  materialSearchLoading.value = true
  try {
    const data = await DefinitionApi.getDefinitionPage({
      name: query,
      status: 1,
      pageNo: 1,
      pageSize: 20
    })
    materialList.value = data.list || []
  } catch (e) {
    console.error('搜索物料失败', e)
    materialList.value = []
  } finally {
    materialSearchLoading.value = false
  }
}

/** 物料选择变更 */
const handleMaterialSelectChange = async (index: number) => {
  const item = formData.value.items[index]
  if (item.materialId) {
    try {
      const material = await DefinitionApi.getDefinition(item.materialId)
      item.materialName = material.name
      item.baseUnit = material.baseUnit
    } catch (e) {
      console.error('获取物料信息失败:', e)
      item.materialName = ''
      item.baseUnit = ''
    }
  } else {
    item.materialName = ''
    item.baseUnit = ''
  }
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 校验物料余额
  if (!validateMaterialBalances()) {
    return
  }

  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as any
    if (formType.value === 'create') {
      await OutboundApi.createMaterialOutbound(data)
      message.success(t('common.createSuccess'))
    } else {
      await OutboundApi.updateMaterialOutbound(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 添加物料明细 */
const addItem = () => {
  formData.value.items.push({
    materialId: undefined,
    materialName: '',
    baseUnit: '',
    quantity: 1
  })
}

/** 删除物料明细 */
const removeItem = (index: number) => {
  if (formData.value.items.length > 1) {
    formData.value.items.splice(index, 1)
  } else {
    message.warning('至少需要保留一个物料')
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    userId: undefined,
    addressId: undefined,
    receiverName: undefined,
    receiverMobile: undefined,
    areaCode: [] as string[],
    receiverProvince: undefined as string | undefined,
    receiverCity: undefined as string | undefined,
    receiverDistrict: undefined as string | undefined,
    receiverDetailAddress: undefined,
    remark: undefined,
    items: [
      {
        materialId: undefined,
        materialName: '',
        baseUnit: '',
        quantity: 1
      }
    ]
  }
  userList.value = []
  addressList.value = []
  materialList.value = []
  formRef.value?.resetFields()
}

// 组件挂载时预加载省市区数据
onMounted(() => {
  loadAreaData()
})
</script>
