package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.gateways.LoanPolicyRepository;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import com.crediya.loan.r2dbc.mapper.LoanPoliciesMapper;
import com.crediya.loan.r2dbc.mapper.LoanTypeEntityMapper;
import com.crediya.loan.r2dbc.repository.LoanTypeReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LoanPolicyRepositoryAdapter implements LoanPolicyRepository {

    private final LoanTypeReactiveRepository loanTypeReactiveRepository;
    private final LoanTypeEntityMapper entityMapper;
    private final TraceLoggerPort logger;

    private Example<LoanTypeEntity> byCode(String code) {
        LoanTypeEntity probe = LoanTypeEntity.builder().code(code).build();
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("minAmount", "maxAmount", "interestRate", "automaticValidation",
                        "createdAt", "updatedAt", "lockVersion", "loanTypeId", "name");
        return Example.of(probe, matcher);
    }

    @Override
    public Mono<AmountRange> amountRangeFor(LoanType type) {
        return loanTypeReactiveRepository.findOne(byCode(type.name()))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Loan type not found: " + type)))
                .map(entityMapper::toAmountRange)
                .doOnSuccess(t -> logger.trace("amountRangeFor type={} min={} max={}", type, t.minAmount(), t.maxAmount()));
    }

    @Override
    public Mono<TermRange> termRangeFor(LoanType type) {
        return loanTypeReactiveRepository.findOne(byCode(type.name()))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Loan type not found: " + type)))
                .map(entityMapper::toTermRange)
                .doOnSuccess(t -> logger.trace("termRangeFor type={} min={} max={}", type, t.minTerm(), t.maxTerm()));
    }

    @Override
    public Mono<LoanPolicies> loadAllPolicies() {
        return loanTypeReactiveRepository.findAll()
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "No loan types configured"));
                    }
                    Map<LoanType, TermRange> terms = new EnumMap<>(LoanType.class);
                    Map<LoanType, AmountRange> amounts = new EnumMap<>(LoanType.class);
                    for (LoanTypeEntity e : list) {
                        LoanType t = entityMapper.toEnum(e);
                        terms.put(t, entityMapper.toTermRange(e));
                        amounts.put(t, entityMapper.toAmountRange(e));
                    }
                    return Mono.just(new LoanPolicies(amounts, terms));
                })
                .doOnSuccess(p -> logger.trace("Loaded policies: amountTypes={} termTypes={}",
                        p.getAmountRange(LoanType.CONSUMER) != null ? "yes" : "no",
                        p.getTermInMonthsRange(LoanType.CONSUMER) != null ? "yes" : "no"));
    }

    @Override
    public Flux<LoanType> loadAllLoanTypes() {
        return loanTypeReactiveRepository.findAll()
                .map(entityMapper::toEnum)
                .doOnNext(t -> logger.trace("catalog loan type available: type={} exists={}", t, true));
    }

    @Override
    public Mono<Boolean> loanTypeExists(LoanType type) {
        return loanTypeReactiveRepository.findOne(byCode(type.name()))
                .hasElement()
                .doOnNext(t -> logger.trace("loanTypeExists type={} exists={}", type, t));
    }
}
