-- ============================================
-- VeriCart AI — Pinduoduo-style category restructure
-- 12 primary categories  ->  11 Pinduoduo primary categories
-- Re-parents existing subs so every product keeps its tag.
-- Idempotent: safe to run more than once.
-- ============================================

USE vericart;

-- 1) Fold the three book-genre subs into a single "Books" sub (id 23)
UPDATE `product` SET `category_id` = 23 WHERE `category_id` IN (24, 25);
UPDATE `category` SET `name` = 'Books' WHERE `id` = 23;

-- 2) Move Shoes & Bags subs under Apparel main (id 2)
UPDATE `category` SET `parent_id` = 2 WHERE `parent_id` = 38;

-- 3) Drop the absorbed mains/subcategories (empty after the moves above)
DELETE FROM `category` WHERE `id` IN (24, 25, 38);

-- 4) Rename mains to Pinduoduo primary categories and set bar order
UPDATE `category` SET `name` = 'Apparel & Fashion',       `sort_order` = 1  WHERE `id` = 2;
UPDATE `category` SET `name` = 'Fresh Produce & Grocery', `sort_order` = 2  WHERE `id` = 34;
UPDATE `category` SET `name` = 'Home & Kitchen',          `sort_order` = 3  WHERE `id` = 3;
UPDATE `category` SET `name` = 'Electronics & Digital',   `sort_order` = 4  WHERE `id` = 1;
UPDATE `category` SET `name` = 'Home Appliances',         `sort_order` = 5  WHERE `id` = 42;
UPDATE `category` SET `name` = 'Beauty & Personal Care',  `sort_order` = 6  WHERE `id` = 26;
UPDATE `category` SET `name` = 'Mother & Baby',           `sort_order` = 7  WHERE `id` = 30;
UPDATE `category` SET `name` = 'Sports & Outdoors',       `sort_order` = 8  WHERE `id` = 5;
UPDATE `category` SET `name` = 'Car & Auto Accessories',  `sort_order` = 9  WHERE `id` = 46;
UPDATE `category` SET `name` = 'Pet Supplies',            `sort_order` = 10 WHERE `id` = 50;
UPDATE `category` SET `name` = 'Stationery & Office',     `sort_order` = 11 WHERE `id` = 4;

-- 5) Align subcategory labels with Pinduoduo naming
UPDATE `category` SET `name` = 'Underwear & Sleepwear'   WHERE `id` = 56;
UPDATE `category` SET `name` = 'Accessories'             WHERE `id` = 16;
UPDATE `category` SET `name` = 'Bedding & Towels'        WHERE `id` = 18;
UPDATE `category` SET `name` = 'Fruits & Vegetables'     WHERE `id` = 37;
UPDATE `category` SET `name` = 'Mobile Accessories'      WHERE `id` = 53;
UPDATE `category` SET `name` = 'Skincare'                WHERE `id` = 27;
UPDATE `category` SET `name` = 'Activewear'              WHERE `id` = 21;
UPDATE `category` SET `name` = 'Car Care & Maintenance'  WHERE `id` = 49;
UPDATE `category` SET `name` = 'Pet Toys & Accessories'  WHERE `id` = 52;

-- 6) New Pinduoduo subcategories (empty until products are added)
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(60, 'Meat & Seafood',          34, 4),
(61, 'Dairy & Bakery',          34, 5),
(62, 'Pantry Essentials',       34, 6),
(63, 'Cleaning Supplies',        3, 6),
(64, 'Personal Care Appliances', 42, 4),
(65, 'Maternity & Baby Gear',    30, 4),
(66, 'Camping Supplies',         5, 5),
(68, 'Pet Grooming & Beds',     50, 3),
(69, 'Office Supplies',          4, 1),
(70, 'School Supplies',          4, 2),
(71, 'Craft Supplies',           4, 4)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 7) Normalise sort_order within each parent (menu order inside the mega-menu)
UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 10 THEN 1 WHEN 9 THEN 2 WHEN 56 THEN 3 WHEN 57 THEN 4 WHEN 16 THEN 5
  WHEN 14 THEN 6 WHEN 39 THEN 7 WHEN 40 THEN 8 WHEN 41 THEN 9 END
WHERE `parent_id` = 2;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 37 THEN 1 WHEN 35 THEN 2 WHEN 36 THEN 3 WHEN 60 THEN 4
  WHEN 61 THEN 5 WHEN 62 THEN 6 END
WHERE `parent_id` = 34;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 17 THEN 1 WHEN 18 THEN 2 WHEN 19 THEN 3 WHEN 58 THEN 4
  WHEN 15 THEN 5 WHEN 63 THEN 6 END
WHERE `parent_id` = 3;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 6 THEN 1 WHEN 7 THEN 2 WHEN 8 THEN 3 WHEN 11 THEN 4 WHEN 12 THEN 5
  WHEN 13 THEN 6 WHEN 53 THEN 7 WHEN 54 THEN 8 WHEN 55 THEN 9 END
WHERE `parent_id` = 1;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 43 THEN 1 WHEN 44 THEN 2 WHEN 45 THEN 3 WHEN 64 THEN 4 END
WHERE `parent_id` = 42;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 27 THEN 1 WHEN 28 THEN 2 WHEN 29 THEN 3 END
WHERE `parent_id` = 26;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 31 THEN 1 WHEN 32 THEN 2 WHEN 33 THEN 3 WHEN 65 THEN 4 END
WHERE `parent_id` = 30;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 20 THEN 1 WHEN 21 THEN 2 WHEN 22 THEN 3 WHEN 59 THEN 4
  WHEN 66 THEN 5 END
WHERE `parent_id` = 5;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 47 THEN 1 WHEN 48 THEN 2 WHEN 49 THEN 3 END
WHERE `parent_id` = 46;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 51 THEN 1 WHEN 52 THEN 2 WHEN 68 THEN 3 END
WHERE `parent_id` = 50;

UPDATE `category` SET `sort_order` = CASE `id`
  WHEN 69 THEN 1 WHEN 70 THEN 2 WHEN 23 THEN 3 WHEN 71 THEN 4 END
WHERE `parent_id` = 4;
