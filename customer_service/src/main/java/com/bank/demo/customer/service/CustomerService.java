package com.bank.demo.customer.service;

import com.bank.demo.customer.events.CustomerEventPublisher;
import com.bank.demo.customer.events.CustomerProfileCreatedEvent;
import com.bank.demo.customer.repository.CustomerProfile;
import com.bank.demo.customer.repository.CustomerProfileRepository;
import com.bank.demo.customer.repository.ProfileStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerProfileRepository repository;
    private final CustomerEventPublisher eventPublisher;

    public CustomerService(CustomerProfileRepository repository,
                           CustomerEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UUID registerCustomer(String name, String nationalId, String email, String phone) {
        CustomerProfile profile = new CustomerProfile(name, nationalId, email, phone, ProfileStatus.ACTIVE);

        CustomerProfile saved = repository.save(profile);

        eventPublisher.publishProfileCreated(
                new CustomerProfileCreatedEvent(saved.getCustomerId(), ProfileStatus.ACTIVE.name())
        );

        return saved.getCustomerId();
    }
}
