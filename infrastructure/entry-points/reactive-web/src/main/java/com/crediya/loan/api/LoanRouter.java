package com.crediya.loan.api;

import com.crediya.loan.api.loan.handler.LoanHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class LoanRouter {

    @Bean
    public RouterFunction<ServerResponse> loanRoutes(LoanHandler handler) {
        return RouterFunctions.route()
                .path("/api/v1", builder ->
                        builder.POST("/solicitud", handler::registerLoan)
                )
                .build();
    }
}
