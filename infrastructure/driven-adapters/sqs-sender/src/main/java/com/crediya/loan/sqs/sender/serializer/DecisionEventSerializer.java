package com.crediya.loan.sqs.sender.serializer;

import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.sqs.sender.dto.DecisionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

@RequiredArgsConstructor
public class DecisionEventSerializer implements SqsMessageSerializer<DecisionEvent> {

    private final ObjectMapper mapper;

    @Override
    public SerializedMessage serialize(DecisionEvent event) throws JsonProcessingException {
        var dto = new DecisionRequest(
                event.eventName(),
                event.eventVersion(),
                event.loanId(),
                event.decision().name(),
                event.email() != null ? event.email().value() : null,
                event.reason(),
                event.createdAt().toString()
        );

        String body = mapper.writeValueAsString(dto);

        java.util.Map<String, MessageAttributeValue> attrs = new java.util.HashMap<>();
        attrs.put("eventName", attr("String", event.eventName()));
        attrs.put("eventVersion", attr("Number", String.valueOf(event.eventVersion())));
        attrs.put("contentType", attr("String", "application/json"));

        return new SerializedMessage(body, java.util.Map.copyOf(attrs));
    }

    private static MessageAttributeValue attr(String type, String value) {
        return MessageAttributeValue.builder()
                .dataType(type)
                .stringValue(value)
                .build();
    }
}
