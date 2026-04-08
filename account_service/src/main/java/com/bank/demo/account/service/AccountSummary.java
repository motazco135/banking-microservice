package com.bank.demo.account.service;

import java.util.UUID;

/**
 * Service-layer projection returned to the api component.
 * Keeps api decoupled from repository entities (ADL: api has NO DEPENDENCY on repository).
 */
public record AccountSummary(UUID accountId, UUID customerId, String type, String status) {}
