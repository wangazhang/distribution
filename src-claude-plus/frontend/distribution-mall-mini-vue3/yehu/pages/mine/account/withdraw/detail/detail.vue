<template>
  <s-layout title="提现详情" back>
    <view v-if="detail" class="withdraw-detail-page">
      <view class="summary-card">
        <view class="summary-header">
          <view class="status-tag" :class="statusClass">
            <view class="status-dot"></view>
            <text class="status-text">{{ detail.statusName || '处理中' }}</text>
          </view>
          <text class="summary-time">更新时间 {{ latestTimeText }}</text>
        </view>

        <view class="amount-box">
          <text class="amount">¥{{ actualAmount }}</text>
          <text class="amount-desc">预计到账金额</text>
        </view>

        <view class="amount-meta">
          <view class="meta-item">
            <text class="meta-label">提现金额</text>
            <text class="meta-value">¥{{ formatMoney(detail.price) }}</text>
          </view>
          <view class="divider"></view>
          <view class="meta-item">
            <text class="meta-label">手续费</text>
            <text class="meta-value">¥{{ formatMoney(detail.feePrice) }}</text>
          </view>
        </view>

        <text class="tips">{{ statusTips }}</text>
      </view>

      <view class="info-card">
        <view class="info-title">金额信息</view>
        <view class="info-row">
          <text class="label">提现金额</text>
          <text class="value">¥{{ formatMoney(detail.price) }}</text>
        </view>
        <view class="info-row">
          <text class="label">手续费</text>
          <text class="value">¥{{ formatMoney(detail.feePrice) }}</text>
        </view>
        <view class="info-row">
          <text class="label">预计到账</text>
          <text class="value highlight">¥{{ actualAmount }}</text>
        </view>
      </view>

      <view class="info-card">
        <view class="info-title">到账账户</view>
        <view class="info-row">
          <text class="label">提现方式</text>
          <text class="value">{{ detail.typeName || '—' }}</text>
        </view>
        <view class="info-row">
          <text class="label">真实姓名</text>
          <text class="value">{{ detail.name || '—' }}</text>
        </view>
        <view class="info-row">
          <text class="label">到账账号</text>
          <text class="value">{{ detail.accountNo || '—' }}</text>
        </view>
        <view class="info-row" v-if="detail.bankName">
          <text class="label">银行名称</text>
          <text class="value">{{ detail.bankName }}</text>
        </view>
        <view class="info-row" v-if="detail.bankAddress">
          <text class="label">开户地址</text>
          <text class="value">{{ detail.bankAddress }}</text>
        </view>
      </view>

      <view class="info-card">
        <view class="info-title">时间信息</view>
        <view class="info-row">
          <text class="label">提交时间</text>
          <text class="value">{{ formatTime(detail.createTime) }}</text>
        </view>
        <view class="info-row">
          <text class="label">审核时间</text>
          <text class="value">{{ formatTime(detail.auditTime) }}</text>
        </view>
        <view class="info-row">
          <text class="label">最近更新时间</text>
          <text class="value">{{ formatTime(detail.updateTime) }}</text>
        </view>
      </view>

      <view class="info-card" v-if="detail.auditReason || isFailed">
        <view class="info-title">处理说明</view>
        <view class="reason-text">{{ detail.auditReason || defaultFailedDesc }}</view>
      </view>

      <!-- <view class="timeline-card">
        <view v-for="item in timelineSteps" :key="item.key" class="timeline-item">
          <view class="timeline-node">
            <view class="dot" :class="{ active: item.active }"></view>
            <view
              class="line"
              v-if="item.key !== 'latest'"
              :class="{ active: item.active }"
            ></view>
          </view>
          <view class="timeline-content">
            <text class="title" :class="{ active: item.active }">{{ item.title }}</text>
            <text class="desc" :class="{ active: item.active }">{{ item.desc }}</text>
            <text class="time">{{ item.time }}</text>
          </view>
        </view>
      </view> -->
    </view>

    <view v-else class="loading-state">
      <text>{{ loading ? '加载中...' : '暂无提现详情' }}</text>
    </view>
  </s-layout>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import sheep from '@/sheep';
