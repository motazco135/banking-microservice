package com.bank.demo.account.events;

import java.util.UUID;

/**
 * Inbound event — published by Customer Service when a customer profile is created.
 * The JSON structure matches what Customer Service sends over Kafka.
 * Type-header deserialization is disabled; this class is the configured default type.
 */
public record CustomerProfileCreatedEvent(UUID customerId, String profileStatus) {}
