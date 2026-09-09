-- ============================================================
-- VeriCart AI - Variant label i18n migration
-- Converts legacy Chinese variant group / option values to English
-- in the product.variants JSON column.
-- Idempotent: nested REPLACE on already-English strings is a no-op,
-- so re-running is safe.
-- ============================================================

USE vericart;

-- 1) Group names
UPDATE product SET variants = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(variants,
  '颜色',     'Color'),
  '尺码',     'Size'),
  '存储',     'Storage'),
  '屏幕尺寸', 'Screen Size'),
  '规格',     'Specification'),
  '色号',     'Shade'),
  '适配',     'Compatibility'),
  '型号',     'Model'),
  '口味',     'Flavor')
WHERE variants IS NOT NULL;

-- 2) Color options
UPDATE product SET variants = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(variants,
  '黑色',     'Black'),
  '白色',     'White'),
  '灰色',     'Gray'),
  '红色',     'Red'),
  '蓝色',     'Blue'),
  '深空灰',   'Space Gray'),
  '银色',     'Silver'),
  '金色',     'Gold'),
  '米色',     'Beige'),
  '原木色',   'Natural Wood'),
  '自然色',   'Natural'),
  '偏白色',   'Fair'),
  '浅粉',     'Light Pink'),
  '浅蓝',     'Light Blue'),
  '米白',     'Cream'),
  '多色',     'Multicolor')
WHERE variants IS NOT NULL;

-- 3) Spec / packaging options
UPDATE product SET variants = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(variants,
  '标准装',   'Standard'),
  '大容量',   'Large'),
  '基础款',   'Basic'),
  '旗舰款',   'Premium'),
  '通用款',   'Universal'),
  '专车专用', 'Model Specific'),
  '单本',     'Single'),
  '套装',     'Set'),
  '小包装',   'Small'),
  '大包装',   'Large')
WHERE variants IS NOT NULL;

-- 4) Flavors (pet food) + screen-size unit
UPDATE product SET variants = REPLACE(REPLACE(REPLACE(REPLACE(variants,
  '鸡肉',     'Chicken'),
  '牛肉',     'Beef'),
  '三文鱼',   'Salmon'),
  '英寸',     ' inch')
WHERE variants IS NOT NULL;