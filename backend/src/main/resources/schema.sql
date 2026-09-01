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
    `ai_model` VARCHAR(50) NOT NULL COMMENT 'deepseek/qwen/hunyuan',
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

-- Categories
INSERT INTO `category` (`id`, `name`, `sort_order`) VALUES
(1, 'Electronics', 1),
(2, 'Fashion', 2),
(3, 'Home & Kitchen', 3),
(4, 'Books', 4),
(5, 'Sports', 5)
ON DUPLICATE KEY UPDATE `name` = `name`;

-- Sub-categories
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(6, 'Smartphones', 1, 1),
(7, 'Laptops', 1, 2),
(8, 'Headphones', 1, 3),
(9, 'Men\'s Clothing', 2, 1),
(10, 'Women\'s Clothing', 2, 2),
(11, 'Tablets', 1, 4),
(12, 'Cameras', 1, 5),
(13, 'Smart Watches', 1, 6),
(14, 'Women\'s Shoes', 2, 3),
(15, 'Furniture', 3, 2)
ON DUPLICATE KEY UPDATE `name` = `name`;

-- Products (seed data with real matching images from Unsplash)
INSERT INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) VALUES
(1, 'iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB. 48MP camera, A17 Pro chip, titanium design.', 999.99, 50, 6, 'Apple',
 '["https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&h=600&fit=crop"]',
 4.5, 128, 92, 'Excellent', 1, 1),
(2, 'MacBook Air M3', 'Apple MacBook Air M3 15-inch, 8GB RAM, 256GB SSD.', 1299.00, 30, 7, 'Apple',
 '["https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=600&h=600&fit=crop"]',
 4.7, 95, 95, 'Excellent', 1, 1),
(3, 'Sony WH-1000XM5', 'Industry-leading noise canceling headphones with 30-hour battery life.', 349.99, 100, 8, 'Sony',
 '["https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600&h=600&fit=crop"]',
 4.6, 210, 88, 'Excellent', 1, 1),
(4, 'Samsung Galaxy S24', 'Samsung Galaxy S24 Ultra 512GB. 200MP camera, S Pen included.', 1199.99, 40, 6, 'Samsung',
 '["https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1567581935884-3349723552ca?w=600&h=600&fit=crop"]',
 4.4, 87, 90, 'Excellent', 1, 1),
(5, 'Nike Air Max 270', 'Nike Air Max 270 Mens Shoes. Lightweight cushioning for all-day comfort.', 150.00, 200, 9, 'Nike',
 '["https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=600&h=600&fit=crop"]',
 4.3, 156, 85, 'High', 1, 1),
(6, 'Dyson V15 Detect', 'Dyson V15 Detect Cordless Vacuum. Laser reveals microscopic dust.', 749.99, 25, 3, 'Dyson',
 '["https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1527515545081-5db172df4a1e?w=600&h=600&fit=crop"]',
 4.8, 64, 91, 'Excellent', 1, 1),
(7, 'Kindle Paperwhite', 'Amazon Kindle Paperwhite 16GB. Waterproof e-reader with 6.8" display.', 139.99, 150, 4, 'Amazon',
 '["https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&h=600&fit=crop"]',
 4.7, 312, 87, 'Excellent', 1, 1),
(8, 'Logitech MX Master 3S', 'Logitech MX Master 3S Wireless Mouse. 8K DPI, quiet clicks, ergonomic design.', 99.99, 300, 1, 'Logitech',
 '["https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&h=600&fit=crop"]',
 4.5, 89, 80, 'High', 1, 1),
(9, 'iPad Air M2', 'Apple iPad Air M2 11-inch. Liquid Retina display, Apple Pencil Pro support.', 599.99, 45, 11, 'Apple',
 '["https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=600&h=600&fit=crop"]',
 4.6, 178, 93, 'Excellent', 1, 1),
(10, 'Canon EOS R6 Mark II', 'Canon EOS R6 Mark II Mirrorless Camera. 24.2MP, 4K 60p video, advanced AF.', 2499.00, 15, 12, 'Canon',
 '["https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=600&h=600&fit=crop"]',
 4.8, 56, 96, 'Excellent', 1, 1),
(11, 'Apple Watch Series 9', 'Apple Watch Series 9 GPS 45mm. Always-On Retina, blood oxygen, ECG.', 429.00, 80, 13, 'Apple',
 '["https://images.unsplash.com/photo-1546868871-af0de0ae72be?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=600&h=600&fit=crop"]',
 4.5, 234, 91, 'Excellent', 1, 1),
(12, 'Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 128GB. 11" Dynamic AMOLED 2X, S Pen included.', 799.99, 35, 11, 'Samsung',
 '["https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=600&h=600&fit=crop"]',
 4.4, 67, 86, 'Excellent', 1, 1),
(13, 'Bose QuietComfort Ultra', 'Bose QuietComfort Ultra Earbuds. Spatial audio, world-class ANC.', 299.99, 120, 8, 'Bose',
 '["https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=600&h=600&fit=crop"]',
 4.5, 189, 84, 'High', 1, 1),
(14, 'Nike Dunk Low', 'Nike Dunk Low Retro Mens Shoes. Classic 80s hoops style, durable leather upper.', 115.00, 180, 9, 'Nike',
 '["https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&h=600&fit=crop"]',
 4.4, 312, 82, 'High', 1, 1),
(15, 'Adidas Ultraboost Light', 'Adidas Ultraboost Light Running Shoes. Lightest Ultraboost ever with LEP torsion system.', 190.00, 150, 9, 'Adidas',
 '["https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1556906781-9a412961c28c?w=600&h=600&fit=crop"]',
 4.3, 145, 79, 'High', 1, 1),
(16, 'Levi\'s 501 Original', 'Levi\'s 501 Original Fit Jeans. Iconic straight leg, non-stretch denim.', 69.50, 250, 9, 'Levi\'s',
 '["https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1604176354204-9268737828e4?w=600&h=600&fit=crop"]',
 4.6, 520, 76, 'High', 1, 1),
(17, 'Nike Air Force 1', 'Nike Air Force 1 \'07 Women\'s Shoes. Legendary style, premium leather.', 120.00, 170, 14, 'Nike',
 '["https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=600&h=600&fit=crop"]',
 4.7, 278, 83, 'High', 1, 1),
(18, 'IKEA KALLAX Shelf', 'IKEA KALLAX Shelf Unit 77x147cm. Versatile storage solution for any room.', 89.99, 60, 15, 'IKEA',
 '["https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1594026112284-02bb6f3352fe?w=600&h=600&fit=crop"]',
 4.2, 340, 72, 'High', 1, 1),
(19, 'Apple AirPods Pro 2', 'Apple AirPods Pro 2nd Gen. Active Noise Cancellation, personalized spatial audio.', 249.00, 200, 8, 'Apple',
 '["https://images.unsplash.com/photo-1588423771073-b8903fbb85b5?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=600&h=600&fit=crop"]',
 4.7, 456, 94, 'Excellent', 1, 1),
(20, 'Herman Miller Aeron', 'Herman Miller Aeron Chair. Fully adjustable ergonomic office chair with PostureFit SL.', 1395.00, 10, 15, 'Herman Miller',
 '["https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=600&h=600&fit=crop", "https://images.unsplash.com/photo-1592078615290-033ee584e267?w=600&h=600&fit=crop"]',
 4.9, 43, 97, 'Excellent', 1, 1)
ON DUPLICATE KEY UPDATE `images` = VALUES(`images`), `description` = VALUES(`description`);
