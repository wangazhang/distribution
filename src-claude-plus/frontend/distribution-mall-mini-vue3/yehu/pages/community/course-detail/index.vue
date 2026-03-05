<template>
<s-layout  navbar="course-detail"  >
  <view class="page-container">
    <!-- 自定义顶部导航栏
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="back-btn" @click="goBack">
          <u-icon name="arrow-left" color="#333333" size="32"></u-icon>
        </view>
        <text class="navbar-title">课程详情</text>
        <view class="placeholder"></view>
      </view>
    </view> -->
    
    <!-- 页面内容区域 -->
    <view class="page-content" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <!-- 课程封面 -->
      <view class="course-cover">
        <image class="cover-image" :src="courseDetail.coverImage || '/static/images/course-cover.png'" mode="aspectFill"></image>
        <view class="course-info">
          <text class="course-title">{{ courseDetail.title }}</text>
          <view class="teacher-info">
            <image class="teacher-avatar" :src="courseDetail.teacherAvatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg'"></image>
            <text class="teacher-name">{{ courseDetail.teacherName }}</text>
          </view>
        </view>
      </view>
      
      <!-- 课程详情 -->
      <view class="detail-section">
        <view class="section-title">课程介绍</view>
        <text class="course-desc">{{ courseDetail.description }}</text>
        
        <view class="section-title">课程亮点</view>
        <view class="highlight-list">
          <view class="highlight-item" v-for="(item, index) in courseDetail.highlights" :key="index">
            <view class="highlight-icon">
              <text>{{ index + 1 }}</text>
            </view>
            <text class="highlight-text">{{ item }}</text>
          </view>
        </view>
        
        <view class="section-title">课程大纲</view>
        <view class="outline-list">
          <view class="outline-item" v-for="(item, index) in courseDetail.outlines" :key="index">
            <view class="outline-header" @click="toggleChapter(index)">
              <text class="chapter-title">{{ item.title }}</text>
              <u-icon :name="expandedChapters.includes(index) ? 'arrow-up' : 'arrow-down'" size="28" color="#999999"></u-icon>
            </view>
            <view class="chapter-lessons" v-if="expandedChapters.includes(index)">
              <view class="lesson-item" v-for="(lesson, lidx) in item.lessons" :key="lidx">
                <u-icon name="play-right" size="28" color="#FF5777"></u-icon>
                <text class="lesson-title">{{ lesson.title }}</text>
                <text class="lesson-duration">{{ lesson.duration }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <view class="price-info">
        <text class="price-label">学习价格</text>
        <text class="price">¥{{ courseDetail.price }}</text>
      </view>
      <view class="action-btns">
        <button class="btn btn-consult">咨询</button>
        <button class="btn btn-enroll">立即报名</button>
      </view>
    </view>
  </view>
</s-layout>
</template>

<script setup >
import { ref, onMounted } from 'vue';
import CommunityApi from '@/yehu/api/community';

// 状态栏高度
const statusBarHeight = ref(0);

// 展开的章节
const expandedChapters = ref([0]);

// 课程详情数据
const courseDetail = ref({
  id: 1,
  title: '三螺旋结构在生物学上有什么功能？',
  coverImage: '/static/images/course-cover.png',
  teacherName: '肖博士',
  teacherAvatar: '/static/images/teacher-avatar.png',
  price: 299,
  description: '本课程深入探讨三螺旋结构在生物学中的应用，从基础原理到实际案例，帮助你全面了解这一重要的生物学结构。通过系统学习，你将掌握相关知识并能应用到实际工作中。',
  highlights: [
    '全面讲解三螺旋结构的基础理论和应用价值',
    '专业讲师团队，通俗易懂的教学方式',
    '随时随地学习，不受时间地点限制',
    '完成课程可获得专业证书，提升个人价值'
  ],
  outlines: [
    {
      title: '第一章：三螺旋结构基础',
      lessons: [
        { title: '三螺旋结构概述', duration: '20分钟' },
        { title: '三螺旋结构的发现历史', duration: '25分钟' },
        { title: '三螺旋结构的基本特性', duration: '30分钟' }
      ]
    },
    {
      title: '第二章：三螺旋结构在DNA中的应用',
      lessons: [
        { title: 'DNA三螺旋结构形成机制', duration: '35分钟' },
        { title: '三螺旋DNA的稳定性研究', duration: '28分钟' },
        { title: '三螺旋结构与基因表达调控', duration: '40分钟' }
      ]
    },
    {
      title: '第三章：三螺旋结构在药物开发中的应用',
      lessons: [
        { title: '以三螺旋结构为靶点的药物设计', duration: '45分钟' },
        { title: '三螺旋结构在癌症治疗中的潜力', duration: '38分钟' },
        { title: '三螺旋结构在基因治疗中的应用', duration: '42分钟' }
      ]
    }
  ]
});

// 切换章节展开状态
const toggleChapter = (index) => {
  const idx = expandedChapters.value.findIndex(i => i === index);
  if (idx > -1) {
    expandedChapters.value.splice(idx, 1);
  } else {
    expandedChapters.value.push(index);
  }
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  // 获取状态栏高度
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0;
    }
  });
  
  // 获取页面参数
  const pageParams = uni.getSystemInfoSync();
  // @ts-ignore
  const query = pageParams.query || {};
  // 使用页面参数中的id，默认为1
  const courseId = Number(query.id) || 1;
  
  // 使用CommunityApi获取课程详情
  console.log('加载课程详情，ID:', courseId);
  getCourseDetail(courseId);
});

