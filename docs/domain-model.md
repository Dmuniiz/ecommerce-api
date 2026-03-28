# Domain Model

## Overview

This document describes the core business entities, relationships, and rules for the e-commerce API.

The goal of the domain model is to represent a realistic e-commerce backend that supports authentication, product catalog management, shopping cart operations, checkout, order processing, and payments.

## Core Entities

- `User`
- `Role`
- `Address`
- `Category`
- `Product`
- `Cart`
- `CartItem`
- `Order`
- `OrderItem`
- `Payment`

---

## User

Represents an authenticated person who can interact with the platform.

### Main fields

- `id`
- `name`
- `email`
- `password`
- `roles (enum)`
- `enabled`
- `createdAt`
- `updatedAt`
- `lastLoginAt`
- `failedLoginAttempts`
- `isActive`

### Relationships

- one user can have many roles
- one user can have many addresses
- one user has one active cart
- one user can place many orders

### Notes

- email must be unique
- password must be stored encoded
- users should only access their own cart, addresses, and orders unless they are admins

---

## Role

Represents a security role assigned to a user.

### Main fields

- `id`
- `name (enum)`
- `description`

### Example values

- `ROLE_USER`
- `ROLE_ADMIN`

### Relationships

- many roles can be assigned to many users

---

## Address

Represents a user address used for shipping or billing.

### Main fields

- `id`
- `street`
- `number`
- `district`
- `city`
- `state`
- `zipCode`
- `country`
- `complement`
- `type`
- `createdAt`

### Relationships

- many addresses belong to one user

### Notes

- can support address types like `SHIPPING` and `BILLING`
- an order may later store an address snapshot instead of referencing the mutable user address directly

---

## Category

Represents a product grouping.

### Main fields

- `id`
- `name`
- `description`
- `Set<Product> products`

### Relationships

- one category can contain many products

### Notes

- category name should usually be unique
- subcategories can be added later if needed

---

## Product

Represents an item available for purchase.

### Main fields

- `id`
- `name`
- `description`
- `price`
- `stock`
- `active`
- `sku`
- `createdAt`
- `updatedAt`

### Relationships

- many products belong to one category
- one product can appear in many cart items
- one product can appear in many order items

### Notes

- `price` should use `BigDecimal`
- `stock` cannot be negative
- `active` can be used instead of hard delete
- `sku` is useful for product identification

---

## Cart

Represents the current shopping cart of a user.

### Main fields

- `id`
- `user`
- `createdAt`
- `updatedAt`

### Relationships

- one cart belongs to one user
- one cart contains many cart items

### Notes

- a user should have only one active cart
- cart total is usually calculated from cart items, not permanently stored at first

---

## CartItem

Represents a product entry inside a cart.

### Main fields

- `id`
- `cart`
- `product`
- `quantity`
- `unitPrice`

### Relationships

- many cart items belong to one cart
- many cart items reference one product

### Notes

- `quantity` must be greater than zero
- `unitPrice` may mirror the current product price for easier total calculation
- cart items are temporary and can change before checkout

---

## Order

Represents a finalized purchase created from checkout.

### Main fields

- `id`
- `user`
- `status`
- `totalAmount`
- `createdAt`
- `updatedAt`

### Relationships

- many orders belong to one user
- one order contains many order items
- one order can have one or more payment records depending on business choice

### Suggested status values

- `CREATED`
- `PENDING_PAYMENT`
- `PAID`
- `CANCELLED`
- `SHIPPED`
- `DELIVERED`

### Notes

- order total should be calculated from order items
- an order should preserve the state of purchased items at checkout time
- paid orders should not allow arbitrary modification

---

## OrderItem

Represents a purchased product snapshot inside an order.

### Main fields

- `id`
- `order`
- `productId`
- `productName`
- `quantity`
- `unitPrice`
- `subtotal`

### Relationships

- many order items belong to one order

### Notes

- `OrderItem` should store snapshot data
- do not rely only on the current `Product` entity after purchase
- product name and price at purchase time should remain immutable for historical accuracy

---

## Payment

Represents payment information related to an order.

### Main fields

- `id`
- `order`
- `provider`
- `providerPaymentId`
- `status`
- `amount`
- `createdAt`
- `updatedAt`

### Relationships

- many payments can belong to one order, or one payment to one order depending on your model
- for flexibility, allowing multiple payment attempts per order is usually better

### Suggested provider values

- `FAKE`
- `STRIPE`

### Suggested status values

- `PENDING`
- `SUCCEEDED`
- `FAILED`
- `CANCELLED`
- `REFUNDED`

### Notes

- payment status should be separate from order status
- provider-specific IDs should be stored for reconciliation
- Stripe webhook events should update this entity

---

## Main Relationships

- `User` many-to-many `Role`
- `User` one-to-many `Address`
- `User` one-to-one `Cart`
- `User` one-to-many `Order`
- `Category` one-to-many `Product`
- `Cart` one-to-many `CartItem`
- `Product` one-to-many `CartItem`
- `Order` one-to-many `OrderItem`
- `Order` one-to-many `Payment`

---

## Domain Rules

### User rules

- email must be unique
- password must be encrypted
- only authenticated users can access protected resources
- regular users can only access their own data
- admins can manage catalog and administrative resources

### Product rules

- product name must not be blank
- price must be greater than zero
- stock must not be negative
- inactive products should not be purchasable

### Cart rules

- a cart item quantity must be greater than zero
- adding an existing product to the cart should update quantity instead of duplicating rows
- cart total is the sum of all item subtotals
- users can only modify their own cart

### Checkout rules

- checkout requires at least one cart item
- checkout must validate stock before creating the order
- checkout converts cart items into order items
- checkout should freeze purchased product data into order items
- cart may be cleared after successful order creation

### Order rules

- an order must contain at least one item
- order total must match the sum of order item subtotals
- order status changes must follow valid transitions
- cancelled or paid orders should have restricted modifications

### Payment rules

- payment amount must match the expected order amount
- payment status updates should be traceable
- payment confirmation should not duplicate successful charges
- Stripe webhook processing should be idempotent

---

## Suggested Enums

### RoleName

- `ROLE_USER`
- `ROLE_ADMIN`

### OrderStatus

- `CREATED`
- `PENDING_PAYMENT`
- `PAID`
- `CANCELLED`
- `SHIPPED`
- `DELIVERED`

### PaymentStatus

- `PENDING`
- `SUCCEEDED`
- `FAILED`
- `CANCELLED`
- `REFUNDED`

### PaymentProvider

- `FAKE`
- `STRIPE`

### AddressType

- `SHIPPING`
- `BILLING`