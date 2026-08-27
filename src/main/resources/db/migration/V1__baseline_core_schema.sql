-- ============================================================
-- V1: Baseline core schema (Phase 1)
-- users, products, orders, order_items
-- ============================================================

CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255)   NOT NULL,
    category       VARCHAR(255)   NOT NULL,
    price          DECIMAL(19, 2) NOT NULL,
    stock_quantity INT            NOT NULL,
    version        BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_products PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_product_category ON products (category);

CREATE TABLE orders (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    user_id      BIGINT         NOT NULL,
    status       VARCHAR(255)   NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    order_date   DATETIME(6)    NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_orders_user ON orders (user_id);

CREATE TABLE order_items (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    order_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE = InnoDB;

CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);
