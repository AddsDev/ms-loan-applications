package com.crediya.loan.model.capacity.gateways;

import com.crediya.loan.model.capacity.LoanCapacityRequested;
import reactor.core.publisher.Mono;

public interface LoanCapacityPublisherPort {
    Mono<Void> publish(LoanCapacityRequested event);
}
