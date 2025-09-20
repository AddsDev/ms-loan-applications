package com.crediya.loan.sqs.sender.adapter;

import com.crediya.loan.model.capacity.LoanCapacityRequested;
import com.crediya.loan.model.capacity.gateways.LoanCapacityPublisherPort;
import com.crediya.loan.model.common.gateways.OutboundPublisherPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.sqs.sender.config.SqsSenderProps;
import com.crediya.loan.sqs.sender.serializer.LoanCapacitySerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@RequiredArgsConstructor
public class SqsLoanCapacityPublisherAdapter implements LoanCapacityPublisherPort {

    private final OutboundPublisherPort<LoanCapacityRequested> delegate;

    public static SqsLoanCapacityPublisherAdapter of(SqsAsyncClient sqs, ObjectMapper mapper, TraceLoggerPort logger, SqsSenderProps properties) {
        var serializer = new LoanCapacitySerializer(mapper);
        var generic = new SqsPublisherAdapter<>(sqs, serializer, logger, properties);
        return new SqsLoanCapacityPublisherAdapter(generic);
    }

    @Override
    public Mono<Void> publish(LoanCapacityRequested event) {
        return delegate.publish(event);
    }
}
