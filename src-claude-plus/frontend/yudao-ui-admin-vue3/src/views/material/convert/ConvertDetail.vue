<template>
  <Dialog :title="'转化详情'" v-model="dialogVisible" width="800px">
    <el-form label-width="120px" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="转化ID：">
            <span>{{ detailData.id || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用户信息：">
            <span>{{ detailData.userNickname || '-' }} (ID: {{ detailData.userId || '-' }})</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="状态：">
            <el-tag v-if="detailData.status === 0" type="warning">待支付</el-tag>
            <el-tag v-else-if="detailData.status === 1" type="success">已完成</el-tag>
            <el-tag v-else-if="detailData.status === 2" type="danger">已取消</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="创建时间：">
            <span>{{ formatDate(detailData.createTime) }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">转化信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="源物料：">
            <div>
              <div>{{ detailData.sourceMaterialName || '-' }} (ID: {{ detailData.sourceMaterialId || '-' }})</div>
              <div class="text-gray-500 text-sm">转化数量: {{ detailData.sourceQuantity || '-' }} {{ detailData.sourceMaterialUnit || '-' }}</div>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标物料：">
            <div>
              <div>{{ detailData.targetMaterialName || '-' }} (ID: {{ detailData.targetMaterialId || '-' }})</div>
              <div class="text-gray-500 text-sm">获得数量: {{ detailData.targetQuantity || '-' }} {{ detailData.targetMaterialUnit || '-' }}</div>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="转化规则ID：">
            <span>{{ detailData.ruleId || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="转化费用：">
            <span v-if="detailData.convertFee && detailData.convertFee > 0">
              ¥{{ (detailData.convertFee / 100).toFixed(2) }}
            </span>
            <span v-else>免费</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">支付信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="支付方式：">
            <el-tag v-if="detailData.paymentType === 1" type="success">余额支付</el-tag>
            <el-tag v-else-if="detailData.paymentType === 2" type="primary">微信支付</el-tag>
            <el-tag v-else-if="detailData.paymentType === 3" type="warning">支付宝</el-tag>
            <span v-else>-</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支付时间：">
            <span>{{ formatDate(detailData.paymentTime) }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="支付订单号：">
            <span>{{ detailData.paymentOrderNo || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="第三方订单号：">
            <span>{{ detailData.thirdPartyOrderNo || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="转化备注：" v-if="detailData.remark">
        <span>{{ detailData.remark }}</span>
      </el-form-item>
      
      <el-form-item label="取消原因：" v-if="detailData.cancelReason">
        <span>{{ detailData.cancelReason }}</span>
      </el-form-item>
      
      <el-row :gutter="20" v-if="detailData.completeTime || detailData.cancelTime">
        <el-col :span="12">
          <el-form-item label="完成时间：" v-if="detailData.completeTime">
            <span>{{ formatDate(detailData.completeTime) }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="取消时间：" v-if="detailData.cancelTime">
            <span>{{ formatDate(detailData.cancelTime) }}</span>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as ConvertApi from '@/api/material/convert'
import { formatDate } from '@/utils/formatTime'

const dialogVisible = ref(false)
const loading = ref(false)
const detailData = ref({})

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    detailData.value = await ConvertApi.getConvert(id)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>