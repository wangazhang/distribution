-- =============================================
-- 物料域统一迁移 - 数据库脚本
-- 执行顺序：1. DDL（表结构）2. DML（数据迁移）
-- =============================================

-- =============================================
-- 第一部分：DDL - 表结构创建和修改
-- =============================================

-- 1. 扩展物料定义表
ALTER TABLE material_definition ADD COLUMN IF NOT EXISTS (
    code VARCHAR(50) COMMENT '物料编码',
    spu_id BIGINT COMMENT '关联SPU ID', 
    image VARCHAR(500) COMMENT '物料图片',
    description TEXT COMMENT '物料描述',
    type INT DEFAULT 1 COMMENT '物料类型：1原料 2产品 3工具',
    support_outbound BOOLEAN DEFAULT TRUE COMMENT '是否支持出库',
    support_convert BOOLEAN DEFAULT TRUE COMMENT '是否支持转化'
);

ALTER TABLE material_definition
    ADD COLUMN IF NOT EXISTS convert_price INT COMMENT '转化单价（单位：分）';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_material_definition_spu ON material_definition(spu_id);
CREATE INDEX IF NOT EXISTS idx_material_definition_code ON material_definition(code);
CREATE INDEX IF NOT EXISTS idx_material_definition_type ON material_definition(type);

-- 2. 创建物料出库主表
CREATE TABLE IF NOT EXISTS material_outbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    outbound_no VARCHAR(50) NOT NULL COMMENT '出库单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    address_id BIGINT COMMENT '收货地址ID',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_mobile VARCHAR(20) COMMENT '收货人手机',
    receiver_province VARCHAR(50) COMMENT '省份',
    receiver_city VARCHAR(50) COMMENT '城市', 
    receiver_district VARCHAR(50) COMMENT '区县',
    receiver_detail_address VARCHAR(200) COMMENT '详细地址',
    status INT DEFAULT 0 COMMENT '状态：0待审核 1已审核 2已发货 3已完成 4已取消',
    logistics_company VARCHAR(100) COMMENT '物流公司',
    logistics_code VARCHAR(100) COMMENT '物流单号',
    approve_time DATETIME COMMENT '审核时间',
    approve_user_id BIGINT COMMENT '审核人ID',
    ship_time DATETIME COMMENT '发货时间',
    complete_time DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    updater VARCHAR(64),
    deleted BIT DEFAULT 0
) COMMENT '物料出库主表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_material_outbound_user ON material_outbound(user_id);
CREATE INDEX IF NOT EXISTS idx_material_outbound_no ON material_outbound(outbound_no);
CREATE INDEX IF NOT EXISTS idx_material_outbound_status ON material_outbound(status);
CREATE INDEX IF NOT EXISTS idx_material_outbound_create_time ON material_outbound(create_time);

-- 3. 创建物料出库明细表
CREATE TABLE IF NOT EXISTS material_outbound_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    outbound_id BIGINT NOT NULL COMMENT '出库单ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    quantity INT NOT NULL COMMENT '出库数量',
    material_name VARCHAR(100) COMMENT '物料名称（冗余）',
    material_code VARCHAR(50) COMMENT '物料编码（冗余）',
    base_unit VARCHAR(20) COMMENT '基础单位（冗余）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    updater VARCHAR(64),
    deleted BIT DEFAULT 0
) COMMENT '物料出库明细表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_material_outbound_item_outbound ON material_outbound_item(outbound_id);
CREATE INDEX IF NOT EXISTS idx_material_outbound_item_material ON material_outbound_item(material_id);

-- 4. 创建物料转化规则表
CREATE TABLE IF NOT EXISTS material_convert_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL COMMENT '转化规则名称',
    source_material_id BIGINT NOT NULL COMMENT '源物料ID',
    target_material_id BIGINT NOT NULL COMMENT '目标物料ID',
    convert_ratio DECIMAL(10,4) NOT NULL DEFAULT 1.0000 COMMENT '转化比例',
    convert_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '转化费用',
    status INT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    description VARCHAR(500) COMMENT '规则描述',
    attrs TEXT COMMENT '扩展属性',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    updater VARCHAR(64),
    deleted BIT DEFAULT 0
) COMMENT '物料转化规则表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_material_convert_rule_source ON material_convert_rule(source_material_id);
CREATE INDEX IF NOT EXISTS idx_material_convert_rule_target ON material_convert_rule(target_material_id);
CREATE INDEX IF NOT EXISTS idx_material_convert_rule_status ON material_convert_rule(status);

