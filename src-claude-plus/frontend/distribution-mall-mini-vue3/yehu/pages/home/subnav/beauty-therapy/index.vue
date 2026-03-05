<template>
  <s-layout navbar="normal">
    <view class="circle-container">
      <!-- 自定义导航栏 -->
<!--      <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">-->
<!--        <view class="navbar-content">-->
<!--          <view class="navbar-left" @tap="goBack">-->
<!--            <uni-icons type="back" size="24" color="#333"></uni-icons>-->
<!--          </view>-->
<!--          <view class="navbar-title">觅她圈层</view>-->
<!--          <view class="navbar-right">-->
<!--            <uni-icons type="home" size="24" color="#333" @tap="goHome"></uni-icons>-->
<!--          </view>-->
<!--        </view>-->
<!--      </view>-->

      <!-- 页面内容 -->
      <view class="page-content">
        <!-- 顶部轮播图 -->
        <swiper class="circle-banner" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="500" circular>
          <swiper-item v-for="(item, index) in bannerList" :key="index">
            <image :src="item.image" mode="aspectFill" class="banner-image"></image>
          </swiper-item>
        </swiper>

        <!-- 圈层简介 -->
        <view class="circle-intro">
          <view class="intro-title">关于觅她圈层</view>
          <view class="intro-content">觅她圈层是一个集美容、健康、社交于一体的高端女性社群，致力于为会员提供优质的生活方式和品质服务。</view>
        </view>

        <!-- 圈层权益 -->
        <view class="circle-benefits">
          <view class="section-title">圈层权益</view>
          <scroll-view scroll-x class="benefits-scroll">
            <view class="benefit-item" v-for="(item, index) in benefitsList" :key="index" @tap="showBenefitDetail(item)">
              <image :src="item.icon" mode="aspectFit" class="benefit-icon"></image>
              <text class="benefit-name">{{ item.name }}</text>
            </view>
          </scroll-view>
        </view>

        <!-- 会员等级 -->
        <view class="member-levels">
          <view class="section-title">会员等级</view>
          <view class="levels-list">
            <view
              class="level-item"
              v-for="(item, index) in memberLevels"
              :key="index"
              :class="{'active': index === activeLevel}"
              @tap="selectLevel(index)"
            >
              <text class="level-name">{{ item.name }}</text>
            </view>
          </view>

          <!-- 等级详情 -->
          <view class="level-details">
            <view class="level-card" :style="{ backgroundColor: memberLevels[activeLevel].color }">
              <view class="level-card-header">
                <text class="level-title">{{ memberLevels[activeLevel].name }}</text>
                <text class="level-condition">{{ memberLevels[activeLevel].condition }}</text>
              </view>
              <view class="level-privileges">
                <view class="privilege-item" v-for="(privilege, idx) in memberLevels[activeLevel].privileges" :key="idx">
                  <uni-icons type="checkmarkempty" color="#fff" size="14"></uni-icons>
                  <text class="privilege-text">{{ privilege }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 最新活动 -->
        <view class="recent-activities">
          <view class="section-title">最新活动</view>
          <view class="activities-list">
            <view class="activity-item" v-for="(item, index) in activitiesList" :key="index" @tap="goToActivityDetail(item)">
              <image :src="item.image" mode="aspectFill" class="activity-image"></image>
              <view class="activity-info">
                <text class="activity-title">{{ item.title }}</text>
                <view class="activity-meta">
                  <text class="activity-time">{{ item.time }}</text>
                  <text class="activity-location">{{ item.location }}</text>
                </view>
                <view class="activity-participants">
                  <view class="avatars-group">
                    <image
                      v-for="(avatar, avatarIndex) in item.participants.slice(0, 3)"
                      :key="avatarIndex"
                      :src="avatar"
                      class="participant-avatar"
                    ></image>
                  </view>
                  <text class="participants-count">{{ item.participants.length }}人参与</text>
                </view>
                <view class="activity-tags">
                  <text class="activity-tag" v-for="(tag, tagIndex) in item.tags" :key="tagIndex">{{ tag }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 加入按钮 -->
        <view class="join-section">
          <view class="join-btn" @tap="joinCircle">立即加入觅她圈层</view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, onMounted } from 'vue';

  // 状态栏高度
  const statusBarHeight = ref(0);

  // 轮播图数据
  const bannerList = ref([
    { image: 'https://cdn.example.com/static/pic/banner/index/1.png' },
    { image: 'https://cdn.example.com/static/pic/banner/index/5.png' },
    { image: 'https://cdn.example.com/static/pic/banner/index/6.png' },
  ]);

  // 圈层权益
  const benefitsList = ref([
    {
      id: 1,
      name: '专属活动',
      icon: 'https://cdn.example.com/static/pic/banner/index/1.png',
      description: '定期举办高端品鉴、沙龙、分享会等活动'
    },
    {
      id: 2,
      name: '专家指导',
      icon: 'https://cdn.example.com/static/pic/banner/index/2.png',
      description: '享受美容、健康等领域专家一对一指导'
    },
    {
      id: 3,
      name: '优先购买',
      icon: 'https://cdn.example.com/static/pic/banner/index/3.png',
      description: '新品上市优先购买权，专属折扣'
    },
    {
      id: 4,
      name: '积分加倍',
      icon: 'https://cdn.example.com/static/pic/banner/index/4.png',
      description: '购物积分最高可获5倍加成'
    },
    {
      id: 5,
      name: '生日礼遇',
      icon: 'https://cdn.example.com/static/pic/banner/index/5.png',
      description: '生日当月享受专属礼品及优惠'
    },
    {
      id: 6,
      name: '私域社群',
      icon: 'https://cdn.example.com/static/pic/banner/index/6.png',
      description: '加入高端私密社群，结识志同道合的好友'
    }
  ]);

  // 会员等级
  const memberLevels = ref([
    {
      name: '觅她白银',
      color: '#C0C0C0',
      condition: '累计消费满1000元',
      privileges: [
        '专属客服一对一服务',
        '生日礼遇',
        '新品9.5折',
        '每月专属活动'
      ]
    },
    {
      name: '觅她黄金',
      color: '#D4AF37',
      condition: '累计消费满5000元',
      privileges: [
        '专属客服一对一服务',
        '生日礼遇',
        '新品8.5折',
        '每月专属活动',
        '免费皮肤检测1次/年'
      ]
    },
    {
      name: '觅她钻石',
      color: '#B9F2FF',
      condition: '累计消费满20000元',
      privileges: [
        '专属客服一对一服务',
        '生日礼遇',
        '新品7.5折',
        '每月专属活动',
        '免费皮肤检测4次/年',
        '明星产品赠送',
        'VIP专属沙龙邀请'
      ]
    },
    {
      name: '觅她黑钻',
      color: '#333333',
      condition: '仅限邀请制',
      privileges: [
        '私人管家服务',
        '专属定制护理方案',
        '产品终身7折',
        '不限次数活动参与',
        '年度豪华套装赠送',
        '国际旅行名额优先'
      ]
    }
  ]);

  // 当前选中的会员等级
  const activeLevel = ref(0);

  // 最新活动
  const activitiesList = ref([
    {
      id: 1,
      title: '春季美肤沙龙',
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      time: '2024-05-25 14:00',
      location: '上海市静安区南京西路会所',
      participants: [
        'https://cdn.example.com/static/pic/banner/index/1.png',
        'https://cdn.example.com/static/pic/banner/index/2.png',
        'https://cdn.example.com/static/pic/banner/index/3.png',
        'https://cdn.example.com/static/pic/banner/index/4.png'
      ],
      tags: ['沙龙', '美肤', '春季']
    },
    {
      id: 2,
      title: '法国葡萄酒品鉴会',
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      time: '2024-06-10 19:00',
      location: '北京市朝阳区三里屯',
      participants: [
        'https://cdn.example.com/static/pic/banner/index/5.png',
        'https://cdn.example.com/static/pic/banner/index/6.png',
        'https://cdn.example.com/static/pic/banner/index/1.png',
        'https://cdn.example.com/static/pic/banner/index/2.png',
        'https://cdn.example.com/static/pic/banner/index/3.png'
      ],
      tags: ['品鉴', '红酒', 'VIP']
    },
    {
      id: 3,
      title: '高端抗衰工作坊',
      image: 'https://cdn.example.com/static/pic/banner/index/3.png',
      time: '2024-06-25 15:00',
      location: '广州市天河区天河城',
      participants: [
        'https://cdn.example.com/static/pic/banner/index/4.png',
        'https://cdn.example.com/static/pic/banner/index/5.png',
        'https://cdn.example.com/static/pic/banner/index/6.png'
      ],
      tags: ['抗衰', '工作坊', '精英']
    }
  ]);

  // 切换会员等级
  const selectLevel = (index) => {
    activeLevel.value = index;
  };

  // 展示权益详情
  const showBenefitDetail = (benefit) => {
    uni.showModal({
      title: benefit.name,
      content: benefit.description,
      showCancel: false
    });
  };

  // 跳转到活动详情
  const goToActivityDetail = (activity) => {
    uni.navigateTo({
      url: `/yehu/pages/community/activity-detail/index?id=${activity.id}`
    });
  };

  // 返回上一页
  const goBack = () => {
    uni.navigateBack();
  };

  // 返回首页
  const goHome = () => {
    uni.switchTab({
      url: '/yehu/pages/home/index'
    });
  };

  // 加入觅她圈层
  const joinCircle = () => {
    uni.showModal({
      title: '加入觅她圈层',
      content: '确认加入觅她圈层吗？年费会员价格为¥1980/年',
      success: (res) => {
        if (res.confirm) {
          uni.showLoading({
            title: '处理中...'
          });

          setTimeout(() => {
            uni.hideLoading();
            uni.showToast({
              title: '加入成功',
              icon: 'success'
            });

            // 加入成功后可以跳转到支付页面或会员页面
            setTimeout(() => {
              uni.navigateTo({
                url: '/yehu/pages/mine/member/index'
              });
            }, 1500);
          }, 1000);
        }
      }
    });
  };

  onMounted(() => {
    // 获取状态栏高度
    uni.getSystemInfo({
      success: (res) => {
        statusBarHeight.value = res.statusBarHeight || 0;
      }
    });
  });
</script>

<style lang="scss" scoped>
  .circle-container {
    min-height: 100vh;
    background-color: #f8f8f8;
  }

  .custom-navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    background-color: #fff;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);

    .navbar-content {
      height: 44px;
      display: flex;
      align-items: center;
      padding: 0 15px;

      .navbar-left, .navbar-right {
        width: 60px;
        display: flex;
        align-items: center;
      }

      .navbar-title {
        flex: 1;
        text-align: center;
        font-size: 18px;
        font-weight: 500;
        color: #333;
      }
    }
  }

  .page-content {
    .circle-banner {
      height: 350rpx;
      width: 100%;

      .banner-image {
        width: 100%;
        height: 100%;
      }
    }

    .circle-intro {
      background-color: #fff;
      padding: 30rpx;
      margin-bottom: 20rpx;

      .intro-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 15rpx;
      }

      .intro-content {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }

    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      padding: 30rpx 30rpx 20rpx;
    }

    .circle-benefits {
      background-color: #fff;
      margin-bottom: 20rpx;

      .benefits-scroll {
        white-space: nowrap;
        padding: 0 20rpx 30rpx;

        .benefit-item {
          display: inline-block;
          width: 160rpx;
          text-align: center;
          margin-right: 30rpx;

          .benefit-icon {
            width: 100rpx;
            height: 100rpx;
            border-radius: 50%;
            margin-bottom: 15rpx;
          }

          .benefit-name {
            font-size: 26rpx;
            color: #333;
          }
        }
      }
    }

    .member-levels {
      background-color: #fff;
      margin-bottom: 20rpx;
      padding-bottom: 30rpx;

      .levels-list {
        display: flex;
        padding: 0 30rpx 20rpx;

        .level-item {
          flex: 1;
          text-align: center;
          padding: 20rpx 0;
          border-bottom: 4rpx solid transparent;

          &.active {
            border-bottom-color: #FF5777;

            .level-name {
              color: #FF5777;
              font-weight: bold;
            }
          }

          .level-name {
            font-size: 28rpx;
            color: #666;
          }
        }
      }

      .level-details {
        padding: 0 30rpx;

        .level-card {
          padding: 30rpx;
          border-radius: 12rpx;
          color: #fff;

          .level-card-header {
            margin-bottom: 20rpx;

            .level-title {
              font-size: 32rpx;
              font-weight: bold;
              display: block;
              margin-bottom: 10rpx;
            }

            .level-condition {
              font-size: 24rpx;
              opacity: 0.8;
            }
          }

          .level-privileges {
            .privilege-item {
              display: flex;
              align-items: center;
              margin-bottom: 15rpx;

              &:last-child {
                margin-bottom: 0;
              }

              .privilege-text {
                font-size: 26rpx;
                margin-left: 10rpx;
              }
            }
          }
        }
      }
    }

    .recent-activities {
      background-color: #fff;
      margin-bottom: 30rpx;

      .activities-list {
        padding: 0 30rpx 30rpx;

        .activity-item {
          margin-bottom: 30rpx;
          border-radius: 12rpx;
          overflow: hidden;
          box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

          &:last-child {
            margin-bottom: 0;
          }

          .activity-image {
            width: 100%;
            height: 300rpx;
          }

          .activity-info {
            padding: 20rpx;

            .activity-title {
              font-size: 28rpx;
              font-weight: bold;
              color: #333;
              margin-bottom: 15rpx;
              display: block;
            }

            .activity-meta {
              display: flex;
              margin-bottom: 15rpx;

              .activity-time, .activity-location {
                font-size: 24rpx;
                color: #999;
              }

              .activity-time {
                margin-right: 20rpx;
              }
            }

            .activity-participants {
              display: flex;
              align-items: center;
              margin-bottom: 15rpx;

              .avatars-group {
                display: flex;
                margin-right: 10rpx;

                .participant-avatar {
                  width: 50rpx;
                  height: 50rpx;
                  border-radius: 50%;
                  border: 2rpx solid #fff;
                  margin-left: -15rpx;

                  &:first-child {
                    margin-left: 0;
                  }
                }
              }

              .participants-count {
                font-size: 24rpx;
                color: #999;
              }
            }

            .activity-tags {
              .activity-tag {
                display: inline-block;
                padding: 4rpx 12rpx;
                background-color: #F8F8F8;
                color: #666;
                font-size: 22rpx;
                border-radius: 6rpx;
                margin-right: 10rpx;
                margin-bottom: 10rpx;
              }
            }
          }
        }
      }
    }

    .join-section {
      padding: 0 30rpx 50rpx;

      .join-btn {
        background-color: #FF5777;
        color: #fff;
        text-align: center;
        padding: 25rpx 0;
        border-radius: 40rpx;
        font-size: 30rpx;
        font-weight: 500;
      }
    }
  }
</style>