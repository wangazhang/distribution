<template>
  <ContentWrap>
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="姓名">
        <el-input v-model="queryParams.signedName" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="queryParams.mobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option :value="0" label="草稿" />
          <el-option :value="10" label="审核中" />
          <el-option :value="20" label="已通过" />
          <el-option :value="30" label="已驳回" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="signedName" label="姓名" min-width="120" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column prop="bankAccountNo" label="银行卡号" min-width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0">草稿</el-tag>
          <el-tag type="warning" v-else-if="row.status === 10">审核中</el-tag>
          <el-tag type="success" v-else-if="row.status === 20">已通过</el-tag>
          <el-tag type="danger" v-else>已驳回</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row.id)">查看</el-button>
          <el-button type="primary" link @click="openEdit(row.id)">修改</el-button>
          <el-button type="primary" link @click="confirmSubmit(row.id)">提交</el-button>
          <el-button type="primary" link @click="handleSync(row.id)">同步</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <el-drawer v-model="detailVisible" title="资料详情" size="50%">
    <el-descriptions :column="2" border v-if="current">
      <el-descriptions-item label="姓名">{{ current.signedName }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ current.mobile }}</el-descriptions-item>
      <el-descriptions-item label="身份证号">{{ current.idCardNo }}</el-descriptions-item>
      <el-descriptions-item label="银行卡号">{{ current.bankAccountNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusText(current.status) }}</el-descriptions-item>
      <el-descriptions-item label="驳回原因">{{ current.rejectReason || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-drawer>

  <el-dialog v-model="editVisible" title="协助修改" width="520px">
    <el-form :model="editForm" label-width="100px">
      <el-form-item label="手机号">
        <el-input v-model="editForm.mobile" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="editForm.email" />
      </el-form-item>
      <el-form-item label="银行卡号">
        <el-input v-model="editForm.bankAccountNo" />
      </el-form-item>
      <el-form-item label="支行">
        <el-input v-model="editForm.bankBranchName" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="editForm.extra" type="textarea" rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="handleUpdate">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SettleAccountApi } from '@/api/pay/settle/account'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const editVisible = ref(false)
const current = ref<any>()

const queryParams = reactive({
  signedName: '',
  mobile: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const editForm = reactive({
  id: 0,
  mobile: '',
  email: '',
  bankAccountNo: '',
  bankBranchName: '',
  bankCardFrontUrl: '',
  extra: ''
})

const statusText = (status?: number) => {
  switch (status) {
    case 0:
      return '草稿'
    case 10:
      return '审核中'
    case 20:
      return '已通过'
    case 30:
      return '已驳回'
    default:
      return '-'
  }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await SettleAccountApi.getPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
getList()

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.signedName = ''
  queryParams.mobile = ''
  queryParams.status = undefined
  handleQuery()
}

const openDetail = async (id: number) => {
  current.value = await SettleAccountApi.get(id)
  detailVisible.value = true
}

const openEdit = async (id: number) => {
  const data = await SettleAccountApi.get(id)
  current.value = data
  Object.assign(editForm, {
    id: data.id,
    mobile: data.mobile,
    email: data.email,
    bankAccountNo: data.bankAccountNo,
    bankBranchName: data.bankBranchName,
    extra: data.extra
  })
  editVisible.value = true
}

const handleUpdate = async () => {
  await SettleAccountApi.update(editForm)
  ElMessage.success('保存成功')
  editVisible.value = false
  getList()
}

const confirmSubmit = async (id: number) => {
  await ElMessageBox.confirm('确认将该资料提交审核？', '提示', { type: 'warning' })
  await SettleAccountApi.submit(id)
  ElMessage.success('已提交')
  getList()
}

const handleSync = async (id: number) => {
  await SettleAccountApi.sync(id)
  ElMessage.success('已同步')
  getList()
}
</script>
