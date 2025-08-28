package com.crediya.loan.model.common.gateways;

import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.LoanType;


public interface CatalogResolverPort {
    ApplicationStatus toStatus(String statusId); //Candidato para Mono<T>
    String toStatusId(ApplicationStatus status);

    LoanType toLoanType(String loanTypeId); //Candidato para Mono<T>
    String toLoanTypeId(LoanType type);
}
