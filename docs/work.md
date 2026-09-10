# VeriCart AI — How the System Works

A field guide to the moving parts of VeriCart AI: the **AI providers** (DeepSeek, Kimi, Hunyuan),
**Docker deployment**, and the **MySQL database** — and how they fit together.

---

## 1. The big picture

```
                  ┌──────────────────────────────────────────────┐
   Browser  ─────►│ Vue 3 SPA (Element Plus, Pinia, i18n en/zh)  │
   (user)         └──────────────────┬───────────────────────────┘
                                     │ REST /api (JSON, JWT in headers)
                                     ▼
                  ┌──────────────────────────────────────────────┐
                  │  Spring Boot backend  (port 8080)            │
                  │  Controller → Service → MyBatis Mapper       │
                  │  Spring Security (JWT + roles)               │
                  │  Scheduler (AiScheduler, every ~30s)         │
                  └───────┬──────────────────────────┬───────────┘
                          │ JDBC                     │ AI calls
                          ▼                          ▼
                  ┌───────────────┐        ┌─────────────────────────┐
                  │ MySQL 8       │        │ AI Gateway (multi-LLM)  │
                  │ (13 tables)   │        │  DeepSeek · Kimi · Hun. │
                  └───────────────┘        │  + mock fallback        │
                                           └─────────────────────────┘
```

Everything runs as **three Docker services** — `mysql`, `backend`, `frontend` — defined in
`docker-compose.yml` (or started manually on a dev machine).

---

## 2. The AI providers — DeepSeek, Kimi and Hunyuan

VeriCart never depends on a single model. A **Multi-LLM Gateway** (`AiGateway`) routes each task to
the model best suited for it, and gracefully falls back if a provider fails.

### Roles

| Task | Primary model | Why | Fallback |
| --- | --- | --- | --- |
| Fake-review detection | **DeepSeek** (`detectFakeReview`) | Strong reasoning & factuality | mock |
| Product authenticity reasoning / trust score | **DeepSeek** (`analyzeTrust`) | Overall trust judgement | mock |
| Sentiment analysis (per review + batch) | **Kimi** (`analyzeSentiment(Batch)`) | Reads tone well | mock |
| Product-scoped & general chat assistant | **Kimi**, else DeepSeek | Conversational quality | DeepSeek → mock |
| Recommendations / preferences | **Kimi** (`recommend`) | Personalisation | mock |
| Seller auto-reply to customer inquiries | **Kimi** (`answerProductQuestion`) | Store-assistant persona | mock |
| Review summarisation (Chinese-friendly) | **Hunyuan** (`generateReviewSummary`) | Strong zh summarisation | mock |

### How each provider is wired

Every provider is a dedicated client class in `com.vericart.ai`:

| Client | Models (defaults) | Base URL |
| --- | --- | --- |
| `DeepSeekClient` | `deepseek-v4-pro-0813` (docker) / `deepseek-chat` | TokenHub / `api.deepseek.com` |
| `KimiClient` | `kimi-k3` | TokenHub `/v1` / `api.moonshot.cn/v1` |
| `HunyuanClient` | `hy4-preview` | TokenHub `/v1` / `tokenhub.tencentmaas.com/v1` |

All three can be served by a **single Tencent Cloud TokenHub (international) key** — one key
authenticates `deepseek-v4-pro-0813`, `kimi-k3` and `hy4-preview`. The client classes expose the same
OpenAI-style chat-completion contract, so the gateway code never cares which company answers.

### Resilience

- Each client knows whether it is **configured** (`isConfigured()` — a real key present). If not, it
  returns a mock answer so demos work offline.
- `AiGateway` wraps every live call in try/catch: chat prefers Kimi and falls back to DeepSeek; a
  failed Hunyuan summary falls back to a mock summary; the whole trust-score pipeline tolerates one
  provider being down.
- **Mock mode** — `AI_MOCK_ENABLED=true` (or `ai.mock.enabled=true` when running the jar without env
  vars) makes every AI result deterministic and heuristic, so tests and the full UI work with no keys
  and no network.

### One real call, end to end

```
POST /api/ai/analyze/{productId}
  → AiService.analyzeProduct()
      ├─ DeepSeekClient.detectFakeReview(...)   per un-analysed review  (fake_probability)
      ├─ KimiClient.analyzeSentiment(...)       per un-analysed review  (sentiment, emotion)
      ├─ AiGateway.generateTrustScore()         DeepSeek analyzeTrust + Kimi sentiment batch
      ├─ HunyuanClient.generateReviewSummary()  readable summary text
      └─ persist: product.trust_score/trust_level/ai_summary, review AI columns, audit log
```

Each model is asked to reply in strict JSON, which is parsed and validated before being stored —
a malformed answer is treated as a failure and handled by the fallback chain.

---

## 3. Docker — one command to run everything

`docker-compose.yml` defines the whole stack:

| Service | Image / build | Port | Notes |
| --- | --- | --- | --- |
| `mysql` | `mysql:8.0` | `3306:3306` | `utf8mb4`; health-checked; data in the `mysql_data` volume |
| `backend` | `./backend` Dockerfile | `8080:8080` | Spring Boot, starts **only after** MySQL is healthy |
| `frontend` | `./frontend` Dockerfile | `80:80` | Built Vue SPA served by nginx, proxies `/api` to the backend |

