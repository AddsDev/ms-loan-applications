package com.crediya.loan.model.decision.gateways;

import com.crediya.loan.model.decision.DecisionEvent;
import reactor.core.publisher.Mono;

public interface DecisionPublisherPort {
    Mono<Void> publish(DecisionEvent event);
}
