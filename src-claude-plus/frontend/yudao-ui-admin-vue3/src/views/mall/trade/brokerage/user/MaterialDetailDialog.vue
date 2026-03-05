<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="90%" :z-index="3000">
    <!-- 数据统计卡片 -->
    <ContentWrap>
      <el-row :gutter="16" class="mb-4">
        <el-col :xs="24" :sm="12" :md="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value text-primary">{{ statistics.totalMaterials }}</div>
              <div class="stat-label">物料种类</div>
            </div>
            <el-icon class="stat-icon text-primary"><Box /></el-icon>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value text-success">{{ formatBalance(statistics.totalBalance) }}</div>
              <div class="stat-label">总余额</div>
            </div>
            <el-icon class="stat-icon text-success"><Coin /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </ContentWrap>

    <!-- 搜索工作栏 -->
    <ContentWrap>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="物料名称" prop="materialName">
          <el-input
            v-model="queryParams.materialName"
            placeholder="请输入物料名称"
            clearable
            class="!w-240px"
            @keyup.enter="handleQuery"
          />
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

    <!-- 物料余额表格 -->
    <ContentWrap>
      <el-table
        v-loading="loading"
        :data="list"
        :show-overflow-tooltip="true"
        :stripe="true"
        empty-text="暂无物料余额记录"
        highlight-current-row
        @current-change="handleCurrentChange"
        class="material-table"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column align="center" label="物料编号" width="100" prop="id">
          <template #default="scope">
            <span class="material-id">#{{ scope.row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="物料名称" min-width="150" prop="materialName">
          <template #default="scope">
            <div class="material-name">
              <Icon icon="ep:box" class="mr-2 text-primary" />
              <span class="font-medium">{{ scope.row.materialName || '未知物料' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="物料余额" width="120" prop="availableBalance" sortable>
          <template #default="scope">
            <span class="balance-value font-medium">{{ formatBalance(scope.row.availableBalance) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :formatter="dateFormatter"
          align="center"
          label="更新时间"
          prop="updateTime"
          width="180"
          sortable
        />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <Pagination
          v-model:limit="queryParams.pageSize"
          v-model:page="queryParams.pageNo"
          :total="total"
          @pagination="getList"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
        />
      </div>
    </ContentWrap>

    <!-- 空数据状态 -->
    <ContentWrap v-if="!loading && list.length === 0 && !firstLoad">
      <el-empty
        description="暂无物料记录"
        :image-size="120"
        class="empty-state"
      >
        <el-button type="primary" @click="refreshData">
          <Icon icon="ep:refresh" class="mr-1" />
          刷新数据
        </el-button>
      </el-empty>
    </ContentWrap>

    <!-- 物料明细 -->
    <ContentWrap v-if="currentRow && currentRow.id">
      <el-tabs model-value="materialDetail">
        <el-tab-pane :label="`${currentRow.materialName} - 变动明细`" name="materialDetail">
          <!-- 明细搜索条件 -->
          <el-form
            ref="detailQueryFormRef"
            :inline="true"
            :model="detailQueryParams"
            class="search-form mb-4"
            label-width="85px"
          >
            <el-row :gutter="16">
              <el-col :span="6">
                <el-form-item label="操作类型" prop="actionType">
                  <el-select
                    v-model="detailQueryParams.actionType"
                    class="w-full"
                    clearable
                    placeholder="请选择操作类型"
                  >
                    <el-option label="增加" :value="1" />
                    <el-option label="减少" :value="-1" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="操作时间" prop="actionDate">
                  <el-date-picker
                    v-model="detailQueryParams.actionDate"
                    :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
                    class="w-full"
                    end-placeholder="结束日期"
                    start-placeholder="开始日期"
                    type="daterange"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <div class="flex items-center justify-end h-[32px]">
                  <el-button @click="handleDetailQuery">
                    <Icon class="mr-1" icon="ep:search" />
                    搜索
                  </el-button>
                  <el-button @click="resetDetailQuery">
                    <Icon class="mr-1" icon="ep:refresh" />
                    重置
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>

          <!-- 明细表格 -->
          <el-table
            v-loading="detailLoading"
            :data="detailList"
            :show-overflow-tooltip="true"
            :stripe="true"
            empty-text="暂无物料变动记录"
          >
            <el-table-column align="center" label="编号" width="80" prop="id" />
            <el-table-column align="center" label="操作类型" width="100" prop="direction">
              <template #default="scope">
                <el-tag :type="scope.row.direction === 1 ? 'success' : 'danger'">
                  {{ scope.row.direction === 1 ? '增加' : '减少' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" label="变动数量" width="100" prop="quantity">
              <template #default="scope">
                <span
                  :class="scope.row.direction === 1 ? 'text-success font-bold' : 'text-danger font-bold'"
                >
                  {{ scope.row.direction === 1 ? '+' : '-' }}{{ Math.abs(scope.row.quantity || 0) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column align="center" label="变动后余额" width="100" prop="balanceAfter">
              <template #default="scope">
                <span class="font-medium">{{ scope.row.balanceAfter || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column align="center" label="业务类型" min-width="120" prop="bizType">
              <template #default="scope">
                <el-tag :type="getBizTypeTagType(scope.row.bizType)">
                  {{ getBizTypeLabel(scope.row.bizType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" label="变更原因" min-width="180" prop="reason">
              <template #default="scope">
                <el-tooltip :content="scope.row.reason || '无说明'" placement="top">
                  <el-tag type="info" class="max-w-full truncate">
                    {{ scope.row.reason || '无说明' }}
                  </el-tag>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column
              :formatter="dateFormatter"
              align="center"
              label="操作时间"
              prop="createTime"
              width="180px"
            />
          </el-table>
          <!-- 明细分页 -->
          <Pagination
            v-model:limit="detailQueryParams.pageSize"
            v-model:page="detailQueryParams.pageNo"
            :total="detailTotal"
            @pagination="getDetailList"
          />
        </el-tab-pane>
      </el-tabs>
    </ContentWrap>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { MaterialBalanceApi } from '@/api/material/balance'
import { TxnApi } from '@/api/material/txn'
import { Box, Coin } from '@element-plus/icons-vue'

/** 物料详情弹窗 */
defineOptions({ name: 'MaterialDetailDialog' })

// 响应式数据
const loading = ref(true)
const firstLoad = ref(true)
const total = ref(0)
const list = ref([])
// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  userId: null as number | null,
  materialName: null as string | null,
  createTime: []
})

// 统计数据
const statistics = reactive({
  totalMaterials: 0,
  totalBalance: 0
})

const queryFormRef = ref()

// 当前选中的物料行
const currentRow = ref<any>(null)

// 物料明细相关
const detailLoading = ref(false)
const detailTotal = ref(0)
const detailList = ref([])
const detailQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: null as number | null,
  materialId: null as number | null,
  actionType: undefined as string | undefined,
  actionDate: []
})
const detailQueryFormRef = ref()

const currentUserId = ref<number | null>(null)
const currentUserNickname = ref('')

// 格式化余额显示
const formatBalance = (balance: number) => {
  if (balance === null || balance === undefined) return '0'
  return balance.toLocaleString('zh-CN')
}

// 获取业务类型标签
const getBizTypeLabel = (bizType: string) => {
  const bizTypeMap: Record<string, string> = {
    'PACKAGE_GRANT': '套包发放',
    'PACKAGE_REVOKE': '套包退款',
    'ORDER_CONSUME': '订单消耗',
    'MANUAL_ADJUST': '手动调整',
    'EXCHANGE_CONSUME': '兑换消耗',
    'EXCHANGE_GRANT': '兑换发放',
    'REFUND_REVOKE': '退款回退',
    'ACTIVITY_GRANT': '活动发放',
    'SIGN_IN_GRANT': '签到奖励'
  }
  return bizTypeMap[bizType] || bizType || '未知类型'
}

// 获取业务类型标签类型
const getBizTypeTagType = (bizType: string) => {
  const tagTypeMap: Record<string, string> = {
    'PACKAGE_GRANT': 'success',
    'PACKAGE_REVOKE': 'warning',
    'ORDER_CONSUME': 'danger',
    'MANUAL_ADJUST': 'info',
    'EXCHANGE_CONSUME': 'danger',
    'EXCHANGE_GRANT': 'success',
    'REFUND_REVOKE': 'warning',
    'ACTIVITY_GRANT': 'success',
    'SIGN_IN_GRANT': 'success'
  }
  return tagTypeMap[bizType] || 'info'
}



/** 刷新数据 */
const refreshData = () => {
  ElMessage.success('数据已刷新')
  getList()
}





/** 打开弹窗 */
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗标题
const open = async (userId: number, nickname: string) => {
  console.log('[物料详情弹窗] open 调用参数:', { userId, nickname })
  dialogVisible.value = true
  dialogTitle.value = `${nickname}的物料详情`
  currentUserId.value = userId
  currentUserNickname.value = nickname
  queryParams.userId = userId
  detailQueryParams.userId = userId
  // 清空之前的选中状态
  currentRow.value = null
  detailList.value = []
  resetQuery()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 查询物料余额列表 */
const getList = async () => {
  loading.value = true
  try {
    console.log('物料详情查询参数:', queryParams)
    const data = await MaterialBalanceApi.getUserBalancePage(queryParams)
    console.log('物料详情查询结果:', data)
    list.value = data.list || []
    total.value = data.total || 0

    // 计算统计数据
    calculateStatistics(data.list || [])

    // 设置首次加载标志
    firstLoad.value = false

    // 如果有数据，默认选中第一行
    if (data.list && data.list.length > 0) {
      currentRow.value = data.list[0]
      // 自动加载第一行的明细
      getDetailList()
    } else {
      currentRow.value = null
      detailList.value = []
    }
  } catch (error) {
    console.error('物料详情查询失败:', error)
    list.value = []
    total.value = 0
    currentRow.value = null
    detailList.value = []
  } finally {
    loading.value = false
  }
}

/** 计算统计数据 */
const calculateStatistics = (dataList: any[]) => {
  statistics.totalMaterials = dataList.length
  statistics.totalBalance = 0

  dataList.forEach(item => {
    const balance = item.availableBalance || 0
    statistics.totalBalance += balance
  })
}

/** 选中行变更 */
const handleCurrentChange = (row: any) => {
  currentRow.value = row
  if (row) {
    // 重置明细查询参数
    detailQueryParams.materialId = row.materialId
    detailQueryParams.pageNo = 1
    detailQueryParams.actionType = undefined
    detailQueryParams.actionDate = []
    // 加载明细数据
    getDetailList()
  } else {
    detailList.value = []
  }
}

/** 查询物料明细列表 */
const getDetailList = async () => {
  if (!currentRow.value) return

  detailLoading.value = true
  try {
    // 调整参数结构以匹配txn接口要求
    const params = {
      pageNo: detailQueryParams.pageNo,
      pageSize: detailQueryParams.pageSize,
      userId: detailQueryParams.userId,
      materialId: currentRow.value.materialId,
      direction: detailQueryParams.actionType  // 将actionType映射为direction
    }

    // 如果没有选择操作类型，则删除该参数
    if (params.direction === undefined || params.direction === null) {
      delete params.direction;
    }
    console.log('物料明细查询参数:', params)
    const data = await TxnApi.getTxnPage(params)
    console.log('物料明细查询结果:', data)
    detailList.value = data.list || []
    detailTotal.value = data.total || 0
  } catch (error) {
    console.error('物料明细查询失败:', error)
    detailList.value = []
    detailTotal.value = 0
  } finally {
    detailLoading.value = false
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
  handleQuery()
}

/** 明细搜索按钮操作 */
const handleDetailQuery = () => {
  detailQueryParams.pageNo = 1
  getDetailList()
}

/** 明细重置按钮操作 */
const resetDetailQuery = () => {
  detailQueryFormRef.value?.resetFields()
  detailQueryParams.actionType = undefined
  detailQueryParams.actionDate = []
  handleDetailQuery()
}
</script>

<style lang="scss" scoped>


// 响应式设计
@media (width <= 768px) {
  .stat-card {
    margin-bottom: 12px;
  }

  .search-form {
    padding: 16px;
  }

  .quick-filters {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .search-actions {
    justify-content: center;
  }

  .material-table {
    :deep(.el-table__body-wrapper) {
      overflow-x: auto;
    }
  }
}

@media (width <= 480px) {
  .stat-value {
    font-size: 20px;
  }

  .stat-icon {
    font-size: 24px;
  }

  .pagination-wrapper {
    :deep(.el-pagination) {
      justify-content: center;
      flex-wrap: wrap;
    }
  }
}

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

.text-info {
  color: var(--el-color-info);
}

.text-muted {
  color: var(--el-text-color-placeholder);
}

.search-form {
  padding: 20px;
  margin-bottom: 16px;
  background: var(--el-bg-color-page);
  border-radius: 8px;
}

.detail-search-form {
  padding: 16px;
  margin-bottom: 16px;
  background: var(--el-bg-color-page);
  border-radius: 8px;
}

.quick-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}

.search-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.material-table {
  :deep(.el-table__header) {
    background-color: var(--el-bg-color-page);
  }
}

.material-id {
  padding: 2px 6px;
  font-family: Monaco, Menlo, 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-bg-color-page);
  border-radius: 4px;
}

.material-name {
  display: flex;
  align-items: center;
}

.balance-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.empty-state {
  padding: 60px 20px;
}
</style>
