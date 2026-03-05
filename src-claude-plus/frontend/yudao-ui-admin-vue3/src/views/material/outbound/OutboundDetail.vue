<template>
  <Dialog :title="'出库详情'" v-model="dialogVisible" width="900px">
    <el-form label-width="120px" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="出库单号：">
            <span>{{ detailData.outboundNo || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="申请用户：">
            <span>{{ detailData.userNickname || '-' }} (ID: {{ detailData.userId || '-' }})</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="状态：">
            <el-tag v-if="detailData.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="detailData.status === 1" type="primary">已审核</el-tag>
            <el-tag v-else-if="detailData.status === 2" type="info">已发货</el-tag>
            <el-tag v-else-if="detailData.status === 3" type="success">已完成</el-tag>
            <el-tag v-else-if="detailData.status === 4" type="danger">已取消</el-tag>
            <el-tag v-else-if="detailData.status === 5" type="danger">审核拒绝</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="创建时间：">
            <span>{{ formatDate(detailData.createTime) }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">收货人信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="收货人姓名：">
            <span>{{ detailData.receiverName || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话：">
            <span>{{ detailData.receiverMobile || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="收货地址：">
        <span>{{ getFullAddress() }}</span>
      </el-form-item>
      
      <el-divider content-position="left">物流信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="物流公司：">
            <span>{{ detailData.logisticsCompany || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="快递单号：">
            <span>{{ detailData.logisticsCode || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="发货时间：">
            <span>{{ formatDate(detailData.shipTime) }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="完成时间：">
            <span>{{ formatDate(detailData.completeTime) }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">出库物料</el-divider>
      <el-table :data="detailData.items || []" border style="width: 100%">
        <el-table-column label="物料ID" prop="materialId" width="100" />
        <el-table-column label="物料名称" prop="materialName" min-width="150" />
        <el-table-column label="出库数量" prop="quantity" width="120" />
        <el-table-column label="基础单位" prop="baseUnit" width="100" />
      </el-table>
      
      <el-form-item label="备注：" v-if="detailData.remark">
        <span>{{ detailData.remark }}</span>
      </el-form-item>
      
      <el-form-item label="审核意见：" v-if="detailData.approveReason">
        <span>{{ detailData.approveReason }}</span>
      </el-form-item>
      
      <el-form-item label="取消原因：" v-if="detailData.cancelReason">
        <span>{{ detailData.cancelReason }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OutboundApi } from '@/api/material/outbound/index'
import { formatDate } from '@/utils/formatTime'

const dialogVisible = ref(false)
const loading = ref(false)
const detailData = ref({})

const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    detailData.value = await OutboundApi.getMaterialOutbound(id)
  } finally {
    loading.value = false
  }
}

const getFullAddress = () => {
  const data = detailData.value
  if (!data.receiverProvince) return '-'
  
  const parts = [
    data.receiverProvince,
    data.receiverCity,
    data.receiverDistrict,
    data.receiverDetailAddress
  ].filter(Boolean)
  
  return parts.join('')
}

defineExpose({ open })
</script>