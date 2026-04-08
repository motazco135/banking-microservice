package com.bank.demo.account.events;

import com.bank.demo.account.repository.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * ADL: account_service.events DEPENDS ON event_bus
 * Publishes AccountActivatedEvent to Kafka after a default account is created.
 */
@Component
public class AccountEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public AccountEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${kafka.topics.account-activated}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishAccountActivated(Account account) {
        AccountActivatedEvent event = new AccountActivatedEvent(
                account.getId(),
                account.getCustomerId(),
                account.getType().name(),
                account.getStatus().name()
        );
        kafkaTemplate.send(topic, account.getId().toString(), event);
        log.info("Published AccountActivatedEvent: accountId={} customerId={}",
                account.getId(), account.getCustomerId());
    }
}
