package com.crediya.loan.model.capacity.events;

public record CapacityResponse<T>(
        String eventName,
        T payload
) {
}
