<template>
  <view>
    <view class="ui-swiper" :class="[props.mode, props.bg, props.ui]">
      <!-- 常规轮播模式 -->
      <swiper
        v-if="!props.centerHighlight"
        :circular="props.circular"
        :current="state.cur"
        :autoplay="props.autoplay && !state.videoPlaySataus"
        :interval="props.interval"
        :duration="props.duration"
        @transition="transition"
        @animationfinish="animationfinish"
        :style="customStyle"
        @change="swiperChange"
      >
        <swiper-item
          class="swiper-item"
          v-for="(item, index) in props.list"
          :key="index"
          :class="{ cur: state.cur == index }"
          @tap="onSwiperItem(item)"
        >
          <view class="ui-swiper-main">
            <image
              v-if="item.type === 'image'"
              class="swiper-image"
              :mode="props.imageMode"
              :src="item.src"
              width="100%"
              height="100%"
              @load="onImgLoad"
            ></image>
            <su-video
              v-else
              :ref="(el) => (refs.videoRef[`video_${index}`] = el)"
              :poster="sheep.$url.cdn(item.poster)"
              :src="sheep.$url.cdn(item.src)"
              :index="index"
              :moveX="state.moveX"
              :initialTime="item.currentTime || 0"
              :height="seizeHeight"
              @videoTimeupdate="videoTimeupdate"
            ></su-video>
          </view>
        </swiper-item>
      </swiper>

      <!-- 中间突出模式（卡片重叠效果） -->
      <view
        v-else
        class="center-highlight-container"
        :style="customStyle"
        @touchstart="touchStart"
        @touchmove="touchMove"
        @touchend="touchEnd"
      >
        <view
          class="card-item"
          v-for="(item, index) in props.list"
          :key="index"
          :class="[getCardPosition(index), { active: index === state.cur }]"
          :style="getCardStyle(index)"
          @tap="onCardTap(item, index)"
        >
          <image
            v-if="item.type === 'image'"
            class="card-image"
            :mode="props.imageMode"
            :src="item.src"
            @load="onImgLoad"
          ></image>
          <su-video
            v-else
            :ref="(el) => (refs.videoRef[`video_${index}`] = el)"
            :poster="sheep.$url.cdn(item.poster)"
            :src="sheep.$url.cdn(item.src)"
            :index="index"
            :moveX="0"
            :initialTime="item.currentTime || 0"
            :height="seizeHeight"
            @videoTimeupdate="videoTimeupdate"
          ></su-video>
        </view>
      </view>

      <template v-if="!state.videoPlaySataus && !props.centerHighlight">
        <view class="ui-swiper-dot" :class="props.dotStyle" v-if="props.dotStyle != 'tag'">
          <view
            class="line-box"
            v-for="(item, index) in props.list"
            :key="index"
            :class="[state.cur == index ? 'cur' : '', props.dotCur]"
          ></view>
        </view>
        <view class="ui-swiper-dot" :class="props.dotStyle" v-if="props.dotStyle == 'tag'">
          <view
            class="ui-tag radius-lg"
            :class="[props.dotCur]"
            style="pointer-events: none; padding: 0 10rpx"
          >
            <view style="transform: scale(0.7)">{{ state.cur + 1 }} / {{ props.list.length }}</view>
          </view>
        </view>
      </template>

      <!-- 卡片模式下的指示器 -->
      <view v-if="props.centerHighlight" class="card-indicator">
        <view
          v-for="(item, index) in props.list"
          :key="index"
          class="card-dot"
          :class="{ active: index === state.cur }"
          @tap="onIndicatorTap(index)"
        ></view>
      </view>
    </view>
  </view>
</template>

