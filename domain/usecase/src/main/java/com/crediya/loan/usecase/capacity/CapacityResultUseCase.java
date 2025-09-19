package com.crediya.loan.usecase.capacity;

import com.crediya.loan.model.capacity.LoanCapacityCalculatedEvent;
import com.crediya.loan.model.capacity.gateways.ApplicationStateRepository;
import com.crediya.loan.model.capacity.gateways.CapacityResultHandlerPort;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.gateways.TransactionPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CapacityResultUseCase  implements CapacityResultHandlerPort {
    private final ApplicationStateRepository stateRepository;
    private final TransactionPort tx;
    private final TraceLoggerPort logger;

    @Override
    public Mono<Void> handle(LoanCapacityCalculatedEvent response) {
        return Mono.defer(() -> {
            if (response == null) {
                return Mono.error(new ValidationException(ErrorCode.REQUIRED_FIELD, "Payload is null or invalid"));
            }

            logger.trace("sqsCapacityResultHandlerAdapter start loanId={} decision={}",
                    response.loanId(), response.decision());


            return tx.transactional(() ->
                            stateRepository.changeStatus(response.loanId(), response.decision())
                                    .flatMap(updated -> {
                                        if (Boolean.FALSE.equals(updated)) {
                                            logger.warn("sqsCapacityResultHandlerAdapter no update loanId={} expectedLock={}", response.loanId());
                                            return Mono.error(new ValidationException(ErrorCode.PERSISTENCE_ERROR, "Lock conflict"));
                                        }
                                        return Mono.empty();
                                    })
                    )
                    .doOnSuccess(v -> logger.info("sqsCapacityResultHandlerAdapter success loanId={} newStatus={}",
                            response.loanId(), response.decision()))
                    .doOnError(e -> logger.error("sqsCapacityResultHandlerAdapter fail loanId={}", response.loanId(), e));
        }).then();
    }
}
