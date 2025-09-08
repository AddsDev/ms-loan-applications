package com.crediya.loan.model.loan;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ApplicationSummary(
        String id,
        String email,
        String document,
        String applicantName,
        String loanType,
        String status,
        BigDecimal amount,
        Integer termInMonths,
        BigDecimal interestRateAnnual,
        BigDecimal baseSalary,
        BigDecimal requestMonthly,
        OffsetDateTime createdAt
) {
}
