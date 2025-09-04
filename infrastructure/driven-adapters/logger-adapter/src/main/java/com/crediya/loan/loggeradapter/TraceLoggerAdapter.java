package com.crediya.loan.loggeradapter;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The TraceLoggerAdapter class is an implementation of the TraceLoggerPort interface.
 * This adapter is designed to provide logging functionalities for various log levels
 * such as TRACE, INFO, WARN, and ERROR. It uses the underlying Logger to log messages.
 */

@Component
@RequiredArgsConstructor
public class TraceLoggerAdapter implements TraceLoggerPort {

    private final Logger logger;

    public TraceLoggerAdapter() {
        this.logger = LoggerFactory.getLogger(TraceLoggerAdapter.class);
    }

    @Override
    public void trace(String message) {
        this.logger.trace(message);
    }

    @Override
    public void trace(String message, Object... args) {
        this.logger.trace(message, args);
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void info(String message, Object... args) {
        this.logger.info(message, args);
    }

    @Override
    public void warn(String message) {
        this.logger.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        this.logger.warn(message, args);
    }

    @Override
    public void error(String message) {
        this.logger.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
}
