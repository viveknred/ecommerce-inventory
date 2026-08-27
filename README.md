# E-Commerce Inventory & Order Fulfillment Engine

A Spring Boot REST API for managing users, products, inventory, orders, coupons, payments, authentication, caching, audit logging, marketplace vendors, verified-purchase reviews, and image uploads.

This project implements the requirements of four assignment phases, each built on top of the last:

| Phase | Assignment | Focus |
|---|---|---|
| 1 | E-Commerce Inventory & Order Fulfillment Engine | Domain model, filtering, transactional orders, order state machine |
| 2 | Security, Coupons, Caching & Audit Logging | JWT authentication, RBAC, coupon engine, Spring Cache, audit trail |
| 3 | Kafka Event Messaging, Scheduling & Observability | Post-commit events, scheduled jobs, Actuator and Micrometer |
| 4 | Multi-Vendor Marketplace, Reviews & File Upload | Flyway migrations, vendors, rating engine, multipart upload |

## Technologies Used

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA
- Hibernate
- Flyway (versioned database migrations)
- Spring Security
- JWT
- Spring Cache (in-memory cache)
- Spring AOP / AspectJ
- Spring for Apache Kafka
- Apache Kafka (local broker)
- Spring Boot Actuator
- Micrometer
- Spring Scheduling
- Spring MVC multipart file upload
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

As of Phase 4 the schema is owned by Flyway, not by Hibernate. Create an empty database and let the application build it:

```sql
CREATE DATABASE ecommerce_inventory;
```

That is the only SQL you have to run by hand. On the first startup Flyway creates every table, index, constraint, and seed row by applying the versioned scripts in `src/main/resources/db/migration`.

The application connects to MySQL using:

```text
src/main/resources/application.properties
```

Configure your local credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_inventory
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

# Flyway owns the schema. Hibernate must never alter it.
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.sql.init.mode=never

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=false
spring.flyway.validate-on-migrate=true

server.port=8080
```

A complete template with every property, including the JWT and media settings, is committed as `src/main/resources/application-example.properties`. Copy it to `application.properties` and fill in your own values.

Do not commit database passwords, JWT secrets, or other local credentials to GitHub.

### Migrations

| Version | Script | Contents |
|---|---|---|
| V1 | `V1__baseline_core_schema.sql` | `users`, `products`, `orders`, `order_items` |
| V2 | `V2__add_coupons_payments_and_audit_logs.sql` | `coupons`, `payments`, `audit_logs` |
| V3 | `V3__seed_product_catalog.sql` | The six catalog products that used to live in `data.sql` |
| V4 | `V4__add_vendors_and_reviews.sql` | `vendors`, `reviews`, and the `vendor_id` / `image_url` / rating columns on `products` |

The version numbers line up with the assignment phases, so V4 contains exactly the Phase 4 schema change and V1 to V3 reconstruct everything the earlier phases had created implicitly.

Flyway records what it has applied in a `flyway_schema_history` table it creates itself. On every later startup it compares the checksum of each script against that history, so an already-migrated database is left untouched and an edited script is reported as an error rather than silently ignored. This is why applied migrations must never be modified: to change the schema, add a new `V5__...` script.

`ddl-auto` is set to `none` rather than `validate` on purpose. Hibernate maps `Boolean` to MySQL's `BIT` while the migrations declare `TINYINT(1)`, so `validate` would fail the schema comparison and refuse to start even though the schema is correct.

### Upgrading a Database From an Earlier Phase

Deleting and recreating the database is the recommended route, and it is what the Phase 4 acceptance criteria assume:

```sql
DROP DATABASE ecommerce_inventory;
CREATE DATABASE ecommerce_inventory;
```

If you have data in an existing Phase 3 database that you want to keep, tell Flyway to adopt the current state as its starting point instead of trying to create tables that already exist:

```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=3
```

Flyway then writes a baseline row for V1 to V3 without running them and applies only V4. Set `baseline-on-migrate` back to `false` afterwards.

One older-schema fix is worth keeping in mind. If a database predates the Phase 1 column rename, `orders` may still carry `created_at` where the assignment requires `order_date`:

```sql
USE ecommerce_inventory;

