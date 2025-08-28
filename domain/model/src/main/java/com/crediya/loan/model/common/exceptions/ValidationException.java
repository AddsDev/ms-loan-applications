package com.crediya.loan.model.common.exceptions;

public class ValidationException extends DomainException {
    public ValidationException(ErrorCode code, String message) {
        super(code, message);
    }
}
