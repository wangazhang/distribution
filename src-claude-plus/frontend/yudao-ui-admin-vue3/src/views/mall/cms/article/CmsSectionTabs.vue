<template>
  <div class="cms-section-tabs">
    <el-tabs
      :value="activeSectionId"
      type="border-card"
      @tab-change="handleTabChange"
      class="section-tabs"
    >
      <el-tab-pane
        v-for="section in sections"
        :key="section.id"
        :label="section.name"
        :name="section.id"
      >
        <!-- Tab内容将由父组件填充 -->
      </el-tab-pane>

      <!-- 新增板块Tab -->
      <el-tab-pane
        key="add-section"
        name="add-section"
        class="add-section-tab"
      >
        <template #label>
          <el-button
            type="primary"
            plain
            size="small"
            @click="handleAddSection"
            class="add-section-button"
          >
            <Icon icon="ep:plus" class="mr-1" />

          </el-button>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

/** 板块Tabs组件 */
defineOptions({ name: 'CmsSectionTabs' })

// 定义Props
const props = defineProps({
  sections: {
    type: Array,
    required: true,
    default: () => []
  },
  activeSectionId: {
    type: [Number, String],
    default: null
  }
})

// 定义Emits
const emit = defineEmits(['change', 'add'])

// 处理Tab切换
const handleTabChange = (sectionId) => {
  // 如果点击的是新增按钮Tab，不触发change事件
  if (sectionId === 'add-section') {
    return
  }
  emit('change', sectionId)
}

// 处理新增板块
const handleAddSection = (event) => {
  // 阻止事件冒泡，避免触发tab-change
  event.stopPropagation()
  emit('add')
}
</script>

<style scoped>
.cms-section-tabs {
  margin-bottom: 0;
}

.section-tabs {
  border: 1px solid var(--el-border-color);
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.section-tabs :deep(.el-tabs__content) {
  display: none;
}

.add-section-tab {
  padding: 0;
}

.add-section-button {
  color: var(--el-color-primary);
  background: transparent;
  border: none;
}
</style>
