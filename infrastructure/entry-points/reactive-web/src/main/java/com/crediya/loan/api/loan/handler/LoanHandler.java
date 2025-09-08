package com.crediya.loan.api.loan.handler;

import com.crediya.loan.api.config.RequestValidatorConfig;
import com.crediya.loan.api.loan.dto.ApplyForLoanRequest;
import com.crediya.loan.api.loan.mapper.LoanMapper;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.usecase.applyforloan.ApplyForLoanUseCase;
import com.crediya.loan.usecase.listapplications.ListApplicationUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class LoanHandler {
    private final ApplyForLoanUseCase applyForLoanUseCase;
    private final ListApplicationUseCase listApplicationUseCase;
    private final RequestValidatorConfig validator;
    private final LoanMapper loanMapper;
    private final TraceLoggerPort logger;


    public LoanHandler(ApplyForLoanUseCase applyForLoanUseCase, ListApplicationUseCase listApplicationUseCase,@Qualifier("entryPointLogger") TraceLoggerPort logger, LoanMapper loanMapper, RequestValidatorConfig validator) {
        this.applyForLoanUseCase = applyForLoanUseCase;
        this.listApplicationUseCase = listApplicationUseCase;
        this.logger = logger;
        this.loanMapper = loanMapper;
        this.validator = validator;
    }

    @PreAuthorize( "hasAnyAuthority('SCOPE_loan:write','ROLE_CLIENTE','ROLE_ADMINISTRADOR')")
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

    @PreAuthorize( "hasAnyAuthority('SCOPE_loan:read','ROLE_ASESOR','ROLE_ADMINISTRADOR')")
    public Mono<ServerResponse> listForAdvisor(ServerRequest serverRequest) {
        var statuses = serverRequest.queryParam("status")
                .map(s -> Arrays.stream(s.split(",")).map(String::trim)
                        .filter(v -> !v.isBlank())
                        .map(String::toUpperCase)
                        .map(ApplicationStatus::valueOf)
                        .toList())
                .orElse(List.of(ApplicationStatus.PENDING, ApplicationStatus.REJECTED, ApplicationStatus.MANUAL_REVIEW));

        int page = parseInt(serverRequest.queryParam("page").orElse("0"));
        int size = parseInt(serverRequest.queryParam("size").orElse("20"), 20, 1, 200);
        String sort = serverRequest.queryParam("sort").orElse("createdAt,desc");

        var q = new ListApplicationsQueryCommand(
                page, size, sort,
                statuses,
                serverRequest.queryParam("email").orElse(null),
                serverRequest.queryParam("document").orElse(null),
                serverRequest.queryParam("loanType").orElse(null)
        );
        return listApplicationUseCase.execute(q)
                .map(loanMapper::toResponse)
                .flatMap( body -> ServerResponse.ok().bodyValue(body))
                .doOnError(e -> logger.error("Error listing for advisor", e));
    }

    private int parseInt(String raw) { return parseInt(raw, 0, 0, Integer.MAX_VALUE); }
    private int parseInt(String raw, int def, int min, int max) {
        try {
            int v = Integer.parseInt(raw);
            return Math.max(min, Math.min(max, v));
        } catch (Exception e) { return def; }
    }
}
