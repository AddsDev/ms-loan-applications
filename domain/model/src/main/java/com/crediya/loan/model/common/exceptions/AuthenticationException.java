package com.crediya.loan.model.common.exceptions;

public class AuthenticationException extends  DomainException{
    public AuthenticationException(String message) {
        super(ErrorCode.EMAIL_MISMATCH, message);
    }
}
