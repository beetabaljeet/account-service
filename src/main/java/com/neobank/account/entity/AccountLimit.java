package com.neobank.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLimit {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "daily_debit_limit", precision = 15, scale = 2)
    private BigDecimal dailyDebitLimit;

    @Column(name = "daily_credit_limit", precision = 15, scale = 2)
    private BigDecimal dailyCreditLimit;

    @Column(name = "max_balance_limit", precision = 15, scale = 2)
    private BigDecimal maxBalanceLimit;

    @Column(name = "single_txn_limit", precision = 15, scale = 2)
    private BigDecimal singleTxnLimit;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
