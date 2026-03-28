# Use Cases

## Overview

This document describes the main use cases of the e-commerce API.

The objective is to represent the core interactions between users, administrators, and the system, covering authentication, catalog management, cart operations, checkout, orders, payments, and addresses.

---

## Actors

- `Visitor`: unauthenticated user
- `Authenticated User`: logged-in customer
- `Admin`: user with administrative privileges
- `Payment Gateway`: external payment provider such as Stripe

---

## Authentication Use Cases

### UC-01: Register User

**Actors**
- Visitor

**Preconditions**
- User is not authenticated
- Email is not already registered

**Main Flow**
1. User sends registration data
2. System validates input
3. System checks if email already exists
4. System encrypts password
5. System creates the user
6. System assigns default role `ROLE_USER`
7. System returns successful response

**Alternative Flows**
- If email is already registered, system returns an error
- If input is invalid, system returns validation errors

**Postconditions**
- A new user account is created

---

### UC-02: Login User

**Actors**
- Visitor

**Preconditions**
- User account exists
- Credentials are valid

**Main Flow**
1. User sends email and password
2. System validates credentials
3. System generates JWT token
4. System returns authentication response

**Alternative Flows**
- If credentials are invalid, system returns unauthorized error

**Postconditions**
- User receives a valid JWT token

---

### UC-03: Get Authenticated User Profile

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated with valid JWT

**Main Flow**
1. User calls protected endpoint
2. System validates JWT
3. System loads authenticated user data
4. System returns user profile

**Postconditions**
- Authenticated user information is returned

---

## Catalog Use Cases

### UC-04: List Products

**Actors**
- Visitor
- Authenticated User

**Preconditions**
- None

**Main Flow**
1. User requests product list
2. System fetches available products
3. System applies pagination and filters if provided
4. System returns product list

**Alternative Flows**
- If no products exist, system returns empty list

**Postconditions**
- Product list is returned

---

### UC-05: View Product Details

**Actors**
- Visitor
- Authenticated User

**Preconditions**
- Product exists

**Main Flow**
1. User requests product by ID
2. System searches product
3. System returns product details

**Alternative Flows**
- If product does not exist, system returns not found error

**Postconditions**
- Product details are returned

---

### UC-06: Create Product

**Actors**
- Admin

**Preconditions**
- Admin is authenticated
- Category exists
- Product data is valid

**Main Flow**
1. Admin sends product data
2. System validates request
3. System checks related category
4. System creates product
5. System returns created product

**Alternative Flows**
- If category does not exist, system returns error
- If input is invalid, system returns validation errors

**Postconditions**
- A new product is created

---

### UC-07: Update Product

**Actors**
- Admin

**Preconditions**
- Admin is authenticated
- Product exists

**Main Flow**
1. Admin sends update request
2. System validates request
3. System loads existing product
4. System updates product fields
5. System saves changes
6. System returns updated product

**Alternative Flows**
- If product does not exist, system returns not found error

**Postconditions**
- Product is updated

---

### UC-08: Delete Product

**Actors**
- Admin

**Preconditions**
- Admin is authenticated
- Product exists

**Main Flow**
1. Admin requests deletion
2. System finds product
3. System deletes or deactivates product
4. System returns success response

**Alternative Flows**
- If product does not exist, system returns not found error

**Postconditions**
- Product is removed or deactivated

---

## Category Use Cases

### UC-09: Manage Categories

**Actors**
- Admin

**Preconditions**
- Admin is authenticated

**Main Flow**
1. Admin creates, updates, lists, or deletes categories
2. System validates requests
3. System persists changes
4. System returns response

**Postconditions**
- Categories are maintained for catalog organization

---

## Cart Use Cases

### UC-10: View Active Cart

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated

**Main Flow**
1. User requests active cart
2. System loads or creates user cart
3. System returns cart with items and totals

**Postconditions**
- User sees current cart state

---

### UC-11: Add Product to Cart

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Product exists
- Product is active
- Quantity is greater than zero

**Main Flow**
1. User sends product ID and quantity
2. System validates request
3. System checks product availability
4. System loads user cart
5. System adds new cart item or updates existing one
6. System recalculates totals
7. System returns updated cart

**Alternative Flows**
- If product does not exist, system returns error
- If quantity is invalid, system returns validation error

**Postconditions**
- Product is added to cart

---

### UC-12: Update Cart Item Quantity

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Cart item exists and belongs to user

**Main Flow**
1. User sends new quantity
2. System validates quantity
3. System updates cart item
4. System recalculates totals
5. System returns updated cart

**Alternative Flows**
- If cart item does not exist, system returns error
- If quantity is zero or negative, system returns validation error

**Postconditions**
- Cart item quantity is updated

---

### UC-13: Remove Item from Cart

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Cart item exists and belongs to user

**Main Flow**
1. User requests item removal
2. System removes item from cart
3. System recalculates totals
4. System returns updated cart

**Postconditions**
- Item is removed from cart

---

### UC-14: Clear Cart

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated

**Main Flow**
1. User requests cart clear operation
2. System removes all cart items
3. System returns empty cart

**Postconditions**
- Cart becomes empty

---

## Order Use Cases

### UC-15: Checkout Cart

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Cart exists
- Cart contains at least one item

