package com.crediya.loan.model.loan.rules;

import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.valueobjects.Amount;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.loan.valueobjects.TermInMonths;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class AmountAllowedForTypeRuleTest {

    private LoanApplication mockLoanApplication(String amt) {
        return new LoanApplication(
                "id", new Email("u@x.com"), new Document("123456"),
                new Amount(new BigDecimal(amt)), new TermInMonths(12),
                LoanType.CONSUMER, null, OffsetDateTime.now()
        );
    }

    private LoanPolicies mockPolicies(BigDecimal min, BigDecimal max) {
        var amounts = new EnumMap<LoanType, AmountRange>(LoanType.class);
        amounts.put(LoanType.CONSUMER, new AmountRange(min, max));
        return new LoanPolicies(amounts, new EnumMap<>(LoanType.class));
    }

    @Test
    void shouldSatisfiedWhenWithinRange() {
        var rule = new AmountAllowedForTypeRule(mockPolicies(new BigDecimal("100"), new BigDecimal("200")));
        assertThat(rule.isSatisfiedBy(mockLoanApplication("150"))).isTrue();
    }

    @Test
    void shouldNotSatisfiedWhenOutOfRangeOrNoPolicy() {
        var withPolicy = new AmountAllowedForTypeRule(mockPolicies(new BigDecimal("100"), new BigDecimal("200")));
        assertThat(withPolicy.isSatisfiedBy(mockLoanApplication("50"))).isFalse();
        var noPolicy = new AmountAllowedForTypeRule(new LoanPolicies(new EnumMap<>(LoanType.class), new EnumMap<>(LoanType.class)));
        assertThat(noPolicy.isSatisfiedBy(mockLoanApplication("150"))).isFalse();
        assertThat(noPolicy.getErrorMessage(mockLoanApplication("150"))).contains("not configured");
    }

    @Test
    void shouldNotSatisfiedWhenIsNull() {
        var noPolicy = new AmountAllowedForTypeRule(new LoanPolicies(new EnumMap<>(LoanType.class), new EnumMap<>(LoanType.class)));
        assertThat(noPolicy.isSatisfiedBy(new LoanApplication(
                "id", new Email("u@x.com"), new Document("123456"),
                new Amount(new BigDecimal("150")), new TermInMonths(12),
                null, null, OffsetDateTime.now()
        ))).isFalse();
        assertThat(noPolicy.getErrorMessage(mockLoanApplication("150"))).contains("not configured");
    }
}