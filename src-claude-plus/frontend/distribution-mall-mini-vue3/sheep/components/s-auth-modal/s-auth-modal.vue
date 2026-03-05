<template>
  <!-- 授权弹窗 -->
  <su-popup
    :show="authType !== ''"
    round="10"
    :showClose="true"
    @close="closeAuthModal"
    :zIndex="1001"
  >
    <view class="login-wrap" :class="{ 'mp-simple-wrap': isMpSimple }">
      <!-- 小程序简化登录（优先渲染，直接覆盖原有复杂项） -->
      <view
        v-if="
          sheep.$platform.name === 'WechatMiniProgram' &&
          ['accountLogin', 'smsLogin'].includes(authType)
        "
        class="mp-simple-login ss-flex ss-flex-col ss-row-center ss-col-center"
      >
        <view class="mp-login-title">登录体验更多功能</view>
        <view class="mp-login-icons ss-flex ss-row-center">
          <button class="ss-reset-button mp-icon-btn" @tap="thirdLogin('wechat')">
            <uni-icons type="weixin" size="28" color="#07C160"></uni-icons>
          </button>
          <button
            class="ss-reset-button mp-icon-btn"
            open-type="getPhoneNumber"
            @getphonenumber="getPhoneNumber"
          >
            <uni-icons type="phone-filled" size="28" color="var(--ui-BG-Main)"></uni-icons>
          </button>
        </view>
      </view>

      <!-- 1. 账号密码登录 accountLogin（小程序简化模式下隐藏） -->
      <account-login
        v-if="authType === 'accountLogin' && sheep.$platform.name !== 'WechatMiniProgram'"
        :agreeStatus="state.protocol"
        @onConfirm="onConfirm"
      />

      <!-- 2. 短信登录  smsLogin（小程序简化模式下隐藏） -->
      <sms-login
        v-if="authType === 'smsLogin' && sheep.$platform.name !== 'WechatMiniProgram'"
        :agreeStatus="state.protocol"
        @onConfirm="onConfirm"
      />

      <!-- 3. 忘记密码 resetPassword-->
      <reset-password v-if="authType === 'resetPassword'" />

      <!-- 4. 绑定手机号 changeMobile -->
      <change-mobile v-if="authType === 'changeMobile'" />

      <!-- 5. 修改密码 changePassword-->
      <changePassword v-if="authType === 'changePassword'" />

      <!-- 6. 微信小程序授权 -->
      <mp-authorization v-if="authType === 'mpAuthorization'" />

      <!-- 7. 第三方登录 -->
      <view
        v-if="
          ['accountLogin', 'smsLogin'].includes(authType) &&
          sheep.$platform.name !== 'WechatMiniProgram'
        "
        class="auto-login-box ss-flex ss-flex-col ss-row-center ss-col-center"
      >
        <!-- 7.1 微信小程序的快捷登录 -->
        <view v-if="sheep.$platform.name === 'WechatMiniProgram'" class="ss-flex register-box">
          <view class="register-title">还没有账号?</view>
          <button
            class="ss-reset-button login-btn"
            open-type="getPhoneNumber"
            @getphonenumber="getPhoneNumber"
          >
            快捷登录
          </button>
          <view class="circle" />
        </view>

        <!-- 7.2 微信的公众号、App、小程序的登录，基于 openid + code -->
        <button
          v-if="
            ['WechatOfficialAccount', 'WechatMiniProgram', 'App'].includes(sheep.$platform.name) &&
            sheep.$platform.isWechatInstalled
          "
          @tap="thirdLogin('wechat')"
          class="ss-reset-button auto-login-btn"
        >
          <image
            class="auto-login-img"
            :src="sheep.$url.static('/static/img/shop/platform/wechat.png')"
          />
        </button>

        <!-- 7.3 iOS 登录 TODO 芋艿：等后面搞 App 再弄 -->
        <button
          v-if="sheep.$platform.os === 'ios' && sheep.$platform.name === 'App'"
          @tap="thirdLogin('apple')"
          class="ss-reset-button auto-login-btn"
        >
          <image
            class="auto-login-img"
            :src="sheep.$url.static('/static/img/shop/platform/apple.png')"
          />
        </button>
      </view>

      <!-- 用户协议的勾选 -->
      <view
        v-if="
          ['accountLogin', 'smsLogin'].includes(authType) ||
          (sheep.$platform.name === 'WechatMiniProgram' &&
            ['accountLogin', 'smsLogin'].includes(authType))
        "
        class="agreement-box ss-flex ss-row-center"
        :class="{ shake: currentProtocol }"
      >
        <label class="radio ss-flex ss-col-center" @tap="onChange">
          <radio
            :checked="state.protocol"
            color="var(--ui-BG-Main)"
            style="transform: scale(0.8)"
            @tap.stop="onChange"
          />
          <view class="agreement-text ss-flex ss-col-center ss-m-l-8">
            我已阅读并遵守
            <view class="tcp-text" @tap.stop="onProtocol('用户协议')"> 《用户协议》 </view>
            <view class="agreement-text">与</view>
            <view class="tcp-text" @tap.stop="onProtocol('隐私协议')"> 《隐私协议》 </view>
          </view>
        </label>
      </view>
      <view class="safe-box" :class="{ 'mp-safe': isMpSimple }" />
    </view>
  </su-popup>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import sheep from '@/sheep';
  import accountLogin from './components/account-login.vue';
  import smsLogin from './components/sms-login.vue';
  import resetPassword from './components/reset-password.vue';
  import changeMobile from './components/change-mobile.vue';
  import changePassword from './components/change-password.vue';
  import mpAuthorization from './components/mp-authorization.vue';
  import { closeAuthModal, showAuthModal } from '@/sheep/hooks/useModal';

  const modalStore = sheep.$store('modal');
  // 授权弹窗类型
  const authType = computed(() => modalStore.auth);
  const isMpSimple = computed(
    () =>
      sheep.$platform.name === 'WechatMiniProgram' &&
      ['accountLogin', 'smsLogin'].includes(authType.value),
  );

  const state = reactive({
    protocol: false,
  });

  const currentProtocol = ref(false);

  // 勾选协议
  function onChange() {
    state.protocol = !state.protocol;
  }

  // 查看协议
  function onProtocol(title) {
    closeAuthModal();
    sheep.$router.go('/pages/public/richtext', {
      title,
    });
  }

  // 点击登录 / 注册事件
  function onConfirm(e) {
    currentProtocol.value = e;
    setTimeout(() => {
      currentProtocol.value = false;
    }, 1000);
  }

  // 第三方授权登陆（微信小程序、Apple）
  const thirdLogin = async (provider) => {
    if (!state.protocol) {
      currentProtocol.value = true;
      setTimeout(() => {
        currentProtocol.value = false;
      }, 1000);
      sheep.$helper.toast('请勾选同意');
      return;
    }
    const loginRes = await sheep.$platform.useProvider(provider).login();
    if (loginRes) {
      // 获取用户信息
      const userInfo = await sheep.$store('user').getInfo();
      // 确保获取最新完整用户数据
      await sheep.$store('user').updateUserData();
      closeAuthModal();

      // 登录成功后刷新当前所在页面，社区页等 tab 会立即展示登录态
      sheep.$router.refreshCurrentPage();
      // 如果是社区页则广播一次刷新事件，确保装修内容重新加载
      if (sheep.$router.getCurrentRoute('route') === 'yehu/pages/community/index') {
        uni.$emit('community:refresh-after-login');
      }

      // 微信审核要求：授权完成后不允许再次强制弹出登录弹窗，仅提示用户自行完善资料
      // #ifdef MP-WEIXIN
      if (!userInfo.avatar || !userInfo.nickname) {
        sheep.$helper.toast('登录成功，可前往“我的-资料”完善头像昵称');
      }
      // #endif
    }
  };

  // 微信小程序的"手机号快速验证"
  const getPhoneNumber = async (e) => {
    if (!state.protocol) {
      currentProtocol.value = true;
      setTimeout(() => {
        currentProtocol.value = false;
      }, 1000);
      sheep.$helper.toast('请勾选同意');
      return;
    }
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      sheep.$helper.toast('快捷登录失败');
      return;
    }
    let result = await sheep.$platform.useProvider().mobileLogin(e.detail);
    if (result) {
      await sheep.$store('user').updateUserData();
      closeAuthModal();
      // 手机号快捷登录同样刷新页面，并在社区页触发数据刷新
      sheep.$router.refreshCurrentPage();
      if (sheep.$router.getCurrentRoute('route') === 'yehu/pages/community/index') {
        uni.$emit('community:refresh-after-login');
      }
    }
  };
