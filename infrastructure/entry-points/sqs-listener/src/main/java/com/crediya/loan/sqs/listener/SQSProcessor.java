package com.crediya.loan.sqs.listener;

import com.crediya.loan.model.capacity.LoanCapacityCalculatedEvent;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.usecase.capacity.CapacityResultUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final CapacityResultUseCase resultUseCase;
    private final TraceLoggerPort logger;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> apply(Message message) {
        logger.trace("sqs received messageId={}", message.messageId());
        return Mono.fromCallable(() -> mapper.readValue(message.body(), new TypeReference<LoanCapacityCalculatedEvent>() {}))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("sqs parse fail messageId={} bodyBytes={}", message.messageId(), message.body() != null ? message.body().length() : 0, e);
                    return Mono.empty();
                })
                .flatMap(env ->
                        resultUseCase.handle(env)
                                .doOnError(e ->
                                        logger.error("sqs handle fail loanId={} messageId={}", env.loanId() != null ? env.loanId() : "unknown", message.messageId(), e)
                                ).onErrorResume(e -> Mono.empty()) // Se borra
                ).then();
    }
}
