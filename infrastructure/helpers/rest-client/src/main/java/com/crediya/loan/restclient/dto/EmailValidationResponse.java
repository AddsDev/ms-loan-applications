package com.crediya.loan.restclient.dto;

import java.math.BigDecimal;

public record EmailValidationResponse(String email, String name, BigDecimal baseSalary, boolean isRegistered) {
}
