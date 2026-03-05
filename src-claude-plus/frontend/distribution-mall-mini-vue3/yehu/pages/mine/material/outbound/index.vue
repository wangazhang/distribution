<template>
  <s-layout title="物料出库申请">
    <view class="outbound-container">
      <!-- 使用su-sticky固定步骤条 -->
      <su-sticky bgColor="#fff">
        <view class="step-container">
          <uni-steps :options="stepOptions" :active="currentStep" :activeColor="'#8f1911'" />
        </view>
      </su-sticky>

      <!-- 步骤1: 选择物料 -->
      <view v-if="currentStep === 0" class="step-content">
        <view class="section-title">
          <text>选择需要出库的物料</text>
        </view>

        <!-- 物料选择列表 -->
        <view v-if="loading" class="loading-container">
          <uni-load-more status="loading"></uni-load-more>
        </view>
        <view v-else-if="materialList.length === 0" class="empty-container">
          <uni-icons type="info" size="30" color="#999"></uni-icons>
          <text class="empty-text">暂无可出库物料</text>
        </view>
        <view v-else class="material-list">
          <view 
            v-for="item in materialList" 
            :key="item.id" 
            class="material-item"
            :class="{ 'selected': selectedMaterials.some(m => m.id === item.id) }"
            @tap="toggleMaterialSelection(item)"
          >
            <view class="material-info">
              <image class="material-image" :src="item.image" mode="aspectFill"></image>
              <view class="material-details">
                <text class="material-name">{{ item.name }}</text>
                <text class="material-stock">库存: {{ item.stock }} {{ item.unit }}</text>
              </view>
            </view>
            <view class="material-select">
              <uni-icons 
                :type="selectedMaterials.some(m => m.id === item.id) ? 'checkbox-filled' : 'circle'" 
                size="24" 
                :color="selectedMaterials.some(m => m.id === item.id) ? '#8f1911' : '#ddd'"
              ></uni-icons>
            </view>
          </view>
        </view>

        <!-- 底部操作按钮 -->
        <view class="bottom-actions">
          <view 
            class="action-btn next-btn" 
            :class="{ 'disabled': selectedMaterials.length === 0 }"
            @tap="goToNextStep"
          >
            下一步
          </view>
        </view>
      </view>

      <!-- 步骤2: 设置数量 -->
      <view v-else-if="currentStep === 1" class="step-content">
        <view class="section-title">
          <text>设置出库数量</text>
        </view>

        <view class="quantity-list">
          <view 
            v-for="(item, index) in selectedMaterials" 
            :key="item.id" 
            class="quantity-item"
          >
            <view class="material-info">
              <image class="material-image" :src="item.image" mode="aspectFill"></image>
              <view class="material-details">
                <text class="material-name">{{ item.name }}</text>
                <text class="material-stock">库存: {{ item.stock }} {{ item.unit }}</text>
              </view>
            </view>
            
            <view class="quantity-setting">
              <view class="quantity-label">数量设置</view>
              <view class="quantity-control">
                <view class="quantity-btn minus-btn" @tap="decreaseQuantity(index)">
                  <text>-</text>
                </view>
                <input 
                  class="quantity-input" 
                  type="number" 
                  v-model="item.quantity" 
                  @input="validateQuantity(index)"
                />
                <view class="quantity-btn plus-btn" @tap="increaseQuantity(index)">
                  <text>+</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 底部操作按钮 -->
        <view class="bottom-actions">
          <view class="action-btn back-btn" @tap="goToPreviousStep">
            上一步
          </view>
          <view 
            class="action-btn next-btn" 
            :class="{ 'disabled': !canProceedToAddress }"
            @tap="goToNextStep"
          >
            下一步
          </view>
        </view>
      </view>

      <!-- 步骤3: 选择收货地址 -->
      <view v-else-if="currentStep === 2" class="step-content">
        <view class="section-title">
          <text>收货信息</text>
          <!-- 从地址库选择按钮 -->
          <view class="action-button select-address-btn" @tap="navigateToAddressList">
            <uni-icons type="location-filled" size="20" color="#8f1911"></uni-icons>
            <text>从地址库选择</text>
          </view>
        </view>

        <!-- 地址信息卡片 -->
        <view class="address-card">
          <!-- 收货人信息 -->
          <view class="form-group">
            <view class="form-item">
              <view class="form-label">
                <text class="required">*</text>
                <text>收货人</text>
              </view>
              <input 
                class="form-input" 
                type="text" 
                v-model="addressForm.name" 
                placeholder="请输入收货人姓名"
              />
            </view>
            
            <view class="form-item">
              <view class="form-label">
                <text class="required">*</text>
                <text>手机号码</text>
              </view>
              <input 
                class="form-input" 
                type="number" 
                v-model="addressForm.mobile" 
                placeholder="请输入手机号码"
                maxlength="11"
              />
            </view>
          </view>
          
          <view class="form-divider"></view>
          
          <!-- 地址信息 -->
          <view class="form-group">
            <view class="form-item" @tap="showAddressSelector">
              <view class="form-label">
                <text class="required">*</text>
                <text>所在地区</text>
              </view>
              <view class="form-input area-input">
                <text :class="{'placeholder': !addressForm.areaName}">
                  {{ addressForm.areaName || '请选择所在地区' }}
                </text>
                <uni-icons type="forward" size="16" color="#999"></uni-icons>
              </view>
            </view>
            
            <view class="form-item">
              <view class="form-label">
                <text class="required">*</text>
                <text>详细地址</text>
              </view>
              <textarea 
                class="form-textarea" 
                v-model="addressForm.detailAddress" 
                placeholder="请输入详细地址信息，如街道、门牌号等"
              ></textarea>
            </view>
          </view>
          
          <view class="form-divider"></view>
          
          <!-- 地址操作区 -->
          <view class="address-actions">
            <!-- 保存地址开关 -->
            <view class="save-address-option">
              <view class="option-label">
                <text>保存到地址库</text>
                <text class="option-desc">方便下次使用</text>
              </view>
              <switch 
                :checked="saveAddressToBook" 
                @change="toggleSaveAddress" 
                color="#8f1911"
                style="transform: scale(0.8);"
              />
            </view>
          </view>
        </view>

        <!-- 备注信息 -->
        <view class="remark-card">
          <view class="remark-title">
            <text>备注信息</text>
            <text class="optional">(选填)</text>
          </view>
          <textarea 
            class="remark-textarea" 
            placeholder="请输入备注信息" 
            v-model="remark"
            maxlength="200"
          ></textarea>
          <text class="remark-count">{{ remark.length }}/200</text>
        </view>

        <!-- 底部操作按钮 -->
        <view class="bottom-actions">
          <view class="action-btn back-btn" @tap="goToPreviousStep">
            上一步
          </view>
          <view 
            class="action-btn submit-btn" 
            :class="{ 'disabled': !canSubmit }"
            @tap="submitOutboundRequest"
          >
            提交申请
          </view>
        </view>
      </view>

      <!-- 步骤4: 提交成功 -->
      <view v-else-if="currentStep === 3" class="step-content success-content">
        <view class="success-icon">
          <uni-icons type="checkmarkempty" size="80" color="#8f1911"></uni-icons>
        </view>
        <text class="success-title">出库申请提交成功</text>
        <text class="success-desc">您的出库申请已提交，请等待审核</text>
        <text class="success-order">申请单号: {{ outboundNo }}</text>
        
        <view class="success-actions">
          <view class="action-btn" @tap="navigateToOutboundList">
            查看申请记录
          </view>
          <view class="action-btn primary-btn" @tap="navigateToMaterialPage">
            返回物料管理
          </view>
        </view>
      </view>

      <!-- 省市区选择弹窗 -->
      <su-region-picker
        :show="showRegionPicker"
        @cancel="showRegionPicker = false"
        @confirm="onRegionConfirm"
      />
    </view>
  </s-layout>
