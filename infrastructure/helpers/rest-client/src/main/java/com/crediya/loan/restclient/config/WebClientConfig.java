package com.crediya.loan.restclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableConfigurationProperties(RestClientProperties.class)
@RequiredArgsConstructor
public class WebClientConfig {
    @Bean
    ExchangeFilterFunction propagateBearerToken() {
        return (request, next) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(AbstractAuthenticationToken.class)
                .map(auth -> {
                    String token = (auth instanceof JwtAuthenticationToken jwtAuth)
                            ? jwtAuth.getToken().getTokenValue()
                            : null;
                    return (token == null) ? request
                            : ClientRequest.from(request).headers(h -> h.setBearerAuth(token)).build();
                })
                .defaultIfEmpty(request)
                .flatMap(next::exchange);
    }

    @Bean("emailValidatorWebClient")
    public WebClient emailValidatorWebClient(RestClientProperties properties, ExchangeFilterFunction propagateBearerToken) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeout())
                .responseTimeout(Duration.ofMillis(properties.readTimeout()))
                .doOnConnected(con -> con.addHandlerLast(new ReadTimeoutHandler(properties.readTimeout(), TimeUnit.MILLISECONDS)));
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(256 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .filter(propagateBearerToken)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
