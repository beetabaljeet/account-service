package com.neobank.account.controller;

import com.neobank.account.dto.AccountResponse;
import com.neobank.account.dto.ApiResponse;
import com.neobank.account.dto.CreateAccountRequest;
import com.neobank.account.dto.UpdateStatusRequest;
import com.neobank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Service", description = "Core Banking - Account Management APIs")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable String accountNumber) {
        AccountResponse response = accountService.getByAccountNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/id/{accountId}")
    @Operation(summary = "Get account by internal ID")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable Long accountId) {
        AccountResponse response = accountService.getById(accountId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all accounts of a customer")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(
            @PathVariable Long customerId) {
        List<AccountResponse> response = accountService.getAccountsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{accountNumber}/status")
    @Operation(summary = "Update account status (ACTIVE / FROZEN / CLOSED etc.)")
    public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(
            @PathVariable String accountNumber,
            @Valid @RequestBody UpdateStatusRequest request) {
        AccountResponse response = accountService.updateStatus(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @PostMapping("/{accountNumber}/close")
    @Operation(summary = "Close an account")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(
            @PathVariable String accountNumber,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Account closed";
        AccountResponse response = accountService.closeAccount(accountNumber, reason);
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", response));
    }

    @GetMapping("/{accountNumber}/active")
    @Operation(summary = "Check if account is active (used by Transfer Service)")
    public ResponseEntity<ApiResponse<Boolean>> isActive(@PathVariable String accountNumber) {
        boolean active = accountService.isAccountActive(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(active));
    }
}
