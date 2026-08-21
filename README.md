# E-Commerce Inventory & Order Fulfillment Engine

A Spring Boot REST API for managing users, products, inventory, orders, coupons, payments, authentication, caching, and audit logging.

This project implements the requirements from both the E-Commerce Inventory & Order Fulfillment Engine Phase 1 assignment and the Phase 2 Security, Coupons, Caching & Audit Logging assignment.

## Technologies Used

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- Spring Cache (in-memory cache)
- Spring AOP / AspectJ
- Spring for Apache Kafka
- Apache Kafka (local broker)
- Spring Boot Actuator
- Micrometer
- Spring Scheduling
- MySQL 8
- Gradle
- Swagger / OpenAPI
- Postman
- Git and GitHub

## Prerequisites

Before running the application, install:

- Java 21
- MySQL 8.0 or later
- Apache Kafka 4.x (local broker)
- Git
- Postman
- Spring Tool Suite (STS), IntelliJ IDEA, or another Java IDE

Check Java:

```bash
java -version
```

The Gradle build uses the Java 21 toolchain.

## Database Setup

Create the MySQL database:

```sql
CREATE DATABASE ecommerce_inventory;
```

The application connects to MySQL using:

```text
src/main/resources/application.properties
```

Configure your local credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_inventory
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.defer-datasource-initialization=true

server.port=8080
```

Do not commit database passwords, JWT secrets, or other local credentials to GitHub.

The project also contains:

```text
src/main/resources/data.sql
```

for database startup data used by the application.

### Existing Database Migration

If an existing database contains the old `orders.created_at` column from an earlier project version, rename it to the assignment-required `order_date` column:

```sql
USE ecommerce_inventory;

ALTER TABLE orders
CHANGE COLUMN created_at order_date DATETIME NOT NULL;
```

## Project Structure

```text
src/main/java/com/example/ecommerce/
├── audit/
│   └── AuditContext.java
│
├── config/
│   ├── AdminSeeder.java
│   ├── CacheConfig.java
│   ├── KafkaConfig.java
│   ├── KafkaHealthIndicator.java
│   ├── OpenApiConfig.java
│   ├── PasswordConfig.java
│   ├── SchedulingConfig.java
│   └── SecurityConfig.java
│
├── controller/
│   ├── AuditLogController.java
│   ├── AuthController.java
│   ├── CouponController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ProductController.java
│   └── UserController.java
│
├── dto/
│   ├── CouponRequest.java
│   ├── CouponResponse.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── OrderItemRequest.java
│   ├── OrderRequest.java
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── RegisterRequest.java
│   ├── UserRequest.java
│   └── UserResponse.java
│
├── entity/
│   ├── AuditLog.java
│   ├── Coupon.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── Payment.java
│   ├── PaymentStatus.java
│   ├── Product.java
│   ├── Role.java
│   └── User.java
│
├── event/
│   ├── OrderCreatedEvent.java
│   ├── OrderCreatedEventPublisher.java
│   ├── OrderCreatedKafkaPublisher.java
│   ├── OrderCreatedKafkaListener.java
│   └── OrderCreatedItem.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── InsufficientStockException.java
│   ├── InvalidCouponException.java
│   ├── InvalidStateTransitionException.java
│   └── ResourceNotFoundException.java
│
├── repository/
│   ├── AuditLogRepository.java
│   ├── CouponRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ProductRepository.java
│   └── UserRepository.java
│
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
│
├── service/
│   ├── AuditService.java
│   ├── AuthService.java
│   ├── CouponService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   ├── ProductService.java
│   ├── ScheduledTaskService.java
│   └── UserService.java
│
└── specification/
    └── ProductSpecification.java
