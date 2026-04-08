package com.bank.demo.account.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence model for the accounts table.
 * customer_id is a logical reference only — no FK crosses into customer_db (ADL: no cross-service DB coupling).
 */
@Entity
@Table(name = "accounts")
public class Account {

    public enum Type   { CURRENT, SAVINGS, LOAN }
    public enum Status { ACTIVE, INACTIVE, SUSPENDED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Account() {}

    public Account(UUID customerId, Type type, Status status) {
        this.customerId = customerId;
        this.type = type;
        this.status = status;
    }

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId()         { return id; }
    public UUID getCustomerId() { return customerId; }
    public Type getType()       { return type; }
    public Status getStatus()   { return status; }
}
