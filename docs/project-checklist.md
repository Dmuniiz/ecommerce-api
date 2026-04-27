# 🛒 E-Commerce API - Project Checklist

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- JWT + Refresh Token
- PostgreSQL
- Flyway
- Docker
- Redis
- Stripe Gateway Payment
- Junit + Mockito
- API Clients Validations
- Testcontainers
- Golang

---

# Architecture & Documentation

- [x] README.md
- [x] Domain documentation
- [X] Use Case Diagram / Doc
- [X] Class Diagram / Doc
- [ ] Architecture documentation
- [ ] Testing strategy documentation
- [ ] Security documentation
- [ ] Deployment documentation

---

# Authentication & Security

- [x] User registration
- [x] User login
- [x] JWT authentication
- [x] Refresh tokens
- [x] Role-based authorization
- [x] Request validation
- [x] Global exception handling
- [x] Rate limiting
- [X] Refresh token rotation

### Pending Security Features
- [ ] Logout endpoint
- [ ] Forgot password
- [ ] Reset password
- [ ] Email verification
- [ ] Security headers
- [ ] Brute-force protection

---

# User Module

### Completed
- [x] User entity
- [x] Role entity
- [x] `GET /users/me`
- [x] `PUT /users/me`
- [X] Address entity

### Pending
- [ ] Address CRUD:
- `PUT /api/v1/addresses/{id}`
- `DELETE /api/v1/addresses/{id}`
- [ ] Admin user management:
- `GET /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`
- [ ] User deletion

---

# Product Catalog

### Completed
- [x] Category entity + table
- [x] Product entity + table

### Pending
- [ ] Category CRUD
- [ ] Product CRUD
- [ ] Product pagination
- [ ] Product sorting
- [ ] Product filtering
- [ ] Product search
- [ ] Product images
- [ ] Product stock management
- [ ] Product variants

---

# Shopping Cart
- [ ] Cart entity
- [ ] CartItem entity
- [ ] Add product to cart
- [ ] Update cart item quantity
- [ ] Remove product from cart
- [ ] Prevent duplicated items
- [ ] Cart total calculation
- [ ] Cart expiration

---

# Order Management

- [ ] Order entity
- [ ] OrderItem entity
- [ ] Checkout flow
- [ ] Order history
- [ ] Order tracking
- [ ] Order cancellation
- [ ] Stock validation
- [ ] Price snapshot

---

# Payment Module

- [ ] Payment entity
- [ ] Payment transaction entity
- [ ] Payment abstraction layer
- [ ] Fake payment gateway
- [ ] Stripe integration
- [ ] Stripe webhook
- [ ] Refund flow
- [ ] Payment retries

---

# Extra Features

- [ ] Wishlist
- [ ] Product reviews
- [ ] Ratings
- [ ] Coupon system
- [ ] Email notifications

### Inventory Module

- [ ] Inventory entity
- [ ] Stock reservation
- [ ] Stock release
- [ ] Inventory logs
- [ ] Low stock alerts

---

# Testing

### Unit Tests
- [ ] Authentication tests
- [ ] Product tests
- [ ] Cart tests
- [ ] Order tests
- [ ] Payment tests

### Integration Tests
- [ ] Repository tests
- [ ] Controller tests
- [ ] Security tests

### Advanced Testing
- [ ] Testcontainers
- [ ] Load testing

---

# Observability

- [ ] Structured logging
- [ ] Spring Boot Actuator
- [ ] Health checks
- [ ] Metrics monitoring

---

# DevOps

- [x] Dockerfile
- [x] Docker Compose
- [ ] Production environment setup
- [ ] Reverse proxy configuration
- [ ] HTTPS setup

---

# Performance

- [ ] Redis caching
- [X] Query optimization
- [X] Database indexing
- [ ] Performance testing

---

# 📈 Future Improvements

- Microservices architecture
- Kafka/RabbitMQ
- Multi-payment providers
- Multi-currency support
- Fraud detection
- Multi-tenant support

---

## Author

Developed by **Davy Muniz**