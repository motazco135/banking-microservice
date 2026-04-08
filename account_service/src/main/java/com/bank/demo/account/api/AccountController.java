package com.bank.demo.account.api;

import com.bank.demo.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ADL: account.api — open additional account
 * Reachable via API Gateway only (POST /accounts).
 * Must NOT depend on account.repository — delegates entirely to account.service.
 */
@Tag(name = "Accounts", description = "Open and manage customer accounts")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
        summary = "Open an additional account",
        description = "Opens a new account (CURRENT | SAVINGS | LOAN) for an existing customer. " +
                      "Routed via API Gateway only.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Account created",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
        }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@Valid @RequestBody OpenAccountRequest request) {
        return AccountResponse.from(
                accountService.openAccount(request.customerId(), request.type()));
    }

    @Operation(
            summary = "Get Customer All Accounts",
            description = "Get Customer All Accounts " +"Routed via API Gateway only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer Account List",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            }
    )
    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountResponse> getCustomerAccountList(@PathVariable UUID customerId) {
        return accountService.getCustomerAccountList(customerId).stream()
                .map(AccountResponse::from)
                .toList();
    }

}
