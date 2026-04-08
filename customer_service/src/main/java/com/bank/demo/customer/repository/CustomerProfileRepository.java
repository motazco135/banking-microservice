package com.bank.demo.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
}
