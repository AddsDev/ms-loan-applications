package com.crediya.loan.model.capacity.gateways;

import com.crediya.loan.model.capacity.LoanCapacityCalculatedEvent;
import reactor.core.publisher.Mono;

public interface CapacityResultHandlerPort {
    Mono<Void> handle(LoanCapacityCalculatedEvent response);
}
