<template>
  <div class="cms-category-bar">
    <div class="filter-container">
      <!-- 分类筛选区域 -->
      <div class="category-section">
        <div class="section-title">分类</div>
        <div class="category-container">
          <el-tag
            v-for="category in categories"
            :key="category.id"
            :type="selectedCategoryIds.includes(category.id) ? 'primary' : 'info'"
            class="category-tag"
            @click="selectCategory(category.id)"
            closable
            @close="removeCategory(category.id)"
          >
            {{ category.name }}
          </el-tag>

          <!-- 新增分类按钮 -->
          <el-button
            type="primary"
            plain
            size="small"
            @click="handleAddCategory"
            class="add-category-button"
          >
            <Icon icon="ep:plus" class="mr-0" />
          </el-button>
        </div>
      </div>

      <!-- 标签筛选区域 - 只有选择了分类才显示 -->
      <div v-if="props.selectedCategoryIds.length > 0" class="tag-section">
        <div class="section-title">
          标签
          <span class="cascade-hint">
            ({{ categories.find(c => c.id === props.selectedCategoryIds[0])?.name }}分类下的标签)
          </span>
        </div>
        <div class="tag-container">
          <el-tag
            v-for="tag in tags"
            :key="tag.id"
            :type="selectedTagIds.includes(tag.id) ? 'primary' : 'info'"
            class="tag-item"
            @click="toggleTag(tag.id)"
          >
            {{ tag.name }}
          </el-tag>
          <div v-if="tags.length === 0" class="empty-tags">
            该分类下暂无标签
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

/** 分类展示组件 */
defineOptions({ name: 'CmsCategoryBar' })

// 定义Props
const props = defineProps({
  categories: {
    type: Array,
    required: true,
    default: () => []
  },
  selectedCategoryIds: {
    type: Array,
    default: () => []
  },
  tags: {
    type: Array,
    default: () => []
  },
  selectedTagIds: {
    type: Array,
    default: () => []
  }
})

// 定义Emits
const emit = defineEmits(['category-change', 'tag-change', 'add-category', 'delete-category'])

// 选择分类（单选）
const selectCategory = (categoryId) => {
  // 如果已选中，则取消选择；否则，只选择这一个分类
  const selectedIds = props.selectedCategoryIds.includes(categoryId) ? [] : [categoryId]
  emit('category-change', selectedIds)
}

// 移除分类
const removeCategory = async (categoryId) => {
  // 发出删除事件，让父组件处理
  emit('delete-category', categoryId)
}

// 处理新增分类
const handleAddCategory = () => {
  emit('add-category')
}

// 切换标签选择（多选）
const toggleTag = (tagId) => {
  const selectedIds = [...props.selectedTagIds]
  const index = selectedIds.indexOf(tagId)

  if (index > -1) {
    // 如果已选中，则移除
    selectedIds.splice(index, 1)
  } else {
    // 如果未选中，则添加
    selectedIds.push(tagId)
  }

  emit('tag-change', selectedIds)
}
</script>

<style scoped>


/* 响应式设计 */
@media (width <= 768px) {
  .filter-container {
    gap: 12px;
  }

  .category-container,
  .tag-container {
    gap: 6px;
  }

  .section-title {
    margin-bottom: 6px;
  }

  .cascade-hint {
    display: block;
    margin-top: 4px;
    margin-left: 0;
  }
}

.cms-category-bar {
  padding: 15px;
  margin-bottom: 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-top: none;
  border-radius: 0 0 4px 4px;
}

.filter-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

/* 分类筛选区域 */
.category-section {
  display: flex;
  flex-direction: column;
}

.category-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.category-tag {
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.category-tag:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

.add-category-button {
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
}

/* 标签筛选区域 */
.tag-section {
  display: flex;
  flex-direction: column;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-item {
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.tag-item:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

.tag-item.el-tag--primary {
  color: white;
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.cascade-hint {
  margin-left: 8px;
  font-size: 12px;
  font-weight: normal;
  color: var(--el-color-info);
}

.empty-tags {
  font-size: 12px;
  font-style: italic;
  color: var(--el-text-color-placeholder);
}
</style>
