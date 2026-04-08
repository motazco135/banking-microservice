package com.bank.demo.account.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence model for the balance_ledger table.
 * ADL: account.ledger IS SOLE OWNER of balance.
 */
@Entity
@Table(name = "balance_ledger")
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    protected AccountBalance() {}

    AccountBalance(UUID accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    @PrePersist
    @PreUpdate
    private void onPersist() {
        lastUpdatedAt = LocalDateTime.now();
    }

    public UUID getId()              { return id; }
    public UUID getAccountId()       { return accountId; }
    public BigDecimal getBalance()   { return balance; }
}
