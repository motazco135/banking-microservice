package com.bank.demo.account.consumer;

import com.bank.demo.account.events.CustomerProfileCreatedEvent;
import com.bank.demo.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerProfileCreatedEventConsumerTest {

    @Mock private AccountService accountService;

    @InjectMocks private CustomerProfileCreatedEventConsumer consumer;

    // ------------------------------------------------------------------
    // Positive
    // ------------------------------------------------------------------

    @Test
    void onCustomerProfileCreated_delegatesCustomerIdToAccountService() {
        UUID customerId = UUID.randomUUID();
        CustomerProfileCreatedEvent event = new CustomerProfileCreatedEvent(customerId, "ACTIVE");

        consumer.onCustomerProfileCreated(event);

        verify(accountService).createDefaultAccount(customerId);
    }

    // ------------------------------------------------------------------
    // Negative
    // ------------------------------------------------------------------

    @Test
    void onCustomerProfileCreated_propagatesExceptionFromService() {
        UUID customerId = UUID.randomUUID();
        CustomerProfileCreatedEvent event = new CustomerProfileCreatedEvent(customerId, "ACTIVE");
        doThrow(new RuntimeException("Service failure"))
                .when(accountService).createDefaultAccount(customerId);

        assertThatThrownBy(() -> consumer.onCustomerProfileCreated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service failure");
    }
}
