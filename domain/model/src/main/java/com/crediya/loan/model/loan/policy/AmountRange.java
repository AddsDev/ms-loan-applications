package com.crediya.loan.model.loan.policy;


import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;

import java.math.BigDecimal;

public record AmountRange(BigDecimal minAmount, BigDecimal maxAmount) {

    public AmountRange {
        if (minAmount == null) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD, "Minimum amount is required.");
        }
        if (maxAmount == null) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD, "Maximum amount is required.");
        }
        if (minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "Minimum amount must be greater than or equal to 0.");
        }
        if (maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "Maximum amount must be greater than or equal to 0.");
        }
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "Minimum amount cannot be greater than maximum amount.");
        }
    }
}
