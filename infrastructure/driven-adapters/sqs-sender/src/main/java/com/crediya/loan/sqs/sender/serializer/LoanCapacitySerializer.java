package com.crediya.loan.sqs.sender.serializer;

import com.crediya.loan.model.capacity.LoanCapacityRequested;
import com.crediya.loan.sqs.sender.dto.LoanCapacityRequestedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LoanCapacitySerializer implements SqsMessageSerializer<LoanCapacityRequested>{
    private final ObjectMapper mapper;

    @Override
    public SerializedMessage serialize(LoanCapacityRequested event) throws JsonProcessingException {
        var payload = new LoanCapacityRequestedPayload(
                event.eventName(),
                event.eventVersion(),
                event.loanId(),
                event.lockVersion(),
                new LoanCapacityRequestedPayload.Applicant(event.applicant().email(), event.applicant().baseSalary()),
                new LoanCapacityRequestedPayload.NewLoan(event.newLoan().amount(), event.newLoan().interestRate(), event.newLoan().termsInMonths()),
                event.activeLoans().stream()
                        .map(a -> new LoanCapacityRequestedPayload.ActiveLoan(a.balance(), a.monthlyRate(), a.termMonths()))
                        .toList(),
                new LoanCapacityRequestedPayload.Policies(event.policies().maximumIncomePct(), event.policies().salaryManualReviewMultiplier()),
                event.createdAt().toString()
        );

        String body = mapper.writeValueAsString(payload);

        Map<String, MessageAttributeValue> attrs = new HashMap<>();
        attrs.put("eventName", MessageAttributeValue.builder().dataType("String").stringValue(event.eventName()).build());
        attrs.put("eventVersion", MessageAttributeValue.builder().dataType("Number").stringValue(String.valueOf(event.eventVersion())).build());
        attrs.put("contentType", MessageAttributeValue.builder().dataType("String").stringValue("application/json").build());

        return new SerializedMessage(body, attrs);
    }
}
