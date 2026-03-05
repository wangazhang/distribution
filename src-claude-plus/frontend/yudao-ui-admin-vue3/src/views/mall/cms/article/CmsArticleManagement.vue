<template>
  <div class="cms-article-management">
    <ContentWrap>
      <!-- 搜索工作栏 -->
      <el-form
        class="-mb-15px"
        :model="searchParams"
        ref="searchFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="文章标题" prop="title">
          <el-input
            v-model="searchParams.title"
            placeholder="请输入文章标题"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch">
            <Icon icon="ep:search" class="mr-5px" /> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button
            type="primary"
            @click="handleAddArticle"
            :disabled="!activeSectionId || activeSectionId === 'add-section'"
          >
            <Icon icon="ep:plus" class="mr-5px" /> 新增文章
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 板块Tabs及内容区域 -->
    <ContentWrap class="section-content-wrap">
      <!-- 板块Tabs -->
      <CmsSectionTabs
        :sections="sections"
        :active-section-id="activeSectionId"
        @change="handleSectionChange"
        @add="handleAddSection"
      />

      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 初始状态或无板块时的提示 -->
        <div v-if="!activeSectionId || activeSectionId === 'add-section'" class="empty-state">
          <el-empty
            v-if="sections.length === 0"
            description="暂无板块"
            :image-size="120"
          >
            <el-button type="primary" @click="handleAddSection">
              <Icon icon="ep:plus" class="mr-5px" />
              创建第一个板块
            </el-button>
          </el-empty>
          <div v-else class="select-section-prompt">
            <Icon icon="ep:guide" class="prompt-icon" />
            <p>请选择一个板块开始管理文章</p>
          </div>
        </div>

        <!-- 板块内容 -->
        <template v-else>
          <!-- 分类和标签筛选区域 -->
          <CmsCategoryBar
            :categories="categories"
            :selected-category-ids="selectedCategoryIds"
            :tags="tags"
            :selected-tag-ids="selectedTagIds"
            @category-change="handleCategoryChange"
            @tag-change="handleTagChange"
            @add-category="handleAddCategory"
            @delete-category="handleDeleteCategory"
          />

          <!-- 文章列表 -->
          <div class="article-list-container">
            <el-table
              v-loading="loading"
              :data="articles"
              :stripe="true"
              :show-overflow-tooltip="true"
            >
              <el-table-column label="编号" align="center" prop="id" width="80" />
              <el-table-column label="文章标题" align="center" prop="title" min-width="200" show-overflow-tooltip />
              <el-table-column label="分类" align="center" prop="categoryId" width="120">
                <template #default="scope">{{ getCategoryName(scope.row.categoryId) }}</template>
              </el-table-column>
              <el-table-column label="作者" align="center" prop="authorName" width="120" />
              <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.auditStatus === 'pending'" type="warning">待审核</el-tag>
                  <el-tag v-else-if="scope.row.auditStatus === 'approved'" type="success">已通过</el-tag>
                  <el-tag v-else-if="scope.row.auditStatus === 'rejected'" type="danger">已拒绝</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布状态" align="center" prop="publishStatus" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.publishStatus === 1" type="success">已发布</el-tag>
                  <el-tag v-else type="info">未发布</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="浏览数" align="center" prop="viewCount" width="80" />
              <el-table-column label="点赞数" align="center" prop="likeCount" width="80" />
              <el-table-column
                label="发布时间"
                align="center"
                prop="publishTime"
                :formatter="dateFormatter"
                width="180px"
              />
              <el-table-column label="关联商品" align="center" width="120">
                <template #default="scope">
                  <el-tag v-if="scope.row.productIds && scope.row.productIds.length > 0" type="success" size="small">
                    {{ scope.row.productIds.length }}个商品
                  </el-tag>
                  <span v-else class="text-gray-400">无</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="240" fixed="right">
                <template #default="scope">
                  <el-button
                    link
                    type="primary"
                    @click="handleEditArticle(scope.row.id)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    link
                    type="success"
                    @click="handleAudit(scope.row)"
                    v-if="scope.row.auditStatus === 'pending'"
                  >
                    审核
                  </el-button>
                  <el-button
                    link
                    type="warning"
                    @click="handlePublish(scope.row.id)"
                    v-if="scope.row.publishStatus === 0 && scope.row.auditStatus === 'approved'"
                  >
                    发布
                  </el-button>
                  <el-button
                    link
                    type="info"
                    @click="handleUnpublish(scope.row.id)"
                    v-if="scope.row.publishStatus === 1"
                  >
                    下架
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    @click="handleDelete(scope.row.id)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <Pagination
              :total="total"
              v-model:page="pagination.pageNo"
              v-model:limit="pagination.pageSize"
              @pagination="handlePageChange"
            />
          </div>
        </template>
      </div>
    </ContentWrap>


    <!-- 新增/编辑文章表单弹窗 -->
    <el-dialog
      v-model="articleFormVisible"
      :title="articleFormTitle"
      width="80%"
      append-to-body
      :close-on-click-modal="false"
    >
      <!-- 步骤指示器 -->
      <div v-if="!isEditMode" class="step-indicator">
        <el-steps :active="articleFormStep - 1" finish-status="success">
          <el-step title="基本信息" description="填写标题、分类、标签等基本信息" />
          <el-step title="内容编辑" description="编写文章正文内容" />
        </el-steps>
      </div>

      <el-form
        ref="articleFormRef"
        :model="articleFormData"
        :rules="getCurrentStepRules()"
        label-width="120px"
        v-loading="formLoading"
      >
        <!-- 编辑模式下的基本信息折叠面板 -->
        <div v-if="isEditMode" class="edit-mode-basic-info" :class="{ expanded: !basicInfoCollapsed }">
          <!-- 自定义折叠面板头部 -->
          <div class="custom-collapse-header" @click="toggleEditMode">
            <Icon icon="ep:document" class="mr-5px" />
            基本信息
            <el-tag v-if="articleFormData.title" type="primary" class="ml-10px">{{ articleFormData.title }}</el-tag>
            <div class="ml-auto">
              <el-button type="text" size="small">
                <Icon :icon="basicInfoCollapsed ? 'ep:edit' : 'ep:arrow-up'" />
                {{ basicInfoCollapsed ? '展开' : '折叠' }}
              </el-button>
            </div>
          </div>

          <!-- 只有在展开时才显示内容 -->
          <div v-if="!basicInfoCollapsed" class="basic-info-content">
            <!-- 编辑模式：可编辑的表单 -->
            <div>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="所属板块" prop="sectionId">
                    <el-select
                      v-model="articleFormData.sectionId"
                      placeholder="请选择板块"
                      class="!w-100%"
                      disabled
                    >
                      <el-option
                        v-for="section in sections"
                        :key="section.id"
                        :label="section.name"
                        :value="section.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="所属分类" prop="categoryId">
                    <el-select
                      v-model="articleFormData.categoryId"
                      placeholder="请选择分类"
                      class="!w-100%"
                      @change="handleFormCategoryChange"
                    >
                      <el-option
                        v-for="category in categories"
                        :key="category.id"
                        :label="category.name"
                        :value="category.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="文章标题" prop="title">
                    <el-input
                      v-model="articleFormData.title"
                      placeholder="请输入文章标题"
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="副标题" prop="subtitle">
                    <el-input
                      v-model="articleFormData.subtitle"
                      placeholder="请输入副标题(可选)"
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="封面图片" prop="coverImages">
                    <UploadImgs v-model="articleFormData.coverImages" :limit="9" />
                    <div class="form-item-tip">最多上传9张图片</div>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="关联标签" prop="tagIds">
                    <div class="tag-selector">
                      <!-- 标签平铺展示区域 -->
                      <div class="existing-tags">
                        <el-tag
                          v-for="tag in tags"
                          :key="tag.id"
                          :type="(articleFormData.tagIds || []).includes(tag.id) ? 'primary' : 'info'"
                          class="tag-item"
                          @click="toggleTag(tag.id)"
                          :closable="false"
                        >
                          {{ tag.name }}
                        </el-tag>

                        <!-- 添加新标签按钮 -->
                        <el-button
                          type="primary"
                          plain
                          size="small"
                          @click="showCreateTagDialog"
                          class="add-tag-btn"
                        >
                          <Icon icon="ep:plus" class="mr-5px" />
                          新建标签
                        </el-button>
                      </div>

                      <!-- 已选标签展示 -->
                      <div v-if="(articleFormData.tagIds || []).length > 0" class="selected-tags">
                        <span class="selected-label">已选标签：</span>
                        <el-tag
                          v-for="tagId in (articleFormData.tagIds || [])"
                          :key="tagId"
                          type="primary"
                          class="selected-tag"
                          @close="removeTag(tagId)"
                          closable
                        >
                          {{ getTagName(tagId) }}
                        </el-tag>
                      </div>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="热门标记" prop="isHot">
                    <el-switch v-model="articleFormData.isHot" :active-value="1" :inactive-value="0" />
                    <span class="ml-10px text-gray-500">标记为热门文章</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="官方标记" prop="isOfficial">
                    <el-switch v-model="articleFormData.isOfficial" :active-value="1" :inactive-value="0" />
                    <span class="ml-10px text-gray-500">标记为官方文章</span>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 功能设置区域 -->
              <el-divider content-position="left">功能设置</el-divider>

              <el-row :gutter="20">
                <!-- 点赞功能 -->
                <el-col :span="8">
                  <el-form-item label="支持点赞">
                    <el-switch v-model="articleFormData.enableLike" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8" v-if="articleFormData.enableLike === 1">
                  <el-form-item label="显示点赞数">
                    <el-switch v-model="articleFormData.showLikeCount" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8" v-if="articleFormData.enableLike === 1">
                  <el-form-item label="初始点赞数">
                    <el-input-number
                      v-model="articleFormData.initialLikeCount"
                      :min="0"
                      :max="999999"
                      placeholder="运营设置的假数据"
                      class="!w-full"
                    />
                  </el-form-item>
                </el-col>

                <!-- 收藏功能 -->
                <el-col :span="8">
                  <el-form-item label="支持收藏">
                    <el-switch v-model="articleFormData.enableCollect" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8" v-if="articleFormData.enableCollect === 1">
                  <el-form-item label="显示收藏数">
                    <el-switch v-model="articleFormData.showCollectCount" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8" v-if="articleFormData.enableCollect === 1">
                  <el-form-item label="初始收藏数">
                    <el-input-number
                      v-model="articleFormData.initialCollectCount"
                      :min="0"
                      :max="999999"
                      placeholder="运营设置的假数据"
                      class="!w-full"
                    />
                  </el-form-item>
                </el-col>

                <!-- 分享功能 -->
                <el-col :span="8">
                  <el-form-item label="支持分享">
                    <el-switch v-model="articleFormData.enableShare" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8" v-if="articleFormData.enableShare === 1">
                  <el-form-item label="显示分享数">
                    <el-switch v-model="articleFormData.showShareCount" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8" v-if="articleFormData.enableShare === 1">
                  <el-form-item label="初始分享数">
                    <el-input-number
                      v-model="articleFormData.initialShareCount"
                      :min="0"
                      :max="999999"
                      placeholder="运营设置的假数据"
                      class="!w-full"
                    />
                  </el-form-item>
                </el-col>

                <!-- 下载功能 -->
                <el-col :span="8">
                  <el-form-item label="支持下载">
                    <el-switch v-model="articleFormData.enableDownload" :active-value="1" :inactive-value="0" />
                    <span class="ml-10px text-gray-500">适用于文章类型</span>
                  </el-form-item>
                </el-col>

                <!-- 报名功能 -->
                <el-col :span="8">
                  <el-form-item label="支持报名">
                    <el-switch v-model="articleFormData.enableRegister" :active-value="1" :inactive-value="0" />
                    <span class="ml-10px text-gray-500">适用于课程类型</span>
                  </el-form-item>
                </el-col>

                <!-- 移动端发布 -->
                <el-col :span="8">
                  <el-form-item label="移动端发布">
                    <el-switch v-model="articleFormData.enableMobilePublish" :active-value="1" :inactive-value="0" />
                    <span class="ml-10px text-gray-500">允许在移动端发布</span>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 关联商品设置 -->
              <el-divider content-position="left">关联商品</el-divider>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="关联商品" prop="productIds">
                    <div class="product-selector">
                      <!-- 商品选择按钮 -->
                      <el-button type="primary" plain @click="showProductSelector">
                        <Icon icon="ep:plus" class="mr-5px" />
                        选择商品
                      </el-button>

                      <!-- 已选商品展示 -->
                      <div v-if="selectedProducts.length > 0" class="selected-products">
                        <div class="selected-products-header">
                          <span class="selected-label">已选商品 ({{ selectedProducts.length }})</span>
                          <el-button type="text" size="small" @click="clearAllProducts">清空</el-button>
                        </div>
                        <div class="product-list">
                          <div
                            v-for="product in selectedProducts"
                            :key="product.id"
                            class="product-item"
                          >
                            <el-image
                              :src="product.picUrl"
                              :alt="product.name"
                              class="product-image"
                              fit="cover"
                            />
                            <div class="product-info">
                              <div class="product-name" :title="product.name">{{ product.name }}</div>
                              <div class="product-price">¥{{ (product.price / 100).toFixed(2) }}</div>
                            </div>
                            <el-button
                              type="text"
                              size="small"
                              class="remove-btn"
                              @click="removeProduct(product.id)"
                            >
                              <Icon icon="ep:close" />
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
        <!-- 第一步：基本信息 -->
        <div v-if="articleFormStep === 1 && !isEditMode">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="所属板块" prop="sectionId">
                <el-select
                  v-model="articleFormData.sectionId"
                  placeholder="请选择板块"
                  class="!w-100%"
                  disabled
                >
                  <el-option
                    v-for="section in sections"
                    :key="section.id"
                    :label="section.name"
                    :value="section.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="所属分类" prop="categoryId">
                <el-select
                  v-model="articleFormData.categoryId"
                  placeholder="请选择分类"
                  class="!w-100%"
                  @change="handleFormCategoryChange"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="文章标题" prop="title">
                <el-input
                  v-model="articleFormData.title"
                  placeholder="请输入文章标题"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="副标题" prop="subtitle">
                <el-input
                  v-model="articleFormData.subtitle"
                  placeholder="请输入副标题(可选)"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="封面图片" prop="coverImages">
                <UploadImgs v-model="articleFormData.coverImages" :limit="9" />
                <div class="form-item-tip">最多上传9张图片</div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="关联标签" prop="tagIds">
                <div class="tag-selector">
                  <!-- 标签平铺展示区域 -->
                  <div class="existing-tags">
                    <el-tag
                      v-for="tag in tags"
                      :key="tag.id"
                      :type="(articleFormData.tagIds || []).includes(tag.id) ? 'primary' : 'info'"
                      class="tag-item"
                      @click="toggleTag(tag.id)"
                      :closable="false"
                    >
                      {{ tag.name }}
                    </el-tag>

                    <!-- 添加新标签按钮 -->
                    <el-button
                      type="primary"
                      plain
                      size="small"
                      @click="showCreateTagDialog"
                      class="add-tag-btn"
                    >
                      <Icon icon="ep:plus" class="mr-5px" />
                      新建标签
                    </el-button>
                  </div>

                  <!-- 已选标签展示 -->
                  <div v-if="(articleFormData.tagIds || []).length > 0" class="selected-tags">
                    <span class="selected-label">已选标签：</span>
                    <el-tag
                      v-for="tagId in (articleFormData.tagIds || [])"
                      :key="tagId"
                      type="primary"
                      class="selected-tag"
                      @close="removeTag(tagId)"
                      closable
                    >
                      {{ getTagName(tagId) }}
                    </el-tag>
                  </div>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="热门标记" prop="isHot">
                <el-switch v-model="articleFormData.isHot" :active-value="1" :inactive-value="0" />
                <span class="ml-10px text-gray-500">标记为热门文章</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="官方标记" prop="isOfficial">
                <el-switch v-model="articleFormData.isOfficial" :active-value="1" :inactive-value="0" />
                <span class="ml-10px text-gray-500">标记为官方文章</span>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 功能设置区域 -->
          <el-divider content-position="left">功能设置</el-divider>

          <el-row :gutter="20">
            <!-- 点赞功能 -->
            <el-col :span="8">
              <el-form-item label="支持点赞">
                <el-switch v-model="articleFormData.enableLike" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="articleFormData.enableLike === 1">
              <el-form-item label="显示点赞数">
                <el-switch v-model="articleFormData.showLikeCount" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="articleFormData.enableLike === 1">
              <el-form-item label="初始点赞数">
                <el-input-number
                  v-model="articleFormData.initialLikeCount"
                  :min="0"
                  :max="999999"
                  placeholder="运营设置的假数据"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>

            <!-- 收藏功能 -->
            <el-col :span="8">
              <el-form-item label="支持收藏">
                <el-switch v-model="articleFormData.enableCollect" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="articleFormData.enableCollect === 1">
              <el-form-item label="显示收藏数">
                <el-switch v-model="articleFormData.showCollectCount" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="articleFormData.enableCollect === 1">
              <el-form-item label="初始收藏数">
                <el-input-number
                  v-model="articleFormData.initialCollectCount"
                  :min="0"
                  :max="999999"
                  placeholder="运营设置的假数据"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>

            <!-- 分享功能 -->
            <el-col :span="8">
              <el-form-item label="支持分享">
                <el-switch v-model="articleFormData.enableShare" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="articleFormData.enableShare === 1">
              <el-form-item label="显示分享数">
                <el-switch v-model="articleFormData.showShareCount" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="articleFormData.enableShare === 1">
              <el-form-item label="初始分享数">
                <el-input-number
                  v-model="articleFormData.initialShareCount"
                  :min="0"
                  :max="999999"
                  placeholder="运营设置的假数据"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>

            <!-- 下载功能 -->
            <el-col :span="8">
              <el-form-item label="支持下载">
                <el-switch v-model="articleFormData.enableDownload" :active-value="1" :inactive-value="0" />
                <span class="ml-10px text-gray-500">适用于文章类型</span>
              </el-form-item>
            </el-col>

            <!-- 报名功能 -->
            <el-col :span="8">
              <el-form-item label="支持报名">
                <el-switch v-model="articleFormData.enableRegister" :active-value="1" :inactive-value="0" />
                <span class="ml-10px text-gray-500">适用于课程类型</span>
              </el-form-item>
            </el-col>

            <!-- 移动端发布 -->
            <el-col :span="8">
              <el-form-item label="移动端发布">
                <el-switch v-model="articleFormData.enableMobilePublish" :active-value="1" :inactive-value="0" />
                <span class="ml-10px text-gray-500">允许在移动端发布</span>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 关联商品设置 -->
          <el-divider content-position="left">关联商品</el-divider>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="关联商品" prop="productIds">
                <div class="product-selector">
                  <!-- 商品选择按钮 -->
                  <el-button type="primary" plain @click="showProductSelector">
                    <Icon icon="ep:plus" class="mr-5px" />
                    选择商品
                  </el-button>

                  <!-- 已选商品展示 -->
                  <div v-if="selectedProducts.length > 0" class="selected-products">
                    <div class="selected-products-header">
                      <span class="selected-label">已选商品 ({{ selectedProducts.length }})</span>
                      <el-button type="text" size="small" @click="clearAllProducts">清空</el-button>
                    </div>
                    <div class="product-list">
                      <div
                        v-for="product in selectedProducts"
                        :key="product.id"
                        class="product-item"
                      >
                        <el-image
                          :src="product.picUrl"
                          :alt="product.name"
                          class="product-image"
                          fit="cover"
                        />
                        <div class="product-info">
                          <div class="product-name" :title="product.name">{{ product.name }}</div>
                          <div class="product-price">¥{{ (product.price / 100).toFixed(2) }}</div>
                        </div>
                        <el-button
                          type="text"
                          size="small"
                          class="remove-btn"
                          @click="removeProduct(product.id)"
                        >
                          <Icon icon="ep:close" />
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 第二步：内容编辑 -->
        <div v-if="articleFormStep === 2">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="内容类型" prop="contentType">
                <el-radio-group v-model="articleFormData.contentType">
                  <el-radio value="richtext">富文本</el-radio>
                  <el-radio value="markdown">源码</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="文章内容" prop="content">
                <!-- 富文本编辑器 -->
                <Editor
                  v-if="articleFormData.contentType === 'richtext'"
                  v-model="articleFormData.content"
                  :height="500"
                />
                <!-- Markdown编辑器 -->
                <el-input
                  v-else
                  v-model="articleFormData.content"
                  type="textarea"
                  :rows="20"
                  placeholder="请输入Markdown格式内容"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseArticleForm">取 消</el-button>

          <!-- 步骤导航按钮 -->
          <el-button v-if="articleFormStep === 2 && !isEditMode" @click="handlePrevStep">
            上一步
          </el-button>

          <el-button
            type="primary"
            @click="submitArticleForm"
            :loading="formLoading"
          >
            {{ isEditMode ? '保 存' : (articleFormStep === 1 ? '下一步' : '完 成') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增板块表单弹窗 -->
    <el-dialog
      v-model="sectionFormVisible"
      :title="sectionFormTitle"
      width="700px"
      append-to-body
    >
      <el-form
        ref="sectionFormRef"
        :model="sectionFormData"
        :rules="sectionFormRules"
        label-width="120px"
      >
        <el-form-item label="板块名称" prop="name">
          <el-input v-model="sectionFormData.name" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="板块类型" prop="type">
          <el-select
            v-model="sectionFormData.type"
            placeholder="请选择板块类型"
            @change="handleSectionTypeChange"
            class="!w-100%"
          >
            <el-option label="文章类" value="article" />
            <el-option label="动态类" value="dynamic" />
            <el-option label="课程类" value="course" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="排版样式" prop="layoutStyle">
          <el-select v-model="sectionFormData.layoutStyle" placeholder="请选择排版样式" class="!w-100%">
            <el-option label="文章样式" value="article_style" />
            <el-option label="动态样式" value="dynamic_style" />
            <el-option label="课程样式" value="course_style" />
            <el-option label="自定义样式" value="custom_style" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面展示" prop="coverDisplayType">
          <el-radio-group v-model="sectionFormData.coverDisplayType">
            <el-radio value="single">单图</el-radio>
            <el-radio value="grid">栅格</el-radio>
            <el-radio value="carousel">轮播</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="功能按钮">
          <el-checkbox-group v-model="configButtons">
            <el-checkbox value="download">下载按钮</el-checkbox>
            <el-checkbox value="share">分享按钮</el-checkbox>
            <el-checkbox value="like">点赞按钮</el-checkbox>
            <el-checkbox value="collect">收藏按钮</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="用户内容生产">
          <el-switch v-model="ugcEnabled" />
          <span class="ml-10px text-gray-500">开启后允许用户在此板块发布内容</span>
        </el-form-item>
        <el-form-item v-if="ugcEnabled" label="自动审核">
          <el-switch v-model="autoAuditEnabled" />
          <span class="ml-10px text-gray-500">开启后系统会在设置时间后自动审核通过</span>
        </el-form-item>
        <el-form-item
          v-if="ugcEnabled && autoAuditEnabled"
          label="自动审核时间"
          prop="autoAuditDelayMinutes"
        >
          <div class="flex items-center gap-10px">
            <el-input-number
              v-model="autoAuditDelayMinutes"
              :min="1"
              :max="1440"
              controls-position="right"
            />
            <span class="text-gray-500">分钟后自动通过</span>
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="sectionFormData.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="sectionFormData.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseSectionForm">取 消</el-button>
        <el-button type="primary" @click="submitSectionForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 新增分类表单弹窗 -->
    <el-dialog
      v-model="categoryFormVisible"
      :title="categoryFormTitle"
      width="600px"
      append-to-body
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryFormData"
        :rules="categoryFormRules"
        label-width="100px"
      >
        <el-form-item label="所属板块" prop="sectionId">
          <el-select
            v-model="categoryFormData.sectionId"
            placeholder="请选择板块"
            class="!w-100%"
            disabled
          >
            <el-option
              v-for="section in sections"
              :key="section.id"
              :label="section.name"
              :value="section.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryFormData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <UploadImg v-model="categoryFormData.icon" :limit="1" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryFormData.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryFormData.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseCategoryForm">取 消</el-button>
        <el-button type="primary" @click="submitCategoryForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 新增标签弹窗 -->
    <el-dialog
      v-model="tagFormVisible"
      title="新建标签"
      width="500px"
      append-to-body
    >
      <el-form
        ref="tagFormRef"
        :model="tagFormData"
        :rules="tagFormRules"
        label-width="80px"
      >
        <el-form-item label="标签名称" prop="name">
          <el-input
            v-model="tagFormData.name"
            placeholder="请输入标签名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="tagFormData.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="tagFormData.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseTagForm">取 消</el-button>
        <el-button type="primary" @click="submitTagForm" :loading="formLoading">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog title="审核文章" v-model="auditDialogVisible" width="500px" append-to-body>
      <el-form ref="auditFormRef" :model="auditFormData" :rules="auditFormRules" label-width="100px">
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditFormData.auditStatus">
            <el-radio value="approved">通过</el-radio>
            <el-radio value="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input
            v-model="auditFormData.auditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 商品选择对话框 -->
    <el-dialog
      v-model="productSelectorVisible"
      title="选择关联商品"
      width="80%"
      append-to-body
      :close-on-click-modal="false"
    >
      <div class="product-selector-dialog">
        <!-- 搜索栏 -->
        <el-form :model="productSearchParams" :inline="true" class="mb-15px">
          <el-form-item label="商品名称">
            <el-input
              v-model="productSearchParams.name"
              placeholder="请输入商品名称"
              clearable
              @keyup.enter="searchProducts"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="searchProducts">
              <Icon icon="ep:search" class="mr-5px" /> 搜索
            </el-button>
            <el-button @click="resetProductSearch">
              <Icon icon="ep:refresh" class="mr-5px" /> 重置
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 商品列表 -->
        <el-table
          v-loading="productLoading"
          :data="productList"
          @selection-change="handleProductSelectionChange"
          max-height="400px"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="商品图片" width="100">
            <template #default="scope">
              <el-image
                :src="scope.row.picUrl"
                :alt="scope.row.name"
                style="width: 60px; height: 60px;"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column label="商品名称" prop="name" min-width="200" show-overflow-tooltip />
          <el-table-column label="价格" prop="price" width="100">
            <template #default="scope">
              ¥{{ (scope.row.price / 100).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="库存" prop="stock" width="80" />
          <el-table-column label="销量" prop="salesCount" width="80" />
          <el-table-column label="状态" prop="status" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 0" type="success">上架</el-tag>
              <el-tag v-else type="info">下架</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <Pagination
          :total="productTotal"
          v-model:page="productPagination.pageNo"
          v-model:limit="productPagination.pageSize"
          @pagination="loadProductList"
          class="mt-15px"
        />
      </div>
      <template #footer>
        <el-button @click="productSelectorVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmProductSelection">
          确定选择 ({{ tempSelectedProducts.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue'
import { dateFormatter } from '@/utils/formatTime'
import * as CmsArticleApi from '@/api/mall/promotion/cmsArticle'
import * as CmsSectionApi from '@/api/mall/promotion/cmsSection'
import * as CmsCategoryApi from '@/api/mall/promotion/cmsCategory'
import * as CmsTagApi from '@/api/mall/promotion/cmsTag'
import * as ProductApi from '@/api/mall/product/spu'
import { Editor } from '@/components/Editor'
import { UploadImgs, UploadImg } from '@/components/UploadFile'
import CmsSectionTabs from './CmsSectionTabs.vue'
import CmsCategoryBar from './CmsCategoryBar.vue'

/** CMS文章管理主页面 */
defineOptions({ name: 'CmsArticleManagement' })

const message = useMessage() // 消息弹窗
const { push } = useRouter() // 路由

// ========== 状态管理 ==========
const loading = ref(false)
const formLoading = ref(false)

// 板块相关
const sections = ref([])
const activeSectionId = ref(null)

// 分类相关
const categories = ref([])
const selectedCategoryIds = ref([])

// 标签相关
const tags = ref([])
const selectedTagIds = ref([])

// 文章相关
const articles = ref([])
const total = ref(0)

// 搜索参数
const searchParams = reactive({
  title: undefined
})

// 分页参数
const pagination = reactive({
  pageNo: 1,
  pageSize: 10
})

// ========== 表单相关 ==========
// 文章表单
const articleFormVisible = ref(false)
const articleFormTitle = ref('新增文章')
const articleFormRef = ref()
const articleFormStep = ref(1) // 当前步骤：1-基本信息，2-内容编辑
const isEditMode = ref(false) // 是否为编辑模式
const basicInfoCollapsed = ref(true) // 基本信息折叠状态，true为折叠
const articleFormData = ref({
  id: undefined,
  sectionId: undefined,
  categoryId: undefined,
  title: '',
  subtitle: '',
  coverImages: [],
  content: '',
  contentType: 'richtext',
  tagIds: [],
  isHot: 0,
  isOfficial: 0,
  // 功能控制字段
  enableLike: 0,
  initialLikeCount: 0,
  showLikeCount: 1,
  enableCollect: 0,
  initialCollectCount: 0,
  showCollectCount: 1,
  enableShare: 1,
  initialShareCount: 0,
  showShareCount: 1,
  enableDownload: 0,
  enableRegister: 0,
  enableMobilePublish: 0,
  productIds: []
})
// 基本信息步骤的验证规则
const basicInfoRules = reactive({
  sectionId: [{ required: true, message: '所属板块不能为空', trigger: 'change' }],
  categoryId: [{ required: true, message: '所属分类不能为空', trigger: 'change' }],
  title: [{ required: true, message: '文章标题不能为空', trigger: 'blur' }],
  subtitle: [{ required: false, trigger: 'blur' }],
  tagIds: [{ required: false, trigger: 'change' }]
})

// 内容编辑步骤的验证规则
const contentEditRules = reactive({
  content: [{ required: true, message: '文章内容不能为空', trigger: 'blur' }],
  contentType: [{ required: true, message: '内容类型不能为空', trigger: 'change' }]
})

// 根据当前步骤获取对应的验证规则
const getCurrentStepRules = () => {
  if (isEditMode.value) {
    // 编辑模式：需要合并基本信息和内容的验证规则
    return {
      ...basicInfoRules,
      ...contentEditRules
    }
  } else {
    // 新增模式：根据步骤返回对应的验证规则
    return articleFormStep.value === 1 ? basicInfoRules : contentEditRules
  }
}

// 板块表单
const sectionFormVisible = ref(false)
const sectionFormTitle = ref('新增板块')
const sectionFormRef = ref()
const sectionFormData = ref({
  id: undefined,
  name: '',
  type: '',
  layoutStyle: '',
  coverDisplayType: 'single',
  config: {},
  requireAudit: false,
  autoAuditEnabled: false,
  autoAuditDelayMinutes: 0,
  sort: 0,
  status: 0
})
const sectionFormRules = reactive({
  name: [{ required: true, message: '板块名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '板块类型不能为空', trigger: 'change' }],
  layoutStyle: [{ required: true, message: '排版样式不能为空', trigger: 'change' }],
  coverDisplayType: [{ required: true, message: '封面展示类型不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }],
  autoAuditDelayMinutes: [
    {
      validator: (_rule: any, value: number, callback: (error?: Error) => void) => {
        if (ugcEnabled.value && autoAuditEnabled.value) {
          if (!value || value <= 0) {
            callback(new Error('请输入大于 0 的分钟数'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
})

// 配置按钮和UGC开关
const configButtons = ref<string[]>([])
const ugcEnabled = ref(false)
const autoAuditEnabled = ref(false)
const autoAuditDelayMinutes = ref(30)

watch(ugcEnabled, (value) => {
  if (!value) {
    autoAuditEnabled.value = false
    autoAuditDelayMinutes.value = 0
    sectionFormData.value.requireAudit = false
  } else {
    sectionFormData.value.requireAudit = true
    if (autoAuditEnabled.value && autoAuditDelayMinutes.value <= 0) {
      autoAuditDelayMinutes.value = 30
    }
  }
})

watch(autoAuditEnabled, (value) => {
  sectionFormData.value.autoAuditEnabled = value
  if (value) {
    sectionFormData.value.requireAudit = true
    if (autoAuditDelayMinutes.value <= 0) {
      autoAuditDelayMinutes.value = 30
    }
  } else {
    autoAuditDelayMinutes.value = 0
  }
})

watch(autoAuditDelayMinutes, (value) => {
  sectionFormData.value.autoAuditDelayMinutes = value || 0
})

// 分类表单
const categoryFormVisible = ref(false)
const categoryFormTitle = ref('新增分类')
const categoryFormRef = ref()
const categoryFormData = ref({
  id: undefined,
  sectionId: undefined,
  name: '',
  icon: '',
  sort: 0,
  status: 0
})
const categoryFormRules = reactive({
  sectionId: [{ required: true, message: '所属板块不能为空', trigger: 'change' }],
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'blur' }]
})

// 标签表单
const tagFormVisible = ref(false)
const tagFormTitle = ref('新建标签')
const tagFormRef = ref()
const tagFormData = ref({
  name: '',
  sort: 0,
  status: 0
})
const tagFormRules = reactive({
  name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})

// 审核表单
const auditDialogVisible = ref(false)
const auditFormRef = ref()
const auditFormData = ref({
  id: undefined,
  auditStatus: 'approved',
  auditRemark: ''
})
const auditFormRules = reactive({
  auditStatus: [{ required: true, message: '审核结果不能为空', trigger: 'change' }]
})

// 商品选择相关
const productSelectorVisible = ref(false)
const productLoading = ref(false)
const productList = ref([])
const productTotal = ref(0)
const tempSelectedProducts = ref([])
const selectedProducts = ref([])

const productSearchParams = reactive({
  name: ''
})

const productPagination = reactive({
  pageNo: 1,
  pageSize: 10
})

// ========== API调用 ==========
/** 获取所有板块 */
const getSections = async () => {
  try {
    const data = await CmsSectionApi.getCmsSectionList()
    sections.value = data || []
    // 不再默认选中第一个板块，让用户主动选择
  } catch (error) {
    message.error('获取板块列表失败')
  }
}

/** 加载板块数据（分类、标签、文章） */
const loadSectionData = async (sectionId) => {
  loading.value = true
  try {
    // 获取分类
    const categoryData = await CmsCategoryApi.getCmsCategoryList()
    categories.value = categoryData.filter(category => category.sectionId === sectionId)

    // 重置标签数据
    tags.value = []

    // 获取文章
    await getArticles()
  } catch (error) {
    message.error('加载板块数据失败')
  } finally {
    loading.value = false
  }
}

/** 根据分类ID加载标签 */
const loadTagsByCategory = async (categoryId) => {
  try {
    if (categoryId) {
      const tagData = await CmsTagApi.getCmsTagListByCategoryId(categoryId)
      tags.value = tagData
    } else {
      const tagData = await CmsTagApi.getCmsTagList()
      tags.value = tagData
    }
  } catch (error) {
    message.error('获取标签列表失败')
  }
}

/** 获取文章列表 */
const getArticles = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      title: searchParams.title,
      sectionId: activeSectionId.value,
      categoryId: selectedCategoryIds.value.length > 0 ? selectedCategoryIds.value[0] : undefined,
      tagIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined
    }

    const data = await CmsArticleApi.getCmsArticlePage(params)
    articles.value = data.list
    total.value = data.total
  } catch (error) {
    message.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

/** 根据分类ID获取分类名称 */
const getCategoryName = (categoryId) => {
  const category = categories.value.find(item => item.id === categoryId)
  return category ? category.name : ''
}

/** 根据标签ID获取标签名称 */
const getTagName = (tagId) => {
  const tag = tags.value.find(item => item.id === tagId)
  return tag ? tag.name : ''
}

/** 根据板块ID获取板块名称 */
const getSectionName = (sectionId) => {
  const section = sections.value.find(item => item.id === sectionId)
  return section ? section.name : ''
}

/** 切换标签选择 */
const toggleTag = (tagId) => {
  const currentTagIds = articleFormData.value.tagIds || []
  const selectedIds = [...currentTagIds]
  const index = selectedIds.indexOf(tagId)

  if (index > -1) {
    // 如果已选中，则移除
    selectedIds.splice(index, 1)
  } else {
    // 如果未选中，则添加
    selectedIds.push(tagId)
  }

  articleFormData.value.tagIds = selectedIds
}

/** 根据板块类型设置默认功能 */
const getDefaultFeaturesByType = (sectionType) => {
  // 根据类型设置默认值
  switch (sectionType) {
    case 'dynamic':
      // 动态类型: 点赞、分享、移动端发布
      return {
        enableLike: 1,
        enableShare: 1,
        enableMobilePublish: 1,
        enableCollect: 0,
        enableDownload: 0,
        enableRegister: 0,
        showLikeCount: 1,
        showCollectCount: 1,
        showShareCount: 1,
        initialLikeCount: 0,
        initialCollectCount: 0,
        initialShareCount: 0
      }
    case 'article':
      // 文章类型: 下载、分享、收藏、移动端发布
      return {
        enableDownload: 1,
        enableShare: 1,
        enableCollect: 1,
        enableMobilePublish: 1,
        enableLike: 0,
        enableRegister: 0,
        showLikeCount: 1,
        showCollectCount: 1,
        showShareCount: 1,
        initialLikeCount: 0,
        initialCollectCount: 0,
        initialShareCount: 0
      }
    case 'course':
      // 课程类型: 报名、分享、收藏
      return {
        enableRegister: 1,
        enableShare: 1,
        enableCollect: 1,
        enableLike: 0,
        enableDownload: 0,
        enableMobilePublish: 0,
        showLikeCount: 1,
        showCollectCount: 1,
        showShareCount: 1,
        initialLikeCount: 0,
        initialCollectCount: 0,
        initialShareCount: 0
      }
    default:
      // 自定义类型: 只开启分享
      return {
        enableShare: 1,
        enableLike: 0,
        enableCollect: 0,
        enableDownload: 0,
        enableRegister: 0,
        enableMobilePublish: 0,
        showLikeCount: 1,
        showCollectCount: 1,
        showShareCount: 1,
        initialLikeCount: 0,
        initialCollectCount: 0,
        initialShareCount: 0
      }
  }
}

/** 移除标签 */
const removeTag = (tagId) => {
  const currentTagIds = articleFormData.value.tagIds || []
  const index = currentTagIds.indexOf(tagId)
  if (index > -1) {
    currentTagIds.splice(index, 1)
    articleFormData.value.tagIds = currentTagIds
  }
}

/** 显示创建标签弹窗 */
const showCreateTagDialog = () => {
  tagFormTitle.value = '新建标签'
  tagFormData.value = {
    name: '',
    sort: 0,
    status: 0
  }
  tagFormVisible.value = true
  nextTick(() => {
    tagFormRef.value?.resetFields()
  })
}

// ========== 事件处理 ==========
/** 处理板块切换 */
const handleSectionChange = async (sectionId) => {
  activeSectionId.value = sectionId
  // 重置筛选条件
  selectedCategoryIds.value = []
  selectedTagIds.value = []
  pagination.pageNo = 1
  // 加载新板块的数据（会在loadSectionData中正确处理标签）
  await loadSectionData(sectionId)
}

/** 处理新增板块 */
const handleAddSection = () => {
  sectionFormTitle.value = '新增板块'
  sectionFormData.value = {
    id: undefined,
    name: '',
    type: '',
    layoutStyle: '',
    coverDisplayType: 'single',
    config: {},
    requireAudit: false,
    autoAuditEnabled: false,
    autoAuditDelayMinutes: 0,
    sort: 0,
    status: 0
  }
  configButtons.value = []
  ugcEnabled.value = false
  autoAuditEnabled.value = false
  autoAuditDelayMinutes.value = 0
  sectionFormData.value.requireAudit = false
  sectionFormData.value.autoAuditEnabled = false
  sectionFormData.value.autoAuditDelayMinutes = 0
  sectionFormVisible.value = true
  nextTick(() => {
    sectionFormRef.value?.resetFields()
  })
}

/** 处理板块类型变化 */
const handleSectionTypeChange = (type) => {
  // 根据板块类型设置默认配置
  const DEFAULT_TYPE_CONFIG = {
    article: {
      layoutStyle: 'article_style',
      buttons: { download: true, share: true, like: true, collect: true },
      ugc_enabled: false,
      autoAuditEnabled: false,
      autoAuditDelayMinutes: 30
    },
    dynamic: {
      layoutStyle: 'dynamic_style',
      buttons: { download: false, share: true, like: true, collect: false },
      ugc_enabled: true,
      autoAuditEnabled: true,
      autoAuditDelayMinutes: 30
    },
    course: {
      layoutStyle: 'course_style',
      buttons: { download: false, share: true, like: false, collect: true },
      ugc_enabled: false,
      autoAuditEnabled: false,
      autoAuditDelayMinutes: 30
    },
    custom: {
      layoutStyle: 'custom_style',
      buttons: { download: false, share: false, like: false, collect: false },
      ugc_enabled: false,
      autoAuditEnabled: false,
      autoAuditDelayMinutes: 30
    }
  }

  const config = DEFAULT_TYPE_CONFIG[type]
  if (config) {
    sectionFormData.value.layoutStyle = config.layoutStyle
    // 更新按钮配置
    configButtons.value = Object.keys(config.buttons).filter(key => config.buttons[key])
    ugcEnabled.value = config.ugc_enabled
    autoAuditEnabled.value = config.autoAuditEnabled ?? false
    autoAuditDelayMinutes.value =
      config.autoAuditDelayMinutes ?? (autoAuditEnabled.value ? 30 : 0)
    sectionFormData.value.requireAudit = ugcEnabled.value ? true : false
    sectionFormData.value.autoAuditEnabled = autoAuditEnabled.value
    sectionFormData.value.autoAuditDelayMinutes = autoAuditDelayMinutes.value
  }
}

/** 处理表单分类变化 - 级联加载标签 */
const handleFormCategoryChange = async (categoryId) => {
  if (!categoryId) {
    // 如果清空分类，也清空已选标签
    articleFormData.value.tagIds = []
    tags.value = []
    return
  }

  try {
    // 加载该分类下的标签
    const categoryTags = await CmsTagApi.getCmsTagListByCategoryId(categoryId)

    // 更新标签数据
    tags.value = categoryTags

    // 清空已选标签，因为新分类下的标签可能不同
    articleFormData.value.tagIds = []
  } catch (error) {
    console.error('加载分类标签失败:', error)
    tags.value = []
    articleFormData.value.tagIds = []
  }
}

/** 处理分类变化 */
const handleCategoryChange = async (categoryIds) => {
  selectedCategoryIds.value = categoryIds
  // 清空标签选择，实现级联关系
  selectedTagIds.value = []

  // 根据选中的分类加载对应的标签
  if (categoryIds.length > 0) {
    await loadTagsByCategory(categoryIds[0])
  } else {
    // 没有选中分类时，清空标签数据
    tags.value = []
  }

  pagination.pageNo = 1
  getArticles()
}

/** 处理标签变化 */
const handleTagChange = (tagIds) => {
  selectedTagIds.value = tagIds
  pagination.pageNo = 1
  getArticles()
}

/** 处理新增分类 */
const handleAddCategory = () => {
  if (!activeSectionId.value) {
    message.warning('请先选择一个板块')
    return
  }

  categoryFormTitle.value = '新增分类'
  const currentSection = sections.value.find(section => section.id === activeSectionId.value)

  categoryFormData.value = {
    id: undefined,
    sectionId: activeSectionId.value,
    name: '',
    icon: '',
    sort: 0,
    status: 0
  }

  categoryFormVisible.value = true
  nextTick(() => {
    // 重置表单但保持板块字段的值
    if (categoryFormRef.value) {
      categoryFormRef.value.resetFields()
      // 手动设置板块字段，确保显示正确
      categoryFormData.value.sectionId = activeSectionId.value
    }
  })
}

/** 处理删除分类 */
const handleDeleteCategory = async (categoryId) => {
  try {
    // 获取分类信息
    const category = categories.value.find(item => item.id === categoryId)
    const categoryName = category ? category.name : '该分类'

    // 先检查该分类下是否有文章
    const articleCountParams = {
      pageNo: 1,
      pageSize: 1, // 只需要知道是否有文章，不需要具体数据
      categoryId: categoryId
    }

    const articleCountData = await CmsArticleApi.getCmsArticlePage(articleCountParams)

    if (articleCountData.total > 0) {
      message.warning(`分类 "${categoryName}" 下有 ${articleCountData.total} 篇文章，请先删除或转移这些文章后再删除分类`)
      return
    }

    // 确认删除
    await message.confirm(`确认删除分类 "${categoryName}" 吗？此操作不可恢复！`)

    // 执行删除
    await CmsCategoryApi.deleteCmsCategory(categoryId)
    message.success('删除成功')

    // 重新加载当前板块的分类
    if (activeSectionId.value) {
      const categoryData = await CmsCategoryApi.getCmsCategoryList()
      categories.value = categoryData.filter(category => category.sectionId === activeSectionId.value)

      // 从已选择的分类中移除被删除的分类
      const selectedIndex = selectedCategoryIds.value.indexOf(categoryId)
      if (selectedIndex > -1) {
        selectedCategoryIds.value.splice(selectedIndex, 1)
        // 重新查询文章
        await getArticles()
      }
    }
  } catch (error) {
    // 用户取消或操作失败
    if (error.message !== 'cancel') {
      message.error('删除分类失败')
    }
  }
}

/** 处理新增文章 */
const handleAddArticle = async () => {
  if (!activeSectionId.value) {
    message.warning('请先选择一个板块')
    return
  }

  articleFormTitle.value = '新增文章'
  articleFormStep.value = 1
  isEditMode.value = false
  basicInfoCollapsed.value = true

  // 获取当前板块信息以设置默认功能
  const currentSection = sections.value.find(section => section.id === activeSectionId.value)

  articleFormData.value = {
    id: undefined,
    sectionId: activeSectionId.value,
    categoryId: selectedCategoryIds.value.length > 0 ? selectedCategoryIds.value[0] : undefined,
    title: '',
    subtitle: '',
    coverImages: [],
    content: '',
    contentType: 'richtext',
    tagIds: selectedTagIds.value.length > 0 ? [...selectedTagIds.value] : [],
    isHot: 0,
    isOfficial: 0,
    // 功能控制字段 - 根据板块类型设置默认值
    ...getDefaultFeaturesByType(currentSection?.type || 'article')
  }

  // 确保标签数据可用
  if (tags.value.length === 0) {
    try {
      await loadTagsByCategory(selectedCategoryIds.value.length > 0 ? selectedCategoryIds.value[0] : null)
    } catch (error) {
      console.error('加载标签失败:', error)
    }
  }

  articleFormVisible.value = true
  nextTick(() => {
    articleFormRef.value?.resetFields()
  })
}

/** 处理编辑文章 */
const handleEditArticle = async (id) => {
  formLoading.value = true
  articleFormVisible.value = true
  articleFormTitle.value = '编辑文章'
  articleFormStep.value = 2 // 编辑时直接进入内容编辑步骤，基本信息折叠显示
  isEditMode.value = true
  basicInfoCollapsed.value = true // 编辑时默认折叠基本信息

  try {
    const data = await CmsArticleApi.getCmsArticle(id)
    articleFormData.value = data

    // 初始化关联商品
    selectedProducts.value = []
    if (data.productIds && data.productIds.length > 0) {
      // 根据商品ID列表获取商品详情
      try {
        const productPromises = data.productIds.map(productId =>
          ProductApi.getSpu(productId).catch(error => {
            console.error(`获取商品${productId}详情失败:`, error)
            return null
          })
        )
        const productResults = await Promise.all(productPromises)
        selectedProducts.value = productResults.filter(product => product !== null)
      } catch (error) {
        console.error('加载关联商品失败:', error)
      }
    }

    // 加载该文章对应分类的标签
    if (data.categoryId) {
      try {
        const categoryTags = await CmsTagApi.getCmsTagListByCategoryId(data.categoryId)
        tags.value = categoryTags
      } catch (error) {
        console.error('加载分类标签失败:', error)
        // 如果加载失败，至少确保现有的标签能正确显示
        if (data.tagIds && data.tagIds.length > 0) {
          // 可以根据现有标签ID获取标签详情
        }
      }
    }
  } catch (error) {
    message.error('获取文章详情失败')
    articleFormVisible.value = false
  } finally {
    formLoading.value = false
  }
}

/** 处理表单关闭 */
const handleCloseArticleForm = () => {
  articleFormVisible.value = false
  articleFormStep.value = 1
  isEditMode.value = false
  basicInfoCollapsed.value = true
  selectedProducts.value = []
  articleFormRef.value?.resetFields()
}

/** 处理步骤切换 - 下一步 */
const handleNextStep = async () => {
  if (!articleFormRef.value) return

  try {
    // 验证当前步骤的表单，使用 Promise 方式
    await new Promise((resolve, reject) => {
      articleFormRef.value.validate((valid) => {
        if (valid) {
          resolve(valid)
        } else {
          reject(new Error('表单验证失败'))
        }
      })
    })
    // 验证通过后进入下一步
    articleFormStep.value = 2
  } catch (error) {
    // 验证失败，不执行任何操作，让Element Plus显示错误信息
    console.error('表单验证失败:', error)
  }
}

/** 处理步骤切换 - 上一步 */
const handlePrevStep = () => {
  articleFormStep.value = 1
}

/** 处理基本信息折叠/展开 */
const toggleEditMode = () => {
  basicInfoCollapsed.value = !basicInfoCollapsed.value
}

const handleCloseSectionForm = () => {
  sectionFormVisible.value = false
  sectionFormRef.value?.resetFields()
}

const handleCloseCategoryForm = () => {
  categoryFormVisible.value = false
  categoryFormRef.value?.resetFields()
}

const handleCloseTagForm = () => {
  tagFormVisible.value = false
  tagFormRef.value?.resetFields()
}

/** 处理搜索 */
const handleSearch = () => {
  pagination.pageNo = 1
  getArticles()
}

/** 重置搜索 */
const resetSearch = () => {
  searchParams.title = undefined
  handleSearch()
}

/** 处理分页变化 */
const handlePageChange = () => {
  getArticles()
}

/** 提交文章表单 */
const submitArticleForm = async () => {
  if (!articleFormRef.value) return

  // 如果是第一步且不是编辑模式，则进入下一步
  if (articleFormStep.value === 1 && !isEditMode.value) {
    await handleNextStep()
    return
  }

  // 验证表单
  try {
    await new Promise((resolve, reject) => {
      articleFormRef.value.validate((valid) => {
        if (valid) {
          resolve(valid)
        } else {
          reject(new Error('表单验证失败'))
        }
      })
    })
  } catch (error) {
    // 验证失败，不执行任何操作，让Element Plus显示错误信息
    console.error('表单验证失败:', error)
    return
  }

  formLoading.value = true
  try {
    const data = { ...articleFormData.value }
    if (data.id) {
      await CmsArticleApi.updateCmsArticle(data)
      message.success('修改成功')
    } else {
      await CmsArticleApi.createCmsArticle(data)
      message.success('新增成功')
    }
    articleFormVisible.value = false
    await getArticles()
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

/** 提交板块表单 */
const submitSectionForm = async () => {
  if (!sectionFormRef.value) return
  const valid = await sectionFormRef.value.validate()
  if (!valid) return

  try {
    // 更新config
    const buttons = {
      download: configButtons.value.includes('download'),
      share: configButtons.value.includes('share'),
      like: configButtons.value.includes('like'),
      collect: configButtons.value.includes('collect')
    }
    sectionFormData.value.config = {
      buttons,
      ugc_enabled: ugcEnabled.value,
      requireAudit: ugcEnabled.value ? true : false,
      autoAuditEnabled: ugcEnabled.value ? autoAuditEnabled.value : false,
      autoAuditDelayMinutes:
        ugcEnabled.value && autoAuditEnabled.value ? autoAuditDelayMinutes.value : 0
    }
    sectionFormData.value.requireAudit = ugcEnabled.value ? true : false
    sectionFormData.value.autoAuditEnabled = ugcEnabled.value ? autoAuditEnabled.value : false
    sectionFormData.value.autoAuditDelayMinutes =
      ugcEnabled.value && autoAuditEnabled.value ? autoAuditDelayMinutes.value : 0

    const data = { ...sectionFormData.value }
    if (data.id) {
      await CmsSectionApi.updateCmsSection(data)
      message.success('修改成功')
    } else {
      await CmsSectionApi.createCmsSection(data)
      message.success('新增成功')
    }
    sectionFormVisible.value = false
    // 重新加载板块列表
    await getSections()
  } catch (error) {
    message.error('操作失败')
  }
}

/** 提交分类表单 */
const submitCategoryForm = async () => {
  if (!categoryFormRef.value) return
  const valid = await categoryFormRef.value.validate()
  if (!valid) return

  try {
    const data = { ...categoryFormData.value }
    if (data.id) {
      await CmsCategoryApi.updateCmsCategory(data)
      message.success('修改成功')
    } else {
      await CmsCategoryApi.createCmsCategory(data)
      message.success('新增成功')
    }
    categoryFormVisible.value = false
    // 重新加载当前板块的分类
    if (activeSectionId.value) {
      const categoryData = await CmsCategoryApi.getCmsCategoryList()
      categories.value = categoryData.filter(category => category.sectionId === activeSectionId.value)
    }
  } catch (error) {
    message.error('操作失败')
  }
}

/** 提交标签表单 */
const submitTagForm = async () => {
  if (!tagFormRef.value) return
  const valid = await tagFormRef.value.validate()
  if (!valid) return

  formLoading.value = true
  try {
    const data = { ...tagFormData.value }
    const newTag = await CmsTagApi.createCmsTag(data)

    message.success('标签创建成功')
    tagFormVisible.value = false

    // 重新加载标签列表
    const tagData = await CmsTagApi.getCmsTagList()
    tags.value = tagData

    // 自动选中新创建的标签
    if (newTag && newTag.id) {
      if (!articleFormData.value.tagIds) {
        articleFormData.value.tagIds = []
      }
      articleFormData.value.tagIds.push(newTag.id)
    }
  } catch (error) {
    message.error('创建标签失败')
  } finally {
    formLoading.value = false
  }
}

/** 处理审核 */
const handleAudit = (row) => {
  auditDialogVisible.value = true
  auditFormData.value = {
    id: row.id,
    auditStatus: 'approved',
    auditRemark: ''
  }
}

/** 提交审核 */
const submitAudit = async () => {
  if (!auditFormRef.value) return
  const valid = await auditFormRef.value.validate()
  if (!valid) return

  try {
    await CmsArticleApi.auditCmsArticle(auditFormData.value)
    message.success('审核成功')
    auditDialogVisible.value = false
    await getArticles()
  } catch (error) {
    message.error('审核失败')
  }
}

/** 处理发布 */
const handlePublish = async (id) => {
  try {
    await message.confirm('确认发布该文章吗?')
    await CmsArticleApi.publishCmsArticle(id)
    message.success('发布成功')
    await getArticles()
  } catch (error) {
    // 用户取消或操作失败
  }
}

/** 处理下架 */
const handleUnpublish = async (id) => {
  try {
    await message.confirm('确认下架该文章吗?')
    await CmsArticleApi.unpublishCmsArticle(id)
    message.success('下架成功')
    await getArticles()
  } catch (error) {
    // 用户取消或操作失败
  }
}

/** 处理删除 */
const handleDelete = async (id) => {
  try {
    await message.delConfirm()
    await CmsArticleApi.deleteCmsArticle(id)
    message.success('删除成功')
    await getArticles()
  } catch (error) {
    // 用户取消或操作失败
  }
}

// ========== 商品选择相关方法 ==========
/** 显示商品选择器 */
const showProductSelector = () => {
  productSelectorVisible.value = true
  loadProductList()
}

/** 加载商品列表 */
const loadProductList = async () => {
  productLoading.value = true
  try {
    const params = {
      pageNo: productPagination.pageNo,
      pageSize: productPagination.pageSize,
      name: productSearchParams.name,
      status: 0 // 只显示上架商品
    }
    const data = await ProductApi.getSpuPage(params)
    productList.value = data.list
    productTotal.value = data.total
  } catch (error) {
    message.error('获取商品列表失败')
  } finally {
    productLoading.value = false
  }
}

/** 搜索商品 */
const searchProducts = () => {
  productPagination.pageNo = 1
  loadProductList()
}

/** 重置商品搜索 */
const resetProductSearch = () => {
  productSearchParams.name = ''
  productPagination.pageNo = 1
  loadProductList()
}

/** 处理商品选择变化 */
const handleProductSelectionChange = (selection) => {
  tempSelectedProducts.value = selection
}

/** 确认商品选择 */
const confirmProductSelection = () => {
  // 合并已选商品和新增商品
  const existingIds = selectedProducts.value.map(p => p.id)
  const newProducts = tempSelectedProducts.value.filter(p => !existingIds.includes(p.id))

  selectedProducts.value = [...selectedProducts.value, ...newProducts]

  // 更新文章表单中的商品ID列表
  articleFormData.value.productIds = selectedProducts.value.map(p => p.id)

  productSelectorVisible.value = false
  tempSelectedProducts.value = []
}

/** 移除商品 */
const removeProduct = (productId) => {
  selectedProducts.value = selectedProducts.value.filter(p => p.id !== productId)
  articleFormData.value.productIds = selectedProducts.value.map(p => p.id)
}

/** 清空所有商品 */
const clearAllProducts = () => {
  selectedProducts.value = []
  articleFormData.value.productIds = []
}

// ========== 初始化 ==========
onMounted(async () => {
  await getSections()
})
</script>

<style scoped>
.cms-article-management {
  padding-bottom: 20px;
}

.section-content-wrap {
  padding: 0;
  background: var(--el-bg-color-page);
}

.content-area {
  padding: 0;
  background: var(--el-bg-color-page);
}

.article-list-container {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.form-item-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-top: none;
  border-radius: 0 0 4px 4px;
}

.select-section-prompt {
  color: var(--el-text-color-regular);
  text-align: center;
}

.prompt-icon {
  display: block;
  margin-bottom: 16px;
  font-size: 48px;
  color: var(--el-color-primary);
}

.select-section-prompt p {
  margin: 0;
  font-size: 16px;
}

/* 标签选择器样式 */
.tag-selector {
  padding: 12px;
  border-radius: 4px;
  //border: 1px solid var(--el-border-color);
  //background: var(--el-bg-color-page);
}

.existing-tags {
  display: flex;
  min-height: 32px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-item {
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.tag-item:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

.tag-item.el-tag--primary {
  color: white;
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.add-tag-btn {
  height: 24px;
  padding: 0 8px;
}

.selected-tags {
  display: flex;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.selected-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.selected-tag {
  margin: 0;
}

/* 步骤指示器样式 */
.step-indicator {
  padding: 0 20px;
  margin-bottom: 20px;
}

/* 编辑模式折叠面板样式 */
.edit-mode-basic-info {
  margin-bottom: 20px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
}

.custom-collapse-header {
  display: flex;
  padding: 12px 16px;
  font-weight: 500;
  cursor: pointer;
  background: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-light);
  transition: background-color 0.2s ease;
  align-items: center;
}

.edit-mode-basic-info:not(.expanded) .custom-collapse-header {
  border-bottom: none;
}

.custom-collapse-header:hover {
  background: var(--el-bg-color-page-lighter);
}

.collapse-title {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.basic-info-content {
  padding: 16px;
  background: var(--el-bg-color);
}

.basic-info-content p {
  margin: 8px 0;
  color: var(--el-text-color-regular);
}

.basic-info-content strong {
  margin-right: 8px;
  color: var(--el-text-color-primary);
}

/* 对话框底部按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 步骤内容区域过渡动画 */
.step-content {
  transition: all 0.3s ease;
}

/* 表单步骤标题 */
.step-title {
  padding-bottom: 10px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

/* 封面图片预览样式 */
.cover-images-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.cover-image-thumb {
  width: 60px;
  height: 60px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.cover-image-thumb:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgb(0 0 0 / 10%);
}

/* 商品选择器样式 */
.product-selector {
  padding: 12px;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
}

.selected-products {
  margin-top: 12px;
}

.selected-products-header {
  display: flex;
  padding-bottom: 8px;
  margin-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  justify-content: space-between;
  align-items: center;
}

.selected-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.product-item {
  position: relative;
  display: flex;
  padding: 8px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  align-items: center;
}

.product-image {
  width: 40px;
  height: 40px;
  margin-right: 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  margin-bottom: 4px;
  overflow: hidden;
  font-size: 13px;
  color: var(--el-text-color-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 12px;
  font-weight: 500;
  color: var(--el-color-danger);
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 2px;
  color: var(--el-text-color-regular);
}

.remove-btn:hover {
  color: var(--el-color-danger);
}

/* 商品选择对话框样式 */
.product-selector-dialog {
  max-height: 70vh;
}
</style>
