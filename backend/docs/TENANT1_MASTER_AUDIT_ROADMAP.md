# Tenant 1 - Master Audit + Roadmap

Date of verification: 2026-06-03 (last updated after Block 7)

Scope:
- Tenant 1 public commerce
- internal dashboard
- security / release readiness
- roadmap and execution plan reconciliation

Method used:
- Checked every task list / checklist file in `backend/docs`
- Cross-checked each item against source code, config, and tests
- Ran backend tests with `./mvnw.cmd -q test`

## 1. Documents audited

Task-bearing docs:
- `backend/docs/TENANT1_REQUIREMENTS.md`
- `backend/docs/TENANT1_EXECUTION_PLAN.md`
- `backend/docs/ROADMAP.md`
- `backend/docs/RELEASE_CHECKLIST.md`

Reference docs (not task lists, but part of the review):
- `backend/docs/API.md`
- `backend/docs/TENANT1_WEB_UX_BLUEPRINT.md`
- `backend/README.md`
- `backend/HELP.md`
- `backend/copilot-instructions.md`
- `frontend/public/products/README.txt`

## 2. Executive summary

Overall state:
- Public commerce flow: implemented and working
- Core tenant-aware backend: implemented and tested
- Security hardening: implemented for release safety baseline, with a few operational gaps still open
- Internal dashboard: implemented (6 pages: Resumen, Pedidos, Clientes, Finanzas, Stock, Producción)
- Finance (income/expenses): implemented — expenses manual, income automatic from confirmed orders; FinanzasPage redesigned (income form removed, 3-card summary, categories panel, no type filter)
- Stock: implemented — entries manual, withdrawals automatic on order confirmation; confirmation now blocked if stock is insufficient (409 with shortage details)
- Production module: implemented — batch tracking with auto stock ENTRY on creation and auto ENTRY removal on batch deletion
- Automations (WhatsApp, email, PDF): not implemented yet

Key takeaways:
- The public site runs the full flow: home → catalog → cart → checkout → confirmation.
- Tenant-aware public order validation is implemented in backend.
- CRUD for tenants, products, clients, orders, and users exists.
- Dashboard has 6 operational pages; the remaining gaps are automations.
- When an order is confirmed: stock is validated first (409 if insufficient), then automatically deducted per item, and the sale appears automatically in the finance summary.
- When a production batch is registered: a stock ENTRY is created atomically. When a batch is deleted: the corresponding stock ENTRY is removed.
- Release readiness is close: production secret handling, production CORS, bootstrap policy, and rollback documentation are done; remaining gaps are operational verification and automations.

## 3. File-by-file status

### `TENANT1_REQUIREMENTS.md`

#### 1) Web public for customers

| Item | Status | Evidence |
|---|---|---|
| Home with branding | Implemented | `frontend/src/pages/HomePage.jsx`, `frontend/src/components/Shell.jsx` |
| Product catalog | Implemented | `frontend/src/pages/CatalogPage.jsx`, `frontend/src/services/api.js` |
| Cart and order building | Implemented | `frontend/src/state/cart.jsx`, `frontend/src/pages/CatalogPage.jsx`, `frontend/src/pages/CheckoutPage.jsx` |
| Checkout customer form | Implemented | `frontend/src/pages/CheckoutPage.jsx` |
| Order confirmation | Implemented | `frontend/src/pages/ConfirmationPage.jsx` |
| Automatic order registration in internal panel | Implemented | `backend/src/main/java/com/manifactory/backend/publicapi/service/PublicOrderService.java`, `frontend/src/pages/DashboardPage.jsx` |

#### 2) Internal panel

| Item | Status | Evidence |
|---|---|---|
| Login with username and password | Implemented | `backend/.../auth/controller/AuthController.java`, `frontend/src/pages/DashboardPage.jsx` |
| Clients module | Implemented | `backend/.../clients/controller/ClientController.java` (search, orderHistory), `backend/.../clients/service/impl/ClientServiceImpl.java`, `ClientServiceTest.java` (6 tests) |
| Orders module | Implemented | `backend/.../orders/controller/OrderController.java`, `frontend/src/pages/PedidosPage.jsx`. Confirmation blocked with 409 + shortage detail if stock is insufficient |
| Stock module | Implemented | `backend/.../stock/` (entity, repository, service, controller, DTOs, mapper), `frontend/src/pages/StockPage.jsx`, `frontend/src/state/stock-ledger.js`, `V4__stock_entries.sql`, `StockServiceTest.java` (6 tests). Withdrawals auto-created on order CONFIRMED via `OrderServiceImpl.deductStockForOrder()` |
| Income/expenses module | Implemented | `backend/.../finance/` (entity, repository, service, controller, DTOs, mapper), `frontend/src/pages/FinanzasPage.jsx` (redesigned: no income form, income purely automatic, 3-card summary, categories panel), `frontend/src/state/finance-ledger.js`, `V3__finance_entries.sql`, `FinanceServiceTest.java` (5 tests) |
| Production module | Implemented | `backend/.../production/` (entity, repository, service, controller, DTOs, mapper), `frontend/src/pages/ProduccionPage.jsx`, `frontend/src/state/production-batches.js`, `V5__production_batches.sql`, `ProductionBatchServiceTest.java` (5 tests). Auto stock ENTRY on batch create; ENTRY removed on batch delete |

