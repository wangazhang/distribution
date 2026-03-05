import $store from '@/sheep/store';
import $platform from '@/sheep/platform';
import { showAuthModal, showShareModal } from '@/sheep/hooks/useModal';
import { isNumber, isString, isEmpty, startsWith, isObject, isNil, clone } from 'lodash-es';
import throttle from '@/sheep/helper/throttle';

// 导航历史记录管理（用于处理特殊业务流程的返回问题）
const navigationHistory = {
  // 记录redirect前的页面信息
  beforeRedirect: null,

  // 记录导航历史
  record(info) {
    this.beforeRedirect = info;
    // 设置过期时间，避免内存泄漏
    setTimeout(() => {
      this.beforeRedirect = null;
    }, 30000); // 30秒后清除
  },

  // 获取并清除记录
  getAndClear() {
    const info = this.beforeRedirect;
    this.beforeRedirect = null;
    return info;
  },
};

let silentLoginPromise = null;

// 尝试在微信小程序内静默登录，失败才回退到弹窗
async function ensureWechatSilentLogin() {
  // 仅在微信小程序且未登录时尝试
  const userStore = $store('user');
  if ($platform.name !== 'WechatMiniProgram' || userStore.isLogin) {
    return userStore.isLogin;
  }
  if (silentLoginPromise) {
    return silentLoginPromise;
  }
  silentLoginPromise = (async () => {
    try {
      const ok = await $platform.useProvider('wechat').login();
      if (ok) {
        await userStore.updateUserData();
        return true;
      }
    } catch (error) {
      console.warn('静默登录失败:', error);
    } finally {
      silentLoginPromise = null;
    }
    return false;
  })();
  return silentLoginPromise;
}

const _go = async (
  path,
  params = {},
  options = {
    redirect: false,
  },
) => {
  let page = ''; // 跳转页面
  let query = ''; // 页面参数
  let url = ''; // 跳转页面完整路径

  if (isString(path)) {
    // 判断跳转类型是 path ｜ 还是http
    if (startsWith(path, 'http')) {
      // #ifdef H5
      window.location = path;
      return;
      // #endif
      // #ifndef H5
      page = `/pages/public/webview`;
      query = `url=${encodeURIComponent(path)}`;
      // #endif
    } else if (startsWith(path, 'action:')) {
      handleAction(path);
      return;
    } else {
      [page, query] = path.split('?');
    }
    if (!isEmpty(params)) {
      let query2 = paramsToQuery(params);
      if (isEmpty(query)) {
        query = query2;
      } else {
        query += '&' + query2;
      }
    }
  }

  if (isObject(path)) {
    page = path.url;
    if (!isNil(path.params)) {
      query = paramsToQuery(path.params);
    }
  }

  if (page && !startsWith(page, '/')) {
    page = `/${page}`;
  }

  const nextRoute = ROUTES_MAP[page];

  // 未找到指定跳转页面
  // mark: 跳转404页
  if (!nextRoute) {
    console.log(`%c跳转路径参数错误<${page || 'EMPTY'}>`, 'color:red;background:yellow');
    return;
  }

  // 页面登录拦截
  if (nextRoute.meta?.auth && !$store('user').isLogin) {
    const silentOk = await ensureWechatSilentLogin();
    if (!$store('user').isLogin && !silentOk) {
      showAuthModal();
      return;
    }
  }

  url = page;
  if (!isEmpty(query)) {
    url += `?${query}`;
  }

  // 跳转底部导航
  if (TABBAR.includes(page)) {
    uni.switchTab({
      url,
    });
    return;
  }

  // 使用redirect跳转
  if (options.redirect) {
    // 记录redirect前的导航历史，用于智能返回
    recordNavigationHistory();
    uni.reLaunch({
      url,
    });
    return;
  }

  // 页面栈预处理：解决右滑手势返回问题
  const currentPages = getCurrentPages();
  const minePagePath = '/yehu/pages/mine/index';

  // 如果当前只有一个页面，且目标页面不是"我的页面"，且不是tabbar页面
  // 则先插入"我的页面"，确保用户右滑返回时能经过"我的页面"
  if (
    currentPages.length === 1 &&
    page !== minePagePath &&
    !TABBAR.includes(page) &&
    !options.skipMinePageInsertion
  ) {
    console.log('页面栈预处理：插入"我的页面"以优化返回体验');
    uni.navigateTo({
      url: minePagePath,
      success: () => {
        // 延迟一下再跳转到目标页面，确保页面栈正确
        setTimeout(() => {
          uni.navigateTo({ url });
        }, 100);
      },
      fail: () => {
        // 如果插入"我的页面"失败，直接跳转到目标页面
        uni.navigateTo({ url });
      },
    });
    return;
  }

  uni.navigateTo({
    url,
  });
};

