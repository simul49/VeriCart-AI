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
│   │   ├── schema.sql        # Database DDL (10 tables)
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

**Frontend** (port 5173):

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 in your browser.

---

## Database

10 MySQL tables managing the full e-commerce trust lifecycle:

| Table              | Purpose                               |
| ------------------ | ------------------------------------- |
| `user`             | User accounts (buyers & sellers)      |
| `product`          | Product listings                      |
| `category`         | Product categories                    |
| `cart_item`        | Shopping cart items                   |
| `wishlist`         | User wishlists                        |
| `orders`           | Purchase orders                       |
| `order_item`       | Line items within orders              |
| `review`           | Product reviews & ratings             |
| `ai_analysis_log`  | AI review analysis results            |
| `notification`     | System notifications                  |

Testing uses H2 in-memory with MySQL compatibility mode (`MODE=MySQL`).

---

## API Endpoints

Base URL: `http://localhost:8080/api`

| Method | Endpoint                  | Description              |
| ------ | ------------------------- | ------------------------ |
| POST   | `/auth/register`          | User registration        |
| POST   | `/auth/login`             | User login (JWT)         |
| GET    | `/products`               | List products            |
| GET    | `/products/{id}`          | Product detail           |
| GET    | `/products/{id}/reviews`  | Product reviews          |
| POST   | `/reviews/analyze`        | AI review analysis       |
| GET    | `/cart`                   | View cart                |
| POST   | `/cart/add`               | Add to cart              |
| GET    | `/wishlist`               | View wishlist            |
| POST   | `/orders`                 | Place order              |
| GET    | `/orders/{id}`            | Order detail             |

---

## AI Features

The platform integrates 3 LLM providers through a unified gateway:

- **DeepSeek** — Core engine for review sentiment analysis and authenticity scoring
- **Qwen (Alibaba)** — Product description generation and multilingual support
- **Hunyuan (Tencent)** — Lightweight fallback for basic analysis tasks

Each provider is configured in `application.yml` with environment-variable API keys.

---

## Running Tests

**Backend** (JUnit + H2 in-memory):

```bash
cd backend
mvn test
```

**Frontend** (Vitest):

```bash
cd frontend
npm test
```

---

## License

This project is developed for academic/learning purposes as part of an internship training program.
