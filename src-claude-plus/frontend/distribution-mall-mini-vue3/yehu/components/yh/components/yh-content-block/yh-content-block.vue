<template>
  <view class="content-block">
    <!-- 标题头部区域 -->
    <view class="tab-header">
      <view class="tab-title">{{ title }}</view>
      <image class="tab-header-image" :src="headerImage" mode="widthFix"></image>
    </view>

    <!-- 内容区域，根据contentType展示不同布局 -->
    <block v-if="contentType === 'product'">
      <view class="ecm-product-grid">
        <view class="ecm-product-item" v-for="(item, index) in contentData" :key="index" @tap="handleItemClick(item)">
          <image :src="item.image" mode="aspectFill" class="ecm-product-image"></image>
          <view class="ecm-product-footer">
            <view class="ecm-buy-btn">立即购买 ></view>
            <view class="ecm-product-price">{{item.price}}</view>
          </view>
        </view>
      </view>
    </block>

    <block v-if="contentType === 'activities'">
      <view class="private-activities">
        <view class="activity-section" v-for="(section, secIndex) in getActivitySections()" :key="secIndex">
          <view class="section-title">{{ section.title || '活动' }}</view>
          <view class="section-divider"></view>
          <view class="activity-list">
            <view class="activity-item" v-for="(item, index) in section.list" :key="index" @tap="handleItemClick(item)">
              <image :src="item.image" mode="aspectFill" class="activity-circle-image"></image>
              <view class="activity-item-title">{{ item.title }}</view>
              <view class="activity-item-subtitle">{{ item.subtitle }}</view>
            </view>
          </view>
        </view>
          <!-- 会员卡区块 -->
          <view class="activity-section" v-if="contentData.membership">
          <view class="section-title">{{ contentData.membership.title || '会员专享' }}</view>
          <view class="section-divider"></view>
          <view class="membership-cards">
            <view class="membership-card" 
                  v-for="(item, index) in contentData.membership.list" 
                  :key="index" 
                  @tap="handleItemClick(item)">
              <view class="membership-title">{{ item.title }}</view>
              <view class="membership-action">
                <view class="action-text">{{ item.subtitle }}</view>
                <view class="action-arrow">></view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
    </block>

    <block v-if="contentType === 'program'">
      <view class="academy-programs">
        <view class="program-item" v-for="(item, index) in contentData" :key="index" @tap="handleItemClick(item)">
          <image :src="item.image" mode="aspectFill" class="program-image"></image>
          <view class="program-content">
            <view class="program-title">{{item.title}}</view>
            <view class="program-subtitle">{{item.subtitle}}</view>
          </view>
        </view>
      </view>
    </block>

    <block v-if="contentType === 'resource'">
      <view class="traffic-resources">
        <view class="resource-section" v-for="(section, secIndex) in contentData" :key="secIndex">
          <view class="section-title">{{ section.title || '资源' }}</view>
          <view class="section-divider"></view>
          <view class="resource-list">
            <view class="resource-item" v-for="(item, index) in section.list" :key="index" @tap="handleItemClick(item)">
              <image :src="item.image" mode="aspectFill" class="resource-circle-image"></image>
              <view class="resource-item-title">{{ item.title }}</view>
              <view class="resource-item-subtitle">{{ item.subtitle }}</view>
            </view>
          </view>
        </view>
      </view>
    </block>
  </view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import sheep from '@/sheep';

const props = defineProps({
  // 内容区块标题
  title: {
    type: String,
    default: '内容区块'
  },
  // 头部图片
  headerImage: {
    type: String,
    default: ''
  },
  // 内容类型：product, activities, program, resource
  contentType: {
    type: String,
    default: 'product'
  },
  // 内容数据
  contentData: {
    type: [Array, Object],
    default: () => []
  }
});

const emit = defineEmits(['itemClick']);

// 获取活动区块数据（排除membership）
const getActivitySections = () => {
  if (!props.contentData) return [];
  
  // 过滤掉membership，只展示其他活动区块
  const sections = [];
  Object.keys(props.contentData).forEach(key => {
    if (key !== 'membership') {
      sections.push(props.contentData[key]);
    }
  });
  
  return sections;
};