// 记录导航历史（在redirect前调用）
function recordNavigationHistory() {
  try {
    const currentPages = getCurrentPages();
    if (currentPages.length > 0) {
      const currentPage = currentPages[currentPages.length - 1];
      navigationHistory.record({
        route: currentPage.route,
        options: currentPage.options,
        timestamp: Date.now(),
      });
      console.log('记录导航历史:', currentPage.route);
    }
  } catch (error) {
    console.warn('记录导航历史失败:', error);
  }
}

// 限流 防止重复点击跳转
function go(...args) {
  throttle(() => {
    _go(...args);
  });
}

function paramsToQuery(params) {
  if (isEmpty(params)) {
    return '';
  }
  // return new URLSearchParams(Object.entries(params)).toString();
  let query = [];
  for (let key in params) {
    query.push(key + '=' + params[key]);
  }

  return query.join('&');
}

function back() {
  // #ifdef H5
  history.back();
  // #endif

  // #ifndef H5
  uni.navigateBack();
  // #endif
}

// 智能返回函数：如果有上一页则正常返回，如果没有上一页则统一返回到"我的页面"
function smartBack() {
  const hasHistoryRecord = hasHistory();
  
  if (hasHistoryRecord) {
    // 有上一页，正常返回（包括上一页是"我的页面"的情况，也不需要变化）
    back();
  } else {
    // 没有上一页，统一返回到"我的页面"
    go('/yehu/pages/mine/index');
  }
}

function redirect(path, params = {}) {
  go(path, params, {
    redirect: true,
  });
}

// 智能redirect：在redirect前记录导航历史，并可选择是否插入"我的页面"
function smartRedirect(path, params = {}, options = {}) {
  go(path, params, {
    redirect: true,
    skipMinePageInsertion: options.skipMinePageInsertion || false,
  });
}

// 刷新当前页面，用于登录后快速重载数据
function refreshCurrentPage() {
  try {
    const currentPages = getCurrentPages();
    if (!currentPages.length) {
      return;
    }
    const currentPage = currentPages[currentPages.length - 1];
    let route = currentPage.route || '';
    if (!route.startsWith('/')) {
      route = `/${route}`;
    }
    const query = paramsToQuery(currentPage.options || {});
    const url = query ? `${route}?${query}` : route;
    if (TABBAR.includes(route)) {
      uni.switchTab({
        url: route,
      });
      return;
    }
    uni.redirectTo({
      url,
    });
  } catch (error) {
    console.warn('刷新页面失败:', error);
  }
}

// 检测是否有浏览器历史
function hasHistory() {
  // #ifndef H5
  const pages = getCurrentPages();
  if (pages.length > 1) {
    return true;
  }
  return false;
  // #endif

  // #ifdef H5
  return !!history.state.back;
  // #endif
}

function getCurrentRoute(field = '') {
  let currentPage = getCurrentPage();
  // #ifdef MP
  currentPage.$page['route'] = currentPage.route;
  currentPage.$page['options'] = currentPage.options;
  // #endif
  if (field !== '') {
    return currentPage.$page[field];
  } else {
    return currentPage.$page;
  }
}

function getCurrentPage() {
  let pages = getCurrentPages();
  return pages[pages.length - 1];
}

function handleAction(path) {
  const action = path.split(':');
  switch (action[1]) {
    case 'showShareModal':
      showShareModal();
      break;
  }
}

function error(errCode, errMsg = '') {
  redirect('/pages/public/error', {
    errCode,
    errMsg,
  });
}

export default {
  go,
  back,
  smartBack,
  hasHistory,
  redirect,
  smartRedirect,
  refreshCurrentPage,
  getCurrentPage,
  getCurrentRoute,
  error,
};
