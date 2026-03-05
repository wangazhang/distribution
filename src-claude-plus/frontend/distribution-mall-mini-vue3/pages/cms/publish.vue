<template>
  <s-layout title="发布内容">
    <view class="publish-page">
      <!-- 封面图片上传区域(宫格式多图) -->
      <view class="cover-upload-grid">
        <view
          class="grid-container"
          :class="{ 'grid-container-empty': formData.coverImages.length === 0 }"
        >
          <!-- 已上传的图片 -->
          <view
            v-for="(image, index) in formData.coverImages"
            :key="index"
            class="grid-item"
            @tap="previewImage(index)"
          >
            <image :src="sheep.$url.cdn(image)" mode="aspectFill" class="grid-image" />
            <view class="delete-btn" @tap.stop="removeImage(index)">
              <uni-icons type="close" size="16" color="#fff"></uni-icons>
            </view>
            <view v-if="index === 0" class="cover-badge">封面</view>
          </view>

          <!-- 添加按钮 -->
          <view
            v-if="formData.coverImages.length < 9"
            class="grid-item add-item"
            @tap="chooseImage"
          >
            <uni-icons type="camera-filled" size="40" color="#ccc"></uni-icons>
            <text class="add-text">{{ formData.coverImages.length }}/9</text>
          </view>
        </view>
        <view class="upload-tip">第一张图片将作为封面,最多上传9张</view>
      </view>

      <!-- 表单内容区域 -->
      <view class="form-content">
        <!-- 标题 -->
        <view class="form-section">
          <view class="section-label">标题</view>
          <input
            v-model="formData.title"
            class="title-input"
            placeholder="填写好的标题会获得更多的赞哦~"
            maxlength="200"
          />
        </view>

        <!-- 摘要 -->
        <view class="form-section">
          <view class="section-label"> 摘要 </view>
          <textarea
            v-model="formData.content"
            class="content-textarea"
            placeholder="填写内容"
            maxlength="5000"
            :auto-height="false"
          ></textarea>
        </view>

        <!--        &lt;!&ndash; 选择分类和标签 &ndash;&gt;-->
        <!--        <view class="form-section-row">-->
        <!--          &lt;!&ndash; 分类选择 &ndash;&gt;-->
        <!--          <picker-->
        <!--            v-if="categoryList.length > 0"-->
        <!--            mode="selector"-->
        <!--            :range="categoryList"-->
        <!--            range-key="name"-->
        <!--            :value="categoryIndex"-->
        <!--            @change="onCategoryChange"-->
        <!--          >-->
        <!--            <view class="select-box">-->
        <!--              <text v-if="selectedCategory" class="select-value">{{ selectedCategory.name }}</text>-->
        <!--              <text v-else class="select-placeholder">选择分类</text>-->
        <!--              <uni-icons type="arrowdown" size="16" color="#999"></uni-icons>-->
        <!--            </view>-->
        <!--          </picker>-->

        <!--          &lt;!&ndash; 标签选择 &ndash;&gt;-->
        <!--          <view class="tag-selector" @tap="showTagPicker">-->
        <!--            <text v-if="formData.tagIds.length > 0" class="tag-count">已选{{ formData.tagIds.length }}个标签</text>-->
        <!--            <text v-else class="select-placeholder">选择标签</text>-->
        <!--            <uni-icons type="arrowdown" size="16" color="#999"></uni-icons>-->
        <!--          </view>-->
        <!--        </view>-->

        <!--        &lt;!&ndash; 已选标签展示 &ndash;&gt;-->
        <!--        <view v-if="formData.tagIds.length > 0" class="selected-tags">-->
        <!--          <view-->
        <!--            v-for="tagId in formData.tagIds"-->
        <!--            :key="tagId"-->
        <!--            class="selected-tag"-->
        <!--            @tap="removeTag(tagId)"-->
        <!--          >-->
        <!--            {{ getTagName(tagId) }}-->
        <!--            <text class="remove-icon">×</text>-->
        <!--          </view>-->
        <!--        </view>-->

        <!-- 关联商品展示 -->
        <view v-if="selectedProducts.length > 0" class="selected-products">
          <view class="section-label">关联商品</view>
          <view class="product-list">
            <view v-for="product in selectedProducts" :key="product.id" class="product-item">
              <image :src="product.picUrl" class="product-image" mode="aspectFill" />
              <view class="product-info">
                <view class="product-name">{{ product.name }}</view>
                <view class="product-price">¥{{ (product.price / 100).toFixed(2) }}</view>
              </view>
              <view class="remove-product" @tap="removeProduct(product.id)">
                <uni-icons type="close" size="16" color="#999"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 悬浮工具栏 - 添加商品按钮 -->
      <view class="floating-toolbar">
        <view class="action-item" @tap="addProduct">
          <view class="icon-wrapper">
            <uni-icons type="cart" size="28" color="#333"></uni-icons>
            <view class="plus-badge">+</view>
          </view>
          <text class="action-text">商品</text>
        </view>
      </view>

      <!-- 底部固定按钮 -->
      <view class="bottom-actions">
        <view class="draft-btn" @tap="saveDraft">
          <uni-icons type="paperclip" size="20" color="#666"></uni-icons>
          <text class="draft-text">存草稿</text>
        </view>
        <button class="publish-btn ss-reset-button" :disabled="!canSubmit" @tap="handleSubmit">
          {{ state.submitting ? '发布中...' : '发布' }}
        </button>
      </view>

      <!-- 成功动画组件 -->
      <s-success-animation
        :visible="showSuccessAnimation"
        title="发布成功"
        subtitle="内容正在审核中，请耐心等待"
        confirm-text="确定"
        @confirm="handleSuccessConfirm"
        @close="handleSuccessClose"
      />
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive, computed, ref, onUnmounted } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import articleApi from '@/sheep/api/promotion/cms/article';
  import categoryApi from '@/sheep/api/promotion/cms/category';
  import tagApi from '@/sheep/api/promotion/cms/tag';
  import FileApi from '@/sheep/api/infra/file';
  import sSuccessAnimation from '@/sheep/components/s-success-animation/s-success-animation.vue';

  const state = reactive({
    sectionId: null,
    submitting: false,
  });

  // 成功动画状态
  const showSuccessAnimation = ref(false);
  const hasNavigatedAfterSuccess = ref(false);

  const formData = reactive({
    title: '',
    subtitle: '',
    coverImages: [],
    content: '',
    categoryId: null,
    tagIds: [],
    productIds: [], // 关联商品ID列表
  });

  const categoryList = ref([]);
  const tagList = ref([]);
  const selectedProducts = ref([]); // 已选商品列表
  const categoryIndex = ref(0);
  const tagPopup = ref(null);

  const selectedCategory = computed(() => {
    return categoryList.value.find((c) => c.id === formData.categoryId);
  });

  const canSubmit = computed(() => {
    return (
      formData.title.trim() && formData.content.trim() && formData.categoryId && !state.submitting
    );
  });

  const compressImageFile = (src) =>
    new Promise((resolve) => {
      uni.compressImage({
        src,
        quality: 80,
        success: (res) => {
          resolve(res.tempFilePath || src);
        },
        fail: (error) => {
          console.warn('图片压缩失败，使用原图上传:', error);
          resolve(src);
        },
      });
    });

  // 选择图片(支持多图)
  const chooseImage = () => {
    // 计算还能上传几张
    const remainCount = 9 - formData.coverImages.length;
    if (remainCount <= 0) {
      sheep.$helper.toast('最多上传9张图片');
      return;
    }

    uni.chooseImage({
      count: Math.min(remainCount, 9), // 允许多选,最多选择剩余数量
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        try {
          // 显示上传进度
          uni.showLoading({
            title: `上传中 0/${res.tempFilePaths.length}`,
            mask: true,
          });

          // 批量上传所有选择的图片
          let successCount = 0;
          for (let i = 0; i < res.tempFilePaths.length; i++) {
            const tempFile = res.tempFiles?.[i];
            const filePath = tempFile?.path || res.tempFilePaths[i];
            const compressedPath = await compressImageFile(filePath);
            const uploadRes = await FileApi.uploadFile(compressedPath);

            if (uploadRes.error === 1) {
              console.error('上传失败:', uploadRes.msg);
              continue;
            }

            // 上传成功，uploadRes.data 直接是图片URL字符串
            if (uploadRes.code === 0 && uploadRes.data) {
              formData.coverImages.push(uploadRes.data);
              successCount++;

              // 更新进度
              uni.showLoading({
                title: `上传中 ${successCount}/${res.tempFilePaths.length}`,
                mask: true,
              });
            }
          }

          uni.hideLoading();

          if (successCount > 0) {
            sheep.$helper.toast(`成功上传${successCount}张图片`);
            console.log('图片上传成功,当前图片列表:', formData.coverImages);
          } else {
            sheep.$helper.toast('上传失败,请重试');
          }
        } catch (error) {
          uni.hideLoading();
          console.error('上传图片失败:', error);
          sheep.$helper.toast('上传图片失败');
        }
      },
      fail: (error) => {
        console.error('选择图片失败:', error);
        sheep.$helper.toast('选择图片失败');
      },
    });
  };

  // 预览图片
  const previewImage = (index) => {
    if (!formData.coverImages.length) return;
    const urls = formData.coverImages.map((img) => sheep.$url.cdn(img));
    uni.previewImage({
      current: index,
      urls,
    });
  };

  // 删除图片(带确认)
  const removeImage = (index) => {
    const isCover = index === 0;
    const title = isCover ? '确定删除封面图片吗?' : '确定删除这张图片吗?';

    uni.showModal({
      title: '提示',
      content: title,
      success: (res) => {
        if (res.confirm) {
          formData.coverImages.splice(index, 1);
          sheep.$helper.toast('删除成功');
          console.log('删除图片后,当前图片列表:', formData.coverImages);
        }
      },
    });
  };

  // 分类选择变更
  const onCategoryChange = async (e) => {
    const index = e.detail.value;
    categoryIndex.value = index;
    formData.categoryId = categoryList.value[index].id;

    // 清空已选标签并重新加载该分类的标签
    formData.tagIds = [];
    await loadTagsByCategory(categoryList.value[index].id);
  };

  // 切换标签
  const toggleTag = (tagId) => {
    const index = formData.tagIds.indexOf(tagId);
    if (index > -1) {
      formData.tagIds.splice(index, 1);
    } else {
      formData.tagIds.push(tagId);
    }
  };

  // 获取标签名称
  const getTagName = (tagId) => {
    const tag = tagList.value.find((t) => t.id === tagId);
    return tag ? tag.name : '';
  };

  // 移除标签
  const removeTag = (tagId) => {
    toggleTag(tagId);
  };

  // 显示标签选择器
  const showTagPicker = () => {
    if (tagPopup.value) {
      tagPopup.value.open();
    }
  };

  // 关闭标签选择器
  const closeTagPicker = () => {
    if (tagPopup.value) {
      tagPopup.value.close();
    }
  };

  // 添加大图
  const addImage = () => {
    uni.showToast({
      title: '添加大图功能开发中',
      icon: 'none',
    });
  };

  // 添加文字
  const addText = () => {
    uni.showToast({
      title: '添加文字功能开发中',
      icon: 'none',
    });
  };

  // 添加商品
  const addProduct = () => {
    uni.navigateTo({
      url: '/pages/cms/product-select',
    });
  };

  // 监听商品选择事件
  uni.$on('selectedProducts', (data) => {
    console.log('收到选择的商品数据:', data);
    if (data && data.products && data.products.length > 0) {
      const newProduct = data.products[0];
      // 检查是否已存在
      const existingIndex = selectedProducts.value.findIndex((p) => p.id === newProduct.id);
      if (existingIndex === -1) {
        // 添加新商品
        selectedProducts.value.push(newProduct);
        formData.productIds = selectedProducts.value.map((p) => p.id);
        console.log('商品添加成功:', selectedProducts.value);
      } else {
        // 商品已存在
        uni.showToast({
          title: '商品已添加',
          icon: 'none',
          duration: 1500,
        });
      }
    }
  });

  // 保存草稿
  const saveDraft = () => {
    uni.showToast({
      title: '草稿已保存',
      icon: 'success',
    });
  };

  // 提交发布
  const handleSubmit = async () => {
    if (!canSubmit.value) return;

    state.submitting = true;
    try {
      const data = {
        sectionId: state.sectionId,
        categoryId: formData.categoryId,
        title: formData.title,
        subtitle: formData.subtitle,
        coverImages: formData.coverImages,
        content: formData.content,
        contentType: 'richtext', // UGC默认富文本
        tagIds: formData.tagIds,
        productIds: formData.productIds,
      };

      const res = await articleApi.publish(data);
      if (res.code === 0) {
        // 显示成功动画
        hasNavigatedAfterSuccess.value = false;
        showSuccessAnimation.value = true;
      }
    } catch (err) {
      sheep.$helper.toast(err.msg || '发布失败');
    } finally {
      state.submitting = false;
    }
  };

  // 加载分类和标签
  const loadCategoriesAndTags = async () => {
    if (!state.sectionId) return;

    try {
      // 加载分类列表
      const categoryRes = await categoryApi.listBySection(state.sectionId);
      categoryList.value = categoryRes.data || [];

      // 如果有分类，默认加载第一个分类的标签
      if (categoryList.value.length > 0) {
        formData.categoryId = categoryList.value[0].id;
        categoryIndex.value = 0;
        await loadTagsByCategory(categoryList.value[0].id);
      }
    } catch (error) {
      console.error('加载分类和标签失败:', error);
    }
  };

  // 根据分类加载标签
  const loadTagsByCategory = async (categoryId) => {
    if (!categoryId) {
      tagList.value = [];
      return;
    }

    try {
      const tagRes = await tagApi.listByCategory(categoryId);
      tagList.value = tagRes.data || [];
    } catch (error) {
      console.error('加载标签失败:', error);
      tagList.value = [];
    }
  };

  // 移除商品
  const removeProduct = (productId) => {
    selectedProducts.value = selectedProducts.value.filter((p) => p.id !== productId);
    formData.productIds = selectedProducts.value.map((p) => p.id);
  };

  // 处理成功动画确认
  const navigateBackOnce = () => {
    if (hasNavigatedAfterSuccess.value) {
      return;
    }
    hasNavigatedAfterSuccess.value = true;
    uni.navigateBack();
  };

  const handleSuccessConfirm = () => {
    showSuccessAnimation.value = false;
    navigateBackOnce();
  };

  // 处理成功动画关闭
  const handleSuccessClose = () => {
    showSuccessAnimation.value = false;
    navigateBackOnce();
  };

  onLoad(async (options) => {
    if (options.sectionId) {
      state.sectionId = options.sectionId;
      await loadCategoriesAndTags();
    }
  });

  // 页面卸载时移除事件监听
  onUnmounted(() => {
    uni.$off('selectedProducts');
  });
