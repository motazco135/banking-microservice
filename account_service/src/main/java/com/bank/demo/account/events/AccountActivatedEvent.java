package com.bank.demo.account.events;

import java.util.UUID;

/**
 * Outbound event — published by Account Service when a default account is activated.
 * ADL: account_service.events DEPENDS ON event_bus
 * Payload: accountId, customerId, type: CURRENT, status: ACTIVE
 */
public record AccountActivatedEvent(UUID accountId, UUID customerId, String type, String status) {}
