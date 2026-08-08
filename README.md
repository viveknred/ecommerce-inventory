\# E-Commerce Inventory \& Order Fulfillment Engine



A Spring Boot REST API for managing users, products, inventory, and e-commerce orders.



\## Technologies Used



\- Java 23

\- Spring Boot

\- Spring Data JPA

\- Hibernate

\- MySQL 8

\- Gradle

\- Postman

\- Git and GitHub



\## Prerequisites



Before running the application, make sure the following are installed:



\- Java 23

\- MySQL 8.0 or later

\- Git

\- Postman

\- Spring Tool Suite (STS), IntelliJ IDEA, or another Java IDE



Check Java:



```bash

java -version

```



\## Database Setup



Create the MySQL database:



```sql

CREATE DATABASE ecommerce\_inventory;

```



The application connects to MySQL using the configuration in:



```text

src/main/resources/application.properties

```



Configure your database username and password:



```properties

spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce\_inventory

spring.datasource.username=YOUR\_USERNAME

spring.datasource.password=YOUR\_PASSWORD



spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true

```



Replace `YOUR\_USERNAME` and `YOUR\_PASSWORD` with your local MySQL credentials.



Do not commit database credentials or other secrets to GitHub.



\## Project Structure



```text

src/main/java/com/example/ecommerce/

├── controller/

│   ├── OrderController.java

│   ├── ProductController.java

│   └── UserController.java

│

├── dto/

│   ├── OrderItemRequest.java

│   ├── OrderRequest.java

│   ├── ProductRequest.java

│   ├── ProductResponse.java

│   ├── UserRequest.java

│   └── UserResponse.java

│

├── entity/

│   ├── Order.java

│   ├── OrderItem.java

│   ├── OrderStatus.java

│   ├── Product.java

│   └── User.java

│

├── exception/

│   ├── GlobalExceptionHandler.java

│   ├── InsufficientStockException.java

│   ├── InvalidStateTransitionException.java

│   └── ResourceNotFoundException.java

│

├── repository/

│   ├── OrderItemRepository.java

│   ├── OrderRepository.java

│   ├── ProductRepository.java

│   └── UserRepository.java

│

├── service/

│   ├── OrderService.java

│   ├── ProductService.java

│   └── UserService.java

│

└── specification/

&#x20;   └── ProductSpecification.java

```



\## Build the Application



From the project root directory:



```cmd

gradlew.bat clean build

```



If the build is successful, the project is ready to run.



\## Run the Application



Run:



```cmd

gradlew.bat bootRun

```



The application starts on:



```text

http://localhost:8080

```



\## API Endpoints



\### Users



\#### Create User



```http

POST /users

```



Example:



```json

{

&#x20;   "email": "vivek@example.com",

&#x20;   "name": "Vivek"

}

```



\#### Get User



```http

GET /users/{id}

```



\---



\### Products



\#### Create Product



```http

POST /products

```



Header:



```text

Content-Type: application/json

```



Example:



```json

{

&#x20;   "name": "iPhone 15",

&#x20;   "category": "Smartphone",

&#x20;   "price": 65000,

&#x20;   "stock": 10

}

```



\#### Get All Products



```http

GET /products

```



\#### Get Product by ID



```http

GET /products/{id}

```



\#### Delete Product



```http

DELETE /products/{id}

```



\## Product Search



Products can be searched using multiple optional filters.



\### Category



```http

GET /products/search?category=Smartphone

```



Category matching is case-insensitive and supports partial matching.



\### Minimum Price



```http

GET /products/search?minPrice=50000

```



\### Maximum Price



```http

GET /products/search?maxPrice=60000

```



\### Price Range



```http

GET /products/search?minPrice=50000\&maxPrice=70000

```



\### In-Stock Products



```http

GET /products/search?inStock=true

```



\### Multiple Filters



```http

GET /products/search?category=Smartphone\&minPrice=50000\&maxPrice=70000\&inStock=true

```



\## Pagination



Pagination is supported through Spring Data's `Pageable`.



Example:



```http

GET /products/search?page=0\&size=5

```



The response contains pagination information such as:



\- Current page

\- Page size

\- Total elements

\- Total pages

\- First/last page indicators



\## Sorting



Products can be sorted using the `sort` parameter.



Sort by price ascending:



```http

GET /products/search?sort=price,asc

```



Sort by price descending:



```http

GET /products/search?sort=price,desc

```



Multiple sorting fields can also be supplied:



```http

GET /products/search?sort=category,asc\&sort=price,desc

```



\## Orders



\### Create Order



```http

POST /orders

```



Header:



```text

Content-Type: application/json

```



