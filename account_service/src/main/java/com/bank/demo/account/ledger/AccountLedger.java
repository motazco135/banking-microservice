package com.bank.demo.account.ledger;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ADL: account.ledger IS SOLE OWNER of balance.
 * All balance mutations must go through this component — never direct repo access from other packages.
 */
@Component
public class AccountLedger {

    private final AccountBalanceRepository balanceRepository;

    public AccountLedger(AccountBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    /**
     * Creates a zero-balance ledger record for a newly activated account.
     * Called by AccountService as part of the default account creation flow.
     */
    public void initialiseBalance(UUID accountId) {
        balanceRepository.save(new AccountBalance(accountId, BigDecimal.ZERO));
    }
}
