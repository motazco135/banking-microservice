# customer_service — Banking Platform

Part of the `com.bank.demo` Banking Platform microservices suite.

**Version:** 0.0.3

## Overview

The Customer Service handles customer registration and profile management. It publishes `CustomerProfileCreatedEvent` to Kafka when a new customer profile is created.

Package root: `com.bank.demo.customer`

## Architecture (ADL Components)

| Component | Package | Responsibility |
|---|---|---|
| Customer API | `com.bank.demo.customer.api` | REST endpoints |
| Customer Service | `com.bank.demo.customer.service` | Business logic |
| Customer Repository | `com.bank.demo.customer.repository` | Data persistence (PostgreSQL) |
| Customer Events | `com.bank.demo.customer.events` | Kafka event publishing |

## Tech Stack

- Java 21
- Spring Boot 3.4.4
- PostgreSQL 16
- Apache Kafka (via Spring Kafka)
- Liquibase (database migrations)
- Maven

## Project Structure

```
src/
  main/
    java/com/bank/demo/customer/
      api/           ← REST controllers
      service/       ← Business logic
      repository/    ← JPA repositories
      events/        ← Kafka event publishers
    resources/
      application.yml
      db/changelog/
        db.changelog-master.xml          ← Liquibase master changelog
        sql/
          001_create_customer_profiles.sql
  test/
    java/com/bank/demo/customer/
      api/           ← Controller slice tests (@WebMvcTest)
      service/       ← Service unit tests (Mockito)
      events/        ← Publisher unit tests (Mockito)
      architecture/  ← ArchUnit architecture enforcement tests
.circleci/
  config.yml
docker-compose.yml
```

## Running Locally

```bash
# Start infrastructure
docker-compose up -d postgres zookeeper kafka

# Run the application
mvn spring-boot:run
```

## Running All Services

```bash
docker-compose up
```

## Tests

```bash
mvn verify
```
