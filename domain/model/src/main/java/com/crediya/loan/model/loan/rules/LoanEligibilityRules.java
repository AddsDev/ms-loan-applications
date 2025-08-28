package com.crediya.loan.model.loan.rules;

import com.crediya.loan.model.common.rules.CompositeRule;
import com.crediya.loan.model.common.rules.DomainRule;
import com.crediya.loan.model.common.rules.LoanTypeValidRule;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.policy.LoanPolicies;

public class LoanEligibilityRules {
    private LoanEligibilityRules() {}

    public static DomainRule<LoanApplication> createLoanEligibilityRules(LoanPolicies policies) {
        return new CompositeRule<>(
                CompositeRule.CompositeType.AND,
                new AmountAllowedForTypeRule(policies),
                new TermAllowedForTypeRule(policies),
                new LoanTypeValidRule()
        );
    }
}
