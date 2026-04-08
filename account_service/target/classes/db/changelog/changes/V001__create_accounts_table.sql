-- liquibase formatted sql

-- changeset account-service:V001__create_accounts_table
-- comment: Creates the accounts table. customer_id is a logical reference only — no FK crosses into customer_db.

CREATE TABLE accounts (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    customer_id UUID        NOT NULL,
    type        VARCHAR(20) NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT now(),

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT chk_accounts_type   CHECK (type   IN ('CURRENT', 'SAVINGS', 'LOAN')),
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);

-- rollback DROP TABLE accounts;