// 处理项目点击
const handleItemClick = (item) => {
  // 触发振动反馈
  uni.vibrateShort({
    success: () => {
      console.log('项目点击震动成功');
    }
  });
  
  // 如果是会员卡，直接用pagePath跳转
  if (item.pagePath) {
    uni.navigateTo({
      url: item.pagePath,
      fail: (err) => {
        console.error('导航失败', err);
        uni.showToast({
          title: '该页面正在开发中',
          icon: 'none'
        });
      }
    });
    
    // 同时向父组件发送点击事件，用于统计或其他处理
    emit('itemClick', {
      type: 'membership',
      item: item
    });
    return;
  }
  
  // 根据不同类型处理跳转
  switch(props.contentType) {
    case 'product':
      navigateToProduct(item);
      break;
    case 'activities':
      navigateToActivity(item);
      break;
    case 'program':
      navigateToProgram(item);
      break;
    case 'resource':
      navigateToResource(item);
      break;
    default:
      console.log('未知内容类型:', props.contentType);
  }
  
  // 同时向父组件发送点击事件，用于统计或其他处理
  emit('itemClick', {
    type: props.contentType,
    item: item
  });
};

// 商品详情页导航
const navigateToProduct = (product) => {
  console.log('商品详情导航:', product);
  console.log('商品ID:', product.productId || product.id, '项目ID:', product.id);
  
  // 优先使用productId，如果不存在则回退到id
  const productId = product.productId || product.id || 0;
  // sheep.$router.go('/pages/goods/index', { id: productId })
  // 导航到商品详情页
  uni.navigateTo({
    url: `/pages/goods/index?id=${productId}&name=${encodeURIComponent(product.name || '')}`,
    fail: (err) => {
      console.error('导航到商品详情页失败', err);
      uni.showToast({
        title: '商品详情页正在开发中',
        icon: 'none'
      });
    }
  });
};

// 活动详情页导航
const navigateToActivity = (activity) => {
  console.log('活动详情导航:', activity);
  
  // 导航到活动详情页
  uni.navigateTo({
    url: `/yehu/pages/activity/detail?title=${encodeURIComponent(activity.title)}&subtitle=${encodeURIComponent(activity.subtitle)}`,
    fail: (err) => {
      console.error('导航到活动详情页失败', err);
      uni.showToast({
        title: '活动详情页正在开发中',
        icon: 'none'
      });
    }
  });
};

// 课程详情页导航
const navigateToProgram = (program) => {
  console.log('课程详情导航:', program);
  
  // 导航到课程详情页
  uni.navigateTo({
    url: `/yehu/pages/academy/program/detail?id=${program.id}&title=${encodeURIComponent(program.title)}`,
    fail: (err) => {
      console.error('导航到课程详情页失败', err);
      uni.showToast({
        title: '课程详情页正在开发中',
        icon: 'none'
      });
    }
  });
};

// 资源详情页导航
const navigateToResource = (resource) => {
  console.log('资源详情导航:', resource);
  
  // 导航到资源详情页
  uni.navigateTo({
    url: `/yehu/pages/resource/detail?id=${resource.id}&title=${encodeURIComponent(resource.title)}`,
    fail: (err) => {
      console.error('导航到资源详情页失败', err);
      uni.showToast({
        title: '资源详情页正在开发中',
        icon: 'none'
      });
    }
  });
};
</script>

