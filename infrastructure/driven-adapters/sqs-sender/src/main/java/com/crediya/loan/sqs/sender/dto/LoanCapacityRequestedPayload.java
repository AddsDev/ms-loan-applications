package com.crediya.loan.sqs.sender.dto;

import java.math.BigDecimal;
import java.util.List;

public record LoanCapacityRequestedPayload(
        String eventName,
        int eventVersion,
        String loanId,
        int lockVersion,
        Applicant applicant,
        NewLoan newLoan,
        List<ActiveLoan> activeLoans,
        Policies policies,
        String createdAt
) {
    public record Applicant(String email, BigDecimal baseSalary) {}
    public record NewLoan(long amount, double interestRate, int termsInMonths) {}
    public record ActiveLoan(BigDecimal balance, double interestRate, int termsInMonths) {}
    public record Policies(double maximumIncomePercentage, int salaryManualReview) {}
}
