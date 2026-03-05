<template>
  <div>
    <Dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="渠道费率" label-width="180px" prop="feeRate">
          <el-input
            v-model="formData.feeRate"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入渠道费率"
          >
            <template #append>%</template>
          </el-input>
        </el-form-item>
        <el-form-item label="商户号" label-width="180px" prop="config.merchantId">
          <el-input
            v-model="formData.config.merchantId"
            :style="{ width: '100%' }"
            clearable
            maxlength="9"
            placeholder="请输入首信易商户号（9位）"
          />
        </el-form-item>
        <el-form-item label="私钥路径" label-width="180px" prop="config.privateKeyPath">
          <el-input
            v-model="formData.config.privateKeyPath"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入客户端私钥路径（.pfx文件）"
          >
            <template #prepend>classpath:</template>
          </el-input>
          <div style=" margin-top: 4px; font-size: 12px;color: #909399;">
            示例: payease/certs/client.pfx
          </div>
        </el-form-item>
        <el-form-item label="私钥密码" label-width="180px" prop="config.privateKeyPassword">
          <el-input
            v-model="formData.config.privateKeyPassword"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入私钥密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="公钥路径" label-width="180px" prop="config.publicKeyPath">
          <el-input
            v-model="formData.config.publicKeyPath"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入服务器公钥路径（.cer文件）"
          >
            <template #prepend>classpath:</template>
          </el-input>
          <div style=" margin-top: 4px; font-size: 12px;color: #909399;">
            示例: payease/certs/public.cer
          </div>
        </el-form-item>
        <el-form-item label="服务商ID" label-width="180px" prop="config.partnerId">
          <el-input
            v-model="formData.config.partnerId"
            :style="{ width: '100%' }"
            clearable
            placeholder="服务商ID（选填，服务商模式时使用）"
          />
          <div style=" margin-top: 4px; font-size: 12px;color: #909399;">
            仅服务商模式需要填写
          </div>
        </el-form-item>
        <el-form-item label="渠道状态" label-width="180px" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio
              v-for="dict in getDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="parseInt(dict.value)"
              :value="parseInt(dict.value)"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" label-width="180px" prop="remark">
          <el-input
            v-model="formData.remark"
            :autosize="{ minRows: 2, maxRows: 4 }"
            :style="{ width: '100%' }"
            placeholder="请输入备注信息"
            type="textarea"
          />
        </el-form-item>
<el-alert
  title="提示"
  type="info"
  :closable="false"
  style="margin-bottom: 20px;"
>
  <div>
    <p>• SDK将自动使用首信易官方生产环境URL</p>
    <p>• 证书文件需放置在项目 resources 目录下</p>
    <p>• 商户号和证书信息请从首信易商户后台获取</p>
    <p v-if="isAccountChannel">• 当前配置用于账户服务（入网/分账/提现），请确保渠道类型为“账户服务渠道”</p>
  </div>
</el-alert>
      </el-form>
      <template #footer>
        <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </Dialog>
  </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import { CommonStatusEnum, PayChannelEnum } from '@/utils/constants'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as ChannelApi from '@/api/pay/channel'

defineOptions({ name: 'PayeaseChannelForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用

const formData = ref<any>({
  appId: '',
  code: '',
  type: 1, // 1-支付渠道 2-账户服务渠道
  status: undefined,
  feeRate: undefined,
  remark: '',
  config: {
    merchantId: '',
    privateKeyPath: 'payease/certs/private.pfx',
    privateKeyPassword: '',
    publicKeyPath: 'payease/certs/public.cer',
    partnerId: ''
  }
})

const isAccountChannel = computed(() => formData.value.code === PayChannelEnum.PAYEASE_ACCOUNT.code)

const formRules = {
  type: [{ required: true, message: '渠道类型不能为空', trigger: 'blur' }],
  feeRate: [{ required: true, message: '请输入渠道费率', trigger: 'blur' }],
  status: [{ required: true, message: '渠道状态不能为空', trigger: 'blur' }],
  'config.merchantId': [
    { required: true, message: '请输入商户号', trigger: 'blur' },
    { pattern: /^\d{9}$/, message: '商户号必须是9位数字', trigger: 'blur' }
  ],
  'config.privateKeyPath': [{ required: true, message: '请输入私钥路径', trigger: 'blur' }],
  'config.privateKeyPassword': [{ required: true, message: '请输入私钥密码', trigger: 'blur' }],
  'config.publicKeyPath': [{ required: true, message: '请输入公钥路径', trigger: 'blur' }]
}

const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (appId, code) => {
  dialogVisible.value = true
  formLoading.value = true
  resetForm(appId, code)
  // 加载数据
  try {
    const data = await ChannelApi.getChannel(appId, code)
    if (data && data.id) {
      formData.value = data
      formData.value.config = JSON.parse(data.config)
    } else if (isAccountChannel.value) {
      // 账户服务渠道需要强制标记为账户类型，避免误用支付渠道。
      formData.value.type = 2
    }
    dialogTitle.value = !formData.value.id ? '创建支付渠道' : '编辑支付渠道'
  } finally {
    formLoading.value = false
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  const formInstance = formRef.value
  if (!formInstance) return
  const valid = await formInstance.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = { ...formData.value } as unknown as ChannelApi.ChannelVO

    data.config = JSON.stringify(formData.value.config)
    if (!data.id) {
      await ChannelApi.createChannel(data)
      message.success(t('common.createSuccess'))
    } else {
      await ChannelApi.updateChannel(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = (appId, code) => {
  formData.value = {
    appId: appId,
    code: code,
    // 首信易账户服务要标记为“账户服务渠道”，否则后端将其视作普通支付渠道。
    type: code === PayChannelEnum.PAYEASE_ACCOUNT.code ? 2 : 1,
    status: CommonStatusEnum.ENABLE,
    feeRate: undefined,
    remark: '',
    config: {
      merchantId: '',
      privateKeyPath: 'payease/certs/private.pfx',
      privateKeyPassword: '',
      publicKeyPath: 'payease/certs/public.cer',
      partnerId: ''
    }
  }
  formRef.value?.resetFields()
}
</script>
