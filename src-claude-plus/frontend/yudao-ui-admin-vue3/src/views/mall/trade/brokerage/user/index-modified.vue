<template>
  <doc-alert title="【交易】分销返佣" url="https://doc.example.com/mall/trade-brokerage/" />

  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="85px"
    >
      <el-form-item label="推广员昵称" prop="bindUserId">
        <el-select
          v-model="queryParams.bindUserId"
          placeholder="请输入推广员昵称搜索"
          filterable
          remote
          reserve-keyword
          clearable
          :remote-method="searchUsers"
          :loading="userLoading"
          @change="handleUserChange"
          class="!w-240px"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.value"
            :label="user.label"
            :value="user.value"
          >
            <div class="flex items-center">
              <el-avatar :src="user.avatar" :size="20" class="mr-2" />
              <span>{{ user.nickname }}</span>
              <span class="text-gray-400 ml-2">(ID: {{ user.value }})</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="用户昵称" prop="userId">
        <el-select
          v-model="queryParams.userId"
          placeholder="请输入用户昵称搜索"
          filterable
          remote
          reserve-keyword
          clearable
          :remote-method="searchNicknameUsers"
          :loading="nicknameUserLoading"
          @change="handleNicknameUserChange"
          class="!w-240px"
        >
          <el-option
            v-for="user in nicknameUserOptions"
            :key="user.value"
            :label="user.label"
            :value="user.value"
          >
            <div class="flex items-center">
              <el-avatar :src="user.avatar" :size="20" class="mr-2" />
              <span>{{ user.nickname }}</span>
              <span class="text-gray-400 ml-2">(ID: {{ user.value }})</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="推广资格" prop="brokerageEnabled">
        <el-select
          v-model="queryParams.brokerageEnabled"
          class="!w-240px"
          clearable
          placeholder="请选择推广资格"
        >
          <el-option :value="true" label="有" />
          <el-option :value="false" label="无" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          end-placeholder="结束日期"
          start-placeholder="开始日期"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
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
        <el-button
          v-hasPermi="['trade:brokerage-user:create']"
          plain
          type="primary"
          @click="openCreateUserForm"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="用户编号" min-width="80px" prop="id" />
      <el-table-column align="center" label="头像" prop="avatar" width="70px">
        <template #default="scope">
          <el-avatar :src="scope.row.avatar" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="昵称" min-width="80px" prop="nickname" />
      <el-table-column align="center" label="上级昵称" min-width="100px" prop="bindUserNickname">
        <template #default="scope">
          <span v-if="scope.row.bindUserNickname">{{ scope.row.bindUserNickname }}</span>
          <span v-else class="text-gray-400">无</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="推广人数" prop="brokerageUserCount" width="80px" />
      <el-table-column
        align="center"
        label="推广订单数量"
        min-width="110px"
        prop="brokerageOrderCount"
      />
      <el-table-column
        :formatter="fenToYuanFormat"
        align="center"
        label="推广订单金额"
        min-width="110px"
        prop="brokerageOrderPrice"
      />
      <el-table-column
        :formatter="fenToYuanFormat"
        align="center"
        label="已提现金额"
        min-width="100px"
        prop="withdrawPrice"
      />
      <el-table-column align="center" label="已提现次数" min-width="100px" prop="withdrawCount" />
      <el-table-column
        :formatter="fenToYuanFormat"
        align="center"
        label="未提现金额"
        min-width="100px"
        prop="price"
      />
      <el-table-column
        :formatter="fenToYuanFormat"
        align="center"
        label="冻结中佣金"
        min-width="100px"
        prop="frozenPrice"
      />
      <el-table-column align="center" label="推广资格" min-width="80px" prop="brokerageEnabled">
        <template #default="scope">
          <el-switch
            v-model="scope.row.brokerageEnabled"
            :disabled="!checkPermi(['trade:brokerage-user:update-bind-user'])"
            active-text="有"
            inactive-text="无"
            inline-prompt
            @change="handleBrokerageEnabledChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column align="center" label="代理等级" width="120px" prop="levelName">
        <template #default="scope">
          <el-tag v-if="scope.row.levelName" :type="getLevelTagType(scope.row.levelName)">
            {{ scope.row.levelName }}
          </el-tag>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="成为推广员时间"
        prop="brokerageTime"
        width="180px"
      />
      <el-table-column align="center" label="上级推广员编号" prop="bindUserId" width="150px" />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="推广员绑定时间"
        prop="bindUserTime"
        width="180px"
      />
      <el-table-column align="center" fixed="right" label="操作" width="320px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openBrokerageDetailDialog(scope.row)"
            v-hasPermi="['trade:brokerage-record:query']"
          >
            佣金详情
          </el-button>
          <el-button
            link
            type="primary"
            @click="openMaterialDetailDialog(scope.row)"
            v-hasPermi="['mb:user-material-balance:query']"
          >
            物料详情
          </el-button>
          <el-button
            link
            type="primary"
            @click="openDistributionGraphDialog(scope.row)"
            v-hasPermi="['trade:brokerage-user:query']"
          >
            <Icon class="mr-1" icon="ep:share" />
            分销关系图谱
          </el-button>
          <el-dropdown
            v-hasPermi="[
              'trade:brokerage-user:user-query',
              'trade:brokerage-user:order-query',
              'trade:brokerage-user:update-bind-user',
              'trade:brokerage-user:clear-bind-user'
            ]"
            @command="(command) => handleCommand(command, scope.row)"
          >
            <el-button link type="primary">
              <Icon icon="ep:d-arrow-right" />
              更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="checkPermi(['trade:brokerage-user:user-query'])"
                  command="openBrokerageUserTable"
                >
                  推广人
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="checkPermi(['trade:brokerage-user:order-query'])"
                  command="openBrokerageOrderTable"
                >
                  推广订单
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="checkPermi(['trade:brokerage-user:update-bind-user'])"
                  command="openUpdateBindUserForm"
                >
                  修改上级推广人
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="
                    scope.row.bindUserId && checkPermi(['trade:brokerage-user:clear-bind-user'])
                  "
                  command="handleClearBindUser"
                >
                  清除上级推广人
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
  
  <!-- 修改上级推广人表单 -->
  <BrokerageUserUpdateForm ref="updateFormRef" @success="getList" />
  <!-- 推广人列表 -->
  <BrokerageUserListDialog ref="listDialogRef" />
  <!-- 推广订单列表 -->
  <BrokerageOrderListDialog ref="orderDialogRef" />
  <!-- 创建分销员 -->
  <BrokerageUserCreateForm ref="createFormRef" @success="getList" />
  <!-- 佣金详情弹窗 -->
  <BrokerageDetailDialog ref="brokerageDetailDialogRef" />
  <!-- 物料详情弹窗 -->
  <MaterialDetailDialog ref="materialDetailDialogRef" />
  <!-- 分销关系图谱弹窗 -->
  <DistributionGraphDialog ref="distributionGraphDialogRef" @node-click="handleNodeClick" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as BrokerageUserApi from '@/api/mall/trade/brokerage/user'
