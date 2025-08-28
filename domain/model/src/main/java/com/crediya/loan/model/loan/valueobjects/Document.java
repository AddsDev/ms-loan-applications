package com.crediya.loan.model.loan.valueobjects;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import lombok.NonNull;

import java.util.Objects;
import java.util.regex.Pattern;

public record Document(String value) {
    private static final Pattern VALID = Pattern.compile("^\\d{6,12}$");

    public Document {
        if (value == null || value.isBlank()) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD, "document is required");
        }
        if (!VALID.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.INVALID_FORMAT, "document must be 6-12 digits");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Document that && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    @NonNull
    public String toString() {
        return value;
    }
}
