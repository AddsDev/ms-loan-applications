package com.crediya.loan.sqs.sender.adapter;

import com.crediya.loan.model.common.gateways.OutboundPublisherPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.report.ReportEvent;
import com.crediya.loan.model.report.gateways.ReportPublisherPort;
import com.crediya.loan.sqs.sender.config.SqsSenderProps;
import com.crediya.loan.sqs.sender.serializer.ReportEventSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@RequiredArgsConstructor
public class SqsReportPublisherAdapter implements ReportPublisherPort {
    private final OutboundPublisherPort<ReportEvent> delegate;

    public static SqsReportPublisherAdapter of(SqsAsyncClient sqs, ObjectMapper mapper, TraceLoggerPort logger, SqsSenderProps properties) {
        var generic = new SqsPublisherAdapter<>(sqs, new ReportEventSerializer(mapper), logger, properties);
        return new SqsReportPublisherAdapter(generic);
    }

    @Override
    public Mono<Void> publish(ReportEvent event) {
        return delegate.publish(event);
    }
}
