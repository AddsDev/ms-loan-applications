package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.common.gateways.TransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ReactiveTransactionAdapter implements TransactionPort {

    private final TransactionalOperator txOperator;

    @Override
    public <T> Mono<T> transactional(Supplier<Mono<T>> transactionalMono) {
        return txOperator.transactional(Mono.defer(transactionalMono));
    }
}
