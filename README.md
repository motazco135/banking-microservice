In this article we will see Architecture Definition Language(ADL) in
Action. The Architecture Definition Language Concept has been introduced
by [Mark Richards](https://www.linkedin.com/in/markrichards3/) and [Neal
Ford](https://nealford.com/).

# What is ADL?

Architecture Definition Language is a pseudo-code for documenting and
governing the architecture of a system.

# ADL Structure

ADL is a free text form that has the following section :

- Metadata

  Describe what the Systems Definition. It tells the systems and
  developers what the architectural test requires and dose.

  - Requires: Declare external tools or resources needed for the test,
    such as ArchUnit.

  - Description: A Human-readable comment summarizing the test purpose.

  - Prompt: Provides a metadata description of the LLM prompts used to
    generate the source code based on this pseudo-code test.

- Artifacts

  Describe the system’s structural hierarchy. Artifacts must be assigned
  both a logical name (human-readable) and a physical name (the
  corresponding namespace, directory, or root package).

  - System: Defines the systems context for the architectural test,
    where the physical\_name represents a root directory or namespace
    for the system and the logical\_name represents a human-readable
    name of the system.

  - Domain: Defines the domain context for the architectural test, where
    the physical\_name represents a high-level directory or namespace
    for the domain and the logical\_name represents a human-readable
    name of the domain.

  - SUBDOMAIN: Defines the subdomain context for the architectural test,
    where the physical\_name represents a mid-level directory or
    namespace for the subdomain and the logical\_name represents a
    human-readable name of the subdomain

  - COMPONENT: Defines a component for the architectural test, where the
    physical\_name represents a leaf-level directory or namespace for
    the component and the logical\_name represents a human-readable name
    of the component.

  - SERVICE: Defines a service (separately deployed unit of software)
    for the architectural test, where the physical\_name represents a
    high-level directory or namespace describing the service boundary
    and the logical\_name represents a human-readable name of the
    service.

  - LIBRARY: Defines a shared or third-party library for the
    architectural test, where the physical\_name represents the name of
    the library artifact and the logical\_name represents a
    human-readable name of the library.

  - CONST: Defines a constant value used within an ASSERT of FOREACH
    action

- Actions

  Describe the rules to govern the Artifacts using specific action
  keywords and relation verbs.

  - Assert: Create direct constraints that the system must follow. For
    example, ASSERT(survey has NO DEPENDENCY on {ticketing, reporting})

  - FOREACH: Write iterative logic to apply a single rule to an entire
    list of artifacts

  - Relation Verbs: Describe the connections between artifacts using
    verbs like **CONTAINED** WITHIN (to ensure lower elements stay
    strictly inside specific domains), CONTAINS, or DEPENDS ON

Example :

    DESCRIPTION Define Domains
    DEFINE DOMAIN Customer AS customer
      DEFINE COMPONENT Customer API AS api
      DEFINE COMPONENT Customer Service AS service
      DEFINE COMPONENT Customer Repository AS repository

    DEFINE DOMAIN Account AS account
      DEFINE COMPONENT Account API AS api
      DEFINE COMPONENT Account Service AS service
      DEFINE COMPONENT Account Repository AS repository

# ADL Real Example

Now lets deep dive into a rela world example where we are building a
Spring Boot Banking service for payment initiation.

The system is organized into three microservices: Customer Service,
Account Service, and Payment Service.

- Customer Service is responsible for customer details.

- Account Service holds customer bank account details along with
  associated balance, it enables the customer to open an additional
  account and maintains the account status.

- Payment service is orchestration engin. It routes to instant payment
  systems, SWIFT or internal transfer, Post GL entries, and fire events.

- Microservice Communication is only through Kafka.

- Controllers expose REST APIs, services contain business logic, and
  repositories handle persistence.

- To preserve architecture integrity, controllers must not directly
  access repositories.

- All REST APIs are accessed through the API gateway.

## Architecture Digram

<figure>
<img src="../../resources/ai/adl/arch-d.png"
alt="Architecture Digram" />
<figcaption aria-hidden="true">Architecture Digram</figcaption>
</figure>

As you see, we have three microservice Customer, Account, and Payment.
All communication between services is done through Kafka. All REST APIs
are only accessible through API Gateway.

## Sequence Digram

New Let’s See how the flow and interaction between our microservice.

<figure>
<img src="../../resources/ai/adl/sequence.png" alt="Sequence Digram" />
<figcaption aria-hidden="true">Sequence Digram</figcaption>
</figure>

- Customer registration Exposes REST API to receive customer details and
  creates a customer profile.

- Customer registration fires an event. That is consumed by Account
  service to create a default account.

- Payment Service Expose REST API to initiate Payment request and
  orchestrate payment.

## Build ADL

Now based on the architecture digram let’s build our ADL by following
the following steps:

1.  Define the Meta data

        DESCRIPTION  Architecture for Banking microservices for payment initiation
        REQUIRES ArchUnit for Java components
        PROMPT Based on this pseudo-code, write ArchUnit tests
               in Java for a Spring Boot microservices project

2.  Map the Artifacts

    Base on the architecture digram, we have the following Artifacts:

        DEFINE SERVICE Customer Service AS customer_service
          DEFINE DOMAIN Customer AS customer
            DEFINE COMPONENT Customer API AS api
            DEFINE COMPONENT Customer Business Logic AS service
            DEFINE COMPONENT Customer Repository AS repository
            DEFINE COMPONENT Customer Event Publisher AS events
          CONST CUSTOMER_ID AS customerId
          CONST PROFILE_STATUS AS PENDING | ACTIVE | SUSPENDED

We will repeat the same for each microservice domain.

1.  Define Actions and Contains

    Now we will write the architecture rules by using action keywords
    and relation verbs.

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

Complete ADL:

    DESCRIPTION  Architecture for Banking microservices for payment initiation
    REQUIRES ArchUnit for Java components
    PROMPT Based on this pseudo-code, write ArchUnit tests
           in Java for a Spring Boot microservices project

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

# Implementation

Now, we’ll dive into the actual construction of our microservices. To
streamline the initial setup, I used the [Claude Code template for
Spring
Boot.](https://piotrminkowski.com/2026/03/24/claude-code-template-for-spring-boot/)

To ensure the AI doesn’t deviate from our design patterns, I updated the
CLAUDE.MD file (the AI’s "instruction manual") with an
Architecture-First Mandate. This forces the agent to treat our design
documents as immutable laws.

    5. Architecture-First Mandate
    - Always Read the ADL: Before generating, modifying, or refactoring any code or infrastructure files (like docker-compose.yml), you MUST silently read the file: customer_service/architecture/ADL.md .

    - Strict Compliance: Never generate code that violates the constraints defined in file: customer_service/architecture/ADL.md .

    - If a user prompt asks you to do something that violates the ADL (e.g., "make a direct REST call to Account Service"), you must refuse, explain the architectural violation, and propose the compliant alternative.

I have completed the Customer and Account microservices. We have
successfully implemented our first core business scenario:

When a customer creates a profile, a trigger event is published via the
Event Broker (Kafka). The Account microservice then consumes this event
to automatically provision a default account for the user. You can
explore the full implementation on
[GitHub](https://github.com/motazco135/banking-microservice.git).

## Generate the Project Skeleton

I provided the AI agent with our Architecture Description Language (ADL)
specifications, the architecture diagram, and our sequence diagrams. By
feeding the ADL for every microservice into the context, I used a
targeted prompt to initialize the project structure:

Now, we’ll dive into building the microservices. To streamline the
setup, I’m using a [Cloud Code AI template for the Spring
Boot.](https://piotrminkowski.com/2026/03/24/claude-code-template-for-spring-boot/),
I have updated the CLAUDE.MD file by adding the following :

    Carefully read the ADL.md file in this directory. We are building the com.bank.demo Banking Platform using Java , Spring Boot , PostgreSQL, and Kafka for the Event Bus.

    As the first stage, do not implement any business logic.
    Only generate the project structure and infrastructure:
            1       Create  pom.xml
            2       Create the foundational directories for customer_service

<figure>
<img src="../../resources/ai/adl/project-structure.png"
alt="The Project Structure" />
<figcaption aria-hidden="true">The Project Structure</figcaption>
</figure>

## ArchUnit Validation

To guarantee the AI-generated components truly follow our design, I use
ArchUnit for automated validation. I prompted the AI agent to create
ArchUnit test suites that verify architectural compliance during every
build.

If the AI (or a human developer) introduces a dependency that violates
the ADL, the build fails instantly. Here is the generated test suite for
the **Customer Microservice**:

    package com.bank.demo.customer.architecture;

    /**
     * ArchUnit tests enforcing the ASSERT constraints defined in architecture/ADL.md.
     *
     * ADL: DEFINE SYSTEM Banking Platform AS com.bank.demo
     *      DEFINE SERVICE Customer Service AS customer_service
     *        DEFINE COMPONENT Customer API           AS api
     *        DEFINE COMPONENT Customer Business Logic AS service
     *        DEFINE COMPONENT Customer Repository    AS repository
     *        DEFINE COMPONENT Customer Event Publisher AS events
     */
    @AnalyzeClasses(
            packages = "com.bank.demo.customer",
            importOptions = ImportOption.DoNotIncludeTests.class
    )
    public class CustomerServiceArchitectureTest {

        private static final String ROOT_PACKAGE    = "com.bank.demo.customer..";
        private static final String API_PACKAGE     = "com.bank.demo.customer.api..";
        private static final String SERVICE_PACKAGE = "com.bank.demo.customer.service..";
        private static final String REPO_PACKAGE    = "com.bank.demo.customer.repository..";
        private static final String EVENTS_PACKAGE  = "com.bank.demo.customer.events..";

        private static final String ACCOUNT_PACKAGE = "com.bank.demo.account..";
        private static final String PAYMENT_PACKAGE = "com.bank.demo.payment..";

        // -----------------------------------------------------------------------
        // ADL ASSERT 1:
        //   CLASSES are only CONTAINED within COMPONENTS within DOMAINS within SERVICES
        //
        //   Every class in com.bank.demo.customer (except the @SpringBootApplication
        //   bootstrap class) must live in one of the four declared components.
        // -----------------------------------------------------------------------
        @ArchTest
        static final ArchRule classes_must_reside_in_designated_components =
                classes()
                        .that().resideInAPackage(ROOT_PACKAGE)
                        .and().areNotAnnotatedWith(SpringBootApplication.class)
                        .should().resideInAnyPackage(
                                API_PACKAGE,
                                SERVICE_PACKAGE,
                                REPO_PACKAGE,
                                EVENTS_PACKAGE
                        )
                        .as("ADL ASSERT 1 — all classes (except the application bootstrap) must reside "
                                + "in a designated component: api | service | repository | events");

        // -----------------------------------------------------------------------
        // ADL ASSERT 2 (FOREACH customer_service):
        //   $S.api has NO DEPENDENCY on $S.repository
        //
        //   Controllers go through the service layer only.
        // -----------------------------------------------------------------------
        @ArchTest
        static final ArchRule api_must_not_depend_on_repository =
                noClasses()
                        .that().resideInAPackage(API_PACKAGE)
                        .should().dependOnClassesThat().resideInAPackage(REPO_PACKAGE)
                        .as("ADL ASSERT 2 — api layer must not directly access the repository layer; "
                                + "all data access must go through the service layer");

        // -----------------------------------------------------------------------
        // ADL ASSERT 3 (FOREACH customer_service):
        //   $S.repository has NO DEPENDENCY on other SERVICES
        //
        //   Repository classes must be confined to the customer domain;
        //   they must not reference Account Service or Payment Service classes.
        // -----------------------------------------------------------------------
        @ArchTest
        static final ArchRule repository_must_not_depend_on_other_services =
                noClasses()
                        .that().resideInAPackage(REPO_PACKAGE)
                        .should().dependOnClassesThat().resideInAnyPackage(
                                ACCOUNT_PACKAGE,
                                PAYMENT_PACKAGE
                        )
                        .as("ADL ASSERT 3 — repository layer must have no dependency on Account "
                                + "or Payment service classes");

        // -----------------------------------------------------------------------
        // ADL ASSERT 4a:
        //   SERVICES have NO DIRECT DEPENDENCY on other SERVICES
        //   — no @FeignClient declarations anywhere in the Customer Service.
        //
        //   Feign client interfaces create compile-time coupling between services,
        //   which violates the event-driven integration contract.
        // -----------------------------------------------------------------------
        @ArchTest
        static final ArchRule no_feign_client_annotations =
                noClasses()
                        .should()
                        .beAnnotatedWith("org.springframework.cloud.openfeign.FeignClient")
                        .as("ADL ASSERT 4a — Customer Service must not declare @FeignClient interfaces; "
                                + "inter-service communication must use the Kafka event bus only");

        // -----------------------------------------------------------------------
        // ADL ASSERT 4b:
        //   SERVICES have NO DIRECT DEPENDENCY on other SERVICES
        //   — no usage of the OpenFeign library (catches Builder, RequestLine, etc.)
        // -----------------------------------------------------------------------
        @ArchTest
        static final ArchRule no_feign_library_usage =
                noClasses()
                        .that().resideInAPackage(ROOT_PACKAGE)
                        .should().dependOnClassesThat().resideInAPackage("feign..")
                        .as("ADL ASSERT 4b — Customer Service must not use the OpenFeign library; "
                                + "use Kafka events for cross-service integration");

        // -----------------------------------------------------------------------
        // ADL ASSERT 4c:
        //   SERVICES have NO DIRECT DEPENDENCY on other SERVICES
        //   — no direct class-level dependency on Account or Payment service packages.
        //
        //   Catches shared-library client classes (e.g. AccountClient, PaymentClient)
        //   or any accidentally shared domain objects across service boundaries.
        // -----------------------------------------------------------------------
        @ArchTest
        static final ArchRule no_direct_dependency_on_account_or_payment_service =
                noClasses()
                        .that().resideInAPackage(ROOT_PACKAGE)
                        .should().dependOnClassesThat().resideInAnyPackage(
                                ACCOUNT_PACKAGE,
                                PAYMENT_PACKAGE
                        )
                        .as("ADL ASSERT 4c — Customer Service must not have any compile-time dependency "
                                + "on Account Service (com.bank.demo.account) or "
                                + "Payment Service (com.bank.demo.payment) classes");
    }

**Breakdown: Enforcement of ADL Assertions** :

- ADL ASSERT 1: Strict Packaging Ensures that every class lives in its
  assigned "home" (api, service, repository, or events). This prevents
  the project from becoming a "big ball of mud" where classes are
  scattered randomly.

- ADL ASSERT 2: Layered Isolation Enforces a strict downward flow. The
  API layer is forbidden from talking directly to the Repository. This
  ensures all business logic is captured in the Service layer and never
  bypassed.

- ADL ASSERT 3 & 4: Zero Coupling (The Golden Rule) This is the most
  critical check for our Event-Driven Architecture.

  - No Direct Imports: The Customer service cannot import classes from
    the Account or Payment services.

  - No Feign Clients: We explicitly forbid @FeignClient. If a developer
    (or the AI) tries to create a synchronous REST call between
    services, the build will fail.

  - The Kafka Mandate: By failing the build on direct dependencies, we
    force the system to use the Event Broker for all inter-service
    communication.

# ADL Benefits

To see the true value of Architecture Definition Language, let’s look at
a common real-world scenario.

A change request came asking:

    When querying the 'Get Customer by ID' API, the response should now also include the customer’s related account details.

In a traditional development cycle, They might implement a Feign Client
inside the Customer service to synchronously call the Account service.
While this works functionally, it violates our Architecture definition
Language (ADL) and introduces tight coupling.

So Let’s assume the development uses the following prompt to implement
the required changes :

    Create the Customer Service 'Inquiry API' :
    - The API will use Customer Id to get the customer profile.

    - The API should include the the customer account details , to achieve this create a Feign Client to call the Account Micro service (service URL : http://localhost:8082/accounts/{customerId}) directly and aggregate the data.

<figure>
<img src="../../resources/ai/adl/ai-feedback.png"
alt="Claude Code Feedback" />
<figcaption>The Result</figcaption>
</figure>

As you see, instead the AI agent starts generating the code, the AI
agent performed a prechecking :

    "I must read the ADL before generating any code, which I've done. I need to flag an architectural violation before proceeding."

Because of the Architecture-First Mandate we configured in CLAUDE.MD,
the agent identified the conflict immediately and responded with:

    Architectural Violation Detected
    The request asks to create a Feign Client that makes a direct REST call from Customer Service to Account Service. This violates the ADL constraint:
    ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)

    A Feign Client calling http://localhost:8082/accounts/{customerId} creates a synchronous runtime dependency between Customer Service and Account Service — exactly what the ADL prohibits.

Now Let’s assume the Development did not use AI agent and start writing
the code, Once they define the "@FeingClient" and build the project, the
Test Case for ARchUnit will fail.

<figure>
<img src="../../resources/ai/adl/code-change.png" alt="Code Change" />
<figcaption aria-hidden="true">Code Change</figcaption>
</figure>

<figure>
<img src="../../resources/ai/adl/build_error.png" alt="Test Fails" />
<figcaption>ArchUnit Test Fails</figcaption>
</figure>

By integrating these tests into our CI/CD pipeline, we move from
"hoping" the architecture is followed to "guaranteeing" its integrity.

If the AI agent or a developer introduces a dependency that violates our
ADL, the mvn test phase will catch it immediately.

Whenever the AI agent or a developer introduces a dependency that
violates our ADL, the mvn test phase will catch it immediately. That
failed test is more than just an error; it is the "source of truth" that
ensures our architecture diagrams and actual implementation remain in
perfect sync.

The ADL is helping to form a communication and collaboration between
Developers and Architect. The team now can communicate and evaluate the
required changes and agree to change the ADL or change the
implementation guide.

# Resources

- [developertoarchitect](https://www.developertoarchitect.com/resources.html)

- [Lesson 210 - Architecture Definition
  Language](https://www.youtube.com/watch?v=z9cfsGk-7pw)

- [Spec Driven workflow with
  Claude](https://www.youtube.com/watch?v=e_D9M_MJ9Hs&list=PL4cUxeGkcC9iq7jQNL4dp0DKAZOpciUKX)

- [Claude Code Template for Spring
  Boot](https://piotrminkowski.com/2026/03/24/claude-code-template-for-spring-boot/)
