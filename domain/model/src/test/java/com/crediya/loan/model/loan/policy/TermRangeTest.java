package com.crediya.loan.model.loan.policy;

import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TermRangeTest {
    @Test
    void okWhenPositiveAndOrdered() {
        TermRange r = new TermRange(6, 84);
        assertThat(r.minTerm()).isEqualTo(6);
        assertThat(r.maxTerm()).isEqualTo(84);
    }

    @Test
    void failWhenNonPositiveOrInverted() {
        assertThatThrownBy(() -> new TermRange(0, 10)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new TermRange(10, 0)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new TermRange(10, 5)).isInstanceOf(ValidationException.class);
    }
}