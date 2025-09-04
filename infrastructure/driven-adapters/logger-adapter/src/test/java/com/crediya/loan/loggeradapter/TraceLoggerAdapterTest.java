package com.crediya.loan.loggeradapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceLoggerAdapterTest {
    @Mock
    private Logger mockLogger;

    @Test
    @DisplayName("El constructor con clase debe obtener el logger para esa clase especifica")
    void constructorWithClassShouldGetLoggerForSpecificClass() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);

            new TraceLoggerAdapter();

            mockedFactory.verify(() -> LoggerFactory.getLogger(TraceLoggerAdapter.class));
        }
    }

    @Test
    @DisplayName("El metodo trace(msg) debe delegar la llamada al logger interno")
    void traceShouldDelegateCall() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();
            String message = "Este es un mensaje de trace";

            adapter.trace(message);

            verify(mockLogger).trace(message);
        }
    }

    @Test
    @DisplayName("El metodo info(msg) debe delegar la llamada al logger interno")
    void infoShouldDelegateCall() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();
            String message = "Este es un mensaje de info";

            adapter.info(message);

            verify(mockLogger).info(message);
        }
    }

    @Test
    @DisplayName("El metodo error(msg, throwable) debe delegar la llamada al logger interno")
    void errorWithThrowableShouldDelegateCall() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();
            String message = "Este es un mensaje de error";
            Throwable exception = new RuntimeException("Error de prueba");

            adapter.error(message, exception);

            verify(mockLogger).error(message, exception);
        }
    }

    @Test
    @DisplayName("El metodo isTraceEnabled debe delegar y devolver el valor correcto")
    void isTraceEnabledShouldDelegateAndReturnCorrectValue() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            when(mockLogger.isTraceEnabled()).thenReturn(true);

            assertTrue(adapter.isTraceEnabled(), "Deberia devolver true");
        }
    }

    @Test
    @DisplayName("El metodo isDebugEnabled debe delegar y devolver el valor correcto")
    void isDebugEnabledShouldDelegateAndReturnCorrectValue() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            when(mockLogger.isDebugEnabled()).thenReturn(false);

            assertFalse(adapter.isDebugEnabled(), "Deberia devolver false");
        }
    }

    @Test
    @DisplayName("El constructor RequiredArgsConstructor debe usar el logger inyectado")
    void requiredArgsConstructorShouldUseInjectedLogger() {
        TraceLoggerAdapter adapter = new TraceLoggerAdapter(mockLogger);

        adapter.info("hello requiredArgs");

        verify(mockLogger).info("hello requiredArgs");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    @DisplayName("trace(msg, args...) debe delegar correctamente (varargs)")
    void traceWithVarargsShouldDelegate() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            adapter.trace("trace {} {}", 1, "two");

            verify(mockLogger).trace(eq("trace {} {}"), aryEq(new Object[]{1, "two"}));
        }
    }

    @Test
    @DisplayName("info(msg, args...) debe delegar correctamente (varargs)")
    void infoWithVarargsShouldDelegate() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            adapter.info("info {} {}", "A", 2);

            verify(mockLogger).info(eq("info {} {}"), aryEq(new Object[]{"A", 2}));
        }
    }

    @Test
    @DisplayName("warn(msg) debe delegar la llamada al logger")
    void warnShouldDelegateMessage() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            adapter.warn("simple warn");

            verify(mockLogger).warn("simple warn");
        }
    }

    @Test
    @DisplayName("warn(msg, args...) debe delegar correctamente (varargs)")
    void warnWithVarargsShouldDelegate() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            adapter.warn("warn {} - {}", "X", 99);

            verify(mockLogger).warn(eq("warn {} - {}"), aryEq(new Object[]{"X", 99}));
        }
    }

    @Test
    @DisplayName("error(msg) debe delegar la llamada al logger")
    void errorShouldDelegateMessageOnly() {
        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(mockLogger);
            TraceLoggerAdapter adapter = new TraceLoggerAdapter();

            adapter.error("just error message");

            verify(mockLogger).error("just error message");
        }
    }
}