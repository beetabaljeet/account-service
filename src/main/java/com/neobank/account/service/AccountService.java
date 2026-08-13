package com.neobank.account.service;

import com.neobank.account.dto.AccountResponse;
import com.neobank.account.dto.CreateAccountRequest;
import com.neobank.account.dto.UpdateStatusRequest;
import com.neobank.account.entity.Account;
import com.neobank.account.entity.AccountStatusHistory;
import com.neobank.account.enums.AccountStatus;
import com.neobank.account.exception.InvalidOperationException;
import com.neobank.account.exception.ResourceNotFoundException;
import com.neobank.account.repository.AccountRepository;
import com.neobank.account.repository.AccountStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountStatusHistoryRepository statusHistoryRepository;

    // Simple in-memory sequence for demo (in real system use DB sequence or Redis)
    private static final AtomicLong ACCOUNT_SEQ = new AtomicLong(1000001);

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for customerId={}", request.getCustomerId());

        String accountNumber = generateAccountNumber(request.getBranchCode(), request.getAccountType().name());

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .accountType(request.getAccountType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(AccountStatus.ACTIVE)          // Auto-activate for demo
                .branchCode(request.getBranchCode())
                .openedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .lastStatusChange(LocalDateTime.now())
                .build();

        Account saved = accountRepository.save(account);

        // Record status history
        saveStatusHistory(saved.getAccountId(), null, AccountStatus.ACTIVE, "Account created", "SYSTEM");

        log.info("Account created successfully: {}", saved.getAccountNumber());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateStatus(String accountNumber, UpdateStatusRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        validateStatusTransition(account.getStatus(), request.getStatus());

        AccountStatus oldStatus = account.getStatus();
        account.setStatus(request.getStatus());
        account.setLastStatusChange(LocalDateTime.now());

        if (request.getStatus() == AccountStatus.CLOSED) {
            account.setClosedAt(LocalDateTime.now());
        }
        if (request.getStatus() == AccountStatus.ACTIVE && oldStatus != AccountStatus.ACTIVE) {
            account.setActivatedAt(LocalDateTime.now());
        }

        Account updated = accountRepository.save(account);

        saveStatusHistory(updated.getAccountId(), oldStatus, request.getStatus(),
                request.getReason(), request.getChangedBy());

        log.info("Account {} status changed from {} to {}", accountNumber, oldStatus, request.getStatus());
        return mapToResponse(updated);
    }

    @Transactional
    public AccountResponse closeAccount(String accountNumber, String reason) {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(AccountStatus.CLOSED);
        request.setReason(reason != null ? reason : "Account closed by customer");
        request.setChangedBy("SYSTEM");
        return updateStatus(accountNumber, request);
    }

    @Transactional(readOnly = true)
    public boolean isAccountActive(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .orElse(false);
    }

    // ==================== Private helpers ====================

    private String generateAccountNumber(String branchCode, String accountType) {
        // Format: Branch(3) + TypeCode(2) + Sequence(7)
        String typeCode = switch (accountType) {
            case "SAVINGS" -> "11";
            case "CURRENT" -> "12";
            case "FIXED_DEPOSIT" -> "13";
            case "RECURRING_DEPOSIT" -> "14";
            default -> "10";
        };
        String branch = (branchCode != null && branchCode.length() >= 3)
                ? branchCode.substring(0, 3) : "001";
        long seq = ACCOUNT_SEQ.getAndIncrement();
        return String.format("%s%s%07d", branch, typeCode, seq);
    }

    private void validateStatusTransition(AccountStatus current, AccountStatus next) {
        if (current == AccountStatus.CLOSED) {
            throw new InvalidOperationException("Cannot change status of a CLOSED account");
        }
        if (current == next) {
            throw new InvalidOperationException("Account is already in status: " + next);
        }
        // You can add more sophisticated transition rules here
    }

    private void saveStatusHistory(Long accountId, AccountStatus oldStatus,
                                   AccountStatus newStatus, String reason, String changedBy) {
        AccountStatusHistory history = AccountStatusHistory.builder()
                .accountId(accountId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(changedBy != null ? changedBy : "SYSTEM")
                .build();
        statusHistoryRepository.save(history);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .productId(account.getProductId())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .branchCode(account.getBranchCode())
                .openedAt(account.getOpenedAt())
                .activatedAt(account.getActivatedAt())
                .closedAt(account.getClosedAt())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