#### 3) Desired automations

| Item | Status | Evidence |
|---|---|---|
| WhatsApp notification | Not implemented | No automation module found |
| Email notification | Not implemented | No automation module found |
| PDF receipt generation | Not implemented | No automation module found |

#### 4) Recommended phases

| Phase item | Status | Notes |
|---|---|---|
| MVP public web | Implemented | Home + catalog + cart + checkout + confirmation exist |
| MVP internal panel | Implemented | Orders, clients, products, finance, stock, and production all implemented |
| Mobile-first simple UX | Implemented in practice | Public flow is responsive and usable; manual full end-to-end validation not yet documented |
| Advanced operation phase | Partial | Production module done; automations and debt tracking not started |
| Expansion phase | Not implemented | Billing, MercadoPago, shipping tracking, stats, app, advanced catalog, special pricing not started |

#### 5) MVP acceptance criteria

| Criterion | Status | Evidence |
|---|---|---|
| Customer can complete a full order from mobile without assistance | Partial | UI and flow exist; manual end-to-end validation not performed |
| Order is saved and appears in internal panel | Implemented | `PublicOrderService.java`, `PedidosPage.jsx` |
| Team can change order status | Implemented | `OrderController.java`, `PedidosPage.jsx`. Confirmar button blocked with inline shortage panel if stock is insufficient |
| Basic stock reflects order impact | Implemented | `OrderServiceImpl.validateStockForOrder()` blocks confirmation if insufficient; `deductStockForOrder()` creates WITHDRAWAL per item on CONFIRMED; `StockPage.jsx` shows current levels |
| Minimum visibility of income/expenses for the month | Implemented | `FinanceController.java` /summary aggregates systemSales + expenses + netResult; `FinanzasPage.jsx` shows 3-card summary (Ventas del mes, Gastos del mes, Resultado neto) |

### `TENANT1_EXECUTION_PLAN.md`

#### Sprint 1

| Item | Status | Evidence |
|---|---|---|
| Formal requirements doc | Implemented | `backend/docs/TENANT1_REQUIREMENTS.md` |
| UX/UI blueprint | Implemented | `backend/docs/TENANT1_WEB_UX_BLUEPRINT.md` |
| Premium frontend base for orders | Implemented | `frontend/src/pages/HomePage.jsx`, `CatalogPage.jsx`, `CheckoutPage.jsx` |
| Public endpoints for catalog and checkout | Implemented | `PublicCatalogController.java`, `PublicOrderController.java` |
| Frontend integration with public endpoints | Implemented | `frontend/src/services/api.js`, `CatalogPage.jsx`, `CheckoutPage.jsx` |
| Manual local end-to-end validation | Pending | Not recorded in any audit |

#### Sprint 2

| Item | Status | Evidence |
|---|---|---|
| Minimum internal orders panel | Implemented | `PedidosPage.jsx`, `OrderController.java` — full list, no silent cap |
| Minimum clients panel | Implemented | `ClientesPage.jsx` with paginated search and per-client order history |
| Mobile UX refinements based on real feedback | Partial | Public UI improved, not formalized as a dedicated sprint item |

#### Sprint 3

| Item | Status | Evidence |
|---|---|---|
| Basic stock operation | Implemented | `StockPage.jsx`, `StockController.java`, auto-deduction on order confirmation, stock validation before confirmation |
| Basic income/expenses operation | Implemented | `FinanzasPage.jsx` (redesigned), `FinanceController.java`, automatic income from orders |
| Full tenant MVP criteria | Partial | 4 of 5 acceptance criteria met; end-to-end manual validation still pending |

### `ROADMAP.md`

