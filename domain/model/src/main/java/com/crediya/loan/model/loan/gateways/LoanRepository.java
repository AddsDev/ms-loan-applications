package com.crediya.loan.model.loan.gateways;

import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.loan.ApplicationSummary;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.model.loan.valueobjects.SortSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanRepository {
    Mono<LoanApplication> save(LoanApplication loanApplication);
    Mono<DecisionEvent> changeStatusIfPending(DecisionEvent decisionEvent);
    Flux<ApplicationSummary> findForAdvisor(ListApplicationsQueryCommand cmd, SortSpec sort);
    Mono<Long> countForAdvisor(ListApplicationsQueryCommand cmd);
}