</template>

<script>
// 组件声明
export default {
  components: {
    'uni-steps': () => import('@/uni_modules/uni-steps/components/uni-steps/uni-steps.vue'),
    'uni-icons': () => import('@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'),
    'uni-load-more': () => import('@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue'),
    'su-region-picker': () => import('@/sheep/ui/su-region-picker/su-region-picker.vue'),
    'su-sticky': () => import('@/sheep/ui/su-sticky/su-sticky.vue')
  }
};
</script>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { getOutboundMaterialList, createMaterialOutbound } from '@/yehu/api/mine/material-outbound';
import AddressApi from '@/sheep/api/member/address';
import { getMaterialList } from '@/yehu/api/mine';
import { onShow } from '@dcloudio/uni-app';

// 步骤配置
const stepOptions = [
  { title: '选择物料' },
  { title: '设置数量' },
  { title: '填写信息' },
  { title: '提交成功' }
];
const currentStep = ref(0);

// 物料数据
const materialList = ref([]);
const selectedMaterials = ref([]);
const loading = ref(true);

// 地址数据
const addressList = ref([]);
const selectedAddressId = ref(null);
const loadingAddress = ref(true);

// 备注信息
const remark = ref('');

// 提交成功信息
const outboundNo = ref('');

// 表单数据
const addressForm = ref({
  name: '',
  mobile: '',
  areaName: '',
  areaId: '',
  detailAddress: '',
  province: '',
  city: '',
  district: ''
});
const saveAddressToBook = ref(true);
const showRegionPicker = ref(false);

