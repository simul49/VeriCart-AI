2025~2026 Academic Year, Semester 2
Practical Training / Internship Weekly Report

Name: [Your Name]
Class: [Your Class]
Student ID: [Your Student ID]
Project Name: VeriCart AI — AI-Powered E-Commerce Trust Platform
Team Name: Algorists
Project Type: Team Project
Role: Team Leader
Supervisor: [Teacher Name]
Date: Week 1 (July 7 – July 11, 2026)

Weekly Plan:

Monday: Project environment setup, full codebase review, understand the Spring Boot 3.2 + Vue 3 + MySQL 8.0 + multi-LLM architecture, identify the gaps in the existing codebase, and produce the 4-week development roadmap with a task breakdown.

Tuesday: Externalize every hardcoded secret (DeepSeek, Kimi and Hunyuan API keys plus the JWT secret) to environment variables, create the `.env.example` file with placeholder keys, and configure an H2 in-memory database for unit testing separately from the production MySQL 8.0 database.

Wednesday: Set up the JUnit 5 + Mockito backend test infrastructure, create an H2-compatible schema for the test database, and write the first unit tests for `JwtTokenProvider` and `AuthService`.

Thursday: Write unit tests for the remaining core services — `ProductService`, `OrderService`, `CartService`, `ReviewService`, `WishlistService`, `AiService` and `AiGateway` — with mocked MyBatis mapper dependencies.

Friday: Write the first Spring MockMvc integration test for `AuthController`, set up the frontend Vitest testing framework with jsdom and `@vue/test-utils`, and write component tests for `ProductCard` and `StarRating`.

Daily Progress:

Monday: Created and configured the 4-week development plan and reviewed the complete codebase — 58 Java backend files, 14 Vue views, a 10-table MySQL schema and the existing REST API (controllers, services and MyBatis mappers). Identified the critical gaps: 0% test coverage, hardcoded API keys in `application.yml`, a missing review submission UI, a missing seller dashboard, no frontend pagination and no project documentation. Established Week 1 as the testing-infrastructure and security-hardening phase.

Tuesday: Replaced all hardcoded API keys (DeepSeek, Kimi, Hunyuan) and the JWT secret in `application.yml` with environment-variable placeholders (for example `${DEEPSEEK_API_KEY:YOUR_DEEPSEEK_API_KEY}`). Created `.env.example` with placeholder keys and setup instructions for all three AI providers. Created `application-test.yml` with an H2 in-memory database for isolated unit testing, leaving the production MySQL 8.0 database untouched. Created `schema-h2.sql` with 10 H2-compatible table definitions mirroring the production schema.

Wednesday: Configured the backend test infrastructure with JUnit 5, Mockito and the H2 in-memory database. Wrote `JwtTokenProviderTest` covering token creation, validation, expiration and invalid-token scenarios. Wrote `AuthServiceTest` covering user registration with duplicate username/email handling, login with valid and invalid credentials, and token generation. Used `@ExtendWith(MockitoExtension.class)` and `@Mock` annotations to isolate the service layer from the database.

Thursday: Wrote comprehensive unit tests for the remaining core services. `ProductServiceTest`: CRUD, product search and category filtering (including the parent-category roll-up). `OrderServiceTest`: order creation, status transitions (PENDING → PAID → SHIPPED → DELIVERED) and order history. `CartServiceTest`: add/remove items, quantity updates and duplicate-item handling. `ReviewServiceTest`: review submission, edit window and AI-triggered analysis. `WishlistServiceTest`: add/remove and duplicate prevention. `AiServiceTest` and `AiGatewayTest`: multi-model routing across DeepSeek, Kimi and Hunyuan, plus the fallback chain.

Friday: Created `AuthControllerIntegrationTest` with Spring MockMvc, testing `POST /api/auth/register` and `POST /api/auth/login` for success and error scenarios. Set up the frontend testing framework by installing `vitest`, `jsdom`, `@vue/test-utils` and `@vitest/coverage-v8`, created `vitest.config.js` with the jsdom environment, and wrote `ProductCard.test.js` (rendering, price formatting, interaction) and `StarRating.test.js` (star count, half-star overlay, read-only mode). Added `test` and `test:coverage` scripts to `package.json`.

Next Week's Plan:

Next Week's Work Plan:
- Build the "Write a Review" form UI in the ProductDetail page with an interactive star rating and text input.
- Build the "My Reviews" tab in the Orders page to review purchased items.
- Build the Seller Dashboard page with product CRUD, order management and AI review monitoring.
- Add pagination controls to Products.vue and the Admin/Seller tables with page/size parameters.
- Write the complete project README.md with tech stack, architecture diagram and setup instructions.
- Write complete API documentation listing all REST endpoints across the application's modules.
- Write database schema documentation for the full schema.

Next Week's Study Plan:
- Learn Vue 3 form handling and validation patterns with Element Plus form components.
- Study Spring Boot pagination implementation patterns (page, size, total, sort parameters).
- Explore project-documentation and API-documentation format standards.
- Improve understanding of role-based access control for the seller vs buyer vs admin dashboards.

Summary:

Summary:
Completed the entire Week 1 testing and security-hardening phase. Established a complete testing infrastructure for the backend (JUnit 5, Mockito, H2 in-memory database) and the frontend (Vitest, jsdom, Vue Test Utils). Externalized all hardcoded AI keys (DeepSeek, Kimi, Hunyuan) and the JWT secret to environment-variable placeholders. Wrote 10 backend test classes covering all core services plus an integration test, and 2 frontend component test files.

Key Points:
- Successfully replaced all hardcoded secrets (3 AI-provider keys + the JWT secret) with environment-variable placeholders in `application.yml`.
- Set up an isolated H2 in-memory test database with a 10-table H2-compatible schema mirroring production MySQL.
- Created comprehensive unit tests for `AuthService`, `ProductService`, `OrderService`, `CartService`, `ReviewService`, `WishlistService`, `AiService`, `AiGateway` and `JwtTokenProvider`.
- Configured the Vitest testing framework with a jsdom environment for Vue 3 component testing.
- Established a scalable test architecture that supported test growth to 96 backend tests (11 classes) and 11 frontend tests by the end of the project.

Difficulties:
- Creating an H2-compatible SQL test schema that mirrors the production MySQL 8.0 schema — required removing MySQL-specific features (ENGINE=InnoDB, JSON columns, ENUM types) while keeping an identical table structure and relationships.
- Mocking MyBatis mapper interfaces correctly with Mockito so the service layer could be tested in isolation without a running MySQL database connection.

Remarks:
The testing infrastructure is now fully in place and was used continuously across the remaining 3 weeks. All secrets are externalized, making the project safe for sharing, deployment and teacher review without exposing API credentials. The codebase is prepared for Week 2 feature development: the review UI, the seller dashboard, pagination and documentation.

Teacher Feedback:
(Leave space for teacher comments)