Example:



```json

{

&#x20;   "userId": 1,

&#x20;   "items": \[

&#x20;       {

&#x20;           "productId": 1,

&#x20;           "quantity": 2

&#x20;       }

&#x20;   ]

}

```



When an order is created:



1\. The user is validated.

2\. Each product is validated.

3\. Available stock is checked.

4\. Product stock is reduced.

5\. The order total is calculated.

6\. Order items are created.

7\. The order is created with `PENDING` status.



\### Get Order



```http

GET /orders/{id}

```



\### Get Orders by User



```http

GET /orders/user/{userId}

```



\## Order Status



The available statuses are:



```text

PENDING

PAID

SHIPPED

CANCELLED

```



Valid transitions include:



```text

PENDING  -> PAID

PAID     -> SHIPPED

PENDING  -> CANCELLED

```



Invalid status transitions are rejected.



\### Update Order Status



```http

PATCH /orders/{id}/status?status=PAID

```



Example:



```http

PATCH /orders/1/status?status=PAID

```



Then:



```http

PATCH /orders/1/status?status=SHIPPED

```



To cancel a pending order:



```http

PATCH /orders/1/status?status=CANCELLED

```



When an order is cancelled, the purchased quantities are restored to the product inventory.



\## Inventory Management



When an order is successfully created, the requested quantity is deducted from the product stock.



For example:



```text

Initial stock: 10

Order quantity: 2

Remaining stock: 8

```



If the order is cancelled:



```text

Stock before cancellation: 8

Restored quantity: 2

Stock after cancellation: 10

```



If the requested quantity is greater than the available stock, the order is rejected.



Example:



```text

Available stock: 9

Requested quantity: 20

```



The API returns an insufficient stock error.



\## Transaction Management



Order creation and order status updates use transactional processing.



This ensures that related database operations are handled consistently.



For example, during order creation, stock deduction and order creation are performed within the same transaction.



If an operation fails, the transaction can be rolled back.



\## Optimistic Locking



The `Product` entity uses JPA optimistic locking with the `@Version` annotation.



This helps prevent conflicting concurrent updates to product inventory.



\## Validation



The application validates incoming request data.



Examples include:



\- Required product name

\- Required category

\- Non-negative product price

\- Non-negative stock quantity

\- Valid order quantities

\- Required user information

\- Required product information



Invalid requests are rejected with appropriate error responses.



\## Exception Handling



The application uses a global exception handler to provide consistent error responses.



\### Resource Not Found



Used when a requested user, product, or order does not exist.



Example:



```json

{

&#x20;   "error": "Product not found"

}

```



\### Insufficient Stock



Returned when the requested order quantity exceeds available inventory.



Example:



```json

{

&#x20;   "error": "Insufficient stock for product: iPhone 15"

}

```



\### Invalid State Transition



Returned when an invalid order status transition is requested.



Example:



```json

{

&#x20;   "error": "Invalid status transition from SHIPPED to CANCELLED"

}

```



\## Postman Testing



The APIs were tested using Postman.



The project includes a Postman collection containing tests for:



\- User creation

\- Product creation

\- Product retrieval

\- Product deletion

\- Product category filtering

\- Minimum price filtering

\- Maximum price filtering

\- Price range filtering

\- Stock filtering

\- Pagination

\- Sorting

\- Multiple search filters

\- Order creation

\- Inventory deduction

\- Payment status update

\- Shipping status update

\- Order cancellation

\- Inventory restoration

\- Insufficient stock

\- Invalid quantity

\- Invalid user

\- Invalid product

\- Invalid order status transition



Import the Postman collection and start the Spring Boot application before running the requests.



\## Error Response Format



Errors are returned as JSON.



Example:



```json

{

&#x20;   "error": "Resource not found"

}

```



\## GitHub Repository



The source code is available at:



https://github.com/viveknred/ecommerce-inventory



\## Running the Project



Clone the repository:



```cmd

git clone https://github.com/viveknred/ecommerce-inventory.git

```



Enter the project directory:



```cmd

cd ecommerce-inventory

```



Configure MySQL credentials in:



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



The application will be available at:



```text

http://localhost:8080

```



\## Testing Summary



The application has been tested for:



\- User operations

\- Product CRUD operations

\- Product filtering

\- Price range filtering

\- Inventory filtering

\- Pagination

\- Sorting

\- Order creation

\- Inventory deduction

\- Order payment

\- Order shipping

\- Order cancellation

\- Inventory restoration

\- Insufficient stock handling

\- Invalid quantity validation

\- Invalid user handling

\- Invalid product handling

\- Invalid order state transitions



