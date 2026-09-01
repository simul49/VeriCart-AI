# VeriCart AI — API Documentation

Base URL: `http://localhost:8080/api`

All responses use a uniform wrapper:

```json
{ "code": 200, "message": "ok", "data": { ... } }
```

Errors: `{ "code": 400|401|403|404|500, "message": "description", "data": null }`

## Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register a new customer. Body: `{username, email, password}` |
| POST | `/auth/login` | Public | Login. Body: `{email, password}` → returns JWT token + user info |
| GET | `/` | Public | Health check / welcome |

All protected endpoints expect the header `Authorization: Bearer <token>`.

Roles: `CUSTOMER` (default), `SELLER`, `ADMIN`.

---

## Products

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/products` | Public | List products. Optional query: `categoryId`, `sort` (e.g. `price_asc`), `keyword` (searches by keyword) |
| GET | `/products/{id}` | Public | Product detail (includes AI trust score, rating, review count) |
| GET | `/products/page` | Public | **Paginated** product list. Query: `page` (1-based), `size`, `categoryId`, `keyword`, `sort`. Returns `{items, total, page, size, totalPages}` |
| GET | `/products/compare` | Public | **AI comparison** of up to 4 products. Query: `ids=1,2,3`. Returns products + `winnerId` + explainable verdict |
| GET | `/categories` | Public | List all product categories |
| POST | `/seller/products` | SELLER/ADMIN | Create a product (seller dashboard) |
| PUT | `/seller/products/{id}` | SELLER/ADMIN | Update a product |
| DELETE | `/seller/products/{id}` | SELLER/ADMIN | Soft-delete a product |

---

## Reviews (AI-analysed)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/reviews/product/{productId}` | Public | List reviews for a product, each with AI verdict (`sentiment`, `emotion`, `fakeProbability`, `isFlagged`) and `verified` purchase badge |
| GET | `/reviews/product/{productId}/trust` | Public | **Trust metrics**: `rawRating`, `trustedRating`, `ratingDelta`, `flaggedReviews`, `totalReviews` |
| GET | `/reviews/product/{productId}/mine` | USER | Reviews written by the current user for this product |
| GET | `/reviews/my` | USER | All reviews written by the current user |
| POST | `/reviews` | USER | Create a review. Body: `{productId, rating(1-5), content, images?, orderId?}` |
| PUT | `/reviews/{id}` | USER | Edit own review (48-hour window, FR-034) |
| POST | `/reviews/{id}/report` | USER | Report a review as suspicious (FR-037) |
| DELETE | `/reviews/{id}` | USER | Delete own review |

---

## AI Analysis

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/ai/analyze/{productId}` | Public | Run the full multi-LLM pipeline on a product: sentiment, fake detection, trust score + **explainable factors**, topic extraction with positive ratios |
| POST | `/ai/chat` | Public | Conversational assistant (simulated) |
| POST | `/ai/batch` | Public | Batch analysis of multiple products |
| POST | `/ai/recommend` | Public | Product recommendations (body: optional `userId`) |

---

## Cart

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/cart` | USER | Current user's cart |
| POST | `/cart/add` | USER | Add item. Body: `{productId, quantity}` |
| PUT | `/cart/{id}` | USER | Update item quantity |
| DELETE | `/cart/{id}` | USER | Remove item |
| DELETE | `/cart/clear` | USER | Clear cart |
| GET | `/cart/count` | USER | Number of items in cart |

---

## Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/orders` | USER | Place an order. Body: `{items: [{productId, quantity}], address, paymentMethod}`. Decrements stock, notifies user |
| GET | `/orders` | USER | Current user's orders |
| GET | `/orders/{id}` | USER | Order detail |
| GET | `/orders/{id}/items` | USER | Order items |
| PUT | `/orders/{id}/cancel` | USER | Cancel order (restores stock) |

---

## User Profile

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/user/profile` | USER | Current profile |
| PUT | `/user/profile` | USER | Update profile |
| PUT | `/user/password` | USER | Change password |

---

## Wishlist

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/wishlist` | USER | List wishlist items |
| POST | `/wishlist/add` | USER | Add product. Body: `{productId}` |
| DELETE | `/wishlist/{productId}` | USER | Remove from wishlist |
| GET | `/wishlist/check/{productId}` | USER | Is the product in the wishlist? |

