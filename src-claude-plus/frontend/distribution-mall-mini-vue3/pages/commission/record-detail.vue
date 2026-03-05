<template>
  <s-layout class="record-detail" title="佣金详情">
    <view v-if="state.loading" class="detail-loading">加载中...</view>
    <template v-else>
      <view v-if="state.detail" class="detail-body">
        <view class="amount-card ui-BG-Main ui-Shadow-Main">
          <view class="amount-value">￥{{ fen2yuan(state.detail.price ?? 0) }}</view>
          <view class="status-tag">{{ state.detail.statusName ?? '-' }}</view>
          <view class="amount-desc">{{ state.detail.title ?? '' }}</view>
        </view>

        <view class="section">
          <view class="section-title">佣金信息</view>
<!--          <view class="info-row">-->
<!--            <text class="label">业务类型</text>-->
<!--            <text class="value">{{ state.detail.bizTypeName ?? '-' }}</text>-->
<!--          </view>-->
          <view class="info-row" v-if="state.detail.bizId">
            <text class="label">业务编号</text>
            <text class="value">{{ state.detail.bizId }}</text>
          </view>
          <view class="info-row">
            <text class="label">佣金金额</text>
            <text class="value highlight">￥{{ fen2yuan(state.detail.price ?? 0) }}</text>
          </view>
          <view class="info-row">
            <text class="label">当前状态</text>
            <text class="value">{{ state.detail.statusName ?? '-' }}</text>
          </view>
          <view class="info-row" v-if="state.detail.description">
            <text class="label">说明</text>
            <text class="value">{{ state.detail.description }}</text>
          </view>
        </view>

        <view class="section">
          <view class="section-title">来源信息</view>
          <view class="user-row" v-if="state.detail.sourceUserId">
            <image
              class="user-avatar"
              :src="state.detail.sourceUserAvatar || defaultAvatar"
              mode="aspectFill"
            />
            <view class="user-info">
              <text class="user-name">{{ state.detail.sourceUserNickname || '匿名用户' }}</text>
              <text class="user-sub">用户编号：{{ state.detail.sourceUserId }}</text>
            </view>
          </view>
          <view class="info-row" v-else>
            <text class="label">来源用户</text>
            <text class="value">-</text>
          </view>
        </view>

        <view class="section">
          <view class="section-title">时间信息</view>
          <view class="info-row">
            <text class="label">创建时间</text>
            <text class="value">{{ formatTime(state.detail.createTime) }}</text>
          </view>
          <view class="info-row" v-if="state.detail.unfreezeTime">
            <text class="label">解冻时间</text>
            <text class="value">{{ formatTime(state.detail.unfreezeTime) }}</text>
          </view>
          <view class="info-row">
            <text class="label">最近更新</text>
            <text class="value">{{ formatTime(state.detail.updateTime) }}</text>
          </view>
          <view class="info-row" v-if="state.detail.frozenDays !== null && state.detail.frozenDays !== undefined">
            <text class="label">冻结天数</text>
            <text class="value">{{ state.detail.frozenDays }} 天</text>
          </view>
        </view>
      </view>
      <s-empty
        v-else
        icon="/static/data-empty.png"
        text="记录不存在或已被删除"
      ></s-empty>
    </template>
  </s-layout>
</template>

<script setup>
import { reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import BrokerageApi from '@/sheep/api/trade/brokerage';
import { fen2yuan } from '@/sheep/hooks/useGoods';

const defaultAvatar = sheep.$url.cdn('/static/img/shop/user/default_avatar.png');

const state = reactive({
  loading: true,
  detail: null,
});

const formatTime = (time) => {
  if (!time) return '-';
  return sheep.$helper.timeFormat(time, 'yyyy-mm-dd hh:MM:ss');
};

const loadDetail = async (id) => {
  state.loading = true;
  try {
    const { code, data } = await BrokerageApi.getBrokerageRecord(id);
    if (code === 0) {
      state.detail = data;
    } else {
      state.detail = null;
    }
  } finally {
    state.loading = false;
  }
};

onLoad((options) => {
  if (options?.id) {
    loadDetail(options.id);
  } else {
    state.loading = false;
  }
});

</script>

<style lang="scss" scoped>
.record-detail {
  //padding: 24rpx;

  .detail-loading {
    display: flex;
    justify-content: center;
    padding-top: 160rpx;
  }

  .detail-body {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
  }

  .amount-card {
    border-radius: 24rpx;
    padding: 32rpx;
    color: #fff;
    display: flex;
    flex-direction: column;
    gap: 16rpx;

    .amount-value {
      font-size: 48rpx;
      font-weight: 600;
    }

    .status-tag {
      font-size: 24rpx;
      opacity: 0.85;
    }

    .amount-desc {
      font-size: 26rpx;
      opacity: 0.9;
    }
  }

  .section {
    background-color: #ffffff;
    border-radius: 24rpx;
    padding: 30rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      margin-bottom: 24rpx;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 18rpx 0;
      font-size: 26rpx;
      border-bottom: 1rpx solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #999999;
      }

      .value {
        color: #333333;
      }

      .value.highlight {
        color: #ff6a00;
        font-weight: 600;
      }
    }

    .user-row {
      display: flex;
      align-items: center;

      .user-avatar {
        width: 92rpx;
        height: 92rpx;
        border-radius: 50%;
        margin-right: 24rpx;
      }

      .user-info {
        display: flex;
        flex-direction: column;
        gap: 10rpx;

        .user-name {
          font-size: 30rpx;
          color: #333333;
        }

        .user-sub {
          font-size: 20rpx;
          color: #999999;
        }
      }
    }
  }
}
</style>
