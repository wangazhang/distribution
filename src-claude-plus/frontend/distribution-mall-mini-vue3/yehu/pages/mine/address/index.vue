<template>
  <view class="address-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="收货地址"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 地址列表 -->
    <view class="address-list" v-if="addressList.length > 0">
      <view class="address-item" v-for="(item, index) in addressList" :key="index">
        <view class="address-info" @tap="selectAddress(item)">
          <view class="user-info">
            <text class="name">{{ item.name }}</text>
            <text class="phone">{{ item.phone }}</text>
            <view class="default-tag" v-if="item.isDefault">默认</view>
          </view>
          <view class="address-detail">
            {{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}
          </view>
        </view>
        <view class="address-action">
          <view class="action-item" @tap="setDefault(index)" v-if="!item.isDefault">
            <u-icon name="checkmark-circle" size="44" color="#FFA0B4"></u-icon>
            <text>设为默认</text>
          </view>
          <view class="action-item" @tap="editAddress(item)">
            <u-icon name="edit-pen" size="44" color="#999"></u-icon>
            <text>编辑</text>
          </view>
          <view class="action-item" @tap="deleteAddress(index)">
            <u-icon name="trash" size="44" color="#999"></u-icon>
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view class="empty-address" v-else>
      <u-empty text="暂无收货地址" mode="address"></u-empty>
    </view>
    
    <!-- 新增地址按钮 -->
    <view class="add-address-btn" @tap="addAddress">
      <u-icon name="plus" size="40" color="#fff"></u-icon>
      <text>新增收货地址</text>
    </view>
  </view>
</template>

<script setup >
import { ref, onMounted } from 'vue';

// 地址列表
const addressList = ref([
  {
    id: '1',
    name: '张三',
    phone: '138****1234',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detail: '张江高科技园区博云路2号',
    isDefault: true
  },
  {
    id: '2',
    name: '李四',
    phone: '159****5678',
    province: '北京市',
    city: '北京市',
    district: '海淀区',
    detail: '中关村科技园区海淀大街甲28号',
    isDefault: false
  }
]);

// 源页面，用于判断是否需要选择地址后返回
const fromPage = ref('');

// 接收页面参数
const onLoad = (options) => {
  console.log('地址页面参数:', options);
  if (options.from) {
    fromPage.value = options.from;
  }
};

// 设置默认地址
const setDefault = (index) => {
  // 先将所有地址设置为非默认
  addressList.value.forEach(item => {
    item.isDefault = false;
  });
  
  // 将当前选中的地址设置为默认
  addressList.value[index].isDefault = true;
  
  uni.showToast({
    title: '设置成功',
    icon: 'success'
  });
};

// 编辑地址
const editAddress = (address) => {
  uni.navigateTo({
    url: `/pages/mine/address/edit?id=${address.id}`
  });
};

// 删除地址
const deleteAddress = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该地址吗？',
    success: (res) => {
      if (res.confirm) {
        addressList.value.splice(index, 1);
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        });
      }
    }
  });
};

// 添加地址
const addAddress = () => {
  uni.navigateTo({
    url: '/pages/mine/address/add'
  });
};

// 选择地址
const selectAddress = (address) => {
  // 如果是从其他页面跳转来选择地址的，则选择后返回
  if (fromPage.value === 'checkout') {
    // 使用事件通信或缓存选中的地址
    uni.setStorageSync('selectedAddress', address);
    uni.navigateBack();
  }
};

onMounted(() => {
  // 页面加载时可以从缓存或API获取地址列表
});
</script>

<style lang="scss" scoped>
.address-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.address-list {
  padding: 20rpx;
}

.address-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  
  .address-info {
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .user-info {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;
      
      .name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333333;
        margin-right: 20rpx;
      }
      
      .phone {
        font-size: 28rpx;
        color: #666666;
        margin-right: 20rpx;
      }
      
      .default-tag {
        background-color: #FFA0B4;
        color: #ffffff;
        font-size: 20rpx;
        padding: 4rpx 10rpx;
        border-radius: 6rpx;
      }
    }
    
    .address-detail {
      font-size: 28rpx;
      color: #666666;
      line-height: 1.5;
    }
  }
  
  .address-action {
    display: flex;
    height: 100rpx;
    align-items: center;
    padding: 0 30rpx;
    
    .action-item {
      display: flex;
      align-items: center;
      margin-right: 40rpx;
      
      text {
        font-size: 26rpx;
        color: #666666;
        margin-left: 8rpx;
      }
    }
  }
}

.empty-address {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.add-address-btn {
  position: fixed;
  left: 20rpx;
  right: 20rpx;
  bottom: 20rpx;
  height: 88rpx;
  background-color: #FFA0B4;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  box-shadow: 0 4rpx 8rpx rgba(255, 160, 180, 0.2);
  
  text {
    font-size: 30rpx;
    margin-left: 10rpx;
  }
}
</style> 