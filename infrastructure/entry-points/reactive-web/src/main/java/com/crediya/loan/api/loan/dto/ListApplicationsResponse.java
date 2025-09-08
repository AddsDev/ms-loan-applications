package com.crediya.loan.api.loan.dto;

import java.math.BigDecimal;
import java.util.List;

public record ListApplicationsResponse(
        List<Item> data, int page, int size, Long total
) {
    public record Item(
            String id,
            String email,
            String document,
            String applicantName,
            String loanType,
            BigDecimal amount,
            BigDecimal rate,
            Integer termInMonths,
            String status,
            BigDecimal baseSalary,
            BigDecimal monthlyRequestAmount,
            String createdAt
    ) {
    }
}