<script setup>
  /**
   * 轮播组件
   *
   * @property {Boolean} circular = false  	- 是否采用衔接滑动，即播放到末尾后重新回到开头
   * @property {Boolean} autoplay = true  	- 是否自动切换
   * @property {Number} interval = 5000  		- 自动切换时间间隔
   * @property {Number} duration = 500  		- 滑动动画时长,app-nvue不支持
   * @property {Array} list = [] 				- 轮播数据
   * @property {String} ui = ''  				- 样式class
   * @property {String} mode  				- 模式
   * @property {String} dotStyle  			- 指示点样式
   * @property {String} dotCur= 'ui-BG-Main' 	- 当前指示点样式,默认主题色
   * @property {String} bg  					- 背景
   * @property {String} height = 300  		- 组件高度
   * @property {String} imgHeight = 300   	- 图片高度
   *
   * @example list = [{url:'跳转路径',urlType:'跳转方式',type:'轮播类型',src:'轮播内容地址',poster:'视频必传'}]
   */

  import { reactive, computed, ref, onMounted, watch } from 'vue';
  import sheep from '@/sheep';
  import { clone } from 'lodash-es';

  // 数据
  const state = reactive({
    imgHeight: 0,
    cur: 0,
    moveX: 0,
    videoPlaySataus: false,
    heightList: [],
    autoplayTimer: null,
    touchStartX: 0,
    touchEndX: 0,
    isTouching: false,
    touchDeltaX: 0,
    touchMovePercent: 0,
    animating: false,
  });

  const refs = reactive({
    videoRef: {},
  });

  // 接收参数
  const props = defineProps({
    circular: {
      type: Boolean,
      default: true,
    },
    autoplay: {
      type: Boolean,
      default: false,
    },
    interval: {
      type: Number,
      default: 3000,
    },
    duration: {
      type: Number,
      default: 500,
    },
    mode: {
      type: String,
      default: 'default',
    },
    imageMode: {
      type: String,
      default: 'scaleToFill',
    },
    list: {
      type: Array,
      default() {
        return [];
      },
    },
    dotStyle: {
      type: String,
      default: 'long', //default long tag
    },
    dotCur: {
      type: String,
      default: 'ss-bg-opactity-block',
    },
    bg: {
      type: String,
      default: 'bg-none',
    },
    height: {
      type: Number,
      default: 0,
    },
    imgHeight: {
      type: Number,
      default: 0,
    },
    imgTopRadius: {
      type: Number,
      default: 0,
    },
    imgBottomRadius: {
      type: Number,
      default: 0,
    },
    isPreview: {
      type: Boolean,
      default: false,
    },
    seizeHeight: {
      type: Number,
      default: 200,
    },
    centerHighlight: {
      type: Boolean,
      default: false,
    },
  });

  // 触摸相关事件处理
  const touchStart = (e) => {
    if (state.videoPlaySataus || state.animating) return;
    state.touchStartX = e.touches[0].pageX;
    state.isTouching = true;
    state.touchDeltaX = 0;
    state.touchMovePercent = 0;

    // 暂停自动播放
    if (state.autoplayTimer) {
      stopAutoplay();
    }
  };

  const touchMove = (e) => {
    if (!state.isTouching || state.videoPlaySataus || state.animating) return;

    // 计算滑动距离
    const currentX = e.touches[0].pageX;
    state.touchDeltaX = currentX - state.touchStartX;

    // 计算滑动百分比，限制在一定范围内(-0.5到0.5之间)
    // 屏幕宽度的50%作为基准
    const screenWidth = uni.getSystemInfoSync().windowWidth;
    state.touchMovePercent = Math.max(-0.5, Math.min(0.5, state.touchDeltaX / screenWidth));

    // 记录最后位置用于判断方向
    state.touchEndX = currentX;
  };

  const touchEnd = (e) => {
    if (!state.isTouching || state.videoPlaySataus) return;

    // 计算滑动距离和方向
    const deltaX = state.touchEndX - state.touchStartX;
    const threshold = 50; // 最小滑动距离阈值

    // 设置动画状态
    state.animating = true;
    setTimeout(() => {
      state.animating = false;
    }, 400); // 与动画时长一致

    if (Math.abs(deltaX) > threshold) {
      if (deltaX > 0) {
        // 向右滑动，显示上一张
        prevCard();
      } else {
        // 向左滑动，显示下一张
        nextCard();
      }
    } else {
      // 滑动距离不够，恢复原位
      state.touchMovePercent = 0;
    }

    // 重置触摸状态
    state.isTouching = false;
    state.touchStartX = 0;
    state.touchEndX = 0;
    state.touchDeltaX = 0;

    // 恢复自动播放
    if (props.autoplay) {
      startAutoplay();
    }
  };

  // 获取卡片位置类名和样式
  const getCardPosition = (index) => {
    const curIndex = state.cur;
    const total = props.list.length;

    if (index === curIndex) return 'card-center';

    // 计算左右相对位置
    let diff = index - curIndex;
    if (props.circular) {
      // 处理循环逻辑
      if (diff > total / 2) diff -= total;
      if (diff < -total / 2) diff += total;
    }

    if (diff === -1 || (diff === total - 1 && props.circular)) return 'card-left';
    if (diff === 1 || (diff === -(total - 1) && props.circular)) return 'card-right';

    if (diff < 0) return 'card-far-left';
    return 'card-far-right';
  };

  // 计算卡片的实时样式，用于滑动时的跟手效果
  const getCardStyle = (index) => {
    if (!state.isTouching || state.touchMovePercent === 0) return {};

    const curIndex = state.cur;
    const total = props.list.length;

    // 计算该卡片相对于当前卡片的位置
    let diff = index - curIndex;
    if (props.circular) {
      if (diff > total / 2) diff -= total;
      if (diff < -total / 2) diff += total;
    }

    // 根据不同位置应用不同的移动效果
    const movePercent = state.touchMovePercent;

    // 中心卡片
    if (diff === 0) {
      // 向右滑动(movePercent > 0)时，中心卡片向右移动并缩小
      // 向左滑动(movePercent < 0)时，中心卡片向左移动并缩小
      const translateX = -50 + movePercent * 50; // 基础位置是-50%
      const scale = 1 - Math.abs(movePercent) * 0.2; // 缩小比例
      const opacity = 1 - Math.abs(movePercent) * 0.4; // 降低透明度

      return {
        transform: `translateX(${translateX}%) scale(${scale})`,
        opacity: opacity,
        'transition-duration': '0s',
      };
    }

    // 左侧卡片
    if (diff === -1) {
      // 向右滑动时，左侧卡片向右移动并放大，接近中心位置
      if (movePercent > 0) {
        const translateX = -100 + movePercent * 50; // 从-100%向右移动
        const scale = 0.85 + movePercent * 0.2; // 从0.85放大到1
        const opacity = 0.65 + movePercent * 0.4; // 从0.65提高到1

        return {
          transform: `translateX(${translateX}%) scale(${scale})`,
          opacity: opacity,
          'transition-duration': '0s',
        };
      }

      // 向左滑动时，左侧卡片继续左移
      return {
        transform: `translateX(${-100 + movePercent * 30}%) scale(0.85)`,
        'transition-duration': '0s',
      };
    }

    // 右侧卡片
    if (diff === 1) {
      // 向左滑动时，右侧卡片向左移动并放大，接近中心位置
      if (movePercent < 0) {
        const translateX = 0 + movePercent * 50; // 从0%向左移动
        const scale = 0.85 + Math.abs(movePercent) * 0.2; // 从0.85放大到1
        const opacity = 0.65 + Math.abs(movePercent) * 0.4; // 从0.65提高到1

        return {
          transform: `translateX(${translateX}%) scale(${scale})`,
          opacity: opacity,
          'transition-duration': '0s',
        };
      }

      // 向右滑动时，右侧卡片继续右移
      return {
        transform: `translateX(${0 + movePercent * 30}%) scale(0.85)`,
        'transition-duration': '0s',
      };
    }

    // 远左侧卡片，仅在向右滑动时略微移动
    if (diff < -1) {
      return {
        transform: `translateX(${-130 + movePercent * 30}%) scale(0.7)`,
        'transition-duration': '0s',
      };
    }

    // 远右侧卡片，仅在向左滑动时略微移动
    if (diff > 1) {
      return {
        transform: `translateX(${30 + movePercent * 30}%) scale(0.7)`,
        'transition-duration': '0s',
      };
    }

    return {};
  };

  // 自动播放功能
  const startAutoplay = () => {
    if (!props.autoplay || state.videoPlaySataus) return;

    stopAutoplay();
    state.autoplayTimer = setInterval(() => {
      nextCard();
    }, props.interval);
  };

  const stopAutoplay = () => {
    if (state.autoplayTimer) {
      clearInterval(state.autoplayTimer);
      state.autoplayTimer = null;
    }
  };

  // 监听自动播放状态变化
  watch(
    () => props.autoplay,
    (newVal) => {
      if (newVal && !state.videoPlaySataus) {
        startAutoplay();
      } else {
        stopAutoplay();
      }
    },
  );

  watch(
    () => state.videoPlaySataus,
    (newVal) => {
      if (newVal) {
        stopAutoplay();
      } else if (props.autoplay) {
        startAutoplay();
      }
    },
  );

  onMounted(() => {
    if (props.autoplay && !state.videoPlaySataus) {
      startAutoplay();
    }
  });

  // 卡片点击事件
  const onCardTap = (item, index) => {
    if (index === state.cur) {
      // 点击当前卡片
      onSwiperItem(item);
    } else {
      // 点击其他卡片，切换到该卡片
      state.cur = index;
    }
  };

  // 切换到上一张卡片
  const prevCard = () => {
    let newIndex = state.cur - 1;
    if (newIndex < 0) {
      newIndex = props.circular ? props.list.length - 1 : 0;
    }
    state.cur = newIndex;
  };

  // 切换到下一张卡片
  const nextCard = () => {
    let newIndex = state.cur + 1;
    if (newIndex >= props.list.length) {
      newIndex = props.circular ? 0 : props.list.length - 1;
    }
    state.cur = newIndex;
  };

  // 指示器点击事件
  const onIndicatorTap = (index) => {
    state.cur = index;
  };

  // current 改变时会触发 change 事件
  const swiperChange = (e) => {
    if (e.detail.source !== 'touch' && e.detail.source !== 'autoplay') return;
    state.cur = e.detail.current;
    state.videoPlaySataus = false;
    if (props.list[state.cur] && props.list[state.cur].type === 'video') {
      refs.videoRef[`video_${state.cur}`]?.pausePlay();
    }
  };

  // 点击轮播组件
  const onSwiperItem = (item) => {
    if (item.type === 'video') {
      state.videoPlaySataus = true;
    } else {
      sheep.$router.go(item.url);
      onPreview();
    }
  };

  const onPreview = () => {
    if (!props.isPreview) return;
    let previewImage = clone(props.list);
    previewImage.forEach((item, index) => {
      if (item.type === 'video') {
        previewImage.splice(index, 1);
      }
    });
    uni.previewImage({
      urls:
        previewImage.length < 1
          ? [props.src]
          : previewImage.reduce((pre, cur) => {
              pre.push(cur.src);
              return pre;
            }, []),
      current: state.cur,
    });
  };

  // swiper-item 的位置发生改变时会触发 transition
  const transition = (e) => {
    // #ifdef APP-PLUS
    state.moveX = e.detail.dx;
    // #endif
  };

  // 动画结束时会触发 animationfinish
  const animationfinish = (e) => {
    state.moveX = 0;
  };

  const videoTimeupdate = (e) => {
    if (props.list[state.cur]) {
      props.list[state.cur].currentTime = e.detail.currentTime;
    }
  };

  // 自动计算高度
  const customStyle = computed(() => {
    let height;

    // 固定高度情况
    if (props.height !== 0) {
      height = props.height;
    }

    // 自动高度情况
    if (props.height === 0) {
      // 图片预加载占位高度
      if (state.imgHeight !== 0) {
        height = state.imgHeight;
      } else if (props.seizeHeight !== 0) {
        height = props.seizeHeight;
      }
    }

    return {
      height: height + 'rpx',
    };
  });

  // 计算轮播图片最大高度
  function onImgLoad(e) {
    if (props.height === 0) {
      let newHeight = (e.detail.height / e.detail.width) * 750;
      if (state.imgHeight < newHeight) {
        state.imgHeight = newHeight;
      }
    }
  }
