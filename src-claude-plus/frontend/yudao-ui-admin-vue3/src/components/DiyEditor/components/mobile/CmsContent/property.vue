<template>
  <ComponentContainerProperty v-model="formData.style">
    <el-form label-width="100px" :model="formData" class="m-t-8px">
      <!-- 基础配置 -->
      <el-card header="基础配置" class="property-group" shadow="never">
        <el-form-item label="选择板块" prop="sectionId">
          <el-select
            v-model="formData.sectionId"
            placeholder="请选择板块"
            clearable
            filterable
            :loading="sectionLoading"
            @change="onSectionChange"
            @focus="handleSectionFocus"
          >
            <el-option
              v-for="section in sectionList"
              :key="section.id"
              :label="section.name"
              :value="section.id"
            />
          </el-select>
        </el-form-item>

        <el-alert
          title="配置说明"
          type="info"
          :closable="false"
          show-icon
          class="m-t-8px"
        >
          <template #default>
            <div style="font-size: 13px; line-height: 1.6;">
              <p>其他配置项请在【内容管理】模块中统一配置：</p>
              <ul style=" padding-left: 20px;margin: 8px 0;">
                <li>分类管理：配置板块下的分类</li>
                <li>标签管理：配置分类下的标签</li>
                <li>文章管理：发布和管理文章内容</li>
              </ul>
              <p>移动端将自动显示该板块下的分类和标签筛选功能。</p>
            </div>
          </template>
        </el-alert>
      </el-card>
    </el-form>
  </ComponentContainerProperty>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useVModel } from '@vueuse/core'
import { CmsContentProperty } from './config'
import * as CmsSectionApi from '@/api/mall/promotion/cmsSection'

/** CMS内容组件属性面板 */
defineOptions({ name: 'CmsContentProperty' })

const props = defineProps<{ modelValue: CmsContentProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

// 板块列表
const sectionList = ref([])
const sectionLoading = ref(false)

// 获取板块列表
const getSectionList = async () => {
  sectionLoading.value = true
  try {
    console.log('开始获取板块列表...')
    const response = await CmsSectionApi.getCmsSectionList()
    console.log('板块列表响应:', response)

    // 根据实际返回的数据结构处理
    if (Array.isArray(response)) {
      sectionList.value = [...response]
    } else if (response && response.data && Array.isArray(response.data)) {
      sectionList.value = [...response.data]
    } else {
      sectionList.value = []
    }

    console.log('板块列表数据:', sectionList.value)
  } catch (error) {
    console.error('获取板块列表失败:', error)
    sectionList.value = []
  } finally {
    sectionLoading.value = false
  }
}

// 板块改变时触发
const onSectionChange = () => {
  console.log('板块已更改:', formData.value.sectionId)
}

// 板块选择框获得焦点时触发（确保数据已加载）
const handleSectionFocus = () => {
  if (sectionList.value.length === 0 && !sectionLoading.value) {
    getSectionList()
  }
}

// 初始化
onMounted(() => {
  getSectionList()
})
</script>

<style scoped lang="scss">
.property-group {
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 12px 16px;
    font-weight: 600;
  }

  :deep(.el-card__body) {
    padding: 16px;
  }
}
</style>
