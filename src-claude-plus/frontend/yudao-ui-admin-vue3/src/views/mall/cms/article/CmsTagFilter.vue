<template>
  <div class="cms-tag-filter" v-if="tags.length > 0">
    <el-select
      v-model="localSelectedTagIds"
      multiple
      collapse-tags
      collapse-tags-tooltip
      placeholder="标签筛选"
      clearable
      @change="handleTagChange"
      class="tag-filter-select"
    >
      <el-option
        v-for="tag in tags"
        :key="tag.id"
        :label="tag.name"
        :value="tag.id"
      >
        <span class="tag-option">
          <el-tag size="small" type="info">{{ tag.name }}</el-tag>
        </span>
      </el-option>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

/** 标签筛选组件 */
defineOptions({ name: 'CmsTagFilter' })

// 定义Props
const props = defineProps({
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
const emit = defineEmits(['change'])

// 本地选中的标签IDs
const localSelectedTagIds = ref([...props.selectedTagIds])

// 监听外部selectedTagIds变化
watch(
  () => props.selectedTagIds,
  (newVal) => {
    localSelectedTagIds.value = [...newVal]
  }
)

// 处理标签变化
const handleTagChange = (tagIds) => {
  emit('change', tagIds)
}
</script>

<style scoped>


/* 响应式设计 */
@media (width <= 768px) {
  .cms-tag-filter {
    margin-top: 10px;
    margin-left: 0;
  }

  .tag-filter-select {
    width: 100%;
  }
}

.cms-tag-filter {
  display: inline-block;
  margin-left: 15px;
}

.tag-filter-select {
  width: 200px;
}

.tag-option {
  display: flex;
  align-items: center;
}
</style>
