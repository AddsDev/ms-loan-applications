package com.crediya.loan.model.common.rules;

import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.valueobjects.Amount;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.loan.valueobjects.TermInMonths;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LoanTypeValidRuleTest {
    private LoanApplication app(LoanType type){
        return new LoanApplication(
                "id", new Email("u@x.com"),
                "Jhon Doe",
                new Document("123456"),
                new Amount(new BigDecimal("1000.00")),
                new TermInMonths(12),
                type,
                new BigDecimal("5100000.00"),
                null,
                OffsetDateTime.now()
        );
    }

    @Test
    void satisfiedWhenTypeNotNull() {
        assertThat(new LoanTypeValidRule().isSatisfiedBy(app(LoanType.CONSUMER))).isTrue();
    }

    @Test
    void notSatisfiedWhenTypeNull() {
        assertThat(new LoanTypeValidRule().isSatisfiedBy(app(null))).isFalse();
        assertThat(new LoanTypeValidRule().getErrorMessage(app(null))).contains("Loan type must be");
    }
}