<template>
  <s-layout :bgStyle="{ color: '#FFF' }" :title="state.title" class="set-wrap">
    <view class="image-list">
      <view
        v-for="(item, index) in imageList"
        :key="index"
        class="image-item"
        @click="handleImageClick(item)"
      >
        <image :src="item.imageUrl" mode="aspectFill" class="image" />
        <text class="title">{{ item.title }}</text>
      </view>
      <view v-if="loading" class="loading">加载中...</view>
    </view>
  </s-layout>
</template>

<script setup>
  // 导入 reactive 和 ref
  import { reactive, ref } from 'vue'; // 添加这一行导入 reactive 和 ref
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { getImageContentList } from '/yehu/api/public/index'; // 假设远程调用API为getImageList

  const state = reactive({
    title: '',
    type: '',
    page: 1,
    pageSize: 10,
    loading: false,
    hasMore: true,
  });
  const imageList = ref([]);

  async function fetchImageList(type, isLoadMore = false) {
    if (state.loading || !state.hasMore) return;
    state.loading = true;
    try {
      const response = await getImageContentList({
        type: type,
        page: state.page,
        pageSize: state.pageSize,
      });
      console.log('Image list:', response)
      // 添加响应数据校验
      if (!response || !response.data) {
        console.error('Invalid response from getImageContentList:', response);
        return;
      }

      if (isLoadMore) {
        imageList.value = [...imageList.value, ...response.data];
      } else {
        imageList.value = response.data;
      }
      state.hasMore = response.data.length >= state.pageSize;
      state.page += 1;
    } catch (error) {
      console.error('Failed to fetch image list:', error);
    } finally {
      state.loading = false;
    }
  }

  const handleImageClick = (item) => {
    uni.navigateTo({
      url: item.pagePath,
    });
  };

  onLoad((options) => {
    console.log("图片列表页加载中", options)
    if (options.title) {
      state.title = options.title;
    }

    if (options.type) {
      state.type = options.type;
      uni.setNavigationBarTitle({
        title: state.title,
      });
      
      fetchImageList(options.type);
    }
  });

  onReachBottom(() => {
    if (state.hasMore) {
      fetchImageList(state.type, true);
    }
  });
</script>

<style scoped lang="scss">
  .image-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 20px 0;
  }

  .image-item {
    position: relative;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
  }

  .image {
    width: 100%;
    height: 200px;
  }

  .title {
    position: absolute;
    bottom: 10px;
    left: 10px;
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    background-color: rgba(0, 0, 0, 0.5);
    padding: 4px 8px;
    border-radius: 4px;
  }

  .loading {
    text-align: center;
    padding: 10px;
    font-size: 14px;
    color: #999;
  }
</style>