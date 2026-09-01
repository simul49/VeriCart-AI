# VeriCart AI — Database Schema

- **Engine**: MySQL 8.0 (`utf8mb4` / `utf8mb4_unicode_ci`)
- **Database**: `vericart` on `localhost:3306`
- **Source of truth**: `backend/src/main/resources/schema.sql` (re-runnable, idempotent with `CREATE TABLE IF NOT EXISTS`)
- **Test database**: H2 in-memory using `backend/src/main/resources/schema-h2.sql` (activated by `application-test.yml`)

## Entity Relationship Overview

```
user ──< product (seller_id)          user ──< cart_item >── product
user ──< orders ──< order_item >── product
user ──< review >── product (order_id → orders)
user ──< wishlist >── product
user ──< notification
user ──< audit_log (user_id nullable)
user ──< inquiry >── product (customer questions → store owner)
product ──< ai_analysis_log (target_id polymorphic)
category ◄── product (category_id)     category ──< category (parent_id)
```

## Tables (12)

### 1. `user`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | auto-increment |
| username | VARCHAR(50) | UNIQUE, NOT NULL |
| email | VARCHAR(100) | UNIQUE, NOT NULL |
| password | VARCHAR(255) | BCrypt hash |
| phone | VARCHAR(20) | |
| avatar | VARCHAR(500) | |
| role | VARCHAR(20) | `CUSTOMER` / `SELLER` / `ADMIN` (default CUSTOMER) |
| status | TINYINT | 0=disabled, 1=active |
| created_at / updated_at | DATETIME | |

Indexes: `idx_email`, `idx_role`.

### 2. `category`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR(100) | |
| parent_id | BIGINT | self-referencing FK, NULL = top level |
| sort_order | INT | |
| status | TINYINT | |
| created_at | DATETIME | |

### 3. `product`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR(200) | |
| description | TEXT | |
| price | DECIMAL(12,2) | |
| stock | INT | |
| category_id | BIGINT | FK → category |
| brand | VARCHAR(100) | |
| images | JSON | array of URLs |
| specifications | JSON | |
| rating | DECIMAL(3,2) | aggregate of visible reviews |
| review_count | INT | |
| trust_score | INT | AI-generated 0–100 |
| trust_level | VARCHAR(20) | Low / Medium / High / Excellent |
| ai_summary | TEXT | AI review summary |
| ai_summary_time | DATETIME | |
| fake_review_count | INT | count of AI-flagged reviews |
| status | TINYINT | 0=inactive, 1=active |
| seller_id | BIGINT | FK → user (the seller) |
| created_at / updated_at | DATETIME | |

Indexes: `idx_category`, `idx_name`, `idx_price`, `idx_trust_score`.

### 4. `cart_item`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | FK → user, CASCADE |
| product_id | BIGINT | FK → product, CASCADE |
| quantity | INT | |
| created_at / updated_at | DATETIME | |

Unique: `uk_user_product (user_id, product_id)` — one row per product per user.

### 5. `orders`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| order_no | VARCHAR(32) | UNIQUE |
| user_id | BIGINT | FK → user, CASCADE |
| total_amount | DECIMAL(12,2) | |
| status | ENUM | `PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED` |
| shipping_name / phone / address | VARCHAR | |
| payment_method | VARCHAR(50) | `SIMULATED` |
| note | VARCHAR(500) | |
| created_at / updated_at | DATETIME | |

Indexes: `idx_user`, `idx_order_no`.

### 6. `order_item`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| order_id | BIGINT | FK → orders, CASCADE |
| product_id | BIGINT | FK → product, NO ACTION |
| product_name | VARCHAR(200) | denormalised snapshot |
| product_image | VARCHAR(500) | |
| price | DECIMAL(12,2) | |
| quantity | INT | |

### 7. `review`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | FK → user, CASCADE |
| product_id | BIGINT | FK → product, CASCADE |
| order_id | BIGINT | nullable — set ⇒ **verified purchase** badge |
| rating | TINYINT | 1–5 (CHECK constraint) |
| content | TEXT | |
| images | JSON | |
| sentiment | VARCHAR(20) | AI: POSITIVE / NEUTRAL / NEGATIVE |
| emotion | VARCHAR(30) | AI emotion |
| fake_probability | DECIMAL(5,2) | AI 0–100 |
| fake_reason | TEXT | why AI flagged it |
| is_flagged | TINYINT | moderation flag |
| status | TINYINT | 0=hidden, 1=visible |
| created_at / updated_at | DATETIME | |

