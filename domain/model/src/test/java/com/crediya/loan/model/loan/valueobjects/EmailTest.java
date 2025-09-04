package com.crediya.loan.model.loan.valueobjects;

import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailTest {
    @Test
    void okWithValidFormatAndLowercasedToString() {
        Email e = new Email("User.Name+tag@Example.COM");
        assertThat(e.value()).isEqualTo("User.Name+tag@Example.COM");
        assertEquals("user.name+tag@example.com", e.toString());
    }

    @Test
    void failOnNullBlankInvalidOrTooLong() {
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Email("")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Email("bad@domain")).isInstanceOf(ValidationException.class);
        String longLocal = "a".repeat(161);
        assertThatThrownBy(() -> new Email(longLocal + "@mail.com"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("longer than 160");
    }
}