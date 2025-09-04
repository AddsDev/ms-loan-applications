package com.crediya.loan.model.loan;

import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public final class TestHelper {

    private TestHelper() {}
    public static ApplyForLoanCommand anyCmd() {
        return new ApplyForLoanCommand(
                new Document("123456"),
                new Email("user@test.com"),
                new BigDecimal("1000000.00"),
                24,
                LoanType.CONSUMER
        );
    }

    public static LoanPolicies policiesForConsumer() {
        Map<LoanType, AmountRange> amounts = new EnumMap<>(LoanType.class);
        Map<LoanType, TermRange> terms   = new EnumMap<>(LoanType.class);

        amounts.put(LoanType.CONSUMER, new AmountRange(new BigDecimal("500000.00"), new BigDecimal("30000000.00")));
        terms.put(LoanType.CONSUMER, new TermRange(6, 84));

        return new LoanPolicies(amounts, terms);
    }
}
