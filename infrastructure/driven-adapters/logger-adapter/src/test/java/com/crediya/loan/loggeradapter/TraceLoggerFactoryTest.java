package com.crediya.loan.loggeradapter;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraceLoggerFactoryTest {
    private TraceLoggerFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TraceLoggerFactory();
    }

    @Test
    @DisplayName("Debe devolver un TraceLoggerAdapter cuando se llama con una clase")
    void shouldReturnLoggerWhenCalledWithClass() {
        TraceLoggerPort logger = factory.getLogger();

        assertNotNull(logger, "El logger no debería ser nulo");
        assertInstanceOf(TraceLoggerAdapter.class, logger, "Debe ser una instancia de TraceLoggerAdapter");
    }

}