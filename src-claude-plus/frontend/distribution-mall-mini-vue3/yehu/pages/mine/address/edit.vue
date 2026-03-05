<template>
  <view class="address-form-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="编辑地址"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 地址表单 -->
    <view class="address-form">
      <view class="form-group">
        <view class="form-item">
          <view class="item-label">收货人</view>
          <view class="item-input">
            <u-input
              v-model="addressForm.name"
              placeholder="请输入收货人姓名"
              border="none"
              :clearable="true"
            ></u-input>
          </view>
        </view>
        
        <view class="form-item">
          <view class="item-label">手机号码</view>
          <view class="item-input">
            <u-input
              v-model="addressForm.phone"
              placeholder="请输入手机号码"
              border="none"
              type="number"
              :clearable="true"
            ></u-input>
          </view>
        </view>
        
        <view class="form-item">
          <view class="item-label">所在地区</view>
          <view class="item-input" @tap="showRegionPicker">
            <text v-if="addressForm.province" class="region-text">{{ addressForm.province }} {{ addressForm.city }} {{ addressForm.district }}</text>
            <text v-else class="placeholder-text">请选择所在地区</text>
            <u-icon name="arrow-right" color="#ccc" size="36"></u-icon>
          </view>
        </view>
        
        <view class="form-item">
          <view class="item-label">详细地址</view>
          <view class="item-input">
            <u-input
              v-model="addressForm.detail"
              placeholder="请输入详细地址"
              border="none"
              :clearable="true"
              type="textarea"
              :auto-height="true"
            ></u-input>
          </view>
        </view>
      </view>
      
      <view class="form-group">
        <view class="form-item">
          <view class="item-label">设为默认地址</view>
          <view class="item-switch">
            <u-switch v-model="addressForm.isDefault" active-color="#FFA0B4"></u-switch>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 提交按钮 -->
    <view class="submit-btn" @tap="saveAddress">保存地址</view>
    
    <!-- 地区选择器 -->
    <u-picker
      v-model="showPicker"
      mode="region"
      @confirm="confirmRegion"
    ></u-picker>
  </view>
</template>

<script setup >
import { ref, reactive, onMounted } from 'vue';

// 地址ID
const addressId = ref('');

// 地址表单数据
const addressForm = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
});

// 地区选择器相关
const showPicker = ref(false);

// 接收页面参数
const onLoad = (options) => {
  console.log('编辑地址页面参数:', options);
  if (options.id) {
    addressId.value = options.id;
    loadAddressData(options.id);
  }
};

// 加载地址数据
const loadAddressData = (id) => {
  try {
    const addressList = uni.getStorageSync('addressList') || [];
    const address = addressList.find((item) => item.id === id);
    
    if (address) {
      addressForm.name = address.name;
      addressForm.phone = address.phone;
      addressForm.province = address.province;
      addressForm.city = address.city;
      addressForm.district = address.district;
      addressForm.detail = address.detail;
      addressForm.isDefault = address.isDefault;
    }
  } catch (e) {
    console.error('获取地址数据失败', e);
  }
};

// 显示地区选择器
const showRegionPicker = () => {
  showPicker.value = true;
};

// 确认地区选择
const confirmRegion = (e) => {
  addressForm.province = e.province.label;
  addressForm.city = e.city.label;
  addressForm.district = e.area.label;
};

// 保存地址
const saveAddress = () => {
  // 表单验证
  if (!addressForm.name) {
    uni.showToast({
      title: '请输入收货人姓名',
      icon: 'none'
    });
    return;
  }
  
  if (!addressForm.phone) {
    uni.showToast({
      title: '请输入手机号码',
      icon: 'none'
    });
    return;
  }
  
  if (!(/^1[3-9]\d{9}$/.test(addressForm.phone))) {
    uni.showToast({
      title: '手机号码格式不正确',
      icon: 'none'
    });
    return;
  }
  
  if (!addressForm.province || !addressForm.city || !addressForm.district) {
    uni.showToast({
      title: '请选择所在地区',
      icon: 'none'
    });
    return;
  }
  
  if (!addressForm.detail) {
    uni.showToast({
      title: '请输入详细地址',
      icon: 'none'
    });
    return;
  }
  
  // 保存地址
  uni.showLoading({ title: '保存中' });
  
  // 模拟保存地址
  setTimeout(() => {
    uni.hideLoading();
    
    try {
      const addressList = uni.getStorageSync('addressList') || [];
      const index = addressList.findIndex((item) => item.id === addressId.value);
      
      if (index !== -1) {
        // 更新地址
        const updatedAddress = {
          id: addressId.value,
          name: addressForm.name,
          phone: addressForm.phone,
          province: addressForm.province,
          city: addressForm.city,
          district: addressForm.district,
          detail: addressForm.detail,
          isDefault: addressForm.isDefault
        };
        
        // 如果设置为默认地址，需要将其他地址设为非默认
        if (updatedAddress.isDefault) {
          addressList.forEach((item) => {
            if (item.id !== addressId.value) {
              item.isDefault = false;
            }
          });
        }
        
        addressList[index] = updatedAddress;
        uni.setStorageSync('addressList', addressList);
        
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        });
        
        // 返回上一页
        setTimeout(() => {
          uni.navigateBack();
        }, 1000);
      }
    } catch (e) {
      console.error('保存地址失败', e);
      uni.showToast({
        title: '保存失败',
        icon: 'none'
      });
    }
  }, 1000);
};

onMounted(() => {
  // 页面初始化逻辑
});
</script>

<style lang="scss" scoped>
.address-form-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.address-form {
  padding: 20rpx;
}

.form-group {
  background-color: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .item-label {
    width: 180rpx;
    font-size: 28rpx;
    color: #333333;
  }
  
  .item-input {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .region-text {
      font-size: 28rpx;
      color: #333333;
    }
    
    .placeholder-text {
      font-size: 28rpx;
      color: #999999;
    }
  }
  
  .item-switch {
    display: flex;
    justify-content: flex-end;
  }
}

.submit-btn {
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
  font-size: 32rpx;
  font-weight: 500;
}
</style> 