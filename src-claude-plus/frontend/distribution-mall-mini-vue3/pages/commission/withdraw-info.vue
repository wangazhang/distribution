<template>
  <s-layout
    title="提现资料"
    :class="['withdraw-layout', { 'change-mode': isModifyMode }]"
  >
    <view class="withdraw-page">
      <!-- 状态提示条（换卡模式隐藏） -->
      <view v-if="!isModifyMode" class="status-strip-shell">
      <view class="status-strip-fixed">
        <view
          v-if="statusStripVisible"
          class="status-strip"
          :class="statusInfo.tag"
          @tap="openStatusModal"
        >
          <view class="strip-label-wrap">
            <text class="strip-label">{{ statusInfo.text }}</text>
          </view>
          <view class="strip-icon-group">
            <view class="strip-actions" @tap.stop="refreshStatus">
              <uni-icons type="reload" size="16" color="#fff" />
            </view>
            <view class="strip-info" @tap.stop="openStatusModal">
              <uni-icons type="info" size="18" color="#fff" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="stepper">
      <view v-for="item in steps" :key="item.index" class="step-item">
        <view class="step-dot" :class="dotClass(item.index)">
          {{ stepLabel(item.index) }}
        </view>
        <view class="step-content">
          <text class="step-title">{{ item.title }}</text>
          <text class="step-desc">{{ item.desc }}</text>
        </view>
        <view v-if="item.index < steps.length" class="step-line"></view>
      </view>
    </view>

    <view v-if="currentStep === 1" class="step-panel" key="identity">
      <view class="info-card">
        <view class="card-header">
          <view>
            <text class="card-title">身份信息</text>
            <text class="card-sub">请保持与证件一致，方便渠道审核</text>
          </view>
        </view>
        <view class="field-grid two">
          <view class="field-item">
            <text class="label">姓名</text>
            <uni-easyinput
              class="input-text"
              v-model="form.signedName"
              :disabled="!canEditIdentity"
              placeholder="请输入真实姓名"
            />
          </view>
          <view class="field-item">
            <text class="label">手机号</text>
            <uni-easyinput
              class="input-text"
              v-model="form.mobile"
              :disabled="!canEditIdentity"
              placeholder="用于审核"
            />
          </view>
          <view class="field-item full">
            <text class="label">身份证号</text>
            <uni-easyinput
              class="input-text"
              v-model="form.idCardNo"
              :disabled="!canEditIdentity"
              placeholder="请输入身份证号"
            />
          </view>
          <view class="field-item picker full">
            <text class="label">证件有效期</text>
            <view class="range">
              <picker
                mode="date"
                :value="form.idCardValidStart"
                :disabled="!canEditIdentity"
                @change="onDateChange('idCardValidStart', $event)"
              >
                <view class="picker-value">{{ form.idCardValidStart || '开始日期' }}</view>
              </picker>
              <text class="split">至</text>
              <picker
                mode="date"
                :value="form.idCardValidEnd"
                :disabled="!canEditIdentity"
                @change="onDateChange('idCardValidEnd', $event)"
              >
                <view class="picker-value">{{ form.idCardValidEnd || '结束日期' }}</view>
              </picker>
            </view>
          </view>
        </view>
        <view class="upload-row">
          <view class="upload-tile">
            <view class="upload-label">身份证正面</view>
            <view class="upload-box">
              <s-uploader
                v-model="idFrontFiles"
                :url="form.idCardFrontUrl"
                @update:url="(val) => (form.idCardFrontUrl = val)"
                :readonly="!canEditIdentity"
                fileMediatype="image"
                :limit="1"
                mode="grid"
                :imageStyles="{ width: '168rpx', height: '168rpx' }"
              >
                <view class="uploader-placeholder">
                  <uni-icons type="camera-filled" size="40" color="#c0c6d8" />
                </view>
              </s-uploader>
            </view>
            <text class="upload-tip">需清晰无遮挡</text>
          </view>
          <view class="upload-tile">
            <view class="upload-label">身份证反面</view>
            <view class="upload-box">
              <s-uploader
                v-model="idBackFiles"
                :url="form.idCardBackUrl"
                @update:url="(val) => (form.idCardBackUrl = val)"
                :readonly="!canEditIdentity"
                fileMediatype="image"
                :limit="1"
                mode="grid"
                :imageStyles="{ width: '168rpx', height: '168rpx' }"
              >
                <view class="uploader-placeholder">
                  <uni-icons type="camera-filled" size="40" color="#c0c6d8" />
                </view>
              </s-uploader>
            </view>
            <text class="upload-tip">请确保信息可辨识</text>
          </view>
        </view>
      </view>

      <view class="info-card">
        <view class="card-header">
          <view>
            <text class="card-title">地址信息</text>
            <text class="card-sub">省市区需与身份证一致</text>
          </view>
        </view>

        <view class="triple-picker">
          <view class="picker-field">
            <text class="label">省份</text>
            <picker
              mode="selector"
              :range="regionState.provinces"
              range-key="name"
              :value="regionState.provinceIndex"
              :disabled="!canEditIdentity"
              @change="onProvinceChange"
            >
              <view class="picker-value">{{ regionState.provinceName || '请选择省份' }}</view>
            </picker>
          </view>
          <view class="picker-field">
            <text class="label">城市</text>
            <picker
              mode="selector"
              :range="regionState.cities"
              range-key="name"
              :value="regionState.cityIndex"
              :disabled="!canEditIdentity || !form.provinceCode"
              @change="onCityChange"
            >
              <view class="picker-value">{{ regionState.cityName || '请选择城市' }}</view>
            </picker>
          </view>
          <view class="picker-field">
            <text class="label">区县</text>
            <picker
              mode="selector"
              :range="regionState.districts"
              range-key="name"
              :value="regionState.districtIndex"
              :disabled="!canEditIdentity || !form.cityCode"
              @change="onDistrictChange"
            >
              <view class="picker-value">{{ regionState.districtName || '请选择区县' }}</view>
            </picker>
          </view>
        </view>

        <view class="field-item full">
          <text class="label">详细地址</text>
          <uni-easyinput
            class="input-text"
            v-model="form.address"
            :disabled="!canEditIdentity"
            placeholder="街道、社区、门牌号"
          />
        </view>
        <view class="field-item full">
          <view class="label-with-action">
            <text class="label">邮寄地址</text>
            <view
              v-if="canEditIdentity"
              class="link-btn"
              @tap="copyAddressFromResidence"
            >
              同上
            </view>
          </view>
          <uni-easyinput
            class="input-text"
            v-model="form.receiverAddress"
            :disabled="!canEditIdentity"
            placeholder="合同邮寄地址"
          />
        </view>
      </view>
    </view>

    <view v-else class="step-panel" key="bank">
      <view class="info-card">
        <view class="card-header">
          <view>
            <text class="card-title">银行卡信息</text>
            <text class="card-sub">提现资金将发放至该卡</text>
          </view>
        </view>
        <view class="card-field">
          <view class="field-item full center">
            <text class="label">银行卡号</text>
            <uni-easyinput
              class="input-text"
              v-model="form.bankAccountNo"
              :disabled="!canEditBank"
              placeholder="仅支持本人储蓄卡"
            />
          </view>
          <view class="field-item full center">
            <text class="label">开户人</text>
            <uni-easyinput
              class="input-text"
              v-model="form.bankAccountName"
              :disabled="!canEditBank"
              placeholder="请输入开户人姓名"
            />
          </view>
          <view class="field-item picker center">
            <text class="label">开户银行</text>
            <picker
              mode="selector"
              :range="bankOptions"
              range-key="label"
              :value="bankIndex"
              :disabled="!canEditBank || !bankOptions.length"
              @change="onBankChange"
            >
              <view class="picker-value">{{ form.bankName || '请选择银行' }}</view>
            </picker>
          </view>
          <view class="field-item full center">
            <text class="label">开户支行</text>
            <uni-easyinput
              class="input-text"
              v-model="form.bankBranchName"
              :disabled="!canEditBank"
              placeholder="支行名称（选填）"
            />
          </view>
        </view>
        <view class="upload-row upload-center">
          <view class="upload-tile single">
            <view class="upload-label">银行卡正面（选填）</view>
            <view class="upload-box">
              <s-uploader
                v-model="bankCardFiles"
                :url="form.bankCardFrontUrl"
                @update:url="(val) => (form.bankCardFrontUrl = val)"
                :readonly="!canEditBank"
                fileMediatype="image"
                :limit="1"
                mode="grid"
                :imageStyles="{ width: '168rpx', height: '168rpx' }"
              >
                <view class="uploader-placeholder">
                  <uni-icons type="camera-filled" size="40" color="#c0c6d8" />
                </view>
              </s-uploader>
            </view>
            <text class="upload-tip">需拍摄银行卡正面</text>
          </view>
        </view>
      </view>

      <view class="info-card">
        <view class="card-header">
          <view>
            <text class="card-title">联系信息</text>
            <text class="card-sub">用于接收审核通知</text>
          </view>
        </view>
        <view class="field-grid two">
          <view class="field-item">
            <text class="label">联系邮箱</text>
            <uni-easyinput
              class="input-text"
              v-model="form.email"
              :disabled="!canEditBank"
              placeholder="选填"
            />
          </view>
        </view>
        <view class="field-item full textarea">
          <text class="label">备注</text>
          <uni-easyinput
            class="input-text"
            type="textarea"
            autoHeight
            v-model="form.extra"
            :disabled="!canEditBank"
            placeholder="补充说明（选填）"
          />
        </view>
      </view>

    </view>

    <view class="action-card fixed">
      <text class="tips">{{ actionTips }}</text>
      <view class="btn-row" v-if="currentStep === 1">
        <button class="primary-btn" @tap="handleNext">下一步</button>
      </view>
      <view class="btn-row" v-else>
        <!-- 换卡模式不展示上一页按钮，避免误操作 -->
        <button v-if="!isModifyMode" class="ghost-btn" @tap="handlePrev">上一步</button>
        <button class="primary-btn" :disabled="!canSubmit || submitting" @tap="handleSubmit">
          {{ submitting ? '提交中' : submitButtonText }}
        </button>
      </view>
    </view>
    </view>
    <view v-if="submitProgress.visible" class="progress-mask">
    <view class="progress-modal">
        <view class="progress-header">
          <text>{{ progressTitle }}</text>
          <text class="progress-close" @tap="closeProgress" v-if="submitProgress.error">关闭</text>
        </view>
        <view
          v-for="phase in submitPhases"
          :key="phase.step"
          class="progress-row"
        >
          <view class="progress-left">
            <text class="progress-index">{{ phase.step === 1 ? '①' : '②' }}</text>
            <text>{{ phase.title }}</text>
          </view>
          <text
            class="progress-state"
            :class="{
              active: submitProgress.step === phase.step && !submitProgress.error,
              done: submitProgress.step > phase.step && !submitProgress.error
            }"
          >
            {{
              submitProgress.step > phase.step
                ? '完成'
                : submitProgress.step === phase.step
                ? submitProgress.error
                  ? '已终止'
                  : '进行中'
                : '待开始'
            }}
            <view
              v-if="submitProgress.step === phase.step && !submitProgress.error"
              class="spinner"
            ></view>
          </text>
        </view>
        <text v-if="submitProgress.error" class="progress-error">{{ submitProgress.error }}</text>
      </view>
    </view>
    <view v-if="statusModal.visible" class="status-mask">
      <view class="status-modal">
        <view class="status-modal-header">
          <text>{{ statusInfo.text }}</text>
          <text class="status-close" @tap="closeStatusModal">关闭</text>
        </view>
        <view class="status-modal-body">
          <view class="status-row">
            <text class="status-row-label">当前状态</text>
            <text>{{ statusInfo.desc }}</text>
          </view>
          <view class="status-row" v-if="form.subMerchantId">
            <text class="status-row-label">子商户号</text>
            <text>{{ form.subMerchantId }}</text>
          </view>
          <view class="status-row" v-if="showChannelHint">
            <text class="status-row-label">{{ form.status === 30 ? '驳回原因' : '渠道提示' }}</text>
            <text>{{ form.rejectReason }}</text>
          </view>
          <view class="status-row" v-else>
            <text class="status-row-label">审核说明</text>
            <text>资料已提交，渠道审核完成前不可修改。</text>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { onLoad, onPullDownRefresh, onBackPress } from '@dcloudio/uni-app'
