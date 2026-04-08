package com.bank.demo.account.api;

import com.bank.demo.account.service.AccountSummary;

import java.util.UUID;

public record AccountResponse(UUID accountId, UUID customerId, String type, String status) {

    public static AccountResponse from(AccountSummary summary) {
        return new AccountResponse(
                summary.accountId(),
                summary.customerId(),
                summary.type(),
                summary.status()
        );
    }
}
