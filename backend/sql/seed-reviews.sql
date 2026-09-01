-- ============================================
-- VeriCart AI — Demo Review Seed Data
-- ============================================
-- Seeds 50 realistic customer reviews across 5 products.
-- Includes deliberately FAKE reviews (generic praise, keyword stuffing,
-- excessive punctuation) so the AI fake-detection has real signal to find.
--
-- AI fields (sentiment, emotion, fake_probability, is_flagged) are left NULL
-- on purpose — run "Refresh Analysis" on a product page (or POST /api/ai/analyze/{id})
-- to let the multi-LLM pipeline analyse them.
--
-- Safe to re-run: clears demo reviews before inserting.
-- ============================================

USE vericart;

DELETE FROM review WHERE id BETWEEN 9001 AND 9100;

-- ============================================
-- Product 1 — iPhone 15 Pro
-- ============================================
INSERT INTO review (id, user_id, product_id, order_id, rating, content, created_at) VALUES
(9001, 2, 1, NULL, 5, 'Upgraded from the iPhone 12 and the difference is night and day. The camera is incredible in low light and the titanium design feels premium without being slippery. Battery easily lasts a full day of heavy use. Delivery was fast and packaging was secure.', '2026-07-02 10:15:00'),
(9002, 3, 1, 2, 4, 'Great phone overall. The A17 Pro chip handles everything I throw at it. Screen display is bright and colours are accurate. Only complaint is the price, it is expensive for what you get. Performance is snappy though.', '2026-07-03 14:22:00'),
(9003, 23, 1, NULL, 5, 'Absolutely love this phone. The build quality is excellent and it feels solid in the hand. Battery life is much better than my old phone. Fast delivery too, arrived in two days.', '2026-07-04 09:05:00'),
(9004, 2, 1, NULL, 2, 'Disappointed with the battery life. After a few months it degrades noticeably and I need to charge twice a day. For this price I expected better. The camera is good but the battery issue is a dealbreaker.', '2026-07-05 18:40:00'),
(9005, 23, 1, NULL, 3, 'Mixed feelings. The design is beautiful and the performance is fast, but it gets quite hot during gaming sessions. Price is also very high. Packaging was nice.', '2026-07-06 11:30:00'),
(9006, 3, 1, NULL, 5, 'Best product ever!!! Amazing quality great price super fast delivery highly recommended buy now!!!', '2026-07-06 12:00:00'),
(9007, 23, 1, NULL, 5, 'This is the best phone I have ever used. Perfect. Great quality. Fast shipping. Five stars!', '2026-07-06 12:05:00'),
(9008, 2, 1, NULL, 5, 'Excellent product great service would buy again amazing value for money top quality highly recommend', '2026-07-06 12:10:00'),
(9009, 3, 1, 3, 4, 'Solid upgrade. Camera photos are sharp with great dynamic range. Comfort in hand is good despite the larger size. Battery could be better but acceptable for my usage.', '2026-07-08 16:45:00'),
(9010, 2, 1, NULL, 5, 'The screen display is gorgeous, 120Hz makes everything smooth. Build quality feels premium. Delivery was quick and well packaged.', '2026-07-09 08:20:00'),
(9011, 23, 1, NULL, 4, 'Good phone, performance is excellent and the design is sleek. Slightly expensive but you get what you pay for. Battery is decent.', '2026-07-10 19:10:00'),
(9012, 3, 1, NULL, 5, 'Fantastic device. The camera quality is outstanding, especially portrait mode. Screen is bright and vivid. Comfortable to hold. Highly recommend despite the price.', '2026-07-11 13:55:00'),