| Item | Status | Evidence / notes |
|---|---|---|
| Flyway baseline | Implemented | V1, V2, V3 (finance_entries), V4 (stock_entries), V5 (production_batches) applied |
| Password policy | Implemented | `PasswordPolicyService.java` |
| Change own password endpoint | Implemented | `AuthController.java` |
| Bootstrap user policy | Pending | Bootstrap exists; stricter initial-change policy not implemented |
| Actuator / observability | Implemented | `pom.xml`, `application.properties` |
| Security/error logging | Implemented | `RestAuthenticationEntryPoint.java`, `GlobalExceptionHandler.java` |
| OpenAPI / Swagger | Implemented | `OpenApiConfig.java` |
| API docs alignment | Partial | `API.md` covers most endpoints; Finance, Stock, and Production endpoints not yet documented |
| Login → token → role access tests | Implemented | `SecurityAuthorizationIntegrationTest.java` |
| Cross-tenant security tests | Implemented | `SecurityAuthorizationIntegrationTest.java` |
| Users endpoints coverage | Implemented | `SecurityAuthorizationIntegrationTest.java`, `AppUserServiceTest.java` |

### `RELEASE_CHECKLIST.md`

#### Security

| Item | Status | Evidence |
|---|---|---|
| `JWT_SECRET` configured and not default | Implemented | `application.properties`, `application-prod.properties` |
| `jwt.allow-insecure-dev-key=false` in production | Implemented | `application-prod.properties` |
| `AUTH_BOOTSTRAP_ENABLED=false` in production | Implemented | `application-prod.properties` |
| No default bootstrap credentials when bootstrap is active | Implemented | `application.properties`, `application-prod.properties` |
| Real production CORS origins | Implemented | `application-prod.properties` |
| Rate limit enabled | Implemented | `application-prod.properties`, `RateLimitingFilter.java` |

#### Database

| Item | Status | Evidence |
|---|---|---|
| Flyway enabled | Implemented | `application.properties` |
| Migrations applied | Implemented | V1–V5 in `db/migration`, backend tests pass |
| Recent backup before deploy | Not evidenced | No backup artifact or procedure in repo |

#### API and stability

| Item | Status | Evidence |
|---|---|---|
| `./mvnw.cmd -q test` green | Implemented | 69 tests pass (Finance, Stock, Clients, Orders, Production, Security suites) |
| Swagger disabled in production | Implemented | `application-prod.properties` |
| Health endpoint responds | Configured, not live-verified | Actuator enabled and exposed |
| Validate login, create user, create product, create order | Implemented | `AuthControllerTest`, `AppUserServiceTest`, `ProductServiceTest`, `OrderServiceTest`, integration tests |

#### Operations

| Item | Status | Evidence |
|---|---|---|
| Env vars documented and loaded | Implemented | `backend/.env.example`, `backend/README.md` |
| App/error logs centralized | Partial | Handlers and logs exist; no central log platform configured |
| Rollback plan defined | Implemented | `backend/docs/ROLLBACK_PLAN.md`, `backend/docs/RELEASE_CHECKLIST.md` |

## 4. Code evidence that matters most

### Public catalog and checkout
- Full public flow: `PublicCatalogController.java`, `PublicOrderController.java`, `PublicOrderService.java`
- Rules enforced: minimum 300 kg per order, minimum 10 kg per flavor, tenant-scoped

### Order → Stock validation → Stock deduction → Finance integration
- Order confirm requested → `OrderServiceImpl.validateStockForOrder()` computes required vs available per product; throws `InsufficientStockException` (→ 409 with `shortages` list) if any product is short
- Order confirmed → `OrderServiceImpl.deductStockForOrder()` creates one WITHDRAWAL per item in `stock_entries` (no try/catch — atomic)
- Order confirmed → appears in `FinanceServiceImpl.summary()` systemSales automatically (filters CONFIRMED/SHIPPED/DELIVERED)
- Stock levels: `StockServiceImpl.currentLevels()` computes sum(ENTRY) − sum(WITHDRAWAL) per product

### Production → Stock integration
- Batch registered → `ProductionBatchServiceImpl.create()` saves batch + calls `stockService.create()` with ENTRY in same `@Transactional` (atomic, no try/catch)
- Batch deleted → `ProductionBatchServiceImpl.delete()` finds stock ENTRY by note `"Producción lote #<id>"` via stream filter, deletes it if present, then deletes batch
- Frontend: `ProduccionPage.jsx` shows 2 metric cards (lotes este mes, kg producidos este mes), register form, and full history ledger with Eliminar button

### FinanzasPage redesign
- Income form removed — income is purely automatic from confirmed orders
- `finance-ledger.js`: `FINANCE_INCOME_SOURCES` export removed
- Layout: Hero → loading/error → Resumen panel (period selects in head + 3 metric cards: Ventas del mes, Gastos del mes, Resultado neto) → Gastos por categoría → expense form → ledger without type filter

