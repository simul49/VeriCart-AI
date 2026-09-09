-- ============================================================
-- Seed scraped product images into the VeriCart DB
-- 1) Link real images to the 6 existing seeded products that match
-- 2) Create new product "cards" for the distinct unmatched brands
-- NOTE: run once. INSERTs are NOT idempotent (would duplicate on re-run).
-- ============================================================

-- ---- 1) Existing seeded products: set images ----
UPDATE product SET images = '["/images/6-1-Apple-iPhone-15-Pro-256GB.jpg","/images/6-1-2-Apple-iPhone-15-Pro-256GB.jpg"]' WHERE id = 1;
UPDATE product SET images = '["/images/6-1-2-Apple-MacBook-Pro-14-M3.jpg","/images/8-1-Apple-MacBook-Pro-14-M3.jpg"]' WHERE id = 2;
UPDATE product SET images = '["/images/11-1-1-Apple-iPad-Air-11-M2.jpg","/images/11-1-2-Apple-iPad-Air-11-M2-2.jpg"]' WHERE id = 9;
UPDATE product SET images = '["/images/13-2-Canon-EOS-R6-Mark-II.jpg"]' WHERE id = 10;
UPDATE product SET images = '["/images/14-1-Apple-Watch-Series-9-1.jpg","/images/14-2-apple-watch-series-9-3.png"]' WHERE id = 11;
UPDATE product SET images = '["/images/Apple-AirPods-Pro-2.jpg","/images/Apple-AirPods-Pro-2.webp"]' WHERE id = 19;

-- ---- 2) New products from scraped brand images ----
INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Nike Sportswear Club T-Shirt', 'Classic Nike cotton club t-shirt.', 29.99, 100, 9, 'Nike',
  '["/images/10-2-1-Nike-Sportswear-Club-T-Shirt.png","/images/10-2-2-Nike-Sportswear-Club-T-Shirt.png","/images/10-2-3-Nike-Sportswear-Club-T-Shirt.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Uniqlo AIRism Cotton T-Shirt', 'Breathable AIRism cotton tee.', 19.90, 120, 9, 'Uniqlo',
  '["/images/9-1-1-Uniqlo-AIRism-Cotton-T-Shirt.png","/images/9-1-2-Uniqlo-AIRism-Cotton-T-Shirt.png","/images/9-1-3-Uniqlo-AIRism-Cotton-T-Shirt.png","/images/Uniqlo-AIRism-Cotton-T-Shirt.jpg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('H&M Floral Wrap Dress', 'Lightweight floral wrap dress.', 49.99, 80, 10, 'H&M',
  '["/images/9-2-1-H-M-Floral-Wrap-Dress.png","/images/9-2-2-H-M-Floral-Wrap-Dress.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Coach Tabby Shoulder Bag', 'Signature Coach leather shoulder bag.', 350.00, 40, 41, 'Coach',
  '["/images/15-1-1-Coach-Tabby-Shoulder-Bag.png","/images/15-1-2-Coach-Tabby-Shoulder-Bag.png","/images/15-1-3-Coach-Tabby-Shoulder-Bag.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Michael Kors Jet Set Crossbody', 'Jet Set logo crossbody bag.', 298.00, 45, 41, 'Michael Kors',
  '["/images/16-2-1-Michael-Kors-Jet-Set-Crossbody.png","/images/16-2-2-Michael-Kors-Jet-Set-Crossbody.png","/images/16-2-3-Michael-Kors-Jet-Set-Crossbody.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Anker 30W Nano GaN Charger', 'Compact 30W GaN USB-C charger.', 25.99, 200, 53, 'Anker',
  '["/images/53-1-1-Anker-30W-Nano-GaN-Charger-1.jpg","/images/53-1-1-Anker-30W-Nano-GaN-Charger-2.jpg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Xiaomi 67W Turbo Fast Charger', '67W turbo fast charge adapter.', 22.99, 180, 54, 'Xiaomi',
  '["/images/54-2-Xiaomi-67W-Turbo-Fast-Charger.png","/images/Xiaomi-67W-Turbo-Fast-Charger.jpeg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Amazon Echo Dot (5th Gen)', 'Smart speaker with Alexa.', 49.99, 150, 55, 'Amazon',
  '["/images/56-2-1-echo-dot-5th-gen.jpg","/images/56-2-2-echo-dot-5th-gen.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Gap Kids Logo Tee', 'Kids cotton logo t-shirt.', 19.99, 130, 57, 'Gap',
  '["/images/57-1-1-Gap-Kids-Logo-Tee.png","/images/57-1-2-Gap-Kids-Logo-Tee.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Carter''s Baby Romper', 'Soft cotton baby romper.', 14.99, 160, 65, 'Carter''s',
  '["/images/58-2-Carter-s-Baby-Romper.png"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Huawei MatePad Pro 13.2', '13.2-inch flagship tablet.', 899.00, 30, 11, 'Huawei',
  '["/images/11-2-1-Huawei-MatePad-Pro-13-2-2.jpg","/images/11-2-2-Huawei-MatePad-Pro-13-2-2.jpg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Huawei Watch GT 4', 'Fitness smartwatch.', 199.00, 70, 13, 'Huawei',
  '["/images/14-2-1-Huawei-Watch-GT-4-1.jpg","/images/14-2-2-Huawei-Watch-GT-4-2.jpg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Atomic Habits', 'Bestselling self-improvement book by James Clear.', 18.99, 250, 23, 'James Clear',
  '["/images/Atomic-Habits-Book.jpeg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Coca-Cola Original 330ml', 'Classic cola, 330ml can.', 1.99, 500, 36, 'Coca-Cola',
  '["/images/Coca-Cola-Original-330ml.jpeg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Red Bull Energy Drink 250ml', 'Energy drink, 250ml can.', 2.49, 500, 36, 'Red Bull',
  '["/images/36Red-Bull-Energy-Drink-250ml-1.jpeg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Xiaomi Redmi Note 13 Pro', 'Mid-range smartphone.', 299.00, 90, 6, 'Xiaomi',
  '["/images/Xiaomi-Redmi-Note-13-Pro.jpg"]', 1, 1);

INSERT INTO product (name, description, price, stock, category_id, brand, images, seller_id, status)
VALUES ('Xiaomi Smart Speaker 2', 'Wi-Fi smart speaker.', 39.99, 110, 55, 'Xiaomi',
  '["/images/Xiaomi-Smart-Speaker-2.png"]', 1, 1);
