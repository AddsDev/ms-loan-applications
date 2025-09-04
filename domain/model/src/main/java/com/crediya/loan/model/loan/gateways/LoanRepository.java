package com.crediya.loan.model.loan.gateways;

import com.crediya.loan.model.loan.LoanApplication;
import reactor.core.publisher.Mono;

public interface LoanRepository {
    Mono<LoanApplication> save(LoanApplication loanApplication);
}
