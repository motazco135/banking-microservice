package com.bank.demo.customer.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

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
