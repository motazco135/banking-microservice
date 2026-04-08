package com.bank.demo.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.gateway.routes[0].id=customer-service",
                "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/customers/**",
                "spring.cloud.gateway.routes[1].id=account-service",
                "spring.cloud.gateway.routes[1].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[1].predicates[0]=Path=/accounts/**",
                "spring.cloud.gateway.routes[2].id=payment-service",
                "spring.cloud.gateway.routes[2].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[2].predicates[0]=Path=/payments/**"
        }
)
@AutoConfigureWireMock(port = 0)
class ApiGatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void resetStubs() {
        reset();
    }

    @Test
    void postCustomersRegister_routesToCustomerService() {
        stubFor(post(urlEqualTo("/customers/register"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"customerId\":\"C001\"}")));

        webTestClient.post()
                .uri("/customers/register")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void postPaymentsTransfer_routesToPaymentService() {
        stubFor(post(urlEqualTo("/payments/transfer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"transactionId\":\"T001\",\"refNo\":\"REF001\"}")));

        webTestClient.post()
                .uri("/payments/transfer")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unmappedPath_returns404() {
        webTestClient.get()
                .uri("/internal/data")
                .exchange()
                .expectStatus().isNotFound();
    }
}
