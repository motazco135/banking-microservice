--liquibase formatted sql

--changeset payment-service:001 labels:payment,init
--comment: Create payment_transactions table — isolated repository, no cross-DB foreign keys
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'payment_transactions'

CREATE TYPE payment_channel AS ENUM ('MOBILE', 'WEB');
CREATE TYPE payment_rail    AS ENUM ('IPS', 'SWIFT', 'INTERNAL');
CREATE TYPE payment_status  AS ENUM ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED', 'REVERSED');

CREATE TABLE payment_transactions (
    id                UUID            NOT NULL DEFAULT gen_random_uuid(),
    customer_id       UUID            NOT NULL,   -- logical ref to Customer Service, no FK
    debit_account_id  UUID            NOT NULL,   -- logical ref to Account Service, no FK
    credit_account_id UUID            NOT NULL,   -- logical ref to Account Service, no FK
    amount            NUMERIC(19, 4)  NOT NULL,
    currency          VARCHAR(3)      NOT NULL,   -- ISO 4217 (e.g. USD, EUR)
    channel           payment_channel NOT NULL,
    rail              payment_rail    NOT NULL,
    status            payment_status  NOT NULL    DEFAULT 'PENDING',
    reference_no      VARCHAR(255)    NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_payment_transactions PRIMARY KEY (id),
    CONSTRAINT uq_payment_reference_no  UNIQUE (reference_no),
    CONSTRAINT chk_payment_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payment_accounts_differ CHECK (debit_account_id != credit_account_id)
);

CREATE INDEX idx_payment_transactions_customer_id      ON payment_transactions (customer_id);
CREATE INDEX idx_payment_transactions_debit_account_id ON payment_transactions (debit_account_id);
CREATE INDEX idx_payment_transactions_status           ON payment_transactions (status);
CREATE INDEX idx_payment_transactions_created_at       ON payment_transactions (created_at DESC);

--rollback DROP TABLE IF EXISTS payment_transactions;
--rollback DROP TYPE IF EXISTS payment_status;
--rollback DROP TYPE IF EXISTS payment_rail;
--rollback DROP TYPE IF EXISTS payment_channel;
