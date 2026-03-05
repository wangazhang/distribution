<template>
  <Dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="90%"
    :z-index="3000"
    :append-to-body="true"
    :destroy-on-close="false"
    class="brokerage-detail-dialog"
  >
    <!-- 数据统计卡片 -->
    <ContentWrap>
      <el-row :gutter="16" class="mb-4">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value text-success">¥{{ (statistics.totalIncome / 100).toFixed(2) }}</div>
              <div class="stat-label">总收入</div>
            </div>
            <el-icon class="stat-icon text-success"><Money /></el-icon>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value text-danger">¥{{ (statistics.totalExpense / 100).toFixed(2) }}</div>
              <div class="stat-label">总支出</div>
            </div>
            <el-icon class="stat-icon text-danger"><Minus /></el-icon>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value text-primary">{{ statistics.totalCount }}</div>
              <div class="stat-label">总笔数</div>
            </div>
            <el-icon class="stat-icon text-primary"><Document /></el-icon>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value text-warning">{{ statistics.pendingCount }}</div>
              <div class="stat-label">待结算</div>
            </div>
            <el-icon class="stat-icon text-warning"><Clock /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </ContentWrap>

    <!-- 搜索工作栏 -->
    <ContentWrap>
      <el-form
        class="-mb-15px search-form"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="业务类型" prop="bizType">
          <el-select
            v-model="queryParams.bizType"
            placeholder="请选择业务类型"
            clearable
            class="!w-240px"
            :popper-options="{ strategy: 'fixed' }"
            popper-class="brokerage-dialog-select-dropdown"
          >
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            class="!w-240px"
            :popper-options="{ strategy: 'fixed' }"
            popper-class="brokerage-dialog-select-dropdown"
          >
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.BROKERAGE_RECORD_STATUS)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间" prop="createTime">
          <el-date-picker
            v-model="queryParams.createTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
            class="!w-240px"
            :popper-options="{ strategy: 'fixed' }"
            popper-class="brokerage-dialog-date-picker"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon class="mr-5px" icon="ep:search" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 佣金记录列表 -->
    <ContentWrap>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <el-table-column label="编号" align="center" prop="id" min-width="60" />
        <el-table-column label="业务类型" align="center" prop="bizType" min-width="85">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE" :value="scope.row.bizType" />
          </template>
        </el-table-column>
        <el-table-column label="业务编号" align="center" prop="bizId" min-width="80" />
        <el-table-column label="标题" align="center" prop="title" min-width="110" />
        <el-table-column
          label="金额"
          align="center"
          prop="price"
          min-width="60"
          :formatter="fenToYuanFormat"
        />
        <el-table-column label="说明" align="center" prop="description" min-width="120" />
        <el-table-column label="状态" align="center" prop="status" min-width="85">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.BROKERAGE_RECORD_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column
          label="解冻时间"
          align="center"
          prop="unfreezeTime"
          :formatter="dateFormatter"
          width="180px"
        />
        <el-table-column
          label="创建时间"
          align="center"
          prop="createTime"
          :formatter="dateFormatter"
          width="180px"
        />
      </el-table>
      <!-- 分页 -->
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import { fenToYuanFormat } from '@/utils/formatter'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as BrokerageRecordApi from '@/api/mall/trade/brokerage/record'
import { Money, Minus, Document, Clock } from '@element-plus/icons-vue'

defineOptions({ name: 'BrokerageDetailDialog' })

// 响应式数据
const loading = ref(false)
const total = ref(0)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')

// 查询参数 - 参考record/index.vue简化
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: null as number | null,
  bizType: undefined as number | undefined,
  status: undefined as number | undefined,
  createTime: [] as string[]
})

// 统计数据
const statistics = reactive({
  totalIncome: 0,
  totalExpense: 0,
  totalCount: 0,
  pendingCount: 0,
  pendingAmount: 0,
  settledCount: 0,
  settledAmount: 0
})

/** 查询参数表单 */
const queryFormRef = ref()