-- 5. 创建物料转化记录表
CREATE TABLE IF NOT EXISTS material_convert_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    rule_id BIGINT NOT NULL COMMENT '转化规则ID',
    source_material_id BIGINT NOT NULL COMMENT '源物料ID',
    target_material_id BIGINT NOT NULL COMMENT '目标物料ID',
    source_quantity INT NOT NULL COMMENT '源物料数量',
    target_quantity INT NOT NULL COMMENT '目标物料数量',
    convert_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '转化费用',
    status INT DEFAULT 1 COMMENT '转化状态：1成功 2失败',
    order_id BIGINT COMMENT '关联订单ID',
    reason VARCHAR(500) COMMENT '转化原因/备注',
    failure_reason VARCHAR(500) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator VARCHAR(64),
    updater VARCHAR(64),
    deleted BIT DEFAULT 0
) COMMENT '物料转化记录表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_material_convert_record_user ON material_convert_record(user_id);
CREATE INDEX IF NOT EXISTS idx_material_convert_record_rule ON material_convert_record(rule_id);
CREATE INDEX IF NOT EXISTS idx_material_convert_record_source ON material_convert_record(source_material_id);
CREATE INDEX IF NOT EXISTS idx_material_convert_record_target ON material_convert_record(target_material_id);
CREATE INDEX IF NOT EXISTS idx_material_convert_record_create_time ON material_convert_record(create_time);

-- =============================================
-- 第二部分：DML - 数据迁移
-- =============================================

-- 1. 迁移MB域的物料定义数据到新的物料定义表
-- 注意：这里需要根据实际的MB域表结构进行调整
INSERT IGNORE INTO material_definition (id, name, code, type, base_unit, status, support_outbound, support_convert, create_time, update_time, creator)
SELECT 
  id,
  name,
  COALESCE(code, CONCAT('MAT', LPAD(id, 6, '0'))) as code, -- 如果没有编码，生成一个
  CASE 
    WHEN type = 'RAW' THEN 1     -- 原料
    WHEN type = 'PRODUCT' THEN 2  -- 产品
    ELSE 3                        -- 工具
  END as type,
  COALESCE(base_unit, '个') as base_unit,
  CASE 
    WHEN status = 'ACTIVE' THEN 1 
    ELSE 0 
  END as status,
  TRUE as support_outbound,  -- 默认支持出库
  TRUE as support_convert,   -- 默认支持转化
  create_time,
  update_time,
  creator
FROM (
  -- 这里模拟从MB域的物料配置表获取数据
  -- 实际执行时需要替换为真实的表名和字段
  SELECT 
    1 as id, '胶原蛋白原料' as name, 'MAT001' as code, 'RAW' as type, '瓶' as base_unit, 'ACTIVE' as status, NOW() as create_time, NOW() as update_time, 'system' as creator
  UNION ALL
  SELECT 
    2 as id, '胶原蛋白产品' as name, 'MAT002' as code, 'PRODUCT' as type, '瓶' as base_unit, 'ACTIVE' as status, NOW() as create_time, NOW() as update_time, 'system' as creator
  UNION ALL
  SELECT 
    3 as id, 'ECM面膜' as name, 'MAT003' as code, 'PRODUCT' as type, '片' as base_unit, 'ACTIVE' as status, NOW() as create_time, NOW() as update_time, 'system' as creator
  -- 这里可以添加更多的物料定义数据
) as mb_materials;

-- 2. 迁移MB域的物料出库数据
INSERT IGNORE INTO material_outbound (
  id, outbound_no, user_id, address_id, receiver_name, receiver_mobile,
  receiver_province, receiver_city, receiver_district, receiver_detail_address,
  status, logistics_company, logistics_code, 
  approve_time, approve_user_id, ship_time, complete_time,
  cancel_time, cancel_reason, remark, create_time, update_time, creator, updater
)
SELECT 
  id, outbound_no, user_id, address_id, receiver_name, receiver_mobile,
  receiver_province, receiver_city, receiver_district, receiver_detail_address,
  status, logistics_company, logistics_code,
  approve_time, approve_user_id, ship_time, complete_time,
  cancel_time, cancel_reason, remark, create_time, update_time, creator, updater
FROM mb_material_outbound
WHERE deleted = 0;

-- 3. 迁移MB域的物料出库明细数据
INSERT IGNORE INTO material_outbound_item (
  id, outbound_id, material_id, quantity, material_name, material_code, base_unit, create_time, update_time
)
SELECT 
  moi.id, 
  moi.outbound_id, 
  moi.material_id, 
  moi.quantity,
  md.name as material_name,
  md.code as material_code,
  md.base_unit,
  moi.create_time, 
  moi.update_time
FROM mb_material_outbound_item moi
LEFT JOIN material_definition md ON moi.material_id = md.id
WHERE moi.deleted = 0;

-- 4. 创建初始的物料转化规则（基于现有MB域的转化配置）
INSERT IGNORE INTO material_convert_rule (rule_name, source_material_id, target_material_id, convert_ratio, convert_price, status, description, create_time, creator)
VALUES 
('蛋白原料转胶原产品', 1, 2, 1.0000, 50.00, 1, '蛋白原料转化为胶原产品，1:1比例转化，需要支付50元转化费', NOW(), 'system'),
('ECM面膜转化', 1, 3, 2.0000, 100.00, 1, '蛋白原料转化为ECM面膜，1:2比例转化，需要支付100元转化费', NOW(), 'system');

