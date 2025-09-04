package com.crediya.loan.model.loan.valueobjects;

import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


class AmountTest {
    @Test
    void shouldCreateValidValue() {
      Amount amount = new Amount(new BigDecimal("100.00"));
      assertEquals("100", amount.toString());
      assertThat(amount.value().toPlainString()).isEqualTo("100");
    }

    @Test
    void shouldCreateInvalidValue() {
        assertThatThrownBy(() -> new Amount(new BigDecimal("-100.00")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("> 0");
    }

    @Test
    void shouldStripTrailingZeros() {
        Amount a = new Amount(new BigDecimal("100.00"));
        assertThat(a.value().toPlainString()).isEqualTo("100"); // stripTrailingZeros
    }

    @Test
    void shouldFailWhenNull() {
        assertThatThrownBy(() -> new Amount(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("amount is required");
    }

    @Test
    void shouldFailWhenMoreThan2Decimals() {
        assertThatThrownBy(() -> new Amount(new BigDecimal("1.234")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("max 2 decimals");
    }

    @Test
    void shouldFailWhenNotPositiveOrZero() {
        assertThatThrownBy(() -> new Amount(new BigDecimal("-100.00")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("> 0");
    }
}