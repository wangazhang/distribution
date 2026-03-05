<template>
  <div class="cms-content-container" :style="getContainerStyle">
    <!-- 分类筛选 - 顶部显示 -->
    <div v-if="categoryList.length" class="filter-bar">
      <div class="filter-scroll">
        <div
class="filter-item category-item"
             :class="{ active: selectedCategory === null }"
             @click="selectCategory(null)">
          全部
        </div>
        <div
v-for="category in categoryList"
             :key="category.id"
             class="filter-item category-item"
             :class="{ active: selectedCategory === category.id }"
             @click="selectCategory(category.id)">
          {{ category.name }}
        </div>
      </div>
    </div>

    <!-- 标签筛选 - 分类下方显示 -->
    <div v-if="selectedCategory && tagList.length" class="filter-bar tag-bar">
      <div class="filter-scroll">
        <div
class="filter-item tag-item"
             :class="{ active: selectedTag === null }"
             @click="selectTag(null)">
          全部
        </div>
        <div
v-for="tag in tagList"
             :key="tag.id"
             class="filter-item tag-item"
             :class="{ active: selectedTag === tag.id }"
             @click="selectTag(tag.id)">
          {{ tag.name }}
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="article-list">
      <!-- 文章类卡片 -->
      <template v-if="sectionType === 'article'">
        <div v-for="article in articleList" :key="article.id" class="article-item">
          <!-- 封面图片 -->
          <div v-if="article.coverImages?.length" class="article-cover">
            <img :src="article.coverImages[0]" :alt="article.title" />
          </div>

          <div class="article-content">
            <!-- 标题 -->
            <h3 class="article-title">{{ article.title }}</h3>

            <!-- 副标题 -->
            <h4 v-if="article.subtitle" class="article-subtitle">{{ article.subtitle }}</h4>

            <!-- 发布信息 -->
            <div class="article-meta">
              <span v-if="article.authorName" class="author">{{ article.authorName }}</span>
              <span v-if="article.isOfficial" class="official-badge">官方</span>
            </div>

            <!-- 统计信息 -->
            <div class="article-stats">
              <span class="stat">
                <el-icon><View /></el-icon>
                <span class="stat-text">{{ article.viewCount || 0 }}</span>
              </span>
              <span class="stat">
                <el-icon><Star /></el-icon>
                <span class="stat-text">{{ article.likeCount || 0 }}</span>
              </span>
            </div>
          </div>
        </div>
      </template>

      <!-- 动态类卡片 -->
      <template v-else-if="sectionType === 'dynamic'">
        <div v-for="article in articleList" :key="article.id" class="dynamic-item">
          <!-- 头像和用户信息 -->
          <div class="dynamic-header">
            <div class="avatar-wrapper">
              <img :src="article.authorAvatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg'" class="avatar" />
            </div>
            <div class="user-info">
              <div class="user-name">
                <span>{{ article.authorName || '匿名用户' }}</span>
                <span v-if="article.isOfficial" class="official-badge">官方</span>
              </div>
              <div class="publish-time">{{ article.publishTime }}</div>
            </div>
          </div>

          <!-- 内容 -->
          <div class="dynamic-content">{{ article.title }}</div>

          <!-- 图片网格（最多显示3张） -->
          <div v-if="article.coverImages?.length" class="image-grid">
            <img
              v-for="(img, idx) in article.coverImages.slice(0, 3)"
              :key="idx"
              :src="img"
              class="grid-image"
            />
          </div>

          <!-- 底部统计 -->
          <div class="dynamic-footer">
            <span class="stat">
              <el-icon><View /></el-icon>
              <span class="stat-text">{{ article.viewCount || 0 }}</span>
            </span>
            <span class="stat">
              <el-icon><Star /></el-icon>
              <span class="stat-text">{{ article.likeCount || 0 }}</span>
            </span>
          </div>
        </div>
      </template>

      <!-- 课程类卡片 -->
      <template v-else-if="sectionType === 'course'">
        <div v-for="article in articleList" :key="article.id" class="course-item">
          <!-- 课程封面 -->
          <div class="course-cover">
            <img :src="article.coverImages?.[0] || '/static/images/default-course.png'" :alt="article.title" />
          </div>

          <div class="course-content">
            <!-- 标题 -->
            <h3 class="course-title">{{ article.title }}</h3>

            <!-- 副标题 -->
            <h4 v-if="article.subtitle" class="course-subtitle">{{ article.subtitle }}</h4>

            <!-- 讲师信息 -->
            <div class="course-meta">
              <span class="instructor">{{ article.authorName || '未知讲师' }}</span>
              <span v-if="article.isOfficial" class="official-badge">官方</span>
            </div>

            <!-- 学习人数 -->
            <div class="course-stats">
              <span class="stat">
                <el-icon><View /></el-icon>
                <span class="stat-text">{{ article.viewCount || 0 }}人学习</span>
              </span>
            </div>
          </div>
        </div>
      </template>

      <!-- 自定义类型 - 简单布局 -->
      <template v-else>
        <div v-for="article in articleList" :key="article.id" class="simple-item">
          <h3 class="simple-title">{{ article.title }}</h3>
          <h4 v-if="article.subtitle" class="simple-subtitle">{{ article.subtitle }}</h4>
        </div>
      </template>
    </div>

    <!-- 空状态 -->
    <div v-if="!hasContent" class="empty-placeholder">
      <p>{{ getEmptyText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { CmsContentProperty } from './config'
import { View, Star, Share, StarFilled } from '@element-plus/icons-vue'
import * as SectionApi from '@/api/mall/cms/section'

// 定义属性
const props = defineProps<{
  property: CmsContentProperty
}>()

// 数据状态
const articleList = ref<any[]>([])
const categoryList = ref<any[]>([])
const tagList = ref<any[]>([])
const selectedCategory = ref<number | null>(null)
const selectedTag = ref<number | null>(null)
const sectionInfo = ref<any>(null)

// 计算属性
const hasContent = computed(() => {
  return articleList.value.length > 0
})

const sectionType = computed(() => {
  return sectionInfo.value?.type || 'article'
})

const sectionConfig = computed(() => {
  return sectionInfo.value?.config || {}
})

const getContainerStyle = computed(() => {
  return {
    backgroundColor: props.property.style?.bgColor || '#fff',
    marginLeft: props.property.style?.marginLeft + 'px',
    marginRight: props.property.style?.marginRight + 'px',
    marginBottom: props.property.style?.marginBottom + 'px',
    borderRadius: props.property.style?.borderRadius + 'px',
    padding: props.property.style?.padding + 'px'
  }
})

const getEmptyText = computed(() => {
  if (!props.property.sectionId) return '请在右侧配置面板选择板块'
  return '暂无文章'
})

// 选择分类
const selectCategory = async (categoryId: number | null) => {
  selectedCategory.value = categoryId
  selectedTag.value = null
  // 加载该分类下的标签
  await loadTags(categoryId)
  loadArticles()
}

// 选择标签
const selectTag = (tagId: number | null) => {
  selectedTag.value = tagId
  loadArticles()
}

// 加载文章列表
const loadArticles = async () => {
  if (!props.property.sectionId) return

  // 预览模式：显示示例数据
  articleList.value = [
    {
      id: 1,
      title: '示例文章标题1',
      subtitle: '这是一个示例副标题',
      coverImages: ['https://via.placeholder.com/400x200'],
      authorName: '作者1',
      isOfficial: true,
      viewCount: 1234,
      likeCount: 56
    },
    {
      id: 2,
      title: '示例文章标题2',
      subtitle: '这是另一个示例副标题',
      coverImages: ['https://via.placeholder.com/400x200'],
      authorName: '作者2',
      isOfficial: false,
      viewCount: 567,
      likeCount: 23
    }
  ]
}

// 加载分类列表
const loadCategories = async () => {
  if (!props.property.sectionId) return

  // 预览模式：显示示例数据
  categoryList.value = [
    { id: 1, name: '分类1' },
    { id: 2, name: '分类2' },
    { id: 3, name: '分类3' }
  ]
}

// 加载标签列表
const loadTags = async (categoryId: number | null) => {
  if (!categoryId) {
    tagList.value = []
    return
  }

  // 预览模式：显示示例数据
  tagList.value = [
    { id: 1, name: '标签1' },
    { id: 2, name: '标签2' },
    { id: 3, name: '标签3' }
  ]
}

// 加载板块信息
const loadSectionInfo = async () => {
  if (!props.property.sectionId) return

  try {
    const res = await SectionApi.getCmsSection(props.property.sectionId)
    if (res) {
      sectionInfo.value = res
    }
  } catch (error) {
    console.error('加载板块信息失败:', error)
  }
}

// 加载内容
const loadContent = async () => {
  if (props.property.sectionId) {
    await loadSectionInfo()
    await loadCategories()
    await loadArticles()
  }
}

// 监听属性变化
watch(() => props.property, loadContent, { deep: true, immediate: true })
</script>

<style lang="scss" scoped>
.cms-content-container {
  min-height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.filter-bar {
  padding: 10px 0;
  background: #fff;
  border-bottom: 1px solid #eee;

  .filter-scroll {
    display: flex;
    padding: 0 10px;
    overflow-x: auto;
    gap: 10px;

    &::-webkit-scrollbar {
      height: 4px;
    }
  }

  .filter-item {
    padding: 6px 14px;
    font-size: 13px;
    color: #666;
    white-space: nowrap;
    cursor: pointer;
    background: #f5f5f5;
    border-radius: 16px;
    transition: all 0.3s;
    flex-shrink: 0;

    &:hover {
      background: #e8e8e8;
    }

    &.category-item.active {
      font-weight: bold;
      color: #fff;
      background: #ff6b00;
    }

    &.tag-item.active {
      font-weight: bold;
      color: #ff6b00;
      background: #ffead9;
    }
  }

  &.tag-bar {
    border-top: 1px solid #eee;
  }
}

.article-list {
  padding: 10px;

  .article-item {
    margin-bottom: 10px;
    overflow: hidden;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgb(0 0 0 / 10%);

    .article-cover {
      width: 100%;
      height: 150px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .article-content {
      padding: 10px;

      .article-title {
        display: -webkit-box;
        margin: 0 0 6px;
        overflow: hidden;
        font-size: 15px;
        font-weight: 600;
        line-height: 1.4;
        color: #333;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
      }

      .article-subtitle {
        display: -webkit-box;
        margin: 0 0 8px;
        overflow: hidden;
        font-size: 13px;
        line-height: 1.4;
        color: #999;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 1;
      }

      .article-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        font-size: 12px;

        .author {
          color: #666;
        }

        .official-badge {
          padding: 2px 6px;
          font-size: 11px;
          color: #fff;
          background: #ff6b00;
          border-radius: 4px;
        }
      }

      .article-stats {
        display: flex;
        gap: 12px;
        font-size: 12px;
        color: #999;

        .stat {
          display: flex;
          align-items: center;
          gap: 4px;

          .el-icon {
            font-size: 14px;
          }

          .stat-text {
            color: #999;
          }
        }
      }
    }
  }
}

.empty-placeholder {
  padding: 40px 20px;
  margin: 10px;
  font-size: 13px;
  color: #999;
  text-align: center;
  background: #fff;
  border-radius: 8px;
}

/* 动态类卡片样式 */
.dynamic-item {
  padding: 12px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgb(0 0 0 / 10%);

  .dynamic-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .avatar-wrapper {
      margin-right: 10px;

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        object-fit: cover;
      }
    }

    .user-info {
      flex: 1;

      .user-name {
        display: flex;
        margin-bottom: 4px;
        font-size: 14px;
        font-weight: 600;
        color: #333;
        align-items: center;
        gap: 6px;

        .official-badge {
          padding: 2px 6px;
          font-size: 10px;
          font-weight: 500;
          color: #fff;
          background: linear-gradient(135deg, #ff6b00 0%, #ff8c00 100%);
          border-radius: 4px;
        }
      }

      .publish-time {
        font-size: 11px;
        color: #999;
      }
    }
  }

  .dynamic-content {
    display: -webkit-box;
    margin-bottom: 12px;
    overflow: hidden;
    font-size: 14px;
    line-height: 1.6;
    color: #333;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
  }

  .image-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 4px;
    margin-bottom: 12px;

    .grid-image {
      width: 100%;
      height: 100px;
      object-fit: cover;
      border-radius: 4px;
    }
  }

  .dynamic-footer {
    display: flex;
    gap: 12px;
    padding-top: 8px;
    border-top: 1px solid #f5f5f5;

    .stat {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #999;

      .el-icon {
        font-size: 14px;
      }

      .stat-text {
        color: #999;
      }
    }
  }
}

/* 课程类卡片样式 */
.course-item {
  margin-bottom: 10px;
  overflow: hidden;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgb(0 0 0 / 10%);

  .course-cover {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 56.25%; /* 16:9 */
    overflow: hidden;

    img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .course-content {
    padding: 12px;

    .course-title {
      display: -webkit-box;
      margin: 0 0 6px;
      overflow: hidden;
      font-size: 15px;
      font-weight: 600;
      line-height: 1.4;
      color: #333;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
    }

    .course-subtitle {
      display: -webkit-box;
      margin: 0 0 8px;
      overflow: hidden;
      font-size: 13px;
      line-height: 1.4;
      color: #999;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 1;
    }

    .course-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      font-size: 12px;

      .instructor {
        color: #666;
      }

      .official-badge {
        padding: 2px 6px;
        font-size: 10px;
        font-weight: 500;
        color: #fff;
        background: linear-gradient(135deg, #ff6b00 0%, #ff8c00 100%);
        border-radius: 4px;
      }
    }

    .course-stats {
      display: flex;
      gap: 12px;
      font-size: 12px;
      color: #999;

      .stat {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          font-size: 14px;
        }

        .stat-text {
          color: #666;
        }
      }
    }
  }
}

/* 简单布局样式 */
.simple-item {
  padding: 12px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgb(0 0 0 / 10%);

  .simple-title {
    display: -webkit-box;
    margin: 0 0 6px;
    overflow: hidden;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .simple-subtitle {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    font-size: 13px;
    color: #999;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
  }
}
</style>
