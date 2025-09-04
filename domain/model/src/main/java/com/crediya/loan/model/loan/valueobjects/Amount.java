package com.crediya.loan.model.loan.valueobjects;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import lombok.NonNull;

import java.math.BigDecimal;

public record Amount(BigDecimal value) {
    public Amount {
        if (value == null) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD, "amount is required");
        }
        if (value.scale() > 2) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "amount must have max 2 decimals");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "amount must be > 0");
        }
        value = value.stripTrailingZeros();
    }

    @Override
    @NonNull
    public String toString() {
        return value.toPlainString();
    }
}
