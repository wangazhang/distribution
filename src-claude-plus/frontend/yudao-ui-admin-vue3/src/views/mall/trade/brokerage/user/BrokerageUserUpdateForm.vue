<template>
  <Dialog v-model="dialogVisible" title="修改上级推广人" width="500">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="推广人" prop="bindUserId">
        <el-select
          v-model="formData.bindUserId"
          placeholder="请输入推广员昵称搜索"
          filterable
          remote
          reserve-keyword
          clearable
          :remote-method="searchUsers"
          :loading="userLoading"
          @change="handleUserChange"
          style="width: 100%"
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
    </el-form>
    <!-- 展示上级推广人的信息 -->
    <el-descriptions v-if="bindUser" :column="1" border>
      <el-descriptions-item label="头像">
        <el-avatar :src="bindUser.avatar" />
      </el-descriptions-item>
      <el-descriptions-item label="昵称">{{ bindUser.nickname }}</el-descriptions-item>
      <el-descriptions-item label="推广资格">
        <el-tag v-if="bindUser.brokerageEnabled">有</el-tag>
        <el-tag v-else type="info">无</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="成为推广员的时间">
        {{ formatDate(bindUser.brokerageTime) }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as BrokerageUserApi from '@/api/mall/trade/brokerage/user'
import { formatDate } from '@/utils/formatTime'
import * as MemberUserApi from '@/api/member/user'
import { onUnmounted, ref } from 'vue'

/** 修改分销用户 */
defineOptions({ name: 'BrokerageUserUpdateForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref()
const formRef = ref() // 表单 Ref
const formRules = reactive({
  bindUserId: [{ required: true, message: '推广员人不能为空', trigger: 'blur' }]
})

// 用户搜索相关
const userOptions = ref([]) // 用户选项列表
const userLoading = ref(false) // 用户搜索加载状态
const searchTimer = ref(null) // 防抖定时器

/** 打开弹窗 */
const open = async (row: BrokerageUserApi.BrokerageUserVO) => {
  resetForm()
  // 设置数据
  formData.value.id = row.id
  formData.value.bindUserId = row.bindUserId
  // 反显上级推广人
  if (row.bindUserId) {
    await handleGetUser()
  }
  dialogVisible.value = true
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
/** 修改上级推广人 */
const submitForm = async () => {
  if (formLoading.value) return
  // 校验表单
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 未查找到合适的上级
  if (!bindUser.value) {
    message.error('请先查询并确认推广人')
    return
  }

  // 提交请求
  formLoading.value = true
  try {
    // 发起修改
    await BrokerageUserApi.updateBindUser(formData.value)
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success', true)
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    bindUserId: undefined
  }
  formRef.value?.resetFields()
  bindUser.value = undefined
  userOptions.value = []
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
const handleUserChange = async (userId: number) => {
  if (userId) {
    formData.value.bindUserId = userId
    await handleGetUser()
  }
}

/** 查询推广员 */
const bindUser = ref<BrokerageUserApi.BrokerageUserVO>()
const handleGetUser = async () => {
  if (formData.value.bindUserId == formData.value.id) {
    message.error('不能绑定自己为推广人')
    return
  }
  formLoading.value = true
  bindUser.value = await BrokerageUserApi.getBrokerageUser(formData.value.bindUserId)
  if (!bindUser.value) {
    message.warning('推广员不存在')
  }
  formLoading.value = false
}

/** 组件销毁时清理定时器 **/
onUnmounted(() => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
})
</script>
