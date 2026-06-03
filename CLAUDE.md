# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This repository is a phone e-commerce application split into two parts:
- A Spring Boot 3 / Java 21 backend in the repository root.
- A Vue 3 + TypeScript + Vite frontend in `cms-user/`.

The frontend talks to the backend over HTTP using `VITE_ROOT_API` and appends `/api` in the shared Axios client.

## Common Commands

### Backend
- Install/build and run tests: `./mvnw test`
- Run the Spring Boot app: `./mvnw spring-boot:run`
- Build the backend jar: `./mvnw package`
- Run a single backend test class: `./mvnw -Dtest=WebsiteSalePhoneApplicationTests test`
- Run a single backend test method: `./mvnw -Dtest=WebsiteSalePhoneApplicationTests#contextLoads test`

### Frontend
- Install dependencies: `cd cms-user && npm install`
- Start the Vite dev server: `cd cms-user && npm run dev`
- Build the frontend: `cd cms-user && npm run build`
- Preview the production build: `cd cms-user && npm run preview`

## Runtime Configuration

- Backend settings live in `src/main/resources/application.properties`.
- The backend expects SQL Server on `localhost:1433` with database `manager_sale_phone`.
- JPA runs with `spring.jpa.hibernate.ddl-auto=update`, so schema changes are applied at startup.
- The frontend environment file is `cms-user/.env`; local development uses `VITE_ROOT_API=http://localhost:8080`.

## Architecture

### Backend Request Flow

Most backend features follow this path:
- `controller/*` exposes REST endpoints under `/api/...`.
- Controllers delegate to `service/*` interfaces and `service/*/impl/*` implementations for business logic.
- Services read and write JPA entities through Spring Data repositories in `repository/*`.
- DTOs in `dto/*` shape request and response payloads.
- API responses are usually wrapped in `comon/CommonResponse` and paginated results use `comon/PageResponse`.

### Security Model

Authentication is JWT-based:
- `controller/AuthController.java` exposes login, logout, register, forgot-password, and reset-password flows.
- `auth/JwtService.java` signs and validates RSA JWTs.
- `auth/JwtAuthenticationFilter.java` reads the `Authorization: Bearer ...` header and populates the Spring Security context.
- `config/ApplicationConfig.java` wires `UserDetailsService`, password encoding, and the authentication provider.
- `config/SecurityConfig.java` installs the JWT filter. Notice that some routes are public (e.g. `/api/auth/**`) while other endpoints (e.g. `/api/chat/**`, `/api/user/**`, `/api/order/**`, `/api/product/**`) require specific roles or authentication.

### WebSocket Chat System

The app implements real-time messaging using STOMP:
- `config/WebSocketConfig.java` registers `/ws` as the endpoint and configures `/topic` as the broker prefix.
- `controller/ChatController.java` maps websocket messages under `/app/chat.sendMessage/{roomId}`.
- WebSockets use `/topic/chat/room/{roomId}` for direct user room updates and `/topic/chat/admins` to notify admins of activity.

### Domain Model

The data model is centered around a catalog/order workflow:
- `entity/Product` holds the base product record.
- `entity/ProductVariant` stores sellable variants and inventory quantities.
- Dynamic attribute entities such as `Color`, `Ram`, `Screen`, `Camera`, `Origin`, `Battery`, `Storage`, `Cpu`, and `OperatingSystem` act as lookup tables used when building variants.
- `entity/Cart` and `entity/CartItem` support the active shopping cart.
- `entity/Order`, `entity/OrderItem`, and `entity/OrderStatusHistory` track checkout and fulfillment progress.
- `entity/User`, `entity/Role`, `entity/Address`, `entity/PasswordResetToken`, and `entity/ExpiredToken` support identity, authorization, and token lifecycle.

### Search and Paging Conventions

Search screens use a shared pattern across modules:
- Search DTOs extend or align with `comon/PagingRequest` semantics (`page`, `size`, `sortBy`, `sortDesc`).
- `utils/Utils.getPaging(...)` converts those DTOs into Spring `PageRequest`.
- More complex filters are implemented as JPA `Specification`s, notably in `spe/OrderSpecification.java` and `spe/UserSpecification.java`.
- Product search currently builds the `Specification` inline in `service/product/impl/ProductServiceImpl.java` rather than in a dedicated specification class.

### Product and Dynamic Attribute Management

Product management spans multiple files:
- `controller/ProductController.java` exposes product CRUD, variant CRUD, image management, quantity checks, and "new product" listing.
- `service/product/impl/ProductServiceImpl.java` is the main orchestration layer for product creation, variant updates, search, and image metadata.
- `controller/DynamicController.java` with `service/dyanmic/impl/DynamicAttributeProductServiceImpl.java` handles lookup-table style CRUD for attributes like color, RAM, screen, camera, origin, and similar values used by product variants.

### Cart and Checkout Flow

The purchase flow is split between cart and order services:
- `controller/CartController.java` exposes add/update/items/checkout endpoints.
- `service/cart/impl/CartServiceImpl.java` resolves the logged-in user from the security context, mutates cart items, validates stock, creates the order, decrements variant inventory, marks cart items checked out, and inserts the first `OrderStatusHistory` row.
- `service/order/impl/OrderServiceImpl.java` handles back-office order search, detail views, status transitions, counts, and dashboard summary metrics.

### PDF and Image Handling

Two features depend on filesystem/resource assumptions that are easy to miss:
- `service/pdf/impl/PDFGeneratorServiceImpl.java` renders order invoices with iText and expects the font resource path `fonts/Roboto-Regular.ttf`.
- `service/dyanmic/impl/ProductImageServiceImpl.java` writes uploaded images to the hard-coded Windows directory `D:\FE_DuAnTotNghiep\assets\ảnh giày`, so image upload behavior is environment-specific.

## Frontend Structure

### App Shell and Routing

The Vue app is route-driven:
- `src/App.vue` is only a `router-view` shell.
- `src/router.ts` defines two main application areas: customer routes under `/customer/...` and admin routes under `/admin/...`.
- Admin pages are wrapped by `src/layout/AdminLayout.vue`.
- Route guards use `meta.requiresAuth` plus `meta.roles` to redirect unauthenticated users to `/login` and unauthorized users to `/403`.

### Frontend API Layer

The frontend uses a shared Axios instance:
- `src/api/api.ts` sets the base URL to `${VITE_ROOT_API}/api`.
- A request interceptor injects the bearer token from `AuthService`.
- A response interceptor clears auth state and redirects to `/login` on `401`.
- Feature services in `src/service/*.ts` are thin wrappers around backend endpoints.

### Frontend Auth and State

Client-side auth state is stored in `localStorage` rather than Pinia:
- `src/service/AuthService.ts` stores the JWT under `Authorization` and the role under `USER-ROLE`.
- Route access depends on `authService.isAuthenticated()` and `authService.getRole()` from `src/router.ts`.
- Pinia is present, but currently used for focused state such as `src/userStore.ts` for the logged-in user profile.

### Frontend Page Organization

The page structure follows the backend domains:
- `src/pages/home/*` contains customer storefront, cart, order history, and profile pages.
- `src/pages/product/*`, `src/pages/order/*`, and `src/pages/user/*` support admin/staff management flows.
- `src/models/*` mirrors backend request/response DTO shapes, so backend contract changes usually require corresponding frontend model and service updates.

## Testing Status

- Backend test coverage is minimal; the only committed test is `src/test/java/org/example/websitesalephone/WebsiteSalePhoneApplicationTests.java`.
- The frontend `package.json` does not define lint or test scripts.
