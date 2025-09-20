package com.crediya.loan.r2dbc.adapter;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface ReactiveTransaction {
    <T> Mono<T> transactional(Supplier<Mono<T>> transactionalMono);
}
