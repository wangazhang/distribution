<template>
  <s-layout navbar="community" tabbar="/yehu/pages/community/index" >
    <view class="page-container">
      <!-- 页面内容区域 -->
      <view class="page-content">
        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <uni-load-more status="loading" iconType="circle" iconSize="36"></uni-load-more>
          <text class="loading-text">数据加载中...</text>
        </view>

        <!-- 错误状态 -->
        <view v-else-if="error" class="error-container">
          <uni-icons type="error" size="60" color="#FF5777"></uni-icons>
          <text class="error-text">{{ errorMessage }}</text>
          <view class="retry-btn" @tap="fetchCommunityData">
            <text>重新加载</text>
          </view>
        </view>

        <!-- 正常内容 -->
        <block v-else>
          <!-- 课程宣传区 -->
          <swiper class="course-promo" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="500">
            <swiper-item v-for="(banner, index) in banners" :key="index" @click="handleBannerClick(banner)">
              <image
                class="promo-image"
                :src="banner.image || 'https://cdn.example.com/static/pic/banner/community_banner.png'"
                mode="widthFix"
              ></image>
            </swiper-item>
          </swiper>

          <!-- 学习内容整体区 -->
          <view class="content-block">
            <!-- 功能区域 -->
            <view class="feature-section">
              <view class="feature-item">
                <view class="feature-icon">
                  <uni-icons type="info" size="16" color="#333"></uni-icons>
                </view>
                <view class="feature-text">科学有据</view>
              </view>

              <view class="feature-item">
                <view class="feature-icon">
                  <uni-icons type="staff" size="16" color="#333"></uni-icons>
                </view>
                <view class="feature-text">分班教学</view>
              </view>

              <view class="feature-item">
                <view class="feature-icon">
                  <uni-icons type="medal" size="16" color="#333"></uni-icons>
                </view>
                <view class="feature-text">学习得积分</view>
              </view>
            </view>
            <!-- 标题区域 -->
            <view class="content-title">
              <image
                class="title-logo"
                src="https://cdn.example.com/static/pic/banner/community_banner.png"
                mode="aspectFit"
              ></image>
              <view class="title-text">元气课堂</view>
              <view class="title-divider"></view>
            </view>

            <!-- 三个课程类型 -->
            <view class="course-types">
              <!-- 悦她公开课 -->
              <view class="course-type-item" @click="navigateToCourseDetail(1)">
                <view class="course-image-container">
                  <image
                    class="course-image"
                    src="https://cdn.example.com/static/pic/banner/community_banner.png"
                    mode="aspectFill"
                  ></image>
                </view>
                <view class="course-info">
                  <view class="course-title">悦她公开课</view>
                  <view class="course-desc">肌肤/饮食/生殖 健康知识</view>
                </view>
              </view>

              <!-- 悦她赋能课 -->
              <view class="course-type-item" @click="navigateToCourseDetail(2)">
                <view class="course-image-container">
                  <image
                    class="course-image"
                    src="https://cdn.example.com/static/pic/banner/community_banner.png"
                    mode="aspectFill"
                  ></image>
                </view>
                <view class="course-info">
                  <view class="course-title">悦她赋能课</view>
                  <view class="course-desc">AI+美业的工具赋能</view>
                </view>
              </view>

              <!-- 悦她进阶课 -->
              <view class="course-type-item" @click="navigateToCourseDetail(3)">
                <view class="course-image-container">
                  <image
                    class="course-image"
                    src="https://cdn.example.com/static/pic/banner/community_banner.png"
                    mode="aspectFill"
                  ></image>
                </view>
                <view class="course-info">
                  <view class="course-title">悦她进阶课</view>
                  <view class="course-desc">研修与考证</view>
                </view>
              </view>
            </view>

            <!-- 社区功能区 -->
            <view class="community-functions">
              <!-- 发圈素材 -->
              <view class="function-item" @click="handleBannerClick({path: '/pages/community/material/index'})">
                <image
                  class="function-image"
                  src="https://cdn.example.com/static/pic/banner/community_banner.png"
                  mode="aspectFill"
                ></image>
                <view class="function-info">
                  <view class="function-title">发圈素材</view>
                  <view class="function-desc">每日更新</view>
                </view>
              </view>

              <!-- 笔记社区 -->
              <view class="function-item" @click="handleBannerClick({path: '/pages/community/notes/index'})">
                <image
                  class="function-image"
                  src="https://cdn.example.com/static/pic/banner/community_banner.png"
                  mode="aspectFill"
                ></image>
                <view class="function-info">
                  <view class="function-title">笔记社区</view>
                  <view class="function-desc">悦她小花园</view>
                </view>
              </view>
            </view>
          </view>
        </block>
      </view>

      <!-- 自定义底部导航栏 -->
      <!-- <yh-tabbar activeColor="#FFA0B4" color="#999999"></yh-tabbar> -->
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  // import { getUserInfo } from '@/yehu/api/mine';
  import CommunityApi from '@/yehu/api/community';
  // import uniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';
  // import uniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue';

  // 加载状态
  const loading = ref(true);
  const error = ref(false);
  const errorMessage = ref('数据加载失败，请重试');

  // 用户信息
  const userInfo = ref({});

  // 最新课程信息
  const latestCourse = ref({});

  // 课程模块
  const courseModules = ref([]);

  // 轮播图数据
  const banners = ref([]);

  // 获取数据
  const fetchCommunityData = async () => {
    loading.value = true;
    error.value = false;

    try {
      // 获取社区数据，不再获取用户信息
      const [bannersData, postsData] = await Promise.all([
        CommunityApi.getCommunityBanners(),
        CommunityApi.getPosts(),
      ]);

      // 设置轮播图数据
      if (bannersData && bannersData.length > 0) {
        banners.value = bannersData.map((banner, index) => ({
          id: index + 1,
          image: banner.image,
          title: banner.title || '课程上新',
          teacher: banner.teacher || '肖博士 元气佳妍创始人'
        }));
      }

      // 处理课程模块数据
      // 这里使用帖子数据作为课程模块，实际应用中应使用专门的课程API
      courseModules.value = postsData.slice(0, 3).map((post, index) => ({
        id: index + 1,
        image:
          post.images[0] ||
          'https://cdn.example.com/static/pic/course/course_house_1.png',
        title: post.productName || '觅她课程',
        description: post.content.slice(0, 15) + '...',
      }));

      console.log('社区数据加载完成');
    } catch (err) {
      console.error('获取社区数据失败', err);
      error.value = true;
      errorMessage.value = err.message || '数据加载失败，请重试';
    } finally {
      loading.value = false;
    }
  };

  // 导航到课程详情页
  const navigateToCourseDetail = (courseId) => {
    uni.navigateTo({
      url: `/pages/community/course-detail/index?id=${courseId}`,
    });
  };

  // 处理轮播图点击
  const handleBannerClick = (banner) => {
    if (banner.path) {
      // 如果有path，则跳转到对应页面
      uni.navigateTo({
        url: banner.path,
        fail: () => {
          // 跳转失败时显示提示
          uni.showToast({
            title: '功能正在开发中，敬请期待',
            icon: 'none',
            duration: 2000
          });
        }
      });
    } else {
      // 否则跳转到课程详情页
      navigateToCourseDetail(banner.id);
    }
  };

  onMounted(() => {
    // 获取社区数据
    fetchCommunityData();
  });

  // 定义页面的分享方法
  defineExpose({
    onShareAppMessage() {
      return {
        title: '元气课堂',
        path: '/pages/community/index',
        imageUrl: 'https://cdn.example.com/static/pic/banner/community_banner.png'
      }
    }
  });
