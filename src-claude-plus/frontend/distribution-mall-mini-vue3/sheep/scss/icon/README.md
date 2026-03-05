# 🎯 图标库使用指南

## 📦 图标库总览

本项目包含 **6 个字体图标库**，提供丰富的图标选择：

| 图标库                | 图标数量 | 文件大小 | 主要特点         | 适用场景             |
| --------------------- | -------- | -------- | ---------------- | -------------------- |
| 🐑 **SheepIcon**      | 22 个    | ~8KB     | 商城专用，轻量级 | **商城应用**（推荐） |
| 🎨 **ColorUI**        | 42 个    | ~15KB    | 通用界面图标     | 通用 Web 应用        |
| 🌈 **ColorIcon**      | 441 个   | ~120KB   | 图标最丰富       | 复杂业务系统         |
| 🔢 **UI-Num**         | 数字专用 | ~5KB     | 数字显示优化     | 价格、倒计时         |
| 🏭 **工作流程图标**   | 流程专用 | ~10KB    | 流程设计器       | 工作流程             |
| 🛠️ **表单构建器图标** | 表单专用 | ~12KB    | 表单构建         | 表单设计器           |

---

## 🚀 快速预览

### **查看所有图标库**

```bash
# 综合对比页面 - 了解所有图标库
open icons-comparison.html

# SheepIcon 预览 - 商城专用图标
open sheepicon-preview.html

# ColorIcon 预览 - 超大图标库
open coloricon-preview.html

# ColorUI 预览 - 通用图标
open icon-preview.html
```

### **🏆 推荐方案**

#### **商城应用首选** ⭐⭐⭐⭐⭐

```scss
// 导入推荐组合
@import './sheep/scss/icon/_sheepicon.scss'; // 商城专用
@import './sheep/scss/icon/_icon.scss'; // 通用补充
```

#### **通用应用推荐** ⭐⭐⭐⭐

```scss
// 导入通用组合
@import './sheep/scss/icon/_icon.scss'; // 主要图标
@import './sheep/scss/icon/_sheepicon.scss'; // 商城补充
```

---

## 🐑 SheepIcon 图标库 (商城专用)

### **基本信息**

- **位置**: `sheep/scss/icon/_sheepicon.scss`
- **类名前缀**: `sicon-`
- **图标数量**: 22 个
- **特点**: 专为商城应用设计，轻量且实用

### **使用方式**

```vue
<template>
  <!-- 导航图标 -->
  <text class="sicon-home"></text>
  <text class="sicon-back"></text>

  <!-- 电商功能 -->
  <text class="sicon-cart"></text>
  <text class="sicon-orders"></text>
  <text class="sicon-goods-list"></text>

  <!-- 用户操作 -->
  <text class="sicon-collect"></text>
  <text class="sicon-edit"></text>
</template>
```

### **完整图标列表**

| 图标 | 类名                     | 描述     |
| ---- | ------------------------ | -------- |
| 🏠   | `sicon-home`             | 首页     |
| 🔙   | `sicon-back`             | 返回     |
| ⚙️   | `sicon-more`             | 更多     |
| ✏️   | `sicon-edit`             | 编辑     |
| ⭐   | `sicon-collect`          | 收藏     |
| 🛒   | `sicon-cart`             | 购物车   |
| 📦   | `sicon-orders`           | 订单     |
| 🛍️   | `sicon-goods-list`       | 商品列表 |
| 💳   | `sicon-goods-card`       | 商品卡片 |
| 🚚   | `sicon-delivery`         | 配送     |
| 🚚   | `sicon-transport`        | 物流     |
| 📱   | `sicon-qrcode`           | 二维码   |
| ✅   | `sicon-check-line`       | 选中     |
| ⭕   | `sicon-unchecked`        | 未选中   |
| ⚠️   | `sicon-warning-line`     | 警告     |
| ❓   | `sicon-question-outline` | 问号     |
| ✅   | `sicon-circlecheck`      | 圆形勾选 |
| ❌   | `sicon-circleclose`      | 圆形关闭 |
| ⭐   | `sicon-score1`           | 星级 1   |
| ⭐   | `sicon-score2`           | 星级 2   |
| ⚠️   | `sicon-warning-outline`  | 警告轮廓 |
| ⚙️   | `sicon-basic`            | 基础设置 |

---

## 🌈 ColorIcon 图标库 (超大图标库)

### **基本信息**

- **位置**: `sheep/scss/icon/_coloricon.scss`
- **类名前缀**: `cicon-`
- **图标数量**: **441 个** (最大图标库)
- **特点**: 功能最全面，分类清晰

### **分类概览**

