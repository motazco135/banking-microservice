package com.bank.demo.customer.service;

import com.bank.demo.customer.events.CustomerEventPublisher;
import com.bank.demo.customer.events.CustomerProfileCreatedEvent;
import com.bank.demo.customer.repository.CustomerProfile;
import com.bank.demo.customer.repository.CustomerProfileRepository;
import com.bank.demo.customer.repository.ProfileStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerProfileRepository repository;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @InjectMocks
    private CustomerService customerService;

    // --- Positive tests ---

    @Test
    void registerCustomer_validRequest_savesProfileAsActive() {
        UUID generatedId = UUID.randomUUID();
        CustomerProfile savedProfile = savedProfileWith(generatedId, ProfileStatus.ACTIVE);
        when(repository.save(any(CustomerProfile.class))).thenReturn(savedProfile);

        UUID result = customerService.registerCustomer("Alice", "NID001", "alice@bank.com", "+15550001");

        assertThat(result).isEqualTo(generatedId);

        ArgumentCaptor<CustomerProfile> profileCaptor = ArgumentCaptor.forClass(CustomerProfile.class);
        verify(repository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(profileCaptor.getValue().getName()).isEqualTo("Alice");
        assertThat(profileCaptor.getValue().getNationalId()).isEqualTo("NID001");
        assertThat(profileCaptor.getValue().getEmail()).isEqualTo("alice@bank.com");
    }

    @Test
    void registerCustomer_validRequest_publishesCustomerProfileCreatedEvent() {
        UUID generatedId = UUID.randomUUID();
        when(repository.save(any())).thenReturn(savedProfileWith(generatedId, ProfileStatus.ACTIVE));

        customerService.registerCustomer("Alice", "NID001", "alice@bank.com", "+15550001");

        ArgumentCaptor<CustomerProfileCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerProfileCreatedEvent.class);
        verify(eventPublisher).publishProfileCreated(eventCaptor.capture());

        CustomerProfileCreatedEvent published = eventCaptor.getValue();
        assertThat(published.customerId()).isEqualTo(generatedId);
        assertThat(published.profileStatus()).isEqualTo("ACTIVE");
    }

    // --- Negative tests ---

    @Test
    void registerCustomer_duplicateNationalId_propagatesDataIntegrityViolation() {
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key: national_id"));

        assertThatThrownBy(() -> customerService.registerCustomer("Bob", "NID001", "bob@bank.com", "+15550002"))
                .isInstanceOf(DataIntegrityViolationException.class);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void registerCustomer_duplicateEmail_propagatesDataIntegrityViolation() {
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key: email"));

        assertThatThrownBy(() -> customerService.registerCustomer("Carol", "NID999", "alice@bank.com", "+15550003"))
                .isInstanceOf(DataIntegrityViolationException.class);

        verifyNoInteractions(eventPublisher);
    }

    // --- Helper ---

    private CustomerProfile savedProfileWith(UUID id, ProfileStatus status) {
        CustomerProfile profile = new CustomerProfile("Alice", "NID001", "alice@bank.com", "+15550001", status);
        // reflectively set the id that Hibernate would normally set
        try {
            var field = CustomerProfile.class.getDeclaredField("customerId");
            field.setAccessible(true);
            field.set(profile, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return profile;
    }
}
