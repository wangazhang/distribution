// 统一导出CMS相关API
export * from './section'
export * from './category'
export * from './tag'
export * from './article'

// 重新导出默认的article API（兼容旧代码）
export { default as cmsArticle } from './article'