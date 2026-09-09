-- ============================================================
-- VeriCart AI — Product variants (SKU options) migration
-- Adds a `variants` JSON column and back-fills options for every
-- product based on its (main) category.
-- Idempotent: re-running is safe.
-- ============================================================

USE vericart;

-- 1) Add the column if it does not already exist
SET @dbname = DATABASE();
SET @tbl = 'product';
SET @col = 'variants';
SET @exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = @dbname AND table_name = @tbl AND column_name = @col
);
SET @sql = IF(@exists = 0,
  'ALTER TABLE product ADD COLUMN variants JSON DEFAULT NULL COMMENT \'SKU options as JSON\' AFTER external_url',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Back-fill variant options per (main) category.
--    For a product whose category_id is a sub-category we walk up to the parent.
UPDATE product p
SET p.variants = CASE
  COALESCE((SELECT c.parent_id FROM category c WHERE c.id = p.category_id AND c.parent_id IS NOT NULL), p.category_id)
  -- 1 Electronics & Digital: phones / laptops / tablets
  WHEN 1  THEN '[{"name":"Color","options":["Space Gray","Silver","Gold","Blue"]},{"name":"Storage","options":["128GB","256GB","512GB","1TB"]},{"name":"Screen Size","options":["6.1 inch","6.7 inch","13 inch","14 inch","16 inch"]}]'
  -- 2 Apparel & Fashion: clothes
  WHEN 2  THEN '[{"name":"Size","options":["XS","S","M","L","XL","XXL"]},{"name":"Color","options":["Black","White","Gray","Red","Blue"]}]'
  -- 3 Home & Kitchen
  WHEN 3  THEN '[{"name":"Color","options":["White","Beige","Natural Wood"]},{"name":"Specification","options":["Standard","Large"]}]'
  -- 4 Stationery & Office
  WHEN 4  THEN '[{"name":"Specification","options":["Single","Set"]},{"name":"Color","options":["Multicolor"]}]'
  -- 5 Sports & Outdoors
  WHEN 5  THEN '[{"name":"Size","options":["S","M","L","XL"]},{"name":"Color","options":["Black","Blue","Red"]}]'
  -- 26 Beauty & Personal Care
  WHEN 26 THEN '[{"name":"Specification","options":["30ml","50ml","100ml"]},{"name":"Shade","options":["Natural","Fair"]}]'
  -- 30 Mother & Baby
  WHEN 30 THEN '[{"name":"Size","options":["S","M","L"]},{"name":"Color","options":["Light Pink","Light Blue","Cream"]}]'
  -- 34 Fresh Produce & Grocery
  WHEN 34 THEN '[{"name":"Specification","options":["500g","1kg","2kg"]}]'
  -- 42 Home Appliances
  WHEN 42 THEN '[{"name":"Color","options":["White","Black"]},{"name":"Model","options":["Basic","Premium"]}]'
  -- 46 Car & Auto Accessories
  WHEN 46 THEN '[{"name":"Compatibility","options":["Universal","Model Specific"]},{"name":"Color","options":["Black","Silver"]}]'
  -- 50 Pet Supplies
  WHEN 50 THEN '[{"name":"Specification","options":["Small","Large"]},{"name":"Flavor","options":["Chicken","Beef","Salmon"]}]'
  ELSE NULL
END;