// 计算属性：是否可以进入地址选择步骤
const canProceedToAddress = computed(() => {
  return selectedMaterials.value.every(item => {
    const quantity = parseInt(item.quantity);
    return !isNaN(quantity) && quantity > 0 && quantity <= item.stock;
  });
});

// 计算属性：是否可以提交
const canSubmit = computed(() => {
  return addressForm.value.name && 
         addressForm.value.mobile && 
         addressForm.value.areaName && 
         addressForm.value.detailAddress;
});

// 获取物料列表
const fetchMaterialList = async () => {
  loading.value = true;
  try {
    const res = await getMaterialList({
      pageNo: 1,
      pageSize: 50
    });
    
    if (res && res.data && res.data.list) {
      // 处理物料数据，只保留有库存的物料，同时过滤掉编号为90000的原料物料
      materialList.value = res.data.list
        .map(item => {
          const materialId = item.materialId || item.id || '';
          const materialName = item.materialName || item.name || '未命名物料';
          const materialImage = item.materialImage || item.image || '';
          const balance = item.balance || item.stock || 0;
          const unit = item.unit || '件';
          
          return {
            id: materialId,
            name: materialName,
            image: materialImage || 'https://cdn.example.com/static/pic/product/1.png',
            stock: balance,
            unit: unit
          };
        })
        .filter(item => item.stock > 0 && item.id !== 90000); // 只保留有库存的物料，并过滤掉编号为90000的原料物料
    } else {
      materialList.value = [];
    }
  } catch (err) {
    console.error('获取物料列表失败', err);
    uni.showToast({
      title: '获取物料列表失败',
      icon: 'none'
    });
    materialList.value = [];
  } finally {
    loading.value = false;
  }
};

// 获取地址列表
const fetchAddressList = async () => {
  loadingAddress.value = true;
  try {
    const res = await AddressApi.getAddressList();
    if (res && res.data) {
      addressList.value = res.data;
      
      // 如果有默认地址，则自动选中
      const defaultAddress = res.data.find(addr => addr.defaultStatus);
      if (defaultAddress) {
        selectedAddressId.value = defaultAddress.id;
      } else if (res.data.length > 0) {
        selectedAddressId.value = res.data[0].id;
      }
    } else {
      addressList.value = [];
    }
  } catch (err) {
    console.error('获取地址列表失败', err);
    uni.showToast({
      title: '获取地址列表失败',
      icon: 'none'
    });
    addressList.value = [];
  } finally {
    loadingAddress.value = false;
  }
};

// 切换物料选择
const toggleMaterialSelection = (material) => {
  const index = selectedMaterials.value.findIndex(item => item.id === material.id);
  if (index !== -1) {
    // 如果已选中，则取消选择
    selectedMaterials.value.splice(index, 1);
  } else {
    // 如果未选中，则添加到选中列表，并设置初始数量为1
    selectedMaterials.value.push({
      ...material,
      quantity: 1
    });
  }
};

