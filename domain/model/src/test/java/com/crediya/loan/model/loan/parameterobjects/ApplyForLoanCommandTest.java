package com.crediya.loan.model.loan.parameterobjects;


import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ApplyForLoanCommandTest {

    @Test
    void ownerEmailReturnsUnderlyingEmailValue() {
        var cmd = new ApplyForLoanCommand(
                new Document("123456"),
                new Email("USER@Test.com"),
                new BigDecimal("100000.00"),
                12,
                LoanType.CONSUMER
        );
        assertThat(cmd.ownerEmail()).isEqualTo("USER@Test.com");
    }

}