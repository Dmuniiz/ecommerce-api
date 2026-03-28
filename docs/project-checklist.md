## Project Checklist

### Week 1: Foundation and Security
- [X] Create the Spring Boot project
- [X] Configure `PostgreSQL`
- [X] Add base dependencies
- [ ] Draw.io - Initial domain model
- [ ] Dbdiagram.io - DB Model
- [ ] Configure `Spring Security`
- [ ] Configure JWT authentication
- [ ] Add `Flyway`
- [X] Create initial project structure
- [ ] Create `README.md`
- [ ] Create `docs/domain-model.md`
- [ ] Model `User` entity
- [ ] Model `Role` entity
- [ ] Model `Address` entity
- [ ] Model `Category` entity
- [ ] Model `Product` entity
- [ ] Create DTOs for auth and catalog
- [ ] Add MapStruct mappers for initial modules
- [ ] Implement `POST /api/v1/auth/register`
- [ ] Implement `POST /api/v1/auth/login`
- [ ] Implement `GET /api/v1/auth/me`
- [ ] Add role-based authorization
- [ ] Add request validation
- [x] Add global exception handling
- [ ] Configure Swagger/OpenAPI
- [ ] Write initial unit tests for authentication

### Week 2: Catalog and Cart
- [ ] Implement category CRUD
- [ ] Implement product CRUD
- [ ] Add product pagination
- [ ] Add product sorting
- [ ] Add basic product filtering
- [ ] Create `Cart` entity
- [ ] Create `CartItem` entity
- [ ] Create cart DTOs
- [ ] Create cart mapper
- [ ] Implement `GET /api/v1/cart`
- [ ] Implement `POST /api/v1/cart/items`
- [ ] Implement `PUT /api/v1/cart/items/{itemId}`
- [ ] Implement `DELETE /api/v1/cart/items/{itemId}`
- [ ] Implement cart total calculation
- [ ] Prevent duplicated cart items for the same product
- [ ] Add unit tests for product rules
- [ ] Add unit tests for cart rules
- [ ] Add integration tests for repositories

### Week 3: Orders and Payments
- [ ] Create `Order` entity
- [ ] Create `OrderItem` entity
- [ ] Create `Payment` entity
- [ ] Create `OrderStatus` enum
- [ ] Create `PaymentStatus` enum
- [ ] Create `PaymentProvider` enum
- [ ] Create order DTOs
- [ ] Create payment DTOs
- [ ] Create order mapper
- [ ] Create payment mapper
- [ ] Implement `POST /api/v1/orders/checkout`
- [ ] Implement `GET /api/v1/orders`
- [ ] Implement `GET /api/v1/orders/{id}`
- [ ] Validate stock during checkout
- [ ] Copy product price into `OrderItem`
- [ ] Clear cart after checkout
- [ ] Create `PaymentGateway` interface
- [ ] Implement `FakePaymentGateway`
- [ ] Implement `POST /api/v1/payments/intent`
- [ ] Start Stripe integration
- [ ] Add initial Stripe webhook endpoint
- [ ] Add unit tests for checkout flow
- [ ] Add integration tests for orders and payments

### Week 4: Production Readiness
- [ ] Refactor services and mappers
- [ ] Improve test coverage
- [ ] Add Testcontainers
- [ ] Create `Dockerfile`
- [ ] Create `docker-compose.yml`
- [ ] Run API with Docker
- [ ] Add structured logging
- [ ] Add `Spring Boot Actuator`
- [ ] Expose Prometheus metrics
- [ ] Configure Grafana
- [ ] Add rate limiting with `Bucket4j`
- [ ] Protect login endpoint with rate limiting
- [ ] Protect register endpoint with rate limiting
- [ ] Protect payment endpoints with rate limiting
- [ ] Create `docs/architecture.md`
- [ ] Create `docs/api-contract.md`
- [ ] Create `docs/testing-strategy.md`
- [ ] Review and update `README.md`

### Must-Have Features
- [ ] JWT authentication
- [ ] Role-based authorization
- [ ] Product CRUD
- [ ] Category CRUD
- [ ] Cart management
- [ ] Checkout flow
- [ ] Order management
- [ ] Stock validation
- [ ] Validation and exception handling
- [ ] Unit tests
- [ ] Integration tests

### Should-Have Features
- [ ] Product search and filtering
- [ ] Address management
- [ ] Payment abstraction
- [ ] Stripe integration
- [ ] Stripe webhook
- [ ] Order status lifecycle
- [ ] Payment status lifecycle
- [ ] Swagger/OpenAPI
- [ ] Flyway
- [ ] MapStruct

### Optional Features
- [ ] MORE User roles and permissions management (Manager, Support, etc.)
- [ ] Coupon system
- [ ] Inventory management
- [ ] Refund flow
- [ ] Product variants
- [ ] Wishlist
- [ ] Reviews and ratings
- [ ] Soft delete
- [ ] Audit logs
- [ ] Email notifications
- [ ] Redis caching
- [ ] Multiple payment providers

### Tooling
- [ ] Docker
- [ ] Docker Compose
- [ ] Structured logging
- [ ] Spring Boot Actuator
- [ ] Prometheus
- [ ] Grafana
- [ ] Rate limiting