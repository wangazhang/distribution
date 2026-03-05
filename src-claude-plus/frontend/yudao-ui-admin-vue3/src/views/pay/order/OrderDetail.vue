<template>
  <Dialog v-model="dialogVisible" title="订单详情" width="700px">
    <div class="flex items-center justify-between mb-10px">
      <h3 class="text-base font-semibold">支付订单基础信息</h3>
      <el-button
        v-if="detailData.id"
        size="small"
        plain
        type="primary"
        :loading="channelDialogLoading"
        @click="handleQueryChannelOrder(detailData.id)"
      >
        查询渠道订单
      </el-button>
    </div>
    <el-descriptions :column="2" label-class-name="desc-label">
      <el-descriptions-item label="商户单号">
        <el-tag size="small">{{ detailData.merchantOrderId }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="支付单号">
        <el-tag type="warning" size="small" v-if="detailData.no">{{ detailData.no }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="应用编号">{{ detailData.appId }}</el-descriptions-item>
      <el-descriptions-item label="应用名称">{{ detailData.appName }}</el-descriptions-item>
      <el-descriptions-item label="支付状态">
        <dict-tag :type="DICT_TYPE.PAY_ORDER_STATUS" :value="detailData.status" size="small" />
      </el-descriptions-item>
      <el-descriptions-item label="支付金额">
        <el-tag type="success" size="small">￥{{ (detailData.price / 100.0).toFixed(2) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="手续费">
        <el-tag type="warning" size="small">
          ￥{{ (detailData.channelFeePrice / 100.0).toFixed(2) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="手续费比例">
        {{ (detailData.channelFeeRate / 100.0).toFixed(2) }}%
      </el-descriptions-item>
      <el-descriptions-item label="支付时间">
        {{ formatDate(detailData.successTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="失效时间">
        {{ formatDate(detailData.expireTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatDate(detailData.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ formatDate(detailData.updateTime) }}
      </el-descriptions-item>
    </el-descriptions>
    <!-- 分割线 -->
    <el-divider />
    <el-descriptions :column="2" label-class-name="desc-label">
      <el-descriptions-item label="商品标题">{{ detailData.subject }}</el-descriptions-item>
      <el-descriptions-item label="商品描述">{{ detailData.body }}</el-descriptions-item>
      <el-descriptions-item label="支付渠道">
        <dict-tag :type="DICT_TYPE.PAY_CHANNEL_CODE" :value="detailData.channelCode" />
      </el-descriptions-item>
      <el-descriptions-item label="支付 IP">{{ detailData.userIp }}</el-descriptions-item>
      <el-descriptions-item label="渠道单号">
        <el-tag size="mini" type="success" v-if="detailData.channelOrderNo">
          {{ detailData.channelOrderNo }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="渠道用户">{{ detailData.channelUserId }}</el-descriptions-item>
      <el-descriptions-item label="退款金额">
        <el-tag size="mini" type="danger">
          ￥{{ (detailData.refundPrice / 100.0).toFixed(2) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="通知 URL">{{ detailData.notifyUrl }}</el-descriptions-item>
    </el-descriptions>
    <!-- 分割线 -->
    <el-divider />
    <el-descriptions :column="1" label-class-name="desc-label" direction="vertical" border>
      <el-descriptions-item label="支付通道异步回调内容">
        <el-text style=" word-break: break-word;white-space: pre-wrap">
          {{ detailData.extension.channelNotifyData }}
        </el-text>
      </el-descriptions-item>
    </el-descriptions>

    <el-dialog
      v-model="channelDialogVisible"
      title="渠道订单详情"
      width="640px"
      append-to-body
      @close="channelOrderData = null"
    >
      <el-skeleton v-if="channelDialogLoading" :rows="5" animated />
      <div v-else-if="channelOrderData" class="space-y-16px">
        <el-descriptions :column="2" label-class-name="desc-label" border>
          <el-descriptions-item label="渠道状态">
            {{ channelOrderData.statusName || channelOrderData.status || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="妥投状态">
            {{ channelOrderData.deliveryStatusName || channelOrderData.deliveryStatus || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商户订单号">
            {{ channelOrderData.outTradeNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="渠道订单号">
            {{ channelOrderData.channelOrderNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="渠道流水号">
            {{ channelOrderData.channelTransactionNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="渠道用户">
            {{ channelOrderData.channelUserId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付完成时间">
            {{ formatDate(channelOrderData.successTime) }}
          </el-descriptions-item>
        </el-descriptions>
        <div>
          <div class="mb-5px text-12px text-gray-500">渠道原始响应</div>
          <el-scrollbar class="channel-json-viewer">
            <pre>{{ channelOrderJson || '{}' }}</pre>
          </el-scrollbar>
        </div>
      </div>
      <div v-else class="py-20px text-center text-gray-400">暂无渠道数据</div>
    </el-dialog>
  </Dialog>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import * as OrderApi from '@/api/pay/order'
import { formatDate } from '@/utils/formatTime'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'PayOrderDetail' })

const dialogVisible = ref(false) // 弹窗的是否展示
const detailLoading = ref(false) // 表单的加载中
const detailData = ref({
  extension: {}
})
const channelDialogVisible = ref(false)
const channelDialogLoading = ref(false)
const channelOrderData = ref<any | null>(null)
const message = useMessage()

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  // 设置数据
  detailLoading.value = true
  try {
    detailData.value = await OrderApi.getOrderDetail(id)
    if (!detailData.value.extension) {
      detailData.value.extension = {}
    }
  } finally {
    detailLoading.value = false
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const handleQueryChannelOrder = async (id?: number | string) => {
  if (!id) {
    message.warning('缺少支付单号，无法查询渠道订单')
    return
  }
  channelDialogVisible.value = true
  channelDialogLoading.value = true
  try {
    const payloadId = typeof id === 'string' ? id : String(id)
    channelOrderData.value = await OrderApi.queryChannelOrder(payloadId)
  } finally {
    channelDialogLoading.value = false
  }
}

const channelOrderJson = computed(() => {
  if (!channelOrderData.value) {
    return ''
  }
  try {
    const raw =
      channelOrderData.value.rawData && Object.keys(channelOrderData.value.rawData).length > 0
        ? channelOrderData.value.rawData
        : channelOrderData.value
    return JSON.stringify(raw, null, 2)
  } catch (error) {
    console.error('渠道订单 JSON 格式化失败', error)
    return JSON.stringify(channelOrderData.value)
  }
})
</script>
<style>
.tag-purple {
  color: #722ed1;
  background: #f9f0ff;
  border-color: #d3adf7;
}

.tag-pink {
  color: #eb2f96;
  background: #fff0f6;
  border-color: #ffadd2;
}

.space-y-16px > * + * {
  margin-top: 16px;
}

.channel-json-viewer {
  max-height: 260px;
  padding: 12px;
  font-family: SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  color: #e6edf3;
  background-color: #0f111a;
  border-radius: 8px;
}

.channel-json-viewer pre {
  margin: 0;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
