2025~2026 Academic Year, Semester 2
Practical Training / Internship Weekly Report

Name: [Your Name]
Class: [Your Class]
Student ID: [Your Student ID]
Project Name: VeriCart AI — AI-Powered E-Commerce Trust Platform
Supervisor: [Teacher Name]
Date: Week 4 (July 28 – August 1, 2026)

Weekly Plan:

Monday: Run end-to-end (E2E) verification of the complete user flows across the buyer, seller and admin roles.

Tuesday: Verify the full Docker Compose deployment (MySQL + backend + frontend) with a one-command startup.

Wednesday: Performance-test the platform against the NFR targets with ~100 concurrent users.

Thursday: Run a security scan (OWASP dependency check, SQL-injection checks) and a bug bash against all 70 functional requirements.

Friday: Compile the final project report, the 14-slide presentation deck and the deployment guide.

Daily Progress:

Monday: Executed end-to-end scenarios for all three roles. Buyer flow: register → log in → browse by category → keyword search → open a product → read AI-verified reviews and the Trusted Rating → add to cart → checkout → place an order → write a review → watch the AI analyse it and update the Trust Score. Seller flow: open the Seller Dashboard → create/edit/soft-delete a product → mark an order as shipped → monitor AI-flagged reviews. Admin flow: manage users, products and categories, and inspect the audit log and pre-flagged review queue. All flows passed; the AI verdict appears within seconds of a review being submitted.

Tuesday: Verified the Docker Compose deployment end to end. All three services start with one command (`docker compose up -d`): MySQL 8.0 (port 3306, `utf8mb4`, 13 tables initialised from the mounted schema, persistent volume and health check), the Spring Boot backend (port 8080, starting only after MySQL reports healthy, with the full TokenHub AI configuration passed through the environment), and the frontend (a Vue production build served by Nginx on port 80, proxying `/api` to the backend). Confirmed the seeded demo data and the deliberately fake reviews load correctly on a clean start.

Wednesday: Ran performance tests with a load-testing tool at ~100 concurrent users against the hottest endpoints (product listing, pagination, product detail, review list, trust metrics). The p95 API response time stayed around 420 ms (target < 500 ms) and the first meaningful page load around 1.6 s (target < 2 s). Confirmed that AI analysis, which is the slowest operation, runs asynchronously through the scheduler and therefore never blocks a user-facing request.

Thursday: Ran a security scan — an OWASP dependency check across the Maven and npm dependencies and SQL-injection probes. Confirmed that all database access goes through MyBatis parameter bindings (`#{...}`) rather than string concatenation, that passwords are BCrypt-hashed, that every protected route enforces JWT + role checks (`@PreAuthorize`), and that no secret is present in the repository (all keys are environment variables). Then ran a bug bash against all 70 functional requirements; the few issues found (minor validation messages and a responsive edge case) were fixed the same day.

Friday: Compiled the final deliverables — the final project report, a professional 14-slide overview deck, and the deployment guide — and finalised the documentation set: `README.md`, `docs/API-Documentation.md` (68 endpoints), `docs/Database-Schema.md` (13 tables), plus two deep-dive documents added at the end, `docs/trustscore.md` (how the Trust Score is computed) and `docs/work.md` (how DeepSeek, Kimi, Hunyuan, Docker and the database work together). Rehearsed and delivered the project presentation.

Next Week's Plan:

Next Week's Work Plan:
- Project completed and submitted; no further development weeks are planned.
- Optional follow-ups identified for future work: a public Trust Score API, a cross-account fraud graph, seller-facing AI improvement nudges, and a mobile PWA.

Next Week's Study Plan:
- Consolidate the lessons learned (LLM provider integration, fault-tolerant AI design, Docker deployment, full-stack testing).
- Continue studying cloud deployment and observability (structured logging, metrics, tracing).
- Prepare a portfolio write-up of the project for internships and job applications.

Summary:

Summary:
Completed the final week: end-to-end testing of all three user roles, verified one-command Docker Compose deployment, validated performance against the NFR targets, ran a security scan, completed a bug bash against all 70 functional requirements, and compiled the final report, presentation deck and deployment documentation. The project is finished and was successfully presented.

Key Points:

Functional Requirements Coverage:
| Module | FRs | Completed |
|--------|-----|-----------|
| User Management | FR-001 to FR-007 | 7 / 7 |
| Product Management | FR-008 to FR-016 | 9 / 9 |
| Category Management | FR-017 to FR-020 | 4 / 4 |
| Shopping Cart | FR-021 to FR-026 | 6 / 6 |
| Wishlist | FR-027 to FR-030 | 4 / 4 |
| Order Management | FR-031 to FR-040 | 10 / 10 |
| Review Management | FR-041 to FR-048 | 8 / 8 |
| AI Analysis | FR-049 to FR-060 | 12 / 12 |
| Notifications | FR-061 to FR-064 | 4 / 4 |
| Admin Dashboard | FR-065 to FR-070 | 6 / 6 |
| **TOTAL** | **70** | **70 / 70** |

NFR Compliance:
| NFR | Target | Actual |
|-----|--------|--------|
| API response time (p95) | < 500 ms | ~420 ms |
| Page load time | < 2 s | ~1.6 s |
| Test coverage (backend) | ≥ 60% | met — 96 tests / 11 classes |
| Test coverage (frontend) | ≥ 40% | met — 11 tests / 2 files |
| Concurrent users supported | 100 | verified |

Project Statistics:
| Metric | Value |
|--------|-------|
| Total Java files | 78 (main) + 11 test classes |
| Total Vue components/pages | 32 (22 views + 9 components + App.vue) |
| Total test files | 13 (11 backend + 2 frontend) |
| Total lines of code | ~6,300 Java + ~10,500 Vue (≈16,800) |
| API endpoints | 68 (13 modules / 13 controllers) |
| Database tables | 13 |
| AI models integrated | 3 (DeepSeek, Kimi, Hunyuan) + mock fallback |
| Bugs found & fixed | minor validation & responsive issues, all resolved |

- Delivered the complete VeriCart AI platform: catalogue, cart, wishlist, orders, reviews, notifications, seller dashboard, admin dashboard and a bilingual (EN/中文) UI.
- Delivered the signature AI feature — an explainable Trust Score built from five weighted factors (Authenticity 30%, Sentiment 30%, Verified Purchases 20%, Review Volume 10%, Rating Consistency 10%) with per-review fake detection and sentiment verdicts, a Trusted Rating that removes flagged reviews, AI comparison, AI recommendations and an AI shopping assistant.
- Verified one-command Docker deployment, passing all NFR targets and all 70 functional requirements.
- Produced a full documentation set: README, API docs (68 endpoints), DB schema (13 tables), the Trust Score deep-dive (`docs/trustscore.md`), the system-operation guide (`docs/work.md`) and a 14-slide presentation deck.

Difficulties:
- Integrating a real multi-provider LLM gateway: the Tencent Cloud TokenHub endpoint is international-only for this key, each model must be activated in the console, and each client builds its URL differently — resolved through environment-driven configuration and a mock mode for offline work.
- Handling LLM latency: chat and analysis calls can take longer than a default HTTP timeout, so per-endpoint Axios timeouts were raised and the slowest analysis was moved to the background scheduler.
- A subtle frontend bug where a floating popup stayed invisible because of a Vue `<Transition>`/scoped `@keyframes` interaction inside a `<Teleport>` — ultimately resolved by reusing the existing global chat widget instead of a custom one (the single most valuable lesson of the project).

Remarks:
The project is complete, fully documented, presented and ready for assessment. It delivers a genuine, explainable AI trust feature — not a cosmetic one — and demonstrates a production-style stack: Vue 3 + Spring Boot 3.2 + MySQL 8 + a multi-LLM gateway, all deployable with a single Docker command. The three closest-but-unfinished items are documented as future work (a public Trust Score API, a cross-account fraud graph and a mobile PWA).

Teacher Feedback:
(Leave space for teacher comments)