-- ============================================
-- Product 3 — Sony WH-1000XM5
-- ============================================
(9013, 2, 3, NULL, 5, 'Noise cancelling is industry leading. I use these on flights and they block out engine noise completely. Battery life is around 30 hours as advertised. Comfort is excellent even for long sessions.', '2026-07-02 15:30:00'),
(9014, 23, 3, NULL, 4, 'Great headphones. Sound quality is rich and balanced. Comfort is superb for long listening. Only issue is they do not fold as compactly as the previous model.', '2026-07-03 10:05:00'),
(9015, 3, 3, 2, 5, 'Best purchase this year. The battery lasts forever and the noise cancellation is superb. Build quality feels premium. Fast delivery and nice packaging.', '2026-07-04 12:40:00'),
(9016, 2, 3, NULL, 3, 'Sound is good but the design feels a bit plasticky for the price. Comfort is fine. Battery life is genuinely excellent. Mixed overall.', '2026-07-05 09:15:00'),
(9017, 23, 3, NULL, 5, 'WOW amazing headphones!!! Best sound quality ever great battery super comfortable must buy!!!', '2026-07-06 12:15:00'),
(9018, 3, 3, NULL, 5, 'Very good product. Good quality. Fast delivery. Good price. Recommend.', '2026-07-06 12:20:00'),
(9019, 23, 3, NULL, 5, 'Absolutely brilliant. The noise cancelling works wonders in my open-plan office. Battery easily lasts a week of commuting. Comfortable for hours.', '2026-07-07 17:25:00'),
(9020, 3, 3, NULL, 4, 'Excellent sound and build quality. The app EQ is useful. Comfort is good though they get warm after a few hours. Price is steep but worth it.', '2026-07-08 11:00:00'),
(9021, 2, 3, NULL, 2, 'Disappointed. The comfort is poor for my head size and they cause pressure after an hour. Sound quality is great but I cannot wear them long. Build quality feels fragile.', '2026-07-09 20:30:00'),
(9022, 23, 3, NULL, 5, 'Fantastic noise cancellation and the sound is crisp. Battery performance is outstanding. Delivery was quick and packaging was secure.', '2026-07-10 14:50:00'),
(9023, 3, 3, NULL, 4, 'Very good overall. Performance and sound are excellent. The design is sleek. Slightly expensive but the quality justifies it.', '2026-07-11 10:35:00'),
(9024, 2, 3, NULL, 5, 'Perfect headphones excellent quality amazing sound great battery life best price highly recommended five stars', '2026-07-11 10:40:00'),

-- ============================================
-- Product 2 — MacBook Air M3
-- ============================================
(9025, 2, 2, NULL, 5, 'Incredible performance for a fanless laptop. The screen display is bright and colour accurate. Battery lasts all day. Build quality is superb.', '2026-07-02 09:00:00'),
(9026, 23, 2, NULL, 4, 'Great laptop for everyday work. Performance is snappy and the design is beautiful. Only issue is the base 8GB RAM feels limiting sometimes.', '2026-07-03 16:20:00'),
(9027, 3, 2, 3, 5, 'The M3 chip is a beast. Handles video editing smoothly. Battery life is phenomenal. Screen is gorgeous. Worth the price.', '2026-07-04 11:10:00'),
(9028, 2, 2, NULL, 5, 'Best laptop ever!!! Great performance amazing battery super fast excellent quality buy now!!!', '2026-07-06 12:25:00'),
(9029, 23, 2, NULL, 5, 'Amazing product. Fast delivery. Great quality. Good price. Highly recommend to everyone.', '2026-07-06 12:30:00'),
(9030, 2, 2, NULL, 4, 'Excellent build quality and the keyboard is much improved. Performance is great for productivity. Battery could be better under heavy load.', '2026-07-07 13:45:00'),
(9031, 23, 2, NULL, 5, 'Love it. The screen display is stunning and the design is sleek and light. Comfort of typing is excellent. Fast delivery.', '2026-07-08 15:05:00'),
(9032, 3, 2, NULL, 3, 'Good machine but the price is high for 256GB storage. Performance is excellent though. Battery is solid. Packaging was minimal.', '2026-07-09 09:50:00'),
(9033, 2, 2, NULL, 5, 'Perfect for students. Battery lasts through a full day of classes. Performance handles everything. Screen is bright and clear.', '2026-07-10 18:15:00'),
(9034, 23, 2, NULL, 4, 'Very good laptop. Build quality is premium and performance is fast. Slightly expensive but reliable. Delivery was quick.', '2026-07-11 12:00:00'),