import sheep from '@/sheep'
import {
  getSettleProfile,
  saveSettleDraft,
  submitSettleAccount,
  saveChangeCardDraft,
  submitChangeCard
} from '@/sheep/api/pay/settle'
import ThirdCodesApi from '@/sheep/api/infra/thirdCodes'
import DictApi from '@/sheep/api/system/dict'

const statusMeta = {
  0: { text: '草稿', desc: '资料仅保存在本系统，可随时继续完善', tag: 'tag-default' },
  10: { text: '审核中', desc: '资料已提交，预计 1-2 个工作日内反馈', tag: 'tag-warning' },
  20: { text: '已通过', desc: '提现资料已通过审核，可直接申请提现', tag: 'tag-success' },
  30: { text: '已驳回', desc: '资料需要调整后重新提交', tag: 'tag-danger' }
}

const form = reactive({
  status: 0,
  rejectReason: '',
  signedName: '',
  mobile: '',
  idCardNo: '',
  idCardValidStart: '',
  idCardValidEnd: '',
  idCardFrontUrl: '',
  idCardBackUrl: '',
  bankAccountNo: '',
  bankAccountName: '',
  bankName: '',
  bankBranchName: '',
  bankCardFrontUrl: '',
  provinceCode: '',
  cityCode: '',
  areaCode: '',
  address: '',
  receiverAddress: '',
  email: '',
  extra: ''
})

