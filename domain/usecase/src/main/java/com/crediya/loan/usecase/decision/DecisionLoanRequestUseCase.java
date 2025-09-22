package com.crediya.loan.usecase.decision;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.ownership.Authorities;
import com.crediya.loan.model.common.services.OwnershipValidatorService;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.gateways.DecisionPublisherPort;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.report.gateways.ReportPublisherPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class DecisionLoanRequestUseCase {
    private final LoanRepository loanRepository;
    private final DecisionPublisherPort decisionPublisher;
    private final ReportPublisherPort reportPublisher;
    private final TraceLoggerPort logger;
    private final OwnershipValidatorService ownershipValidator;


    public Mono<DecisionEvent> execute(DecisionEventCommand command) {
        return Mono.defer(() -> {
            logger.trace("useCase=DecisionLoanRequest start loanId={} decision={}",
                    command.loanId(), command.decision());
            return ownershipValidator.assertOwner(command, Set.of(Authorities.ROLE_ADMINISTRADOR))
                    .then(Mono.fromSupplier(() -> DecisionEvent.register(command)))
                    .flatMap(event ->
                            loanRepository.changeStatusIfPending(event)
                                    .switchIfEmpty(Mono.error(new ValidationException(ErrorCode.BUSINESS_RULE_VIOLATION,
                                            "Only PENDING can be changed to APPROVED or REJECTED.")))
                                    .flatMap(response ->
                                            decisionPublisher.publish(response)
                                                    .then()
                                                    .onErrorResume(e -> Mono.empty())
                                    )
                                    .thenReturn(event)
                    )
                    .flatMap(decision ->
                            loanRepository.findForReportEvent(decision.loanId()).flatMap(
                                    response -> reportPublisher.publish(response).thenReturn(decision)
                            ))
                    .doOnSuccess(de -> logger.warn("useCase=DecisionLoanRequest success loanId={} decision={}", de.loanId(), de.decision().name()))
                    .doOnSubscribe(subscription -> logger.trace("useCase=DecisionLoanRequestUseCase start"))
                    .doOnError(e -> logger.error("useCase=DecisionLoanRequestUseCase fail loanId={}", command.loanId(), e));
        });
    }
}