### Stock validation UX
- `PedidosPage.jsx`: Confirmar button calls `handleConfirmOrder()` which awaits `changeOrderStatus()` result
- `dashboard-context.jsx`: `changeOrderStatus()` returns `{ ok: true }` on success, `{ ok: false, stockShortages: [...] }` on 409, `{ ok: false }` on other errors
- On shortage: inline alert panel appears above order list with product name, required kg, and available kg; closeable

### Tenant-aware rules
- `OrderRulesProperties.java`, `application.properties`, `application-prod.properties`
- Tenant 1: minimum order 300 kg, minimum per flavor 10 kg

### Security
- JWT auth, role access (SUPERADMIN/ADMIN/USER), CORS, rate limit, exception handlers
- `SecurityConfig.java`, `CorsConfig.java`, `RateLimitingFilter.java`, `JwtProperties.java`
- `InsufficientStockException` → `GlobalExceptionHandler.handleInsufficientStock()` → 409 with `{ timestamp, status, error, message, path, shortages }`

### Tests (all passing — 69 total)
- `FinanceServiceTest` — 5 tests
- `StockServiceTest` — 6 tests
- `ClientServiceTest` — 6 tests
- `OrderServiceTest` — 3 tests (includes `updateStatusToConfirmed_createsStockWithdrawals` with mocked `currentLevels`)
- `ProductionBatchServiceTest` — 5 tests
- `SecurityIntegrationTest` — 12 tests (products, orders, clients, finance, stock + 2 production)
- `SecurityAuthorizationIntegrationTest`, `AppUserServiceTest`, `ProductServiceTest`, `TenantServiceTest`, `TenantResolverTest`

## 5. Gaps still open

### Must fix for release readiness
1. Verify backup / restore procedure before the next deploy.
2. Live-verify the health endpoint in the target environment.
3. Centralize application logs if the deployment target requires it.
4. Document Finance, Stock, and Production endpoints in `API.md`.

### Product gaps
1. Automations: WhatsApp notification, email notification, PDF receipt
2. Manual end-to-end validation of the full public flow

### Ecommerce UX gaps
1. Customer-facing order tracking / status visibility
2. Better post-purchase history and re-order flow
3. More advanced catalog filtering / merchandising
4. More complete checkout flow data if business requires it

## 6. Recommended next priorities

### Priority 1 - Automations
- WhatsApp notification on order confirmation (most operationally urgent)
- Email notification as fallback
- PDF receipt on delivery

### Priority 2 - Close documentation
- Document Finance, Stock, and Production endpoints in `API.md`
- Perform and record manual end-to-end validation

### Priority 3 - Ecommerce maturity
- Order history / status tracking for customers
- Better merchandising and catalog filters
- MercadoPago integration

## 7. Decision log

What is solid and tested:
- Public commerce flow (home → catalog → cart → checkout → confirmation)
- Tenant-aware product seed and order validation
- Auth and role-based security
- CRUD for tenants, products, clients, orders, users
- Finance: expense tracking manual, income automatic from confirmed orders; redesigned UI with no income form
- Stock: entries manual, withdrawals automatic on order confirmation; confirmation blocked with 409 + shortage detail if stock insufficient
- Production: batch tracking with auto stock ENTRY on create and ENTRY removal on delete
- Dashboard with 6 operational pages (Resumen, Pedidos, Clientes, Finanzas, Stock, Producción)

What is still not ready:
- Automations (WhatsApp, email, PDF)
- Production-grade operational verification (backup, health check, logs)
- API.md documentation for Finance, Stock, and Production endpoints

## 8. Block implementation history

| Block | Description | Status |
|---|---|---|
| Block 1–4 | Core backend (auth, tenants, products, clients, orders, stock, finance), security hardening, release readiness | Done |
| Block 5 | FinanzasPage redesign — income form removed, 3-card layout, categories panel, period selects in head | Done |
| Block 6 | Production module — V5 migration, entity, repo, DTOs, mapper, service (atomic create + cleanup delete), controller, 5 service tests, 2 security tests, ProduccionPage.jsx, production-batches.js, api.js, App.jsx, Shell.jsx | Done |
| Block 7 | Stock validation before order confirmation — StockShortageDTO, InsufficientStockException, GlobalExceptionHandler 409 handler, validateStockForOrder() in OrderServiceImpl, deductStockForOrder() cleaned (no try/catch), dashboard-context changeOrderStatus returns result object, PedidosPage inline shortage panel | Done |

## 9. Final note

This document is the single source of truth for the current state of the repo at the date above.
It intentionally separates:
- implemented
- partial
- pending

so the next iterations can be done without losing traceability.
