<template>
  <el-form label-width="80px" :model="formData">
    <el-card header="基础配置" class="property-group" shadow="never">
      <el-form-item label="背景颜色" prop="bgColor">
        <el-color-picker v-model="formData.bgColor" show-alpha />
      </el-form-item>
      <el-form-item label="文字颜色" prop="textColor">
        <el-color-picker v-model="formData.textColor" show-alpha />
      </el-form-item>
      <el-form-item label="激活颜色" prop="activeColor">
        <el-color-picker v-model="formData.activeColor" show-alpha />
      </el-form-item>
      <el-form-item label="显示下划线" prop="showUnderline">
        <el-switch v-model="formData.showUnderline" />
      </el-form-item>
      <el-form-item v-if="formData.showUnderline" label="下划线颜色" prop="underlineColor">
        <el-color-picker v-model="formData.underlineColor" show-alpha />
      </el-form-item>
    </el-card>

    <el-card header="导航区域配置" class="property-group" shadow="never">
      <div class="mb-16px flex justify-between items-center">
        <el-button type="primary" size="small" @click="addNewSection">
          <Icon icon="ep:plus" class="mr-4px" /> 添加导航项
        </el-button>
        
        <el-button type="default" size="small" @click="showSortDialog = true">
          <Icon icon="ep:sort" class="mr-4px" /> 排序导航项
        </el-button>
      </div>
      
      <el-collapse v-model="activeSection" accordion>
        <template v-for="(section, index) in formData.contentSections" :key="section.id">
          <el-collapse-item :name="section.id">
            <template #title>
              <div class="flex items-center w-full">
                <span class="mr-8px">{{ index + 1 }}.</span>
                <span>{{ section.title || `导航${index + 1}` }}</span>
              </div>
            </template>
            
            <div class="section-form-content">
              <!-- 导航项配置 -->
              <el-form-item :label="'标题'" :prop="`contentSections[${index}].title`">
                <el-input v-model="section.title" placeholder="请输入导航文字" />
              </el-form-item>
              
              <el-form-item label="锚点ID" :prop="`contentSections[${index}].anchorId`">
                <el-input v-model="section.anchorId" placeholder="锚点ID会自动生成" disabled>
                  <template #prepend>
                    <el-tooltip content="锚点ID会自动生成，格式为content-索引">
                      <el-icon><Warning /></el-icon>
                    </el-tooltip>
                  </template>
                </el-input>
              </el-form-item>
              
              <!-- 内容区域组件配置 -->
              <div class="content-components-config">
                <div class="section-title">
                  <span>内容区域组件</span>
                  <el-button type="primary" link size="small" @click="openComponentSelector(index)">
                    <Icon icon="ep:plus" class="mr-4px" /> 添加组件
                  </el-button>
                </div>
                
                <div v-if="!section.components || section.components.length === 0" class="empty-components">
                  <Icon icon="ep:info-filled" :size="20" color="#909399" />
                  <span>暂无组件，请点击"添加组件"按钮</span>
                </div>
                
                <div v-else class="component-list">
                  <div v-for="(component, componentIndex) in section.components" :key="componentIndex" class="component-item">
                    <div class="component-header">
                      <span class="mr-8px">{{ componentIndex + 1 }}.</span>
                      <span>{{ getComponentName(component.type) }}</span>
                      <div class="component-actions">
                        <el-button 
                          v-if="componentIndex > 0"
                          type="primary" 
                          link 
                          size="small" 
                          @click="moveComponent(index, componentIndex, componentIndex - 1)"
                        >
                          <Icon icon="ep:arrow-up" />
                        </el-button>
                        <el-button 
                          v-if="componentIndex < section.components.length - 1"
                          type="primary" 
                          link 
                          size="small" 
                          @click="moveComponent(index, componentIndex, componentIndex + 1)"
                        >
                          <Icon icon="ep:arrow-down" />
                        </el-button>
                        <el-button 
                          type="primary" 
                          link 
                          size="small" 
                          @click="editComponent(index, componentIndex)"
                        >
                          <Icon icon="ep:edit" />
                        </el-button>
                        <el-button 
                          type="danger" 
                          link 
                          size="small" 
                          @click="removeComponent(index, componentIndex)"
                        >
                          <Icon icon="ep:delete" />
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 删除导航项按钮 -->
              <div class="section-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="removeSection(index)"
                  :disabled="formData.contentSections.length <= 1"
                >
                  删除此导航项
                </el-button>
              </div>
            </div>
          </el-collapse-item>
        </template>
      </el-collapse>
    </el-card>

    <el-card header="样式设置" class="property-group" shadow="never">
      <el-form-item label="内边距">
        <div class="flex flex-wrap gap-y-8px">
          <div class="w-full flex items-center">
            <div class="flex-1">上</div>
            <el-slider
              v-model="formData.style.paddingTop"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
          <div class="w-full flex items-center">
            <div class="flex-1">右</div>
            <el-slider
              v-model="formData.style.paddingRight"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
          <div class="w-full flex items-center">
            <div class="flex-1">下</div>
            <el-slider
              v-model="formData.style.paddingBottom"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
          <div class="w-full flex items-center">
            <div class="flex-1">左</div>
            <el-slider
              v-model="formData.style.paddingLeft"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
        </div>
      </el-form-item>
      <el-form-item label="外边距">
        <div class="flex flex-wrap gap-y-8px">
          <div class="w-full flex items-center">
            <div class="flex-1">上</div>
            <el-slider
              v-model="formData.style.marginTop"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
          <div class="w-full flex items-center">
            <div class="flex-1">右</div>
            <el-slider
              v-model="formData.style.marginRight"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
          <div class="w-full flex items-center">
            <div class="flex-1">下</div>
            <el-slider
              v-model="formData.style.marginBottom"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
          <div class="w-full flex items-center">
            <div class="flex-1">左</div>
            <el-slider
              v-model="formData.style.marginLeft"
              class="flex-3"
              :max="50"
              :min="0"
              show-input
              input-size="small"
            />
          </div>
        </div>
      </el-form-item>
      <el-form-item label="圆角">
        <el-slider
          v-model="formData.style.borderRadius"
          :max="20"
          :min="0"
          show-input
          input-size="small"
        />
      </el-form-item>
    </el-card>

    <el-card header="高级设置" class="property-group" shadow="never">
      <el-form-item label="启用吸顶" prop="settings.enableSticky">
        <el-switch v-model="formData.settings.enableSticky" />
      </el-form-item>
      
      <el-form-item v-if="formData.settings.enableSticky" label="吸顶偏移" prop="settings.stickyOffset">
        <el-input-number 
          v-model="formData.settings.stickyOffset" 
          :min="0" 
          :max="100" 
          :step="1"
          size="small"
        />
        <span class="ml-8px text-gray-400">像素(px)</span>
      </el-form-item>
      
      <el-form-item label="显示指示器" prop="settings.showIndicator">
        <el-switch v-model="formData.settings.showIndicator" />
      </el-form-item>
      
      <el-form-item label="自动更新" prop="settings.autoUpdateActive">
        <el-switch v-model="formData.settings.autoUpdateActive" />
        <div class="text-gray-400 text-12px mt-4px">
          滚动时自动更新激活的导航项
        </div>
      </el-form-item>
      
      <el-form-item label="滚动时长" prop="settings.scrollDuration">
        <el-input-number 
          v-model="formData.settings.scrollDuration" 
          :min="100" 
          :max="1000" 
          :step="50"
          size="small"
        />
        <span class="ml-8px text-gray-400">毫秒(ms)</span>
      </el-form-item>
    </el-card>
    
    <!-- 组件选择器对话框 -->
    <el-dialog
      v-model="componentSelectorVisible"
      title="添加组件"
      width="600px"
      destroy-on-close
    >
      <div class="component-selector">
        <div class="component-category" v-for="(category, index) in componentLibrary" :key="index">
          <div class="category-title">{{ category.name }}</div>
          <div class="category-components">
            <div
              v-for="componentType in category.components"
              :key="componentType"
              class="component-option"
              @click="selectComponent(componentType)"
            >
              <Icon :icon="getComponentIcon(componentType)" :size="24" />
              <span>{{ getComponentName(componentType) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 添加导航项排序对话框 -->
    <el-dialog
      v-model="showSortDialog"
      title="导航项排序"
      width="500px"
    >
      <el-table :data="formData.contentSections" row-key="id">
        <el-table-column label="序号" width="80">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        
        <el-table-column label="导航标题">
          <template #default="{ row }">
            {{ row.title || '未命名导航' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="{ $index }">
            <el-button 
              :disabled="$index === 0" 
              type="primary" 
              link 
              @click="moveSection($index, $index - 1)"
            >
              <Icon icon="ep:arrow-up" />
            </el-button>
            <el-button 
              :disabled="$index === formData.contentSections.length - 1" 
              type="primary" 
              link 
              @click="moveSection($index, $index + 1)"
            >
              <Icon icon="ep:arrow-down" />
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showSortDialog = false">取消</el-button>
          <el-button type="primary" @click="showSortDialog = false">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加组件编辑对话框 -->
    <el-dialog
      v-model="componentEditorVisible"
      :title="`编辑${currentComponentName}`"
      width="750px"
      destroy-on-close
      append-to-body
    >
      <div v-if="currentComponent" class="component-edit-container">
        <!-- 组件预览 -->
        <div class="component-preview-panel">
          <div class="preview-title">组件预览</div>
          <div class="preview-content">
            <!-- 使用动态组件加载预览 -->
            <div v-if="hasComponentPreview" class="preview-wrapper">
              <component 
                :is="resolveComponentName(currentComponent.type)"
                :property="currentComponent.property"
              />
            </div>
            <div v-else class="preview-placeholder">
              <Icon icon="ep:picture" :size="32" color="#dcdfe6" />
              <span>该组件无预览</span>
            </div>
          </div>
        </div>
        
        <!-- 组件属性编辑 -->
        <div class="component-property-panel">
          <div class="property-title">组件属性</div>
          <div class="property-content">
            <!-- 使用动态组件加载属性编辑器 -->
            <div v-if="hasPropertyEditor" class="property-editor">
              <component 
                :is="resolvePropertyName(currentComponent.type)"
                v-model="currentComponent.property"
              />
            </div>
            <div v-else class="property-placeholder">
              <el-alert
                title="无法加载属性编辑器"
                type="warning"
                :closable="false"
                show-icon
              >
                <p>该组件类型不支持属性编辑或编辑器未注册</p>
              </el-alert>
              
              <!-- 添加JSON编辑器作为备选方案 -->
              <div class="json-editor-container">
                <div class="json-editor-title">JSON编辑</div>
                <el-input
                  v-model="componentJsonString"
                  type="textarea"
                  :rows="10"
                  placeholder="请输入JSON格式的组件属性"
                  @change="updateComponentFromJson"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="componentEditorVisible = false">取消</el-button>
          <el-button type="primary" @click="saveComponentEdit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </el-form>
</template>

<script setup lang="ts">
import { StickyNavBarProperty, StickyNavContentSection, StickyNavSectionComponent } from './config'
import { useVModel } from '@vueuse/core'
import { Warning } from '@element-plus/icons-vue'
import { PAGE_LIBS } from '@/components/DiyEditor/util'
import { componentConfigs, components } from '@/components/DiyEditor/components/mobile'
import { v4 as uuidv4 } from 'uuid'
import { ElMessageBox } from 'element-plus'
import { ref, onMounted, computed } from 'vue'

// 吸顶导航属性面板
defineOptions({ name: 'StickyNavBarProperty' })

const props = defineProps<{ modelValue: StickyNavBarProperty }>()
const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

// 当前激活的导航区域
const activeSection = ref<string>('')

// 组件选择器相关
const componentSelectorVisible = ref(false)
const currentSectionIndex = ref(0)
const currentComponentIndex = ref(0)

// 组件编辑相关
const componentEditorVisible = ref(false)
const currentComponent = ref<StickyNavSectionComponent | null>(null)
const currentComponentName = ref('')
const componentJsonString = ref('')

// 检查组件是否有预览
const hasComponentPreview = computed(() => {
  if (!currentComponent.value?.type) return false
  return !!components[currentComponent.value.type]
})

// 检查组件是否有属性编辑器
const hasPropertyEditor = computed(() => {
  if (!currentComponent.value?.type) return false
  return !!components[`${currentComponent.value.type}Property`]
})

// 解析组件名称
const resolveComponentName = (type: string) => {
  return components[type] || null
}

// 解析属性编辑器名称
const resolvePropertyName = (type: string) => {
  return components[`${type}Property`] || null
}

// 更新JSON字符串
const updateComponentFromJson = () => {
  if (!currentComponent.value) return
  
  try {
    const parsedJson = JSON.parse(componentJsonString.value)
    currentComponent.value.property = parsedJson
  } catch (error) {
    ElMessageBox.alert('JSON格式错误，请检查后重试', '错误', {
      type: 'error',
      confirmButtonText: '确定'
    })
  }
}

// 组件库
const componentLibrary = PAGE_LIBS.map(category => {
  // 过滤掉不存在的组件
  const availableComponents = category.components.filter(componentType => {
    return !!componentConfigs[componentType];
  });
  
  return {
    ...category,
    components: availableComponents
  };
}).filter(category => category.components.length > 0); // 过滤掉没有可用组件的分类

// 获取组件名称
const getComponentName = (componentType: string) => {
  const config = componentConfigs[componentType]
  return config?.name || componentType
}

// 获取组件图标
const getComponentIcon = (componentType: string) => {
  const config = componentConfigs[componentType]
  return config?.icon || 'ep:question-filled'
}

// 添加新的导航区域
const addNewSection = () => {
  // 生成唯一ID
  const newId = `nav-${uuidv4().substring(0, 8)}`;
  const newIndex = formData.value.contentSections.length;
  
  const newSection: StickyNavContentSection = {
    id: newId,
    title: `导航${newIndex + 1}`,
    anchorId: `content-${newIndex}`,
    components: [] // 确保初始化components数组
  }
  
  formData.value.contentSections.push(newSection);
  
  // 自动展开新添加的导航项
  setTimeout(() => {
    activeSection.value = newId;
  }, 100);
}

// 删除导航区域
const removeSection = (index: number) => {
  if (formData.value.contentSections.length <= 1) {
    return
  }
  
  ElMessageBox.confirm(
    '确定要删除此导航项吗？相关的内容区域组件也将被删除。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    formData.value.contentSections.splice(index, 1)
  }).catch(() => {})
}

// 打开组件选择器
const openComponentSelector = (sectionIndex: number) => {
  currentSectionIndex.value = sectionIndex
  componentSelectorVisible.value = true
}

// 选择组件
const selectComponent = (componentType: string) => {
  const config = componentConfigs[componentType]
  if (!config) {
    ElMessageBox.alert(`组件 ${componentType} 不存在或未注册`, '错误', {
      type: 'error',
      confirmButtonText: '确定'
    });
    return;
  }
  
  // 确保components数组已初始化
  if (!formData.value.contentSections[currentSectionIndex.value].components) {
    formData.value.contentSections[currentSectionIndex.value].components = []
  }
  
  const newComponent: StickyNavSectionComponent = {
    type: componentType,
    property: JSON.parse(JSON.stringify(config.property))
  }
  
  formData.value.contentSections[currentSectionIndex.value].components.push(newComponent)
  componentSelectorVisible.value = false
}

// 删除组件
const removeComponent = (sectionIndex: number, componentIndex: number) => {
  formData.value.contentSections[sectionIndex].components.splice(componentIndex, 1)
}

// 添加导航项排序对话框
const showSortDialog = ref(false)

// 移动导航项
const moveSection = (fromIndex: number, toIndex: number) => {
  if (fromIndex === toIndex) return
  
  const sections = formData.value.contentSections
  const section = sections[fromIndex]
  
  sections.splice(fromIndex, 1)
  sections.splice(toIndex, 0, section)
}

// 移动组件
const moveComponent = (sectionIndex: number, fromIndex: number, toIndex: number) => {
  if (fromIndex === toIndex) return
  
  const sections = formData.value.contentSections
  const section = sections[sectionIndex]
  const component = section.components[fromIndex]
  
  section.components.splice(fromIndex, 1)
  section.components.splice(toIndex, 0, component)
}

// 编辑组件
const editComponent = (sectionIndex: number, componentIndex: number) => {
  // 保存当前编辑的区域索引
  currentSectionIndex.value = sectionIndex;
  
  // 获取当前组件的引用
  const component = formData.value.contentSections[sectionIndex].components[componentIndex];
  if (!component) return;
  
  // 检查组件类型是否存在
  const config = componentConfigs[component.type];
  if (!config) {
    ElMessageBox.alert(`组件 ${component.type} 不存在或未注册，无法编辑`, '错误', {
      type: 'error',
      confirmButtonText: '确定'
    });
    return;
  }
  
  // 创建组件的深拷贝，避免直接修改原始数据
  currentComponent.value = {
    type: component.type,
    property: JSON.parse(JSON.stringify(component.property))
  };
  
  // 保存组件名称和索引
  currentComponentName.value = getComponentName(component.type);
  currentComponentIndex.value = componentIndex;
  
  // 初始化JSON字符串，用于JSON编辑器
  try {
    componentJsonString.value = JSON.stringify(currentComponent.value.property, null, 2);
  } catch (e) {
    componentJsonString.value = '{}';
  }
  
  // 显示编辑对话框
  componentEditorVisible.value = true;
}

// 保存组件编辑
const saveComponentEdit = () => {
  if (currentComponent.value && currentSectionIndex.value !== undefined) {
    try {
      // 使用保存的索引直接更新组件
      const section = formData.value.contentSections[currentSectionIndex.value];
      if (section && section.components && currentComponentIndex.value !== undefined) {
        // 使用深拷贝更新组件属性
        section.components[currentComponentIndex.value] = {
          type: currentComponent.value.type,
          property: JSON.parse(JSON.stringify(currentComponent.value.property))
        };
      }
    } catch (error) {
      console.error('保存组件编辑失败:', error);
    }
  }
  
  // 关闭对话框
  componentEditorVisible.value = false;
}

// 初始化
onMounted(() => {
  // 确保contentSections字段存在
  if (!formData.value.contentSections) {
    formData.value.contentSections = [];
  }
  
  // 确保每个导航项都有components数组和正确的锚点ID格式
  formData.value.contentSections.forEach((section, index) => {
    if (!section.components) {
      section.components = [];
    }
    
    // 更新锚点ID为统一格式
    section.anchorId = `content-${index}`;
  });
  
  // 如果有导航项，默认展开第一个
  if (formData.value.contentSections.length > 0) {
    activeSection.value = formData.value.contentSections[0].id;
  }
  
  // 确保settings字段存在
  if (!formData.value.settings) {
    formData.value.settings = {
      enableSticky: true,
      stickyOffset: 0,
      showIndicator: true,
      autoUpdateActive: true,
      scrollDuration: 300
    };
  }
});
</script>

<style scoped lang="scss">
.drag-handle {
  color: #909399;
  cursor: move;
}

.section-form-content {
  padding: 10px 0;
}

.content-components-config {
  padding: 16px;
  margin-top: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    font-weight: 500;
  }
  
  .empty-components {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 30px 0;
    color: #909399;
    background-color: #f8f8f8;
    border-radius: 4px;
    
    span {
      margin-top: 8px;
      font-size: 14px;
    }
  }
  
  .component-list {
    .component-item {
      margin-bottom: 8px;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      
      .component-header {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        background-color: #f8f8f8;
        
        span {
          &:first-child {
            font-weight: 500;
            color: #909399;
          }
          
          &:nth-child(2) {
            flex: 1;
            margin-left: 8px;
          }
        }
        
        .component-actions {
          display: flex;
        }
      }
      
      &:hover {
        border-color: #dcdfe6;
        box-shadow: 0 2px 6px rgb(0 0 0 / 5%);
        
        .component-header {
          background-color: #f5f7fa;
        }
      }
    }
  }
}

.section-actions {
  display: flex;
  margin-top: 16px;
  justify-content: center;
}

.component-selector {
  max-height: 400px;
  overflow-y: auto;
  
  .component-category {
    margin-bottom: 16px;
    
    .category-title {
      padding-bottom: 8px;
      margin-bottom: 8px;
      font-weight: 500;
      border-bottom: 1px solid #ebeef5;
    }
    
    .category-components {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      
      .component-option {
        display: flex;
        width: 80px;
        padding: 12px 0;
        cursor: pointer;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        transition: all 0.3s;
        flex-direction: column;
        align-items: center;
        
        &:hover {
          background-color: #f5f7fa;
          border-color: #dcdfe6;
        }
        
        span {
          margin-top: 8px;
          font-size: 12px;
          text-align: center;
        }
      }
    }
  }
}

.component-edit-container {
  display: flex;
  height: 60vh;
  
  .component-preview-panel {
    display: flex;
    width: 40%;
    padding: 0 16px;
    border-right: 1px solid #ebeef5;
    flex-direction: column;
    
    .preview-title {
      padding-bottom: 8px;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 500;
      border-bottom: 1px solid #ebeef5;
    }
    
    .preview-content {
      display: flex;
      padding: 16px;
      overflow: auto;
      background-color: #f5f7fa;
      border-radius: 4px;
      flex: 1;
      justify-content: center;
      align-items: flex-start;
      
      .preview-wrapper {
        width: 100%;
        height: 100%;
        overflow: auto;
        background-color: #fff;
        border-radius: 4px;
        box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
      }
      
      .preview-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: #909399;
        
        span {
          margin-top: 8px;
          font-size: 14px;
        }
      }
    }
  }
  
  .component-property-panel {
    display: flex;
    width: 60%;
    padding: 0 16px;
    flex-direction: column;
    
    .property-title {
      padding-bottom: 8px;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 500;
      border-bottom: 1px solid #ebeef5;
    }
    
    .property-content {
      padding-right: 8px;
      overflow: auto;
      flex: 1;
      
      .property-editor {
        padding: 0 10px;
      }
      
      .property-placeholder {
        padding: 10px 0;
        
        .json-editor-container {
          margin-top: 20px;
          
          .json-editor-title {
            margin-bottom: 10px;
            font-size: 14px;
            font-weight: 500;
            color: #606266;
          }
        }
      }
      
      &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #c0c4cc;
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-track {
        background: #f5f7fa;
      }
    }
  }
}
</style> 