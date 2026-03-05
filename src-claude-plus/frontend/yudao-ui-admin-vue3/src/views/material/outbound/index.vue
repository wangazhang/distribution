<template>
  <ContentWrap>
    <!-- 搜索工作区 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="出库单号" prop="outboundNo">
        <el-input
          v-model="queryParams.outboundNo"
          placeholder="请输入出库单号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="用户昵称" prop="userNickname">
        <el-input
          v-model="queryParams.userNickname"
          placeholder="请输入用户昵称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="收货人" prop="receiverName">
        <el-input
          v-model="queryParams.receiverName"
          placeholder="请输入收货人姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option label="待审核" :value="0" />
          <el-option label="已审核" :value="1" />
          <el-option label="已发货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="审核拒绝" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['material:outbound:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增出库申请
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div class="mb-10px flex justify-between">
      <div class="flex">
        <el-button
          v-if="selectedRows.length > 0"
          type="primary"
          @click="handleBatchApprove"
          :loading="batchApproveLoading"
          v-hasPermi="['material:outbound:approve']"
        >
          <Icon icon="ep:check" class="mr-5px" /> 批量审核通过 ({{selectedRows.length}})
        </el-button>
        <el-button
          type="success"
          @click="handleBatchShipping"
          v-hasPermi="['material:outbound:ship']"
        >
          <Icon icon="ep:box" class="mr-5px" /> 批量发货
        </el-button>
      </div>
    </div>
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      highlight-current-row
      @current-change="handleCurrentChange"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="出库ID" align="center" prop="id" width="80" />
      <el-table-column label="出库单号" align="center" prop="outboundNo" width="140" />
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="用户昵称" align="center" prop="userNickname" width="120" />
      <el-table-column label="收货人信息" align="center" min-width="200">
        <template #default="scope">
          <div>{{ scope.row.receiverName }}</div>
          <div class="text-gray-500 text-sm">{{ scope.row.receiverMobile }}</div>
          <div class="text-gray-500 text-sm">
            {{ scope.row.receiverProvince }}{{ scope.row.receiverCity }}{{ scope.row.receiverDistrict }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="物流信息" align="center" width="140">
        <template #default="scope">
          <div v-if="scope.row.logisticsCompany">{{ scope.row.logisticsCompany }}</div>
          <div v-if="scope.row.logisticsCode" class="text-gray-500 text-sm">{{ scope.row.logisticsCode }}</div>
          <span v-if="!scope.row.logisticsCompany">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="primary">已审核</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="info">已发货</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 4" type="danger">已取消</el-tag>
          <el-tag v-else-if="scope.row.status === 5" type="danger">审核拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
          >
            详情
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="success"
            @click="handleApprove(scope.row.id, true)"
            v-hasPermi="['material:outbound:approve']"
          >
            审核通过
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="danger"
            @click="handleApprove(scope.row.id, false)"
            v-hasPermi="['material:outbound:approve']"
          >
            审核拒绝
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            link
            type="primary"
            @click="handleShip(scope.row.id)"
            v-hasPermi="['material:outbound:ship']"
          >
            发货
          </el-button>
          <el-button
            v-if="scope.row.status === 2"
            link
            type="success"
            @click="handleComplete(scope.row.id)"
            v-hasPermi="['material:outbound:complete']"
          >
            确认收货
          </el-button>
          <el-button
            v-if="scope.row.status < 3"
            link
            type="danger"
            @click="handleCancel(scope.row.id)"
            v-hasPermi="['material:outbound:cancel']"
          >
            取消
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

  <!-- 出库单明细 -->
  <ContentWrap v-if="currentRow && currentRow.id">
    <el-tabs model-value="materialItems">
      <el-tab-pane label="物料明细" name="materialItems">
        <MaterialOutboundItemList :outbound-id="String(currentRow.id)" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <OutboundForm ref="formRef" @success="getList" />

  <!-- 详情弹窗 -->
  <OutboundDetail ref="detailRef" />

  <!-- 发货弹窗 -->
  <OutboundShip ref="shipRef" @success="getList" />

  <!-- 取消弹窗 -->
  <OutboundCancel ref="cancelRef" @success="getList" />

  <!-- 批量发货导入弹窗 -->
  <ImportShippingDialog ref="importShippingDialogRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { OutboundApi } from '@/api/material/outbound/index'
import OutboundForm from './OutboundForm.vue'
import OutboundDetail from './OutboundDetail.vue'
import OutboundShip from './OutboundShip.vue'
import OutboundCancel from './OutboundCancel.vue'
import ImportShippingDialog from './ImportShippingDialog.vue'
import MaterialOutboundItemList from './components/MaterialOutboundItemList.vue'

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const selectedRows = ref([]) // 选中的行
const currentRow = ref<any>() // 当前选中行
const batchApproveLoading = ref(false) // 批量审核按钮加载状态
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  outboundNo: null,
  userId: null,
  userNickname: null,
  receiverName: null,
  status: null,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

/** 处理选择变化 */
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await OutboundApi.getMaterialOutboundPage(queryParams)
    list.value = data.list
    total.value = data.total

    // 如果有数据，默认选中第一行
    if (data.list && data.list.length > 0) {
      currentRow.value = data.list[0]
    } else {
      currentRow.value = undefined
    }
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
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 详情操作 */
const detailRef = ref()
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

/** 审核操作 */
const handleApprove = async (id: number, approved: boolean) => {
  try {
    const action = approved ? '通过' : '拒绝'
    await message.confirm(`确认${action}该出库申请吗？`)

    let reason = ''
    if (!approved) {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '拒绝原因不能为空'
          }
          return true
        }
      })
      reason = value
    }

    await OutboundApi.approveMaterialOutbound({
      id,
      approved,
      reason
    })
    message.success(`${action}成功`)
    await getList()
  } catch (e) {
    console.log(e)
  }
}

/** 发货操作 */
const shipRef = ref()
const handleShip = (id: number) => {
  shipRef.value.open(id)
}

/** 确认收货操作 */
const handleComplete = async (id: number) => {
  try {
    await message.confirm('确认该出库单已收货吗？')
    await OutboundApi.completeMaterialOutbound(id)
    message.success('确认收货成功')
    await getList()
  } catch (e) {
    console.log(e)
  }
}

/** 取消操作 */
const cancelRef = ref()
const handleCancel = (id: number) => {
  cancelRef.value.open(id)
}

/** 批量审核通过 */
const handleBatchApprove = async () => {
  if (selectedRows.value.length === 0) {
    message.warning('请至少选择一条记录')
    return
  }

  try {
    await message.confirm(`确认批量审核通过选中的 ${selectedRows.value.length} 条出库申请吗？`)
    batchApproveLoading.value = true

    const ids = selectedRows.value.map((row: any) => row.id)
    await OutboundApi.batchApproveMaterialOutbound({ ids })

    message.success('批量审核成功')
    selectedRows.value = []
    await getList()
  } catch (e) {
    console.log(e)
  } finally {
    batchApproveLoading.value = false
  }
}

/** 批量发货 */
const importShippingDialogRef = ref()
const handleBatchShipping = () => {
  importShippingDialogRef.value.open()
}

/** 选中行变更 */
const handleCurrentChange = (row: any) => {
  currentRow.value = row
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>