package com.crediya.loan.model.capacity;

import com.crediya.loan.model.loan.ApplicationStatus;

import java.time.OffsetDateTime;

public record LoanCapacityCalculatedEvent(
        String loanId,
        ApplicationStatus decision,
        OffsetDateTime calculatedAt
) {
}
