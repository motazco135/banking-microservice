# Banking Microservice

## Architecture Diagram

```mermaid
graph TD
    Client([Client])
    GW["API Gateway
    api_gateway"]
Client --> GW

subgraph CS ["Customer Service — com.bank.demo.customer"]
C_API["Customer API
customer.api"]
C_SVC["Customer Logic
customer.service
status: PENDING|ACTIVE|SUSPENDED"]
C_REPO["Customer Repository
customer.repository"]
C_PUB["Event Publisher
CustomerProfileCreatedEvent"]
C_API --> C_SVC
C_SVC --> C_REPO
C_SVC --> C_PUB
end

subgraph AS ["Account Service - com.bank.demo.account"]
A_API["Account API
account.api
open additional account"]
A_SVC["Account Logic
account.service
key: customerId
type: CURRENT|SAVINGS|LOAN
status: ACTIVE|INACTIVE|SUSPENDED"]
A_REPO["Account Repository
account.repository"]
A_LED["Balance Ledger
account.ledger
OWNER of balance"]
A_PUB["Event Publisher
AccountActivatedEvent"]
A_CON["Event Consumer
CustomerProfileCreatedEvent
GLDebitPostedEvent
GLCreditPostedEvent"]
A_API --> A_SVC
A_SVC --> A_REPO
A_SVC --> A_LED
A_CON --> A_SVC
A_SVC --> A_PUB
end

subgraph PS ["Payment Service - com.bank.demo.payment"]
P_API["Payment API
payment.api
channel: MOBILE|WEB"]
P_ORC["Payment Orchestrator
payment.orchestrator
rail: IPS|SWIFT|INTERNAL"]
P_RAIL["Rail Adapter
payment.rail
IPS | SWIFT | INTERNAL"]
P_GL["GL Posting
payment.gl
DEBIT entry + CREDIT entry"]
P_REPO["Payment Repository
payment.repository"]
P_PUB["Event Publisher
PaymentProcessedEvent
GLDebitPostedEvent
GLCreditPostedEvent"]
P_CON["Event Consumer
AccountActivatedEvent"]
P_API --> P_ORC
P_ORC --> P_RAIL
P_ORC --> P_GL
P_GL --> P_PUB
P_ORC --> P_REPO
P_CON --> P_ORC
end

EB[["Event - Kafka"]]

GW -->|POST register| C_API
GW -->|POST open account| A_API
GW -->|POST payment transfer| P_API

C_PUB -->|CustomerProfileCreatedEvent| EB
A_PUB -->|AccountActivatedEvent| EB
P_PUB -->|GLDebitPostedEvent
GLCreditPostedEvent
PaymentProcessedEvent| EB

EB -->|CustomerProfileCreatedEvent| A_CON
EB -->|AccountActivatedEvent| P_CON
EB -->|GLDebitPostedEvent
GLCreditPostedEvent| A_CON

style CS fill:#E6F1FB,stroke:#185FA5
style AS fill:#E1F5EE,stroke:#0F6E56
style PS fill:#FAECE7,stroke:#993C1D
style GW fill:#FAEEDA,stroke:#854F0B
style EB fill:#EEEDFE,stroke:#534AB7
style Client fill:#F1EFE8,stroke:#5F5E5A
```

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber

    actor Client
    participant GW as API Gateway
    participant CS as Customer Service
    participant EB as Event Bus
    participant AS as Account Service
    participant PS as Payment Service
    participant RAIL as IPS / SWIFT / Internal

    rect rgb(230, 241, 251)
        Note over Client,CS: Step 1 - Customer Registration
        Client->>GW: POST /customers/register
        Note right of Client: name, nationalId,<br/>email, phone
        GW->>CS: route → customer.api
        CS->>CS: customer.service validates profile
        CS->>CS: customer.repository saves profile
        CS->>CS: generates customerId
        CS-->>GW: 201 Created customerId
        GW-->>Client: 201 Created customerId
        CS--)EB: publish CustomerProfileCreatedEvent
        Note right of CS: customerId,<br/>profileStatus: ACTIVE
    end

    rect rgb(225, 245, 238)
        Note over EB,AS: Step 2 - Default Account Auto-Creation
        EB--)AS: deliver CustomerProfileCreatedEvent
        AS->>AS: account.consumer receives event
        AS->>AS: account.service creates default CURRENT account
        AS->>AS: account.service sets status = ACTIVE
        AS->>AS: account.service sets opening balance = 0
        AS->>AS: account.repository saves account
        AS->>AS: account.ledger initialises balance record
        AS->>AS: generates accountId
        AS--)EB: publish AccountActivatedEvent
        Note right of AS: accountId, customerId,<br/>type: CURRENT,<br/>status: ACTIVE
        AS-->>Client: push — default account activated
    end

    %% rect rgb(225, 245, 238)
    %%     Note over Client,AS: Act 3 - Customer Opens Additional Account
    %%     Client->>GW: POST /accounts/open
    %%     Note right of Client: customerId,<br/>accountType: SAVINGS
    %%     GW->>AS: route → account.api
    %%     AS->>AS: account.service validates customerId exists
    %%     AS->>AS: account.service creates SAVINGS account
    %%     AS->>AS: account.service sets status = ACTIVE
    %%     AS->>AS: account.repository saves account
    %%     AS->>AS: account.ledger initialises balance record
    %%     AS->>AS: generates accountId
    %%     AS-->>GW: 201 Created accountId status ACTIVE
    %%     GW-->>Client: 201 Created accountId status ACTIVE
    %%     AS--)EB: publish AccountActivatedEvent
    %%     Note right of AS: accountId, customerId,<br/>type: SAVINGS,<br/>status: ACTIVE
    %% end

    rect rgb(250, 236, 231)
        Note over Client,RAIL:  Payment Initiation and Orchestration
        Client->>GW: POST /payments/transfer
        Note right of Client: customerId,<br/>debitAccountId,<br/>creditAccountId,<br/>amount, currency,<br/>channel
        GW->>PS: route → payment.api
        PS->>PS: payment.orchestrator validates customerId
        PS->>PS: payment.orchestrator validates debitAccountId
        Note right of PS: debitAccountId must<br/>belong to customerId
        PS->>PS: payment.orchestrator validates creditAccountId
        PS->>PS: payment.orchestrator validates both accounts ACTIVE
        Note right of PS: IPS | SWIFT |<br/>INTERNAL
        PS->>RAIL: submit payment instruction
        RAIL-->>PS: payment accepted + RefNo
        PS->>PS: payment.repository saves transaction
        PS->>PS: generates transactionId
        PS-->>GW: 200 OK transactionId RefNo
        GW-->>Client: 200 OK transactionId RefNo
    end
