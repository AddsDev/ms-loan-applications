package com.crediya.loan.model.common.exceptions;

public class ExternalServiceException extends DomainException {

    public ExternalServiceException(ErrorCode code, String message) {
        super(code, message);
    }
}
