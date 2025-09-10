package com.crediya.loan.model.validation;

import com.crediya.loan.model.loan.valueobjects.Email;

import java.math.BigDecimal;

public record EmailValidationResult(Email email, String name, BigDecimal baseSalary, boolean isRegistered) {
}