import { checkPermi } from '@/utils/permission'
import { fenToYuanFormat } from '@/utils/formatter'
import BrokerageUserUpdateForm from '@/views/mall/trade/brokerage/user/BrokerageUserUpdateForm.vue'
import BrokerageUserListDialog from '@/views/mall/trade/brokerage/user/BrokerageUserListDialog.vue'
import BrokerageOrderListDialog from '@/views/mall/trade/brokerage/user/BrokerageOrderListDialog.vue'
import BrokerageUserCreateForm from '@/views/mall/trade/brokerage/user/BrokerageUserCreateForm.vue'
import BrokerageDetailDialog from '@/views/mall/trade/brokerage/user/BrokerageDetailDialog.vue'
import MaterialDetailDialog from '@/views/mall/trade/brokerage/user/MaterialDetailDialog.vue'
import DistributionGraphDialog from '@/views/mall/trade/brokerage/user/DistributionGraphDialog.vue'
import * as MemberUserApi from '@/api/member/user'
import { onMounted, onUnmounted, ref } from 'vue'

defineOptions({ name: 'TradeBrokerageUser' })

const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  bindUserId: null,
  userId: null, // 添加用户ID字段
  brokerageEnabled: true,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

// 推广员搜索相关
const userOptions = ref([]) // 推广员选项列表
const userLoading = ref(false) // 推广员搜索加载状态
const searchTimer = ref(null) // 防抖定时器

