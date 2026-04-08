package com.bank.demo.customer.api;

import com.bank.demo.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customers", description = "Customer registration and profile management")
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Register a new customer",
               description = "Creates a customer profile with status ACTIVE and publishes a CustomerProfileCreatedEvent to the event bus")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer registered successfully",
                    content = @Content(schema = @Schema(implementation = RegisterCustomerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request — missing or malformed fields",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate nationalId or email",
                    content = @Content)
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterCustomerResponse register(@Valid @RequestBody RegisterCustomerRequest request) {
        return new RegisterCustomerResponse(
                customerService.registerCustomer(
                        request.name(), request.nationalId(), request.email(), request.phone()));
    }
}