// 增加数量
const increaseQuantity = (index) => {
  const item = selectedMaterials.value[index];
  const currentValue = parseInt(item.quantity) || 0;
  
  // 不能超过库存量
  if (currentValue < item.stock) {
    item.quantity = currentValue + 1;
  } else {
    // 超过库存时给予提示
    uni.showToast({
      title: `最多只能设置${item.stock}${item.unit}`,
      icon: 'none'
    });
  }
};

// 减少数量
const decreaseQuantity = (index) => {
  const item = selectedMaterials.value[index];
  const currentValue = parseInt(item.quantity) || 0;
  
  if (currentValue > 1) {
    item.quantity = currentValue - 1;
  } else {
    // 当数量已经为1时给予提示
    uni.showToast({
      title: '出库数量最少为1',
      icon: 'none'
    });
  }
};

// 验证数量输入
const validateQuantity = (index) => {
  const item = selectedMaterials.value[index];
  let val = parseInt(item.quantity);
  
  if (isNaN(val) || val < 1) {
    item.quantity = 1;
    uni.showToast({
      title: '出库数量最少为1',
      icon: 'none'
    });
  } else if (val > item.stock) {
    item.quantity = item.stock;
    uni.showToast({
      title: `最多只能设置${item.stock}${item.unit}`,
      icon: 'none'
    });
  } else {
    item.quantity = val;
  }
};

// 选择地址
const selectAddress = (address) => {
  selectedAddressId.value = address.id;
};

// 格式化地址
const formatAddress = (address) => {
  const province = address.province || '';
  const city = address.city || '';
  const district = address.district || '';
  const detail = address.detail || '';
  
  return `${province} ${city} ${district} ${detail}`.trim();
};

// 跳转到添加地址页面
const navigateToAddAddress = () => {
  uni.navigateTo({
    url: '/pages/user/address/edit'
  });
};

// 前往下一步
const goToNextStep = () => {
  if (currentStep.value === 0) {
    if (selectedMaterials.value.length === 0) {
      uni.showToast({
        title: '请选择至少一种物料',
        icon: 'none'
      });
      return;
    }
    currentStep.value = 1;
  } else if (currentStep.value === 1) {
    // 进行严格的库存检查
    const invalidItem = selectedMaterials.value.find(item => {
      const quantity = parseInt(item.quantity);
      // 检查数量是否有效且不超过库存
      return isNaN(quantity) || quantity <= 0 || quantity > item.stock;
    });
    
    if (invalidItem) {
      uni.showToast({
        title: `${invalidItem.name}的数量无效或超出库存限制`,
        icon: 'none'
      });
      return;
    }
    
    // 如果全部通过库存检查，进入下一步
    currentStep.value = 2;
    // 加载地址列表
    fetchAddressList();
  }
};

// 返回上一步
const goToPreviousStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
};

// 显示省市区选择弹窗
const showAddressSelector = () => {
  showRegionPicker.value = true;
};

// 处理省市区选择
const onRegionConfirm = (region) => {
  addressForm.value.areaName = `${region.province_name} ${region.city_name} ${region.district_name}`;
  addressForm.value.areaId = region.district_id;
  addressForm.value.province = region.province_name;
  addressForm.value.city = region.city_name;
  addressForm.value.district = region.district_name;
  showRegionPicker.value = false;
};

// 跳转到地址列表页面
const navigateToAddressList = () => {
  uni.navigateTo({
    url: '/pages/user/address/list?type=select'
  });
};

// 切换保存地址到地址库的开关
const toggleSaveAddress = (e) => {
  saveAddressToBook.value = e.detail.value;
};

// 监听地址选择事件
uni.$on('SELECT_ADDRESS', ({ addressInfo }) => {
  if (addressInfo) {
    addressForm.value.name = addressInfo.name;
    addressForm.value.mobile = addressInfo.mobile;
    addressForm.value.areaName = addressInfo.areaName;
    addressForm.value.areaId = addressInfo.areaId;
    addressForm.value.detailAddress = addressInfo.detailAddress;
    
    // 解析省市区信息
    if (addressInfo.areaName) {
      const areaNames = addressInfo.areaName.split(' ');
      if (areaNames.length >= 3) {
        addressForm.value.province = areaNames[0] || '';
        addressForm.value.city = areaNames[1] || '';
        addressForm.value.district = areaNames[2] || '';
      }
    }
  }
});

