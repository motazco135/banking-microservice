package com.bank.demo.customer.events;

import java.util.UUID;

public record CustomerProfileCreatedEvent(UUID customerId, String profileStatus) {
}
