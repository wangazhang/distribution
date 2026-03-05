<!-- 分佣提现 -->
<template>
  <s-layout title="申请提现" class="withdraw-wrap" navbar="inner">
    <view class="page-bg"></view>
    <view
      class="wallet-num-box ss-flex ss-col-center ss-row-between"
      :style="[
        {
          marginTop: '-' + Number(statusBarHeight + 88) + 'rpx',
          paddingTop: Number(statusBarHeight + 108) + 'rpx',
        },
      ]"
    >
      <view class="">
        <view class="num-title">可提现金额（元）</view>
        <view class="wallet-num">{{ fen2yuan(state.brokerageInfo.brokeragePrice) }}</view>
      </view>
      <button
        class="ss-reset-button log-btn"
        @tap="sheep.$router.go('/yehu/pages/mine/account/withdraw/detail/index')"
      >
        提现记录
      </button>
    </view>
    <!-- 提现输入卡片-->
    <view class="draw-card">
      <view class="bank-box ss-flex ss-col-center ss-row-between ss-m-b-30">
        <view class="name">提现方式</view>
        <view class="bank-list ss-flex ss-col-center" @tap="onAccountSelect(true)">
          <view v-if="!state.accountInfo.type" class="empty-text">请选择提现方式</view>
          <view v-else class="selected-account">
            <image
              v-if="getAccountTypeImage(state.accountInfo.type)"
              class="account-type-icon-img"
              :src="getAccountTypeImage(state.accountInfo.type)"
              mode="aspectFit"
            />
            <text v-else class="account-type-icon" :class="getAccountTypeIcon(state.accountInfo.type)"></text>
            <view class="account-info">
              <text class="account-name">{{ getAccountTypeName(state.accountInfo.type) }}</text>
            </view>
          </view>
          <text class="cicon-forward" />
        </view>
      </view>
      <view v-if="needSettleGuideBanner" class="settle-guide">
        <view class="guide-text">
          <text>首次银行卡提现需先完善资料并绑定银行卡。</text>
          <text v-if="settleStatus === 30 && settleRejectReason" class="guide-reason">
            驳回原因：{{ settleRejectReason }}
          </text>
          <text v-else-if="settleStatus === 10" class="guide-reason">
            资料审核中，通过后即可提现。
          </text>
        </view>
        <button class="guide-btn" @tap="openSettlePage()">
          {{ settleStatus === 10 ? '我知道了' : '去完善' }}
        </button>
      </view>
      <view
        v-if="state.accountInfo.type === '2' && activeBankCard"
        class="settle-card"
        @tap="openBankInfoModal"
      >
        <view class="settle-card__info">
          <text class="settle-card__bank">{{ activeBankCard ? activeBankCard.bankName : '银行' }}</text>
          <text class="settle-card__no">
            {{ activeBankCard ? maskCardNo(activeBankCard.bankAccountNo) : '**** **** **** ****' }}
          </text>
          <text class="settle-card__holder">
            {{ activeBankCard ? activeBankCard.bankAccountName : '' }}
          </text>
        </view>
        <!-- 换卡按钮阻止冒泡，避免误触弹窗 -->
        <button class="change-card-btn" @tap.stop="openChangeCard">换卡</button>
      </view>
      <!-- 提现金额 -->
      <view class="card-title">提现金额</view>
      <view class="input-box ss-flex ss-col-center border-bottom">
        <view class="unit">￥</view>
        <uni-easyinput
          :inputBorder="false"
          class="ss-flex-1 ss-p-l-10"
          v-model="state.accountInfo.price"
          type="number"
          placeholder="请输入提现金额"
        />
      </view>
      <!-- 提现账号 -->
      <view class="card-title" v-show="['3', '4', '5'].includes(state.accountInfo.type)">
        提现账号
      </view>
      <view
        class="input-box ss-flex ss-col-center border-bottom"
        v-show="['3', '4'].includes(state.accountInfo.type)"
      >
        <view class="unit" />
        <uni-easyinput
          :inputBorder="false"
          class="ss-flex-1 ss-p-l-10"
          v-model="state.accountInfo.accountNo"
          :disabled="bankFieldsReadonly"
          placeholder="请输入提现账号"
        />
      </view>
      <!-- 收款码 -->
      <view class="card-title" v-show="['3', '4'].includes(state.accountInfo.type)">收款码</view>
      <view
        class="input-box ss-flex ss-col-center"
        v-show="['3', '4'].includes(state.accountInfo.type)"
      >
        <view class="unit" />
        <view class="upload-img">
          <s-uploader
            v-model:url="state.accountInfo.accountQrCodeUrl"
            fileMediatype="image"
            limit="1"
            mode="grid"
            :imageStyles="{ width: '168rpx', height: '168rpx' }"
            @success="(payload) => state.accountInfo.accountQrCodeUrl = payload.tempFilePaths[0]"
          />
        </view>
      </view>
      <!-- 银行卡信息在资料卡片中展示，非银行卡提现保留输入 -->
      <button class="ss-reset-button save-btn ui-BG-Main-Gradient ui-Shadow-Main" @tap="onConfirm">
        确认提现
      </button>
    </view>

    <!-- 提现说明 -->
    <view class="draw-notice">
      <view class="title ss-m-b-30">提现说明</view>
      <view class="draw-list"> 最低提现金额 {{ fen2yuan(state.minPrice) }} 元 </view>
      <view class="draw-list">
        冻结佣金：<text>￥{{ fen2yuan(state.brokerageInfo.frozenPrice) }}</text>
        （每笔佣金的冻结期为 {{ state.frozenDays }} 天，到期后可提现）
      </view>
    </view>

    <!-- 原有的账户类型选择（保留兼容） -->
    <account-type-select
      :show="state.accountSelect"
      @close="onAccountSelect(false)"
      round="10"
      v-model="state.accountInfo"
      :methods="state.withdrawTypes"
    />

    <!-- 点击遮罩层关闭银行卡信息弹窗 -->
    <view v-if="bankInfoModal.visible" class="bank-info-mask" @tap="closeBankInfoModal">
      <view class="bank-info-modal" @tap.stop>
        <view class="bank-info-header">
          <text>银行卡信息</text>
          <text class="bank-info-close" @tap="closeBankInfoModal">关闭</text>
        </view>
        <view class="bank-info-body">
          <view class="bank-info-row">
            <text class="label">提现账号</text>
            <text>{{ activeBankCard ? activeBankCard.bankAccountNo : '-' }}</text>
          </view>
          <view class="bank-info-row">
            <text class="label">持卡人</text>
            <text>{{ activeBankCard ? activeBankCard.bankAccountName : '-' }}</text>
          </view>
          <view class="bank-info-row">
            <text class="label">提现银行</text>
            <text>{{ activeBankCard ? activeBankCard.bankName : '-' }}</text>
          </view>
          <view class="bank-info-row">
            <text class="label">开户地址</text>
            <text>{{ settleProfile?.bankBranchName || '-' }}</text>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { computed, onBeforeMount, reactive, ref, watch } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import accountTypeSelect from './components/account-type-select.vue';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import TradeConfigApi from '@/sheep/api/trade/config';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import DictApi from '@/sheep/api/system/dict';
  import { getSettleProfile } from '@/sheep/api/pay/settle';
  import SLayout from '@/sheep/components/s-layout/s-layout.vue';

  const headerBg = sheep.$url.css('/static/img/shop/user/withdraw_bg.png');
  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;

  const userStore = sheep.$store('user');
  const userInfo = computed(() => userStore.userInfo);
  const state = reactive({
    accountInfo: {
      // 提现表单
      type: undefined,
      accountNo: undefined,
      accountQrCodeUrl: undefined,
      name: undefined,
      bankName: undefined,
      bankAddress: undefined,
      price: '', // 提现金额
    },

    accountSelect: false,

    brokerageInfo: {}, // 分销信息

    frozenDays: 0, // 冻结天数
    minPrice: 0, // 最低提现金额
    withdrawTypes: [], // 提现方式
    bankList: [], // 银行字典数据
    bankListSelectedIndex: '', // 选中银行 bankList 的 index
    savedAccountMap: {}, // 最近使用的提现账户记录
  });
  const bankInfoModal = reactive({ visible: false });

  // ======= 首信易实名状态缓存：提现方式切到“银行卡”时触发 =======
  const settleProfile = ref(null);
  let settleGuideInitialized = false;
  let settleGuideThrottle = 0;

  const settleStatus = computed(() => settleProfile.value?.status ?? 0);
  const activeBankCard = computed(() => {
    if (settleProfile.value?.status === 20) {
      return {
        bankName: settleProfile.value.bankName,
        bankAccountNo: settleProfile.value.bankAccountNo,
        bankAccountName: settleProfile.value.bankAccountName,
      };
    }
    return null;
  });
  const settleRejectReason = computed(() => settleProfile.value?.rejectReason || '');
  const supportBankWithdraw = computed(() =>
    (state.withdrawTypes || []).some((item) => String(item) === '2'),
  );
  const needSettleGuideBanner = computed(
    () =>
      String(state.accountInfo.type ?? '') === '2' &&
      settleStatus.value !== 20 &&
      supportBankWithdraw.value,
  );
  const bankFieldsReadonly = computed(
    () => String(state.accountInfo.type ?? '') === '2',
  );

  // 查询提现资料最新状态，用于提示是否需要先入网/绑卡
  const fetchSettleProfile = async () => {
    try {
      const resp = await getSettleProfile();
      console.log('[withdraw] fetchSettleProfile ->', resp);
      if (resp?.code === 0) {
        settleProfile.value = resp.data || null;
      } else {
        settleProfile.value = null;
      }
      if (settleProfile.value?.status === 20) {
        prefillBankFromProfile(settleProfile.value);
        preferBankWithdrawType();
      }
    } catch (error) {
      console.error('获取提现资料失败', error);
    }
  };

  const openSettlePage = (mode = 'declare') => {
    const query = mode === 'change' ? '?mode=change' : '';
    sheep.$router.go(`/pages/commission/withdraw-info${query}`);
  };
  const openChangeCard = () => openSettlePage('change');

  // 弹出引导文案：首次绑卡/审核中/被驳回场景
  const showSettleGuide = (status, reason) => {
    const now = Date.now();
    if (now - settleGuideThrottle < 1000) {
      return;
    }
    settleGuideThrottle = now;
    let content = '首次银行卡提现需先补充实名认证资料并绑定银行卡。';
    let confirmText = '去完善';
    let showCancel = true;
    const finalStatus = status ?? 0;
    if (finalStatus === 10) {
      content = '资料审核中，通过后即可自动打款。';
      confirmText = '我知道了';
      showCancel = false;
    } else if (finalStatus === 30 && reason) {
      content = `资料被驳回：${reason}`;
    }
    uni.showModal({
      title: '完善提现资料',
      content,
      confirmText,
      showCancel,
      success: ({ confirm }) => {
        if (finalStatus === 10) {
          return;
        }
        if (confirm) {
          openSettlePage();
        }
      },
    });
  };

  // 判断银行卡提现前提是否满足：如果未通过则引导补资料
  const ensureSettleReady = async (withGuide = false) => {
    if (String(state.accountInfo.type ?? '') !== '2') {
      return true;
    }
    if (!settleProfile.value) {
      await fetchSettleProfile();
    }
    const profile = settleProfile.value;
    console.log('[withdraw] ensureSettleReady profile/status', profile, profile?.status)
    const status = profile?.status ?? 0;
    if (status === 20) {
      prefillBankFromProfile(profile);
      preferBankWithdrawType();
      return true;
    }
    if (withGuide) {
      showSettleGuide(status, profile?.rejectReason);
    }
    return false;
  };

  // 重置需要持久化的账户字段
  const clearAccountFields = () => {
    state.accountInfo.accountNo = undefined;
    state.accountInfo.accountQrCodeUrl = undefined;
    state.accountInfo.name = undefined;
    state.accountInfo.bankName = undefined;
    state.accountInfo.bankAddress = undefined;
    state.bankListSelectedIndex = '';
  };

  // 根据已选择的银行名称同步 picker 选中态
  function syncBankSelection() {
    if (state.accountInfo.type !== '2' || !Array.isArray(state.bankList)) {
      state.bankListSelectedIndex = '';
      return;
    }
    const bankLabel = state.accountInfo.bankName;
    const targetName = settleProfile.value?.bankName;
    const index = state.bankList.findIndex(
      (item) =>
        item.label === bankLabel ||
        item.value === bankLabel ||
        (!!targetName && (item.value === targetName || item.label === targetName)),
    );
    state.bankListSelectedIndex = index >= 0 ? index : '';
  }

  const prefillBankFromProfile = (profile) => {
    if (!profile || profile.status !== 20) {
      return;
    }
    state.accountInfo.accountNo = profile.bankAccountNo || state.accountInfo.accountNo;
    state.accountInfo.name = profile.bankAccountName || state.accountInfo.name;
    state.accountInfo.bankName =
      profile.bankName || profile.bankBranchName || state.accountInfo.bankName;
    state.accountInfo.bankAddress = profile.bankBranchName || state.accountInfo.bankAddress;
    syncBankSelection();
  };

  const preferBankWithdrawType = () => {
    if (!supportBankWithdraw.value) {
      return;
    }
    if (String(state.accountInfo.type ?? '') !== '2') {
      state.accountInfo.type = '2';
    }
  };

  // 按提现方式回填最近一次使用的账户信息
  const applySavedAccount = (type) => {
    const typeStr = String(type ?? '');
    if (typeStr === '2') {
      return;
    }
    clearAccountFields();
    if (!['3', '4'].includes(typeStr)) {
      return;
    }
    const saved = state.savedAccountMap[typeStr];
    if (!saved) {
      return;
    }
    state.accountInfo.accountNo = saved.accountNo || undefined;
    state.accountInfo.accountQrCodeUrl = saved.accountQrCodeUrl || undefined;
    if (typeStr === '2') {
      state.accountInfo.name = saved.name || undefined;
      state.accountInfo.bankName = saved.bankName || undefined;
      state.accountInfo.bankAddress = saved.bankAddress || undefined;
    }
    syncBankSelection();
  };

  // 切换提现方式时注入历史账户 & 校验是否需要实名
  watch(
    () => state.accountInfo.type,
    async (type) => {
      applySavedAccount(type);
      if (String(type ?? '') === '2') {
        await ensureSettleReady(settleGuideInitialized);
      }
      if (!settleGuideInitialized) {
        settleGuideInitialized = true;
      }
    },
    { immediate: true }
  );

  // 获取最近使用的提现账户
  const fetchSavedAccounts = async () => {
    try {
      const { code, data } = await BrokerageApi.getLatestWithdrawAccounts();
      if (code !== 0) {
        return;
      }
      const map = {};
      if (Array.isArray(data)) {
        data.forEach((item) => {
          if (item && item.type !== undefined && item.type !== null) {
            map[String(item.type)] = item;
          }
        });
      }
      const needPrefill =
        ['2', '3', '4'].includes(String(state.accountInfo.type ?? '')) &&
        !state.accountInfo.accountNo &&
        !state.accountInfo.accountQrCodeUrl &&
        !state.accountInfo.name &&
        !state.accountInfo.bankName &&
        !state.accountInfo.bankAddress;
      state.savedAccountMap = map;
      if (needPrefill) {
        applySavedAccount(state.accountInfo.type);
      } else if (state.accountInfo.type === '2') {
        syncBankSelection();
      }
    } catch (error) {
      console.error('获取提现账户失败', error);
    }
  };

  // 获取账户类型图标
  const getAccountTypeIcon = (type) => {
    const iconMap = {
      1: 'cicon-wallet',
      2: 'cicon-bank-card',
      3: 'cicon-wechat',
      4: 'cicon-alipay',
      5: 'cicon-wechat-pay',
    };
    return iconMap[type] || 'cicon-wallet';
  };

  // 获取账户类型图片
  const getAccountTypeImage = (type) => {
    const imageMap = {
      2: '/static/img/shop/pay/bank.png',
      3: '/static/img/shop/pay/wechat.png',
    };
    return imageMap[type];
  };

  // 获取账户类型名称
  const getAccountTypeName = (type) => {
    const nameMap = {
      1: '钱包余额',
      2: '银行卡转账',
      3: '微信账户',
      4: '支付宝账户',
      5: '微信零钱',
    };
    return nameMap[type] || '';
  };
  const maskCardNo = (value) => {
    if (!value) {
      return '';
    }
    const card = String(value).replace(/\s+/g, '');
    if (card.length <= 8) {
      return card;
    }
    return `${card.slice(0, 4)} **** **** ${card.slice(-4)}`;
  };

  const openBankInfoModal = () => {
    if (!activeBankCard.value) {
      openSettlePage();
      return;
    }
    bankInfoModal.visible = true;
  };

  const closeBankInfoModal = () => {
    bankInfoModal.visible = false;
  };

  const handleChangeCardFromModal = () => {
    closeBankInfoModal();
    openChangeCard();
  };

  // 打开提现方式的弹窗（保留原有功能）
  const onAccountSelect = (e) => {
    state.accountSelect = e;
  };

  // 提交提现
  const onConfirm = async () => {
    // 参数校验
    //debugger;
    if (
      !state.accountInfo.price ||
      state.accountInfo.price > state.brokerageInfo.price ||
      state.accountInfo.price <= 0
    ) {
      sheep.$helper.toast('请输入正确的提现金额');
      return;
    }
    if (!state.accountInfo.type) {
      sheep.$helper.toast('请选择提现方式');
      return;
    }

    const type = String(state.accountInfo.type);
    // 银行转账需先通过实名校验，避免用户多次输入
    if (type === '2') {
      const settleOk = await ensureSettleReady(true);
      if (!settleOk) {
        return;
      }
    }

    // 构建提现请求数据
    const withdrawData = {
      price: Number(state.accountInfo.price) * 100,
      type: Number(state.accountInfo.type),
    };

    if (['2', '3', '4'].includes(type)) {
      if (!state.accountInfo.accountNo) {
        sheep.$helper.toast('请输入提现账号');
        return;
      }
      withdrawData.accountNo = state.accountInfo.accountNo;
    }
    if (['3', '4'].includes(type)) {
      if (!state.accountInfo.accountQrCodeUrl) {
        sheep.$helper.toast('请上传收款码');
        return;
      }
      withdrawData.accountQrCodeUrl = state.accountInfo.accountQrCodeUrl;
    }
    if (type === '2') {
      if (!state.accountInfo.name) {
        sheep.$helper.toast('请输入持卡人姓名');
        return;
      }
      if (!state.accountInfo.bankName) {
        sheep.$helper.toast('请选择银行');
        return;
      }
      withdrawData.name = state.accountInfo.name;
      withdrawData.bankName = state.accountInfo.bankName;
      withdrawData.bankAddress = state.accountInfo.bankAddress;
    }

    // 提交请求
    let { code } = await BrokerageApi.createBrokerageWithdraw(withdrawData);
    if (code !== 0) {
      return;
    }
    // 提示
    uni.showModal({
      title: '操作成功',
      content: '您的提现申请已成功提交',
      cancelText: '继续提现',
      confirmText: '查看记录',
      success: (res) => {
        fetchSavedAccounts();
        if (res.confirm) {
          sheep.$router.go('/pages/commission/wallet', { type: 2 });
          return;
        }
        getBrokerageUser();
        state.accountInfo.price = '';
        clearAccountFields();
      },
    });
  };

  // 获得分销配置
  async function getWithdrawRules() {
    let { code, data } = await TradeConfigApi.getTradeConfig();
    if (code !== 0) {
      return;
    }
    if (data) {
      state.minPrice = data.brokerageWithdrawMinPrice || 0;
      state.frozenDays = data.brokerageFrozenDays || 0;
      state.withdrawTypes = data.brokerageWithdrawTypes;
      if (
        !state.accountInfo.type &&
        Array.isArray(data.brokerageWithdrawTypes) &&
        data.brokerageWithdrawTypes.length > 0
      ) {
        state.accountInfo.type = String(data.brokerageWithdrawTypes[0]);
      }
      if (settleProfile.value?.status === 20) {
        preferBankWithdrawType();
      }
    }
  }

  // 获得分销信息
  async function getBrokerageUser() {
    const { data, code } = await BrokerageApi.getBrokerageUser();
    if (code === 0) {
      state.brokerageInfo = data;
    }
  }

  // 获取提现银行配置字典
  async function getDictDataListByType() {
    let { code, data } = await DictApi.getDictDataListByType('brokerage_bank_name');
    if (code !== 0) {
      return;
    }
    state.bankList = Array.isArray(data) ? data : [];
    syncBankSelection();
  }

  // 银行选择
  function bankChange(e) {
    const value = e.detail.value;
    state.bankListSelectedIndex = value;
    state.accountInfo.bankName = state.bankList[value].label;
  }

  onBeforeMount(() => {
    getWithdrawRules();
    getBrokerageUser();
    getDictDataListByType(); //获取银行字典数据
    fetchSavedAccounts();
    fetchSettleProfile();

    // 如需恢复测试账号预填充，可取消下方的注释
    /*
    setTimeout(() => {
      state.accountInfo.type = '2';
      state.accountInfo.price = '0.01';
      state.accountInfo.accountNo = '6222021203025258301';
      state.accountInfo.name = '王章';
      state.accountInfo.bankName = '工商银行';
      state.accountInfo.bankAddress = '浙江温州龙湾支行';
      const bankIndex = state.bankList.findIndex(bank => bank.label === '工商银行');
      if (bankIndex !== -1) {
        state.bankListSelectedIndex = bankIndex;
      }
    }, 1000);
    */
  });

  onShow(() => {
    fetchSettleProfile();
  });
