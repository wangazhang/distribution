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
        <el-form-item label="商户号" label-width="180px" prop="config.huifuId">
          <el-input
            v-model="formData.config.huifuId"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入汇付天下商户号"
          />
        </el-form-item>
        <el-form-item label="产品号" label-width="180px" prop="config.productId">
          <el-input
            v-model="formData.config.productId"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入产品号"
          />
        </el-form-item>
        <el-form-item label="系统ID" label-width="180px" prop="config.systemId">
          <el-input
            v-model="formData.config.systemId"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入系统ID"
          />
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
        <el-form-item label="签名类型" label-width="180px" prop="config.signType">
          <el-radio-group v-model="formData.config.signType">
            <el-radio value="RSA">RSA</el-radio>
            <el-radio value="CERT">证书</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="RSA公钥" label-width="180px" prop="config.publicKey">
          <el-input
            v-model="formData.config.publicKey"
            :autosize="{ minRows: 4, maxRows: 6 }"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入RSA公钥"
            type="textarea"
          />
        </el-form-item>
        <el-form-item label="RSA私钥" label-width="180px" prop="config.privateKey">
          <el-input
            v-model="formData.config.privateKey"
            :autosize="{ minRows: 4, maxRows: 6 }"
            :style="{ width: '100%' }"
            clearable
            placeholder="请输入RSA私钥"
            type="textarea"
          />
        </el-form-item>
        <div v-if="formData.config.signType === 'CERT'">
          <el-form-item label="证书路径" label-width="180px" prop="config.certPath">
            <el-input
              v-model="formData.config.certPath"
              clearable
              placeholder="请输入证书路径"
            />
          </el-form-item>
          <el-form-item label="证书密码" label-width="180px" prop="config.certPwd">
            <el-input
              v-model="formData.config.certPwd"
              clearable
              placeholder="请输入证书密码"
              show-password
            />
          </el-form-item>
        </div>
        <el-form-item label="日志级别" label-width="180px" prop="config.logLevel">
          <el-select v-model="formData.config.logLevel" placeholder="请选择日志级别">
            <el-option label="INFO" value="INFO" />
            <el-option label="DEBUG" value="DEBUG" />
            <el-option label="ERROR" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" label-width="180px" prop="remark">
          <el-input v-model="formData.remark" :style="{ width: '100%' }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </Dialog>
  </div>
</template>
<script lang="ts" setup>
import { CommonStatusEnum } from '@/utils/constants'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import * as ChannelApi from '@/api/pay/channel'

defineOptions({ name: 'HuifuChannelForm' })

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
    huifuId: '',
    productId: '',
    systemId: '',
    publicKey: '',
    privateKey: '',
    signType: 'RSA',
    logLevel: 'INFO',
    certPath: '',
    certPwd: ''
  }
})
const formRules = {
  type: [{ required: true, message: '渠道类型不能为空', trigger: 'blur' }],
  feeRate: [{ required: true, message: '请输入渠道费率', trigger: 'blur' }],
  status: [{ required: true, message: '渠道状态不能为空', trigger: 'blur' }],
  'config.huifuId': [{ required: true, message: '请输入商户号', trigger: 'blur' }],
  'config.productId': [{ required: true, message: '请输入产品号', trigger: 'blur' }],
  'config.systemId': [{ required: true, message: '请输入系统ID', trigger: 'blur' }],
  'config.signType': [{ required: true, message: '签名类型不能为空', trigger: 'blur' }],
  'config.publicKey': [{ required: true, message: '请输入RSA公钥', trigger: 'blur' }],
  'config.privateKey': [{ required: true, message: '请输入RSA私钥', trigger: 'blur' }],
  'config.certPath': [{ required: true, message: '请输入证书路径', trigger: 'blur' }],
  'config.certPwd': [{ required: true, message: '请输入证书密码', trigger: 'blur' }],
  'config.logLevel': [{ required: true, message: '请选择日志级别', trigger: 'blur' }]
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
  if (!formRef) return
  const valid = await formRef.value.validate()
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
    type: 1, // 1-支付渠道 2-账户服务渠道
    status: CommonStatusEnum.ENABLE,
    feeRate: undefined,
    remark: '',
    config: {
      huifuId: '',
      productId: '',
      systemId: '',
      publicKey: '',
      privateKey: '',
      signType: 'RSA',
      logLevel: 'INFO',
      certPath: '',
      certPwd: ''
    }
  }
  formRef.value?.resetFields()
}
</script> 