```

## Build the Application

From the project root:

```cmd
gradlew.bat clean build
```

A successful build ends with:

```text
BUILD SUCCESSFUL
```

## Run the Application

```cmd
gradlew.bat bootRun
```

The application starts at:

```text
http://localhost:8080
```

## Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Use the **Authorize** button in Swagger and enter the JWT token returned by the login endpoint.

The value must be the JWT itself. Swagger sends it as:

```text
Authorization: Bearer <JWT_TOKEN>
```

## Authentication and RBAC

### Register

```http
POST /api/v1/auth/register
```

Example:

```json
{
  "name": "New Customer",
  "email": "newcustomer@example.com",
  "password": "YourPassword"
}
```

Registration creates a CUSTOMER account.

### Login

```http
POST /api/v1/auth/login
```

Example:

```json
{
  "email": "customer@example.com",
  "password": "YOUR_CUSTOMER_PASSWORD"
}
```

The response contains:

```json
{
  "token": "<JWT>",
  "email": "customer@example.com",
  "role": "CUSTOMER"
}
```

### Test Accounts

The current database contains these test identities:

| Purpose | Email | Role |
|---|---|---|
| Admin | `admin@example.com` | ADMIN |
| Customer | `customer@example.com` | CUSTOMER |

Passwords are not stored in this README. Use the passwords configured when these accounts were created and put them into the Postman environment before running the login requests.

### Role Permissions

#### CUSTOMER

- Browse products
- Search/filter products
- Place orders
- View own order history
- Process payment for own orders
- Access customer-level endpoints only

#### ADMIN

- Add products
- Update products/inventory
- Delete products
- Create coupons
- Change order status
- View audit logs

Admin-only operations return HTTP 403 for CUSTOMER users.

## API Endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |

### Users

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/users` | Authenticated |
| GET | `/api/v1/users` | Authenticated |
| GET | `/api/v1/users/{id}` | Authenticated |
| DELETE | `/api/v1/users/{id}` | Authenticated / according to current security configuration |

### Products

The assignment-compliant catalog endpoint is:

```http
GET /api/v1/products
```

It supports all of the required optional parameters:

- `category`
- `minPrice`
- `maxPrice`
- `inStock`
- `page`
- `size`
- `sort`

Examples:

```http
GET /api/v1/products
```

```http
GET /api/v1/products?category=phone
```

```http
GET /api/v1/products?minPrice=1000&maxPrice=50000
```

```http
GET /api/v1/products?inStock=true
```

```http
GET /api/v1/products?page=0&size=5
```

```http
GET /api/v1/products?sort=price,desc
```

Multiple sort values can be supplied:

```http
GET /api/v1/products?sort=category,asc&sort=price,desc
```

