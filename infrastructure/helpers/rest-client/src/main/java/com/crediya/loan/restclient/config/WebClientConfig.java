package com.crediya.loan.restclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableConfigurationProperties(RestClientProperties.class)
public class WebClientConfig {
    @Bean("emailValidatorWebClient")
    public WebClient emailValidatorWebClient(RestClientProperties properties) {
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
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .filter(ExchangeFilterFunctions.statusError(HttpStatusCode::isError,
                        clientResponse -> clientResponse.createException().block()
                ))
                .build();
    }
}