ALTER TABLE orders
CHANGE COLUMN created_at order_date DATETIME NOT NULL;
```

## Project Structure

Files added in Phase 4 are marked with a trailing comment.

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
│   ├── MediaProperties.java            # Phase 4
│   ├── OpenApiConfig.java
│   ├── PasswordConfig.java
│   ├── SchedulingConfig.java
│   ├── SecurityConfig.java
│   └── WebConfig.java                  # Phase 4
│
├── controller/
│   ├── AuditLogController.java
│   ├── AuthController.java
│   ├── CouponController.java
│   ├── MediaController.java            # Phase 4
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ProductController.java
│   ├── ReviewController.java           # Phase 4
│   ├── UserController.java
│   └── VendorController.java           # Phase 4
│
├── dto/
│   ├── CouponRequest.java
│   ├── CouponResponse.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── MediaUploadResponse.java        # Phase 4
│   ├── OrderItemRequest.java
│   ├── OrderRequest.java
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── ProductReviewsResponse.java     # Phase 4
│   ├── RegisterRequest.java
│   ├── ReviewRequest.java              # Phase 4
│   ├── ReviewResponse.java             # Phase 4
│   ├── UserRequest.java
│   ├── UserResponse.java
│   ├── VendorRequest.java              # Phase 4
│   └── VendorResponse.java             # Phase 4
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
│   ├── Review.java                     # Phase 4
│   ├── Role.java
│   ├── User.java
│   └── Vendor.java                     # Phase 4
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
│   ├── InvalidFileException.java        # Phase 4
│   ├── InvalidStateTransitionException.java
│   ├── ResourceNotFoundException.java
│   ├── ReviewNotAllowedException.java   # Phase 4
│   └── StorageException.java            # Phase 4
│
├── repository/
│   ├── AuditLogRepository.java
│   ├── CouponRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ProductRepository.java
│   ├── ReviewRepository.java            # Phase 4
│   ├── UserRepository.java
│   └── VendorRepository.java            # Phase 4
│
├── security/
│   ├── CurrentUserService.java          # Phase 4
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
│
├── service/
│   ├── AuditService.java
│   ├── AuthService.java
│   ├── CouponService.java
│   ├── MediaStorageService.java         # Phase 4
│   ├── OrderService.java
│   ├── PaymentService.java
│   ├── ProductService.java
│   ├── ReviewService.java               # Phase 4
│   ├── ScheduledTaskService.java
│   ├── UserService.java
│   └── VendorService.java               # Phase 4
│
├── specification/
│   └── ProductSpecification.java
│
└── util/
    └── PageableFactory.java             # Phase 4
```

```text
src/main/resources/
├── application-example.properties
├── application.properties               # not committed
└── db/migration/                        # Phase 4
    ├── V1__baseline_core_schema.sql
    ├── V2__add_coupons_payments_and_audit_logs.sql
    ├── V3__seed_product_catalog.sql
    └── V4__add_vendors_and_reviews.sql
```

```text
postman/
├── Ecommerce-Inventory.postman_collection.json
├── Ecommerce-Inventory.postman_environment.json
└── sample-files/                        # Phase 4
    └── generate-samples.py
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

Phase 4 introduces a third role, VENDOR. There is no self-service route into it: an administrator creates a vendor and names an existing account as its owner, which promotes that account and binds it to exactly one vendor.

#### CUSTOMER

- Browse products, vendors, and reviews
- Search/filter products
- Place orders
- View own order history
- Process payment for own orders
- Submit a review for a product they have actually bought
- Upload images

#### VENDOR

- Everything a customer can browse
- Create products under its own vendor id
- Update and delete only its own products
- Upload images

#### ADMIN

- Add products for any vendor
- Update products/inventory
- Delete products
- Create and verify vendors
- Create coupons
- Change order status
- List, inspect, and delete user accounts
- View audit logs

A CUSTOMER calling a VENDOR-only or ADMIN-only endpoint receives HTTP 403. A VENDOR reaching for another vendor's product receives HTTP 403 with a message naming the boundary it crossed, rather than a silent reassignment or a generic denial.

The full authorization matrix is in the Phase 4 section below.

## API Endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |

### Users

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/users` | ADMIN |
| GET | `/api/v1/users` | ADMIN |
| GET | `/api/v1/users/{id}` | ADMIN |
| DELETE | `/api/v1/users/{id}` | ADMIN |

