package com.bank.demo.customer.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventPublisher.class);

    private final KafkaTemplate<String, CustomerProfileCreatedEvent> kafkaTemplate;
    private final String topic;

    public CustomerEventPublisher(
            KafkaTemplate<String, CustomerProfileCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.customer-profile-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishProfileCreated(CustomerProfileCreatedEvent event) {
        kafkaTemplate.send(topic, event.customerId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish CustomerProfileCreatedEvent for customerId={}: {}",
                                event.customerId(), ex.getMessage(), ex);
                    } else {
                        log.info("Published CustomerProfileCreatedEvent for customerId={} to topic={} partition={} offset={}",
                                event.customerId(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
