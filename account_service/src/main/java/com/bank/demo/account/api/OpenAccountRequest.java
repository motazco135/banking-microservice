package com.bank.demo.account.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OpenAccountRequest(
        @NotNull UUID customerId,
        @NotBlank String type
) {}
