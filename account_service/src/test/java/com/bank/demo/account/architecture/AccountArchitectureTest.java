package com.bank.demo.account.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit enforcement of ADL ASSERT constraints for the Account Service.
 *
 * ADL source: architecture/ADL.md — ASSERT block
 *
 *   ASSERT(CLASSES are only CONTAINED within COMPONENTS within DOMAINS within SERVICES)
 *   ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)
 *   ASSERT($S.api has NO DEPENDENCY on $S.repository)
 *   ASSERT($S.repository has NO DEPENDENCY on other SERVICES)
 */
@AnalyzeClasses(
        packages = "com.bank.demo.account",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class AccountArchitectureTest {

    // -----------------------------------------------------------------------
    // Package constants
    // -----------------------------------------------------------------------

    private static final String ROOT          = "com.bank.demo.account";
    private static final String API           = ROOT + ".api..";
    private static final String SERVICE       = ROOT + ".service..";
    private static final String REPOSITORY    = ROOT + ".repository..";
    private static final String LEDGER        = ROOT + ".ledger..";
    private static final String EVENTS        = ROOT + ".events..";
    private static final String CONSUMER      = ROOT + ".consumer..";

    /** Other Banking Platform services — account_service must never import these. */
    private static final String CUSTOMER_PKG  = "com.bank.demo.customer..";
    private static final String PAYMENT_PKG   = "com.bank.demo.payment..";

    // -----------------------------------------------------------------------
    // ASSERT 1 — Classes are only contained within their designated components
    // -----------------------------------------------------------------------

    /**
     * Every production class must live inside one of the six declared
     * components.  The @SpringBootApplication root class is excluded because
     * it is the entry-point, not a component artefact.
     *
     * ADL: ASSERT(CLASSES are only CONTAINED within COMPONENTS
     *             within DOMAINS within SERVICES)
     */
    @ArchTest
    static final ArchRule classes_must_reside_in_designated_components =
            classes()
                    .that().resideInAPackage(ROOT + "..")
                    .and().areNotAnnotatedWith(
                            org.springframework.boot.autoconfigure.SpringBootApplication.class)
                    .should().resideInAnyPackage(
                            API, SERVICE, REPOSITORY, LEDGER, EVENTS, CONSUMER)
                    .because("ADL ASSERT: classes are only contained within components " +
                             "(api | service | repository | ledger | events | consumer)");

    // -----------------------------------------------------------------------
    // ASSERT 2 — api must not depend on repository
    // -----------------------------------------------------------------------

    /**
     * Controllers in account.api must never access account.repository directly;
     * all data access must flow through account.service.
     *
     * ADL: ASSERT($S.api has NO DEPENDENCY on $S.repository)
     */
    @ArchTest
    static final ArchRule api_must_not_depend_on_repository =
            noClasses()
                    .that().resideInAPackage(API)
                    .should().dependOnClassesThat().resideInAPackage(REPOSITORY)
                    .because("ADL ASSERT: account.api has NO DEPENDENCY on account.repository — " +
                             "controllers must go through account.service");

    // -----------------------------------------------------------------------
    // ASSERT 3 — repository must not depend on anything outside the domain
    // -----------------------------------------------------------------------

    /**
     * Repositories must be self-contained within the account domain.
     * They must never import Customer Service or Payment Service classes.
     *
     * ADL: ASSERT($S.repository has NO DEPENDENCY on other SERVICES)
     */
    @ArchTest
    static final ArchRule repository_must_not_depend_on_customer_service =
            noClasses()
                    .that().resideInAPackage(REPOSITORY)
                    .should().dependOnClassesThat().resideInAPackage(CUSTOMER_PKG)
                    .because("ADL ASSERT: account.repository has NO DEPENDENCY on Customer Service");

    @ArchTest
    static final ArchRule repository_must_not_depend_on_payment_service =
            noClasses()
                    .that().resideInAPackage(REPOSITORY)
                    .should().dependOnClassesThat().resideInAPackage(PAYMENT_PKG)
                    .because("ADL ASSERT: account.repository has NO DEPENDENCY on Payment Service");

    // -----------------------------------------------------------------------
    // ASSERT 4 — No direct inter-service dependencies (Feign / REST)
    // -----------------------------------------------------------------------

    /**
     * No class in account_service may carry a @FeignClient annotation.
     * Feign is a compile-time signal of a direct REST coupling to another service.
     *
     * ADL: ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)
     */
    @ArchTest
    static final ArchRule no_feign_clients_allowed =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().beAnnotatedWith(
                            "org.springframework.cloud.openfeign.FeignClient")
                    .because("ADL ASSERT: SERVICES have NO DIRECT DEPENDENCY on other SERVICES — " +
                             "FeignClient creates a direct HTTP coupling; use Kafka events instead");

    /**
     * No class may declare a dependency on RestTemplate.
     * The only legitimate HTTP calls in account_service are inbound (via Controllers);
     * outbound calls to other banking services are forbidden by ADL.
     *
     * ADL: ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)
     */
    @ArchTest
    static final ArchRule no_rest_template_usage =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName("org.springframework.web.client.RestTemplate")
                    .because("ADL ASSERT: SERVICES have NO DIRECT DEPENDENCY on other SERVICES — " +
                             "RestTemplate signals an outbound REST call; use Kafka events instead");

    /**
     * No class may declare a dependency on WebClient.
     *
     * ADL: ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)
     */
    @ArchTest
    static final ArchRule no_web_client_usage =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName(
                            "org.springframework.web.reactive.function.client.WebClient")
                    .because("ADL ASSERT: SERVICES have NO DIRECT DEPENDENCY on other SERVICES — " +
                             "WebClient signals an outbound REST call; use Kafka events instead");

    /**
     * Belt-and-suspenders: no class in account_service may import any class
     * from com.bank.demo.customer or com.bank.demo.payment at the type level.
     * This catches hand-rolled HTTP clients, shared DTOs smuggled via
     * classpath, or any other form of compile-time cross-service coupling.
     *
     * ADL: ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)
     */
    @ArchTest
    static final ArchRule no_direct_dependency_on_customer_service =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat().resideInAPackage(CUSTOMER_PKG)
                    .because("ADL ASSERT: account_service must NOT depend on Customer Service — " +
                             "cross-service communication must use Kafka events");

    @ArchTest
    static final ArchRule no_direct_dependency_on_payment_service =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat().resideInAPackage(PAYMENT_PKG)
                    .because("ADL ASSERT: account_service must NOT depend on Payment Service — " +
                             "cross-service communication must use Kafka events");
}
