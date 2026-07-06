# VeriCart AI — 4-Week Development Plan

> **Starting Point:** Core CRUD + AI pipeline is built. Focus is on testing, missing features, polish, and documentation.

---

## Current State Summary

| Area | Status |
|------|--------|
| Backend (58 Java files) | ✅ Complete |
| Frontend (14 views, 4 components) | ✅ Complete |
| Database (10 tables) | ✅ Complete |
| AI Pipeline (3 LLM providers) | ✅ Complete |
| Docker Compose (3 services) | ✅ Complete |
| Unit/Integration Tests | ❌ 0% |
| Review Submission UI | ❌ Missing |
| Seller Dashboard UI | ❌ Missing |
| Pagination (frontend) | ❌ Missing |
| Project Documentation | ❌ Missing |
| Hardcoded Secrets | ⚠️ Needs cleanup |

---

## Week 1: Testing & Security Hardening (July 7–11)

### Objectives
- Establish testing infrastructure for backend and frontend
- Fix hardcoded secrets
- Write core unit tests

### Tasks

| ID | Task | Effort | Priority |
|----|------|--------|----------|
| W1-1 | Install Vitest + @vue/test-utils for frontend tests | 2h | P0 |
| W1-2 | Write backend unit tests: AuthService, JwtTokenProvider | 4h | P0 |
| W1-3 | Write backend unit tests: ProductService, OrderService | 4h | P0 |
| W1-4 | Write backend unit tests: AiService, AiGateway | 4h | P0 |
| W1-5 | Write backend unit tests: ReviewService, CartService, WishlistService | 4h | P1 |
| W1-6 | Write backend API integration tests (Auth, Product, Order controllers) | 6h | P1 |
| W1-7 | Move AI API keys & JWT secret to environment variables / .env | 2h | P0 |
| W1-8 | Add `.env.example` file with placeholder keys | 1h | P1 |
| W1-9 | Write frontend component tests: ProductCard, StarRating, ChatPanel | 3h | P1 |

### Deliverables
- [ ] Test report: 60%+ backend unit test coverage
- [ ] All secrets externalized to environment variables
- [ ] Frontend test framework configured

### Weekly Report Focus
- Testing methodology (JUnit 5, Mockito, Vitest)
- Security improvements (secrets management)
- Test coverage metrics

---

## Week 2: Missing Features & Documentation (July 14–18)

### Objectives
- Build the missing "Write a Review" UI
- Build the Seller Dashboard page
- Add pagination to product listing
- Complete project documentation

### Tasks

| ID | Task | Effort | Priority |
|----|------|--------|----------|
| W2-1 | Build "Write a Review" form in ProductDetail.vue (rating stars, text area) | 3h | P0 |
| W2-2 | Build "My Reviews" tab in Orders.vue (review purchased items) | 2h | P1 |
| W2-3 | Build Seller Dashboard page (product CRUD, view orders, view reviews) | 6h | P0 |
| W2-4 | Add seller route + navigation to navbar for seller role | 2h | P1 |
| W2-5 | Add pagination controls to Products.vue (page/size params) | 3h | P1 |
| W2-6 | Add pagination support to Admin.vue tables | 2h | P2 |
| W2-7 | Write README.md (project overview, tech stack, setup, architecture) | 3h | P0 |
| W2-8 | Write API documentation summary (list all endpoints) | 2h | P1 |
| W2-9 | Write database schema documentation | 1h | P2 |
| W2-10| Add footer page content (How It Works, Trust Score, AI Technology) | 2h | P2 |

### Deliverables
- [ ] Review submission fully functional (front-to-back)
- [ ] Seller Dashboard operational
- [ ] Product listing with pagination
- [ ] README.md complete
- [ ] API & DB docs

### Weekly Report Focus
- New features implemented
- Documentation completeness
- User flow demonstrations

---

## Week 3: Polish, Performance & Edge Cases (July 21–25)

### Objectives
- Performance optimization (API latency, bundle size, lazy loading)
- Edge case handling (error states, empty states, validation)
- UI/UX polish
- AI pipeline refinement

### Tasks

