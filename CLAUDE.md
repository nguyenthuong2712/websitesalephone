# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This repository is a phone e-commerce application with:
- a Spring Boot 3 / Java 21 backend in the repository root
- a Vue 3 + TypeScript + Vite frontend in `cms-user/`
- a SQL Server database, typically started with Docker Compose and restored from `manager_sale_phone.bak`

The frontend uses `VITE_ROOT_API` and the shared Axios client appends `/api`, so frontend requests target `${VITE_ROOT_API}/api`.

## Commands

### Database
- Start SQL Server: `docker compose up -d`
- Stop SQL Server: `docker compose down`

### Backend
- Run tests: `./mvnw test`
- Run one test class: `./mvnw -Dtest=WebsiteSalePhoneApplicationTests test`
- Run one test method: `./mvnw -Dtest=WebsiteSalePhoneApplicationTests#contextLoads test`
- Run the app: `./mvnw spring-boot:run`
- Build the jar: `./mvnw package`

### Frontend
- Install dependencies: `cd cms-user && npm install`
- Start the dev server: `cd cms-user && npm run dev`
- Build production assets: `cd cms-user && npm run build`
- Preview the production build: `cd cms-user && npm run preview`

`cms-user/package.json` defines `dev`, `build`, and `preview` only. There is no frontend lint or test script.

### Convenience scripts
- `runbe.sh` runs `./mvnw spring-boot:run`
- `rundev.sh` is stale and points at a non-existent Next.js app. Do not use it for this repository.

## Runtime configuration

- Backend configuration lives in `src/main/resources/application.properties`.
- The backend datasource is controlled by environment variables with defaults: `${DB_HOST:localhost}`, `${DB_PORT:1433}`, `${DB_NAME:manager_sale_phone}`, `${DB_USERNAME:sa}`, `${DB_PASSWORD:123456}`.
- Docker Compose reads `.env` values such as `MSSQL_SA_PASSWORD`, `DB_NAME`, `DB_PORT`, and `DB_CONTAINER_NAME`. Copy `.env.example` to `.env` when setting up locally.
- Important mismatch: the backend defaults `DB_PASSWORD` to `123456`, while `docker-compose.yml` defaults `MSSQL_SA_PASSWORD` to `YourStrong!Passw0rd`. Set them consistently or the backend will fail to connect to the Dockerized database.
- `docker/init-db.sh` waits for SQL Server, restores `manager_sale_phone.bak`, and skips restore if the database already exists.
- JPA runs with `spring.jpa.hibernate.ddl-auto=update`, `show-sql=true`, and `open-in-view=false`.
- The frontend local env file is `cms-user/.env`; local development uses `VITE_ROOT_API=http://localhost:8080`.

## Project architecture

### Backend structure

The backend follows a conventional Spring layered architecture:
- `controller/*` exposes REST endpoints under `/api/...`
- `service/*` and `service/*/impl/*` contain business logic
- `repository/*` contains Spring Data JPA repositories
- `entity/*` contains persistence models
- `dto/*` contains request/response payload models
- responses are commonly wrapped in `comon/CommonResponse` and paginated data in `comon/PageResponse`

Most filtered list pages follow the same paging pattern:
- search DTOs align with `comon/PagingRequest`
- `utils/Utils.getPaging(...)` builds the `PageRequest`
- more complex filters use JPA `Specification`s, especially `spe/OrderSpecification.java` and `spe/UserSpecification.java`
- product filtering is an exception: its specification logic is built inline in `service/product/impl/ProductServiceImpl.java`

### Authentication and authorization

Authentication is JWT-based:
- `controller/AuthController.java` handles login, logout, register, forgot-password, and reset-password flows
- `auth/JwtService.java` signs and validates RSA JWTs
- `auth/JwtAuthenticationFilter.java` reads `Authorization: Bearer ...` and populates the Spring Security context
- `config/ApplicationConfig.java` wires the `UserDetailsService`, password encoder, and authentication provider
- `config/SecurityConfig.java` installs the JWT filter and defines which routes are public vs role-restricted

On the frontend:
- `src/api/api.ts` injects the token into requests and clears auth state on `401`
- `src/service/AuthService.ts` stores auth data in `localStorage`, including `Authorization` and `USER-ROLE`
- `cms-user/src/router.ts` uses `meta.requiresAuth` and `meta.roles` to gate routes and redirect to `/login` or `/403`

Pinia exists, but client auth is not centered in Pinia; it is primarily stored in `localStorage`.

### Frontend structure