<style lang="scss" scoped>
.content-block {
  background-color: #f8f8f8;
  
  .tab-header {
    margin-bottom: 30rpx;
    padding: 0;
    overflow: hidden;
    height: 400rpx;
    position: relative;
    
    .tab-title {
      position: absolute;
      top: 20rpx;
      left: 30rpx;
      z-index: 5;
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
      background: rgba(0, 0, 0, 0.2);
      padding: 8rpx 20rpx;
      border-radius: 6rpx;
    }
    
    .tab-header-image {
      width: 100%;
      height: 400rpx;
      border-radius: 0;
      object-fit: cover;
      display: block;
    }
  }
  
  // ECM胶原产品网格样式
  .ecm-product-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 0;
    background-color: #f0f0f0;
    
    .ecm-product-item {
      width: 50%;
      height: 420rpx;
      position: relative;
      border-right: 2rpx solid #f0f0f0;
      border-bottom: 2rpx solid #f0f0f0;
      box-sizing: border-box;
      background-color: #f8f8f8;
      display: flex;
      flex-direction: column;
      
      &:nth-child(2n) {
        border-right: none;
      }
      
      &:nth-child(3), &:nth-child(4) {
        border-bottom: none;
      }
      
      .ecm-product-image {
        width: 100%;
        height: 340rpx;
        object-fit: contain;
        padding: 30rpx;
        box-sizing: border-box;
        flex: 1;
      }
      
      .ecm-product-footer {
        width: 100%;
        height: 80rpx;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        padding: 0 30rpx;
        box-sizing: border-box;
        background-color: #f8f8f8;
        
        .ecm-buy-btn {
          font-size: 28rpx;
          color: #333;
        }
        
        .ecm-product-price {
          font-size: 32rpx;
          color: #000;
          font-weight: bold;
          &::before {
            content: "¥";
            font-size: 26rpx;
            padding-right: 2rpx;
            font-weight: normal;
          }
        }
      }
    }
  }
  
  // 商学院课程样式
  .academy-programs {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10rpx;
    background-color: #f0f0f0;
    padding: 10rpx;
    
    .program-item {
      position: relative;
      background-color: #fff;
      height: 280rpx;
      overflow: hidden;
      border-radius: 8rpx;
      box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.1);
      
      .program-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .program-content {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        padding: 8rpx 0;
        background-color: rgba(255, 255, 255, 0.9);
        
        .program-title {
          font-size: 24rpx;
          font-weight: bold;
          color: #333;
          margin-bottom: 2rpx;
          text-align: center;
        }
        
        .program-subtitle {
          font-size: 18rpx;
          color: #666;
          text-align: center;
        }
      }
    }
  }
  
  // 私域活动样式
  .private-activities {
    padding: 20rpx 30rpx;
    
    .activity-section {
      margin-bottom: 40rpx;
      text-align: center;
      
      .section-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
        padding: 10rpx 0;
        display: inline-block;
        vertical-align: middle;
      }
      
      .section-divider {
        height: 2rpx;
        background-color: #e5e5e5;
        margin: 0 0 0 10rpx;
        width: 80%;
        display: inline-block;
        vertical-align: middle;
      }
      
      // 会员卡样式
      .membership-cards {
        margin-top: 30rpx;
        display: flex;
        flex-direction: column;
        gap: 20rpx;
        
        .membership-card {
          background-color: #fff;
          border-radius: 15rpx;
          height: 160rpx;
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 0 40rpx;
          box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.35);
          
          .membership-title {
            font-size: 60rpx;
            font-weight: bold;
            color: #000;
          }
          
          .membership-action {
            display: flex;
            align-items: center;
            
            .action-text {
              font-size: 26rpx;
              color: #999;
              margin-right: 8rpx;
            }
            
            .action-arrow {
              font-size: 26rpx;
              color: #999;
            }
          }
        }
      }
      
      .activity-list {
        margin-top: 30rpx;
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .activity-item {
          width: 220rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          
          .activity-circle-image {
            width: 180rpx;
            height: 180rpx;
            border-radius: 50%;
            margin-bottom: 20rpx;
          }
          
          .activity-item-title {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
            margin-bottom: 6rpx;
            text-align: center;
          }
          
          .activity-item-subtitle {
            font-size: 22rpx;
            color: #666;
            text-align: center;
            line-height: 1.2;
          }
        }
      }
    }
  }
  
  // 流量密码资源样式
  .traffic-resources {
    padding: 20rpx 30rpx;
    
    .resource-section {
      margin-bottom: 40rpx;
      
      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        padding: 10rpx 0;
      }
      
      .section-divider {
        height: 2rpx;
        background-color: #e5e5e5;
        margin: 10rpx 0 30rpx 0;
        width: 100%;
      }
      
      .resource-list {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .resource-item {
          width: 220rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          
          .resource-circle-image {
            width: 180rpx;
            height: 180rpx;
            border-radius: 50%;
            margin-bottom: 20rpx;
          }
          
          .resource-item-title {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
            margin-bottom: 6rpx;
            text-align: center;
          }
          
          .resource-item-subtitle {
            font-size: 24rpx;
            color: #666;
            text-align: center;
          }
        }
      }
    }
  }
}
</style> 