**Main Flow**
1. User requests checkout
2. System validates cart contents
3. System validates stock for all items
4. System creates order
5. System converts cart items into order items
6. System copies purchase-time product data
7. System calculates total order amount
8. System updates order status to initial value
9. System clears cart
10. System returns created order

**Alternative Flows**
- If cart is empty, system rejects checkout
- If stock is insufficient, system rejects checkout

**Postconditions**
- New order is created
- Cart is cleared

---

### UC-16: View Order History

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated

**Main Flow**
1. User requests order list
2. System retrieves user orders
3. System returns order history

**Postconditions**
- User sees personal order list

---

### UC-17: View Order Details

**Actors**
- Authenticated User
- Admin

**Preconditions**
- Order exists
- Order belongs to user or requester is admin

**Main Flow**
1. User requests order details
2. System validates access
3. System returns order with items and payment summary

**Alternative Flows**
- If order does not exist, system returns error
- If access is not allowed, system returns forbidden error

**Postconditions**
- Order details are returned

---

### UC-18: Update Order Status

**Actors**
- Admin

**Preconditions**
- Admin is authenticated
- Order exists

**Main Flow**
1. Admin sends new status
2. System validates status transition
3. System updates order
4. System returns updated order

**Alternative Flows**
- If transition is invalid, system returns business error

**Postconditions**
- Order status is updated

---

### UC-19: Cancel Order

**Actors**
- Authenticated User
- Admin

**Preconditions**
- Order exists
- Cancellation is still allowed by business rules

**Main Flow**
1. User requests order cancellation
2. System validates if order can be cancelled
3. System updates order status to cancelled
4. System returns updated order

**Alternative Flows**
- If order is already paid/shipped and cancellation is not allowed, system rejects request

**Postconditions**
- Order is cancelled when allowed

---

## Payment Use Cases

### UC-20: Create Payment Intent

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Order exists
- Order belongs to user
- Order is awaiting payment

**Main Flow**
1. User requests payment creation
2. System validates order state
3. System calls payment gateway abstraction
4. System creates payment record
5. System returns payment information

**Alternative Flows**
- If order is not eligible for payment, system returns error
- If external payment gateway fails, system returns integration error

**Postconditions**
- Payment attempt is created

---

### UC-21: Process Payment with Fake Provider

**Actors**
- System

**Preconditions**
- Payment request is valid

**Main Flow**
1. System calls fake provider
2. System simulates payment response
3. System stores payment result
4. System updates order if payment succeeded

**Postconditions**
- Payment flow can be tested without external dependency

---

### UC-22: Process Payment with Stripe

**Actors**
- Authenticated User
- Payment Gateway

**Preconditions**
- Stripe integration is configured
- Order is eligible for payment

**Main Flow**
1. System creates Stripe payment intent
2. Stripe returns external payment reference
3. System stores provider payment ID
4. System returns payment details to client

**Postconditions**
- Stripe payment flow is initialized

---

### UC-23: Receive Stripe Webhook

**Actors**
- Payment Gateway

**Preconditions**
- Webhook endpoint is configured
- Stripe sends event notification

**Main Flow**
1. Stripe sends webhook event
2. System validates webhook signature
3. System identifies payment record
4. System updates payment status
5. System updates order status if needed
6. System returns success response

**Alternative Flows**
- If webhook signature is invalid, system rejects request
- If event was already processed, system ignores duplicate safely

**Postconditions**
- Local payment state matches Stripe event

---

## Address Use Cases

### UC-24: Create Address

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated

**Main Flow**
1. User sends address data
2. System validates request
3. System stores address linked to user
4. System returns created address

**Postconditions**
- Address is created

---

### UC-25: Update Address

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Address belongs to user

**Main Flow**
1. User sends updated address data
2. System validates ownership
3. System updates address
4. System returns updated address

**Postconditions**
- Address is updated

---

### UC-26: Delete Address

**Actors**
- Authenticated User

**Preconditions**
- User is authenticated
- Address belongs to user

**Main Flow**
1. User requests address deletion
2. System validates ownership
3. System deletes address
4. System returns success response

**Postconditions**
- Address is removed

---

## Administrative Use Cases

### UC-27: List Users

**Actors**
- Admin

**Preconditions**
- Admin is authenticated

**Main Flow**
1. Admin requests user list
2. System validates admin access
3. System returns users

**Postconditions**
- Admin can inspect registered users

---

### UC-28: View Operational Metrics

**Actors**
- Admin
- Operator

**Preconditions**
- Monitoring tools are configured

**Main Flow**
1. Operator accesses metrics endpoint or dashboard
2. System exposes health and performance metrics
3. Monitoring stack displays application status

**Postconditions**
- Operational visibility is available

---

## Future Use Cases

These can be added later:

- Apply coupon to cart
- Refund payment
- Add product review
- Add product to wishlist
- Manage product variants
- Receive low-stock alerts
- Enforce rate limiting on sensitive endpoints
- Send email notifications for order updates

---

## Notes

This use case document should evolve together with the implementation.

Recommended next related documents:
- `docs/domain-model.md`
- `docs/architecture.md`
- `docs/api-contract.md`
- `docs/testing-strategy.md`
