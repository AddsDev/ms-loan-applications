package com.crediya.loan.usecase.applyforloan;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.gateways.TransactionPort;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.gateways.LoanPolicyRepository;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplyForLoanUseCase {
    private final LoanRepository loanRepository;
    private final LoanPolicyRepository policyRepository;
    private final TransactionPort tx;
    private final TraceLoggerPort logger;

    public Mono<LoanApplication> execute(ApplyForLoanCommand command) {
        return policyRepository.loadAllPolicies()
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Loan policies not configured")))
                .flatMap(policies -> {
                    var app = LoanApplication.register(command, policies);
                    return policyRepository.loanTypeExists(app.loanType())
                            .flatMap(exists -> exists
                                    ?  Mono.just(app)
                                    : Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Loan type is not configured in catalog.")));
                })
                .flatMap(app -> tx.transactional(() -> loanRepository.save(app)))
                .doOnSubscribe(s -> logger.trace("ApplyForLoan start, doc={} email={}", maskDoc(command.document().value()), command.email()))
                .doOnSuccess( app -> logger.info("tx[ApplyForLoanUseCase] success id={} status={}", app.id(), app.status().name()))
                .doOnError(e -> logger.error("tx[ApplyForLoanUseCase] fail", e));
    }

    private String maskDoc(String value) {
        if (value == null || value.length() < 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }
}
