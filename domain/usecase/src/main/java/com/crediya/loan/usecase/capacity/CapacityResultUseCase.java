package com.crediya.loan.usecase.capacity;

import com.crediya.loan.model.capacity.LoanCapacityCalculatedEvent;
import com.crediya.loan.model.capacity.gateways.ApplicationStateRepository;
import com.crediya.loan.model.capacity.gateways.CapacityResultHandlerPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.report.gateways.ReportPublisherPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CapacityResultUseCase implements CapacityResultHandlerPort {
    private final LoanRepository loanRepository;
    private final ApplicationStateRepository stateRepository;
    private final ReportPublisherPort reportPublisher;
    private final TraceLoggerPort logger;

    @Override
    public Mono<Void> handle(LoanCapacityCalculatedEvent response) {
        return Mono.defer(() -> {
            logger.trace("sqsCapacityResultHandlerAdapter start loanId={} decision={}",
                    response.loanId(), response.decision());
            
            return stateRepository.changeStatus(response.loanId(), response.decision())
                    .flatMap(e ->
                            loanRepository.findForReportEvent(response.loanId()).flatMap(
                                    event -> reportPublisher.publish(event).thenReturn(Mono.empty())
                            )
                    )
                    .doOnSuccess(v -> logger.info("sqsCapacityResultHandlerAdapter success loanId={} newStatus={}",
                            response.loanId(), response.decision()))
                    .doOnError(e -> logger.error("sqsCapacityResultHandlerAdapter fail loanId={}", response.loanId(), e));
        }).then();
    }
}
