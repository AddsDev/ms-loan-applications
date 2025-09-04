package com.crediya.loan.model.common.exceptions;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException{

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

}
