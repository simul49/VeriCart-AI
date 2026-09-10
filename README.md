# VeriCart AI

**AI-Powered E-Commerce Trust Platform**

VeriCart AI is an intelligent e-commerce platform that uses multiple LLM providers to deliver AI-driven review analysis, authenticity verification, and smart shopping recommendations — building trust between buyers and sellers. Instead of blindly trusting star averages, VeriCart shows customers a **Trust Score** that accounts for fake reviews, sentiment, and purchase verification.

---

## Why VeriCart AI

Online reviews are easy to manipulate. A product with a 4.8-star average might be propped up by paid or bot-written reviews. VeriCart AI analyses every review with language models to decide how much the ratings can actually be believed, then surfaces that verdict to shoppers in plain language.

**Core differentiators**

- **Explainable Trust Score** — a single 0–100 number, with the reasons behind it.
- **Fake-review detection** — suspicious reviews are flagged and excluded from the rating you see.
- **AI shopping assistant** — natural-language product search and comparisons across the whole catalogue.
- **Multi-provider AI** — resilient by design, with automatic fallback between providers.

---

## Tech Stack

| Layer | Technology |
| ----------- | --------------------------------------------------------------- |
| **Backend** | Spring Boot 3.2, Java 17, MyBatis 3, Spring Security + JWT |
| **Frontend**| Vue 3, Vite, Pinia, Vue Router, Element Plus, Axios |
| **Database**| MySQL 8.0 (production), H2 (testing) |
| **AI** | DeepSeek, Kimi (Moonshot), Hunyuan (Tencent) via a unified Tencent Cloud TokenHub gateway |
| **Infra** | Docker Compose |

---

## Features

- **AI Trust Score** — 0–100 score computed from 5 weighted factors (see below).
- **Fake & Paid Review Detection** — DeepSeek flags suspicious reviews; they are removed before the visible "Trusted Rating" is averaged.
- **Sentiment Analysis** — Kimi reads the tone behind each review (positive / negative).
- **Topic Extraction** — surfaces what buyers say about battery, price, comfort, delivery, etc., with per-topic positivity.
- **AI Shopping Assistant** — ask in plain language ("a cheap red dress for a wedding guest") and get matched products; compare up to 4 products side by side.
- **Roles & Dashboards** — buyers (cart, wishlist, orders, reviews), sellers (product & order management), and admins (moderation, audit logs).

---

## How the Trust Score works

For every product, each review is analysed by the AI:

1. **DeepSeek** runs fake / paid-review detection and returns a probability; reviews above the threshold are flagged.
2. **Kimi** reads the **sentiment** (positive / negative) of the review text.

The final 0–100 score is a weighted blend of five factors:

| Factor | Weight | Formula |
| --- | --- | --- |
| Review Authenticity | 30% | `(total − flagged) / total × 100` |
| Customer Sentiment | 30% | `positive / total × 100` |
| Verified Purchases | 20% | `verified / total × 100` |
| Review Volume | 10% | `min(100, total / 50 × 100)` |
| Rating Consistency | 10% | `max(0, 100 − stdDev × 40)` |

Flagged reviews are **removed** before the visible **Trusted Rating** is averaged — that is why the Trusted Rating is lower than the raw star average. The score recalculates automatically whenever new reviews arrive.

---

## Project Structure

```
VeriCart AI/
├── backend/                  # Spring Boot backend
│   ├── src/main/java/com/vericart/
│   │   ├── config/           # Security, AI, CORS configuration
│   │   ├── controller/       # REST API controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # MyBatis entity classes
│   │   ├── mapper/           # MyBatis mapper interfaces
│   │   ├── service/          # Business logic & AI gateway
│   │   └── util/             # Utility classes
│   ├── src/main/resources/
│   │   ├── mapper/           # MyBatis XML SQL mappings
│   │   ├── schema.sql        # Database DDL (11 tables)
│   │   └── application.yml   # Main configuration
│   └── src/test/             # JUnit tests (H2 in-memory)
├── frontend/                 # Vue 3 SPA frontend
│   ├── src/
│   │   ├── components/       # Reusable Vue components
│   │   ├── views/            # Page-level views
│   │   ├── router/           # Vue Router config
│   │   ├── stores/           # Pinia state stores
│   │   └── api/              # Axios API clients
│   └── vite.config.js
├── docs/                     # Project documentation & reports
├── PRD/                      # Product requirements documents
├── docker-compose.yml        # Full-stack Docker deployment
└── README.md
```

