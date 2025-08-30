package com.crediya.loan.restclient;

import com.crediya.loan.model.common.gateways.EmailValidationPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.validation.EmailValidationResult;
import com.crediya.loan.restclient.config.RestClientProperties;
import com.crediya.loan.restclient.dto.EmailValidationResponse;
import com.crediya.loan.restclient.exception.RestClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailValidationRestClient implements EmailValidationPort {
    private final WebClient client;
    private final RestClientProperties properties;
    private final TraceLoggerPort logger;

    @Override
    public Mono<EmailValidationResult> checkEmail(Email email) {
        return client.get()
                .uri(builder -> buildUri(builder, email.value()))
                .retrieve()
                .bodyToMono(EmailValidationResponse.class)
                .timeout(Duration.ofMillis(properties.connectTimeout()))
                .retry(properties.defaultRetryAttempts())
                .map(res -> new EmailValidationResult(new Email(res.email()), res.isRegistered()))
                .onErrorResume(e -> Mono.error(new RestClientException( "Fail consulting email validator",e)));
    }

    private URI buildUri(UriBuilder builder, String email) {
        return builder.path("/api/v1/usuarios/validador")
                .queryParam("email", email)
                .build();
    }
}
