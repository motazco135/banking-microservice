package com.bank.demo.account.consumer;

import com.bank.demo.account.events.CustomerProfileCreatedEvent;
import com.bank.demo.account.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ADL: account_service.consumer SUBSCRIBES TO CustomerProfileCreatedEvent FROM event_bus
 * Receives the event and delegates to AccountService to create the default account.
 */
@Component
public class CustomerProfileCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CustomerProfileCreatedEventConsumer.class);

    private final AccountService accountService;

    public CustomerProfileCreatedEventConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(topics = "${kafka.topics.customer-profile-created}")
    public void onCustomerProfileCreated(CustomerProfileCreatedEvent event) {
        log.info("Received CustomerProfileCreatedEvent: customerId={}", event.customerId());
        accountService.createDefaultAccount(event.customerId());
    }
}
