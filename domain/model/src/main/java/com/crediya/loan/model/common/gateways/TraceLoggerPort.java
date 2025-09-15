package com.crediya.loan.model.common.gateways;

public interface TraceLoggerPort {
    /**
     * Logs a message at TRACE level.
     * @param message the message to log.
     */
    void trace(String message);

    /**
     * Logs a message at TRACE level.
     * @param message the message to log.
     * @param args the arguments to log.
     */
    void trace(String message, Object... args);

    /**
     * Logs a message at INFO level.
     * @param message the message to log.
     */
    void info(String message);

    /**
     * Logs a message at INFO level.
     * @param message the message to log.
     * @param args the arguments to log.
     */
    void info(String message, Object... args);

    /**
     * Logs a message at WARN level.
     * @param message the message to log.
     */
    void warn(String message);

    /**
     * Logs a message at WARN level.
     * @param message the message to log.
     * @param args the arguments to log.
     */
    void warn(String message, Object... args);

    /**
     * Logs a message at ERROR level.
     * @param message the message to log.
     */
    void error(String message);

    void error(String message, Object... args);

    /**
     * Logs a message at ERROR level.
     * @param message the message to log.
     * @param t the throwable to log.
     */
    void error(String message, Throwable t);

    /**
     * Checks if TRACE level is enabled.
     * @return true if TRACE level is enabled, false otherwise.
     */
    boolean isTraceEnabled();

    /**
     * Checks if DEBUG level is enabled.
     * @return true if DEBUG level is enabled, false otherwise.
     */
    boolean isDebugEnabled();
}
