package com.crediya.loan.sqs.sender.serializer;

import com.crediya.loan.model.report.ReportEvent;
import com.crediya.loan.sqs.sender.dto.ReportRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

@RequiredArgsConstructor
public class ReportEventSerializer implements SqsMessageSerializer<ReportEvent> {

    private final ObjectMapper mapper;

    @Override
    public SerializedMessage serialize(ReportEvent event) throws JsonProcessingException {
        var dto = new ReportRequest(
                event.amount(),
                event.loanId(),
                event.approvedAt()
        );

        return new SerializedMessage(mapper.writeValueAsString(dto), Map.of(
                "eventName", attr("String", event.eventName()),
                "eventVersion", attr("Number", String.valueOf(event.eventVersion())),
                "contentType", attr("String", "application/json")
        ));
    }

    private static MessageAttributeValue attr(String type, String value) {
        return MessageAttributeValue.builder()
                .dataType(type)
                .stringValue(value)
                .build();
    }
}