| ID | Task | Effort | Priority |
|----|------|--------|----------|
| W3-1 | Add loading skeletons/spinners for all async pages | 3h | P0 |
| W3-2 | Add error boundaries / error states for API failures | 3h | P0 |
| W3-3 | Add form validation edge cases (email format, password strength, etc.) | 2h | P1 |
| W3-4 | Add empty state designs for all list views | 2h | P1 |
| W3-5 | Optimize AI pipeline: add caching for repeated analyses (Redis or in-memory) | 4h | P1 |
| W3-6 | Add response compression (Gzip) to Spring Boot | 1h | P2 |
| W3-7 | Frontend bundle optimization: code splitting, tree shaking | 2h | P2 |
| W3-8 | Mobile responsive QA — test all pages at 375px width | 3h | P1 |
| W3-9 | Fix hardcoded Chinese text → i18n or translate to English consistently | 2h | P2 |
| W3-10| Add rate limiting to AI endpoints (prevent abuse) | 2h | P1 |

### Deliverables
- [ ] All pages have loading + error + empty states
- [ ] AI pipeline with response caching
- [ ] Mobile responsive across all pages
- [ ] Performance benchmarks

### Weekly Report Focus
- Performance improvements (before/after metrics)
- UI/UX enhancements
- AI optimization results

---

## Week 4: Final Testing, Deployment & Report (July 28 – Aug 1)

### Objectives
- End-to-end testing
- Docker deployment verification
- Performance testing
- Final report compilation

### Tasks

| ID | Task | Effort | Priority |
|----|------|--------|----------|
| W4-1 | Write E2E test scenarios (user registration → browse → purchase flow) | 4h | P0 |
| W4-2 | Run full Docker Compose deployment and verify all services | 3h | P0 |
| W4-3 | Performance test with JMeter/k6 (100 concurrent users) | 3h | P0 |
| W4-4 | Security scan: OWASP dependency check, SQL injection tests | 2h | P1 |
| W4-5 | Bug bash: test all 70 FRs against the running app | 4h | P0 |
| W4-6 | Fix critical bugs found during testing | 6h | P0 |
| W4-7 | Compile final project report (summary of all 4 weeks) | 4h | P0 |
| W4-8 | Prepare demo video/screenshots for presentation | 3h | P1 |
| W4-9 | Create deployment guide (step-by-step Docker instructions) | 2h | P1 |

### Deliverables
- [ ] E2E test suite passing
- [ ] Docker Compose deployment verified
- [ ] Performance test report (<500ms API, <2s page load per NFRs)
- [ ] Bug fix log
- [ ] Final project report
- [ ] Deployment guide

### Weekly Report Focus
- Final testing results
- Performance benchmarks vs NFR targets
- Project completion summary
- Lessons learned

---

## Weekly Report Template

Each week, submit a report with the following structure:

```
=== VeriCart AI — Week [X] Progress Report ===
Date: [Monday] – [Friday], July 2026
Student: [Your Name]
Project: VeriCart AI — AI-Powered E-Commerce Trust Platform

1. OBJECTIVES THIS WEEK
   - [List 3-5 key objectives from the plan]

2. COMPLETED TASKS
   | Task ID | Description | Status | Notes |
   |---------|-------------|--------|-------|
   | WX-1    | ...         | ✅ Done | ...   |
   | WX-2    | ...         | ✅ Done | ...   |
   | WX-3    | ...         | 🚧 In Progress | ...   |

3. KEY DELIVERABLES
   - [List files created/modified]
   - [Code commits summary]

4. TECHNICAL HIGHLIGHTS
   - [1-2 interesting technical decisions or challenges solved]

5. METRICS
   - Lines of code written: [X]
   - Test coverage: [X%]
   - Bugs fixed: [X]
   - New features: [X]

6. BLOCKERS & RISKS
   - [Any issues preventing progress]

7. NEXT WEEK PLAN
   - [Brief overview of next week's objectives]
```

---

## Success Criteria (End of Week 4)

| Criterion | Target |
|-----------|--------|
| Backend test coverage | ≥ 60% |
| Frontend test coverage | ≥ 40% |
| All 70 FRs implemented | ✅ |
| API response time (p95) | < 500ms |
| Page load time | < 2s |
| Docker deployment | One-command start |
| Project documentation | Complete |
| Secrets externalized | ✅ |
| AI pipeline working | All 3 models operational |
