package com.crediya.loan.config;

import com.crediya.loan.loggeradapter.TraceLoggerFactory;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class LoggerConfiguration {

    private final TraceLoggerFactory traceLoggerFactory;

    /**
     * DomainLayer bean.
     * @return TraceLoggerPort.
     */
    @Bean
    @Primary
    public TraceLoggerPort domainLayerLogger() {
        return traceLoggerFactory.getLogger();
    }

    /**
     * EntryPointLogger bean.
     * @return TraceLoggerPort.
     */
    @Bean("entryPointLogger")
    public TraceLoggerPort entryPointLogger() {
        return traceLoggerFactory.getLogger();
    }
}