const regionState = reactive({
  provinces: [],
  cities: [],
  provinceName: '',
  cityName: '',
  provinceIndex: 0,
  cityIndex: 0,
  districts: [],
  districtName: '',
  districtIndex: 0
})

const bankOptions = ref([])
const bankIndex = ref(-1)
const idFrontFiles = ref([])
const idBackFiles = ref([])
const bankCardFiles = ref([])

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
// 提交审核的阶段提示：先创建系统入网账号，再提交渠道审核
const submitProgress = reactive({
  visible: false,
  step: 0,
  error: ''
})
const declareSubmitPhases = [
  { step: 1, title: '创建系统入网账号' },
  { step: 2, title: '提交渠道审核' }
]
const changeSubmitPhases = [
  { step: 1, title: '同步换卡资料' },
  { step: 2, title: '更新渠道结算卡' }
]
const submitPhases = computed(() =>
  isModifyMode.value ? changeSubmitPhases : declareSubmitPhases
)
const progressTitle = computed(() =>
  isModifyMode.value ? '银行卡换卡中' : '提现资料提交中'
)
const submitButtonText = computed(() => (isModifyMode.value ? '提交换卡' : '提交审核'))
const actionTips = computed(() =>
  isModifyMode.value
    ? '换卡资料将加密同步至渠道，请确认为本人银行卡。'
    : '资料将加密传输，仅用于提现审核。'
)

