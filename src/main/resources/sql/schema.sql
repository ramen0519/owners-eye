-- =============================================
-- owners-eye Schema DDL (Oracle 21c XE / ATP)
-- 실행 계정: ownerseye
-- =============================================

-- =========================
-- 1. 시퀀스
-- =========================
CREATE SEQUENCE users_seq          START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE store_seq          START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE upload_seq         START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE pos_sales_seq      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE baemin_sales_seq   START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE baemin_ad_seq      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE coupang_sales_seq  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE expense_seq        START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- =========================
-- 2. 테이블
-- =========================

CREATE TABLE users (
    user_id    NUMBER        NOT NULL,
    email      VARCHAR2(100) NOT NULL,
    password   VARCHAR2(255) NOT NULL,
    name       VARCHAR2(50)  NOT NULL,
    created_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE store (
    store_id   NUMBER        NOT NULL,
    user_id    NUMBER        NOT NULL,
    store_name VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_store PRIMARY KEY (store_id),
    CONSTRAINT fk_store_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE upload (
    upload_id    NUMBER        NOT NULL,
    store_id     NUMBER        NOT NULL,
    upload_type  VARCHAR2(10)  NOT NULL,
    year_month   DATE          NOT NULL,
    file_name    VARCHAR2(255) NOT NULL,
    parse_status VARCHAR2(10)  DEFAULT 'PENDING' NOT NULL,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_upload PRIMARY KEY (upload_id),
    CONSTRAINT fk_upload_store FOREIGN KEY (store_id) REFERENCES store(store_id)
);

CREATE TABLE pos_sales (
    pos_sale_id   NUMBER       NOT NULL,
    upload_id     NUMBER       NOT NULL,
    year_month    DATE         NOT NULL,
    channel       VARCHAR2(20) NOT NULL,
    total_revenue NUMBER(15)   DEFAULT 0 NOT NULL,
    avg_price     NUMBER(15)   DEFAULT 0 NOT NULL,
    total_orders  NUMBER(10)   DEFAULT 0 NOT NULL,
    CONSTRAINT pk_pos_sales PRIMARY KEY (pos_sale_id),
    CONSTRAINT fk_pos_sales_upload FOREIGN KEY (upload_id) REFERENCES upload(upload_id),
    CONSTRAINT uq_pos_sales UNIQUE (upload_id, channel)
);

CREATE TABLE baemin_sales (
    baemin_delivery_id NUMBER       NOT NULL,
    upload_id          NUMBER       NOT NULL,
    year_month         DATE         NOT NULL,
    delivery_type      VARCHAR2(20) NOT NULL,
    order_amount       NUMBER       DEFAULT 0 NOT NULL,
    service_fee        NUMBER       DEFAULT 0 NOT NULL,
    coupon_discount    NUMBER       DEFAULT 0 NOT NULL,
    delivery_cost      NUMBER       DEFAULT 0 NOT NULL,
    payment_fee        NUMBER       DEFAULT 0 NOT NULL,
    vat                NUMBER       DEFAULT 0 NOT NULL,
    CONSTRAINT pk_baemin_sales PRIMARY KEY (baemin_delivery_id),
    CONSTRAINT fk_baemin_sales_upload FOREIGN KEY (upload_id) REFERENCES upload(upload_id),
    CONSTRAINT uq_baemin_sales UNIQUE (upload_id, delivery_type)
);

CREATE TABLE baemin_ad (
    baemin_ad_id NUMBER    NOT NULL,
    upload_id    NUMBER    NOT NULL,
    year_month   DATE      NOT NULL,
    ad_fee       NUMBER    DEFAULT 0 NOT NULL,
    CONSTRAINT pk_baemin_ad PRIMARY KEY (baemin_ad_id),
    CONSTRAINT fk_baemin_ad_upload FOREIGN KEY (upload_id) REFERENCES upload(upload_id),
    CONSTRAINT uq_baemin_ad UNIQUE (upload_id)
);

CREATE TABLE coupang_sales (
    coupang_sale_id  NUMBER    NOT NULL,
    upload_id        NUMBER    NOT NULL,
    year_month       DATE      NOT NULL,
    order_amount     NUMBER    NOT NULL,
    service_fee      NUMBER    NOT NULL,
    payment_fee      NUMBER    NOT NULL,
    delivery_fee     NUMBER    NOT NULL,
    instant_discount NUMBER    NOT NULL,
    coupon_discount  NUMBER    NOT NULL,
    ad_fee           NUMBER    NOT NULL,
    CONSTRAINT pk_coupang_sales PRIMARY KEY (coupang_sale_id),
    CONSTRAINT fk_coupang_sales_upload FOREIGN KEY (upload_id) REFERENCES upload(upload_id),
    CONSTRAINT uq_coupang_sales UNIQUE (upload_id)
);

CREATE TABLE expense (
    expense_id   NUMBER       NOT NULL,
    store_id     NUMBER       NOT NULL,
    expense_type VARCHAR2(15) NOT NULL,
    amount       NUMBER       NOT NULL,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_expense PRIMARY KEY (expense_id),
    CONSTRAINT fk_expense_store FOREIGN KEY (store_id) REFERENCES store(store_id)
);

-- =========================
-- 3. 인덱스
-- =========================
-- UNIQUE 제약조건이 없는 FK 컬럼에만 인덱스 추가
-- (UNIQUE 제약조건은 Oracle이 자동으로 인덱스 생성함)
CREATE INDEX idx_store_user_id    ON store(user_id);
CREATE INDEX idx_upload_store_id  ON upload(store_id);
CREATE INDEX idx_expense_store_id ON expense(store_id);
