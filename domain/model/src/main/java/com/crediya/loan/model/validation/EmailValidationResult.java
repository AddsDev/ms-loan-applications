package com.crediya.loan.model.validation;

import com.crediya.loan.model.loan.valueobjects.Email;

public record EmailValidationResult(Email email, boolean isRegistered) {
}
