package com.crediya.loan.model.loan.rules;

import com.crediya.loan.model.common.rules.DomainRule;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;

import java.util.Objects;

public class TermAllowedForTypeRule implements DomainRule<LoanApplication> {
    private final LoanPolicies policies;

    public TermAllowedForTypeRule(LoanPolicies policies) {
        this.policies = Objects.requireNonNull(policies);
    }

    @Override
    public boolean isSatisfiedBy(LoanApplication domain) {
        TermRange range = policies.getTermInMonthsRange(domain.loanType());
        if (range == null) {
            return false;
        }
        return domain.term().value() >= range.minTerm()
                && domain.term().value() <= range.maxTerm();
    }

    @Override
    public String getErrorMessage(LoanApplication domain) {
        TermRange range = policies.getTermInMonthsRange(domain.loanType());
        return (range == null)
                ? "Term policy not configured for loan type."
                : String.format(
                "Term must be between %d and %d months for loan type %s.",
                range.minTerm(),
                range.maxTerm(),
                domain.loanType()
        );
    }
}
