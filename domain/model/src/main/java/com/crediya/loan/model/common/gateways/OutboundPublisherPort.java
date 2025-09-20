package com.crediya.loan.model.common.gateways;

import reactor.core.publisher.Mono;

public interface OutboundPublisherPort<T> {
    Mono<Void> publish(T event);
}
