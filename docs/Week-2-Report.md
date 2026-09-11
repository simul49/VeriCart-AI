2025~2026 Academic Year, Semester 2
Practical Training / Internship Weekly Report

Name: [Your Name]
Class: [Your Class]
Student ID: [Your Student ID]
Project Name: VeriCart AI — AI-Powered E-Commerce Trust Platform
Supervisor: [Teacher Name]
Date: Week 2 (July 14 – July 18, 2026)

Weekly Plan:

Monday: Build the "Write a Review" form with an interactive star rating in `ProductDetail.vue` and connect it to `POST /api/reviews` with the AI analysis trigger.

Tuesday: Build the "My Reviews" tab in `Orders.vue` for reviewing delivered items, using Element Plus form validation with duplicate-review prevention.

Wednesday: Build the Seller Dashboard with product CRUD, order management and review-monitoring panels, protected by role-based navigation guards.

Thursday: Add pagination to `Products.vue` and the Admin/Seller tables (backend page/size parameters + frontend `el-pagination`), and write the complete README.md.

Friday: Write the API documentation (68 endpoints across 13 modules), the database schema documentation (13 tables) and three educational footer pages.

Daily Progress:

Monday: Upgraded `StarRating.vue` to a dual-mode component (display + interactive) by adding an `editable` prop and `v-model` support with hover-preview highlighting, keeping it backward-compatible with the four existing read-only usages. Built the review form in `ProductDetail.vue` with `el-form` validation (rating 1–5 required, content 10–2000 characters required, optional image URL). On submission through `reviewApi.create()`, the form resets, `ElMessage` confirms success and the review list refreshes. The backend `POST /api/reviews` automatically triggers AI analysis — DeepSeek performs fake-review detection and Kimi performs sentiment analysis, with the results (sentiment, emotion, fake probability, flagged status) shown on the review card.

Tuesday: Added a "My Reviews" tab to `Orders.vue` with `el-tabs`, fetching delivered orders via `GET /api/orders/my?status=DELIVERED`. Each item shows a "Write Review" button that opens an `el-dialog` with the interactive form pre-populated with `productId` and `orderId`. Validation prevents duplicate reviews on the same order item. After submission the button switches to the disabled "Reviewed" state and the list refreshes reactively. Toast notifications handle both success and network-failure cases.

Wednesday: Created `SellerDashboard.vue` with three `el-tabs` behind a `meta: { role: 'SELLER' }` route guard. Tab 1 — Product Management: an `el-table` with CRUD, inline editing, soft-delete confirmation and an "Add Product" dialog with full validation. Tab 2 — Order Management: the seller's orders with status badges and a "Mark as Shipped" action (PENDING → SHIPPED). Tab 3 — Review Monitoring: every review with its AI verdict, with flagged reviews (fake probability ≥ 70%) highlighted in red. The seller identity is extracted from the JWT claims. Added a "Seller Dashboard" link to the `App.vue` navbar.

Thursday: Backend — added `@RequestParam page/size` to `ProductController` with a structured `{ items, total, page, size, totalPages }` response, using MyBatis `RowBounds` for offset pagination. Frontend — rebuilt `Products.vue` with `el-pagination` (12/24/48 per page), page buttons and a total-count display, syncing the page state with URL query parameters via vue-router so it survives a refresh. Applied the same pagination to the Seller Dashboard tables. Wrote README.md covering the tech stack, project structure, prerequisites (Java 17, Node 18+, MySQL 8.0), `.env` setup, Docker Compose + manual startup, database overview, API quick reference, architecture diagram and testing instructions.

Friday: Created `docs/API-Documentation.md` covering all 68 endpoints across 13 modules (Auth, User, Product, Category, Cart, Wishlist, Orders, Reviews, AI Analysis, Notifications, Admin, Seller, Health), each with HTTP method, URL, auth requirement, parameters and response/error examples. Created `docs/Database-Schema.md` with the complete DDL, column descriptions, constraints, indexes and ER relationships for all 13 tables — including the newer `audit_log`, `inquiry` and `inquiry_message` tables. Built three educational footer pages: `HowItWorks.vue` (the 4-step AI verification pipeline), `TrustScore.vue` (the explainable 5-factor scoring model) and `AITechnology.vue` (the multi-model LLM gateway with a provider-comparison table). Added the routes and linked them from the `App.vue` footer.

Next Week's Plan:

Next Week's Work Plan:
- Add loading skeletons and error-state handling for all async pages.
- Add empty-state designs for the list views (products, orders, reviews, cart, wishlist).
- Harden the AI pipeline: provider fallback chain and mock-mode resilience, with additional tests.
- Add the automatic AI analysis scheduler for un-analysed reviews.
- Introduce bilingual i18n (English / 中文) across all pages.
- Mobile responsive QA at 375px viewport.
- Add Gzip compression and frontend code splitting.

Next Week's Study Plan:
- Learn Vue 3 lazy-loaded routes and async component patterns.
- Study in-memory caching strategies for AI responses.
- Explore Spring Boot compression and safe public-endpoint conventions.
- Improve CSS responsive-design and frontend performance-optimization skills (code splitting, tree shaking).

Summary:

Summary:
Completed the Week 2 feature-development and documentation phase. Built the complete review-submission flow with an upgraded dual-mode `StarRating` component integrated into both `ProductDetail.vue` and `Orders.vue`. Constructed a full Seller Dashboard with product CRUD, order management and AI-powered review monitoring behind role-based route guards. Implemented end-to-end pagination with backend MyBatis `RowBounds` and frontend `el-pagination` synchronised with URL query parameters. Authored three documentation deliverables: a 68-endpoint API reference, a 13-table database schema with ER relationships, and a professional README with an architecture overview and quick-start guides. Built three educational footer pages explaining the AI trust-verification pipeline.

Key Points:
- Built a dual-mode `StarRating` component (read-only + interactive `v-model`) with backward-compatible defaults, reused across the review form and the "My Reviews" dialog.
- Created the Seller Dashboard with 3 tabs (Product CRUD, Order Management, Review Monitoring) protected by Vue Router guards and backend `@PreAuthorize` role checks.
- Implemented full-stack pagination: backend page/size parameters with MyBatis `RowBounds`, and frontend `el-pagination` with URL-query synchronisation.
- Documented the modernised Trust Score model — five weighted factors: Review Authenticity 30%, Customer Sentiment 30%, Verified Purchases 20%, Review Volume 10% and Rating Consistency 10% — surfaced through the `TrustScoreExplainer` and the `/trust-score` page.
- Authored API docs (68 endpoints × 13 modules), DB schema docs (13 tables with ER relationships) and a polished README with an architecture diagram.
- Built 3 footer pages (How It Works, Trust Score, AI Technology) with CSS diagrams and provider-comparison tables.

Difficulties:
- Making `StarRating` work in both modes without breaking the four existing usages — solved with optional `editable`/`modelValue` props with safe defaults.
- Coordinating role-based access across the Vue Router guard, the Axios interceptor and `@PreAuthorize` — standardised the "SELLER" role string across all layers after debugging mismatches.
- Syncing pagination with URL query parameters while resetting to page 1 on a filter change — used a reactive `watch` + `router.replace()` to avoid polluting the browser history.

Remarks:
All Week 2 deliverables are complete — the platform now supports the full buyer-to-seller journey and the documentation package is ready for teacher review. On track for Week 3: UI polish, AI resilience, bilingual i18n, performance optimisation and mobile responsiveness.

Teacher Feedback:
(Leave space for teacher comments)
