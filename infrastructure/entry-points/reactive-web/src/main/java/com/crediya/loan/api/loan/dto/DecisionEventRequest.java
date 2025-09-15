package com.crediya.loan.api.loan.dto;

import com.crediya.loan.model.loan.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record DecisionEventRequest(
        @NotNull(message = "The id cannot be empty")
        String id,
        @NotNull(message = "The decision cannot be empty")
        ApplicationStatus decision,
        String reason,
        @NotNull(message = "The email cannot be empty")
        String advisorEmail
) {

}