</script>

<style lang="scss" scoped>
  .ui-swiper {
    position: relative;

    .ui-swiper-main {
      width: 100%;
      height: 100%;
    }

    .ui-swiper-main .swiper-image {
      width: 100%;
      height: 100%;
    }

    .ui-swiper-dot {
      position: absolute;
      width: 100%;
      bottom: 20rpx;
      height: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.default .line-box {
        display: inline-flex;
        border-radius: 50rpx;
        width: 6px;
        height: 6px;
        border: 2px solid transparent;
        margin: 0 10rpx;
        opacity: 0.3;
        position: relative;
        justify-content: center;
        align-items: center;

        &.cur {
          width: 8px;
          height: 8px;
          opacity: 1;
          border: 0px solid transparent;
        }

        &.cur::after {
          content: '';
          border-radius: 50rpx;
          width: 4px;
          height: 4px;
          background-color: #fff;
        }
      }

      &.long .line-box {
        display: inline-block;
        border-radius: 100rpx;
        width: 6px;
        height: 6px;
        margin: 0 10rpx;
        opacity: 0.3;
        position: relative;

        &.cur {
          width: 24rpx;
          opacity: 1;
        }

        &.cur::after {
        }
      }

      &.line {
        bottom: 20rpx;

        .line-box {
          display: inline-block;
          width: 30px;
          height: 3px;
          opacity: 0.3;
          position: relative;

          &.cur {
            opacity: 1;
          }
        }
      }

      &.tag {
        justify-content: flex-end;
        position: absolute;
        bottom: 20rpx;
        right: 20rpx;
      }
    }

    // 卡片重叠效果容器
    .center-highlight-container {
      position: relative;
      width: 100%;
      overflow: hidden;
      padding: 0 0 15rpx 0;
      display: flex;
      justify-content: center;
    }

    // 卡片样式
    .card-item {
      position: absolute;
      top: 0;
      left: 50%;
      width: 80%;
      height: 100%;
      border-radius: 8rpx;
      overflow: hidden;
      transition: all 0.4s ease;
      z-index: 1;

      &.card-center {
        transform: translateX(-50%); // 相对于自身左边缘的50%
        z-index: 5;
        opacity: 1;
      }

      &.card-left {
        transform: translateX(-100%) scale(0.85); // 向左移动一个卡片宽度
        z-index: 4;
        opacity: 0.65;
      }

      &.card-right {
        transform: translateX(0%) scale(0.85); // 不移动，显示在右侧
        z-index: 4;
        opacity: 0.65;
      }

      &.card-far-left {
        transform: translateX(-130%) scale(0.7); // 向左移动更远
        z-index: 3;
        opacity: 0.4;
      }

      &.card-far-right {
        transform: translateX(30%) scale(0.7); // 向右移动更远
        z-index: 3;
        opacity: 0.4;
      }
    }

    .card-image {
      width: 100%;
      height: 100%;
    }

    // 指示器
    .card-indicator {
      position: absolute;
      bottom: 20rpx;
      left: 0;
      right: 0;
      display: flex;
      justify-content: center;
      z-index: 10;

      .card-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;
        background-color: rgba(255, 255, 255, 0.4);
        margin: 0 8rpx;
        transition: all 0.3s ease;

        &.active {
          width: 24rpx;
          border-radius: 12rpx;
          background-color: #ffffff;
        }
      }
    }

    &.card {
      .swiper-item {
        width: 610rpx !important;
        left: 70rpx;
        box-sizing: border-box;
        padding: 20rpx 0rpx 60rpx;
        overflow: initial;
      }

      .swiper-item .ui-swiper-main {
        width: 100%;
        display: block;
        height: 100%;
        transform: scale(0.9);
        transition: all 0.2s ease-in 0s;
        position: relative;
        background-size: cover;

        .swiper-image {
          height: 100%;
        }
      }

      .swiper-item .ui-swiper-main::before {
        content: '';
        display: block;
        background: inherit;
        filter: blur(5px);
        position: absolute;
        width: 100%;
        height: 100%;
        top: 10rpx;
        left: 10rpx;
        z-index: -1;
        opacity: 0.3;
        transform-origin: 0 0;
        transform: scale(1, 1);
      }

      .swiper-item.cur .ui-swiper-main {
        transform: scale(1);
        transition: all 0.2s ease-in 0s;
      }

      .ui-swiper-dot.tag {
        position: absolute;
        bottom: 85rpx;
        right: 75rpx;
      }
    }

    &.hotelCard {
      .swiper-item {
        width: 650rpx !important;
        left: 30rpx;
        box-sizing: border-box;
        padding: 0rpx 0rpx 50rpx;
        overflow: initial;
      }

      .swiper-item .ui-swiper-main {
        width: 100%;
        display: block;
        height: 100%;
        transform: scale(0.9);
        opacity: 0.8;
        transition: all 0.2s ease-in 0s;
        position: relative;
        background-size: cover;

        .swiper-image {
          width: 100%;
          height: 400rpx;
        }
      }

      .swiper-item .ui-swiper-main::before {
        content: '';
        display: block;
        background: inherit;
        filter: blur(5px);
        position: absolute;
        width: 100%;
        height: 100%;
        top: 10rpx;
        left: 10rpx;
        z-index: -1;
        opacity: 0.3;
        transform-origin: 0 0;
        transform: scale(1, 1);
      }

      .swiper-item.cur .ui-swiper-main {
        transform: scale(1);
        transition: all 0.2s ease-in 0s;
        opacity: 1;
      }

      .ui-swiper-dot {
        display: none;
      }
    }

    &.hotelDetail {
      .swiper-item {
        width: 690rpx !important;
        left: 30rpx;
        box-sizing: border-box;
        padding: 20rpx 0rpx;
        overflow: initial;
      }

      .swiper-item .ui-swiper-main {
        width: 100%;
        display: block;
        height: 100%;
        transform: scale(0.96);
        transition: all 0.2s ease-in 0s;
        position: relative;
        background-size: cover;

        .swiper-image {
          height: 100%;
        }
      }

      .swiper-item.cur .ui-swiper-main {
        transform: scale(0.96);
        transition: all 0.2s ease-in 0s;
      }
    }
  }
</style>
