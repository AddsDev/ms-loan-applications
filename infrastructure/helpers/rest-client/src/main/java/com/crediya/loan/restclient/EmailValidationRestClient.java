package com.crediya.loan.restclient;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ExternalServiceException;
import com.crediya.loan.model.common.gateways.EmailValidationPort;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.validation.EmailValidationResult;
import com.crediya.loan.restclient.config.RestClientProperties;
import com.crediya.loan.restclient.dto.EmailValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class EmailValidationRestClient implements EmailValidationPort {
    private final WebClient client;
    private final RestClientProperties properties;

    @Override
    public Mono<EmailValidationResult> checkEmail(Email email) {
        return client.get()
                .uri(builder -> buildUri(builder, email.value()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .defaultIfEmpty(resp.statusCode().toString())
                        .map(msg -> new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_TIMEOUT, "The email validator service did not respond in time. Please try again later.")))
                .bodyToMono(EmailValidationResponse.class)
                .timeout(Duration.ofMillis(properties.readTimeout()))
                .retryWhen(Retry.backoff(properties.defaultRetryAttempts(), Duration.ofMillis(200))
                        .filter(this::isTransient))
                .map(res -> new EmailValidationResult(new Email(res.email()), res.name(), res.baseSalary(), res.isRegistered()))
                .onErrorResume(e -> Mono.error(new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR, "The provided email does not match the user's emails.")));
    }


    private boolean isTransient(Throwable t) {
        return t instanceof IOException || t instanceof TimeoutException;
    }

    private URI buildUri(UriBuilder builder, String email) {
        return builder.path("/api/v1/usuarios/validador")
                .queryParam("email", email)
                .build();
    }
}
