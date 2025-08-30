package com.crediya.loan.api.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RequestValidatorConfig {

    private final Validator validator;

    public <T> Mono<T> validate(T object) {
        if (object == null) return Mono.error(new IllegalArgumentException("Object to validate cannot  be null"));
        Set<ConstraintViolation<T>> violations = this.validator.validate(object);
        return violations.isEmpty() ? Mono.just(object) : Mono.error(new ConstraintViolationException(violations));
    }
}
