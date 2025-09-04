package com.crediya.loan.model.common.rules;

import com.crediya.loan.model.loan.LoanApplication;

public class LoanTypeValidRule implements DomainRule<LoanApplication> {
    @Override
    public boolean isSatisfiedBy(LoanApplication loanApplication) {
        return loanApplication.loanType() != null;
    }

    @Override
    public String getErrorMessage(LoanApplication loanApplication) {
        return "Loan type must be one of the allowed values.";
    }
}
