$OutputEncoding = New-Object System.Text.UTF8Encoding($false)
$sql = @"
-- 1) Remove the 17 duplicate rows this session accidentally inserted
DELETE FROM product WHERE id BETWEEN 190482 AND 190498;

-- 2) Overwrite the existing catalog's placeholder (loremflickr) images with real local files
UPDATE product SET images = '["/images/10-2-1-Nike-Sportswear-Club-T-Shirt.png","/images/10-2-2-Nike-Sportswear-Club-T-Shirt.png","/images/10-2-3-Nike-Sportswear-Club-T-Shirt.png"]' WHERE id = 190301;
UPDATE product SET images = '["/images/9-1-1-Uniqlo-AIRism-Cotton-T-Shirt.png","/images/9-1-2-Uniqlo-AIRism-Cotton-T-Shirt.png","/images/9-1-3-Uniqlo-AIRism-Cotton-T-Shirt.png","/images/Uniqlo-AIRism-Cotton-T-Shirt.jpg"]' WHERE id = 190300;
UPDATE product SET images = '["/images/9-2-1-H-M-Floral-Wrap-Dress.png","/images/9-2-2-H-M-Floral-Wrap-Dress.png"]' WHERE id = 190303;
UPDATE product SET images = '["/images/15-1-1-Coach-Tabby-Shoulder-Bag.png","/images/15-1-2-Coach-Tabby-Shoulder-Bag.png","/images/15-1-3-Coach-Tabby-Shoulder-Bag.png"]' WHERE id = 190314;
UPDATE product SET images = '["/images/16-2-1-Michael-Kors-Jet-Set-Crossbody.png","/images/16-2-2-Michael-Kors-Jet-Set-Crossbody.png","/images/16-2-3-Michael-Kors-Jet-Set-Crossbody.png"]' WHERE id = 190315;
UPDATE product SET images = '["/images/11-2-1-Huawei-MatePad-Pro-13-2-2.jpg","/images/11-2-2-Huawei-MatePad-Pro-13-2-2.jpg"]' WHERE id = 190305;
UPDATE product SET images = '["/images/14-2-1-Huawei-Watch-GT-4-1.jpg","/images/14-2-2-Huawei-Watch-GT-4-2.jpg"]' WHERE id = 190309;
UPDATE product SET images = '["/images/Atomic-Habits-Book.jpeg"]' WHERE id = 190330;
UPDATE product SET images = '["/images/Coca-Cola-Original-330ml.jpeg"]' WHERE id = 190348;
UPDATE product SET images = '["/images/36Red-Bull-Energy-Drink-250ml-1.jpeg"]' WHERE id = 190349;
UPDATE product SET images = '["/images/Xiaomi-Redmi-Note-13-Pro.jpg"]' WHERE id = 190295;

-- 3) Insert only the 6 brands that have no existing product
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
VALUES ('Xiaomi Smart Speaker 2', 'Wi-Fi smart speaker.', 39.99, 110, 55, 'Xiaomi',
  '["/images/Xiaomi-Smart-Speaker-2.png"]', 1, 1);
"@

$sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pparadox49 vericart
Write-Host "cleanup + seed finished"