</script>

<style lang="scss" scoped>
  .page-container {
    min-height: 100vh;
    background-color: #f8f8f8;
  }

  .custom-navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 999;
    background-color: #ffffff;

    .navbar-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20rpx;
      position: relative;

      .user-info {
        display: flex;
        align-items: center;
        flex: 1;

        .user-avatar {
          width: 65rpx;
          height: 65rpx;
          border-radius: 50%;
        }

        .user-details {
          margin-left: 10rpx;

          .user-name {
            font-size: 25rpx;
            font-weight: 500;
            color: #333333;
            display: block;
          }

          .school-title {
            display: block;
            margin-top: -5rpx;
            font-size: 20rpx;
            color: #666666;
          }
        }
      }

      .share-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 60rpx;
        height: 60rpx;
        background-color: #f5f5f5;
        border-radius: 50%;
      }
    }
  }

  .page-content {
    padding-bottom: 140rpx;

    .course-promo {
      width: 100%;
      margin: 0;
      padding: 0;
      position: relative;
      z-index: 1;
      height: 800rpx;

      .promo-image {
        width: 100%;
        height: 100%;
      }
    }

    // 学习内容整体区样式
    .content-block {
      padding: 30rpx 30rpx 50rpx;
      background-color: #fff;
      border-radius: 30rpx 30rpx 0 0;
      margin-top: -120rpx;
      position: relative;
      z-index: 2;
      box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);

      // 标题区域
      .content-title {
        display: flex;
        align-items: center;
        margin-bottom: 40rpx;

        .title-logo {
          width: 60rpx;
          height: 60rpx;
          margin-right: 15rpx;
        }

        .title-text {
          font-size: 40rpx;
          font-weight: bold;
          color: #333;
          margin-right: 20rpx;
        }

        .title-divider {
          flex: 1;
          height: 2rpx;
          background-color: #f0f0f0;
        }
      }

      // 课程类型区域
      .course-types {
        display: flex;
        justify-content: space-between;
        margin-bottom: 50rpx;

        .course-type-item {
          width: 30%;
          border-radius: 15rpx;
          overflow: hidden;
          box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
          background-color: #fff;

          .course-image-container {
            width: 100%;
            height: 180rpx;
            overflow: hidden;
            border-radius: 15rpx 15rpx 0 0;

            .course-image {
              width: 100%;
              height: 100%;
              border-radius: 15rpx 15rpx 0 0;
            }
          }

          .course-info {
            padding: 20rpx 15rpx;
            text-align: center;

            .course-title {
              font-size: 32rpx;
              font-weight: bold;
              color: #333;
              margin-bottom: 10rpx;
            }

            .course-desc {
              font-size: 18rpx;
              color: #999;
              line-height: 1.4;
            }
          }
        }
      }

      // 功能区域
      .feature-section {
        display: flex;
        justify-content: space-around;
        padding: 30rpx 20rpx;
        border-radius: 15rpx;

        .feature-item {
          display: flex;
          flex-direction: row;
          align-items: center;

          .feature-icon {
            margin-right: 8rpx;
          }

          .feature-text {
            font-size: 24rpx;
            color: #333;
            font-weight: 500;
          }
        }
      }

      // 社区功能区样式
      .community-functions {
        margin-top: 40rpx;
        display: flex;
        gap: 20rpx;

        .function-item {
          flex: 1;
          background: #fff;
          border-radius: 16rpx;
          overflow: hidden;
          box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
          position: relative;

          .function-image {
            width: 100%;
            height: 200rpx;
            display: block;
          }

          .function-info {
            padding: 20rpx;
            background: linear-gradient(180deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.9) 50%, #ffffff 100%);
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;

            .function-title {
              font-size: 32rpx;
              font-weight: bold;
              color: #333;
              margin-bottom: 8rpx;
            }

            .function-desc {
              font-size: 24rpx;
              color: #666;
            }
          }
        }
      }
    }
  }

  // 加载和错误状态样式
  .loading-container,
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;

    .loading-text,
    .error-text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }

    .retry-btn {
      margin-top: 30rpx;
      padding: 16rpx 40rpx;
      background-color: #ffa0b4;
      color: #fff;
      border-radius: 30rpx;
      font-size: 28rpx;
    }
  }
</style>
