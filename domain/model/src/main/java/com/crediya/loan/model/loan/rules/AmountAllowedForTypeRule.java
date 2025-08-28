package com.crediya.loan.model.loan.rules;

import com.crediya.loan.model.common.rules.DomainRule;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;

import java.util.Objects;

public class AmountAllowedForTypeRule implements DomainRule<LoanApplication> {

    private final LoanPolicies policies;

    public AmountAllowedForTypeRule(LoanPolicies policies) {
        this.policies = Objects.requireNonNull(policies);
    }

    @Override
    public boolean isSatisfiedBy(LoanApplication domain) {
        AmountRange range = policies.getAmountRange(domain.loanType());
        if (range == null) {
            return false;
        }
        return domain.amount().value().compareTo(range.minAmount()) >= 0
                && domain.amount().value().compareTo(range.maxAmount()) <= 0;
    }

    @Override
    public String getErrorMessage(LoanApplication domain) {
        AmountRange range = policies.getAmountRange(domain.loanType());
        return (range == null)
                ? "Amount policy not configured for loan type."
                : String.format(
                "Amount must be between %s and %s for loan type %s.",
                range.minAmount(),
                range.maxAmount(),
                domain.loanType()
        );
    }
}
