package com.crediya.loan.model.common.gateways;

import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.validation.EmailValidationResult;
import reactor.core.publisher.Mono;

public interface EmailValidationPort {
    Mono<EmailValidationResult> checkEmail(Email email);
}