-- ============================================
-- Product 9 — iPad Air M2
-- ============================================
(9035, 2, 9, NULL, 5, 'Fantastic tablet. The screen display is gorgeous and bright. Performance with the M2 chip is effortless. Battery lasts all day. Great for drawing.', '2026-07-03 10:30:00'),
(9036, 23, 9, NULL, 4, 'Excellent tablet. Build quality is premium and the design is slim. Performance is smooth. Only complaint is the price of accessories.', '2026-07-04 14:10:00'),
(9037, 3, 9, NULL, 5, 'Great tablet amazing quality fast delivery best price excellent screen highly recommended!!!', '2026-07-06 12:35:00'),
(9038, 3, 9, 2, 5, 'Love the display and the performance. Battery life is excellent for watching videos. Comfort of holding is good. Highly recommend.', '2026-07-07 09:25:00'),
(9039, 23, 9, NULL, 3, 'Good but expensive. Screen is beautiful and performance is fast. Battery is decent. Packaging was nice but the price is high.', '2026-07-08 17:40:00'),
(9040, 2, 9, NULL, 4, 'Solid tablet. Camera is decent for video calls. Performance is snappy. Build quality feels great. Delivery was fast.', '2026-07-09 11:20:00'),
(9041, 3, 9, NULL, 5, 'Perfect for productivity. The screen is stunning and battery lasts long. Design is sleek and light. Comfort to use for hours.', '2026-07-10 15:55:00'),
(9042, 23, 9, NULL, 5, 'Excellent product great quality fast shipping good value amazing display five stars recommend', '2026-07-11 10:45:00'),

-- ============================================
-- Product 10 — Canon EOS R6 Mark II
-- ============================================
(9043, 2, 10, NULL, 5, 'Outstanding camera. Autofocus is lightning fast and accurate. Image quality is superb even at high ISO. Battery life is good. Build quality is weather sealed and solid.', '2026-07-02 13:00:00'),
(9044, 23, 10, NULL, 4, 'Excellent camera for professionals. The image quality is stunning and performance is reliable. Battery could be better. Price is high but justified.', '2026-07-03 15:35:00'),
(9045, 3, 10, NULL, 5, 'Best camera ever!!! Amazing quality great photos fast delivery excellent price buy now!!!', '2026-07-06 12:40:00'),
(9046, 3, 10, 3, 5, 'Superb. The autofocus tracks subjects perfectly. Screen display is clear and bright. Comfort of grip is excellent. Highly recommend.', '2026-07-07 10:10:00'),
(9047, 2, 10, NULL, 4, 'Great camera. Image quality is fantastic and the build feels durable. Battery drains faster than expected during video. Delivery was quick.', '2026-07-08 16:30:00'),
(9048, 23, 10, NULL, 5, 'Professional grade. Autofocus performance is incredible and image quality is top notch. Comfort is good for long shoots. Worth the price.', '2026-07-09 09:40:00'),
(9049, 3, 10, NULL, 4, 'Excellent overall. The camera produces beautiful images. Battery life is average. Build quality is solid. Packaging was secure.', '2026-07-10 14:20:00'),
(9050, 2, 10, NULL, 3, 'Good camera but very expensive. Image quality is excellent. Battery life is poor for video work. Performance otherwise great.', '2026-07-11 11:50:00');

-- Refresh product rating aggregates from the seeded reviews
UPDATE product p SET
  p.review_count = (SELECT COUNT(*) FROM review r WHERE r.product_id = p.id AND r.status = 1),
  p.rating = (SELECT COALESCE(AVG(r.rating), 0) FROM review r WHERE r.product_id = p.id AND r.status = 1)
WHERE p.id IN (1, 2, 3, 9, 10);

SELECT COUNT(*) AS reviews_seeded FROM review WHERE id BETWEEN 9001 AND 9100;
