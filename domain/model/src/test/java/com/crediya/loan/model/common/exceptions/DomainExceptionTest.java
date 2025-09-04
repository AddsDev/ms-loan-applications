package com.crediya.loan.model.common.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DomainExceptionTest {
    @Test
    void storesCodeAndMessage() {
        ValidationException ex = new ValidationException(ErrorCode.INVALID_FORMAT, "bad");
        assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_FORMAT);
        assertThat(ex).hasMessage("bad");
    }
}