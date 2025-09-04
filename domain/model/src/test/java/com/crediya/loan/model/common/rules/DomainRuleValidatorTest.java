package com.crediya.loan.model.common.rules;


import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DomainRuleValidatorTest {
    static class AlwaysFalse implements DomainRule<String> {
        public boolean isSatisfiedBy(String s) { return false; }
        public String getErrorMessage(String s) { return "bad"; }
    }

    @Test
    void throwsWhenRuleFails() {
        assertThatThrownBy(() -> DomainRuleValidator.validate("x", new AlwaysFalse()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("bad");
    }
}