const closeProgress = () => {
  submitProgress.visible = false
  submitProgress.error = ''
}

const openStatusModal = () => {
  if (!statusStripVisible.value) return
  statusModal.visible = true
}

const closeStatusModal = () => {
  statusModal.visible = false
}

const buildUploadList = (value) => (value ? [{ url: value, path: value }] : [])

const syncUploadFromForm = () => {
  console.log('[upload][sync] form urls', form.idCardFrontUrl, form.idCardBackUrl, form.bankCardFrontUrl)
  idFrontFiles.value = buildUploadList(form.idCardFrontUrl)
  idBackFiles.value = buildUploadList(form.idCardBackUrl)
  bankCardFiles.value = buildUploadList(form.bankCardFrontUrl)
  console.log('[upload][sync] final lists', idFrontFiles.value, idBackFiles.value, bankCardFiles.value)
}

syncUploadFromForm()

// 将请求返回统一转换为 { code, data, msg }，同时把网络异常(false/null)识别为错误
const normalize = (resp) => {
  if (resp === false || resp === undefined || resp === null) {
    return { code: -1, data: null, msg: '网络异常，请稍后重试' }
  }
  if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
    return {
      code: resp.code,
      data: resp.data,
      msg: resp.msg
    }
  }
  return { code: 0, data: resp, msg: '' }
}

const steps = [
  { index: 1, title: '身份信息', desc: '实名信息及证件上传' },
  { index: 2, title: '银行卡信息', desc: '结算卡及联系信息' }
]

const routeMode = ref('declare')
const currentStep = ref(1)
const changeModeBackLocked = ref(false)

const isModifyMode = computed(() => routeMode.value === 'change')

const statusInfo = computed(() => statusMeta[form.status] ?? statusMeta[0])
const statusStripVisible = computed(() => form.status && form.status !== 0)
const showChannelHint = computed(() => !!form.rejectReason && form.status !== 20)
const statusModal = reactive({ visible: false })
const auditLocked = computed(() => form.status === 10 && !form.rejectReason)
const canEdit = computed(() => !auditLocked.value)
const canEditIdentity = computed(() => canEdit.value && !isModifyMode.value)
const canEditBank = computed(() => canEdit.value)

const requiredDeclareFields = [
  'signedName',
  'mobile',
  'idCardNo',
  'idCardValidStart',
  'idCardValidEnd',
  'idCardFrontUrl',
  'idCardBackUrl',
  'bankAccountNo',
  'bankAccountName',
  'bankName',
  'provinceCode',
  'cityCode',
  'areaCode',
  'address',
  'receiverAddress'
]
// 换卡仅校验核心卡信息，银行卡照片为可选
const requiredChangeFields = [
  'bankAccountNo',
  'bankAccountName',
  'bankName',
  'provinceCode',
  'cityCode',
  'areaCode',
  'receiverAddress'
]
const canSubmit = computed(() => {
  if (!canEdit.value) return false
  const required = isModifyMode.value ? requiredChangeFields : requiredDeclareFields
  return required.every((key) => form[key])
})

