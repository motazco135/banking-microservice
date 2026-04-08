--liquibase formatted sql

--changeset customer-service:001 labels:customer
--comment: Create customer_profiles table with status constraint; no cross-DB foreign keys

CREATE TYPE customer_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED');

CREATE TABLE customer_profiles (
    customer_id  UUID         NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    national_id  VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone        VARCHAR(50)  NOT NULL,
    status       customer_status NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_customer_profiles PRIMARY KEY (customer_id),
    CONSTRAINT uq_customer_national_id UNIQUE (national_id),
    CONSTRAINT uq_customer_email       UNIQUE (email)
);

--rollback DROP TABLE customer_profiles;
--rollback DROP TYPE customer_status;
