package com.crediya.loan.loggeradapter;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import org.springframework.stereotype.Component;

@Component
public class TraceLoggerFactory {
    /*public TraceLoggerPort getLogger(Class<?> clazz) {
        return new TraceLoggerAdapter(clazz);
    }*/

    public TraceLoggerPort getLogger() {
        return new TraceLoggerAdapter();
    }
}
