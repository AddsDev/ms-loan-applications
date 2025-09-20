package com.crediya.loan.model.capacity;

import com.crediya.loan.model.capacity.policy.Policies;
import com.crediya.loan.model.capacity.valueobjects.ActiveLoan;
import com.crediya.loan.model.capacity.valueobjects.Applicant;
import com.crediya.loan.model.capacity.valueobjects.NewLoan;

import java.time.OffsetDateTime;
import java.util.List;

public record LoanCapacityRequested(
        String eventName,
        int eventVersion,
        String loanId,
        int lockVersion,
        Applicant applicant,
        NewLoan newLoan,
        List<ActiveLoan> activeLoans,
        Policies policies,
        OffsetDateTime createdAt
) {
    public static LoanCapacityRequested of(String loanId,
                                           Applicant applicant,
                                           NewLoan newLoan,
                                           List<ActiveLoan> activeLoans,
                                           Policies policies,
                                           OffsetDateTime createdAt) {
        return new LoanCapacityRequested(
                "LoanCapacityRequested",
                1,
                loanId,
                1,
                applicant,
                newLoan,
                activeLoans,
                policies,
                createdAt == null ? OffsetDateTime.now() : createdAt
        );
    }
}
