package com.bank.demo.account.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Package-private — only AccountLedger may access this repository.
 * This enforces the ADL invariant that ledger is the sole owner of balance.
 */
interface AccountBalanceRepository extends JpaRepository<AccountBalance, UUID> {}