Other product endpoints:

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/products` | ADMIN |
| GET | `/api/v1/products/{id}` | CUSTOMER / ADMIN |
| PUT | `/api/v1/products/{id}` | ADMIN |
| DELETE | `/api/v1/products/{id}` | ADMIN |

A backward-compatible search endpoint is also retained:

```http
GET /api/v1/products/search
```

### Product Caching

Spring Cache is enabled with an in-memory cache.

The catalog GET endpoint uses:

```java
@Cacheable(value = "products")
```

Product creation, update, and deletion evict the product caches so stale inventory and pricing are not served.

### Orders

Create order:

```http
POST /api/v1/orders
```

Example without a coupon:

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

Example with a coupon:

```json
{
  "userId": 1,
  "couponCode": "SAVE20",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

Order processing:

1. Validate the user.
2. Validate each product.
3. Check available stock.
4. Calculate item totals.
5. Validate an optional coupon.
6. Apply the coupon discount.
7. Deduct inventory.
8. Save the order with `PENDING` status.

Order endpoints:

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/orders` | CUSTOMER / ADMIN |
| GET | `/api/v1/orders/{id}` | CUSTOMER / ADMIN (ownership enforced for CUSTOMER) |
| GET | `/api/v1/orders/my-orders` | CUSTOMER / ADMIN |
| GET | `/api/v1/orders/user/{userId}` | ADMIN |
| PATCH | `/api/v1/orders/{id}/status?newStatus={STATUS}` | ADMIN |

### Order State Machine

Allowed transitions:

```text
PENDING  -> PAID
PENDING  -> CANCELLED

PAID     -> SHIPPED
PAID     -> CANCELLED

SHIPPED  -> terminal
CANCELLED -> terminal
```

Example:

```http
PATCH /api/v1/orders/1/status?newStatus=PAID
```

```http
PATCH /api/v1/orders/1/status?newStatus=SHIPPED
```

```http
PATCH /api/v1/orders/1/status?newStatus=CANCELLED
```

Cancellation restores the purchased inventory within the same transaction.

### Inventory

If a product has:

```text
Initial stock: 10
Order quantity: 2
```

the remaining stock becomes:

```text
8
```

If the order is cancelled, the stock is restored to:

```text
10
```

Insufficient stock is rejected with HTTP 400.

The Product entity uses JPA optimistic locking through `@Version` to help protect concurrent inventory updates.

## Coupons

Coupon creation is ADMIN-only:

```http
POST /api/v1/admin/coupons
```

Example:

```json
{
  "code": "SAVE20",
  "discountPercent": 20,
  "expirationDate": "2026-12-31T23:59:59",
  "isActive": true
}
```

Coupon validation checks:

1. Coupon exists.
2. Coupon is active.
3. Coupon expiration date is after the current timestamp.

An invalid, inactive, expired, or non-existent coupon produces HTTP 400 through `InvalidCouponException`.

## Payments

Payment processing is an additional feature implemented on top of the assignment order workflow.

Process payment:

```http
POST /api/v1/payments
```

Example:

```json
{
  "orderId": 1
}
```

A successful payment creates a payment record and moves the order from `PENDING` to `PAID`.

Get payment for an order:

```http
GET /api/v1/payments/order/1
```

Payment states:

```text
PENDING
SUCCESS
FAILED
```

## Audit Logging

The audit system records business state changes through the centralized `AuditService`.

The audit log stores:

- `entity_name`
- `action`
- `changed_by`
- `timestamp`
- `details`

Tracked events include:

- Order status changes
- Product stock adjustments
- Stock reduction during order creation
- Inventory restoration during cancellation
- Product inventory updates
- Product creation, update, and deletion events
- Coupon creation and coupon application
- Successful payment events

Admin audit endpoint:

```http
GET /api/v1/admin/audit-logs?page=0&size=10&sort=id,desc
```

CUSTOMER access is rejected with HTTP 403.

## Validation and Exception Handling

The application uses `@RestControllerAdvice` for consistent JSON errors.

Standard response structure:

```json
{
  "timestamp": "2026-08-14T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "/api/v1/orders"
}
```

Exception mapping includes:

| Exception | HTTP Status | Example |
|---|---:|---|
| `ResourceNotFoundException` | 404 | Invalid user/product/order ID |
| `InsufficientStockException` | 400 | Requested quantity exceeds stock |
| `InvalidCouponException` | 400 | Invalid/expired/inactive coupon |
| `InvalidStateTransitionException` | 422 | Illegal order-state transition |
| Validation errors | 400 | Invalid request payload |
| Access denied | 403 | CUSTOMER calling an ADMIN-only endpoint |

## Postman Testing

The project includes a Postman collection and environment in the `postman` directory.

The collection covers both Phase 1 and Phase 2 requirements, including:

### Authentication

- Register
- Login as customer
- Login as admin
- Bearer-token authorization
- CUSTOMER attempting an ADMIN-only endpoint

### Product Catalog

- Catalog pagination
- Category filtering
- Minimum price
- Maximum price
- Price range
- In-stock filtering
- Multiple filters
- Out-of-bounds page
- Price sorting
- Multi-field sorting
- Product creation
- Product update
- Product deletion
- Cache read / cache eviction checks

### Orders

- Order creation
- Order retrieval
- My orders
- Insufficient stock
- Invalid quantity
- Invalid user/product
- Coupon order
- Order payment
- Shipping
- Illegal status transition
- Cancellation
- Inventory restoration

### Coupons

- Admin coupon creation
- Valid coupon application
- Invalid coupon
- Expired coupon
- Inactive coupon

### Audit

- Admin audit-log retrieval
- Audit pagination and sorting
- CUSTOMER audit-log access returning 403

Import the environment and collection, start the application, set the admin/customer passwords in the environment, and run the requests in order.

### Phase 3 - Kafka & Observability

The collection also includes:

- `POST /api/v1/orders` to trigger the post-commit Kafka `OrderCreatedEvent`
- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/metrics`
- `GET /actuator/metrics/orders.revenue.total`

Kafka itself is not accessed through Postman. Start the local Kafka broker separately and observe the asynchronous listener processing in the Spring Boot console.

## Phase 3 - Kafka Event Messaging, Background Scheduling & Observability

Phase 3 extends the existing Phase 1 and Phase 2 application with asynchronous event processing, scheduled background jobs, and production observability. The Docker / Docker Compose module is intentionally not implemented.

### Kafka Configuration

The project uses Apache Kafka instead of RabbitMQ.

Kafka broker:

```text
localhost:9092
```

Kafka topic:

```text
order-created
```

The local Kafka broker must be running before the Spring Boot application is started.

Example Windows Kafka startup:

```cmd
cd /d D:\kafka\kafka
bin\windows\kafka-server-start.bat config\server.properties
```

Verify the topic:

```cmd
bin\windows\kafka-topics.bat --list --bootstrap-server 127.0.0.1:9092
```

Expected topic:

```text
order-created
```

### Asynchronous OrderCreatedEvent

When `POST /api/v1/orders` completes successfully:

1. The order, order items, and inventory changes are processed in the existing database transaction.
2. An `OrderCreatedEvent` is created containing the order ID, user email, and line items.
3. The Spring transaction event is published to Kafka only after the database transaction commits.
4. The Kafka listener receives the message asynchronously.
5. The listener logs processing start.
6. The listener waits 2 seconds to simulate receipt/notification processing.
7. The listener logs processing completion.

Kafka processing therefore runs outside the main HTTP request/response work.

### Scheduled Background Jobs

The application enables Spring scheduling through `@EnableScheduling`.

#### Hourly coupon expiry

Cron:

```text
0 0 * * * *
```

The job finds active coupons whose expiration date is before the current timestamp and marks them inactive.

#### Daily low-stock scan

Cron:

```text
0 0 0 * * *
```

The job finds products with:

```text
stock_quantity < 5
```

and creates a low-stock alert audit record for administrators.

### Actuator and Micrometer

The application exposes:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
```

The readiness and liveness health groups are also enabled:

```text
http://localhost:8080/actuator/health/liveness
http://localhost:8080/actuator/health/readiness
```

### Custom Kafka Health Check

A custom `KafkaHealthIndicator` checks whether the Kafka broker and configured `order-created` topic are reachable.

The result is included in:

```text
/actuator/health
```

### Revenue Metric

The application registers the Micrometer counter:

```text
orders.revenue.total
```

View it at:

```text
http://localhost:8080/actuator/metrics/orders.revenue.total
```

The counter increases by the total amount of each successfully committed order.

### Phase 3 Postman Requests

The Postman collection includes:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/metrics`
- `GET /actuator/metrics/orders.revenue.total`
- `POST /api/v1/orders` for triggering the Kafka `OrderCreatedEvent` flow

### Docker

Docker / Docker Compose is intentionally not implemented.

## GitHub Repository

Repository:

```text
https://github.com/viveknred/ecommerce-inventory
```

## Git Hygiene

Phase 2 changes should be committed on top of Phase 1 using descriptive commit messages, for example:

```text
feat: add JWT authentication
feat: implement RBAC
feat: add coupon engine
feat: implement product caching
feat: add audit logging
feat: add kafka order events
feat: add scheduled background jobs
feat: add actuator observability
chore: update README and Postman collection
```

Do not commit:

- `build/`
- IDE configuration files
- local database credentials
- JWT secrets
- other local secrets

## Running the Project from GitHub

Clone:

```cmd
git clone https://github.com/viveknred/ecommerce-inventory.git
```

Enter the project directory:

```cmd
cd ecommerce-inventory
```

Configure MySQL in:

```text
src/main/resources/application.properties
```

Build:

```cmd
gradlew.bat clean build
```

Run:

```cmd
gradlew.bat bootRun
```

Application:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## Submission Checklist

### Assignment 1

- [x] MySQL database
- [x] Users
- [x] Products and indexed category
- [x] Optimistic locking with `@Version`
- [x] Product filtering
- [x] Pagination
- [x] Sorting
- [x] Transactional order creation
- [x] Stock deduction and rollback
- [x] Order state machine
- [x] Cancellation stock restoration
- [x] Global exception handling
- [x] Postman collection
- [x] README

### Assignment 2

- [x] JWT authentication
- [x] BCrypt password hashing
- [x] Role-based access control
- [x] Coupon creation and validation
- [x] Coupon integration with orders
- [x] Spring Cache product caching
- [x] Cache eviction on product changes
- [x] Spring AOP audit logging
- [x] Audit-log database table
- [x] Admin-only paginated audit-log endpoint
- [x] Phase 2 Postman requests
- [x] README credentials/JWT instructions

### Assignment 3

- [x] Spring for Apache Kafka integration
- [x] OrderCreatedEvent DTO
- [x] Publish OrderCreatedEvent after transaction commit
- [x] Asynchronous Kafka listener
- [x] 2-second receipt/notification simulation
- [x] Hourly coupon-expiration scheduler
- [x] Daily low-stock scheduler
- [x] Actuator health endpoint
- [x] Actuator metrics endpoint
- [x] Liveness/readiness health groups enabled
- [x] Custom Kafka HealthIndicator
- [x] `orders.revenue.total` Micrometer counter
- [x] Phase 3 Postman requests
- [x] Phase 3 README documentation
- [ ] Dockerfile
- [ ] docker-compose.yml

