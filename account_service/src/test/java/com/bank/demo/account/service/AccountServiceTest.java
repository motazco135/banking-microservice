package com.bank.demo.account.service;

import com.bank.demo.account.events.AccountEventPublisher;
import com.bank.demo.account.ledger.AccountLedger;
import com.bank.demo.account.repository.Account;
import com.bank.demo.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private AccountLedger accountLedger;
    @Mock private AccountEventPublisher eventPublisher;

    @InjectMocks private AccountService accountService;

    // ------------------------------------------------------------------
    // Positive
    // ------------------------------------------------------------------

    @Test
    void createDefaultAccount_savesCurrentAccountWithActiveStatus() {
        UUID customerId = UUID.randomUUID();
        UUID accountId  = UUID.randomUUID();
        Account saved   = new Account(customerId, Account.Type.CURRENT, Account.Status.ACTIVE);
        ReflectionTestUtils.setField(saved, "id", accountId);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        accountService.createDefaultAccount(customerId);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account persisted = captor.getValue();
        assertThat(persisted.getCustomerId()).isEqualTo(customerId);
        assertThat(persisted.getType()).isEqualTo(Account.Type.CURRENT);
        assertThat(persisted.getStatus()).isEqualTo(Account.Status.ACTIVE);
    }

    @Test
    void createDefaultAccount_initialisesLedgerWithSavedAccountId() {
        UUID customerId = UUID.randomUUID();
        UUID accountId  = UUID.randomUUID();
        Account saved   = new Account(customerId, Account.Type.CURRENT, Account.Status.ACTIVE);
        ReflectionTestUtils.setField(saved, "id", accountId);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        accountService.createDefaultAccount(customerId);

        verify(accountLedger).initialiseBalance(accountId);
    }

    @Test
    void createDefaultAccount_publishesAccountActivatedEvent() {
        UUID customerId = UUID.randomUUID();
        UUID accountId  = UUID.randomUUID();
        Account saved   = new Account(customerId, Account.Type.CURRENT, Account.Status.ACTIVE);
        ReflectionTestUtils.setField(saved, "id", accountId);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        accountService.createDefaultAccount(customerId);

        verify(eventPublisher).publishAccountActivated(saved);
    }

    @Test
    void openAccount_returnsSummaryWithCorrectFields() {
        UUID customerId = UUID.randomUUID();
        UUID accountId  = UUID.randomUUID();
        Account saved   = new Account(customerId, Account.Type.SAVINGS, Account.Status.ACTIVE);
        ReflectionTestUtils.setField(saved, "id", accountId);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountSummary summary = accountService.openAccount(customerId, "SAVINGS");

        assertThat(summary.accountId()).isEqualTo(accountId);
        assertThat(summary.customerId()).isEqualTo(customerId);
        assertThat(summary.type()).isEqualTo("SAVINGS");
        assertThat(summary.status()).isEqualTo("ACTIVE");
    }

    @Test
    void openAccount_whenInvalidType_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> accountService.openAccount(UUID.randomUUID(), "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository);
    }

    // ------------------------------------------------------------------
    // Negative
    // ------------------------------------------------------------------

    @Test
    void createDefaultAccount_whenRepositoryThrows_ledgerAndPublisherAreNeverCalled() {
        UUID customerId = UUID.randomUUID();
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new RuntimeException("DB unavailable"));

        assertThatThrownBy(() -> accountService.createDefaultAccount(customerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB unavailable");

        verifyNoInteractions(accountLedger);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createDefaultAccount_whenLedgerThrows_publisherIsNeverCalled() {
        UUID customerId = UUID.randomUUID();
        UUID accountId  = UUID.randomUUID();
        Account saved   = new Account(customerId, Account.Type.CURRENT, Account.Status.ACTIVE);
        ReflectionTestUtils.setField(saved, "id", accountId);

        when(accountRepository.save(any(Account.class))).thenReturn(saved);
        org.mockito.Mockito.doThrow(new RuntimeException("Ledger error"))
                .when(accountLedger).initialiseBalance(any());

        assertThatThrownBy(() -> accountService.createDefaultAccount(customerId))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(eventPublisher);
    }
}
