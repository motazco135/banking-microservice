package com.bank.demo.payment.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing the Payment Service ADL ASSERT constraints.
 *
 * ADL source: payment_service/architecture/ADL.md
 *
 * Enforced rules:
 *  1. All classes reside only within designated component packages.
 *  2. payment.api has NO dependency on payment.repository.
 *  3. payment.repository has NO dependency outside the payment domain.
 *  4. No Feign clients, RestTemplate, WebClient, or HttpClient
 *     (SERVICES have NO DIRECT DEPENDENCY on other SERVICES).
 */
@AnalyzeClasses(
        packages = "com.bank.demo.payment",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class PaymentArchitectureTest {

    private static final String ROOT    = "com.bank.demo.payment";
    private static final String API     = ROOT + ".api";
    private static final String ORC     = ROOT + ".orchestrator";
    private static final String RAIL    = ROOT + ".rail";
    private static final String GL      = ROOT + ".gl";
    private static final String REPO    = ROOT + ".repository";
    private static final String EVENTS  = ROOT + ".events";
    private static final String CONSUMER = ROOT + ".consumer";

    // -------------------------------------------------------------------------
    // Rule 1 — Classes are only contained within their designated components
    //          ADL: ASSERT(CLASSES are only CONTAINED within COMPONENTS
    //                      within DOMAINS within SERVICES)
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule classes_reside_only_in_designated_components =
            classes()
                    .that().resideInAPackage(ROOT + "..")
                    .should().resideInAnyPackage(
                            ROOT,          // PaymentServiceApplication lives here
                            API      + "..",
                            ORC      + "..",
                            RAIL     + "..",
                            GL       + "..",
                            REPO     + "..",
                            EVENTS   + "..",
                            CONSUMER + ".."
                    )
                    .as("All payment classes must reside in a designated component package: " +
                        "api | orchestrator | rail | gl | repository | events | consumer");

    // -------------------------------------------------------------------------
    // Rule 2 — payment.api must NOT depend on payment.repository
    //          ADL: ASSERT($S.api has NO DEPENDENCY on $S.repository)
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule api_must_not_depend_on_repository =
            noClasses()
                    .that().resideInAPackage(API + "..")
                    .should().dependOnClassesThat().resideInAPackage(REPO + "..")
                    .as("payment.api must not depend on payment.repository — " +
                        "controllers must go through payment.orchestrator")
                    .allowEmptyShould(true);

    // -------------------------------------------------------------------------
    // Rule 3 — payment.repository must NOT depend on other service domains
    //          ADL: ASSERT($S.repository has NO DEPENDENCY on other SERVICES)
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule repository_must_not_depend_on_other_service_domains =
            noClasses()
                    .that().resideInAPackage(REPO + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "com.bank.demo.customer..",
                            "com.bank.demo.account.."
                    )
                    .as("payment.repository must not depend on customer or account service domains")
                    .allowEmptyShould(true);

    // -------------------------------------------------------------------------
    // Rule 4a — No Feign client interfaces anywhere in the payment service
    //           ADL: ASSERT(SERVICES have NO DIRECT DEPENDENCY on other SERVICES)
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule no_feign_client_annotation =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().beAnnotatedWith("org.springframework.cloud.openfeign.FeignClient")
                    .as("Payment service must not declare @FeignClient interfaces — " +
                        "inter-service communication must go through the event bus (Kafka)");

    // -------------------------------------------------------------------------
    // Rule 4b — No dependency on the Feign library itself
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule no_feign_library_dependency =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat().resideInAPackage("feign..")
                    .as("Payment service must not use the Feign library for inter-service HTTP calls");

    // -------------------------------------------------------------------------
    // Rule 4c — No RestTemplate usage (direct HTTP calls to other services)
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule no_rest_template =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName("org.springframework.web.client.RestTemplate")
                    .as("Payment service must not use RestTemplate — " +
                        "no direct HTTP calls to other services are permitted");

    // -------------------------------------------------------------------------
    // Rule 4d — No WebClient usage (reactive HTTP calls to other services)
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule no_web_client =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName(
                            "org.springframework.web.reactive.function.client.WebClient")
                    .as("Payment service must not use WebClient — " +
                        "no direct HTTP calls to other services are permitted");

    // -------------------------------------------------------------------------
    // Rule 4e — No java.net.http.HttpClient usage
    // -------------------------------------------------------------------------
    @ArchTest
    static final ArchRule no_java_http_client =
            noClasses()
                    .that().resideInAPackage(ROOT + "..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName("java.net.http.HttpClient")
                    .as("Payment service must not use java.net.http.HttpClient — " +
                        "no direct HTTP calls to other services are permitted");
}