</script>

<style lang="scss" scoped>
  .publish-page {
    min-height: 100vh;
    background-color: #ffffff;
    padding-bottom: 200rpx; // 为底部按钮和悬浮工具栏留空间
  }

  /* 封面上传区域 - 宫格式多图 */
  .cover-upload-grid {
    padding: 50rpx 0rpx;
    background: white;
    margin: 0 auto; // 水平居中整个上传区域
    width: 100%; // 确保在小屏幕上填满宽度

    .grid-container {
      display: flex;
      flex-wrap: wrap;
      margin-right: -16rpx;
      margin-bottom: -16rpx;
    }

    .grid-container-empty {
      justify-content: center;
    }

    .grid-item {
      position: relative;
      box-sizing: border-box;
      width: 210rpx;
      height: 210rpx;
      margin-right: 16rpx;
      margin-bottom: 16rpx;
      background: #f7f8fa;
      border-radius: 12rpx;
      overflow: hidden;
    }

    .grid-image {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: cover;
    }

    .delete-btn {
      position: absolute;
      top: 8rpx;
      right: 8rpx;
      width: 40rpx;
      height: 40rpx;
      background: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10;
    }

    .cover-badge {
      position: absolute;
      top: 12rpx;
      left: 12rpx;
      padding: 6rpx 18rpx;
      background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
      color: #fff;
      font-size: 20rpx;
      border-radius: 999rpx;
      z-index: 9;
    }

    .add-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: 2rpx dashed #ddd;
      color: #999;
      gap: 12rpx;
    }

    .add-text {
      font-size: 24rpx;
    }

    .grid-item:nth-child(3n) {
      margin-right: 0;
    }

    .grid-container-empty .grid-item {
      margin-right: 0;
    }

    .upload-tip {
      font-size: 24rpx;
      color: #999;
      margin-top: 16rpx;
      text-align: center;
    }
  }

  /* 表单内容区域 */
  .form-content {
    background-color: white;
    padding: 30rpx;
  }

  .form-section {
    margin-bottom: 40rpx;

    .section-label {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 20rpx;

      .label-highlight {
        color: #e60012;
        margin-left: 10rpx;
      }
    }

    .title-input {
      width: 100%;
      font-size: 28rpx;
      color: #333;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #eee;

      &::placeholder {
        color: #999;
      }
    }

    .content-textarea {
      width: 100%;
      min-height: 200rpx;
      font-size: 28rpx;
      color: #333;
      line-height: 1.6;

      &::placeholder {
        color: #999;
      }
    }
  }

  /* 悬浮工具栏 - 紧贴底部按钮栏上方 */
  .floating-toolbar {
    position: fixed;
    bottom: calc(100rpx + env(safe-area-inset-bottom)); // 紧贴底部按钮栏上方
    left: 0;
    right: 0;

    display: flex;
    align-items: center;
    justify-content: flex-start; // 靠左对齐
    gap: 40rpx;
    padding: 12rpx 30rpx; // 减小高度

    /* 白色背景 */
    background-color: white;
    border-top: 1rpx solid #eee;

    z-index: 90;

    .action-item {
      display: flex;
      flex-direction: row; // 改为横向排列
      align-items: center;
      gap: 12rpx;
      padding: 8rpx 16rpx;
      transition: all 0.3s ease;

      &:active {
        background: rgba(139, 0, 0, 0.08);
        transform: scale(0.95);
        border-radius: 12rpx;
      }

      .icon-wrapper {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;

        .plus-badge {
          position: absolute;
          top: -6rpx;
          right: -6rpx;
          width: 24rpx;
          height: 24rpx;
          background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
          color: white;
          font-size: 18rpx;
          font-weight: bold;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          line-height: 1;
          border: 2rpx solid white;
        }
      }

      .action-text {
        font-size: 26rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }

  /* 分类和标签选择行 */
  .form-section-row {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;

    .select-box,
    .tag-selector {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 24rpx;
      background-color: #f7f8fa;
      border-radius: 12rpx;
      font-size: 26rpx;

      .select-value,
      .tag-count {
        color: #333;
      }

      .select-placeholder {
        color: #999;
      }
    }
  }

  /* 已选标签 */
  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-bottom: 30rpx;

    .selected-tag {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 20rpx;
      background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
      color: white;
      border-radius: 24rpx;
      font-size: 24rpx;

      .remove-icon {
        font-size: 32rpx;
        font-weight: bold;
      }
    }
  }

  /* 底部固定按钮 */
  .bottom-actions {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx 30rpx;
    background-color: white;
    border-top: 1rpx solid #eee;
    padding-bottom: calc(10rpx + env(safe-area-inset-bottom));

    .draft-btn {
      display: flex;
      align-items: center;
      gap: 10rpx;
      padding: 0 24rpx;

      .draft-text {
        font-size: 26rpx;
        color: #666;
      }
    }

    .publish-btn {
      flex: 1;
      height: 80rpx;
      background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
      border-radius: 40rpx;
      font-size: 30rpx;
      font-weight: bold;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;

      &[disabled] {
        opacity: 0.5;
      }
    }
  }

  /* 标签选择弹窗 */
  .tag-popup {
    background-color: white;
    border-radius: 24rpx 24rpx 0 0;
    max-height: 80vh;

    .popup-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx;
      border-bottom: 1rpx solid #eee;

      .popup-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }

      .popup-close {
        font-size: 28rpx;
        color: #8b0000;
      }
    }

    .tag-list {
      padding: 30rpx;
      display: flex;
      flex-wrap: wrap;
      gap: 20rpx;
      max-height: 60vh;
      overflow-y: auto;

      .tag-option {
        padding: 16rpx 32rpx;
        background-color: #f7f8fa;
        border-radius: 32rpx;
        font-size: 26rpx;
        color: #666;

        &.active {
          background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
          color: white;
        }
      }
    }
  }

  /* 关联商品样式 */
  .selected-products {
    margin-bottom: 30rpx;

    .product-list {
      display: flex;
      flex-direction: column;
      gap: 16rpx;
    }

    .product-item {
      display: flex;
      align-items: center;
      padding: 16rpx;
      background-color: #f7f8fa;
      border-radius: 12rpx;
      position: relative;
    }

    .product-image {
      width: 80rpx;
      height: 80rpx;
      border-radius: 8rpx;
      margin-right: 16rpx;
    }

    .product-info {
      flex: 1;
      min-width: 0;

      .product-name {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 8rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-price {
        font-size: 24rpx;
        color: #e60012;
        font-weight: 500;
      }
    }

    .remove-product {
      padding: 8rpx;
    }
  }
</style>
