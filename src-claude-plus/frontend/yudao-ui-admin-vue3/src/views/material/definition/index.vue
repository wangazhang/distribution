<template>
  <ContentWrap>
    <!-- 搜索工作区 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="物料名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入物料名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="物料编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入物料编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="物料类型" prop="type">
        <el-select
          v-model="queryParams.type"
          placeholder="请选择物料类型"
          clearable
          class="!w-240px"
        >
          <el-option label="半成品" :value="1" />
          <el-option label="成品" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          @click="openForm('create')"
          v-hasPermi="['material:definition:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="物料ID" align="center" prop="id" width="80" />
      <el-table-column label="物料编码" align="center" prop="code" width="120" />
      <el-table-column label="物料名称" align="center" prop="name" min-width="150" />
      <el-table-column label="关联SPU" align="center" prop="spuId" min-width="160">
        <template #default="{ row }">
          <div v-if="row.spuId" class="spu-meta">
            <span>{{ row.spuName || `SPU#${row.spuId}` }}</span>
            <span class="spu-meta__id">ID：{{ row.spuId }}</span>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="关联模式" align="center" prop="linkMode" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.linkMode === 1" type="info">映射</el-tag>
          <el-tag v-else-if="scope.row.linkMode === 2" type="warning">快照</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="物料图片" align="center" prop="image" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.image"
            :src="scope.row.image"
            :preview-src-list="[scope.row.image]"
            style="width: 40px; height: 40px"
            fit="cover"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="物料类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 1" type="success">半成品</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="primary">成品</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="基础单位" align="center" prop="baseUnit" width="100" />
      <el-table-column label="支持出库" align="center" prop="supportOutbound" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.supportOutbound" type="success">是</el-tag>
          <el-tag v-else type="danger">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支持转化" align="center" prop="supportConvert" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.supportConvert" type="success">是</el-tag>
          <el-tag v-else type="danger">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="转化目标SPU" align="center" prop="convertedSpuId" width="180">
        <template #default="{ row }">
          <div v-if="row.convertedSpuId" class="spu-meta">
            <span>{{ row.convertedSpuName || `SPU#${row.convertedSpuId}` }}</span>
            <span class="spu-meta__id">ID：{{ row.convertedSpuId }}</span>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="转化单价(元)" align="center" prop="convertPrice" width="140">
        <template #default="scope">
          <span v-if="scope.row.convertPrice !== null && scope.row.convertPrice !== undefined">
            {{ fenToYuan(scope.row.convertPrice) }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['material:definition:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['material:definition:delete']"
          >
            删除
          </el-button>
          <!-- 快照模式物料显示"更新快照"按钮 -->
          <el-button
            v-if="scope.row.linkMode === 2 && scope.row.spuId"
            link
            type="warning"
            @click="handleUpdateSnapshot(scope.row)"
            v-hasPermi="['material:definition:update']"
          >
            更新快照
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <DefinitionForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE } from '@/utils/dict'
import { fenToYuan } from '@/utils'
import * as DefinitionApi from '@/api/material/definition'
import DefinitionForm from './DefinitionForm.vue'

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: null,
  code: null,
  type: null,
  status: null
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DefinitionApi.getDefinitionPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await DefinitionApi.deleteDefinition(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch (e) {
    console.log(e)
  }
}

/** 更新快照按钮操作 */
const handleUpdateSnapshot = async (row: any) => {
  try {
    await message.confirm('确认从SPU重新获取最新信息更新快照吗?', '更新快照')
    await DefinitionApi.updateSpuSnapshot(row.id)
    message.success('快照更新成功')
    // 刷新列表
    await getList()
  } catch (e) {
    console.log(e)
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.spu-meta {
  display: flex;
  flex-direction: column;
  line-height: 18px;
}

.spu-meta__id {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
