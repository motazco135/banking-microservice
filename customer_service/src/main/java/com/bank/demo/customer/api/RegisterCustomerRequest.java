package com.bank.demo.customer.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterCustomerRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "nationalId is required") String nationalId,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        @NotBlank(message = "phone is required") String phone
) {
}