const toast = (msg) => msg && sheep.$helper.toast(msg)

// 换卡模式下拦截返回操作，避免误触回到上一页
onBackPress(() => {
  if (!changeModeBackLocked.value) {
    return false
  }
  toast('换卡资料提交前不可返回上一页')
  return true
})

// 换卡提交前提示期间银行卡无法使用
const confirmChangeModeUnavailable = () => {
  if (!isModifyMode.value) {
    return Promise.resolve(true)
  }
  return new Promise((resolve) => {
    uni.showModal({
      title: '温馨提示',
      content: '换卡期间两张卡都不能使用，直到换卡审核成功',
      confirmText: '知道了',
      showCancel: false,
      success: ({ confirm }) => resolve(!!confirm),
      fail: () => resolve(false)
    })
  })
}

const ensureEditable = (scope = 'all') => {
  if (scope === 'identity' && !canEditIdentity.value) {
    if (isModifyMode.value) {
      toast('换卡仅支持修改银行卡信息')
    } else {
      toast('资料审核中，如需修改请等待审核结果')
    }
    return false
  }
  if (scope === 'bank' && !canEditBank.value) {
    toast('资料审核中，如需修改请等待审核结果')
    return false
  }
  if (!canEdit.value) {
    toast('资料审核中，如需修改请等待审核结果')
    return false
  }
  return true
}

const loadBankList = async () => {
  const { code, data, msg } = normalize(await DictApi.getDictDataListByType('brokerage_bank_name'))
  bankOptions.value = code === 0 && Array.isArray(data) ? data : []
  if (code !== 0 && msg) {
    toast(msg)
  }
  syncBankPicker()
}

const syncBankPicker = () => {
  if (!form.bankName) {
    bankIndex.value = -1
    return
  }
  const idx = bankOptions.value.findIndex(
    (item) => item.label === form.bankName || item.value === form.bankName
  )
  bankIndex.value = idx
}

const onBankChange = (event) => {
  if (!ensureEditable('bank')) return
  const index = Number(event.detail.value)
  const target = bankOptions.value[index]
  if (!target) return
  bankIndex.value = index
  form.bankName = target.label
}

const syncProvinceName = () => {
  const match = regionState.provinces.find((item, index) => {
    if (item.code === form.provinceCode) {
      regionState.provinceIndex = index
      return true
    }
    return false
  })
  regionState.provinceName = match ? match.name : ''
}

const syncCityName = () => {
  const match = regionState.cities.find((item, index) => {
    if (item.code === form.cityCode) {
      regionState.cityIndex = index
      return true
    }
    return false
  })
  regionState.cityName = match ? match.name : ''
}

const syncDistrictName = () => {
  const match = regionState.districts.find((item, index) => {
    if (item.code === form.areaCode) {
      regionState.districtIndex = index
      return true
    }
    return false
  })
  regionState.districtName = match ? match.name : ''
}

const loadProvinces = async () => {
  const { code, data, msg } = normalize(await ThirdCodesApi.listProvinces())
  regionState.provinces = code === 0 && Array.isArray(data) ? data : []
  if (code !== 0) toast(msg)
  syncProvinceName()
}

const loadCities = async (provinceCode) => {
  if (!provinceCode) {
    regionState.cities = []
    regionState.cityName = ''
    form.cityCode = ''
    await loadDistricts('')
    return
  }
  const { code, data, msg } = normalize(await ThirdCodesApi.listCities(provinceCode))
  regionState.cities = code === 0 && Array.isArray(data) ? data : []
  if (code !== 0) toast(msg)
  syncCityName()
  await loadDistricts(form.cityCode)
}

const loadDistricts = async (cityCode) => {
  if (!cityCode) {
    regionState.districts = []
    regionState.districtName = ''
    regionState.districtIndex = 0
    form.areaCode = ''
    return
  }
  const { code, data, msg } = normalize(await ThirdCodesApi.listDistricts(cityCode))
  regionState.districts = code === 0 && Array.isArray(data) ? data : []
  if (code !== 0) toast(msg)
  syncDistrictName()
}