---

## Notifications

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/notifications` | USER | Notifications (newest first) |
| GET | `/notifications/unread-count` | USER | Unread count (used by the navbar bell) |
| PUT | `/notifications/{id}/read` | USER | Mark one as read |
| PUT | `/notifications/read-all` | USER | Mark all as read |
| DELETE | `/notifications/{id}` | USER | Delete a notification |

---

## Seller Dashboard

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/seller/products` | SELLER/ADMIN | Products owned by the seller |
| GET | `/seller/orders` | SELLER/ADMIN | Orders containing the seller's products |
| GET | `/seller/orders/{id}/items` | SELLER/ADMIN | Line items of an order (seller scope) |
| PUT | `/seller/orders/{id}/status` | SELLER/ADMIN | Update fulfilment status (PENDING → SHIPPED → DELIVERED) |
| GET | `/seller/reviews` | SELLER/ADMIN | AI-analysed reviews for the seller's products |
| GET | `/seller/stats` | SELLER/ADMIN | Sales + review stats for the dashboard |

---

## Product Inquiries (Message the Store Owner)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/messages` | USER | Start a new conversation about a product. Body: `{ productId, message }`. **The AI assistant auto-replies immediately** (async) while the seller is unavailable — the reply is appended to the thread and the customer is notified. |
| POST | `/messages/{id}/send` | USER | Send a follow-up in an existing conversation (multi-turn chat). Body: `{ message }`. The AI replies again instantly. |
| GET | `/messages/my` | USER | The customer's conversations. Each item is a thread: `{ id, productName, status, messages: [{ id, sender: USER/AI/SELLER, content, isRead, createdAt }] }` |
| GET | `/seller/messages` | SELLER/ADMIN | Store owner's inbox — each thread includes customer + product info and the full `messages` array |
| GET | `/seller/messages/unread-count` | SELLER/ADMIN | Number of unread inquiries |
| PUT | `/seller/messages/{id}/reply` | SELLER/ADMIN | Append a personal reply to a thread. Body: `{ reply }`. Marks the thread read and notifies the customer. |
| PUT | `/seller/messages/{id}/read` | SELLER/ADMIN | Mark a conversation as read |

---

## Admin

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/admin/dashboard` | ADMIN | KPIs: revenue, orders, users, flagged reviews |
| GET | `/admin/users` | ADMIN | All users |
| GET | `/admin/products` | ADMIN | All products (incl. soft-deleted) |
| GET | `/admin/orders` | ADMIN | All orders |
| PUT | `/admin/orders/{id}/status` | ADMIN | Update any order status |
| GET | `/admin/reviews/flagged` | ADMIN | Reviews flagged for moderation |
| GET | `/admin/audit-logs` | ADMIN | **FR-069**: activity audit log. Query: `category`, `limit` |
| GET | `/admin/audit-logs/errors` | ADMIN | **FR-070**: recorded exceptions |
| GET | `/admin/audit-logs/stats` | ADMIN | Audit totals (`totalEntries`, `totalErrors`) |

---

## Example: Trust Metrics Response

```json
{
  "code": 200,
  "data": {
    "productId": 1,
    "rawRating": 4.33,
    "trustedRating": 4.11,
    "ratingDelta": -0.22,
    "flaggedReviews": 3,
    "totalReviews": 12
  }
}
```

## Example: AI Analysis Response (excerpt)

```json
{
  "code": 200,
  "data": {
    "productId": 1,
    "trustScore": 76,
    "trustLevel": "High",
    "totalReviews": 12,
    "fakeReviewCount": 3,
    "explanation": [
      { "factor": "Review Authenticity", "weight": 30, "score": 82,
        "contribution": 25, "detail": "3 of 12 reviews appear suspicious" }
    ],
    "topics": [
      { "topic": "Battery Life", "mentions": 6, "positiveRatio": 0.83 }
    ]
  }
}
```

## Frontend ↔ Backend

The Vue 3 frontend (dev server on port **3000**) proxies `/api` → `http://localhost:8080` via `vite.config.js`, so all calls use relative `/api/...` URLs in the browser.
