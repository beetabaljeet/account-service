package com.neobank.account.dto;

import com.neobank.account.enums.AccountStatus;
import com.neobank.account.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {

    private Long accountId;
    private String accountNumber;
    private Long customerId;
    private Long productId;
    private AccountType accountType;
    private String currency;
    private AccountStatus status;
    private String branchCode;
    private LocalDateTime openedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