</script>

<style lang="scss" scoped>
  :deep() {
    .uni-input-input {
      font-family: OPPOSANS !important;
    }
  }

  .wallet-num-box {
    padding: 0 40rpx 80rpx;
    background: var(--ui-BG-Main) v-bind(headerBg) center/750rpx 100% no-repeat;
    border-radius: 0 0 5% 5%;

    .num-title {
      font-size: 26rpx;
      font-weight: 500;
      color: $white;
      margin-bottom: 20rpx;
    }

    .wallet-num {
      font-size: 60rpx;
      font-weight: 500;
      color: $white;
      font-family: OPPOSANS;
    }

    .log-btn {
      width: 170rpx;
      height: 60rpx;
      line-height: 60rpx;
      border: 1rpx solid $white;
      border-radius: 30rpx;
      padding: 0;
      font-size: 26rpx;
      font-weight: 500;
      color: $white;
    }
  }

  // 提现输入卡片
  .draw-card {
    background-color: $white;
    border-radius: 20rpx;
    width: 690rpx;
    min-height: 560rpx;
    margin: -60rpx 30rpx 30rpx 30rpx;
    padding: 30rpx;
    position: relative;
    z-index: 3;
    box-sizing: border-box;

    .card-title {
      font-size: 30rpx;
      font-weight: 500;
      margin-bottom: 30rpx;
    }

    .settle-guide {
      background: #fff7ef;
      border-radius: 16rpx;
      padding: 20rpx;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24rpx;

      .guide-text {
        font-size: 24rpx;
        color: #8a4b08;
        line-height: 1.5;
        flex: 1;

        .guide-reason {
          display: block;
          margin-top: 8rpx;
          font-size: 24rpx;
          color: #d5451d;
        }
      }

      .guide-btn {
        margin-left: 16rpx;
        padding: 0rpx 20rpx;
        border-radius: 999rpx;
        border: none;
        background: linear-gradient(90deg, #ff9f54 0%, #ff6b2b 100%);
        color: #fff;
        font-size: 24rpx;
      }
    }

    .settle-card {
      background: linear-gradient(135deg, #f3f6ff, #ffffff);
      border-radius: 18rpx;
      padding: 24rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border: 1rpx solid #edf1ff;
      box-shadow: 0 10rpx 20rpx rgba(59, 86, 201, 0.08);
      margin-bottom: 24rpx;
      cursor: pointer;
    }

    .settle-card__info {
      display: flex;
      flex-direction: column;
      gap: 8rpx;
    }

    .settle-card__bank {
      font-size: 30rpx;
      font-weight: 600;
      color: #1f2555;
    }

    .settle-card__no {
      font-size: 34rpx;
      letter-spacing: 4rpx;
      color: #2f3c7e;
    }

    .settle-card__holder {
      font-size: 24rpx;
      color: #848aa5;
    }

    .change-card-btn {
      padding: 0rpx 40rpx;
      border-radius: 999rpx;
      font-size: 22rpx;
      border: 1rpx solid rgba(75, 99, 192, 0.4);
      background: rgba(255, 255, 255, 0.9);
      color: #4b63c0;
      box-shadow: none;
    }

    .change-card-btn.subtle {
      padding: 12rpx 0;
      width: 100%;
      border-color: rgba(75, 99, 192, 0.25);
      background: transparent;
    }

    .bank-box {
      .name {
        font-size: 28rpx;
        font-weight: 500;
      }

      .bank-list {
        .empty-text {
          font-size: 28rpx;
          font-weight: 400;
          color: $dark-9;
        }

        .selected-account {
          display: flex;
          align-items: center;
          gap: 10rpx;
          flex: 1;
        }

        .account-type-icon {
          font-size: 28rpx;
          color: var(--ui-BG-Main);
        }

        .account-type-icon-img {
          width: 28rpx;
          height: 28rpx;
        }

        .account-info {
          display: flex;
          flex-direction: column;
          flex: 1;
        }

        .account-name {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
        }

        .account-detail {
          font-size: 24rpx;
          color: #999;
          margin-top: 2rpx;
        }

        .default-tag {
          background: var(--ui-BG-Main);
          color: #fff;
          font-size: 20rpx;
          padding: 2rpx 8rpx;
          border-radius: 8rpx;
        }

        .cicon-forward {
          color: $dark-9;
        }
      }



      .input-box {
        width: 624rpx;
        height: 100rpx;
        margin-bottom: 40rpx;

        .unit {
          font-size: 48rpx;
          color: #333;
          font-weight: 500;
        }

        .uni-easyinput__placeholder-class {
          font-size: 30rpx;
          height: 36rpx;
        }

        :deep(.uni-easyinput__content-input) {
          font-size: 48rpx;
        }
      }

      .save-btn {
        width: 616rpx;
        height: 86rpx;
        line-height: 86rpx;
        border-radius: 40rpx;
        margin-top: 80rpx;
      }
    }

    .bind-box {
      .placeholder-text {
        font-size: 26rpx;
        color: $dark-9;
      }

      .add-btn {
        width: 100rpx;
        height: 50rpx;
        border-radius: 25rpx;
        line-height: 50rpx;
        font-size: 22rpx;
        color: var(--ui-BG-Main);
        background-color: var(--ui-BG-Main-light);
      }
    }

    .input-box {
      width: 624rpx;
      height: 100rpx;
      margin-bottom: 40rpx;

      .unit {
        font-size: 48rpx;
        color: #333;
        font-weight: 500;
      }

      .uni-easyinput__placeholder-class {
        font-size: 30rpx;
      }

      :deep(.uni-easyinput__content-input) {
        font-size: 48rpx;
      }
    }

    .save-btn {
      width: 616rpx;
      height: 86rpx;
      line-height: 86rpx;
      border-radius: 40rpx;
      margin-top: 80rpx;
    }
  }

  .bank-info-mask {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 99;
    padding: 40rpx;
  }

  .bank-info-modal {
    width: 620rpx;
    background: #fff;
    border-radius: 28rpx;
    padding: 32rpx;
    box-shadow: 0 20rpx 40rpx rgba(15, 24, 68, 0.2);
  }

  .bank-info-header {
    display: flex;
    justify-content: space-between;
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 20rpx;
  }

  .bank-info-close {
    font-size: 26rpx;
    color: #8a8f99;
  }

  .bank-info-body {
    display: flex;
    flex-direction: column;
    gap: 18rpx;
  }

  .bank-info-row {
    display: flex;
    justify-content: space-between;
    font-size: 28rpx;
    color: #1d2333;
  }

  .bank-info-row .label {
    color: #848aa5;
  }

  .bank-info-actions {
    margin-top: 28rpx;
    display: flex;
    justify-content: center;
  }

  // 提现说明
  .draw-notice {
    width: 684rpx;
    background: #ffffff;
    border: 2rpx solid #fffaee;
    border-radius: 20rpx;
    margin: 20rpx 32rpx 0 32rpx;
    padding: 30rpx;
    box-sizing: border-box;

    .title {
      font-weight: 500;
      color: #333333;
      font-size: 30rpx;
    }

    .draw-list {
      font-size: 24rpx;
      color: #999999;
      line-height: 46rpx;
    }
  }
</style>
