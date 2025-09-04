package com.crediya.loan.model.loan.policy;


import com.crediya.loan.model.loan.LoanType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LoanPoliciesTest {
    @Test
    void gettersReturnConfiguredRanges() {
        var amounts = new EnumMap<LoanType, AmountRange>(LoanType.class);
        var terms   = new EnumMap<LoanType, TermRange>(LoanType.class);
        amounts.put(LoanType.CONSUMER, new AmountRange(new BigDecimal("100"), new BigDecimal("200")));
        terms.put(LoanType.CONSUMER, new TermRange(6, 12));

        LoanPolicies lp = new LoanPolicies(amounts, terms);
        assertThat(lp.getAmountRange(LoanType.CONSUMER).minAmount()).isEqualByComparingTo("100");
        assertThat(lp.getTermInMonthsRange(LoanType.CONSUMER).maxTerm()).isEqualTo(12);
        assertThat(lp.getAmountRange(LoanType.MORTGAGE)).isNull();
    }
}