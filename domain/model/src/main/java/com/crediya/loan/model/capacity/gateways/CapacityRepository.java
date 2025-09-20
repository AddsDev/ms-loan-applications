package com.crediya.loan.model.capacity.gateways;

import com.crediya.loan.model.capacity.policy.Policies;
import com.crediya.loan.model.capacity.valueobjects.ActiveLoan;
import com.crediya.loan.model.loan.ApplicationStatus;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface CapacityRepository {
    record ApplicantProjection(String email, BigDecimal baseSalary) {}
    record NewLoanProjection(long amount, double interestRate, int termsInMonths, boolean automaticValidation) {}
    record ActiveLoanProjection(long balance, double interestRate, int termsInMonths) {}

    Mono<ApplicantProjection> findApplicantByLoanId(String loanId);
    Mono<NewLoanProjection>   findNewLoanByLoanId(String loanId);
    //Flux
    Mono<List<ActiveLoan>> findActiveLoansByApplicantEmail(String email);

    Mono<Policies> loadPolicies();
    Mono<Void> updateLoanStatus(String loanId, ApplicationStatus decision, long lockVersion);
}
