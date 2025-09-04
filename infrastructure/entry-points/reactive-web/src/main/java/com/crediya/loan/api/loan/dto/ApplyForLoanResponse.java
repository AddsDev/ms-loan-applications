package com.crediya.loan.api.loan.dto;

import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.LoanType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ApplyForLoanResponse(
        String id,
        String document,
        String email,
        BigDecimal amount,
        int termInMonths,
        LoanType type,
        ApplicationStatus status,
        OffsetDateTime createdAt
) {
}
