<!-- 分销商品列表  -->
<template>
  <s-layout title="推广商品" :onShareAppMessage="state.shareInfo">
    <view class="goods-item ss-m-20" v-for="item in state.pagination.list" :key="item.id">
      <s-goods-item
        size="lg"
        :img="item.picUrl"
        :title="item.name"
        :subTitle="item.introduction"
        :price="item.price"
        :originPrice="item.marketPrice"
        priceColor="#333"
        @tap="sheep.$router.go('/pages/goods/index', { id: item.id })"
      >
        <template #rightBottom>
          <view class="ss-flex ss-row-between">
            <view class="stock-info" v-if="item.stock === undefined">
              <!-- 加载中或获取失败时不显示内容 -->
            </view>
            <view class="stock-info" v-else-if="item.stock !== null">
              我的库存: {{ item.stock }} {{ item.unit || '件' }}
            </view>
            <view class="stock-info" v-else>
              <!-- 获取失败时不显示内容 -->
            </view>
            <button class="share-btn" @tap.stop="onShareGoods(item)"> 分享 </button>
          </view>
        </template>
      </s-goods-item>
    </view>
    <s-empty
      v-if="state.pagination.total === 0"
      icon="/static/goods-empty.png"
      text="暂无推广商品"
    />
    <!-- 加载更多 -->
    <uni-load-more
      v-if="state.pagination.total > 0"
      :status="state.loadStatus"
      :content-text="{
        contentdown: '上拉加载更多',
      }"
      @tap="loadMore"
    />
  </s-layout>
</template>

<script setup>
  import sheep from '@/sheep';
  import $share from '@/sheep/platform/share';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { reactive, ref } from 'vue';
  import _ from 'lodash-es';
  import { showShareModal } from '@/sheep/hooks/useModal';
  import SpuApi from '@/sheep/api/product/spu';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import { getMaterialList } from '@/yehu/api/mine';

  const state = reactive({
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 8,
    },
    loadStatus: '',
    shareInfo: {},
  });

  const materialStockMap = ref(new Map());
  let materialStockLoaded = false;
  let materialStockLoadingPromise = null;
  const MATERIAL_PAGE_SIZE = 100;

  function resetMaterialStockCache() {
    materialStockLoaded = false;
    materialStockMap.value = new Map();
    materialStockLoadingPromise = null;
  }

  async function loadMaterialStock() {
    const map = new Map();
    let pageNo = 1;
    const pageSize = MATERIAL_PAGE_SIZE;

    while (true) {
      const res = await getMaterialList({
        pageNo,
        pageSize,
      });

      if (!res || res.code !== 0 || !res.data) {
        const errorMessage = res?.msg || '加载物料库存失败';
        throw new Error(errorMessage);
      }

      const list = Array.isArray(res.data.list) ? res.data.list : [];
      list.forEach((item) => {
        const stockRaw =
          item.balance ?? item.stock ?? item.availableBalance ?? 0;
        const stockValue = Number(stockRaw);
        const normalizedStock = Number.isNaN(stockValue) ? 0 : stockValue;
        const unitValue = item.unit || '件';
        const materialIdValue = item.materialId ?? item.id ?? null;
        const payload = {
          materialId: materialIdValue,
          stock: normalizedStock,
          unit: unitValue,
        };
        const candidateKeys = [
          item.shareId,
          item.refillId,
          materialIdValue,
          item.id,
          item.convertTargetSpuId,
          item.convertedSpuId,
        ];

        candidateKeys.forEach((candidate) => {
          if (candidate === undefined || candidate === null) {
            return;
          }
          const key = String(candidate);
          map.set(key, payload);
        });
      });

      const total = Number(res.data.total ?? 0);
      if (!total || pageNo * pageSize >= total) {
        break;
      }
      pageNo += 1;
    }

    materialStockMap.value = map;
  }

  async function ensureMaterialStockLoaded(forceRefresh = false) {
    if (forceRefresh) {
      resetMaterialStockCache();
    }
    if (materialStockLoaded) {
      return;
    }
    if (!materialStockLoadingPromise) {
      materialStockLoadingPromise = loadMaterialStock()
        .then(() => {
          materialStockLoaded = true;
        })
        .catch((error) => {
          materialStockLoaded = false;
          materialStockMap.value = new Map();
          throw error;
        })
        .finally(() => {
          materialStockLoadingPromise = null;
        });
    }
    return materialStockLoadingPromise;
  }

  function onShareGoods(goodsInfo) {
    state.shareInfo = $share.getShareInfo(
      {
        title: goodsInfo.name,
        image: sheep.$url.cdn(goodsInfo.picUrl),
        desc: goodsInfo.introduction,
        params: {
          page: '2',
          query: goodsInfo.id,
        },
      },
      {
        type: 'goods', // 商品海报
        title: goodsInfo.name, // 商品名称
        image: sheep.$url.cdn(goodsInfo.picUrl), // 商品主图
        price: fen2yuan(goodsInfo.price), // 商品价格
        original_price: fen2yuan(goodsInfo.marketPrice), // 商品原价
      },
    );
    showShareModal();
  }

  // 批量查询库存：基于“我的物料”页的库存快照进行映射
  async function batchQueryStock(goodsList) {
    if (!goodsList || goodsList.length === 0) return;

    try {
      await ensureMaterialStockLoaded(state.pagination.pageNo === 1);

      const stockMap = materialStockMap.value;
      goodsList.forEach((goodsItem) => {
        const key =
          goodsItem && goodsItem.id !== undefined && goodsItem.id !== null
            ? String(goodsItem.id)
            : null;
        const stockInfo = key ? stockMap.get(key) : undefined;
        if (stockInfo) {
          goodsItem.stock = stockInfo.stock;
          goodsItem.unit = stockInfo.unit || '件';
          goodsItem.materialId = stockInfo.materialId;
        } else {
          goodsItem.stock = 0;
          goodsItem.unit = goodsItem.unit || '件';
          goodsItem.materialId = null;
        }
      });
    } catch (error) {
      console.error('获取物料库存信息失败', error);
      goodsList.forEach((goodsItem) => {
        goodsItem.stock = null;
        goodsItem.unit = '件';
        goodsItem.materialId = null;
      });
    }
  }

  async function getGoodsList() {
    state.loadStatus = 'loading';
    let { code, data } = await SpuApi.getSpuPage({
      pageSize: state.pagination.pageSize,
      pageNo: state.pagination.pageNo,
    });

    if (code !== 0) {
      state.loadStatus = 'error'; // 处理错误状态
      return;
    }

    // 将新获取的商品追加到列表中
    const newList = data.list;
    state.pagination.list = _.concat(state.pagination.list, newList);
    state.pagination.total = data.total;

    // 批量查询库存信息
    await batchQueryStock(newList);

    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  onLoad(() => {
    resetMaterialStockCache();
    state.pagination.list = [];
    state.pagination.total = 0;
    state.pagination.pageNo = 1;
    state.loadStatus = '';
    state.shareInfo = {};
    getGoodsList();
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getGoodsList();
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });
</script>

<style lang="scss" scoped>
  .goods-item {
    .stock-info {
      font-size: 24rpx;
      font-weight: 500;
      color: #8f1911;
      flex: 1;
    }

    .share-btn {
      width: 120rpx;
      height: 50rpx;
      border-radius: 25rpx;
      background-color: transparent;
      color: #8f1911;
      border: 1rpx solid #8f1911;
      font-size: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
      margin-left: 20rpx;

      &:active {
        background-color: rgba(143, 25, 17, 0.1);
      }
    }
  }
</style>