// 请求课程详情数据的方法
const getCourseDetail = (courseId) => {
  // 使用CommunityApi获取课程/动态详情
  CommunityApi.getPostDetail(courseId)
    .then(res => {
      console.log('获取课程详情成功:', res);
      if (res) {
        // 将帖子数据转换为课程详情格式
        courseDetail.value = {
          ...courseDetail.value,
          id: res.id,
          title: res.productName || '三螺旋结构在生物学上有什么功能？',
          coverImage: res.images && res.images.length > 0 
            ? res.images[0] 
            : '/static/images/course-cover.png',
          teacherName: res.userName || '肖博士',
          teacherAvatar: res.userAvatar || '/static/images/teacher-avatar.png',
          description: res.content || courseDetail.value.description
        };
      }
    })
    .catch(err => {
      console.error('获取课程详情失败', err);
      uni.showToast({
        title: '获取课程详情失败',
        icon: 'none'
      });
    });
};
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 120rpx;
}

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #ffffff;
  
  .navbar-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20rpx;
    
    .back-btn, .placeholder {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .navbar-title {
      font-size: 34rpx;
      font-weight: 500;
      color: #333333;
    }
  }
}

.page-content {
  .course-cover {
    position: relative;
    height: 420rpx;
    
    .cover-image {
      width: 100%;
      height: 100%;
    }
    
    .course-info {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 30rpx 20rpx;
      background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
      
      .course-title {
        font-size: 36rpx;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 20rpx;
        display: block;
      }
      
      .teacher-info {
        display: flex;
        align-items: center;
        
        .teacher-avatar {
          width: 60rpx;
          height: 60rpx;
          border-radius: 50%;
          margin-right: 10rpx;
          border: 2rpx solid #ffffff;
        }
        
        .teacher-name {
          font-size: 28rpx;
          color: #ffffff;
        }
      }
    }
  }
  
  .detail-section {
    background-color: #ffffff;
    padding: 30rpx 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333333;
      margin: 30rpx 0 20rpx;
      position: relative;
      padding-left: 20rpx;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 8rpx;
        width: 6rpx;
        height: 30rpx;
        background-color: #FF5777;
        border-radius: 3rpx;
      }
    }
    
    .course-desc {
      font-size: 28rpx;
      color: #666666;
      line-height: 1.6;
    }
    
    .highlight-list {
      margin-bottom: 20rpx;
      
      .highlight-item {
        display: flex;
        align-items: flex-start;
        margin-bottom: 20rpx;
        
        .highlight-icon {
          width: 40rpx;
          height: 40rpx;
          background-color: #FF5777;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;
          flex-shrink: 0;
          
          text {
            font-size: 24rpx;
            color: #ffffff;
          }
        }
        
        .highlight-text {
          font-size: 28rpx;
          color: #666666;
          line-height: 1.5;
          flex: 1;
        }
      }
    }
    
    .outline-list {
      .outline-item {
        margin-bottom: 20rpx;
        
        .outline-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20rpx;
          background-color: #f8f8f8;
          border-radius: 10rpx;
          
          .chapter-title {
            font-size: 30rpx;
            font-weight: 500;
            color: #333333;
          }
        }
        
        .chapter-lessons {
          padding: 0 20rpx;
          
          .lesson-item {
            display: flex;
            align-items: center;
            padding: 20rpx 0;
            border-bottom: 1rpx solid #f0f0f0;
            
            .lesson-title {
              flex: 1;
              font-size: 28rpx;
              color: #666666;
              margin: 0 20rpx;
            }
            
            .lesson-duration {
              font-size: 24rpx;
              color: #999999;
            }
          }
        }
      }
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .price-info {
    .price-label {
      font-size: 24rpx;
      color: #999999;
      margin-right: 10rpx;
    }
    
    .price {
      font-size: 38rpx;
      font-weight: 600;
      color: #FF5777;
    }
  }
  
  .action-btns {
    display: flex;
    
    .btn {
      height: 80rpx;
      line-height: 80rpx;
      padding: 0 40rpx;
      font-size: 28rpx;
      border-radius: 40rpx;
      margin-left: 20rpx;
      
      &.btn-consult {
        background-color: #F8F8F8;
        color: #666666;
      }
      
      &.btn-enroll {
        background-color: #FF5777;
        color: #ffffff;
      }
    }
  }
}
</style> 