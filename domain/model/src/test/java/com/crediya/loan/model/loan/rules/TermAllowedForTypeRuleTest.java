package com.crediya.loan.model.loan.rules;

import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;
import com.crediya.loan.model.loan.valueobjects.Amount;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.loan.valueobjects.TermInMonths;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TermAllowedForTypeRuleTest {

    private LoanApplication mockApplicationWithTerm(int term) {
        return new LoanApplication(
                "id", new Email("u@x.com"),
                "Jhon Doe",
                new Document("123456"),
                new Amount(new BigDecimal("1000.00")), new TermInMonths(term),
                LoanType.CONSUMER,
                new BigDecimal("5100000.00"),
                null,
                OffsetDateTime.now()
        );
    }

    private LoanPolicies mockPolicies(int min, int max) {
        var terms = new EnumMap<com.crediya.loan.model.loan.LoanType, TermRange>(LoanType.class);
        terms.put(LoanType.CONSUMER, new TermRange(min, max));
        return new LoanPolicies(new EnumMap<>(LoanType.class), terms);
    }

    @Test
    void satisfiedWhenWithinRange() {
        var rule = new TermAllowedForTypeRule(mockPolicies(6, 84));
        assertThat(rule.isSatisfiedBy(mockApplicationWithTerm(24))).isTrue();
    }

    @Test
    void notSatisfiedWhenOutOfRangeOrNoPolicy() {
        var rule = new TermAllowedForTypeRule(mockPolicies(10, 100));
        assertThat(rule.isSatisfiedBy(mockApplicationWithTerm(3))).isFalse();
        var noPolicy = new TermAllowedForTypeRule(new LoanPolicies(new EnumMap<>(LoanType.class), new EnumMap<>(LoanType.class)));
        assertThat(noPolicy.isSatisfiedBy(mockApplicationWithTerm(24))).isFalse();
        assertThat(noPolicy.getErrorMessage(mockApplicationWithTerm(24))).contains("not configured");
    }
}