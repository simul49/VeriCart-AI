2025~2026 Academic Year, Semester 2
Practical Training / Internship Weekly Report

Name: [Your Name]
Class: [Your Class]
Student ID: [Your Student ID]
Project Name: VeriCart AI — AI-Powered E-Commerce Trust Platform
Supervisor: [Teacher Name]
Date: Week 1 (July 7 – July 11, 2026)

Weekly Plan:

Monday: Project environment setup, codebase review, understand Spring Boot + Vue 3 + MySQL 8.0 architecture (production database on localhost:3306), identify gaps in existing codebase, plan 4-week development roadmap with task breakdown.

Tuesday: Externalize hardcoded API keys and JWT secret to environment variables, create .env.example file with placeholder keys, configure H2 in-memory database for unit testing (separate from production MySQL 8.0 database) with application-test.yml.

Wednesday: Set up JUnit 5 + Mockito backend test infrastructure, create H2-compatible schema.sql for test database, write unit tests for AuthService and JwtTokenProvider.

Thursday: Write unit tests for ProductService, OrderService, CartService, ReviewService, WishlistService, AiService, and AiGateway with mocked mapper dependencies.

Friday: Write integration tests for AuthController using Spring MockMvc, set up frontend Vitest testing framework with jsdom and @vue/test-utils, write component tests for ProductCard and StarRating.

Daily Progress:

Monday: Created and configured the 4-week development plan, reviewed the complete codebase including 58 Java backend files and 14 Vue frontend views, identified critical gaps: 0% test coverage, hardcoded API keys in application.yml, missing review submission UI, missing seller dashboard, no pagination. Established Week 1 focus on testing infrastructure and security hardening.

Tuesday: Replaced all hardcoded API keys (DeepSeek, Qwen, Hunyuan) and JWT secret in application.yml with environment variable placeholders (${DEEPSEEK_API_KEY:YOUR_DEEPSEEK_API_KEY}). Created .env.example file with placeholder keys and setup instructions for all three AI providers. Created application-test.yml with H2 in-memory database configuration for isolated unit testing, keeping production MySQL 8.0 database untouched. Created schema-h2.sql with 10 H2-compatible table definitions mirroring the production MySQL schema.

Wednesday: Configured backend test infrastructure with JUnit 5, Mockito, and H2 in-memory database. Wrote JwtTokenProviderTest covering token creation, validation, expiration, and invalid token scenarios. Wrote AuthServiceTest covering user registration with duplicate username/email handling, login with valid and invalid credentials, and token generation. Used @ExtendWith(MockitoExtension.class) and @Mock annotations to isolate service layer from database dependencies.

Thursday: Wrote comprehensive unit tests for six core service classes. ProductServiceTest: CRUD operations, product search, and category filtering. OrderServiceTest: order creation, status transitions (PENDING → PAID → SHIPPED → DELIVERED), and order history retrieval. CartServiceTest: add/remove items, quantity updates, and duplicate item handling. ReviewServiceTest: submit review, review moderation, and fake review detection. WishlistServiceTest: add/remove items and duplicate prevention. AiServiceTest: AI analysis trigger and multi-model routing across DeepSeek, Qwen, and Hunyuan.

Friday: Created AuthControllerIntegrationTest using Spring MockMvc testing registration POST /api/auth/register and login POST /api/auth/login endpoints with success and error scenarios. Set up frontend testing framework: installed vitest, jsdom, @vue/test-utils, @vitest/coverage-v8 via npm. Created vitest.config.js with jsdom environment configuration. Wrote ProductCard.test.js for rendering, price formatting, and click interaction testing. Wrote StarRating.test.js for star count display, half-star rendering, and readonly mode verification. Added test and test:coverage scripts to package.json.

Next Week's Plan:

Next Week's Work Plan:
Build "Write a Review" form UI in ProductDetail page with interactive star rating and text input.
Build "My Reviews" tab in Orders page for reviewing purchased items.
Build Seller Dashboard page with product CRUD, order management, and review monitoring.
Add pagination controls to Products.vue and Admin.vue with page/size parameters.
Write complete project README.md with tech stack, architecture diagram, and setup instructions.
Write API documentation listing all 49 REST endpoints.
Write database schema documentation for all 10 tables.

Next Week's Study Plan:
Learn Vue 3 form handling and validation patterns with Element Plus form components.
Study Spring Boot pagination implementation patterns (page, size, total, sort parameters).
Explore project documentation best practices and API documentation format standards.
Improve understanding of role-based access control for seller vs buyer vs admin dashboards.

Summary:

Summary:
Completed the entire Week 1 testing and security hardening phase. Established complete testing infrastructure for both backend using JUnit 5, Mockito, and H2 in-memory database, and frontend using Vitest, jsdom, and Vue Test Utils. Externalized all hardcoded API keys for DeepSeek, Qwen, and Hunyuan, and the JWT secret to environment variable placeholders. Wrote 10 backend test classes covering all 8 core services plus an integration test, and 2 frontend component tests.

Key Points:
Successfully replaced all hardcoded secrets (3 AI provider keys + JWT secret) with environment variable placeholders in application.yml.
Set up isolated H2 in-memory test database with H2-compatible schema mirroring production MySQL.
Created comprehensive unit tests for AuthService, ProductService, OrderService, CartService, ReviewService, WishlistService, AiService, AiGateway, and JwtTokenProvider.
Configured Vitest testing framework with jsdom environment for Vue 3 component testing.
Established scalable test architecture that supports future test development in Week 2–4.

Difficulties:
Creating H2-compatible SQL test schema that mirrors the production MySQL 8.0 schema — required removing MySQL-specific features (ENGINE=InnoDB, JSON columns, ENUM types) while maintaining identical table structure and relationships across all 10 tables.
Mocking MyBatis mapper interfaces correctly with Mockito for isolated service layer testing without requiring a running MySQL database connection.

Remarks:
The testing infrastructure is now fully in place and ready for continuous use throughout the remaining 3 weeks of development. All secrets are externalized, making the project safe for sharing, deployment, and teacher review without exposing API credentials. The codebase is prepared for Week 2 feature development including review UI, seller dashboard, and pagination.
