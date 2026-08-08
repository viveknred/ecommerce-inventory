INSERT IGNORE INTO users (id, email, name, created_at)
VALUES
(1, 'vivek@example.com', 'Vivek', CURRENT_TIMESTAMP);

INSERT IGNORE INTO products
(id, name, category, price, stock_quantity, version)
VALUES
(1, 'iPhone 15', 'Smartphone', 65000.00, 10, 0),
(2, 'Samsung Galaxy S24', 'Smartphone', 70000.00, 10, 0),
(3, 'OnePlus 12', 'Smartphone', 55000.00, 10, 0),
(4, 'Redmi Note 13', 'Smartphone', 18000.00, 10, 0),
(5, 'Dell Laptop', 'Laptop', 75000.00, 10, 0),
(6, 'HP Laptop', 'Laptop', 65000.00, 10, 0);