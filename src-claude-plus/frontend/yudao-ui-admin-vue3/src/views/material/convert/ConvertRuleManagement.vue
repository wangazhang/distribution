<template>
  <Dialog :title="'转化规则管理'" v-model="dialogVisible" width="1200px">
    <div v-loading="loading">
      <!-- 搜索区域 -->
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="80px"
      >
        <el-form-item label="源物料" prop="sourceMaterialId">
          <el-input-number
            v-model="queryParams.sourceMaterialId"
            placeholder="源物料ID"
            controls-position="right"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="目标物料" prop="targetMaterialId">
          <el-input-number
            v-model="queryParams.targetMaterialId"
            placeholder="目标物料ID"
            controls-position="right"
            class="!w-160px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            class="!w-120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery" size="small">
            <Icon icon="ep:search" class="mr-5px" /> 搜索
          </el-button>
          <el-button @click="resetQuery" size="small">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button
            type="primary"
            @click="openRuleForm('create')"
            size="small"
            v-hasPermi="['material:convert:rule:create']"
          >
            <Icon icon="ep:plus" class="mr-5px" /> 新增规则
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 规则列表 -->
      <el-table :data="ruleList" border style="width: 100%">
        <el-table-column label="规则ID" prop="id" width="80" />
        <el-table-column label="源物料" min-width="150">
          <template #default="scope">
            <div>{{ scope.row.sourceMaterialName }} (ID: {{ scope.row.sourceMaterialId }})</div>
            <div class="text-gray-500 text-sm">{{ scope.row.sourceMaterialUnit }}</div>
          </template>
        </el-table-column>
        <el-table-column label="目标物料" min-width="150">
          <template #default="scope">
            <div>{{ scope.row.targetMaterialName }} (ID: {{ scope.row.targetMaterialId }})</div>
            <div class="text-gray-500 text-sm">{{ scope.row.targetMaterialUnit }}</div>
          </template>
        </el-table-column>
        <el-table-column label="转化比例" width="120">
          <template #default="scope">
            {{ scope.row.sourceQuantity }} : {{ scope.row.targetQuantity }}
          </template>
        </el-table-column>
        <el-table-column label="转化费用" width="100">
          <template #default="scope">
            <span v-if="scope.row.convertFee && scope.row.convertFee > 0">
              ¥{{ (scope.row.convertFee / 100).toFixed(2) }}
            </span>
            <span v-else>免费</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openRuleForm('update', scope.row.id)"
              v-hasPermi="['material:convert:rule:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDeleteRule(scope.row.id)"
              v-hasPermi="['material:convert:rule:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        :total="ruleTotal"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getRuleList"
        class="mt-4"
      />
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>

    <!-- 规则表单弹窗 -->
    <ConvertRuleForm ref="ruleFormRef" @success="getRuleList" />
  </Dialog>
</template>

<script setup lang="ts">
import * as ConvertRuleApi from '@/api/material/convertRule'
import ConvertRuleForm from './ConvertRuleForm.vue'

const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const ruleList = ref([])
const ruleTotal = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sourceMaterialId: null,
  targetMaterialId: null,
  status: null
})
const queryFormRef = ref()

const open = async () => {
  dialogVisible.value = true
  await getRuleList()
}

/** 查询规则列表 */
const getRuleList = async () => {
  loading.value = true
  try {
    const data = await ConvertRuleApi.getConvertRulePage(queryParams)
    ruleList.value = data.list
    ruleTotal.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getRuleList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 新增/编辑规则 */
const ruleFormRef = ref()
const openRuleForm = (type: string, id?: number) => {
  ruleFormRef.value.open(type, id)
}

/** 删除规则 */
const handleDeleteRule = async (id: number) => {
  try {
    await message.delConfirm()
    await ConvertRuleApi.deleteConvertRule(id)
    message.success('删除成功')
    await getRuleList()
  } catch (e) {
    console.log(e)
  }
}

defineExpose({ open })
</script>