package com.crediya.loan.usecase.listapplications;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.pagers.PageResult;
import com.crediya.loan.model.loan.ApplicationSummary;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.model.loan.valueobjects.SortSpec;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListApplicationUseCase {
    private final LoanRepository loanRepository;
    private final TraceLoggerPort logger;

    public Mono<PageResult<ApplicationSummary>> execute(ListApplicationsQueryCommand cmd) {
        logger.trace("ListApplications start page={} size={} statuses={} email={} doc={} type={}",
                cmd.page(), cmd.size(), cmd.statuses(), cmd.email(), cmd.document(), cmd.loanTypeCode());
        //Sanitizar for maliciosos inputs
        var sort = SortSpec.from(cmd.sort());
        logger.trace("ListApplications query started");
        return loanRepository.findForAdvisor(cmd, sort)
                .collectList()
                .zipWith(loanRepository.countForAdvisor(cmd))
                .map(tuple -> PageResult.of(tuple.getT1(), cmd.page(), cmd.size(), tuple.getT2()))
                .doOnSuccess(s -> logger.trace("ListApplications query success total={}", s.totalItems()))
                .doOnError(e -> logger.error("ListApplications query fail", e));
    }

}