Phase 4 closed a gap here. These routes were previously only `authenticated()`, which meant any logged-in customer could list every account or delete other users. The whole controller is now ADMIN-only. Self-service signup is unaffected and remains public at `POST /api/v1/auth/register`.

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
- `vendorId` (Phase 4)
- `minRating` (Phase 4)
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
GET /api/v1/products?vendorId=1
```

```http
GET /api/v1/products?minRating=4
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
| POST | `/api/v1/products` | VENDOR / ADMIN |
| GET | `/api/v1/products/{id}` | CUSTOMER / VENDOR / ADMIN |
| PUT | `/api/v1/products/{id}` | VENDOR (own products) / ADMIN |
| DELETE | `/api/v1/products/{id}` | VENDOR (own products) / ADMIN |

A product response carries the vendor it belongs to and its live rating aggregates:

```json
{
  "id": 7,
  "name": "Vendor Reviewable Widget",
  "category": "Accessories",
  "price": 1499.00,
  "stock": 25,
  "vendorId": 1,
  "vendorName": "Postman Traders",
  "imageUrl": "/uploads/products/img_1739met_9c2f1a04.png",
  "ratingAverage": 4.0,
  "reviewCount": 1
}
```

A backward-compatible search endpoint is also retained:

```http
GET /api/v1/products/search
```

### Vendors

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/vendors` | ADMIN |
| GET | `/api/v1/vendors` | CUSTOMER / VENDOR / ADMIN |
| GET | `/api/v1/vendors/{id}` | CUSTOMER / VENDOR / ADMIN |
| GET | `/api/v1/vendors/{id}/products` | CUSTOMER / VENDOR / ADMIN |
| PATCH | `/api/v1/vendors/{id}/verification?verified=` | ADMIN |

### Reviews

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/products/{productId}/reviews` | CUSTOMER, verified purchase only |
| GET | `/api/v1/products/{productId}/reviews` | CUSTOMER / VENDOR / ADMIN |

