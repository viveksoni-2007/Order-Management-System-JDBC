CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

CREATE TABLE IF NOT EXISTS Orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    product_name VARCHAR(100),
    quantity INT,
    unit_price DOUBLE,
    status VARCHAR(30)
);

-- Optional sample records (the Java menu also has an option to insert these)
INSERT IGNORE INTO Orders VALUES
(101, 'Aman', 'Keyboard', 3, 799.00, 'Pending'),
(102, 'Riya', 'Mouse', 5, 499.00, 'Shipped'),
(103, 'Karan', 'Monitor', 2, 8500.00, 'Cancelled');
