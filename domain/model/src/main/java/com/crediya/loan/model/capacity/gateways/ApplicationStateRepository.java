package com.crediya.loan.model.capacity.gateways;

import com.crediya.loan.model.loan.ApplicationStatus;
import reactor.core.publisher.Mono;

public interface ApplicationStateRepository {
    Mono<Boolean> changeStatus(String loanId, ApplicationStatus newStatus);
}
