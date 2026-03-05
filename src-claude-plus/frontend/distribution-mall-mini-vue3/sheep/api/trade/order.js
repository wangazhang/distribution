import request from '@/sheep/request';
import { isEmpty } from '@/sheep/helper/utils';

const OrderApi = {
  settlementOrder: (data) => {
    const data2 = {
      ...data,
    };
    if (!(data.couponId > 0)) {
      delete data2.couponId;
    }
    if (!(data.addressId > 0)) {
      delete data2.addressId;
    }
    if (!(data.pickUpStoreId > 0)) {
      delete data2.pickUpStoreId;
    }
    if (isEmpty(data.receiverName)) {
      delete data2.receiverName;
    }
    if (isEmpty(data.receiverMobile)) {
      delete data2.receiverMobile;
    }
    if (!(data.combinationActivityId > 0)) {
      delete data2.combinationActivityId;
    }
    if (!(data.combinationHeadId > 0)) {
      delete data2.combinationHeadId;
    }
    if (!(data.seckillActivityId > 0)) {
      delete data2.seckillActivityId;
    }
    if (!(data.pointActivityId > 0)) {
      delete data2.pointActivityId;
    }
    if (!(data.deliveryType > 0)) {
      delete data2.deliveryType;
    }
    delete data2.items;
    for (let i = 0; i < data.items.length; i++) {
      data2[encodeURIComponent('items[' + i + '' + '].skuId')] = data.items[i].skuId + '';
      data2[encodeURIComponent('items[' + i + '' + '].count')] = data.items[i].count + '';
      if (data.items[i].cartId) {
        data2[encodeURIComponent('items[' + i + '' + '].cartId')] = data.items[i].cartId + '';
      }
    }
    const queryString = Object.keys(data2)
      .map((key) => key + '=' + data2[key])
      .join('&');
    return request({
      url: `/trade/order/settlement?${queryString}`,
      method: 'GET',
      custom: {
        showError: true,
        showLoading: true,
      },
    });
  },
  getSettlementProduct: (spuIds) => {
    return request({
      url: '/trade/order/settlement-product',
      method: 'GET',
      params: { spuIds },
      custom: {
        showLoading: false,
        showError: false,
      },
    });
  },
  createOrder: (data) => {
    return request({
      url: `/trade/order/create`,
      method: 'POST',
      data,
    });
  },
  getOrderDetail: (id, sync) => {
    return request({
      url: `/trade/order/get-detail`,
      method: 'GET',
      params: {
        id,
        sync,
      },
      custom: {
        showLoading: false,
      },
    });
  },
  getOrderPage: (params) => {
    return request({
      url: '/trade/order/page',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
      },
    });
  },
  receiveOrder: (id) => {
    return request({
      url: `/trade/order/receive`,
      method: 'PUT',
      params: {
        id,
      },
    });
  },
  checkChannelConfirm: (data) => {
    return request({
      url: '/trade/order/check-channel-confirm',
      method: 'POST',
      data,
    });
  },
  cancelOrder: (id) => {
    return request({
      url: `/trade/order/cancel`,
      method: 'DELETE',
      params: {
        id,
      },
    });
  },
  deleteOrder: (id) => {
    return request({
      url: `/trade/order/delete`,
      method: 'DELETE',
      params: {
        id,
      },
    });
  },
  getOrderExpressTrackList: (id) => {
    return request({
      url: `/trade/order/get-express-track-list`,
      method: 'GET',
      params: {
        id,
      },
    });
  },
  getOrderCount: () => {
    return request({
      url: '/trade/order/get-count',
      method: 'GET',
      custom: {
        showLoading: false,
        auth: true,
      },
    });
  },
  createOrderItemComment: (data) => {
    return request({
      url: `/trade/order/item/create-comment`,
      method: 'POST',
      data,
    });
  },
};

export default OrderApi;
