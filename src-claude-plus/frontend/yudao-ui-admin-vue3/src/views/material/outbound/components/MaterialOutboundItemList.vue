<template>
  <ContentWrap title="出库物料明细">
    <!-- 列表 -->
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
    >
      <el-table-column label="物料图片" align="center" width="80">
        <template #default="{ row }">
          <el-image
            v-if="row.materialImage"
            :src="row.materialImage"
            style="width: 50px; height: 50px;"
            :preview-src-list="[row.materialImage]"
            fit="cover"
          />
          <el-icon v-else><Picture /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="物料名称" align="center" prop="materialName" min-width="150" />
      <el-table-column label="数量" align="center" prop="quantity" min-width="100" />
      <el-table-column label="单位" align="center" prop="unit" min-width="80" />
      <el-table-column label="备注" align="center" prop="remark" min-width="150" />
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import { MaterialOutboundItemApi, MaterialOutboundItemVO } from '@/api/material/outbound-item'
import { Picture } from '@element-plus/icons-vue'

defineOptions({ name: 'MaterialOutboundItemList' })

// 接收父组件传递的出库单ID
const props = defineProps({
  outboundId: {
    type: String,
    required: true
  }
})

const loading = ref(false) // 列表的加载中
const list = ref<MaterialOutboundItemVO[]>([]) // 列表的数据

/** 查询列表 */
const getList = async () => {
  if (!props.outboundId) {
    list.value = []
    return
  }

  loading.value = true
  try {
    // 使用新的API获取完整的物料明细信息
    const response = await MaterialOutboundItemApi.getMaterialOutboundItemListByOutboundId(props.outboundId)
    list.value = response || []
  } finally {
    loading.value = false
  }
}

/** 监听出库单ID变化，重新加载数据 */
watch(() => props.outboundId, () => {
  getList()
}, { immediate: true })
</script>
