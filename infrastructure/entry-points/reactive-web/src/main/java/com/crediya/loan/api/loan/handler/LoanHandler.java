package com.crediya.loan.api.loan.handler;

import com.crediya.loan.api.config.RequestValidatorConfig;
import com.crediya.loan.api.loan.dto.ApplyForLoanRequest;
import com.crediya.loan.api.loan.mapper.LoanMapper;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.usecase.applyforloan.ApplyForLoanUseCase;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class LoanHandler {
    private final ApplyForLoanUseCase applyForLoanUseCase;
    private final RequestValidatorConfig validator;
    private final LoanMapper loanMapper;
    private final TraceLoggerPort logger;


    public LoanHandler(ApplyForLoanUseCase applyForLoanUseCase, @Qualifier("entryPointLogger") TraceLoggerPort logger, LoanMapper loanMapper, RequestValidatorConfig validator) {
        this.applyForLoanUseCase = applyForLoanUseCase;
        this.logger = logger;
        this.loanMapper = loanMapper;
        this.validator = validator;
    }

    public Mono<ServerResponse> registerLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplyForLoanRequest.class)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("Request body is empty")))
                .flatMap(validator::validate)
                .map(loanMapper::toCommand)
                .flatMap(applyForLoanUseCase::execute)
                .map(loanMapper::toResponse)
                .flatMap( response -> {
                    logger.trace("Loan application successfully registered: {}", response);
                    return ServerResponse.ok().bodyValue(response);
                })
                .doOnError(e -> logger.error("Error registering apply for loan", e));
    }
}
