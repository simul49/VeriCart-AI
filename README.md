# VeriCart AI

**AI-Powered E-Commerce Trust Platform**

VeriCart AI is an intelligent e-commerce platform that leverages multiple LLM providers (DeepSeek, Qwen, Hunyuan) to deliver AI-driven product reviews analysis, authenticity verification, and smart shopping recommendations — building trust between buyers and sellers.

---

## Tech Stack

| Layer       | Technology                                                      |
| ----------- | --------------------------------------------------------------- |
| **Backend** | Spring Boot 3.2.0, Java 17, MyBatis 3.0.3, Spring Security + JWT |
| **Frontend**| Vue 3.4, Vite 5, Pinia, Vue Router, Element Plus, Axios         |
| **Database**| MySQL 8.0 (production), H2 (testing)                            |
| **AI**      | DeepSeek (deepseek-chat), Qwen (qwen-plus), Hunyuan (hunyuan-lite) |
| **Infra**   | Docker Compose                                                  |

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
- **Docker & Docker Compose** (optional)

### 1. Clone & Environment

```bash
git clone https://github.com/simul49/VeriCart-AI.git
cd VeriCart-AI
```

Set your API keys and database password via environment variables (or create a `.env` file):

```bash
export DB_PASSWORD=your_mysql_password
export DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxx
export QWEN_API_KEY=sk-xxxxxxxxxxxxxxxx
export HUNYUAN_API_KEY=sk-xxxxxxxxxxxxxxxx
export JWT_SECRET=your_jwt_secret
```

### 2. Start with Docker (Recommended)

```bash
docker-compose up -d
```

This starts MySQL, the Spring Boot backend (port 8080), and the Vue frontend (port 80) in one command.

### 3. Manual Start

**Database** — Create the MySQL database:

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

**Frontend** (port 3000 — see `vite.config.js`):

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:3000 in your browser.

---

## Database

11 MySQL tables managing the full e-commerce trust lifecycle (see [docs/Database-Schema.md](docs/Database-Schema.md)):

| Table              | Purpose                                   |
| ------------------ | ----------------------------------------- |
| `user`             | User accounts (buyers, sellers, admins)   |
| `product`          | Product listings + AI trust score fields  |
| `category`         | Product categories (hierarchical)         |
| `cart_item`        | Shopping cart items                       |
| `wishlist`         | User wishlists                            |
| `orders`           | Purchase orders (status lifecycle)        |
| `order_item`       | Line items within orders                  |
| `review`           | Product reviews + AI analysis columns     |
| `ai_analysis_log`  | AI review analysis results                |
| `notification`     | System notifications (bell + polling)     |
| `audit_log`        | Activity & error audit (FR-069/070)       |

Testing uses H2 in-memory with MySQL compatibility mode (`MODE=MySQL`).

---

## API Endpoints

Base URL: `http://localhost:8080/api` — full reference in [docs/API-Documentation.md](docs/API-Documentation.md) (49 endpoints).

| Method | Endpoint                          | Auth    | Description                          |
| ------ | --------------------------------- | ------- | ------------------------------------ |
| POST   | `/auth/register`                  | Public  | User registration                    |
| POST   | `/auth/login`                     | Public  | User login (JWT)                     |
| GET    | `/products`                       | Public  | List products (filter/sort/search)   |
| GET    | `/products/{id}`                  | Public  | Product detail + trust score         |
| GET    | `/products/page`                  | Public  | **Paginated** product list           |
| GET    | `/products/compare`               | Public  | **AI comparison** of 4 products      |
| GET    | `/categories`                     | Public  | List categories                      |
| POST   | `/ai/analyze/{productId}`         | Public  | **Explainable AI review analysis**   |
| POST   | `/ai/chat` / `/ai/batch` / `/ai/recommend` | Public | AI assistant / batch / recommendations |
| GET    | `/reviews/product/{productId}`    | Public  | Reviews with AI verdicts             |
| GET    | `/reviews/product/{productId}/trust` | Public | Raw vs **trusted rating** metrics   |
| POST   | `/reviews`                        | USER    | Create review                        |
| PUT    | `/reviews/{id}`                   | USER    | Edit review (48h window)             |
| POST   | `/reviews/{id}/report`            | USER    | Report suspicious review             |
| GET    | `/reviews/my`                     | USER    | My reviews                           |
| GET    | `/cart` + `/cart/add` + `/cart/count` + ... | USER | Cart management                 |
| POST   | `/orders`                         | USER    | Place order (stock decrement)        |
| GET    | `/orders` + `/orders/{id}` + ...  | USER    | Order management                     |
| GET    | `/wishlist` + ...                 | USER    | Wishlist management                  |
| GET    | `/notifications` + `/unread-count` + ... | USER | Notification centre              |
| GET    | `/user/profile` + ...             | USER    | Profile & password                   |
| GET    | `/seller/...`                     | SELLER  | Seller dashboard (products, orders, reviews, stats) |
| GET    | `/admin/...`                      | ADMIN   | Admin dashboard, moderation, **audit logs** |

---

## AI Features

The platform integrates 3 LLM providers through a unified gateway (`AiGateway`):

- **DeepSeek** — Sentiment analysis and fake-review detection
- **Qwen (Alibaba)** — Topic extraction and positive-ratio scoring
- **Hunyuan (Tencent)** — Fallback + cross-checking when another provider fails

Key AI capabilities:

- **Explainable Trust Score** — 0–100 score with 5 weighted factors (Authenticity 30 / Sentiment 30 / Verified 20 / Volume 10 / Consistency 10) and plain-English reasons for each
- **Fake Review Detection** — flags spam / purchased reviews; removed from the **Trusted Rating** so scores cannot be inflated
- **Topic Extraction** — battery, price, comfort, delivery… with per-topic positive ratio
- **Multi-LLM Redundancy** — automatic fallback if a provider errors

**Mock mode** is enabled by default (`ai.mock.enabled=true`) so the full pipeline runs offline with realistic heuristic results (no API keys needed). To use real LLMs, set `AI_MOCK_ENABLED=false` and provide the provider API keys.

## Demo Credentials

| Role    | Email                 | Password       |
| ------- | --------------------- | -------------- |
| Admin   | `admin@vericart.ai`   | `admin123`     |
| Seller  | `admin@vericart.ai`   | `admin123`     |
| Customer| `john@vericart.com`   | `password123`  |

Demo data includes **50 seeded reviews** (8 deliberately fake) across 5 products — run `POST /api/ai/analyze/{productId}` (or click "Refresh Analysis" on a product page) to watch the AI detect them.

> Note: `.env.example` is not present yet — API keys are read from environment variables directly (see `application.yml`).

---

## Running Tests

**Backend** — 74 tests across 10 test classes (JUnit 5 + H2 in-memory):

```bash
cd backend
mvn test
```

**Frontend** — 11 tests (Vitest + Vue Test Utils + jsdom):

```bash
cd frontend
npx vitest run
```

Note: Mockito 5.16.1 + ByteBuddy 1.15.11 are pinned in `backend/pom.xml` for Java 24 runtime compatibility.

---

## License

This project is developed for academic/learning purposes as part of an internship training program.