import BrokerageApi from '@/sheep/api/trade/brokerage';
import { fen2yuan } from '@/sheep/hooks/useGoods';

const detail = ref(null);
const loading = ref(false);

const STATUS_GROUP_MAP = {
  success: [15],
  pending: [0, 10, 12, 13, 14],
  failed: [20, 21],
};

const formatMoney = (price) => {
  return fen2yuan(price || 0);
};

const formatTime = (time) => {
  if (!time) return '--';
  return sheep.$helper.timeFormat(time, 'yyyy-mm-dd hh:MM:ss');
};

const actualAmount = computed(() => {
  if (!detail.value) return '0.00';
  const price = detail.value.price || 0;
  const fee = detail.value.feePrice || 0;
  return fen2yuan(Math.max(price - fee, 0));
});

const latestTimeText = computed(() => {
  if (!detail.value) return '--';
  return formatTime(detail.value.updateTime || detail.value.createTime);
});

const isFailed = computed(() => {
  if (!detail.value) return false;
  return STATUS_GROUP_MAP.failed.includes(detail.value.status);
});

const statusClass = computed(() => {
  if (!detail.value) return 'status-pending';
  if (STATUS_GROUP_MAP.success.includes(detail.value.status)) {
    return 'status-success';
  }
  if (STATUS_GROUP_MAP.failed.includes(detail.value.status)) {
    return 'status-failed';
  }
  return 'status-pending';
});

const defaultFailedDesc = '提现失败，请核对账户信息或联系管理员。';

const statusTips = computed(() => {
  if (!detail.value) return '';
  if (STATUS_GROUP_MAP.success.includes(detail.value.status)) {
    return '预计 1-3 个工作日到账，请注意查收。';
  }
  if (STATUS_GROUP_MAP.failed.includes(detail.value.status)) {
    return detail.value.auditReason || defaultFailedDesc;
  }
  return '提现申请已提交，审核通过后会自动打款。';
});

const timelineSteps = computed(() => {
  if (!detail.value) return [];
  return [
    {
      key: 'create',
      title: '提交申请',
      desc: '提现申请已提交',
      time: formatTime(detail.value.createTime),
      active: true,
    },
    {
      key: 'audit',
      title: '审核记录',
      desc: detail.value.auditReason ? `审核说明：${detail.value.auditReason}` : '审核处理中',
      time: formatTime(detail.value.auditTime),
      active: detail.value.auditTime,
    },
    {
      key: 'latest',
      title: '最新状态',
      desc: detail.value.statusName || '处理中',
      time: formatTime(detail.value.updateTime || detail.value.createTime),
      active: STATUS_GROUP_MAP.success.includes(detail.value.status),
    },
  ];
});

const fetchDetail = async (id) => {
  loading.value = true;
  try {
    const { code, data } = await BrokerageApi.getBrokerageWithdraw(id);
    if (code !== 0) {
      throw new Error('接口返回异常');
    }
    detail.value = data;
  } catch (error) {
    console.error('获取提现详情失败', error);
    sheep.$helper.toast('获取提现详情失败');
  } finally {
    loading.value = false;
  }
};

onLoad((options) => {
  if (!options?.id) {
    sheep.$helper.toast('缺少提现编号');
    return;
  }
  fetchDetail(options.id);
});
</script>

<style lang="scss" scoped>
.withdraw-detail-page {
  min-height: 100vh;
  padding: 32rpx 24rpx 48rpx;
  background-color: #f6f6f6;
}

