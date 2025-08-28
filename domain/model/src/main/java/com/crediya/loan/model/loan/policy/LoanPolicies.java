package com.crediya.loan.model.loan.policy;

import com.crediya.loan.model.loan.LoanType;

import java.util.Map;


//TODO: Validar para un Strategy si es requerido
public final class LoanPolicies {
    private final Map<LoanType, AmountRange> amountRanges;
    private final Map<LoanType, TermRange> termInMonthsRanges;

    public LoanPolicies(Map<LoanType, AmountRange> amountRanges,
                         Map<LoanType, TermRange> termInMonthsRanges) {
        this.amountRanges = amountRanges;
        this.termInMonthsRanges = termInMonthsRanges;
    }

    public AmountRange getAmountRange(LoanType type) {
        return amountRanges.get(type);
    }

    public TermRange getTermInMonthsRange(LoanType type) {
        return termInMonthsRanges.get(type);
    }
}
