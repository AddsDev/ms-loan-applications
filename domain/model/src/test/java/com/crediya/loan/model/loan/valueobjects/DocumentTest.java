package com.crediya.loan.model.loan.valueobjects;


import com.crediya.loan.model.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DocumentTest {

    @Test
    void shouldCreateDocument() {
        assertThat(new Document("123456").value()).isEqualTo("123456");
        assertThat(new Document("123456789012").value()).isEqualTo("123456789012");
    }

    @Test
    void failWhenBlankOrNull() {
        assertThatThrownBy(() -> new Document(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("document is required");
        assertThatThrownBy(() -> new Document(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void failWhenNotDigitsOrOutOfRange() {
        assertThatThrownBy(() -> new Document("cc123456"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("6-12 digits");
        assertThatThrownBy(() -> new Document("12345"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Document("1234567890123"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void equalsAndHashCodeByValue() {
        assertThat(new Document("123456"))
                .isEqualTo(new Document("123456"))
                .hasSameHashCodeAs(new Document("123456"));
    }
}