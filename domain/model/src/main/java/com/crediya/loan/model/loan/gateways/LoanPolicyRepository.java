package com.crediya.loan.model.loan.gateways;

import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface LoanPolicyRepository {
    Mono<AmountRange> amountRangeFor(LoanType type);
    Mono<TermRange> termRangeFor(LoanType type);
    Mono<LoanPolicies> loadAllPolicies();
    Flux<LoanType> loadAllLoanTypes();
    Mono<Boolean> loanTypeExists(LoanType type);
}
