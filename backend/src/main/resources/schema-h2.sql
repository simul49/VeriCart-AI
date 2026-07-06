-- ============================================
-- VeriCart AI — H2 Database Schema (MySQL Compatibility Mode)
-- ============================================

-- Users Table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20),
    `avatar` VARCHAR(500),
    `role` VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories Table
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`parent_id`) REFERENCES `category`(`id`) ON DELETE SET NULL
);

-- Products Table
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(12,2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `category_id` BIGINT,
    `brand` VARCHAR(100),
    `images` TEXT,
    `specifications` TEXT,
    `rating` DECIMAL(3,2) DEFAULT 0.00,
    `review_count` INT DEFAULT 0,
    `trust_score` INT DEFAULT NULL,
    `trust_level` VARCHAR(20) DEFAULT NULL,
    `ai_summary` TEXT,
    `ai_summary_time` TIMESTAMP,
    `fake_review_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `seller_id` BIGINT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`seller_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
);

-- Shopping Cart Table
CREATE TABLE IF NOT EXISTS `cart_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (`user_id`, `product_id`)
);

-- Orders Table
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'PENDING',
    `shipping_name` VARCHAR(50),
    `shipping_phone` VARCHAR(20),
    `shipping_address` VARCHAR(500),
    `payment_method` VARCHAR(50) DEFAULT 'SIMULATED',
    `note` VARCHAR(500),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- Order Items Table
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(200) NOT NULL,
    `product_image` VARCHAR(500),
    `price` DECIMAL(12,2) NOT NULL,
    `quantity` INT NOT NULL,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE
);

-- Reviews Table
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `order_id` BIGINT,
    `rating` TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    `content` TEXT,
    `images` TEXT,
    `sentiment` VARCHAR(20),
    `emotion` VARCHAR(30),
    `fake_probability` DECIMAL(5,2),
    `fake_reason` TEXT,
    `is_flagged` TINYINT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE
);

-- Wishlist Table
CREATE TABLE IF NOT EXISTS `wishlist` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product_wl (`user_id`, `product_id`)
);

-- AI Analysis Log Table
CREATE TABLE IF NOT EXISTS `ai_analysis_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_type` VARCHAR(20) NOT NULL,
    `target_id` BIGINT NOT NULL,
    `ai_model` VARCHAR(50) NOT NULL,
    `task` VARCHAR(50) NOT NULL,
    `request` TEXT,
    `response` TEXT,
    `processing_time_ms` INT,
    `status` VARCHAR(20) DEFAULT 'SUCCESS',
    `error_message` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications Table
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `type` VARCHAR(50),
    `is_read` TINYINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- ============================================
-- Seed Data
-- ============================================

-- Admin user (password: admin123, BCrypt encoded)
MERGE INTO `user` (`id`, `username`, `email`, `password`, `role`, `status`) KEY (`id`) VALUES
(1, 'admin', 'admin@vericart.ai', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', 1);

MERGE INTO `user` (`id`, `username`, `email`, `password`, `role`, `status`) KEY (`id`) VALUES
(2, 'testuser', 'test@vericart.ai', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'CUSTOMER', 1);

-- Categories
MERGE INTO `category` (`id`, `name`, `sort_order`) KEY (`id`) VALUES
(1, 'Electronics', 1);
MERGE INTO `category` (`id`, `name`, `sort_order`) KEY (`id`) VALUES
(2, 'Fashion', 2);
MERGE INTO `category` (`id`, `name`, `sort_order`) KEY (`id`) VALUES
(3, 'Home & Kitchen', 3);
MERGE INTO `category` (`id`, `name`, `sort_order`) KEY (`id`) VALUES
(4, 'Books', 4);
MERGE INTO `category` (`id`, `name`, `sort_order`) KEY (`id`) VALUES
(5, 'Sports', 5);

-- Sub-categories
MERGE INTO `category` (`id`, `name`, `parent_id`, `sort_order`) KEY (`id`) VALUES
(6, 'Smartphones', 1, 1);
MERGE INTO `category` (`id`, `name`, `parent_id`, `sort_order`) KEY (`id`) VALUES
(7, 'Laptops', 1, 2);
MERGE INTO `category` (`id`, `name`, `parent_id`, `sort_order`) KEY (`id`) VALUES
(8, 'Headphones', 1, 3);
MERGE INTO `category` (`id`, `name`, `parent_id`, `sort_order`) KEY (`id`) VALUES
(9, 'Men''s Clothing', 2, 1);
MERGE INTO `category` (`id`, `name`, `parent_id`, `sort_order`) KEY (`id`) VALUES
(10, 'Women''s Clothing', 2, 2);

-- Sample Products
MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(1, 'Wireless Bluetooth Headphones', 'Premium noise-cancelling wireless headphones with 30-hour battery life. Features active noise cancellation, comfortable over-ear design, and crystal-clear audio quality.', 299.99, 150, 8, 'SoundMax', '["https://picsum.photos/seed/headphones1/400/400","https://picsum.photos/seed/headphones2/400/400"]', 4.5, 128, 85, 'Excellent', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(2, 'Smartphone X Pro', 'Latest flagship smartphone with 6.7-inch AMOLED display, 108MP camera, and 5G connectivity. Powered by the fastest chipset with 12GB RAM and 256GB storage.', 899.99, 75, 6, 'TechVibe', '["https://picsum.photos/seed/phone1/400/400","https://picsum.photos/seed/phone2/400/400"]', 4.7, 256, 92, 'Excellent', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(3, 'Ultra-Slim Laptop 15"', 'Powerful ultrabook with 15.6-inch 4K display, Intel i9 processor, 16GB RAM, 512GB SSD. Perfect for professionals and creators.', 1299.99, 40, 7, 'TechVibe', '["https://picsum.photos/seed/laptop1/400/400","https://picsum.photos/seed/laptop2/400/400"]', 4.6, 189, 88, 'Excellent', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(4, 'Men''s Classic Fit Polo Shirt', 'Premium cotton polo shirt with classic fit. Breathable fabric, available in multiple colors. Machine washable.', 49.99, 300, 9, 'UrbanWear', '["https://picsum.photos/seed/polo1/400/400","https://picsum.photos/seed/polo2/400/400"]', 4.2, 89, 72, 'High', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(5, 'Women''s Summer Dress', 'Elegant floral summer dress made from lightweight, breathable fabric. Perfect for casual outings and beach days.', 69.99, 200, 10, 'Elegance', '["https://picsum.photos/seed/dress1/400/400","https://picsum.photos/seed/dress2/400/400"]', 4.4, 156, 78, 'High', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(6, 'Stainless Steel Cookware Set', '10-piece premium cookware set with non-stick coating. Includes pots, pans, and utensils. Dishwasher safe.', 199.99, 80, 3, 'HomeChef', '["https://picsum.photos/seed/cookware1/400/400","https://picsum.photos/seed/cookware2/400/400"]', 4.3, 67, 76, 'High', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(7, 'Fitness Tracker Watch', 'Advanced fitness tracker with heart rate monitor, GPS, sleep tracking, and 7-day battery life. Water resistant to 50m.', 149.99, 120, 5, 'FitLife', '["https://picsum.photos/seed/fitness1/400/400","https://picsum.photos/seed/fitness2/400/400"]', 4.1, 94, 69, 'Medium', 1, 1);

MERGE INTO `product` (`id`, `name`, `description`, `price`, `stock`, `category_id`, `brand`, `images`, `rating`, `review_count`, `trust_score`, `trust_level`, `status`, `seller_id`) KEY (`id`) VALUES
(8, 'Best-Selling Mystery Novel', 'Award-winning mystery thriller that will keep you on the edge of your seat. #1 New York Times Bestseller.', 19.99, 500, 4, 'Penguin Books', '["https://picsum.photos/seed/book1/400/400","https://picsum.photos/seed/book2/400/400"]', 4.8, 312, 95, 'Excellent', 1, 1);

-- Sample Reviews
MERGE INTO `review` (`id`, `user_id`, `product_id`, `rating`, `content`, `sentiment`, `is_flagged`, `status`) KEY (`id`) VALUES
(1, 2, 1, 5, 'Amazing headphones! The noise cancellation is incredible and the battery lasts forever.', 'POSITIVE', 0, 1);
MERGE INTO `review` (`id`, `user_id`, `product_id`, `rating`, `content`, `sentiment`, `is_flagged`, `status`) KEY (`id`) VALUES
(2, 2, 1, 4, 'Great sound quality but the ear cushions could be more comfortable for long sessions.', 'NEUTRAL', 0, 1);
MERGE INTO `review` (`id`, `user_id`, `product_id`, `rating`, `content`, `sentiment`, `is_flagged`, `status`) KEY (`id`) VALUES
(3, 2, 2, 5, 'Best phone I have ever owned! Camera is stunning and battery life is impressive.', 'POSITIVE', 0, 1);
MERGE INTO `review` (`id`, `user_id`, `product_id`, `rating`, `content`, `sentiment`, `is_flagged`, `status`) KEY (`id`) VALUES
(4, 2, 3, 5, 'This laptop handles everything I throw at it. Perfect for video editing!', 'POSITIVE', 0, 1);
