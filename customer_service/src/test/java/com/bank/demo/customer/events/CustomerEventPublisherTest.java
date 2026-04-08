package com.bank.demo.customer.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerEventPublisherTest {

    @Mock
    private KafkaTemplate<String, CustomerProfileCreatedEvent> kafkaTemplate;

    private static final String TOPIC = "customer.profile.created";

    // --- Positive tests ---

    @Test
    void publishProfileCreated_sendsEventToCorrectTopic() {
        UUID customerId = UUID.randomUUID();
        CustomerProfileCreatedEvent event = new CustomerProfileCreatedEvent(customerId, "ACTIVE");

        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, CustomerProfileCreatedEvent>> future =
                mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        CustomerEventPublisher publisher = new CustomerEventPublisher(kafkaTemplate, TOPIC);
        publisher.publishProfileCreated(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CustomerProfileCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerProfileCreatedEvent.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(TOPIC);
        assertThat(keyCaptor.getValue()).isEqualTo(customerId.toString());
        assertThat(eventCaptor.getValue().customerId()).isEqualTo(customerId);
        assertThat(eventCaptor.getValue().profileStatus()).isEqualTo("ACTIVE");
    }

    // --- Negative tests ---

    @Test
    void publishProfileCreated_kafkaSendFails_doesNotThrow() {
        UUID customerId = UUID.randomUUID();
        CustomerProfileCreatedEvent event = new CustomerProfileCreatedEvent(customerId, "ACTIVE");

        CompletableFuture<SendResult<String, CustomerProfileCreatedEvent>> failedFuture =
                CompletableFuture.failedFuture(new RuntimeException("Kafka broker unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        CustomerEventPublisher publisher = new CustomerEventPublisher(kafkaTemplate, TOPIC);

        // Must not throw — failure is handled asynchronously via whenComplete logging
        publisher.publishProfileCreated(event);

        verify(kafkaTemplate).send(eq(TOPIC), eq(customerId.toString()), eq(event));
    }
}
