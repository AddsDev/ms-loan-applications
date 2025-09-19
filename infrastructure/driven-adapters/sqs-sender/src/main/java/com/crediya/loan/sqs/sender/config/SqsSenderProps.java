package com.crediya.loan.sqs.sender.config;

import java.time.Duration;
import java.util.function.Function;

public record SqsSenderProps(
        String queueUrl,
        Duration timeout,
        int maxAttempts,
        Duration backoff,
        double jitter,
        Function<Object,String> deduplicationKeyFn
) {

    public static SqsSenderProps defaults(String queueUrl) {
        return new SqsSenderProps(
                queueUrl,
                Duration.ofSeconds(10),
                3,
                Duration.ofSeconds(1),
                0.2,
                o -> null
        );
    }
}