// 提交出库申请
const submitOutboundRequest = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善收货信息',
      icon: 'none'
    });
    return;
  }
  
  // 检查省市区是否已选择
  if (!addressForm.value.province || !addressForm.value.city || !addressForm.value.district) {
    uni.showToast({
      title: '请选择所在地区',
      icon: 'none'
    });
    return;
  }
  
  // 再次校验所有物料的出库数量是否合法
  const invalidItem = selectedMaterials.value.find(item => {
    const quantity = parseInt(item.quantity);
    return isNaN(quantity) || quantity <= 0 || quantity > item.stock;
  });
  
  if (invalidItem) {
    uni.showToast({
      title: `${invalidItem.name}的数量无效或超出库存限制`,
      icon: 'none'
    });
    return;
  }
  
  uni.showLoading({
    title: '提交申请中...'
  });
  
  try {
    // 先保存地址到地址库，并获取地址ID
    let addressId = null;
    
    try {
      const addressData = {
        name: addressForm.value.name,
        mobile: addressForm.value.mobile,
        areaId: addressForm.value.areaId,
        areaName: addressForm.value.areaName,
        detailAddress: addressForm.value.detailAddress,
        defaultStatus: saveAddressToBook.value // 根据用户选择决定是否设为默认地址
      };
      
      // 保存地址，后端会自动检查是否存在相同地址
      const res = await AddressApi.createAddress(addressData);
      if (res && res.data) {
        addressId = res.data;
      } else {
        throw new Error('创建地址失败');
      }
    } catch (err) {
      console.error('保存地址失败', err);
      throw new Error('保存收货地址失败，请重试');
    }
    
    // 准备提交数据
    const outboundData = {
      addressId: addressId, // 使用保存后的地址ID
      // 添加收货地址信息
      receiverName: addressForm.value.name,
      receiverMobile: addressForm.value.mobile,
      receiverProvince: addressForm.value.province || '',
      receiverCity: addressForm.value.city || '',
      receiverDistrict: addressForm.value.district || '',
      receiverDetailAddress: addressForm.value.detailAddress,
      remark: remark.value,
      items: selectedMaterials.value.map(item => ({
        materialId: item.id,
        quantity: parseInt(item.quantity),
        unit: item.unit // 添加单位信息
      }))
    };
    
    console.log('提交出库申请数据:', outboundData);
    
    // 发送请求
    const res = await createMaterialOutbound(outboundData);
    console.log('出库申请响应:', res);
    
    // 处理响应 - 后端直接返回出库单ID和单号
    if (res && res.code === 0 && res.data) {
      // 直接使用返回的出库单号
      outboundNo.value = res.data.outboundNo || `临时单号-${res.data.id}`;
      currentStep.value = 3; // 进入成功页面
    } else {
      throw new Error(res.msg || '提交失败，请重试');
    }
  } catch (err) {
    console.error('提交出库申请失败', err);
    uni.showToast({
      title: err.message || '提交出库申请失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};

// 跳转到出库申请记录页面
const navigateToOutboundList = () => {
  uni.navigateTo({
    url: '/yehu/pages/mine/material/outbound/list'
  });
};

// 返回物料管理页面
const navigateToMaterialPage = () => {
  uni.navigateBack({
    delta: 1
  });
};

onMounted(() => {
  // 加载物料列表
  fetchMaterialList();
  
  // 监听地址选择事件
  uni.$on('SELECT_ADDRESS', ({ addressInfo }) => {
    if (addressInfo) {
      addressForm.value.name = addressInfo.name;
      addressForm.value.mobile = addressInfo.mobile;
      addressForm.value.areaName = addressInfo.areaName;
      addressForm.value.areaId = addressInfo.areaId;
      addressForm.value.detailAddress = addressInfo.detailAddress;
      
      // 解析省市区信息
      if (addressInfo.areaName) {
        const areaNames = addressInfo.areaName.split(' ');
        if (areaNames.length >= 3) {
          addressForm.value.province = areaNames[0] || '';
          addressForm.value.city = areaNames[1] || '';
          addressForm.value.district = areaNames[2] || '';
        }
      }
    }
  });
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  uni.$off('SELECT_ADDRESS');
});
</script>

<style lang="scss" scoped>
.outbound-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  position: relative;
}

// 步骤条容器样式
.step-container {
  background-color: #fff;
  padding: 30rpx 20rpx;
  z-index: 10;
}

.step-content {
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .select-address-btn {
    font-size: 26rpx;
    font-weight: normal;
    padding: 10rpx 20rpx;
    background-color: rgba(143, 25, 17, 0.08);
    border-radius: 30rpx;
    display: flex;
    align-items: center;
    
    .uni-icons {
      margin-right: 8rpx;
    }
  }
}

// 物料列表样式
.material-list {
  margin-bottom: 30rpx;
}

.material-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  &.selected {
    background-color: rgba(143, 25, 17, 0.05);
  }
  
  .material-info {
    display: flex;
    align-items: flex-start;
    
    .material-image {
      width: 100rpx;
      height: 100rpx;
      border-radius: 8rpx;
      background-color: #f5f5f5;
      object-fit: cover;
    }
    
    .material-details {
      margin-left: 20rpx;
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .material-name {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 12rpx;
        word-break: break-all;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        line-height: 1.3;
      }
      
      .material-stock {
        font-size: 24rpx;
        color: #999;
        line-height: 1.2;
        display: block;
      }
    }
  }
}

// 数量控制样式
.quantity-list {
  padding: 0 20rpx;
}

.quantity-item {
  display: flex;
  flex-direction: column;
  padding: 30rpx;
  margin-bottom: 30rpx;
  background-color: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  
  .material-info {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .material-image {
      width: 120rpx;
      height: 120rpx;
      border-radius: 12rpx;
      background-color: #f5f5f5;
      object-fit: cover;
      box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
    }
    
    .material-details {
      margin-left: 20rpx;
      flex: 1;
      
      .material-name {
        font-size: 30rpx;
        color: #333;
        font-weight: 600;
        margin-bottom: 12rpx;
        display: block;
      }
      
      .material-stock {
        font-size: 26rpx;
        color: #999;
        background-color: #f8f8f8;
        padding: 4rpx 12rpx;
        border-radius: 20rpx;
        display: block;
        margin-top: 10rpx;
      }
    }
  }
  
  .quantity-setting {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 20rpx;
    border-top: 1rpx dashed #eee;
    
    .quantity-label {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }
    
    .quantity-control {
      display: flex;
      align-items: center;
      border: 1.5rpx solid #e0e0e0;
      border-radius: 12rpx;
      overflow: hidden;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
      
      .quantity-btn {
        width: 70rpx;
        height: 70rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f8f8f8;
        font-size: 32rpx;
        font-weight: bold;
        transition: all 0.2s;
        
        &:active {
          background-color: #eaeaea;
        }
        
        &.minus-btn {
          border-right: 1rpx solid #e0e0e0;
          color: #666;
        }
        
        &.plus-btn {
          border-left: 1rpx solid #e0e0e0;
          color: #8f1911;
        }
      }
      
      .quantity-input {
        width: 100rpx;
        height: 70rpx;
        text-align: center;
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        background-color: #fff;
      }
    }
  }
}

// 地址列表样式
.address-list {
  margin-bottom: 30rpx;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  &.selected {
    background-color: rgba(143, 25, 17, 0.05);
  }
  
  .address-info {
    flex: 1;
    
    .address-header {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;
      
      .address-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-right: 20rpx;
      }
      
      .address-mobile {
        font-size: 26rpx;
        color: #666;
      }
      
      .address-default-tag {
        margin-left: 20rpx;
        font-size: 22rpx;
        color: #8f1911;
        border: 1rpx solid #8f1911;
        padding: 2rpx 10rpx;
        border-radius: 20rpx;
      }
    }
    
    .address-detail {
      font-size: 26rpx;
      color: #666;
      line-height: 1.4;
    }
  }
}