| 分类        | 图标数量 | 主要用途 |
| ----------- | -------- | -------- |
| 📱 基础图标 | 40+      | 基础操作 |
| 🎯 操作类   | 35+      | 用户操作 |
| 🧭 导航类   | 30+      | 界面导航 |
| 👥 社交类   | 35+      | 社交功能 |
| 🛍️ 电商类   | 25+      | 商城功能 |
| 🎬 媒体类   | 20+      | 多媒体   |
| 📁 文件类   | 30+      | 文件管理 |
| ⚙️ 系统类   | 25+      | 系统功能 |
| 👤 用户类   | 20+      | 用户相关 |
| 💬 通讯类   | 25+      | 通讯功能 |
| 💳 支付类   | 20+      | 支付相关 |
| 🎨 杂项     | 100+     | 其他功能 |

### **使用方式**

```vue
<template>
  <!-- 基础图标 -->
  <text class="cicon-home"></text>
  <text class="cicon-user"></text>

  <!-- 社交图标 -->
  <text class="cicon-wechat"></text>
  <text class="cicon-qq"></text>

  <!-- 电商图标 -->
  <text class="cicon-cart"></text>
  <text class="cicon-store"></text>
</template>
```

---

## 🎨 ColorUI IconFont (通用图标)

### **基本信息**

- **项目 ID**: 2620914
- **类名前缀**: `_icon-`
- **图标数量**: 42 个
- **特点**: 经典通用图标，轻量稳定

### **使用方式**

```vue
<template>
  <!-- 基础操作 -->
  <text class="_icon-search"></text>
  <text class="_icon-add"></text>
  <text class="_icon-edit"></text>
  <text class="_icon-delete"></text>

  <!-- 导航 -->
  <text class="_icon-home"></text>
  <text class="_icon-back"></text>
  <text class="_icon-more"></text>

  <!-- 状态 -->
  <text class="_icon-loading"></text>
  <text class="_icon-warn"></text>
  <text class="_icon-info"></text>
</template>
```

---

## 🚀 快速开始

### 1. 打开预览页面

```bash
# 在浏览器中打开预览页面查看所有图标
open icon-preview.html
```

### 2. 在项目中使用

#### **ColorUI 图标使用方式**

**方式 1: 直接使用类名**

```vue
<template>
  <!-- 搜索图标 -->
  <text class="_icon-search"></text>

  <!-- 添加图标 -->
  <view class="_icon-add"></view>

  <!-- 删除图标 -->
  <text class="_icon-delete"></text>
</template>
```

**方式 2: CSS 伪元素**

```scss
.custom-icon {
  &::before {
    font-family: 'colorui';
    content: '\e782'; /* 搜索图标 Unicode */
  }
}
```

#### **UI-Num 数字字体使用**

```vue
<template>
  <!-- 价格显示 -->
  <text class="ui-number">¥128.99</text>

  <!-- 倒计时 -->
  <view class="ui-number">09:58:30</text>

  <!-- 百分比 -->
  <text class="ui-number">85%</text>
</template>

<style>
.ui-number {
  font-family: 'ui-num', monospace;
  font-size: 1.2em;
  color: #4f46e5;
}
</style>
```

---

## 📋 完整图标列表

### 🔤 操作类图标 (9 个)

| 图标 | 类名                | Unicode | 描述         |
| ---- | ------------------- | ------- | ------------ |
| ✏️   | `_icon-edit`        | \e649   | 编辑         |
| ❌   | `_icon-close`       | \e6ed   | 关闭         |
| 🗑️   | `_icon-delete`      | \e707   | 删除         |
| 🗑️   | `_icon-delete-o`    | \e709   | 空心删除     |
| ➕   | `_icon-add-round`   | \e717   | 圆形添加     |
| ➕   | `_icon-add-round-o` | \e718   | 空心圆形添加 |
| ➕   | `_icon-add`         | \e6e4   | 添加         |
| 🔄   | `_icon-move`        | \e768   | 移动         |
| 📋   | `_icon-copy`        | \e85c   | 复制         |
| 📋   | `_icon-copy-o`      | \e7bc   | 空心复制     |

### 🔄 状态类图标 (8 个)

| 图标 | 类名                  | Unicode | 描述         |
| ---- | --------------------- | ------- | ------------ |
| ✅   | `_icon-check-round`   | \e6f1   | 圆形打勾     |
| ✅   | `_icon-check-round-o` | \e6f2   | 空心圆形打勾 |
| ⏳   | `_icon-waiting`       | \e6f8   | 等待         |
| ⏳   | `_icon-waiting-o`     | \e6f9   | 空心等待     |
| ⚠️   | `_icon-warn`          | \e662   | 警告         |
| ⚠️   | `_icon-warn-o`        | \e675   | 空心警告     |
| ℹ️   | `_icon-info`          | \e6ef   | 信息         |
| ℹ️   | `_icon-info-o`        | \e705   | 空心信息     |
| ✅   | `_icon-check`         | \e69f   | 对勾         |
| 🔄   | `_icon-loading`       | \e746   | 加载中       |
| 🔄   | `_icon-loader`        | \e76d   | 加载器       |

### 🧭 导航类图标 (4 个)