---

## Quick Start

### Prerequisites

- **Java 17** + Maven 3.8+
- **Node.js 18+** + npm
- **MySQL 8.0** (local or Docker)
- **Docker & Docker Compose** (recommended)

### 1. Clone

```bash
git clone https://github.com/simul49/VeriCart-AI.git
cd VeriCart-AI
```

### 2. Environment

All AI providers are served through a single **Tencent Cloud TokenHub (international)** key. Configure the following in your environment (or a `.env` file):

```bash
# Database
export DB_PASSWORD=your_mysql_password

# Tencent Cloud TokenHub (single key serves kimi-k3, hy4-preview, deepseek-v4-pro-0813)
export DEEPSEEK_API_KEY=YOUR_TOKENHUB_KEY
export KIMI_API_KEY=YOUR_TOKENHUB_KEY
export HUNYUAN_API_KEY=YOUR_TOKENHUB_KEY

# Auth
export JWT_SECRET=your_jwt_secret
```

To run fully offline without any API key, enable mock mode:

```bash
export AI_MOCK_ENABLED=true
```

Mock mode uses realistic heuristics so the entire pipeline (analysis, scoring, chat) runs without network calls.

### 3. Start with Docker (recommended)

```bash
docker-compose up -d
```

This starts MySQL, the Spring Boot backend (port 8080), and the Vue frontend (served on port 80) together.

### 4. Manual start

**Database** — create the schema:

```sql
CREATE DATABASE IF NOT EXISTS vericart
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tables are auto-created by `schema.sql` on startup.

**Backend** (port 8080):

```bash
cd backend
mvn spring-boot:run
```

**Frontend** (Vite dev server):

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 in your browser.

---

## Database

11 MySQL tables manage the full e-commerce trust lifecycle:

| Table | Purpose |
| --- | --- |
| `user` | User accounts (buyers, sellers, admins) |
| `product` | Product listings + AI trust score fields |
| `category` | Product categories (hierarchical) |
| `cart_item` | Shopping cart items |
| `wishlist` | User wishlists |
| `orders` | Purchase orders (status lifecycle) |
| `order_item` | Line items within orders |
| `review` | Product reviews + AI analysis columns |
| `ai_analysis_log` | AI review analysis results |
| `notification` | System notifications (bell + polling) |
| `audit_log` | Activity & error audit |

Testing uses H2 in-memory with MySQL compatibility mode.

---

## API Endpoints

Base URL: `http://localhost:8080/api` (full reference in `docs/API-Documentation.md`).

| Method | Endpoint | Auth | Description |
| --- | --- | --- | --- |
| POST | `/auth/register` | Public | User registration |
| POST | `/auth/login` | Public | User login (JWT) |
| GET | `/products` | Public | List products (filter/sort/search) |
| GET | `/products/{id}` | Public | Product detail + trust score |
| GET | `/products/compare` | Public | AI comparison of up to 4 products |
| POST | `/ai/analyze/{productId}` | Public | Explainable AI review analysis |
| POST | `/ai/chat` | Public | AI shopping assistant |
| POST | `/ai/recommend` | Public | AI product recommendations |
| GET | `/reviews/product/{id}/trust` | Public | Raw vs trusted rating metrics |
| POST | `/reviews` | USER | Create review |
| GET | `/cart` | USER | Cart management |
| POST | `/orders` | USER | Place order |
| GET | `/seller/...` | SELLER | Seller dashboard |
| GET | `/admin/...` | ADMIN | Admin dashboard, moderation, audit logs |

---

## Demo Credentials

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@vericart.ai` | `admin123` |
| Seller | `seller@vericart.ai` | `seller123` |
| Customer | `john@vericart.com` | `password123` |

Demo data includes seeded reviews (some deliberately fake) across several products. Run `POST /api/ai/analyze/{productId}` — or click **Refresh Analysis** on a product page — to watch the AI detect them.

---

## Running Tests

**Backend** — JUnit 5 + H2 in-memory:

```bash
cd backend
mvn test
```

**Frontend** — Vitest + Vue Test Utils:

```bash
cd frontend
npx vitest run
```

---

## License

This project is developed for academic / learning purposes as part of an internship training program.
