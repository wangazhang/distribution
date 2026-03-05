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
      <el-form-item label="转化ID" prop="id">
        <el-input-number
          v-model="queryParams.id"
          placeholder="请输入转化ID"
          controls-position="right"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input-number
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          controls-position="right"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="源物料" prop="sourceMaterialId">
        <el-input-number
          v-model="queryParams.sourceMaterialId"
          placeholder="请输入源物料ID"
          controls-position="right"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="目标物料" prop="targetMaterialId">
        <el-input-number
          v-model="queryParams.targetMaterialId"
          placeholder="请输入目标物料ID"
          controls-position="right"
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
          <el-option label="待支付" :value="0" />
          <el-option label="已完成" :value="1" />
          <el-option label="已取消" :value="2" />
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
          @click="openRuleManagement"
          v-hasPermi="['material:convert:rule']"
        >
          <Icon icon="ep:setting" class="mr-5px" /> 转化规则管理
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="转化ID" align="center" prop="id" width="80" />
      <el-table-column label="用户信息" align="center" min-width="120">
        <template #default="scope">
          <div>ID: {{ scope.row.userId }}</div>
          <div class="text-gray-500 text-sm">{{ scope.row.userNickname || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="源物料" align="center" min-width="150">
        <template #default="scope">
          <div>{{ scope.row.sourceMaterialName }} (ID: {{ scope.row.sourceMaterialId }})</div>
          <div class="text-gray-500 text-sm">数量: {{ scope.row.sourceQuantity }} {{ scope.row.sourceMaterialUnit }}</div>
        </template>
      </el-table-column>
      <el-table-column label="目标物料" align="center" min-width="150">
        <template #default="scope">
          <div>{{ scope.row.targetMaterialName }} (ID: {{ scope.row.targetMaterialId }})</div>
          <div class="text-gray-500 text-sm">数量: {{ scope.row.targetQuantity }} {{ scope.row.targetMaterialUnit }}</div>
        </template>
      </el-table-column>
      <el-table-column label="转化费用" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.convertFee && scope.row.convertFee > 0">
            ¥{{ (scope.row.convertFee / 100).toFixed(2) }}
          </span>
          <span v-else>免费</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.paymentType === 1" type="success">余额支付</el-tag>
          <el-tag v-else-if="scope.row.paymentType === 2" type="primary">微信支付</el-tag>
          <el-tag v-else-if="scope.row.paymentType === 3" type="warning">支付宝</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待支付</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="150" fixed="right">
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
            type="danger"
            @click="handleCancel(scope.row.id)"
            v-hasPermi="['material:convert:cancel']"
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

  <!-- 详情弹窗 -->
  <ConvertDetail ref="detailRef" />
  
  <!-- 转化规则管理弹窗 -->
  <ConvertRuleManagement ref="ruleRef" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as ConvertApi from '@/api/material/convert'
import ConvertDetail from './ConvertDetail.vue'
import ConvertRuleManagement from './ConvertRuleManagement.vue'

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: null,
  userId: null,
  sourceMaterialId: null,
  targetMaterialId: null,
  status: null,
  createTime: []
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ConvertApi.getConvertPage(queryParams)
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

/** 详情操作 */
const detailRef = ref()
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

/** 转化规则管理 */
const ruleRef = ref()
const openRuleManagement = () => {
  ruleRef.value.open()
}

/** 取消操作 */
const handleCancel = async (id: number) => {
  try {
    await message.confirm('确认取消该转化申请吗？')
    await ConvertApi.cancelConvert(id)
    message.success('取消成功')
    await getList()
  } catch (e) {
    console.log(e)
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>