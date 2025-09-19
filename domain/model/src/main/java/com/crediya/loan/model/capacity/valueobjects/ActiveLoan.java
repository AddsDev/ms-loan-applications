package com.crediya.loan.model.capacity.valueobjects;

import java.math.BigDecimal;

public record ActiveLoan(BigDecimal balance, double monthlyRate, int termMonths) {}
