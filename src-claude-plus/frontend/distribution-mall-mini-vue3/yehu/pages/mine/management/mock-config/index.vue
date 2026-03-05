<template>
  <view class="mock-config-container">
    <view class="header">
      <view class="title">接口Mock配置面板</view>
      <view class="desc">可单独控制各个接口的mock状态</view>
    </view>

    <view class="global-switch">
      <view class="label">全局Mock开关: </view>
      <view class="value">{{ useMock ? '开启' : '关闭' }}</view>
    </view>

    <view class="section">
      <view class="section-title">首页模块</view>
      <view class="api-item" v-for="(api, index) in homeApiList" :key="index">
        <view class="api-info">
          <view class="api-name">{{ api.name }}</view>
          <view class="api-path">{{ api.path }}</view>
        </view>
        <u-switch 
          v-model="api.useMock" 
          @change="(value) => toggleApiMock(api.module, api.key, value)"
          size="34"
          active-color="#c82436"
        ></u-switch>
      </view>
    </view>

    <view class="action-buttons">
      <u-button type="primary" @click="resetAllApis">重置所有接口</u-button>
      <u-button type="warning" @click="toggleAllApis(true)" class="mt-10">全部开启</u-button>
      <u-button type="info" @click="toggleAllApis(false)" class="mt-10">全部关闭</u-button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { mockControl, useMock, shouldUseMock, apiMockConfig } from 'yehu/api';

// 首页API列表
const homeApiPaths = reactive([
  { name: '首页轮播图', path: '/home/banner', key: 'banner', module: 'home', useMock: true },
  { name: '功能导航', path: '/home/function', key: 'function', module: 'home', useMock: true },
  { name: '底部导航', path: '/home/bottomNav', key: 'bottomNav', module: 'home', useMock: true },
  { name: '活动策划', path: '/home/activity', key: 'activity', module: 'home', useMock: true },
  { name: 'ECM胶原产品', path: '/home/ecm', key: 'ecm', module: 'home', useMock: true },
  { name: '商学院课程', path: '/home/course', key: 'course', module: 'home', useMock: true },
  { name: '直播MCN', path: '/home/live', key: 'live', module: 'home', useMock: true }
]);

// 整合所有API
const homeApiList = ref(homeApiPaths);

// 初始化各API的mock状态
onMounted(() => {
  // 更新首页API的状态
  homeApiList.value.forEach(api => {
    api.useMock = shouldUseMock(api.path);
  });
});

// 切换单个API的mock状态
const toggleApiMock = (module, key, value) => {
  // 根据模块查找对应的控制函数
  if (module === 'home' && mockControl.modules.home) {
    const homeMockControls = mockControl.modules.home;
    if (typeof homeMockControls[key] === 'function') {
      homeMockControls[key](value);
      uni.showToast({
        title: `${value ? '开启' : '关闭'}了${key}的Mock`,
        icon: 'none'
      });
    } else {
      uni.showToast({
        title: `未找到${module}.${key}的控制函数`,
        icon: 'none'
      });
    }
  }
};

// 重置所有接口配置
const resetAllApis = () => {
  mockControl.api.reset();
  // 更新UI状态
  homeApiList.value.forEach(api => {
    api.useMock = useMock; // 重置为全局设置
  });
  uni.showToast({
    title: '已重置所有接口配置',
    icon: 'none'
  });
};

// 批量设置所有接口
const toggleAllApis = (value) => {
  // 首页模块的批量设置
  const homeMockControls = mockControl.modules.home;
  if (homeMockControls && typeof homeMockControls.all === 'function') {
    homeMockControls.all(value);
  }
  
  // 更新UI状态
  homeApiList.value.forEach(api => {
    api.useMock = value;
  });
  
  uni.showToast({
    title: `已${value ? '开启' : '关闭'}所有接口Mock`,
    icon: 'none'
  });
};
</script>

<style lang="scss">
.mock-config-container {
  padding: 20rpx;
  
  .header {
    margin-bottom: 30rpx;
    
    .title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
    }
    
    .desc {
      font-size: 26rpx;
      color: #666;
      margin-top: 10rpx;
    }
  }
  
  .global-switch {
    display: flex;
    align-items: center;
    background-color: #f8f8f8;
    padding: 20rpx;
    border-radius: 8rpx;
    margin-bottom: 30rpx;
    
    .label {
      font-size: 30rpx;
      color: #333;
      font-weight: bold;
    }
    
    .value {
      margin-left: 20rpx;
      font-size: 30rpx;
      color: #c82436;
    }
  }
  
  .section {
    margin-bottom: 40rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 20rpx;
      border-left: 8rpx solid #c82436;
      padding-left: 20rpx;
    }
    
    .api-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx;
      background-color: #fff;
      margin-bottom: 20rpx;
      border-radius: 8rpx;
      box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
      
      .api-info {
        .api-name {
          font-size: 28rpx;
          color: #333;
          font-weight: bold;
        }
        
        .api-path {
          font-size: 24rpx;
          color: #999;
          margin-top: 6rpx;
        }
      }
    }
  }
  
  .action-buttons {
    margin-top: 40rpx;
  }
  
  .mt-10 {
    margin-top: 20rpx;
  }
}
</style> 