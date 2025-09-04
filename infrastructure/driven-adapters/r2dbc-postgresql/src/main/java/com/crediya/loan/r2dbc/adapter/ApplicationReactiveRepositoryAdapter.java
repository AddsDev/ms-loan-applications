package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.r2dbc.common.DatabaseErrorMapper;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.ApplicationEntityMapper;
import com.crediya.loan.r2dbc.repository.ApplicationReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<LoanApplication, ApplicationEntity, UUID, ApplicationReactiveRepository> implements LoanRepository {
    private final TraceLoggerPort logger;
    private final ApplicationEntityMapper entityMapper;

    protected ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper, ApplicationEntityMapper entityMapper, TraceLoggerPort logger) {
        super(repository, mapper, entityMapper::toDomain);
        this.entityMapper = entityMapper;
        this.logger = logger;
    }

    @Override
    protected ApplicationEntity toData(LoanApplication entity) {
        return entityMapper.toEntity(entity);
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication domain) {
        logger.trace("Saving application id={}, document={}, createdAt={}", domain.id(), domain.identityDocument().value(), domain.createdAt());
        return repository.save(entityMapper.toEntity(domain))
                .map(entityMapper::toDomain)
                .onErrorMap(e -> DatabaseErrorMapper.mapUniqueViolation(e, () -> new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Application already exists")));
    }
}