/** 获取统计数据 */
const getStatistics = async () => {
  if (!queryParams.userId) return
  try {
    const statisticsData = await BrokerageRecordApi.getBrokerageStatistics(queryParams.userId)
    statistics.totalIncome = statisticsData.totalIncome || 0
    statistics.totalExpense = statisticsData.totalExpense || 0
    statistics.totalCount = statisticsData.totalCount || 0
    statistics.pendingCount = statisticsData.pendingCount || 0
    statistics.pendingAmount = statisticsData.pendingAmount || 0
    statistics.settledCount = statisticsData.settledCount || 0
    statistics.settledAmount = statisticsData.settledAmount || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
    statistics.totalIncome = 0
    statistics.totalExpense = 0
    statistics.totalCount = 0
    statistics.pendingCount = 0
    statistics.pendingAmount = 0
    statistics.settledCount = 0
    statistics.settledAmount = 0
  }
}

/** 查询列表 */
const getList = async () => {
  if (!queryParams.userId) return

  loading.value = true
  try {
    console.log('查询参数:', queryParams)
    const data = await BrokerageRecordApi.getBrokerageRecordPage(queryParams)
    console.log('API返回数据:', data)

    list.value = data.list || []
    total.value = data.total || 0

    statistics.totalCount = statistics.totalCount || data.total || 0
  } catch (error) {
    console.error('获取佣金记录失败:', error)
    list.value = []
    total.value = 0

    // 重置统计数据
    statistics.totalIncome = 0
    statistics.totalExpense = 0
    statistics.totalCount = 0
    statistics.pendingCount = 0
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
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  getList()
}

/** 打开弹窗 */
const open = async (userId: number, nickname: string) => {
  console.log('[佣金详情弹窗] open 调用参数:', { userId, nickname })
  dialogVisible.value = true
  dialogTitle.value = `${nickname}的佣金详情`
  queryParams.userId = userId

  // 重置查询条件
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1

  // 先获取统计数据，再加载列表数据
  await getStatistics()
  getList()
}

defineExpose({ open })
</script>

<style lang="scss" scoped>
.stat-card {
  position: relative;
  overflow: hidden;

  :deep(.el-card__body) {
    display: flex;
    padding: 20px;
    align-items: center;
    justify-content: space-between;
  }
}

.stat-content {
  flex: 1;
}

.stat-value {
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  line-height: 1;
  color: var(--el-text-color-regular);
}

.stat-icon {
  font-size: 32px;
  opacity: 0.8;
}

.text-success {
  color: var(--el-color-success);
}

.text-danger {
  color: var(--el-color-danger);
}

.text-primary {
  color: var(--el-color-primary);
}

.text-warning {
  color: var(--el-color-warning);
}

.text-muted {
  color: var(--el-text-color-placeholder);
}

/* 修复搜索栏组件被遮挡的问题 */
.search-form {
  position: relative;
  z-index: 1;
}

/* 确保下拉组件显示在最上层 */
:deep(.el-select__popper) {
  z-index: 4000 !important;
}

:deep(.el-picker__popper) {
  z-index: 4000 !important;
}

/* 为弹窗内的下拉组件设置更高的z-index */
:deep(.el-popper) {
  z-index: 4000 !important;
}

/* 确保表单项不会被遮挡 */
.el-form-item {
  position: relative;
  z-index: 1;
}

/* 修复下拉选项的显示问题 */
:deep(.el-select-dropdown) {
  z-index: 4000 !important;
}

:deep(.el-date-picker__popper) {
  z-index: 4000 !important;
}
</style>

<!-- 全局样式，确保弹窗内的下拉组件不被遮挡 -->
<style>
.brokerage-dialog-select-dropdown {
  z-index: 4000 !important;
}

.brokerage-dialog-date-picker {
  z-index: 4000 !important;
}

/* 确保所有Element Plus的弹出层都有足够高的z-index */
.el-popper.is-pure {
  z-index: 4000 !important;
}

.el-select-dropdown.el-popper {
  z-index: 4000 !important;
}

.el-picker-panel {
  z-index: 4000 !important;
}
</style>
