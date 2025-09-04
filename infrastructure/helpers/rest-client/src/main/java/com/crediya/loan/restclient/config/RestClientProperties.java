package com.crediya.loan.restclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.email-validator")
public record RestClientProperties(
        String baseUrl,
        Integer connectTimeout,
        Integer readTimeout,
        Integer defaultRetryAttempts
) {
}
