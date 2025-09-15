package com.crediya.loan.api.loan.dto;

public record DecisionEventResponse(
        String id,
        String status,
        String reason,
        String updatedAt
) {
}
