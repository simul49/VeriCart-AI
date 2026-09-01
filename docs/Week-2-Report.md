2025~2026 Academic Year, Semester 2
Practical Training / Internship Weekly Report

Name: [Your Name]
Class: [Your Class]
Student ID: [Your Student ID]
Project Name: VeriCart AI — AI-Powered E-Commerce Trust Platform
Supervisor: [Teacher Name]
Date: Week 2 (July 14 – July 18, 2026)

Weekly Plan:

Monday: Build "Write a Review" form with interactive star rating in ProductDetail.vue, connect to POST /api/reviews endpoint with AI analysis trigger.

Tuesday: Build "My Reviews" tab in Orders.vue for reviewing delivered items, implement Element Plus form validation with duplicate-prevention.

Wednesday: Build Seller Dashboard with product CRUD, order management, and review monitoring panels, protected by role-based navigation guards.

Thursday: Add pagination to Products.vue and Seller Dashboard (backend page/size params + frontend el-pagination), write complete README.md.

Friday: Write API documentation (49 endpoints), database schema documentation (10 tables), and three footer pages (How It Works, Trust Score, AI Technology).

Daily Progress:

Monday: Upgraded StarRating.vue to dual-mode (display + interactive) by adding `editable` prop and v-model support with hover-preview highlighting. Built review form in ProductDetail.vue using el-form with validation (rating 1–5 required, content 10–2000 chars required, optional image URL). On submission via reviewApi.create(), the form resets, ElMessage confirms success, and the review list refreshes. Backend POST /api/reviews auto-triggers AI analysis (DeepSeek for sentiment, Hunyuan as fallback), returning sentiment, emotion, and fake-probability displayed in the review card.

Tuesday: Added "My Reviews" tab to Orders.vue using el-tabs, fetching DELIVERED orders via GET /api/orders/my?status=DELIVERED. Each item shows a "Write Review" button opening an el-dialog with the interactive form pre-populated with productId and orderId. Async validation prevents duplicate reviews on the same order item. After submission, the button disables to "Reviewed" state and reviews refresh reactively. Toast notifications handle both success and network-failure states.

Wednesday: Created SellerDashboard.vue with three el-tabs and a `meta: { role: 'SELLER' }` route guard. Tab 1 — Product Management: el-table CRUD with inline editing, soft-delete confirmation, and "Add Product" dialog with full validation. Tab 2 — Order Management: displays seller's orders with status badges and "Mark as Shipped" action for PENDING → SHIPPED transitions. Tab 3 — Review Monitoring: lists all reviews with AI analysis results; flagged reviews (fake ≥ 70%) highlighted in red. Seller identity extracted from JWT claims. Added "Seller Dashboard" link in App.vue navbar.

Thursday: Backend: added `@RequestParam page/size` to ProductController with structured `{ items, total, page, size, totalPages }` response, using MyBatis RowBounds for offset pagination. Frontend: rebuilt Products.vue with el-pagination (12/24/48 per page), page buttons, and total count display. Pagination state syncs with URL query parameters via vue-router so state survives refresh. Applied same pagination to Seller Dashboard tables. Wrote README.md covering tech stack, project structure, prerequisites (Java 17, Node 18+, MySQL 8.0), .env setup, Docker Compose + manual startup, database overview, API quick reference, architecture diagram, and testing instructions.

Friday: Created docs/API-Documentation.md covering all 49 endpoints across 12 modules (Auth, User, Product, Category, Cart, Wishlist, Orders, Reviews, AI Analysis, Notifications, Admin, Health), each documented with HTTP method, URL, auth requirement, parameters, response/error examples. Created docs/Database-Schema.md with complete DDL, column descriptions, constraints, indexes, and ER relationships for all 10 tables. Built three footer pages: HowItWorks.vue (4-step AI verification pipeline), TrustScore.vue (5-factor scoring algorithm: Review Authenticity 30%, Seller History 25%, Product Quality 20%, Shipping 15%, Service 10%), and AITechnology.vue (multi-model LLM gateway with provider comparison table). Added routes and linked from App.vue footer.

Next Week's Plan:

Next Week's Work Plan:
Add loading skeletons and error-state handling for all async pages.
Add empty-state designs for list views (products, orders, reviews, cart).
Optimize AI pipeline with response caching and rate limiting.
Mobile responsive QA at 375px viewport.
Add Gzip compression to Spring Boot backend.

Next Week's Study Plan:
Learn Vue 3 Suspense and async component patterns.
Study in-memory caching strategies (Caffeine/Redis) for AI responses.
Explore Spring Boot rate limiting with Bucket4j/Resilience4j.
Improve CSS responsive design and frontend performance optimization (code splitting, lazy loading).

Summary:

Summary:
Completed the Week 2 feature-development and documentation phase. Built the complete review submission flow with an upgraded dual-mode StarRating component (display + v-model input) integrated into ProductDetail.vue and Orders.vue. Constructed a full Seller Dashboard with product CRUD, order management, and AI-powered review monitoring behind role-based route guards. Implemented end-to-end pagination with backend MyBatis RowBounds and frontend el-pagination synchronized with URL query parameters. Authored three documentation files: 49-endpoint API reference, 10-table DB schema with ER diagrams, and a professional README with architecture overview and quick-start guides. Built three educational footer pages explaining the AI trust verification pipeline.

Key Points:
Built dual-mode StarRating component (read-only + interactive v-model) with backward-compatible defaults, used across ProductDetail review form and Orders My Reviews dialog.
Created Seller Dashboard with 3 tabs (Product CRUD, Order Management, Review Monitoring) protected by Vue Router + backend @PreAuthorize role guards.
Implemented full-stack pagination: backend page/size params with MyBatis RowBounds, frontend el-pagination with URL query synchronization.
Authored API docs (49 endpoints × 12 modules), DB schema docs (10 tables with ER relationships), and polished README with architecture diagram.
Built 3 footer pages (How It Works, Trust Score, AI Technology) with CSS diagrams and provider comparison tables.

Difficulties:
Making StarRating work in both modes without breaking 4 existing usages — solved with optional `editable`/`modelValue` props with safe defaults.
Coordinating role-based access across Vue Router guard, Axios interceptor, and @PreAuthorize — standardized "SELLER" across all layers after debugging mismatches.
Syncing pagination with URL query params while resetting to page 1 on filter change — used reactive watch + router.replace() to avoid history pollution.

Remarks:
All Week 2 deliverables complete — the platform now supports the full buyer-to-seller journey. Documentation package ready for teacher review. On track for Week 3: UI polish, performance optimization, and mobile responsiveness.