</script>

<style lang="scss" scoped>
  @import './index.scss';

  .shake {
    animation: shake 0.05s linear 4 alternate;
  }

  @keyframes shake {
    from {
      transform: translateX(-10rpx);
    }
    to {
      transform: translateX(10rpx);
    }
  }

  /* 确保全局授权弹窗的层级高于一切 */
  :deep(.su-popup) {
    z-index: 1001 !important;
  }

  /* 遮罩层样式，确保覆盖整个屏幕 */
  :deep(.su-popup__mask) {
    z-index: 1000 !important;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.7);
  }

  /* 确保弹窗内容在最顶层 */
  .login-wrap {
    position: relative;
    z-index: 1002;
  }

  .register-box {
    position: relative;
    justify-content: center;
    .register-btn {
      color: #999999;
      font-size: 30rpx;
      font-weight: 500;
    }
    .register-title {
      color: #999999;
      font-size: 30rpx;
      font-weight: 400;
      margin-right: 24rpx;
    }
    .or-title {
      margin: 0 16rpx;
      color: #999999;
      font-size: 30rpx;
      font-weight: 400;
    }
    .login-btn {
      color: var(--ui-BG-Main);
      font-size: 30rpx;
      font-weight: 500;
    }
    .circle {
      position: absolute;
      right: 0rpx;
      top: 18rpx;
      width: 8rpx;
      height: 8rpx;
      border-radius: 8rpx;
      background: var(--ui-BG-Main);
    }
  }

  /* 小程序简化模式图标布局样式 */
  .mp-simple-login {
    padding: 12rpx 24rpx 0;
  }
  .mp-login-icons {
    gap: 40rpx;
  }
  .mp-icon-btn {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.06);
  }
  .safe-box {
    height: calc(constant(safe-area-inset-bottom) / 5 * 3);
    height: calc(env(safe-area-inset-bottom) / 5 * 3);
  }

  /* 简化模式标题和高度优化 */
  .mp-login-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin: 12rpx 0 28rpx;
  }
  /* 简化模式下，减少底部安全留白高度 */
  .mp-safe {
    height: 0;
  }
  /* 顶部安全区与关闭按钮定位 */
  :deep(.su-popup__close) {
    top: calc(env(safe-area-inset-top) + 16rpx);
    right: 16rpx;
  }
  /* 小程序简化模式整体高度优化：去除多余底部空白 */
  .mp-simple-wrap {
    padding-bottom: 16rpx;
  }

  /* 图标布局 */
  .mp-login-icons {
    gap: 44rpx;
  }
  .mp-icon-btn {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.06);
  }

  .tcp-text {
    color: var(--ui-BG-Main);
  }

  .agreement-text {
    color: $dark-9;
  }
</style>