The Vue app is route-driven:
- `src/App.vue` is a `router-view` shell
- `src/router.ts` splits the app into customer routes under `/customer/...` and admin routes under `/admin/...`
- admin pages are wrapped with `src/layout/AdminLayout.vue`
- `src/service/*.ts` files are thin API wrappers around backend endpoints
- `src/models/*` mirrors backend DTO shapes, so backend contract changes usually need matching frontend model updates

### Core business areas

The main domain is a catalog and order workflow:
- `entity/Product` is the base product record
- `entity/ProductVariant` is the sellable SKU with quantity/inventory
- lookup-table style entities such as `Color`, `Ram`, `Screen`, `Camera`, `Origin`, `Battery`, `Storage`, `Cpu`, and `OperatingSystem` define dynamic product attributes
- `entity/Cart` and `entity/CartItem` support the active cart
- `entity/Order`, `entity/OrderItem`, and `entity/OrderStatusHistory` track checkout and fulfillment
- `entity/User`, `entity/Role`, `entity/Address`, `entity/PasswordResetToken`, and `entity/ExpiredToken` cover identity and token lifecycle

Product management spans multiple layers:
- `controller/ProductController.java` exposes product CRUD, variant CRUD, image management, quantity checks, and the “new product” listing
- `service/product/impl/ProductServiceImpl.java` is the main orchestration layer for create/update/search flows
- `controller/DynamicController.java` with `service/dyanmic/impl/DynamicAttributeProductServiceImpl.java` manages lookup-table style dynamic attributes

Cart and order processing is split between two services:
- `controller/CartController.java` and `service/cart/impl/CartServiceImpl.java` handle cart mutation, stock validation, checkout, inventory decrement, cart item checkout state, and initial order status history creation
- `controller/OrderController.java` and `service/order/impl/OrderServiceImpl.java` handle order search, detail views, status transitions, counts, and dashboard summaries

### Shop registration flow

Shop registration is a cross-cutting feature:
- `controller/ShopController.java` delegates to `service/shop/impl/ShopServiceImpl.java`
- shop data is stored in `ShopRegistration` and `ShopPaymentMethod`
- uploaded shop assets are written to `uploads/shop` relative to the backend run directory
- a successful registration updates the user role in the database to `STAFF`
- `cms-user/src/pages/home/ShopRegisterPage.vue` saves the client role as `PARTNER`, but this page is not wired into `cms-user/src/router.ts`

### Payment flow

VNPAY integration is handled by:
- `config/VnPayConfig.java` for VNPAY properties
- `controller/PaymentController.java` for payment creation and VNPAY return handling

The VNPAY return handler redirects back to Vue routes on `http://localhost:5173`, so payment callback behavior currently assumes that frontend port.

### Realtime chat

Realtime messaging uses STOMP over WebSocket:
- `config/WebSocketConfig.java` registers `/ws` and the `/topic` broker prefix
- `controller/ChatController.java` maps messages under `/app/chat.sendMessage/{roomId}`
- room updates publish to `/topic/chat/room/{roomId}` and admin notifications to `/topic/chat/admins`
- the frontend chat code lives in `cms-user/src/components/ChatWidget.vue`, `cms-user/src/pages/chat/AdminChat.vue`, and `cms-user/src/service/ChatService.ts`

### Startup seeders

`config/init/*` contains `CommandLineRunner` seeders that populate lookup data if missing. Notable examples:
- `RoleDataInitializer.java` inserts all `RoleEnums`
- `ColorDataInitializer.java` and `OriginDataInitializer.java` seed dynamic attribute values

## Filesystem-dependent behavior

Two backend features depend on local filesystem assumptions:
- `service/dyanmic/impl/ProductImageServiceImpl.java` writes product images to the hard-coded Windows path `D:\FE_DuAnTotNghiep\assets\ảnh giày`
- `service/pdf/impl/PDFGeneratorServiceImpl.java` generates invoices and expects the font resource `fonts/Roboto-Regular.ttf`

## Reference docs and repo-specific notes

- `docs/backend-api-spec.md` is the generated REST API reference for backend endpoints. `.html` and `.pdf` copies also exist.
- `docs/auto-product-approval-image-hashing.md` documents the image-hashing / auto product-approval design.
- `AGENTS.md` contains project-local LLM working rules: think before coding, prefer the simplest solution, and keep edits surgical.
- The root `README.md` and `cms-user/README.md` do not contain meaningful project-specific setup guidance beyond template text.

## Testing status

- Backend automated coverage is minimal; the committed test suite is essentially `src/test/java/org/example/websitesalephone/WebsiteSalePhoneApplicationTests.java`.
- The frontend has no committed automated test or lint command.
