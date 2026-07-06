# VeriCart AI — Long-term Memory

## Project Conventions
- All communication in English (user preference)
- Project: AI-powered e-commerce trust platform (VeriCart AI)
- Stack: Vue.js 3 + Element Plus (frontend), Spring Boot 3.2 + MyBatis + MySQL 8.0 (backend)
- AI: Multi-LLM Gateway with DeepSeek, Qwen, Hunyuan
- Database: MySQL `vericart` on localhost:3306, user root / paradox49
- API keys are externalized (not in repo)

## PRD Documentation
- 25 chapters covering all aspects (Executive Summary through References)
- 70 Functional Requirements (FR-001 to FR-070), 50+ NFRs
- 49 REST APIs across 10 modules
- 10 database tables
- Located in `PRD/` directory (25 .docx + extracted .txt files)

## 4-Week Development Plan (July 7 – Aug 1, 2026)
- Week 1: Testing & Security (JUnit 5, Vitest, externalize secrets)
- Week 2: Missing Features (Review UI, Seller Dashboard, pagination, docs)
- Week 3: Polish & Performance (loading states, caching, mobile QA)
- Week 4: Final Testing & Report (E2E, Docker deploy, bug bash, final report)
- Plan and report templates in `docs/`
- User needs to submit weekly reports to teacher

## Current Codebase State (as of July 6, 2026)
- Backend: 58 Java files, all CRUD complete, AI pipeline working
- Frontend: 14 views, 4 components, all routes defined
- Tests: 0% coverage (no test files written)
- Missing: Review submission UI, Seller Dashboard, pagination, docs, footer pages