.add-address-row {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  margin-bottom: 30rpx;
  
  .add-address-text {
    font-size: 28rpx;
    color: #8f1911;
    margin-left: 10rpx;
  }
}

// 备注区域样式
.remark-section {
  margin-bottom: 30rpx;
  
  .remark-input {
    width: 100%;
    height: 200rpx;
    padding: 20rpx;
    font-size: 28rpx;
    background-color: #f8f8f8;
    border-radius: 8rpx;
    box-sizing: border-box;
  }
  
  .remark-count {
    font-size: 24rpx;
    color: #999;
    text-align: right;
    margin-top: 10rpx;
  }
}

// 底部操作按钮
.bottom-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 40rpx;
  
  .action-btn {
    flex: 1;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 40rpx;
    font-size: 28rpx;
    
    &.back-btn {
      margin-right: 20rpx;
      background-color: #f0f0f0;
      color: #666;
    }
    
    &.next-btn, &.submit-btn {
      background-color: #8f1911;
      color: #fff;
      
      &.disabled {
        opacity: 0.6;
      }
    }
  }
}

// 加载和空状态样式
.loading-container, .empty-container {
  padding: 60rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
    margin-bottom: 30rpx;
  }
  
  .add-address-btn {
    padding: 16rpx 40rpx;
    background-color: #8f1911;
    color: #fff;
    border-radius: 30rpx;
    font-size: 28rpx;
  }
}

