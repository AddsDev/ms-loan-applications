package com.crediya.loan.sqs.sender.adapter;

import com.crediya.loan.model.common.gateways.OutboundPublisherPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.sqs.sender.config.SqsSenderProps;
import com.crediya.loan.sqs.sender.serializer.SqsMessageSerializer;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@RequiredArgsConstructor
public class SqsPublisherAdapter<T> implements OutboundPublisherPort<T> {
    private final SqsAsyncClient sqs;
    private final SqsMessageSerializer<T> serializer;
    private final TraceLoggerPort logger;
    private final SqsSenderProps props;

    @Override
    public Mono<Void> publish(T event) {
        return Mono.defer(() -> {
                    if (event == null) return Mono.error(new IllegalArgumentException("Event is null"));

                    return Mono.fromCallable(() -> serializer.serialize(event))
                            .doOnSubscribe(s -> logger.trace("sqs publish serialize start"))
                            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                            .flatMap(msg -> {
                                var reqBuilder = SendMessageRequest.builder()
                                        .queueUrl(props.queueUrl())
                                        .messageBody(msg.body())
                                        .messageAttributes(msg.attributes());

                                var request = reqBuilder.build();
                                logger.trace("sqs publish attempt queueUrl={}", props.queueUrl());
                                return Mono.fromFuture(sqs.sendMessage(request))
                                        .timeout(props.timeout())
                                        .doOnSuccess(resp -> logger.trace("sqs publish ok messageId={}", resp.messageId()))
                                        .doOnError(e -> logger.error("sqs publish fail", e))
                                        .then();
                            });
                })
                .retryWhen(Retry.backoff(props.maxAttempts(), props.backoff()).jitter(props.jitter()).filter(this::isTransient));
    }

    private boolean isTransient(Throwable t) {
        if (t instanceof com.fasterxml.jackson.core.JsonProcessingException) return false;
        if (t instanceof IllegalArgumentException) return false;

        return (t instanceof software.amazon.awssdk.core.exception.SdkException se) && se.retryable();
    }
}