Key behaviours:

1. **Database bootstrap** — `backend/sql/schema.sql` is mounted into
   `/docker-entrypoint-initdb.d/schema.sql`, so the MySQL container creates the schema and seed data
   on first start. The backend additionally runs `schema.sql` from its classpath
   (`spring.sql.init.mode: always`, `continue-on-error: true`) so local/`java -jar` runs also self-setup.
2. **Startup ordering** — `backend` uses `depends_on: mysql: condition: service_healthy`, so it never
   races a not-yet-ready database.
3. **Environment** — every secret has a safe `${VAR:-default}`:
   `DB_PASSWORD`, `JWT_SECRET`, `DEEPSEEK_API_KEY`, `KIMI_API_KEY`, `HUNYUAN_API_KEY`, model names and
   base URLs, plus `AI_MOCK_ENABLED`.
4. **Day-to-day**:

   ```bash
   docker compose up -d                 # start all three
   docker compose up -d --build backend # rebuild after backend changes
   docker compose logs -f backend       # watch Spring Boot + AI logs
   ```

---

## 4. The database — 13 tables, one trust lifecycle

The schema (`schema.sql`, MySQL 8 / InnoDB / `utf8mb4`) groups the whole commerce + AI domain:

### Commerce & catalogue
| Table | Purpose |
| --- | --- |
| `user` | Accounts with `role` (CUSTOMER / SELLER / ADMIN) and status |
| `category` | Hierarchical categories (`parent_id`), with image URLs |
| `product` | Listings: price, stock, JSON `images`/`specifications`/`variants`, **AI fields** (`trust_score`, `trust_level`, `ai_summary`, `fake_review_count`) |
| `cart_item` | Shopping cart (unique per user+product) |

### Orders & reviews (the trust layer)
| Table | Purpose |
| --- | --- |
| `orders` | Order lifecycle `PENDING→PAID→PROCESSING→SHIPPED→DELIVERED` |
| `order_item` | Line items (snapshot name/image/price) |
| `review` | Star rating + content + **AI verdict columns**: `sentiment`, `emotion`, `fake_probability`, `fake_reason`, `is_flagged`; `order_id` marks **Verified Purchase** |
| `wishlist` | Saved products |

### Support & platform
| Table | Purpose |
| --- | --- |
| `ai_analysis_log` | Every AI run (`task` = sentiment / fake_detection / summary / trust_score / recommendation) |
| `notification` | Bell notifications for users |
| `audit_log` | FR-069/070 traceability (LOGIN, ORDER_CREATED, REVIEW_CREATED, AI_ANALYSIS…) |
| `inquiry`, `inquiry_message` | Buyer↔seller Q&A with AI auto-reply |

### Design notes
- **JSON columns** store flexible payloads (product images/specs/variants, review images) so the
  schema stays stable while content varies.
- **Indexes** cover the hot paths: `product` by category/name/price and **`trust_score`**
  (sorting by trust), `review` by product/user, orders by user and `order_no`, categories by parent.
- **Foreign keys** keep referential integrity (cascade deletes from `user`, `orders`, `product`).
- **Tests** run on H2 in MySQL-compatibility mode (`MODE=MySQL`), so the same SQL works in CI without
  a MySQL server.

---

## 5. Request-flow cheat sheet

**A shopper writes a review**
1. `POST /api/reviews` (JWT) → saved instantly, `sentiment IS NULL` → UI shows "Pending AI".
2. Within ~30s `AiScheduler` picks it up (batch ≤ 20 products/cycle) and runs the AI pipeline
   (Section 2).
3. Review gets its verdict badge; the product's Trust Score and Trusted Rating are recomputed; the
   seller gets a notification; the action is audit-logged.

**A shopper asks the assistant "which laptop is most trustworthy?"**
1. `POST /api/ai/chat` with product context.
2. `AiGateway.chat` → Kimi (or DeepSeek) answers with trust data included; on any error it falls back
   through the chain so the user always gets an answer.

---

## 6. Manual (non-Docker) development

```bash
# Database: create schema (auto-run by the app on start too)
# Set env: DB_PASSWORD, JWT_SECRET, DEEPSEEK/KIMI/HUNYUAN keys, optionally AI_MOCK_ENABLED=true

cd backend
mvn spring-boot:run        # backend on :8080

cd frontend
npm install
npm run dev                # Vite dev server (default :5173), proxies /api → :8080
```

Tests:

```bash
cd backend  && mvn test            # JUnit 5 + H2
cd frontend && npx vitest run      # Vitest + Vue Test Utils
```

---

## 7. Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| AI calls return mock answers in prod | `AI_MOCK_ENABLED` is `true`; set it to `false` and provide keys |
| `401` from AI providers | Wrong TokenHub / provider key, or wrong `base-url` (region matters for TokenHub) |
| "Hunyuan summary failed" in logs | Expected resilience path — summary falls back to mock; check `HUNYUAN_API_KEY`/model |
| Backend can't reach MySQL in Docker | MySQL not healthy yet — `docker compose ps`; wait for `service_healthy` |
| Schema changes not applied | `spring.sql.init.mode` only runs DDL on startup; re-create the container/volume for a clean reseed |
| Port already in use | `8080` (backend), `80` (frontend), `3306` (mysql) — change the `ports` mapping in compose |
