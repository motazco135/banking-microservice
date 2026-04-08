-- liquibase formatted sql

-- changeset account-service:V002__create_balance_ledger_table
-- comment: Creates the balance_ledger table. account_id FK is intra-database only — no FK crosses into any other service DB.

CREATE TABLE balance_ledger (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    account_id      UUID            NOT NULL,
    balance         NUMERIC(19, 4)  NOT NULL DEFAULT 0,
    last_updated_at TIMESTAMP       NOT NULL DEFAULT now(),

    CONSTRAINT pk_balance_ledger   PRIMARY KEY (id),
    CONSTRAINT uq_balance_ledger_account UNIQUE (account_id),
    CONSTRAINT fk_balance_ledger_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_balance_ledger_balance CHECK (balance >= 0)
);

-- rollback DROP TABLE balance_ledger;