// 用户昵称搜索相关
const nicknameUserOptions = ref([]) // 用户昵称选项列表
const nicknameUserLoading = ref(false) // 用户昵称搜索加载状态
const nicknameSearchTimer = ref(null) // 用户昵称防抖定时器

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await BrokerageUserApi.getBrokerageUserPage(queryParams)
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
  userOptions.value = [] // 清空推广员选项
  nicknameUserOptions.value = [] // 清空用户昵称选项
  handleQuery()
}

/** 用户昵称搜索 */
const searchUsers = (query: string) => {
  if (!query || query.trim().length < 2) {
    userOptions.value = []
    return
  }

  // 防抖处理
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }

  searchTimer.value = setTimeout(async () => {
    try {
      userLoading.value = true
      const users = await MemberUserApi.getUserListByNickname(query.trim())
      userOptions.value = users.map(user => ({
        value: user.id,
        label: `${user.nickname} (ID: ${user.id})`,
        nickname: user.nickname,
        avatar: user.avatar
      }))
    } catch (error) {
      console.error('搜索用户失败:', error)
      userOptions.value = []
    } finally {
      userLoading.value = false
    }
  }, 300) // 300ms 防抖
}

/** 推广员选择变化 */
const handleUserChange = (userId: number) => {
  // 当用户选择了具体推广员后，使用该用户ID进行查询
  queryParams.bindUserId = userId
}

/** 用户昵称搜索 */
const searchNicknameUsers = (query: string) => {
  if (!query || query.trim().length < 2) {
    nicknameUserOptions.value = []
    return
  }

  // 防抖处理
  if (nicknameSearchTimer.value) {
    clearTimeout(nicknameSearchTimer.value)
  }

  nicknameSearchTimer.value = setTimeout(async () => {
    try {
      nicknameUserLoading.value = true
      const users = await MemberUserApi.getUserListByNickname(query.trim())
      nicknameUserOptions.value = users.map(user => ({
        value: user.id,
        label: `${user.nickname} (ID: ${user.id})`,
        nickname: user.nickname,
        avatar: user.avatar
      }))
    } catch (error) {
      console.error('搜索用户失败:', error)
      nicknameUserOptions.value = []
    } finally {
      nicknameUserLoading.value = false
    }
  }, 300) // 300ms 防抖
}

/** 用户昵称选择变化 */
const handleNicknameUserChange = (userId: number) => {
  // 当用户选择了具体用户后，使用该用户ID进行查询
  queryParams.userId = userId
}

/** 获取等级标签颜色 */
const getLevelTagType = (levelName: string) => {
  if (!levelName) return 'info'

  // 根据等级名称的关键词来判断颜色
  const name = levelName.toLowerCase()
  if (name.includes('vip') || name.includes('会员')) {
    return 'primary'
  } else if (name.includes('svip') || name.includes('超级') || name.includes('钻石')) {
    return 'warning'
  } else if (name.includes('分公司') || name.includes('公司') || name.includes('总监')) {
    return 'danger'
  } else if (name.includes('总代理') || name.includes('总代') || name.includes('金牌')) {
    return 'success'
  } else if (name.includes('代理') || name.includes('经理')) {
    return 'info'
  } else {
    return 'info'
  }
}