// 成功页面样式
.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  
  .success-icon {
    margin-bottom: 30rpx;
  }
  
  .success-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .success-desc {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 20rpx;
  }
  
  .success-order {
    font-size: 26rpx;
    color: #999;
    margin-bottom: 60rpx;
  }
  
  .success-actions {
    width: 100%;
    display: flex;
    justify-content: space-between;
    
    .action-btn {
      flex: 1;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 40rpx;
      font-size: 28rpx;
      margin: 0 20rpx;
      
      &:first-child {
        background-color: #f0f0f0;
        color: #666;
      }
      
      &.primary-btn {
        background-color: #8f1911;
        color: #fff;
      }
    }
  }
}

// 地址卡片样式
.address-card {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 24rpx;
  
  .form-group {
    padding: 10rpx 0;
  }
  
  .form-item {
    margin-bottom: 24rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    
    .required {
      color: #ff4d4f;
      margin-right: 4rpx;
    }
  }
  
  .form-input {
    width: 100%;
    height: 88rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #333;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    box-sizing: border-box;
    transition: all 0.3s;
    
    &:focus {
      background-color: #f0f0f0;
    }
  }
  
  .form-textarea {
    width: 100%;
    height: 180rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #333;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    box-sizing: border-box;
    transition: all 0.3s;
    
    &:focus {
      background-color: #f0f0f0;
    }
  }
  
  .area-input {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    
    .placeholder {
      color: #999;
    }
  }
  
  .form-divider {
    height: 1rpx;
    background-color: #f0f0f0;
    margin: 20rpx 0;
  }
}

// 地址操作区样式
.address-actions {
  display: flex;
  // justify-content: center;
  align-items: center;
  padding: 20rpx 0;
  
  .save-address-option {
    display: flex;
    // align-items: center;
    justify-content: space-between;
    
    .option-label {
      margin-right: 20rpx;
      display: flex;
      flex-direction: column;
      
      text {
        font-size: 26rpx;
        color: #333;
      }
      
      .option-desc {
        font-size: 22rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
  }
}

// 备注卡片样式
.remark-card {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
  
  .remark-title {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    
    .optional {
      font-size: 24rpx;
      color: #999;
      font-weight: normal;
      margin-left: 8rpx;
    }
  }
  
  .remark-textarea {
    width: 100%;
    height: 200rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #333;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    box-sizing: border-box;
    margin-bottom: 10rpx;
  }
  
  .remark-count {
    font-size: 24rpx;
    color: #999;
    text-align: right;
  }
}

// 底部操作按钮
.bottom-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 40rpx;
  
  .action-btn {
    flex: 1;
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 44rpx;
    font-size: 30rpx;
    font-weight: 500;
    transition: all 0.3s;
    
    &.back-btn {
      margin-right: 20rpx;
      background-color: #f5f5f5;
      color: #666;
      
      &:active {
        background-color: #e8e8e8;
      }
    }
    
    &.next-btn, &.submit-btn {
      background-color: #8f1911;
      color: #fff;
      
      &:active {
        opacity: 0.9;
      }
      
      &.disabled {
        opacity: 0.6;
      }
    }
  }
}
</style> 