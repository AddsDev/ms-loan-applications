package com.crediya.loan.model.validation;

import com.crediya.loan.model.loan.valueobjects.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailValidationResultTest {
    @Test
    void recordHoldsValues() {
        EmailValidationResult r = new EmailValidationResult(new Email("a@b.com"), true);
        assertEquals("a@b.com",r.email().toString());
        assertThat(r.isRegistered()).isTrue();
    }
}