| 图标 | 类名            | Unicode | 描述     |
| ---- | --------------- | ------- | -------- |
| 🏠   | `_icon-home-o`  | \e70a   | 空心首页 |
| 🏠   | `_icon-home`    | \e70d   | 实心首页 |
| ⬅️   | `_icon-back`    | \e600   | 返回     |
| ➡️   | `_icon-forward` | \e601   | 前进     |
| ➡️   | `_icon-arrow`   | \e608   | 箭头     |

### 📝 表单类图标 (4 个)

| 图标 | 类名               | Unicode | 描述           |
| ---- | ------------------ | ------- | -------------- |
| ☑️   | `_icon-checkbox`   | \e713   | 选中的复选框   |
| ⬜   | `_icon-box`        | \e714   | 未选中的复选框 |
| ☑️   | `_icon-checkbox-o` | \e715   | 空心复选框     |
| ⭕   | `_icon-round`      | \e716   | 圆形单选       |

### 🎨 界面类图标 (7 个)

| 图标 | 类名                 | Unicode | 描述         |
| ---- | -------------------- | ------- | ------------ |
| ⚙️   | `_icon-more`         | \e688   | 更多         |
| 📝   | `_icon-title`        | \e82f   | 标题         |
| 📝   | `_icon-titles`       | \e745   | 多标题       |
| ⬇️   | `_icon-drop-down`    | \e61c   | 下拉         |
| ⬆️   | `_icon-drop-up`      | \e61d   | 上拉         |
| 🔄   | `_icon-move-round`   | \e602   | 圆形移动     |
| 🔄   | `_icon-move-round-o` | \e603   | 空心圆形移动 |

---

## 🎯 最佳实践

### **在 Uni-app 中使用**

```vue
<template>
  <!-- 按钮中的图标 -->
  <button class="btn">
    <text class="_icon-search"></text>
    搜索
  </button>

  <!-- 导航图标 -->
  <view class="nav-item">
    <text class="_icon-home"></text>
    <text>首页</text>
  </view>

  <!-- 状态图标 -->
  <view class="status">
    <text class="_icon-loading" v-if="loading"></text>
    <text class="_icon-check" v-else></text>
  </view>
</template>
```

### **数字显示场景**

```vue
<template>
  <!-- 价格 -->
  <view class="price">
    <text class="ui-number">¥</text>
    <text class="ui-number">{{ price }}</text>
  </view>

  <!-- 倒计时 -->
  <view class="countdown">
    <text class="ui-number">{{ formatTime(time) }}</text>
  </view>

  <!-- 统计数字 -->
  <view class="stats">
    <text class="ui-number">{{ count }}</text>
    <text>件商品</text>
  </view>
</template>
```

### **自定义样式**

```scss
// 图标大小
.icon-large {
  font-size: 24px;
}

.icon-small {
  font-size: 12px;
}

// 图标颜色
.icon-primary {
  color: #4f46e5;
}

.icon-success {
  color: #10b981;
}

.icon-warning {
  color: #f59e0b;
}

.icon-danger {
  color: #ef4444;
}

// 数字样式
.price-number {
  font-family: 'ui-num', monospace;
  font-size: 1.5em;
  font-weight: bold;
  color: #ef4444;
}

.countdown-number {
  font-family: 'ui-num', monospace;
  font-size: 1.2em;
  letter-spacing: 2px;
}
```

---

## 🔧 图标库扩展

### **添加新图标**

1. **访问 IconFont 平台**

   ```
   https://www.iconfont.cn/manage/index?manage_type=myprojects&projectId=2620914
   ```

2. **添加图标到项目**

   - 选择需要的图标
   - 添加到项目 ID 2620914
   - 重新生成字体文件

3. **更新字体文件**
   - 更新 `_icon.scss` 中的 Base64 字符串
   - 添加新的图标类定义

### **创建自定义图标**

```scss
// 自定义图标类
.my-custom-icon {
  font-family: 'colorui';

  &::before {
    content: '\eXXX'; // 替换为实际Unicode
  }
}
```

---

## 🐛 常见问题

### **Q: 图标不显示？**

A: 检查以下几点：

1. 确保导入了 `_icon.scss` 文件
2. 检查类名是否正确
3. 确认字体文件路径正确

### **Q: 数字字体样式不生效？**

A: 确保：

1. 设置了 `font-family: 'ui-num'`
2. 字体文件已正确加载
3. 数字内容为纯数字或允许的符号

### **Q: 如何改变图标大小？**

A: 使用 `font-size` 属性：

```scss
.my-icon {
  font-size: 20px; /* 设置图标大小 */
}
```

### **Q: 如何改变图标颜色？**

A: 使用 `color` 属性：

```scss
.my-icon {
  color: #4f46e5; /* 设置图标颜色 */
}
```

---

## 📞 技术支持

如有问题或建议，请：

1. 查看 `icon-preview.html` 了解所有可用图标
2. 检查本 README 文档的使用说明
3. 联系项目维护者

---

**最后更新**: 2024-12-12 **版本**: v1.0.0
