package com.crediya.loan.sqs.sender.config;

import com.crediya.loan.model.capacity.gateways.CapacityResultHandlerPort;
import com.crediya.loan.model.capacity.gateways.LoanCapacityPublisherPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.decision.gateways.DecisionPublisherPort;
import com.crediya.loan.sqs.sender.adapter.SqsDecisionPublisherAdapter;
import com.crediya.loan.sqs.sender.adapter.SqsLoanCapacityPublisherAdapter;
import com.crediya.loan.sqs.sender.consumer.AutomaticQueueSqsConsumer;
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
            SQSSenderProperties properties
    ) {
        var props = SqsSenderProps.defaults(properties.requestQueueUrl());
        return SqsDecisionPublisherAdapter.of(sqs, mapper, logger, props);
    }

    @Bean
    public LoanCapacityPublisherPort loanCapacityPublisherPort(
            SqsAsyncClient sqs,
            ObjectMapper mapper,
            @Qualifier("entryPointLogger")
            TraceLoggerPort logger,
            SQSSenderProperties properties
    ) {
        var props = SqsSenderProps.defaults(properties.automaticQueueUrl());
        return SqsLoanCapacityPublisherAdapter.of(sqs, mapper, logger, props);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public AutomaticQueueSqsConsumer automaticQueueConsumer(
            software.amazon.awssdk.services.sqs.SqsAsyncClient sqs,
            com.fasterxml.jackson.databind.ObjectMapper mapper,
            com.crediya.loan.model.common.gateways.TraceLoggerPort logger,
            SQSSenderProperties properties,
            CapacityResultHandlerPort handler
    ) {
        return new AutomaticQueueSqsConsumer(sqs, mapper, logger, properties.resultQueueUrl(), handler);
    }
}
