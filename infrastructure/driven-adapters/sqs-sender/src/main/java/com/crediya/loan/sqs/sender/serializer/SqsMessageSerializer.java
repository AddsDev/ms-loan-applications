package com.crediya.loan.sqs.sender.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

public interface SqsMessageSerializer<T> {
    SerializedMessage serialize(T event) throws JsonProcessingException;
    record SerializedMessage(String body, Map<String, MessageAttributeValue> attributes) { }

}
