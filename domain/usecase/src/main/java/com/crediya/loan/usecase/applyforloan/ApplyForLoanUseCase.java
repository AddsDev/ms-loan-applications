package com.crediya.loan.usecase.applyforloan;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.EmailValidationPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.gateways.TransactionPort;
import com.crediya.loan.model.common.ownership.Authorities;
import com.crediya.loan.model.common.services.OwnershipValidatorService;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.gateways.LoanPolicyRepository;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class ApplyForLoanUseCase {
    private final LoanRepository loanRepository;
    private final EmailValidationPort emailValidationPort;
    private final LoanPolicyRepository policyRepository;
    private final TransactionPort tx;
    private final TraceLoggerPort logger;
    private final OwnershipValidatorService ownershipValidator;

    public Mono<LoanApplication> execute(ApplyForLoanCommand command) {
        return ownershipValidator.assertOwner(command, Set.of(Authorities.ROLE_ADMINISTRADOR))
                .then(Mono.defer(() ->
                        policyRepository.loadAllPolicies()
                                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Loan policies not configured")))
                                .flatMap(policies -> {
                                    var app = LoanApplication.register(command, policies);
                                    return policyRepository.loanTypeExists(app.loanType())
                                            .flatMap(exists -> Boolean.TRUE.equals(exists)
                                                    ? Mono.just(app)
                                                    : Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Loan type is not configured in catalog.")));
                                })
                                .flatMap(loan ->
                                        emailValidationPort.checkEmail(loan.email()).flatMap(response -> response.isRegistered()
                                                        ? Mono.just(
                                                        new LoanApplication(
                                                                loan.id(),
                                                                loan.email(),
                                                                response.name(),
                                                                loan.identityDocument(),
                                                                loan.amount(),
                                                                loan.term(),
                                                                loan.loanType(),
                                                                response.baseSalary(),
                                                                loan.status(),
                                                                loan.createdAt()
                                                        )
                                                )
                                                        : Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "The email address is not registered in the system")))
                                                .doOnSubscribe(s -> logger.trace("Email validation started"))
                                                .doOnSuccess(t -> logger.trace("Email validation result: {}", t))
                                                .doOnError(e -> logger.error("Email validation failed", e))
                                )
                                .flatMap(app -> tx.transactional(() -> loanRepository.save(app)))
                                .doOnSubscribe(s -> logger.trace("ApplyForLoan start, doc={} email={}", maskDoc(command.document().value()), command.email()))
                                .doOnSuccess(app -> logger.info("tx[ApplyForLoanUseCase] success id={} status={}", app.id(), app.status().name()))
                                .doOnError(e -> logger.error("tx[ApplyForLoanUseCase] fail", e))
                ));
    }

    private String maskDoc(String value) {
        if (value == null || value.length() < 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }
}