Indexes: `idx_product`, `idx_user`.

### 8. `wishlist`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | FK → user, CASCADE |
| product_id | BIGINT | FK → product, CASCADE |
| created_at | DATETIME | |

Unique: `uk_user_product`.

### 9. `ai_analysis_log`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| target_type | VARCHAR(20) | REVIEW / PRODUCT / RECOMMENDATION |
| target_id | BIGINT | polymorphic target |
| ai_model | VARCHAR(50) | deepseek / qwen / hunyuan |
| task | VARCHAR(50) | sentiment / fake_detection / summary / trust_score / recommendation |
| request / response | TEXT | payloads |
| processing_time_ms | INT | |
| status | VARCHAR(20) | SUCCESS / FAILED |
| error_message | TEXT | |
| created_at | DATETIME | |

### 10. `notification`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | FK → user, CASCADE |
| title | VARCHAR(200) | |
| content | TEXT | |
| type | VARCHAR(50) | ORDER / REVIEW / SYSTEM / AI |
| is_read | TINYINT | |
| created_at | DATETIME | |

Index: `idx_user_read (user_id, is_read)`.

### 11. `audit_log` — FR-069 / FR-070
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT | NULL for guest actions |
| username | VARCHAR(50) | denormalised for readability |
| action | VARCHAR(50) | LOGIN, REGISTER, ORDER_CREATED, ORDER_CANCELLED, ORDER_STATUS_CHANGED, REVIEW_CREATED, REVIEW_UPDATED, REVIEW_DELETED, REVIEW_REPORTED, AI_ANALYSIS, AI_ANALYSIS_FAILED, SYSTEM_ERROR |
| category | VARCHAR(20) | LOGIN / ORDER / REVIEW / AI / ERROR |
| target_type | VARCHAR(30) | PRODUCT / ORDER / REVIEW / USER |
| target_id | BIGINT | |
| detail | TEXT | human-readable |
| ip | VARCHAR(45) | |
| is_error | TINYINT | 1 = error entry (FR-070) |
| created_at | DATETIME | |

Indexes: `idx_category`, `idx_error`, `idx_created`.

### 12. `inquiry` — product messages to the store owner
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| product_id | BIGINT | FK → product, CASCADE |
| user_id | BIGINT | FK → user (customer), CASCADE |
| seller_id | BIGINT | store owner (denormalised from product) |
| message | TEXT | first customer question (kept for reference) |
| reply | TEXT | legacy single-answer snapshot (multi-turn chat uses `inquiry_message`) |
| reply_source | VARCHAR(20) | legacy who-wrote-the-reply snapshot |
| status | VARCHAR(20) | OPEN / REPLIED / CLOSED |
| is_read | TINYINT | 1 = owner has seen it |

**`inquiry_message`** — chat turns inside an inquiry thread (multi-turn conversations):

| Column | Type | Notes |
|---|---|---|
| id | BIGINT | PK |
| inquiry_id | BIGINT | FK → inquiry.id, cascade delete |
| sender | VARCHAR(10) | USER / AI / SELLER |
| content | TEXT | the message text |
| is_read | TINYINT | 1 = the other side has seen it |
| created_at | TIMESTAMP | turn time |
| created_at | DATETIME | |
| updated_at | DATETIME | |

Indexes: `idx_inquiry_seller (seller_id, is_read)`, `idx_inquiry_user (user_id)`.

---

## Seed Data

- **Admin user**: `admin@vericart.ai` / `admin123` (role ADMIN) — also used as the demo seller (seller_id=1 on all seeded products).
- **15 categories** (5 top-level + 10 sub-categories).
- **20 products** with real Unsplash images.
- **50 demo reviews** in `backend/sql/seed-reviews.sql` (ids 9001–9100) across products 1, 2, 3, 9, 10 — including 8 deliberately fake reviews so the AI fake-detection has real signal. AI fields start NULL and are filled by running `POST /api/ai/analyze/{productId}`.
