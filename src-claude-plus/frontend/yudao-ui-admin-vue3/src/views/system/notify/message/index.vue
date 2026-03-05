<template>
  <doc-alert title="站内信配置" url="https://doc.example.com/notify/" />

  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="用户昵称" prop="userId">
        <el-select
          v-model="queryParams.userId"
          placeholder="请输入用户昵称搜索"
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
      <el-form-item label="用户类型" prop="userType">
        <el-select
          v-model="queryParams.userType"
          placeholder="请选择用户类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.USER_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="模板编码" prop="templateCode">
        <el-input
          v-model="queryParams.templateCode"
          placeholder="请输入模板编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="模版类型" prop="templateType">
        <el-select
          v-model="queryParams.templateType"
          placeholder="请选择模版类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE)"
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
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户类型" align="center" prop="userType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.USER_TYPE" :value="scope.row.userType" />
        </template>
      </el-table-column>
      <el-table-column label="用户编号" align="center" prop="userId" width="80" />
      <el-table-column label="模板编码" align="center" prop="templateCode" width="80" />
      <el-table-column label="发送人名称" align="center" prop="templateNickname" width="180" />
      <el-table-column
        label="模版内容"
        align="center"
        prop="templateContent"
        width="200"
        show-overflow-tooltip
      />
      <el-table-column
        label="模版参数"
        align="center"
        prop="templateParams"
        width="180"
        show-overflow-tooltip
      >
        <template #default="scope"> {{ scope.row.templateParams }}</template>
      </el-table-column>
      <el-table-column label="模版类型" align="center" prop="templateType" width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE" :value="scope.row.templateType" />
        </template>
      </el-table-column>
      <el-table-column label="是否已读" align="center" prop="readStatus" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.readStatus" />
        </template>
      </el-table-column>
      <el-table-column
        label="阅读时间"
        align="center"
        prop="readTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row)"
            v-hasPermi="['system:notify-message:query']"
          >
            详情
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

  <!-- 表单弹窗：详情 -->
  <NotifyMessageDetail ref="detailRef" />
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as NotifyMessageApi from '@/api/system/notify/message'
import NotifyMessageDetail from './NotifyMessageDetail.vue'
import * as MemberUserApi from '@/api/member/user'
import { onMounted, onUnmounted, ref } from 'vue'

defineOptions({ name: 'SystemNotifyMessage' })

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userType: undefined,
  userId: undefined,
  templateCode: undefined,
  templateType: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

// 用户搜索相关
const userOptions = ref([]) // 用户选项列表
const userLoading = ref(false) // 用户搜索加载状态
const searchTimer = ref(null) // 防抖定时器

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await NotifyMessageApi.getNotifyMessagePage(queryParams)
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
  userOptions.value = [] // 清空用户选项
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

/** 用户选择变化 */
const handleUserChange = (userId: number) => {
  // 当用户选择了具体用户后，使用该用户ID进行查询
  queryParams.userId = userId
}

/** 详情操作 */
const detailRef = ref()
const openDetail = (data: NotifyMessageApi.NotifyMessageVO) => {
  detailRef.value.open(data)
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
})
</script>