```

## Sequence Diagram-Customer Registration and Payment scenario

```text

DEFINE SYSTEM Banking Platform AS com.bank.demo

DEFINE SERVICE Customer Service AS customer_service
  DEFINE DOMAIN Customer AS customer
    DEFINE COMPONENT Customer API AS api
    DEFINE COMPONENT Customer Business Logic AS service
    DEFINE COMPONENT Customer Repository AS repository
    DEFINE COMPONENT Customer Event Publisher AS events
  CONST CUSTOMER_ID AS customerId
  CONST PROFILE_STATUS AS PENDING | ACTIVE | SUSPENDED

DEFINE SERVICE Account Service AS account_service
  DEFINE DOMAIN Account AS account
    DEFINE COMPONENT Account API AS api
    DEFINE COMPONENT Account Business Logic AS service
    DEFINE COMPONENT Account Repository AS repository
    DEFINE COMPONENT Account Balance Ledger AS ledger
    DEFINE COMPONENT Account Event Publisher AS events
    DEFINE COMPONENT Account Event Consumer AS consumer
  CONST ACCOUNT_KEY AS customerId
  CONST ACCOUNT_TYPE AS CURRENT | SAVINGS | LOAN
  CONST ACCOUNT_STATUS AS ACTIVE | INACTIVE | SUSPENDED
  CONST BALANCE_OWNER AS account_service

DEFINE SERVICE Payment Service AS payment_service
  DEFINE DOMAIN Payment AS payment
    DEFINE COMPONENT Payment API AS api
    DEFINE COMPONENT Payment Orchestrator AS orchestrator
    DEFINE COMPONENT Payment Rail Adapter AS rail
    DEFINE COMPONENT Payment GL Posting AS gl
    DEFINE COMPONENT Payment Repository AS repository
    DEFINE COMPONENT Payment Event Publisher AS events
    DEFINE COMPONENT Payment Event Consumer AS consumer
  CONST DEBIT_ACCOUNT AS debitAccountId
  CONST CREDIT_ACCOUNT AS creditAccountId
  CONST CHANNEL AS MOBILE | WEB | ATM | BRANCH
  CONST RAIL AS IPS | SWIFT | INTERNAL | CARD
  CONST GL_ENTRY AS DEBIT | CREDIT

DEFINE SERVICE API Gateway AS api_gateway
DEFINE SERVICE Event Bus AS event_bus

ASSERT(CLASSES are only CONTAINED within COMPONENTS 
       within DOMAINS within SERVICES)
ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)

ASSERT(customer_service publishes 
       CustomerProfileCreatedEvent TO event_bus)
ASSERT(account_service.consumer SUBSCRIBES TO 
       CustomerProfileCreatedEvent FROM event_bus)
ASSERT(account_service.service CREATES default account 
       ON CustomerProfileCreatedEvent WITH status ACTIVE)

ASSERT(account_service.api ACCEPTS additional account 
       requests FROM customerId)
ASSERT(account_service.service VALIDATES customerId 
       EXISTS before opening additional account)
ASSERT(account_service.ledger IS SOLE OWNER of balance)
ASSERT(payment_service has NO DEPENDENCY on 
       account_service.ledger)

ASSERT(payment_service.orchestrator RESOLVES rail 
       AS IPS | SWIFT | INTERNAL | CARD)
ASSERT(payment_service.orchestrator VALIDATES 
       debitAccountId AND creditAccountId via event_bus)
ASSERT(payment_service.gl POSTS GL_ENTRY DEBIT 
       TO event_bus AS GLDebitPostedEvent)
ASSERT(payment_service.gl POSTS GL_ENTRY CREDIT 
       TO event_bus AS GLCreditPostedEvent)

ASSERT(account_service.consumer SUBSCRIBES TO 
       GLDebitPostedEvent FROM event_bus)
ASSERT(account_service.consumer SUBSCRIBES TO 
       GLCreditPostedEvent FROM event_bus)
ASSERT(account_service.ledger UPDATES balance 
       ON GLDebitPostedEvent)
ASSERT(account_service.ledger UPDATES balance 
       ON GLCreditPostedEvent)

ASSERT(payment_service.events PUBLISHES 
       PaymentProcessedEvent TO event_bus)

FOREACH $S IN {customer_service, account_service, 
               payment_service} DO
  ASSERT($S.api has NO DEPENDENCY on $S.repository)
  ASSERT($S.repository has NO DEPENDENCY on other SERVICES)
  ASSERT($S.api is REACHABLE via api_gateway ONLY)
  ASSERT($S.events DEPENDS ON event_bus)
END
```
