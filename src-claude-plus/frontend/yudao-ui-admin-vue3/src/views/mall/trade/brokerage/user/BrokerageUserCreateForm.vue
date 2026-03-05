<template>
  <Dialog v-model="dialogVisible" title="创建分销员" width="800">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90"
    >
      <el-row :gutter="20">
        <el-col :span="12" :xs="24">
          <el-form-item label="分销员" prop="userId">
            <el-select
              v-model="formData.userId"
              placeholder="请输入分销员昵称搜索"
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
          <!-- 展示分销员的信息 -->
          <el-descriptions v-if="userInfo.user" :column="1" border>
            <el-descriptions-item label="头像">
              <el-avatar :src="userInfo.user?.avatar" />
            </el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userInfo.user?.nickname }}</el-descriptions-item>
          </el-descriptions>
        </el-col>

        <el-col :span="12" :xs="24">
          <el-form-item label="上级推广人" prop="bindUserId">
            <el-select
              v-model="formData.bindUserId"
              placeholder="请输入推广员昵称搜索"
              filterable
              remote
              reserve-keyword
              clearable
              :remote-method="searchBindUsers"
              :loading="bindUserLoading"
              @change="handleBindUserChange"
              style="width: 100%"
            >
              <el-option
                v-for="user in bindUserOptions"
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
          <!-- 展示上级推广人的信息 -->
          <el-descriptions v-if="userInfo.bindUser" :column="1" border>
            <el-descriptions-item label="头像">
              <el-avatar :src="userInfo.bindUser?.avatar" />
            </el-descriptions-item>
            <el-descriptions-item label="昵称"
              >{{ userInfo.bindUser?.nickname }}
            </el-descriptions-item>
            <el-descriptions-item label="推广资格">
              <el-tag v-if="userInfo.bindUser?.brokerageEnabled">有</el-tag>
              <el-tag v-else type="info">无</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="成为推广员的时间">
              {{ formatDate(userInfo.bindUser?.brokerageTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as BrokerageUserApi from '@/api/mall/trade/brokerage/user'
import * as UserApi from '@/api/member/user'
import { formatDate } from '@/utils/formatTime'
import * as MemberUserApi from '@/api/member/user'
import { onUnmounted, ref } from 'vue'

defineOptions({ name: 'BrokerageUserCreateForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  userId: undefined,
  bindUserId: undefined
})
const formRef = ref() // 表单 Ref
const formRules = reactive({
  userId: [{ required: true, message: '分销员不能为空', trigger: 'blur' }]
})

// 用户搜索相关
const userOptions = ref([]) // 用户选项列表
const bindUserOptions = ref([]) // 推广员选项列表
const userLoading = ref(false) // 用户搜索加载状态
const bindUserLoading = ref(false) // 推广员搜索加载状态
const searchTimer = ref(null) // 防抖定时器
const bindSearchTimer = ref(null) // 推广员防抖定时器

/** 打开弹窗 */
const open = async () => {
  resetForm()
  dialogVisible.value = true
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
/** 创建分销员 */
const submitForm = async () => {
  if (formLoading.value) return
  // 校验表单
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 提交请求
  formLoading.value = true
  try {
    // 发起修改
    await BrokerageUserApi.createBrokerageUser(formData.value)
    message.success(t('common.createSuccess'))
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    userId: undefined,
    bindUserId: undefined
  }

  userInfo.bindUser = undefined
  userInfo.user = undefined
  userOptions.value = []
  bindUserOptions.value = []
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

/** 推广员昵称搜索 */
const searchBindUsers = (query: string) => {
  if (!query || query.trim().length < 2) {
    bindUserOptions.value = []
    return
  }

  // 防抖处理
  if (bindSearchTimer.value) {
    clearTimeout(bindSearchTimer.value)
  }

  bindSearchTimer.value = setTimeout(async () => {
    try {
      bindUserLoading.value = true
      const users = await MemberUserApi.getUserListByNickname(query.trim())
      bindUserOptions.value = users.map(user => ({
        value: user.id,
        label: `${user.nickname} (ID: ${user.id})`,
        nickname: user.nickname,
        avatar: user.avatar
      }))
    } catch (error) {
      console.error('搜索推广员失败:', error)
      bindUserOptions.value = []
    } finally {
      bindUserLoading.value = false
    }
  }, 300) // 300ms 防抖
}

/** 用户选择变化 */
const handleUserChange = async (userId: number) => {
  if (userId) {
    formData.value.userId = userId
    await handleGetUser(userId, '分销员')
  }
}

/** 推广员选择变化 */
const handleBindUserChange = async (userId: number) => {
  if (userId) {
    formData.value.bindUserId = userId
    await handleGetUser(userId, '推广员')
  }
}

/** 查询推广员和分销员 */
const userInfo = reactive<{
  bindUser: BrokerageUserApi.BrokerageUserVO | undefined
  user: BrokerageUserApi.BrokerageUserVO | undefined
}>({
  bindUser: undefined,
  user: undefined
})
const handleGetUser = async (id: any, userType: string) => {
  if (!id) {
    message.warning(`请先输入${userType}编号后重试！！！`)
    return
  }
  if (userType === '推广员' && formData.value.bindUserId == formData.value.userId) {
    message.error('不能绑定自己为推广人')
    return
  }
  const user =
    userType === '推广员' ? await BrokerageUserApi.getBrokerageUser(id) : await UserApi.getUser(id)
  userType === '推广员' ? (userInfo.bindUser = user) : (userInfo.user = user)
  if (!user) {
    message.warning(`${userType}不存在`)
  }
}

/** 组件销毁时清理定时器 **/
onUnmounted(() => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  if (bindSearchTimer.value) {
    clearTimeout(bindSearchTimer.value)
  }
})
</script>