### Media

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/media/upload` | CUSTOMER / VENDOR / ADMIN |
| GET | `/uploads/**` | Public |

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
| `ResourceNotFoundException` | 404 | Invalid user/product/order/vendor ID |
| `InsufficientStockException` | 400 | Requested quantity exceeds stock |
| `InvalidCouponException` | 400 | Invalid/expired/inactive coupon |
| `InvalidStateTransitionException` | 422 | Illegal order-state transition |
| `ReviewNotAllowedException` | 403 | Reviewing an un-purchased product, or reviewing twice |
| `InvalidFileException` | 400 | Wrong type, wrong extension, oversized, or undecodable upload |
| `MaxUploadSizeExceededException` | 400 | Upload beyond the container's multipart limit |
| `MissingServletRequestPartException` | 400 | Multipart request with no `file` part |
| `MissingServletRequestParameterException` | 400 | Required query or form parameter absent |
| `IllegalArgumentException` | 400 | Duplicate business name, missing `vendorId` |
| `ObjectOptimisticLockingFailureException` | 409 | Concurrent writes to the same product `@Version` |
| `StorageException` | 500 | Upload directory unwritable |
| Validation errors | 400 | Invalid request payload |
| Access denied | 403 | CUSTOMER calling an ADMIN-only endpoint, or a vendor crossing into another vendor's catalog |

Access-denied responses preserve the reason where one was given. A vendor editing somebody else's product gets `Product 3 belongs to another vendor and cannot be modified by your account` rather than a bare `Access Denied`, which makes the 403s in the Postman collection self-explaining.


## Postman Testing

The project includes a Postman collection and environment in the `postman` directory. The collection covers all four phases across ten folders and 97 requests, each with assertions attached.

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

### Phase 4 - Vendors, Reviews & Media Upload

Three folders cover Phase 4. They are order-dependent, because each one hands identifiers to the next through environment variables:

| Folder | Requests | Covers |
|---|---:|---|
| 8. Phase 4 - Vendors & Marketplace | 21 | Vendor onboarding, owner promotion, verification, per-vendor catalogs, cross-vendor 403s |
| 9. Phase 4 - Reviews & Rating Engine | 15 | The verified-purchase gate, aggregate metrics, rating recalculation on product and vendor |
| 10. Phase 4 - Media Upload | 11 | Multipart upload, public URL retrieval, and the four rejection cases |

Recommended run order is folder 1, then 8, then 10, then 9. Folder 10 populates the uploaded image URL that the review in folder 9 attaches, so running 10 first gives the review a photo. Running 8, 9, 10 also works; the review simply carries no image.

Folder 8 opens with `Resolve Customer User Id - Admin`, which reads `GET /api/v1/users` and writes the customer's numeric id into the `userId` variable. `POST /api/v1/orders` requires the body `userId` to match the authenticated caller, so this removes the need to set it by hand.

The Phase 4 requests that matter most for the acceptance criteria:

- `Submit Review - Unpaid Order Blocked` proves a PENDING order does not qualify as a purchase
- `Submit Review - Authorized Purchase` is the authorized review submission flow
- `Submit Review - Not Purchased` is the 403 for an un-purchased product
- `Product Rating Recalculated` and `Vendor Rating Recalculated` read back both aggregates after the review
- `Create Product - Vendor Cross-Vendor Blocked` and `Update Product - Foreign Product Blocked` are the vendor ownership boundary
- `Upload Product Image - Valid PNG` followed by `Fetch Uploaded Image From Public URL` is the round trip through the static path
- `Upload - Executable Rejected`, `Upload - PDF Rejected`, `Upload - Renamed Executable With PNG Extension`, and `Upload - Oversized File Rejected` are the four 400 cases

#### Sample Upload Files

The multipart requests point at files in `postman/sample-files`. Generate them once:

```cmd
python postman\sample-files\generate-samples.py
```

The script only uses the Python standard library and writes:

| File | Sent as | Expected |
|---|---|---|
| `sample-product.png` | `image/png` | 201 Created |
| `sample-product.jpg` | `image/jpeg` | 201 Created (only produced if Pillow is installed) |
| `not-an-image.exe` | `application/octet-stream` | 400, rejected on content type |
| `not-an-image.pdf` | `application/pdf` | 400, rejected on content type |
| `renamed-executable.png` | `image/png` | 400, rejected on failed image decode |
| `oversized-image.png` | `image/png`, 2.3 MB | 400, rejected on size |

`renamed-executable.png` is the interesting one: the extension and the declared content type both look correct, so it passes the first three checks and is caught only when the bytes fail to decode as an image.

`oversized-image.png` is deliberately not committed, because a 2.3 MB fixture does not belong in source control. Regenerate it with the script, or point the request at any photo larger than 2 MB.

Postman resolves these relative paths against its working directory, so set that to the repository's `postman` folder under Settings, General, Working directory. If a file shows as missing, re-select it with the Select Files button.


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

## Phase 4 - Multi-Vendor Marketplace, Reviews & File Upload

Phase 4 turns the single-tenant catalog into a marketplace. Products acquire an owner, customers acquire a voice, and the schema stops being a side effect of Hibernate's `ddl-auto`.

### Module 1 - Database Migrations

`spring.jpa.hibernate.ddl-auto` is now `none`, and Flyway is the single source of truth for the schema. See the Database Setup and Migrations sections above for the version table and the upgrade path from an earlier phase.

The move matters beyond tidiness. Under `ddl-auto=update` the schema was whatever Hibernate inferred from the entities at startup, which means it was never reviewable, never reproducible, and unable to express anything Hibernate does not generate: check constraints, seed rows, data backfills, column renames. Nothing recorded what had already been applied, so two databases that had seen different versions of the code could quietly diverge. Under Flyway every change is a numbered file in source control, applied once, checksummed, and recorded.

The old `src/main/resources/data.sql` has been deleted. Its six catalog rows now live in `V3__seed_product_catalog.sql`, so seeding happens inside the migration sequence rather than alongside it, and `spring.sql.init.mode` stays `never`.

### Module 2 - Marketplace API

A vendor is a seller with its own catalog, support contact, logo, verification flag, and aggregate rating:

```http
POST /api/v1/vendors
```

```json
{
  "businessName": "Postman Traders",
  "supportEmail": "support@postman-traders.example.com",
  "logoUrl": "/uploads/vendors/img_1739000000_3af19b2c.png",
  "ownerEmail": "vendor.owner@example.com"
}
```

`businessName` and `supportEmail` are required, and `businessName` is unique. `logoUrl` is optional and is normally a path returned by the media endpoint.

`ownerEmail` is the interesting field. Supplying it links an already-registered account to the new vendor and promotes it to ROLE_VENDOR in the same transaction. Two cases are refused rather than applied: an ADMIN account is never demoted to VENDOR, and an account already bound to a different vendor is not moved. There is no self-service route to ROLE_VENDOR, which keeps onboarding an administrative act.

The catalog for one vendor is paginated:

```http
GET /api/v1/vendors/{id}/products?page=0&size=10&sort=name,asc
```

An unknown vendor id here returns 404 rather than an empty page, because an empty page and a nonexistent seller are different answers.

Verification is a separate administrative step, so a vendor can trade before it is verified:

```http
PATCH /api/v1/vendors/{id}/verification?verified=true
```

#### Product Ownership

The ownership rule is enforced in the service, not the controller, so it holds for every path into a write:

| Caller | Creating a product | Editing or deleting a product |
|---|---|---|
| VENDOR | Forced onto the caller's own vendor. A `vendorId` naming anyone else is a 403 | Allowed only for products whose `vendor_id` matches the caller's |
| ADMIN | `vendorId` is required; every product in a marketplace has a seller | Any product |

A vendor account that exists but is not linked to a vendor is refused with an explanation rather than a null pointer, which is the state an account is briefly in if an administrator promotes it by hand.

The catalog gained two filters that compose with the existing Phase 2 specifications:

```http
GET /api/v1/products?vendorId=1&minRating=4&inStock=true&sort=price,desc
```

### Module 3 - Reviews & Rating Engine

A review requires a verified purchase:

```http
POST /api/v1/products/{productId}/reviews
```

```json
{
  "rating": 4,
  "comment": "Arrived quickly and works exactly as described.",
  "imageUrl": "/uploads/reviews/img_1739000000_7c1e4b09.png"
}
```

`rating` must be 1 to 5, enforced both by Bean Validation and by a `CHECK` constraint in the migration. `comment` is required. `imageUrl` is optional.

Eligibility is a single repository query: does the caller have at least one order containing this product whose status is PAID or SHIPPED. The domain has no COMPLETED status, so those two are what "completed order" means here; PENDING is an unpaid intention and CANCELLED is a purchase that did not happen. Failing that check is a `ReviewNotAllowedException`, which the handler maps to 403 with a message naming the product.

One review per customer per product, enforced in the service for a clear error message and by a unique index on `(product_id, user_id)` so a race cannot slip a second one through.

Reading reviews returns the page together with the aggregate metrics:

```http
GET /api/v1/products/{productId}/reviews?page=0&size=10&sort=rating,desc
```

```json
{
  "productId": 7,
  "productName": "Vendor Reviewable Widget",
  "averageRating": 4.0,
  "totalReviews": 1,
  "ratingBreakdown": { "5": 0, "4": 1, "3": 0, "2": 0, "1": 0 },
  "reviews": [ "..." ],
  "page": 0,
  "size": 10,
  "totalPages": 1,
  "last": true
}
```

The breakdown is seeded with explicit zeros for all five stars, so a client can render a histogram without null checks. Reviews sort newest first unless a sort is given.

#### Recalculation

Every accepted review recalculates two aggregates inside the same transaction:

- the product's `rating_average` and `review_count`, from `AVG(rating)` and `COUNT(*)` over that product's reviews
- the parent vendor's `rating_average`, from `AVG(rating)` over every review of every product that vendor sells

The vendor figure is a volume-weighted average of individual ratings, not an average of its products' averages. Those two are only equal when every product has the same number of reviews, and the weighted form is the one that answers "how do this seller's customers rate it".

Both aggregates are denormalised columns rather than computed on read. That is what makes `GET /api/v1/products?minRating=4` a plain indexed predicate instead of a correlated subquery on every catalog page. Writing them evicts the product caches, so the next catalog read reflects the new rating.

A product with no vendor, which is the case for the six V3 seed rows, skips the vendor half rather than failing.

### Module 4 - File Upload

```http
POST /api/v1/media/upload
Content-Type: multipart/form-data
```

| Part | Required | Notes |
|---|---|---|
| `file` | Yes | JPEG or PNG, at most 2 MB |
| `category` | No | `products` (default), `vendors`, or `reviews` |

```json
{
  "fileName": "img_1739000000_9c2f1a04.png",
  "url": "/uploads/products/img_1739000000_9c2f1a04.png",
  "contentType": "image/png",
  "sizeBytes": 21326
}
```

The returned `url` is served as static content by a resource handler mapped onto the same directory the service writes into, and `GET /uploads/**` is explicitly public so the path works in an `<img>` tag without a token. The directory is read back off the storage service rather than recomputed, so the write path and the read path cannot drift apart.

Validation runs in four layers, in this order, because any single one can be defeated:

1. Presence: a missing or empty `file` part is a 400 naming the expected field
2. Size against `app.media.max-file-size-bytes`, reported as `File is too large: 2.32 MB. The maximum allowed size is 2.00 MB.`
3. Declared content type against `image/jpeg` and `image/png`
4. Filename extension against `.jpg`, `.jpeg`, `.png`

Then the decisive one: the bytes are handed to `ImageIO.read`. A renamed executable arrives with a `.png` name and a forged `image/png` content type, so it passes all four checks and is caught only here, when it fails to decode. `postman/sample-files/renamed-executable.png` exists to demonstrate exactly that.

Client filenames are never used on disk. Every upload is written under a freshly generated `img_<epochMillis>_<random>.<ext>`, which removes path traversal as a possibility rather than trying to filter for it, and keeps concurrent uploads from colliding. The resolved path is still checked against the upload root as a guard against a future change to the naming scheme.

`category` is a whitelist rather than free text, so a request cannot create directories outside the upload root.

Configuration:

```properties
app.media.upload-dir=uploads
app.media.public-base-path=/uploads
app.media.max-file-size-bytes=2097152

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=12MB
```

The container limits are deliberately looser than the application limit. If Tomcat rejected a 3 MB upload itself, the caller would get its generic message; letting the request through to `MediaStorageService` produces one that states both the actual size and the allowed size. `MaxUploadSizeExceededException` is still handled as a 400 backstop for anything past 10 MB.

The `uploads/` directory is gitignored. It is local user data, not source.

### Schema Changes

```sql
CREATE TABLE vendors (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    business_name  VARCHAR(255) NOT NULL,
    support_email  VARCHAR(255) NOT NULL,
    logo_url       VARCHAR(255) NULL,
    rating_average DOUBLE       NOT NULL DEFAULT 0.0,
    is_verified    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at     DATETIME(6)  NOT NULL,
    CONSTRAINT pk_vendors PRIMARY KEY (id),
    CONSTRAINT uk_vendors_business_name UNIQUE (business_name)
) ENGINE = InnoDB;

CREATE TABLE reviews (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    product_id BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    rating     INT          NOT NULL,
    comment    TEXT         NOT NULL,
    image_url  VARCHAR(255) NULL,
    created_at DATETIME(6)  NOT NULL,
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT fk_reviews_product      FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_user         FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT uk_reviews_product_user UNIQUE (product_id, user_id),
    CONSTRAINT ck_reviews_rating       CHECK (rating BETWEEN 1 AND 5)
) ENGINE = InnoDB;
```

| Table | Column | Purpose |
|---|---|---|
| `products` | `vendor_id` | Owning vendor, FK to `vendors` |
| `products` | `image_url` | Path returned by the media endpoint |
| `products` | `rating_average` | Denormalised average, recalculated per review |
| `products` | `review_count` | Denormalised count, recalculated per review |
| `users` | `vendor_id` | The vendor a ROLE_VENDOR account belongs to |

Indexes are added on `reviews.product_id`, `reviews.user_id`, `products.vendor_id`, and `users.vendor_id`, since every one of them is a lookup key on a hot path.

### Authorization Matrix

| Endpoint | Public | CUSTOMER | VENDOR | ADMIN |
|---|:-:|:-:|:-:|:-:|
| `POST /api/v1/auth/register` | yes | yes | yes | yes |
| `POST /api/v1/auth/login` | yes | yes | yes | yes |
| `GET /uploads/**` | yes | yes | yes | yes |
| `GET /api/v1/products` | no | yes | yes | yes |
| `GET /api/v1/products/{id}` | no | yes | yes | yes |
| `POST /api/v1/products` | no | no | own vendor | yes |
| `PUT /api/v1/products/{id}` | no | no | own products | yes |
| `DELETE /api/v1/products/{id}` | no | no | own products | yes |
| `POST /api/v1/vendors` | no | no | no | yes |
| `GET /api/v1/vendors` | no | yes | yes | yes |
| `GET /api/v1/vendors/{id}/products` | no | yes | yes | yes |
| `PATCH /api/v1/vendors/{id}/verification` | no | no | no | yes |
| `POST /api/v1/products/{id}/reviews` | no | purchasers | no | no |
| `GET /api/v1/products/{id}/reviews` | no | yes | yes | yes |
| `POST /api/v1/media/upload` | no | yes | yes | yes |
| `/api/v1/users/**` | no | no | no | yes |
| `/api/v1/admin/**` | no | no | no | yes |

### Audit Trail

Four actions were added to the existing audit log, each rendered as a readable sentence rather than a JSON blob:

| Action | Recorded |
|---|---|
| `VENDOR_CREATED` | Business name, support email, and the linked owner if any |
| `VENDOR_VERIFIED` | Which vendor, and the new flag value |
| `REVIEW_CREATED` | Product, author, rating, and both recalculated averages |
| `MEDIA_UPLOADED` | Stored filename, category, content type, and a human-readable size |

### Two Pre-Existing Bugs Fixed Along the Way

Both surfaced while `ProductService` was being reworked for vendor ownership, and both are now covered by comments in the code so they are not reintroduced.

`search()` used to call `getProducts()` directly. That is a self-invocation: the call never leaves the object, so it bypasses the Spring cache proxy, and `@Cacheable` was silently doing nothing for every request that arrived through `/search`. Both methods now delegate to a private helper and each carries its own annotation.

`update()` read the old price off the entity after the setters had already run, so the audit log recorded the new price as both the old and the new value. The previous stock and price are now captured before mutation.

### Acceptance Criteria

| Criterion | Where it is satisfied |
|---|---|
| Builds and launches on a fresh database with no manual SQL | `CREATE DATABASE` then run; Flyway applies V1 to V4 |
| Un-purchased review attempt rejected with 403 or 400 | `ReviewNotAllowedException` to 403; Postman `Submit Review - Not Purchased` |
| Unsupported extension returns 400 with a clear message | Extension and content-type layers; Postman `Upload - Executable Rejected` and `Upload - PDF Rejected` |
| Oversized file returns 400 with a clear message | Size layer, message states both sizes; Postman `Upload - Oversized File Rejected` |
| Rating recalculated for product and vendor on each review | `ProductService.recalculateRatingAverage` and `VendorService.recalculateRatingAverage`, both called from `ReviewService.create` |
| Vendor scoping on product management | `resolveVendorForWrite` and `assertCanManage` in `ProductService` |
| Postman collection updated with multipart upload and authorized review flows | Folders 8, 9, and 10 |

## GitHub Repository

Repository:

```text
https://github.com/viveknred/ecommerce-inventory
```

## Git Hygiene

Each phase is committed on top of the previous one using conventional commit prefixes, for example:

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

Phase 4:

```text
feat: integrate flyway and take schema ownership from hibernate
feat: add vendor entity and marketplace endpoints
feat: scope product management to the owning vendor
feat: add verified-purchase review submission
feat: recalculate product and vendor ratings on each review
feat: add multipart media upload with validation
fix: cache product search results through the spring proxy
fix: capture previous price before mutation in the product audit log
chore: update README and Postman collection for phase 4
```

Do not commit:

- `build/`
- IDE configuration files
- local database credentials
- JWT secrets
- other local secrets
- `uploads/` - locally uploaded media is user data, not source
- `postman/sample-files/oversized-image.png` - the 2 MB+ upload fixture is generated by `generate-samples.py`

Two entries deserve a note. `src/main/resources/application.properties` holds a real database password and JWT secret and is gitignored; `application-example.properties` is the committed template. And a migration that has already been applied anywhere is never edited, because Flyway checksums it - a correction ships as a new `V5__...` file.

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


