package com.crediya.loan.api.loan.dto;

import com.crediya.loan.model.loan.LoanType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ApplyForLoanRequest(
        @NotBlank(message = "document es requerido")
        @Pattern(regexp = "\\d{6,12}", message = "document debe tener entre 6 y 12 dígitos")
        String document,
        @NotBlank(message = "The email cannot be empty")
        @Email(message = "The email format is not valid")
        String email,
        @NotNull(message = "The field amount cannot be zero")
        @Digits(integer = 14, fraction = 2, message = "amount debe tener máximo 14 enteros y 2 decimales")
        @DecimalMin(value = "0.01", message = "amount debe ser mayor a 0")
        BigDecimal amount,
        @Min(value = 1, message = "termInMonths debe ser mayor o igual a 1")
        @Max(value = 360, message = "termInMonths no debe superar 360")
        int termInMonths,
        @NotNull(message = "type es requerido")
        LoanType loanType
) {

}
