package com.crediya.loan.usecase.capacity;

import com.crediya.loan.model.capacity.LoanCapacityRequested;
import com.crediya.loan.model.capacity.gateways.CapacityRepository;
import com.crediya.loan.model.capacity.gateways.LoanCapacityPublisherPort;
import com.crediya.loan.model.capacity.valueobjects.ActiveLoan;
import com.crediya.loan.model.capacity.valueobjects.Applicant;
import com.crediya.loan.model.capacity.valueobjects.NewLoan;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
public class RequestLoanCapacityUseCase {
    private final CapacityRepository readPort;
    private final LoanCapacityPublisherPort publisher;
    private final TraceLoggerPort logger;

    public Mono<LoanCapacityRequested> execute(String loanId) {
        if (loanId == null || loanId.isBlank()) {
            return Mono.error(new ValidationException(ErrorCode.REQUIRED_FIELD, "loanId is required"));
        }

        logger.trace("useCase[RequestLoanCapacity] start loanId={}", loanId);

        return Mono.zip(
                        readPort.findApplicantByLoanId(loanId),
                        readPort.findNewLoanByLoanId(loanId)
                )
                .flatMap(tuple -> {
                    var applicant = tuple.getT1();
                    var newLoan = tuple.getT2();

                    if (!newLoan.automaticValidation()) {
                        logger.info("useCase[RequestLoanCapacity] skip [automatic-validation=false] loanId={}", loanId);
                        return Mono.empty();
                    }

                    return readPort.findActiveLoansByApplicantEmail(applicant.email())
                            .flatMap(activeLoans -> readPort.loadPolicies()
                                    .map(policies -> LoanCapacityRequested.of(
                                            loanId,
                                            new Applicant(applicant.email(), applicant.baseSalary()),
                                            new NewLoan(newLoan.amount(), newLoan.interestRate(), newLoan.termsInMonths()),
                                            activeLoans.stream()
                                                    .map(a -> new ActiveLoan(a.balance(), a.monthlyRate(), a.termMonths()))
                                                    .toList(),
                                            policies,
                                            OffsetDateTime.now()
                                    ))
                            );
                })
                .flatMap(event -> publisher.publish(event).thenReturn(event))
                .doOnSuccess(ev -> {
                    if (ev != null) {
                        logger.info("useCase[RequestLoanCapacity] published loanId={} version={}", ev.loanId(), ev.eventVersion());
                    }
                })
                .doOnSubscribe(s -> logger.trace("tx[RequestLoanCapacityUseCase] start"))
                .doOnError(e -> logger.error("tx[RequestLoanCapacityUseCase] fail", e));
    }
}
