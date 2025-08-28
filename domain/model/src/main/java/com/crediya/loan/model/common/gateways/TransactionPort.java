package com.crediya.loan.model.common.gateways;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface TransactionPort {
    /**
     * Executes a transactional operation, ensuring that the provided operation
     * is wrapped within a transactional context.
     *
     * @param <T>               the type of the result emitted by the provided Mono.
     * @param transactionalMono a supplier of a Mono representing the transactional operation to be executed.
     * @return a Mono emitting the result of the transactional operation, or an error if the transaction fails.
     */
    <T> Mono<T> transactional(Supplier<Mono<T>> transactionalMono);
}
