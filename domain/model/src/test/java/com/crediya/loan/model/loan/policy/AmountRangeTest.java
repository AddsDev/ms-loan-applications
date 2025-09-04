package com.crediya.loan.model.loan.policy;

import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class AmountRangeTest {
    @Test
    void shouldCreateAmountRangeValid() {
        AmountRange r = new AmountRange(new BigDecimal("1.00"), new BigDecimal("10.00"));
        assertThat(r.minAmount()).isEqualByComparingTo("1.00");
        assertThat(r.maxAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldFailWhenNullOrNegativeOrMinGreaterThanMax() {
        assertThatThrownBy(() -> new AmountRange(null, new BigDecimal("1"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new AmountRange(new BigDecimal("1"), null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new AmountRange(new BigDecimal("-1"), new BigDecimal("1"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new AmountRange(new BigDecimal("10"), new BigDecimal("-1"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new AmountRange(new BigDecimal("5"), new BigDecimal("1"))).isInstanceOf(ValidationException.class);
    }
}