/** 打开佣金详情弹窗 */
const brokerageDetailDialogRef = ref()
const openBrokerageDetailDialog = (row: BrokerageUserApi.BrokerageUserVO) => {
  brokerageDetailDialogRef.value.open(row.id, row.nickname)
}

/** 打开物料详情弹窗 */
const materialDetailDialogRef = ref()
const openMaterialDetailDialog = (row: BrokerageUserApi.BrokerageUserVO) => {
  materialDetailDialogRef.value.open(row.id, row.nickname)
}

/** 打开分销关系图谱弹窗 */
const distributionGraphDialogRef = ref()
const openDistributionGraphDialog = (row: BrokerageUserApi.BrokerageUserVO) => {
  distributionGraphDialogRef.value.open(row.id, row.nickname)
}

/** 处理图谱节点点击事件 */
const handleNodeClick = (nodeData: any) => {
  // 可以在这里处理节点点击事件，比如跳转到用户详情页
  console.log('节点点击:', nodeData)
  // 示例：可以打开用户的佣金详情
  if (nodeData.id) {
    const userId = parseInt(nodeData.id.replace('user_', ''))
    // 这里可以调用其他弹窗或跳转页面
    message.success(`点击了用户：${nodeData.nickname}`)
  }
}

const handleCommand = (command: string, row: BrokerageUserApi.BrokerageUserVO) => {
  switch (command) {
    case 'openBrokerageUserTable':
      openBrokerageUserTable(row.id)
      break
    case 'openBrokerageOrderTable':
      openBrokerageOrderTable(row.id)
      break
    case 'openUpdateBindUserForm':
      openUpdateBindUserForm(row)
      break
    case 'handleClearBindUser':
      handleClearBindUser(row)
      break
  }
}

/** 打开推广人列表 */
const listDialogRef = ref()
const openBrokerageUserTable = (id: number) => {
  listDialogRef.value.open(id)
}

/** 打开推广订单列表 */
const orderDialogRef = ref()
const openBrokerageOrderTable = (id: number) => {
  orderDialogRef.value.open(id)
}

/** 打开表单：修改上级推广人 */
const updateFormRef = ref()
const openUpdateBindUserForm = (row: BrokerageUserApi.BrokerageUserVO) => {
  updateFormRef.value.open(row)
}

/** 创建分销员 */
const createFormRef = ref<InstanceType<typeof BrokerageUserCreateForm>>()
const openCreateUserForm = () => {
  createFormRef.value?.open()
}

/** 清除上级推广人 */
const handleClearBindUser = async (row: BrokerageUserApi.BrokerageUserVO) => {
  try {
    // 二次确认
    await message.confirm(`确认要清除"${row.nickname}"的上级推广人吗？`)
    // 发起修改
    await BrokerageUserApi.clearBindUser({ id: row.id })
    message.success('清除成功')
    // 刷新列表
    await getList()
  } catch {}
}

/** 推广资格：开通/关闭 */
const handleBrokerageEnabledChange = async (row: BrokerageUserApi.BrokerageUserVO) => {
  try {
    // 二次确认
    const text = row.brokerageEnabled ? '开通' : '关闭'
    await message.confirm(`确认要${text}"${row.nickname}"的推广资格吗？`)
    // 发起修改
    await BrokerageUserApi.updateBrokerageEnabled({ id: row.id, enabled: row.brokerageEnabled })
    message.success(text + '成功')
    // 刷新列表
    await getList()
  } catch {
    // 异常时，需要重置回之前的值
    row.brokerageEnabled = !row.brokerageEnabled
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})

/** 组件销毁时清理定时器 **/
onUnmounted(() => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  if (nicknameSearchTimer.value) {
    clearTimeout(nicknameSearchTimer.value)
  }
})
</script>
