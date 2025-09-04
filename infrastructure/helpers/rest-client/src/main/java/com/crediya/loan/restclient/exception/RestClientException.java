package com.crediya.loan.restclient.exception;

public class RestClientException extends RuntimeException{
    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestClientException(String message) {
        super(message);
    }
}
