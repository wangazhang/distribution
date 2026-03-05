<template>
  <s-layout :title="getPageTitle">
    <view class="download-page" v-if="state.article.id">
      <!-- 素材信息预览 -->
      <view class="material-preview bg-white">
        <!-- 九宫格图片选择 -->
        <view v-if="allImages.length > 0" class="image-grid-section ss-p-30">
          <view class="section-header ss-flex ss-row-between ss-col-center ss-m-b-20">
            <text class="section-title">{{ getContentImageTitle }}</text>
            <text class="select-all-btn" @tap="toggleSelectAll">
              {{ isAllSelected ? '取消全选' : '全选' }}
            </text>
          </view>
          <view class="image-grid">
            <view
              v-for="(img, index) in allImages"
              :key="index"
              class="grid-item"
              @tap="toggleImageSelect(index)"
            >
              <image :src="sheep.$url.cdn(img)" mode="aspectFill" class="grid-image" />
              <!-- 选中状态 -->
              <view class="select-mask" :class="{ selected: state.selectedImages[index] }">
                <view v-if="state.selectedImages[index]" class="check-icon">
                  <text class="_icon-check-round" style="font-size: 40rpx; color: white"></text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 文本内容预览 -->
        <view class="preview-text ss-p-30">
          <view class="material-title">{{ state.article.title }}</view>
          <view v-if="state.article.subtitle" class="material-subtitle ss-m-t-16">
            {{ state.article.subtitle }}
          </view>
          <view class="material-content ss-m-t-16">
            {{ getTextContent }}
          </view>
        </view>
      </view>

      <!-- 操作按钮区域 -->
      <view class="action-section bg-white ss-m-t-20">
        <!-- 复制文案按钮 -->
        <view class="action-item copy-section">
          <view class="action-header ss-flex ss-row-between ss-col-center">
            <view class="action-title">
              <text
                class="_icon-copy-o"
                style="font-size: 32rpx; color: #666; margin-right: 12rpx"
              ></text>
              <text class="title-text">复制文案</text>
            </view>
            <view class="copy-btn ss-flex ss-col-center" @tap="handleCopyText">
              <text class="copy-icon">复制</text>
            </view>
          </view>
          <view class="action-desc ss-m-t-12"> 复制标题、副标题、正文的全部文本内容 </view>
        </view>

        <!-- 下载图片按钮 -->
        <view v-if="allImages.length > 0" class="action-item download-section">
          <view class="action-header ss-flex ss-row-between ss-col-center">
            <view class="action-title">
              <text
                class="_icon-copy-o"
                style="font-size: 32rpx; color: #666; margin-right: 12rpx"
              ></text>
              <text class="title-text">下载图片 ({{ selectedImagesCount }})</text>
            </view>
            <view class="download-btn ss-flex ss-col-center" @tap="handleDownloadImages">
              <text class="download-icon">下载</text>
            </view>
          </view>
          <view class="action-desc ss-m-t-12"> 下载选中的图片到相册 </view>
        </view>
      </view>

      <!-- 素材统计信息 -->
      <view class="stats-section bg-white ss-m-t-20">
        <view class="stats-title">{{ getContentInfoTitle }}</view>
        <view class="stats-list">
          <view class="stats-item ss-flex ss-row-between">
            <text class="stats-label">发布者</text>
            <text class="stats-value">{{ state.article.authorName || '匿名' }}</text>
          </view>
          <view class="stats-item ss-flex ss-row-between">
            <text class="stats-label">发布时间</text>
            <text class="stats-value">{{ formatTime(state.article.publishTime) }}</text>
          </view>
          <view class="stats-item ss-flex ss-row-between">
            <text class="stats-label">浏览量</text>
            <text class="stats-value">{{ state.article.viewCount || 0 }}</text>
          </view>
          <view class="stats-item ss-flex ss-row-between">
            <text class="stats-label">点赞数</text>
            <text class="stats-value">{{ state.article.likeCount || 0 }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-else class="loading-container">
      <view class="loading-text">加载中...</view>
    </view>

  </s-layout>
</template>

<script setup>
  import { reactive, computed } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import articleApi from '@/sheep/api/promotion/cms/article';

  const state = reactive({
    article: {},
    selectedImages: [], // 图片选中状态数组
  });

  // 根据板块类型获取页面标题
  const getPageTitle = computed(() => {
    const sectionType = state.article.sectionType || 'article';
    switch (sectionType) {
      case 'article':
        return '下载文章';
      case 'dynamic':
        return '保存动态';
      case 'course':
        return '保存课程';
      case 'custom':
        return '保存内容';
      default:
        return '保存内容';
    }
  });

  // 根据板块类型获取图片标题
  const getContentImageTitle = computed(() => {
    const sectionType = state.article.sectionType || 'article';
    switch (sectionType) {
      case 'article':
        return '文章图片';
      case 'dynamic':
        return '动态图片';
      case 'course':
        return '课程图片';
      case 'custom':
        return '内容图片';
      default:
        return '内容图片';
    }
  });

  // 根据板块类型获取信息标题
  const getContentInfoTitle = computed(() => {
    const sectionType = state.article.sectionType || 'article';
    switch (sectionType) {
      case 'article':
        return '文章信息';
      case 'dynamic':
        return '动态信息';
      case 'course':
        return '课程信息';
      case 'custom':
        return '内容信息';
      default:
        return '内容信息';
    }
  });

  // 获取所有图片（封面图 + 正文中提取的图片）
  const allImages = computed(() => {
    const images = [];

    // 添加封面图片
    if (state.article.coverImages && state.article.coverImages.length > 0) {
      images.push(...state.article.coverImages);
    }

    // 从正文中提取图片链接（简单实现）
    if (state.article.content) {
      const imgRegex = /<img[^>]+src=["']([^"']+)["']/g;
      let match;
      while ((match = imgRegex.exec(state.article.content)) !== null) {
        images.push(match[1]);
      }

      // 处理Markdown格式的图片
      const mdImgRegex = /!\[.*?\]\((.*?)\)/g;
      while ((match = mdImgRegex.exec(state.article.content)) !== null) {
        images.push(match[1]);
      }
    }

    // 去重
    return [...new Set(images)];
  });

  // 获取纯文本内容
  const getTextContent = computed(() => {
    if (!state.article.content) return '';

    // 移除HTML标签
    let text = state.article.content.replace(/<[^>]+>/g, '');

    // 移除Markdown图片语法
    text = text.replace(/!\[.*?\]\(.*?\)/g, '');

    // 限制长度
    return text.length > 200 ? text.substring(0, 200) + '...' : text;
  });

  // 选中的图片数量
  const selectedImagesCount = computed(() => {
    return state.selectedImages.filter(Boolean).length;
  });

  // 是否全选
  const isAllSelected = computed(() => {
    return state.selectedImages.length > 0 && state.selectedImages.every(Boolean);
  });

  // 加载素材详情
  const loadArticle = async (id) => {
    const res = await articleApi.detail(id);
    if (res.code === 0) {
      state.article = res.data;
      // 初始化图片选中状态为全选
      state.selectedImages = allImages.value.map(() => true);
    }
  };

  // 全选/取消全选
  const toggleSelectAll = () => {
    const newState = !isAllSelected.value;
    state.selectedImages = allImages.value.map(() => newState);
  };

  // 切换单张图片选中状态
  const toggleImageSelect = (index) => {
    state.selectedImages[index] = !state.selectedImages[index];
  };

  // 格式化时间
  const formatTime = (time) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');

    // 显示完整日期时间
    if (year === now.getFullYear()) {
      return `${month}-${day} ${hour}:${minute}`;
    }

    return `${year}-${month}-${day} ${hour}:${minute}`;
  };

  // 预览图片
  const previewImage = (index) => {
    if (allImages.value.length === 0) return;

    const urls = allImages.value.map((img) => sheep.$url.cdn(img));
    uni.previewImage({
      current: index,
      urls: urls,
    });
  };

  // 复制文案内容
  const handleCopyText = () => {
    const parts = [];

    // 添加标题
    if (state.article.title) {
      parts.push(state.article.title);
    }

    // 添加副标题
    if (state.article.subtitle) {
      parts.push(state.article.subtitle);
    }

    // 添加正文内容
    if (state.article.content) {
      // 移除HTML标签，保留纯文本
      let content = state.article.content.replace(/<[^>]+>/g, '');
      parts.push(content);
    }

    const copyText = parts.join('\n');

    if (copyText) {
      uni.setClipboardData({
        data: copyText,
        success: () => {
          uni.showToast({
            title: '文案已复制到剪贴板',
            icon: 'success',
          });
        },
        fail: () => {
          uni.showToast({
            title: '复制失败',
            icon: 'none',
          });
        },
      });
    } else {
      uni.showToast({
        title: '暂无可复制的内容',
        icon: 'none',
      });
    }
  };

  // 下载图片
  const handleDownloadImages = async () => {
    // 筛选出选中的图片
    const selectedImageUrls = allImages.value.filter((img, index) => state.selectedImages[index]);

    if (selectedImageUrls.length === 0) {
      uni.showToast({
        title: '请至少选择一张图片',
        icon: 'none',
      });
      return;
    }

    try {
      uni.showLoading({
        title: '下载中',
        mask: true,
      });
      for (let i = 0; i < selectedImageUrls.length; i++) {
        const imageUrl = sheep.$url.cdn(selectedImageUrls[i]);

        await downloadSingleImage(imageUrl, i);
      }

      uni.showToast({
        title: '所有图片已保存到相册',
        icon: 'success',
      });
    } catch (error) {
      console.error('下载失败:', error);
      uni.showToast({
        title: '下载过程中出现错误',
        icon: 'none',
      });
    } finally {
      uni.hideLoading();
    }
  };

  // 下载单张图片
  const downloadSingleImage = (url, index) => {
    return new Promise((resolve, reject) => {
      uni.downloadFile({
        url: url,
        success: (res) => {
          if (res.statusCode === 200) {
            // 保存到相册
            uni.saveImageToPhotosAlbum({
              filePath: res.tempFilePath,
              success: () => {
                console.log(`图片 ${index + 1} 保存成功`);
                resolve();
              },
              fail: (err) => {
                console.error(`图片 ${index + 1} 保存失败:`, err);
                reject(err);
              },
            });
          } else {
            reject(new Error(`下载失败，状态码: ${res.statusCode}`));
          }
        },
        fail: (err) => {
          console.error(`图片 ${index + 1} 下载失败:`, err);
          reject(err);
        },
      });
    });
  };

  onLoad(async (options) => {
    if (options.id) {
      await loadArticle(options.id);
    }
  });
