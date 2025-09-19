package com.crediya.loan.sqs.sender.consumer;

import com.crediya.loan.model.capacity.LoanCapacityCalculatedEvent;
import com.crediya.loan.model.capacity.events.CapacityResponse;
import com.crediya.loan.model.capacity.gateways.CapacityResultHandlerPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class AutomaticQueueSqsConsumer {
    private final SqsAsyncClient sqs;
    private final ObjectMapper mapper;
    private final TraceLoggerPort logger;
    private final String queueUrl;
    private final CapacityResultHandlerPort handler;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void start() {
        if (!running.compareAndSet(false, true)) return;
        poll();
    }

    public void stop() {
        running.set(false);
    }

    private void poll() {
        final ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)     // polling
                .visibilityTimeout(30)
                .build();

        Mono.defer(() -> Mono.fromFuture(sqs.receiveMessage(req)))
                .onErrorResume(e -> {
                    logger.error("sqs receive failed", e);
                    return Mono.empty();
                })
                .flatMapMany(response -> {
                    List<Message> msgs = response.messages();
                    if (msgs == null || msgs.isEmpty()) return Mono.<Void>empty().flux();
                    return Flux.fromIterable(msgs).flatMap(this::processOne, 4);
                })
                .repeatWhen(repeat -> repeat.takeWhile(sig -> running.get()))
                .subscribe();
    }

    private Mono<Void> processOne(Message m) {
        logger.trace("sqs received messageId={}", m.messageId());
        return Mono.fromCallable(() -> mapper.readValue(m.body(), new TypeReference<LoanCapacityCalculatedEvent>() {}))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("sqs parse fail messageId={} bodyBytes={}", m.messageId(), m.body() != null ? m.body().length() : 0, e);
                    return deleteMessage(m).onErrorResume(ex -> Mono.empty()).then(Mono.empty());
                })
                .flatMap(env ->
                        handler.handle(env)
                                .then(deleteMessage(m))
                                .timeout(Duration.ofSeconds(10))//  SLA de procesamiento
                                .retryWhen(Retry.backoff(2, Duration.ofMillis(200)).jitter(0.3).filter(this::isTransient))
                                .doOnError(e ->
                                        logger.error("sqs handle fail loanId={} messageId={}", env.loanId() != null ? env.loanId() : "unknown", m.messageId(), e)
                                ).onErrorResume(e -> Mono.empty()) // Se borra
                );
    }

    private Mono<Void> deleteMessage(Message m) {
        var del = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(m.receiptHandle())
                .build();
        return Mono.fromFuture(sqs.deleteMessage(del))
                .doOnSuccess(v -> logger.trace("sqs deleted messageId={}", m.messageId()))
                .then();
    }

    private boolean isTransient(Throwable t) {
        return (t instanceof software.amazon.awssdk.core.exception.SdkException se) && se.retryable();
    }

}
