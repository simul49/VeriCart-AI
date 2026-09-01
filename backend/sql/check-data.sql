-- Diagnostic: list users and products available for seeding reviews
SELECT id, username, email, role FROM `user`;
SELECT id, name, seller_id, review_count, trust_score FROM product WHERE status = 1 ORDER BY id;
SELECT COUNT(*) AS existing_reviews FROM review;
