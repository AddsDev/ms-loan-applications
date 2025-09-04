package com.crediya.loan.model.loan.valueobjects;


import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TermInMonthsTest {
    @Test
    void shouldCreateARangeValid() {
        assertThat(new TermInMonths(1).value()).isEqualTo(1);
        assertThat(new TermInMonths(360).value()).isEqualTo(360);
    }

    @Test
    void shouldFailWhenOutOfRange() {
        assertThatThrownBy(() -> new TermInMonths(0)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new TermInMonths(361)).isInstanceOf(ValidationException.class);
    }
}