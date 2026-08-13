package com.neobank.account.dto;

import com.neobank.account.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "New status is required")
    private AccountStatus status;

    private String reason;

    private String changedBy = "SYSTEM";
}
