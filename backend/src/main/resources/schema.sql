-- ============================================
-- VeriCart AI — Database Schema v1.0
-- ============================================

CREATE DATABASE IF NOT EXISTS vericart
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE vericart;

-- ============================================
-- Users Table
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20),
    `avatar` VARCHAR(500),
    `role` VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=disabled, 1=active',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (`email`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Categories Table
-- ============================================
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `images` VARCHAR(1000) DEFAULT NULL COMMENT 'JSON array of offline/local image URLs for sub-category cards',
    FOREIGN KEY (`parent_id`) REFERENCES `category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Products Table
-- ============================================
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(12,2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `category_id` BIGINT,
    `brand` VARCHAR(100),
    `images` JSON COMMENT 'Array of image URLs',
    `specifications` JSON COMMENT 'Product specs as JSON',
    `rating` DECIMAL(3,2) DEFAULT 0.00,
    `review_count` INT DEFAULT 0,
    `trust_score` INT DEFAULT NULL COMMENT 'AI-generated 0-100',
    `trust_level` VARCHAR(20) DEFAULT NULL COMMENT 'Low/Medium/High/Excellent',
    `ai_summary` TEXT COMMENT 'AI-generated review summary',
    `ai_summary_time` DATETIME COMMENT 'When summary was last generated',
    `fake_review_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1 COMMENT '0=inactive, 1=active',
    `seller_id` BIGINT,
    `external_url` VARCHAR(1024) DEFAULT NULL COMMENT 'External/Taobao product URL',
    `variants` JSON DEFAULT NULL COMMENT 'SKU options as JSON',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX idx_category (`category_id`),
    INDEX idx_name (`name`),
    INDEX idx_price (`price`),
    INDEX idx_trust_score (`trust_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Shopping Cart Table
-- ============================================
CREATE TABLE IF NOT EXISTS `cart_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Orders Table
-- ============================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL,
    `status` ENUM('PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    `shipping_name` VARCHAR(50),
    `shipping_phone` VARCHAR(20),
    `shipping_address` VARCHAR(500),
    `payment_method` VARCHAR(50) DEFAULT 'SIMULATED',
    `note` VARCHAR(500),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX idx_user (`user_id`),
    INDEX idx_order_no (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Order Items Table
-- ============================================
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(200) NOT NULL,
    `product_image` VARCHAR(500),
    `price` DECIMAL(12,2) NOT NULL,
    `quantity` INT NOT NULL,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Reviews Table
-- ============================================
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `order_id` BIGINT,
    `rating` TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    `content` TEXT,
    `images` JSON COMMENT 'Array of image URLs',
    `sentiment` VARCHAR(20) COMMENT 'POSITIVE/NEUTRAL/NEGATIVE — AI',
    `emotion` VARCHAR(30) COMMENT 'AI emotion detection',
    `fake_probability` DECIMAL(5,2) COMMENT 'AI fake review probability 0-100',
    `fake_reason` TEXT COMMENT 'Why AI flagged as suspicious',
    `is_flagged` TINYINT DEFAULT 0,
    `status` TINYINT DEFAULT 1 COMMENT '0=hidden, 1=visible',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    INDEX idx_product (`product_id`),
    INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Wishlist Table
-- ============================================
CREATE TABLE IF NOT EXISTS `wishlist` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- AI Analysis Log Table
-- ============================================
CREATE TABLE IF NOT EXISTS `ai_analysis_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_type` VARCHAR(20) NOT NULL COMMENT 'REVIEW/PRODUCT/RECOMMENDATION',
    `target_id` BIGINT NOT NULL,
    `ai_model` VARCHAR(50) NOT NULL COMMENT 'deepseek/kimi/hunyuan',
    `task` VARCHAR(50) NOT NULL COMMENT 'sentiment/fake_detection/summary/trust_score/recommendation',
    `request` TEXT,
    `response` TEXT,
    `processing_time_ms` INT,
    `status` VARCHAR(20) DEFAULT 'SUCCESS',
    `error_message` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Notifications Table
-- ============================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `type` VARCHAR(50) COMMENT 'ORDER/REVIEW/SYSTEM/AI',
    `is_read` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX idx_user_read (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Audit Log Table (FR-069 / FR-070)
-- ============================================
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT 'User performing the action (NULL for guests)',
    `username` VARCHAR(50),
    `action` VARCHAR(50) NOT NULL COMMENT 'e.g. LOGIN, ORDER_CREATED, REVIEW_CREATED, AI_ANALYSIS',
    `category` VARCHAR(20) NOT NULL COMMENT 'LOGIN/ORDER/REVIEW/AI/ERROR',
    `target_type` VARCHAR(30),
    `target_id` BIGINT,
    `detail` TEXT,
    `ip` VARCHAR(45),
    `is_error` TINYINT DEFAULT 0 COMMENT '1 = error entry (FR-070)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (`category`),
    INDEX idx_error (`is_error`),
    INDEX idx_created (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Product Inquiries Table (customer → store owner)
-- ============================================
CREATE TABLE IF NOT EXISTS `inquiry` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `message` TEXT NOT NULL,
    `reply` TEXT,
    `reply_source` VARCHAR(20) COMMENT 'AI/SELLER - who wrote the reply',
    `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN/REPLIED/CLOSED',
    `is_read` TINYINT DEFAULT 0 COMMENT '1 = seller has seen it',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX idx_inquiry_seller (`seller_id`, `is_read`),
    INDEX idx_inquiry_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat turns inside an inquiry thread (multi-turn conversations)
CREATE TABLE IF NOT EXISTS `inquiry_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `inquiry_id` BIGINT NOT NULL,
    `sender` VARCHAR(10) NOT NULL COMMENT 'USER / AI / SELLER',
    `content` TEXT NOT NULL,
    `is_read` TINYINT DEFAULT 0 COMMENT '1 = the other side has seen it',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`inquiry_id`) REFERENCES `inquiry`(`id`) ON DELETE CASCADE,
    INDEX idx_inquiry_msg (`inquiry_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Backfill chat turns from the old single Q&A columns (safe no-op when already migrated)
INSERT INTO `inquiry_message` (`inquiry_id`, `sender`, `content`, `is_read`, `created_at`)
SELECT `id`, 'USER', `message`, `is_read`, `created_at` FROM `inquiry`
WHERE `id` NOT IN (SELECT `inquiry_id` FROM `inquiry_message`);
INSERT INTO `inquiry_message` (`inquiry_id`, `sender`, `content`, `is_read`, `created_at`)
SELECT `id`, IF(`reply_source` = 'AI', 'AI', 'SELLER'), `reply`, 0, `updated_at` FROM `inquiry`
WHERE `reply` IS NOT NULL
  AND `id` NOT IN (SELECT `inquiry_id` FROM `inquiry_message` WHERE `sender` <> 'USER');

-- ============================================
-- Seed Data
-- ============================================

-- Admin user (password: admin123)
INSERT INTO `user` (`username`, `email`, `password`, `role`, `status`)
VALUES ('admin', 'admin@vericart.ai', '$2a$10$6Vzf8EuBVoQ4fJVFxjZ/1OwvjtMQWeMxYkw/GMU2gx9GiJNufe0Lu', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- Categories — Pinduoduo-style primary categories (11 mains)
INSERT INTO `category` (`id`, `name`, `sort_order`) VALUES
(2, 'Apparel & Fashion', 1),
(34, 'Fresh Produce & Grocery', 2),
(3, 'Home & Kitchen', 3),
(1, 'Electronics & Digital', 4),
(42, 'Home Appliances', 5),
(26, 'Beauty & Personal Care', 6),
(30, 'Mother & Baby', 7),
(5, 'Sports & Outdoors', 8),
(46, 'Car & Auto Accessories', 9),
(50, 'Pet Supplies', 10),
(4, 'Stationery & Office', 11)
ON DUPLICATE KEY UPDATE id = id;

-- Sub-categories
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(6, 'Smartphones', 1, 1),
(7, 'Laptops', 1, 2),
(8, 'Headphones', 1, 3),
(11, 'Tablets', 1, 4),
(12, 'Cameras', 1, 5),
(13, 'Smart Watches', 1, 6),
(53, 'Mobile Accessories', 1, 7),
(54, 'Computer Accessories', 1, 8),
(55, 'Smart Devices', 1, 9),
(10, 'Women''s Clothing', 2, 1),
(9, 'Men''s Clothing', 2, 2),
(56, 'Underwear & Sleepwear', 2, 3),
(57, 'Kids'' Clothing', 2, 4),
(16, 'Accessories', 2, 5),
(14, 'Shoes', 2, 6),
(39, 'Women''s Shoes', 2, 7),
(40, 'Men''s Shoes', 2, 8),
(41, 'Bags & Luggage', 2, 9),
(17, 'Kitchenware', 3, 1),
(18, 'Bedding & Towels', 3, 2),
(19, 'Home Decor', 3, 3),
(58, 'Storage & Organization', 3, 4),
(15, 'Furniture', 3, 5),
(63, 'Cleaning Supplies', 3, 6),
(69, 'Office Supplies', 4, 1),
(70, 'School Supplies', 4, 2),
(23, 'Books', 4, 3),
(71, 'Craft Supplies', 4, 4),
(20, 'Fitness Equipment', 5, 1),
(21, 'Activewear', 5, 2),
(22, 'Outdoor Gear', 5, 3),
(59, 'Sports Shoes', 5, 4),
(66, 'Camping Supplies', 5, 5),
(27, 'Skincare', 26, 1),
(28, 'Makeup & Fragrance', 26, 2),
(29, 'Hair & Body Care', 26, 3),
(31, 'Baby Food & Formula', 30, 1),
(32, 'Diapers & Wipes', 30, 2),
(33, 'Toys & Games', 30, 3),
(65, 'Maternity & Baby Gear', 30, 4),
(37, 'Fruits & Vegetables', 34, 1),
(35, 'Snacks', 34, 2),
(36, 'Beverages', 34, 3),
(60, 'Meat & Seafood', 34, 4),
(61, 'Dairy & Bakery', 34, 5),
(62, 'Pantry Essentials', 34, 6),
(43, 'Kitchen Appliances', 42, 1),
(44, 'Cleaning Appliances', 42, 2),
(45, 'Large Appliances', 42, 3),
(64, 'Personal Care Appliances', 42, 4),
(47, 'Car Electronics', 46, 1),
(48, 'Car Interior', 46, 2),
(49, 'Car Care & Maintenance', 46, 3),
(51, 'Pet Food', 50, 1),
(52, 'Pet Toys & Accessories', 50, 2),
(68, 'Pet Grooming & Beds', 50, 3)
ON DUPLICATE KEY UPDATE id = id;

-- Products (seed data — offline/local images only, no online URLs)
INSERT INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) VALUES
(1, 'iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB. 48MP camera, A17 Pro chip, titanium design.', 999.99, 50, 6, 'Apple',
 '[]',
 4.5, 128, 92, 'Excellent', 1, 1),
(2, 'MacBook Air M3', 'Apple MacBook Air M3 15-inch, 8GB RAM, 256GB SSD.', 1299.00, 30, 7, 'Apple',
 '[]',
 4.7, 95, 95, 'Excellent', 1, 1),
(3, 'Sony WH-1000XM5', 'Industry-leading noise canceling headphones with 30-hour battery life.', 349.99, 100, 8, 'Sony',
 '[]',
 4.6, 210, 88, 'Excellent', 1, 1),
(4, 'Samsung Galaxy S24', 'Samsung Galaxy S24 Ultra 512GB. 200MP camera, S Pen included.', 1199.99, 40, 6, 'Samsung',
 '[]',
 4.4, 87, 90, 'Excellent', 1, 1),
(5, 'Nike Air Max 270', 'Nike Air Max 270 Mens Shoes. Lightweight cushioning for all-day comfort.', 150.00, 200, 9, 'Nike',
 '[]',
 4.3, 156, 85, 'High', 1, 1),
(6, 'Dyson V15 Detect', 'Dyson V15 Detect Cordless Vacuum. Laser reveals microscopic dust.', 749.99, 25, 3, 'Dyson',
 '[]',
 4.8, 64, 91, 'Excellent', 1, 1),
(7, 'Kindle Paperwhite', 'Amazon Kindle Paperwhite 16GB. Waterproof e-reader with 6.8" display.', 139.99, 150, 4, 'Amazon',
 '[]',
 4.7, 312, 87, 'Excellent', 1, 1),
(8, 'Logitech MX Master 3S', 'Logitech MX Master 3S Wireless Mouse. 8K DPI, quiet clicks, ergonomic design.', 99.99, 300, 1, 'Logitech',
 '[]',
 4.5, 89, 80, 'High', 1, 1),
(9, 'iPad Air M2', 'Apple iPad Air M2 11-inch. Liquid Retina display, Apple Pencil Pro support.', 599.99, 45, 11, 'Apple',
 '[]',
 4.6, 178, 93, 'Excellent', 1, 1),
(10, 'Canon EOS R6 Mark II', 'Canon EOS R6 Mark II Mirrorless Camera. 24.2MP, 4K 60p video, advanced AF.', 2499.00, 15, 12, 'Canon',
 '[]',
 4.8, 56, 96, 'Excellent', 1, 1),
(11, 'Apple Watch Series 9', 'Apple Watch Series 9 GPS 45mm. Always-On Retina, blood oxygen, ECG.', 429.00, 80, 13, 'Apple',
 '[]',
 4.5, 234, 91, 'Excellent', 1, 1),
(12, 'Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 128GB. 11" Dynamic AMOLED 2X, S Pen included.', 799.99, 35, 11, 'Samsung',
 '[]',
 4.4, 67, 86, 'Excellent', 1, 1),
(13, 'Bose QuietComfort Ultra', 'Bose QuietComfort Ultra Earbuds. Spatial audio, world-class ANC.', 299.99, 120, 8, 'Bose',
 '[]',
 4.5, 189, 84, 'High', 1, 1),
(14, 'Nike Dunk Low', 'Nike Dunk Low Retro Mens Shoes. Classic 80s hoops style, durable leather upper.', 115.00, 180, 9, 'Nike',
 '[]',
 4.4, 312, 82, 'High', 1, 1),
(15, 'Adidas Ultraboost Light', 'Adidas Ultraboost Light Running Shoes. Lightest Ultraboost ever with LEP torsion system.', 190.00, 150, 9, 'Adidas',
 '[]',
 4.3, 145, 79, 'High', 1, 1),
(16, 'Levi\'s 501 Original', 'Levi\'s 501 Original Fit Jeans. Iconic straight leg, non-stretch denim.', 69.50, 250, 9, 'Levi\'s',
 '[]',
 4.6, 520, 76, 'High', 1, 1),
(17, 'Nike Air Force 1', 'Nike Air Force 1 \'07 Women\'s Shoes. Legendary style, premium leather.', 120.00, 170, 14, 'Nike',
 '[]',
 4.7, 278, 83, 'High', 1, 1),
(18, 'IKEA KALLAX Shelf', 'IKEA KALLAX Shelf Unit 77x147cm. Versatile storage solution for any room.', 89.99, 60, 15, 'IKEA',
 '[]',
 4.2, 340, 72, 'High', 1, 1),
(19, 'Apple AirPods Pro 2', 'Apple AirPods Pro 2nd Gen. Active Noise Cancellation, personalized spatial audio.', 249.00, 200, 8, 'Apple',
 '[]',
 4.7, 456, 94, 'Excellent', 1, 1),
(20, 'Herman Miller Aeron', 'Herman Miller Aeron Chair. Fully adjustable ergonomic office chair with PostureFit SL.', 1395.00, 10, 15, 'Herman Miller',
 '[]',
 4.9, 43, 97, 'Excellent', 1, 1)
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
