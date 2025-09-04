package com.crediya.loan.model.loan;

import com.crediya.loan.model.common.rules.DomainRule;
import com.crediya.loan.model.common.rules.DomainRuleValidator;
import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.rules.LoanEligibilityRules;
import com.crediya.loan.model.loan.valueobjects.Amount;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.loan.valueobjects.TermInMonths;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

public record LoanApplication(
        String id,
        Email email,
        Document identityDocument,
        Amount amount,
        TermInMonths term,
        LoanType loanType,
        ApplicationStatus status,
        OffsetDateTime createdAt
) {

    public static LoanApplication register(ApplyForLoanCommand cmd, LoanPolicies policies) {
        Objects.requireNonNull(cmd, "cmd is required");
        Objects.requireNonNull(policies, "policies are required");

        var newLoan = new LoanApplication(
                UUID.randomUUID().toString(),
                cmd.email(),
                new Document(cmd.document().value()),
                new Amount(cmd.amount()),
                new TermInMonths(cmd.termInMonths()),
                cmd.type(),
                ApplicationStatus.PENDING_REVIEW,
                OffsetDateTime.now(ZoneOffset.UTC)
        );


        DomainRule<LoanApplication> eligibility = LoanEligibilityRules.createLoanEligibilityRules(policies);
        DomainRuleValidator.validate(newLoan, eligibility);

        return newLoan;
    }

    public LoanApplication markApproved() {
        return new LoanApplication(id, email, identityDocument, amount, term, loanType, ApplicationStatus.APPROVED, createdAt);
    }

    public LoanApplication markRejected() {
        return new LoanApplication(id, email, identityDocument, amount, term, loanType, ApplicationStatus.REJECTED, createdAt);
    }
}
