# payment_service

Banking Platform — Payment Service (`com.bank.demo.payment`)
Version: **0.0.1**

## Overview

Handles payment initiation, orchestration across rails (IPS / SWIFT / INTERNAL), GL posting, and event publishing to the Kafka event bus.

## Architecture (per ADL)

```
com.bank.demo.payment
├── api          — REST endpoints (channel: MOBILE | WEB)
├── orchestrator — Payment orchestration (rail: IPS | SWIFT | INTERNAL)
├── rail         — Rail adapters (IPS | SWIFT | INTERNAL)
├── gl           — GL posting (DEBIT + CREDIT entries)
├── repository   — JPA persistence
├── events       — Kafka publishers (PaymentProcessedEvent, GLDebitPostedEvent, GLCreditPostedEvent)
└── consumer     — Kafka consumers (AccountActivatedEvent)
```

## ADL Constraints

- API is reachable via API Gateway **only**
- No direct service-to-service REST calls — all cross-service communication via Kafka
- `api` has **no** dependency on `repository`
- `repository` has **no** dependency on other services
- `events` depends on the event bus

## Stack

| Component  | Technology            |
|------------|-----------------------|
| Language   | Java 21               |
| Framework  | Spring Boot 3.4.4     |
| Database   | PostgreSQL 16         |
| Event Bus  | Apache Kafka 7.7.0    |
| Build      | Maven                 |
| CI/CD      | CircleCI              |

## Running Locally

```bash
docker-compose up -d
mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```
