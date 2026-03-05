<template>
  <s-layout title="吸顶导航示例">
    <view class="page-container">
      <!-- 顶部内容 -->
      <view class="header-section">
        <view class="header-title">吸顶导航示例</view>
        <view class="header-desc">向下滚动页面，导航栏会吸附在顶部</view>
      </view>

      <!-- 吸顶导航组件 -->
      <s-sticky-navbar :data="navbarData" :styles="navbarStyles" />

      <!-- 内容区域 -->
      <view class="content-section">
        <!-- 推荐内容区域 -->
        <view id="section-1" class="content-item section-header">
          <view class="item-title">推荐内容</view>
          <view class="item-desc">这是推荐内容区域，展示推荐的商品和服务。</view>
        </view>
        <view v-for="(item, index) in recommendItems" :key="index" class="content-item">
          <view class="item-title">{{ item.title }}</view>
          <view class="item-desc">{{ item.desc }}</view>
        </view>

        <!-- 热门商品区域 -->
        <view id="section-2" class="content-item section-header">
          <view class="item-title">热门商品</view>
          <view class="item-desc">这是热门商品区域，展示最受欢迎的商品。</view>
        </view>
        <view v-for="(item, index) in hotItems" :key="index" class="content-item">
          <view class="item-title">{{ item.title }}</view>
          <view class="item-desc">{{ item.desc }}</view>
        </view>

        <!-- 新品上市区域 -->
        <view id="section-3" class="content-item section-header">
          <view class="item-title">新品上市</view>
          <view class="item-desc">这是新品上市区域，展示最新上架的商品。</view>
        </view>
        <view v-for="(item, index) in newItems" :key="index" class="content-item">
          <view class="item-title">{{ item.title }}</view>
          <view class="item-desc">{{ item.desc }}</view>
        </view>

        <!-- 限时活动区域 -->
        <view id="section-4" class="content-item section-header">
          <view class="item-title">限时活动</view>
          <view class="item-desc">这是限时活动区域，展示正在进行的促销活动。</view>
        </view>
        <view v-for="(item, index) in activityItems" :key="index" class="content-item">
          <view class="item-title">{{ item.title }}</view>
          <view class="item-desc">{{ item.desc }}</view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref } from 'vue';

  // 吸顶导航数据
  const navbarData = ref({
    bgColor: '#ffffff',
    activeColor: '#8F1911',
    textColor: '#333333',
    showUnderline: true,
    underlineColor: '#8F1911',
    list: [
      { text: '推荐', anchorId: 'section-1' },
      { text: '热门', anchorId: 'section-2' },
      { text: '新品', anchorId: 'section-3' },
      { text: '活动', anchorId: 'section-4' },
    ],
  });

  // 吸顶导航样式
  const navbarStyles = ref({
    paddingTop: 10,
    paddingRight: 0,
    paddingBottom: 10,
    paddingLeft: 0,
    marginTop: 0,
    marginRight: 0,
    marginBottom: 20,
    marginLeft: 0,
    borderRadius: 0,
  });

  // 推荐内容项
  const recommendItems = ref([
    {
      title: '推荐商品1',
      desc: '这是推荐内容区域的商品1，向下滚动页面查看吸顶效果。',
    },
    {
      title: '推荐商品2',
      desc: '这是推荐内容区域的商品2，向下滚动页面查看吸顶效果。',
    },
    {
      title: '推荐商品3',
      desc: '这是推荐内容区域的商品3，向下滚动页面查看吸顶效果。',
    },
  ]);

  // 热门商品项
  const hotItems = ref([
    {
      title: '热门商品1',
      desc: '这是热门商品区域的商品1，向下滚动页面查看吸顶效果。',
    },
    {
      title: '热门商品2',
      desc: '这是热门商品区域的商品2，向下滚动页面查看吸顶效果。',
    },
    {
      title: '热门商品3',
      desc: '这是热门商品区域的商品3，向下滚动页面查看吸顶效果。',
    },
  ]);

  // 新品上市项
  const newItems = ref([
    {
      title: '新品商品1',
      desc: '这是新品上市区域的商品1，向下滚动页面查看吸顶效果。',
    },
    {
      title: '新品商品2',
      desc: '这是新品上市区域的商品2，向下滚动页面查看吸顶效果。',
    },
    {
      title: '新品商品3',
      desc: '这是新品上市区域的商品3，向下滚动页面查看吸顶效果。',
    },
  ]);

  // 限时活动项
  const activityItems = ref([
    {
      title: '活动商品1',
      desc: '这是限时活动区域的商品1，向下滚动页面查看吸顶效果。',
    },
    {
      title: '活动商品2',
      desc: '这是限时活动区域的商品2，向下滚动页面查看吸顶效果。',
    },
    {
      title: '活动商品3',
      desc: '这是限时活动区域的商品3，向下滚动页面查看吸顶效果。',
    },
  ]);
</script>

<style lang="scss" scoped>
  .page-container {
    min-height: 100vh;
    background-color: #f8f8f8;
    padding-bottom: 40rpx;
  }

  .header-section {
    padding: 40rpx 30rpx;
    background-color: #fff;
    margin-bottom: 20rpx;

    .header-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 16rpx;
    }

    .header-desc {
      font-size: 28rpx;
      color: #666;
    }
  }

  .content-section {
    padding: 0 30rpx;

    .content-item {
      background-color: #fff;
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;

      &.section-header {
        background-color: #f5f0ee;
        border-left: 8rpx solid #8f1911;
      }

      .item-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 16rpx;
      }

      .item-desc {
        font-size: 28rpx;
        color: #666;
        line-height: 1.5;
      }
    }
  }
</style>
