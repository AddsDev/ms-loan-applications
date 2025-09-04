package com.crediya.loan.model.loan.policy;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;

public record TermRange(int minTerm, int maxTerm) {
    public TermRange {
        if (minTerm <= 0 || maxTerm <= 0)
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "Minimum and maximum terms must be positive.");
        if (minTerm > maxTerm)
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "Minimum term cannot be greater than maximum term.");
    }
}