const onProvinceChange = async (event) => {
  if (!ensureEditable('identity')) return
  const index = event.detail.value
  const item = regionState.provinces[index]
  if (!item) return
  form.provinceCode = item.code
  regionState.provinceName = item.name
  regionState.provinceIndex = index
  form.cityCode = ''
  regionState.cityName = ''
  regionState.cityIndex = 0
  form.areaCode = ''
  regionState.districts = []
  regionState.districtName = ''
  regionState.districtIndex = 0
  await loadCities(item.code)
}

const onCityChange = async (event) => {
  if (!ensureEditable('identity')) return
  const index = event.detail.value
  const item = regionState.cities[index]
  if (!item) return
  form.cityCode = item.code
  regionState.cityName = item.name
  regionState.cityIndex = index
  form.areaCode = ''
  await loadDistricts(item.code)
}

const onDistrictChange = (event) => {
  if (!ensureEditable('identity')) return
  const index = event.detail.value
  const item = regionState.districts[index]
  if (!item) return
  form.areaCode = item.code
  regionState.districtName = item.name
  regionState.districtIndex = index
}

const onDateChange = (field, event) => {
  if (!ensureEditable('identity')) return
  form[field] = event.detail.value
}

const refreshStatus = async () => {
  await loadProfile(true)
}

const loadProfile = async (showMsg = false) => {
  loading.value = true
  try {
  const { code, data, msg } = normalize(await getSettleProfile())
    if (code === 0 && data) {
      Object.assign(form, data, {
        idCardValidStart: data.idCardValidStart || '',
        idCardValidEnd: data.idCardValidEnd || '',
        receiverAddress: data.receiverAddress || data.address || ''
      })
      if (!form.bankName && data.bankCode) {
        form.bankName = data.bankCode
      }
      syncUploadFromForm()
    } else if (showMsg) {
      toast(msg)
    }
    syncProvinceName()
    await loadCities(form.provinceCode)
    syncBankPicker()
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!ensureEditable()) return
  saving.value = true
  try {
    const { code, msg } = normalize(await saveSettleDraft(form))
    if (code !== 0) {
      toast(msg)
      return
    }
    toast('草稿已保存')
  } finally {
    saving.value = false
  }
}

const handleSubmit = async () => {
  const scope = isModifyMode.value ? 'bank' : 'all'
  if (!ensureEditable(scope)) return
  if (!canSubmit.value) {
    toast(isModifyMode.value ? '请先完善银行卡信息' : '请先完善必填信息')
    return
  }
  // 换卡模式下提交前提示银行卡暂不可用
  const warningAccepted = await confirmChangeModeUnavailable()
  if (!warningAccepted) {
    return
  }
  submitProgress.visible = true
  submitProgress.step = 1
  submitProgress.error = ''
  submitting.value = true
  const stageOne = isModifyMode.value ? () => saveChangeCardDraft(pickBankPayload()) : () => saveSettleDraft(form)
  const stageTwo = isModifyMode.value ? () => submitChangeCard() : () => submitSettleAccount()
  const stageOneError = isModifyMode.value ? '同步换卡资料失败' : '同步资料失败'
  const stageTwoError = isModifyMode.value ? '提交换卡失败' : '提交渠道审核失败'
  try {
    const saveResult = normalize(await stageOne())
    if (saveResult.code !== 0) {
      submitProgress.error = saveResult.msg || stageOneError
      toast(submitProgress.error)
      return
    }
    submitProgress.step = 2
    const submitResult = normalize(await stageTwo())
    if (submitResult.code !== 0) {
      submitProgress.error = submitResult.msg || stageTwoError
      toast(submitProgress.error)
      return
    }
    if (isModifyMode.value) {
      toast('换卡申请已提交')
      await loadProfile(true)
    } else {
      form.status = 10
    }
    submitProgress.visible = false
    submitProgress.step = 0
    changeModeBackLocked.value = false
    setTimeout(() => uni.navigateBack(), 800)
  } catch (error) {
    submitProgress.error = (error && error.message) || '网络异常，请稍后重试'
    toast(submitProgress.error)
    return
  } finally {
    submitting.value = false
    if (!submitProgress.error) {
      submitProgress.visible = false
      submitProgress.step = 0
    }
  }
}

const pickBankPayload = () => ({
  bankAccountNo: form.bankAccountNo,
  bankAccountName: form.bankAccountName,
  bankName: form.bankName,
  bankBranchName: form.bankBranchName,
  bankCardFrontUrl: form.bankCardFrontUrl,
  provinceCode: form.provinceCode,
  cityCode: form.cityCode,
  areaCode: form.areaCode,
  receiverAddress: form.receiverAddress
})