.summary-card {
  background: linear-gradient(135deg, #8f1911 0%, #b0271d 100%);
  border-radius: 28rpx;
  padding: 40rpx 36rpx 44rpx;
  color: #fff;
  margin-bottom: 28rpx;
  box-shadow: 0 18rpx 40rpx rgba(143, 25, 17, 0.16);

  .summary-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 36rpx;
  }

  .status-tag {
    display: inline-flex;
    align-items: center;
    padding: 12rpx 28rpx;
    border-radius: 999rpx;
    font-size: 26rpx;
    background-color: rgba(255, 255, 255, 0.18);
    color: #fff;
    backdrop-filter: blur(4rpx);
  }

  .status-dot {
    width: 14rpx;
    height: 14rpx;
    border-radius: 50%;
    margin-right: 12rpx;
    background-color: currentColor;
  }

  .summary-time {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.78);
  }

  .amount-box {
    text-align: center;
    margin-bottom: 34rpx;
  }

  .amount {
    font-size: 72rpx;
    font-weight: 700;
  }

  .amount-desc {
    display: block;
    font-size: 26rpx;
    margin-top: 6rpx;
    color: rgba(255, 255, 255, 0.76);
  }

  .amount-meta {
    display: flex;
    align-items: stretch;
    justify-content: space-between;
    border-radius: 24rpx;
    overflow: hidden;
    background-color: rgba(255, 255, 255, 0.16);
    margin-bottom: 30rpx;
  }

  .meta-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 12rpx;
  }

  .meta-label {
    display: block;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.75);
    margin-bottom: 6rpx;
  }

  .meta-value {
    font-size: 34rpx;
    font-weight: 600;
  }

  .divider {
    width: 2rpx;
    background-color: rgba(255, 255, 255, 0.18);
  }

  .tips {
    font-size: 26rpx;
    text-align: center;
    color: rgba(255, 255, 255, 0.85);
    line-height: 1.5;
  }
}

.status-tag.status-success {
  background-color: rgba(46, 204, 113, 0.22);
  color: #2ecc71;
}

.status-tag.status-pending {
  background-color: rgba(247, 183, 49, 0.22);
  color: #f7b731;
}

.status-tag.status-failed {
  background-color: rgba(255, 107, 107, 0.25);
  color: #ff6b6b;
}

.info-card {
  background-color: #fff;
  border-radius: 26rpx;
  padding: 34rpx 30rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 16rpx 36rpx rgba(15, 16, 18, 0.06);

  .info-title {
    position: relative;
    padding-left: 22rpx;
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 28rpx;
    color: #1f1f1f;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 8rpx;
      height: 100%;
      border-radius: 999rpx;
      background: linear-gradient(180deg, #8f1911 0%, #b0271d 100%);
    }
  }

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18rpx 8rpx;
    margin-bottom: 0;
    font-size: 28rpx;
    color: #333;
    border-bottom: 1rpx solid #f1f1f1;

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #666;
    }

    .value {
      font-weight: 500;
      color: #1f1f1f;
    }

    .highlight {
      color: #8f1911;
    }
  }

  .reason-text {
    margin-top: 6rpx;
    padding: 26rpx;
    font-size: 26rpx;
    color: #c8332d;
    line-height: 1.6;
    background-color: #fff3f3;
    border-radius: 20rpx;
  }
}

.timeline-card {
  background-color: #fff;
  border-radius: 26rpx;
  padding: 34rpx 30rpx;
  box-shadow: 0 16rpx 36rpx rgba(15, 16, 18, 0.06);

  .timeline-item {
    display: flex;
    align-items: flex-start;

    &:not(:last-child) {
      margin-bottom: 32rpx;
    }
  }

  .timeline-node {
    width: 44rpx;
    display: flex;
    flex-direction: column;
    align-items: center;

    .dot {
      width: 18rpx;
      height: 18rpx;
      border-radius: 50%;
      background-color: #d8d8d8;
      border: 4rpx solid #fff;
      box-shadow: 0 0 0 4rpx rgba(143, 25, 17, 0.08);

      &.active {
        background-color: #8f1911;
        box-shadow: 0 0 0 4rpx rgba(143, 25, 17, 0.2);
      }
    }

    .line {
      flex: 1;
      width: 2rpx;
      background-color: #eee;
      margin-top: 8rpx;

      &.active {
        background: linear-gradient(180deg, #b0271d 0%, rgba(143, 25, 17, 0));
      }
    }
  }

  .timeline-content {
    flex: 1;
    padding-left: 20rpx;

    .title {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;

      &.active {
        color: #8f1911;
      }
    }

    .desc {
      font-size: 26rpx;
      color: #666;
      margin: 6rpx 0;

      &.active {
        color: #8f1911;
      }
    }

    .time {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.loading-state {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 28rpx;
}
</style>
