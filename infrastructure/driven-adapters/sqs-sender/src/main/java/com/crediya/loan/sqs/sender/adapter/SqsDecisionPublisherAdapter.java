package com.crediya.loan.sqs.sender.adapter;

import com.crediya.loan.model.common.gateways.OutboundPublisherPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.gateways.DecisionPublisherPort;
import com.crediya.loan.sqs.sender.config.SQSSenderProperties;
import com.crediya.loan.sqs.sender.config.SqsSenderProps;
import com.crediya.loan.sqs.sender.dto.DecisionRequest;
import com.crediya.loan.sqs.sender.serializer.DecisionEventSerializer;
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

    private final OutboundPublisherPort<DecisionEvent> delegate;

    public static SqsDecisionPublisherAdapter of(SqsAsyncClient sqs, ObjectMapper mapper, TraceLoggerPort logger, SqsSenderProps properties) {
        var serializer = new DecisionEventSerializer(mapper);
        var generic = new SqsPublisherAdapter<>(sqs, serializer, logger, properties);
        return new SqsDecisionPublisherAdapter(generic);
    }

    @Override
    public Mono<Void> publish(DecisionEvent event) {
        return delegate.publish(event);
    }
}