-- 5. 创建物料转化记录（基于MB域的胶原转化订单）
-- 这里需要根据实际的MB订单表结构进行调整
INSERT IGNORE INTO material_convert_record (
  user_id, rule_id, source_material_id, target_material_id, 
  source_quantity, target_quantity, convert_price, status, order_id, 
  reason, create_time, creator
)
SELECT 
  mo.agent_user_id as user_id,
  1 as rule_id, -- 假设使用规则ID为1（蛋白原料转胶原产品）
  mo.product_id as source_material_id,  -- MB订单中的产品ID作为源物料
  2 as target_material_id, -- 胶原产品ID
  mo.quantity as source_quantity,
  mo.quantity as target_quantity, -- 1:1转化
  mo.pay_price as convert_price,
  CASE 
    WHEN mo.status IN ('PAID', 'SUCCESS') THEN 1 -- 成功
    ELSE 2 -- 失败
  END as status,
  mo.id as order_id,
  CONCAT('MB域胶原转化订单迁移，原订单号：', mo.id) as reason,
  mo.create_time,
  'migration' as creator
FROM mb_order mo
WHERE mo.deleted = 0 
  AND mo.order_no LIKE 'MC%' -- 胶原转化订单前缀
  AND mo.create_time >= DATE_SUB(NOW(), INTERVAL 1 YEAR); -- 只迁移近一年的数据

-- =============================================
-- 第三部分：数据验证和清理
-- =============================================

-- 验证数据迁移结果
SELECT 
  '物料定义' as table_name, 
  COUNT(*) as total_count,
  COUNT(CASE WHEN support_outbound = 1 THEN 1 END) as support_outbound_count,
  COUNT(CASE WHEN support_convert = 1 THEN 1 END) as support_convert_count
FROM material_definition
WHERE deleted = 0

UNION ALL

SELECT 
  '物料出库主表' as table_name,
  COUNT(*) as total_count,
  COUNT(CASE WHEN status = 0 THEN 1 END) as pending_count,
  COUNT(CASE WHEN status = 3 THEN 1 END) as completed_count
FROM material_outbound
WHERE deleted = 0

UNION ALL

SELECT 
  '物料出库明细' as table_name,
  COUNT(*) as total_count,
  SUM(quantity) as total_quantity,
  COUNT(DISTINCT material_id) as unique_materials
FROM material_outbound_item
WHERE deleted = 0

UNION ALL

SELECT 
  '物料转化规则' as table_name,
  COUNT(*) as total_count,
  COUNT(CASE WHEN status = 1 THEN 1 END) as active_rules,
  AVG(convert_price) as avg_convert_price
FROM material_convert_rule
WHERE deleted = 0

UNION ALL

SELECT 
  '物料转化记录' as table_name,
  COUNT(*) as total_count,
  COUNT(CASE WHEN status = 1 THEN 1 END) as success_count,
  SUM(convert_price) as total_convert_fees
FROM material_convert_record
WHERE deleted = 0;

-- 清理临时数据或修正数据不一致问题
-- 更新物料定义表中缺失的编码
UPDATE material_definition 
SET code = CONCAT('MAT', LPAD(id, 6, '0'))
WHERE code IS NULL OR code = '';

-- 确保物料出库明细的冗余字段正确
UPDATE material_outbound_item moi
INNER JOIN material_definition md ON moi.material_id = md.id
SET 
  moi.material_name = md.name,
  moi.material_code = md.code,
  moi.base_unit = md.base_unit
WHERE moi.material_name IS NULL OR moi.material_name = '';

-- =============================================
-- 第四部分：权限和配置
-- =============================================

-- 创建物料域相关的权限菜单（这里只是示例，实际需要根据具体的权限系统调整）
-- INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
-- VALUES 
-- ('物料管理', 'material:definition:query', 1, 1, 0, '/material', 'material', '', 1),
-- ('物料定义', 'material:definition:query', 2, 1, (SELECT id FROM system_menu WHERE permission = 'material:definition:query' AND type = 1), '/material/definition', '', 'material/definition/index', 1),
-- ('物料出库', 'material:outbound:query', 2, 2, (SELECT id FROM system_menu WHERE permission = 'material:definition:query' AND type = 1), '/material/outbound', '', 'material/outbound/index', 1),
-- ('物料转化', 'material:convert:query', 2, 3, (SELECT id FROM system_menu WHERE permission = 'material:definition:query' AND type = 1), '/material/convert', '', 'material/convert/index', 1);

-- =============================================
-- 迁移完成标记
-- =============================================
INSERT INTO system_config (config_name, config_value, remark, create_time) 
VALUES ('material_domain_migration_completed', '1', '物料域统一迁移完成标记', NOW())
ON DUPLICATE KEY UPDATE 
  config_value = '1', 
  update_time = NOW();
