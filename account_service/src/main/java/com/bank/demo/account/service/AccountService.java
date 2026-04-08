package com.bank.demo.account.service;

import com.bank.demo.account.events.AccountEventPublisher;
import com.bank.demo.account.ledger.AccountLedger;
import com.bank.demo.account.repository.Account;
import com.bank.demo.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Core business logic for the Account domain.
 *
 * Step 2 flow (ADL Sequence Diagram):
 *   1. Create CURRENT account with status ACTIVE
 *   2. Persist via AccountRepository
 *   3. Initialise zero-balance ledger record via AccountLedger
 *   4. Publish AccountActivatedEvent
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountLedger accountLedger;
    private final AccountEventPublisher eventPublisher;

    public AccountService(AccountRepository accountRepository,
                          AccountLedger accountLedger,
                          AccountEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.accountLedger = accountLedger;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Opens an additional account for an existing customer.
     * ADL: $S.api ACCEPTS additional account requests FROM customerId
     *
     * @param customerId must belong to an existing customer
     * @param type       requested account type (CURRENT | SAVINGS | LOAN)
     * @return the persisted Account
     */
    @Transactional
    public AccountSummary openAccount(UUID customerId, String type) {
        Account.Type accountType = Account.Type.valueOf(type);
        log.info("Opening {} account for customerId={}", accountType, customerId);

        Account account = accountRepository.save(
                new Account(customerId, accountType, Account.Status.ACTIVE));

        accountLedger.initialiseBalance(account.getId());

        log.info("Account opened: accountId={} type={} customerId={}", account.getId(), accountType, customerId);
        return new AccountSummary(account.getId(), account.getCustomerId(),
                account.getType().name(), account.getStatus().name());
    }

    /**
     * Creates a default CURRENT account for a newly registered customer.
     * Triggered by CustomerProfileCreatedEvent via the consumer component.
     *
     * @param customerId logical reference to the customer (no cross-DB FK)
     */
    @Transactional
    public void createDefaultAccount(UUID customerId) {
        log.info("Creating default CURRENT account for customerId={}", customerId);

        Account account = accountRepository.save(
                new Account(customerId, Account.Type.CURRENT, Account.Status.ACTIVE));

        accountLedger.initialiseBalance(account.getId());
        eventPublisher.publishAccountActivated(account);

        log.info("Default account created: accountId={} customerId={}", account.getId(), customerId);
    }


    /**
     * Get All Customer Accounts by customerId
     *
     * @param customerId must belong to an existing customer
     * @return
     */
    public List<AccountSummary> getCustomerAccountList(UUID customerId){
        log.info("Get all customer account for customerId={}", customerId);
        List<Account> customerAccountList = accountRepository.findAllByCustomerId(customerId);
        return customerAccountList.stream()
                .map(a -> new AccountSummary(a.getId(), a.getCustomerId(), a.getType().name(), a.getStatus().name()))
                .toList();
    }

}
