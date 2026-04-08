package com.bank.demo.account.api;

import com.bank.demo.account.service.AccountService;
import com.bank.demo.account.service.AccountSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock private AccountService accountService;

    @InjectMocks private AccountController controller;

    // ------------------------------------------------------------------
    // Positive
    // ------------------------------------------------------------------

    @Test
    void openAccount_returnsAccountResponseWithCorrectFields() {
        UUID customerId = UUID.randomUUID();
        UUID accountId  = UUID.randomUUID();
        AccountSummary summary = new AccountSummary(accountId, customerId, "SAVINGS", "ACTIVE");

        when(accountService.openAccount(customerId, "SAVINGS")).thenReturn(summary);

        AccountResponse response = controller.openAccount(
                new OpenAccountRequest(customerId, "SAVINGS"));

        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.type()).isEqualTo("SAVINGS");
        assertThat(response.status()).isEqualTo("ACTIVE");

        verify(accountService).openAccount(customerId, "SAVINGS");
    }

    // ------------------------------------------------------------------
    // Negative
    // ------------------------------------------------------------------

    @Test
    void openAccount_propagatesExceptionFromService() {
        UUID customerId = UUID.randomUUID();
        when(accountService.openAccount(customerId, "LOAN"))
                .thenThrow(new RuntimeException("Customer not found"));

        assertThatThrownBy(() -> controller.openAccount(
                new OpenAccountRequest(customerId, "LOAN")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Customer not found");
    }
}
