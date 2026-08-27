-- ============================================================
-- V2: Phase 2 / Phase 3 additions
-- coupons, payments, audit_logs
-- ============================================================

CREATE TABLE coupons (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    code             VARCHAR(255) NOT NULL,
    discount_percent INT          NOT NULL,
    expiration_date  DATETIME(6)  NOT NULL,
    is_active        TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_coupons PRIMARY KEY (id),
    CONSTRAINT uk_coupons_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE TABLE payments (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    order_id       BIGINT         NOT NULL,
    amount         DECIMAL(19, 2) NOT NULL,
    status         VARCHAR(255)   NOT NULL,
    transaction_id VARCHAR(255)   NULL,
    created_at     DATETIME(6)    NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uk_payments_order UNIQUE (order_id),
    CONSTRAINT uk_payments_transaction UNIQUE (transaction_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB;

CREATE TABLE audit_logs (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    operation_id VARCHAR(255) NOT NULL,
    entity_name  VARCHAR(255) NOT NULL,
    action       VARCHAR(255) NOT NULL,
    changed_by   VARCHAR(255) NOT NULL,
    `timestamp`  DATETIME(6)  NOT NULL,
    details      TEXT         NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_audit_logs_operation ON audit_logs (operation_id);
CREATE INDEX idx_audit_logs_entity_action ON audit_logs (entity_name, action);
