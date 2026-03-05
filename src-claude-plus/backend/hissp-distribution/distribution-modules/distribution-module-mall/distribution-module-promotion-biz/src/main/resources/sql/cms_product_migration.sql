-- CMS文章关联商品字段迁移脚本
-- 为 cms_article 表添加 product_ids 字段

-- 检查字段是否已存在，避免重复添加
SET @columnExists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'cms_article'
    AND COLUMN_NAME = 'product_ids'
    AND TABLE_SCHEMA = DATABASE()
);

-- 只有当字段不存在时才添加
SET @sql = IF(@columnExists = 0,
    'ALTER TABLE cms_article ADD COLUMN product_ids JSON COMMENT "关联商品ID列表(JSON数组)"',
    'SELECT "product_ids column already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建索引以提高查询性能
SET @indexExists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_NAME = 'cms_article'
    AND INDEX_NAME = 'idx_cms_article_product_ids'
    AND TABLE_SCHEMA = DATABASE()
);

SET @sql = IF(@indexExists = 0,
    'CREATE INDEX idx_cms_article_product_ids ON cms_article ((CAST(product_ids AS CHAR(255) ARRAY)))',
    'SELECT "Index already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;