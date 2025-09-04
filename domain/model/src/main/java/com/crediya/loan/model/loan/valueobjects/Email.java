package com.crediya.loan.model.loan.valueobjects;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import lombok.NonNull;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_RX = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD, "Email cannot be null or blank");
        }
        if (!EMAIL_RX.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "Invalid email format");
        }
        if (value.length() > 160) {
            throw new ValidationException(ErrorCode.INVALID_LENGTH, "Email cannot be longer than 160 characters");
        }
    }

    @Override
    @NonNull
    public String toString() {
        return value.toLowerCase();
    }
}
