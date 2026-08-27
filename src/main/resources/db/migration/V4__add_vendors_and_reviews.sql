-- ============================================================
-- V4: Phase 4 - Multi-vendor marketplace and customer reviews
--
--  * vendors table
--  * reviews table
--  * products.vendor_id foreign key + denormalised rating columns
--  * users.vendor_id so a ROLE_VENDOR account is bound to one vendor
-- ============================================================

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
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    product_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    rating     INT         NOT NULL,
    comment    TEXT        NOT NULL,
    image_url  VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_reviews_product_user UNIQUE (product_id, user_id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE = InnoDB;

CREATE INDEX idx_reviews_product ON reviews (product_id);
CREATE INDEX idx_reviews_user ON reviews (user_id);

ALTER TABLE products
    ADD COLUMN vendor_id      BIGINT       NULL,
    ADD COLUMN image_url      VARCHAR(255) NULL,
    ADD COLUMN rating_average DOUBLE       NOT NULL DEFAULT 0.0,
    ADD COLUMN review_count   INT          NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT fk_products_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (id);

CREATE INDEX idx_products_vendor ON products (vendor_id);

ALTER TABLE users
    ADD COLUMN vendor_id BIGINT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (id);

CREATE INDEX idx_users_vendor ON users (vendor_id);