onLoad(async (options) => {
  routeMode.value = options?.mode === 'change' ? 'change' : 'declare'
  currentStep.value = routeMode.value === 'change' ? 2 : 1
  changeModeBackLocked.value = routeMode.value === 'change'
  await Promise.all([loadBankList(), loadProvinces()])
  await loadProfile()
})

onPullDownRefresh(async () => {
  await loadProfile(true)
  uni.stopPullDownRefresh()
})

const handleNext = () => {
  currentStep.value = 2
}

const handlePrev = () => {
  currentStep.value = 1
}

const copyAddressFromResidence = () => {
  if (!ensureEditable('identity')) return
  if (!form.address) {
    toast('请先填写详细地址')
    return
  }
  const merged = [
    regionState.provinceName,
    regionState.cityName,
    regionState.districtName,
    form.address
  ]
    .filter(Boolean)
    .join('')
  form.receiverAddress = merged
  toast('已复制到邮寄地址')
}

const dotClass = (index) => {
  const step = currentStep.value
  if (step === index) return 'active'
  if (step > index) return 'done'
  return 'pending'
}

const stepLabel = (index) => {
  return currentStep.value > index ? '✓' : index
}
</script>

<style scoped lang="scss">
.withdraw-layout {
  min-height: 100vh;
}

.change-mode {
  :deep(.ui-navbar-box .icon-box) {
    display: none;
  }
}

