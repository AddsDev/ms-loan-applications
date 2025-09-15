package com.crediya.loan.sqs.sender.adapter;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.gateways.DecisionPublisherPort;
import com.crediya.loan.sqs.sender.config.SQSSenderProperties;
import com.crediya.loan.sqs.sender.dto.DecisionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class SqsDecisionPublisherAdapter implements DecisionPublisherPort {

    private final SqsAsyncClient sqs;
    private final ObjectMapper mapper;
    private final TraceLoggerPort logger;
    private final SQSSenderProperties properties;

    @Override
    public Mono<Void> publish(DecisionEvent event) {
        return Mono.defer(() -> {
                    if (event == null) {
                        return Mono.error(new IllegalArgumentException("Event is null"));
                    }
                    final String body;

                    try {
                        body = mapper.writeValueAsString(
                                new DecisionRequest(
                                        event.eventName(),
                                        event.eventVersion(),
                                        event.loanId(),
                                        event.decision().name(),
                                        event.email().value(),
                                        event.reason(),
                                        event.createdAt().toString()
                                )
                        );
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }

                    Map<String, MessageAttributeValue> attrs = new HashMap<>();
                    attrs.put("eventName", MessageAttributeValue.builder()
                            .dataType("String").stringValue(event.eventName()).build());
                    attrs.put("eventVersion", MessageAttributeValue.builder()
                            .dataType("Number").stringValue(String.valueOf(event.eventVersion())).build());
                    attrs.put("contentType", MessageAttributeValue.builder()
                            .dataType("String").stringValue("application/json").build());

                    var rqe = SendMessageRequest.builder()
                            .queueUrl(properties.queueUrl())
                            .messageBody(body)
                            .messageAttributes(attrs);
                    logger.trace("sqs publish attempt loanId={} event={}:{}",
                            event.loanId(), event.eventName(), event.eventVersion());

                    logger.trace("Sending decision event to SQS: {}", body);


                    return Mono.fromFuture(sqs.sendMessage(rqe.build()))
                            .timeout(Duration.ofSeconds(5))
                            .doOnSuccess(response -> logger.trace("sqs publish ok messageId={} loanId={}", response.messageId(), event.loanId()))
                            .doOnError(e -> logger.error("sqs publish failed loanId={}", event.loanId(), e))
                            .then();
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(250))
                        .jitter(0.35)
                        .filter(this::isTransient)
                );
    }

    private boolean isTransient(Throwable t) {
        return (t instanceof SdkException se) && se.retryable();
    }
}
