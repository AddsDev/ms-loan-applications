package com.crediya.loan.sqs.sender.config;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.decision.gateways.DecisionPublisherPort;
import com.crediya.loan.sqs.sender.adapter.SqsDecisionPublisherAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {
    @Bean
    public DecisionPublisherPort decisionPublisherPort(
            SqsAsyncClient sqs,
            ObjectMapper mapper,
            @Qualifier("entryPointLogger")
            TraceLoggerPort logger,
            SQSSenderProperties queueUrl
    ) {
        return new SqsDecisionPublisherAdapter(sqs, mapper, logger, queueUrl);
    }
}