.withdraw-page {
  padding: 24rpx;
  padding-bottom: 260rpx;
  background: linear-gradient(180deg, #f9fbff 0%, #f5f7fb 50%, #f2f4f8 100%);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

//.status-strip-shell {
//  height: 96rpx;
//  position: relative;
//}

.status-strip-fixed {
  position: sticky;
  top: 16rpx;
  z-index: 50;
  width: 100%;
}

.status-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 25rpx 20rpx;
  border-radius: 999rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  color: #fff;
}

.status-strip .strip-label-wrap {
  flex: 1;
}

.status-strip .strip-label {
  font-size: 24rpx;
}

.status-strip.placeholder {
  visibility: hidden;
  pointer-events: none;
}

.status-strip .strip-icon-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.status-strip .strip-actions,
.status-strip .strip-info {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-strip.tag-default {
  background: linear-gradient(135deg, #b6bdc7, #a6aebd);
}

.status-strip.tag-warning {
  background: linear-gradient(135deg, #ffb078, #ff8c47);
}

.status-strip.tag-success {
  background: linear-gradient(135deg, #7ad59f, #4caf50);
}

.status-strip.tag-danger {
  background: linear-gradient(135deg, #ff0000, #ff5950);
}

.stepper {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 6rpx 16rpx rgba(105, 119, 165, 0.08);
}

.step-item {
  flex: 1;
  display: flex;
  align-items: center;
  position: relative;
}

.step-dot {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  text-align: center;
  line-height: 48rpx;
  font-size: 26rpx;
  background: #f0f1f5;
  color: #8a8f99;

  &.active {
    background: linear-gradient(135deg, #ff885b, #ff6432);
    color: #fff;
    box-shadow: 0 6rpx 16rpx rgba(255, 136, 91, 0.4);
  }

  &.done {
    background: #6fcf97;
    color: #fff;
  }
}

.step-content {
  margin-left: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.step-title {
  font-size: 26rpx;
  color: #1d1f2a;
}

.step-desc {
  font-size: 22rpx;
  color: #8e8e93;
}

.step-line {
  position: absolute;
  right: -30%;
  top: 50%;
  width: 60%;
  height: 2rpx;
  background: #e6e8ef;
}

.step-panel {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.info-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 6rpx 16rpx rgba(51, 83, 145, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;

  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1d1f2a;
  }

  .card-sub {
    display: block;
    font-size: 24rpx;
    color: #8e8e93;
    margin-top: 6rpx;
  }
}

.field-grid {
  display: grid;
  gap: 24rpx;

  &.two {
    grid-template-columns: repeat(auto-fit, minmax(300rpx, 1fr));
  }
}

.card-field {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  align-items: center;
}

.field-item.center {
  width: 100%;
}

.field-item {
  display: flex;
  flex-direction: column;
  padding: 12rpx 0;

  &.full {
    grid-column: 1 / -1;
  }

  .label {
    font-size: 24rpx;
    color: #8a8f99;
    margin-bottom: 12rpx;
  }

  &.center {
    //align-items: center;
  }
}

.label-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .link-btn {
    font-size: 24rpx;
    color: #4b63c0;
  }
}

.picker {
  .picker-value {
    font-size: 28rpx;
    padding: 22rpx 26rpx;
    border-radius: 12rpx;
    border: 1rpx solid #f0f0f3;
    background: #fafbff;
    color: #c3bdbd;
  }
}

.triple-picker {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;

  .picker-field {
    flex: 1;
    display: flex;
    flex-direction: column;

    .label {
      font-size: 24rpx;
      color: #8a8f99;
      margin-bottom: 12rpx;
    }
  }
}

.range {
  display: flex;
  align-items: center;
  gap: 12rpx;

  picker {
    flex: 1;
  }

  .split {
    color: #8a8f99;
    font-size: 24rpx;
  }
}

.input-text {
  width: 100%;

  :deep(.uni-easyinput__content) {
    border-radius: 12rpx;
    background: #fafbff;
    border: 1rpx solid #f0f0f3;
  }

  :deep(.uni-input-input) {
    padding-left: 5rpx;
  }

  :deep(.uni-input-placeholder) {
    padding-left: 5rpx;
    color: #c5cad8;
  }
}

:deep(.uni-easyinput) {
  .uni-easyinput__content {
    border-radius: 12rpx;
    background: #fafbff;
    border: 1rpx solid #f0f0f3;
    padding: 0 5rpx;
  }

  .uni-input-input {
    font-size: 28rpx;
    padding-left: 5rpx;
  }

  .uni-input-placeholder {
    padding-left: 5rpx;
    color: #c5cad8;
  }
}

.upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
  margin-top: 16rpx;
}

.upload-row.upload-center {
  justify-content: center;
}

.upload-tile {
  flex: 1;
  min-width: 280rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  &.single {
    max-width: 320rpx;
  }

  .upload-label {
    font-size: 24rpx;
    color: #8a8f99;
    margin-bottom: 12rpx;
  }

  .upload-tip {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    color: #b0b3c3;
  }

  .upload-box {
    width: 100%;
    display: flex;
    justify-content: center;
  }
}

.uploader-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  border: 2rpx dashed #e0e5f2;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdfdff;
}

.progress-card {
  border: 1rpx solid #f0f0f5;
  border-radius: 16rpx;
  padding: 18rpx 22rpx;
  margin-bottom: 20rpx;
  background: #fafbff;
}

.progress-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 99;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

.progress-modal {
  width: 90%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 18rpx 40rpx rgba(14, 30, 70, 0.18);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #1d1f2a;
  margin-bottom: 16rpx;
}

.progress-close {
  font-size: 24rpx;
  color: #8a8f99;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  color: #4a4f5d;
  padding: 6rpx 0;
}

.progress-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.progress-index {
  font-weight: 600;
  color: #ec5e2a;
}

.progress-state {
  color: #8a8f99;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.progress-state.active {
  color: #ec5e2a;
}

.progress-state.done {
  color: #4caf50;
}

.progress-error {
  display: block;
  margin-top: 10rpx;
  color: #d93025;
  font-size: 24rpx;
}

.spinner {
  width: 24rpx;
  height: 24rpx;
  border: 4rpx solid rgba(236, 94, 42, 0.2);
  border-radius: 50%;
  border-top-color: #ec5e2a;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.status-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 90;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

.status-modal {
  width: 620rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 18rpx 32rpx rgba(0, 0, 0, 0.18);
}

.status-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.status-close {
  font-size: 24rpx;
  color: #8a8f99;
}

.status-modal-body {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.status-row {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  font-size: 26rpx;
  color: #4a4f5d;
}

.status-row-label {
  font-size: 24rpx;
  color: #8a8f99;
}

.action-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 -6rpx 24rpx rgba(20, 32, 68, 0.08), 0 6rpx 20rpx rgba(51, 83, 145, 0.04);
  border-top: 1rpx solid rgba(226, 232, 247, 0.8);

  .tips {
    display: block;
    font-size: 24rpx;
    color: #8a8f99;
    margin-bottom: 24rpx;
  }

  .btn-row {
    display: flex;
    gap: 20rpx;
  }
}

.action-card.fixed {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(env(safe-area-inset-bottom) + 24rpx);
  z-index: 20;
}

.ghost-btn,
.primary-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
  border: none;
}

.ghost-btn {
  border: 1rpx solid #ec5e2a;
  color: #ec5e2a;
  background: transparent;
}

.primary-btn {
  background: linear-gradient(135deg, #ff875e, #ec5e2a);
  color: #fff;
}

.ghost-btn:disabled,
.primary-btn:disabled {
  opacity: 0.5;
}
</style>
