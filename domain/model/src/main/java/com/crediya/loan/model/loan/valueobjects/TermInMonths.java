package com.crediya.loan.model.loan.valueobjects;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;

public record TermInMonths(int value) {
    public TermInMonths {
        if (value <= 0) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "term must be > 0 months");
        }
        if (value > 360) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "term must be <= 360 months");
        }
    }
}
