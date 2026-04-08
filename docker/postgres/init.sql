-- Banking Platform — PostgreSQL initialisation
-- Creates three logical databases in a single instance.
-- Executed once on first container start via docker-entrypoint-initdb.d

CREATE DATABASE customer_db;
CREATE DATABASE account_db;
CREATE DATABASE payment_db;

-- Grant the service user access to each database
\connect customer_db
GRANT ALL PRIVILEGES ON DATABASE customer_db TO postgres;

\connect account_db
GRANT ALL PRIVILEGES ON DATABASE account_db TO postgres;

\connect payment_db
GRANT ALL PRIVILEGES ON DATABASE payment_db TO postgres;