</script>

<style lang="scss" scoped>
  .download-page {
    min-height: 100vh;
    background-color: #f7f8fa;
    padding-bottom: 40rpx;
  }

  /* 素材预览区域 */
  .material-preview {
    border-radius: 16rpx;
    margin: 20rpx;
    overflow: hidden;

    /* 九宫格图片选择区域 */
    .image-grid-section {
      .section-header {
        margin-bottom: 20rpx;

        .section-title {
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
        }

        .select-all-btn {
          font-size: 26rpx;
          color: #8b0000;
          padding: 8rpx 16rpx;
          background-color: #f7f8fa;
          border-radius: 12rpx;

          &:active {
            opacity: 0.7;
          }
        }
      }

      .image-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12rpx;

        .grid-item {
          position: relative;
          width: 100%;
          padding-top: 100%; /* 1:1 aspect ratio */
          border-radius: 8rpx;
          overflow: hidden;

          .grid-image {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .select-mask {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0);
            transition: background-color 0.3s;
            display: flex;
            align-items: center;
            justify-content: center;

            &.selected {
              background-color: rgba(139, 0, 0, 0.3);
            }

            .check-icon {
              width: 48rpx;
              height: 48rpx;
              background-color: #8b0000;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 40rpx;
              color: white;
            }
          }

          &:active {
            opacity: 0.9;
          }
        }
      }
    }

    .preview-images {
      .preview-image {
        width: 100%;
        height: 300rpx;
        object-fit: cover;

        &:not(:last-child) {
          margin-bottom: 8rpx;
        }
      }
    }

    .preview-text {
      .material-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        line-height: 1.5;
      }

      .material-subtitle {
        font-size: 26rpx;
        color: #666;
        line-height: 1.5;
      }

      .material-content {
        font-size: 24rpx;
        color: #999;
        line-height: 1.6;
      }
    }
  }

  /* 操作区域 */
  .action-section {
    border-radius: 16rpx;
    margin: 20rpx;
    padding: 30rpx;

    .action-item {
      padding: 20rpx 0;

      &:not(:last-child) {
        border-bottom: 1rpx solid #f0f0f0;
      }

      .action-header {
        .action-title {
          display: flex;
          align-items: center;

          .title-text {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
          }
        }

        .copy-btn,
        .download-btn {
          padding: 8rpx 24rpx;
          background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
          border-radius: 20rpx;

          .copy-icon,
          .download-icon {
            font-size: 24rpx;
            color: white;
            font-weight: 500;
          }

          &:active {
            opacity: 0.8;
          }
        }
      }

      .action-desc {
        font-size: 22rpx;
        color: #999;
        line-height: 1.4;
      }
    }
  }

  /* 统计信息区域 */
  .stats-section {
    border-radius: 16rpx;
    margin: 20rpx;
    padding: 30rpx;

    .stats-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 20rpx;
    }

    .stats-list {
      .stats-item {
        padding: 12rpx 0;

        .stats-label {
          font-size: 24rpx;
          color: #666;
        }

        .stats-value {
          font-size: 24rpx;
          color: #333;
          font-weight: 500;
        }

        &:not(:last-child) {
          border-bottom: 1rpx solid #f5f5f5;
        }
      }
    }
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
  }

  .loading-text {
    font-size: 28rpx;
    color: #999;
  }
</style>
