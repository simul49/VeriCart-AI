-- ============================================
-- VeriCart AI — add external_url (Taobao link) to product
-- Idempotent: safe to run more than once.
-- ============================================

USE vericart;

SET @db = DATABASE();
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'product' AND column_name = 'external_url'
);

SET @sql = IF(
  @col_exists = 0,
  'ALTER TABLE product ADD COLUMN external_url VARCHAR(1024) DEFAULT NULL COMMENT ''External/Taobao product URL''',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
