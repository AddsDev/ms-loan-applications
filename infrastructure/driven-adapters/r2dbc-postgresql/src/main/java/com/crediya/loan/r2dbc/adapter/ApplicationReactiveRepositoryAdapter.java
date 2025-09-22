package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.capacity.gateways.ApplicationStateRepository;
import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.ApplicationSummary;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.model.loan.valueobjects.SortSpec;
import com.crediya.loan.model.report.ReportEvent;
import com.crediya.loan.r2dbc.common.DatabaseErrorMapper;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.ApplicationEntityMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationSummaryMapper;
import com.crediya.loan.r2dbc.query.ApplicationQuerySpec;
import com.crediya.loan.r2dbc.query.DefaultApplicationQuerySpec;
import com.crediya.loan.r2dbc.query.SqlFragments;
import com.crediya.loan.r2dbc.repository.ApplicationReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<LoanApplication, ApplicationEntity, UUID, ApplicationReactiveRepository> implements LoanRepository, ApplicationStateRepository {
    private final DatabaseClient db;
    private final TraceLoggerPort logger;
    private final ApplicationEntityMapper entityMapper;
    private final ApplicationSummaryMapper summaryMapper;
    private final ReactiveTransaction tx;

    protected ApplicationReactiveRepositoryAdapter(DatabaseClient db, ApplicationReactiveRepository repository, ObjectMapper mapper, ApplicationEntityMapper entityMapper, ApplicationSummaryMapper summaryMapper, TraceLoggerPort logger, ReactiveTransaction tx) {
        super(repository, mapper, entityMapper::toDomain);
        this.db = db;
        this.entityMapper = entityMapper;
        this.summaryMapper = summaryMapper;
        this.logger = logger;
        this.tx = tx;
    }

    @Override
    protected ApplicationEntity toData(LoanApplication entity) {
        return entityMapper.toEntity(entity);
    }

    @Override
    public Mono<Boolean> changeStatus(String loanId, ApplicationStatus newStatus) {
        return repository.findById(UUID.fromString(loanId))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Application not found")))
                .map(e -> entityMapper.update(e, newStatus))
                .flatMap(entity -> {
                    logger.trace("repo=Application update status id={}", entity.getApplicationId());
                    return tx.transactional(() -> repository.save(entity));
                })
                .map(e -> true);
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication domain) {
        logger.trace("Saving application id={}, document={}, createdAt={}", domain.id(), domain.identityDocument().value(), domain.createdAt());
        return tx.transactional(() -> repository.save(entityMapper.toEntity(domain)))
                .map(entityMapper::toDomain)
                .doOnSuccess(d -> logger.info("repo=Application save ok id={}", d.id()))
                .doOnError(e -> logger.error("repo=Application save fail id={}", domain.id(), e))
                .onErrorMap(e -> DatabaseErrorMapper.mapUniqueViolation(e, () -> new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Application already exists")))
                .doOnSuccess(app -> logger.info("tx[ApplicationReactiveRepositoryAdapter] success id={} status={}", app.id(), app.status().name()))
                .doOnError(e -> logger.error("tx[ApplicationReactiveRepositoryAdapter] fail", e));
    }

    @Override
    public Mono<DecisionEvent> changeStatusIfPending(DecisionEvent decisionEvent) {
        logger.trace("Update application for decision id={}, status={}, createdAt={}", decisionEvent.loanId(), decisionEvent.decision(), decisionEvent.createdAt());
        return repository.findById(UUID.fromString(decisionEvent.loanId()))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Application not found")))
                .map(entityMapper::toDomain)
                .flatMap(app -> List.of(ApplicationStatus.PENDING, ApplicationStatus.REVIEW_MANUAL).contains(app.status()) ? Mono.just(app) : Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Application status is not PENDING")))
                .map(app -> entityMapper.toEntity(app, decisionEvent))
                .flatMap(entity -> {
                    logger.trace("repo=Application update status id={}", entity.getApplicationId());
                    return tx.transactional(() -> repository.save(entity))
                            .doOnSuccess(de -> logger.warn("repository=ApplicationReactiveRepositoryAdapter success loanId={} decision={}", decisionEvent.loanId(), decisionEvent.decision().name()))
                            .doOnSubscribe(subscription -> logger.trace("tx[ApplicationReactiveRepositoryAdapter] start"))
                            .doOnError(e -> logger.error("tx[ApplicationReactiveRepositoryAdapter] fail loanId={}", decisionEvent.loanId(), e));
                })
                .map(entityMapper::toDomainDecision)
                .onErrorMap(e -> DatabaseErrorMapper.mapUniqueViolation(e, () -> new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "Application not exists")));
    }

    @Override
    public Mono<ReportEvent> findForReportEvent(String loanId) {
        logger.trace("Update application for report id={}", loanId);
        return repository.findById(UUID.fromString(loanId))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Application not found")))
                .map(e -> ReportEvent.register(e.getAmount(), e.getApplicationId().toString(), e.getUpdatedAt().toString()));
    }

    @Override
    public Flux<ApplicationSummary> findForAdvisor(ListApplicationsQueryCommand cmd, SortSpec sort) {
        ApplicationQuerySpec spc = DefaultApplicationQuerySpec.from(cmd, sort);
        var sql = SqlFragments.queryFilterSql(spc);
        var exe = db.sql(sql).bindValues(spc.parameters());
        if (spc.pageSize().isPresent()) {
            exe = exe.bind("pageSize", spc.pageSize().getAsInt());
        }
        if (spc.offset().isPresent()) {
            exe = exe.bind("offset", spc.offset().getAsLong());
        }
        return exe.map((row, meta) -> summaryMapper.toSummary(row))
                .all()
                .onErrorMap(e -> new DomainException(ErrorCode.PERSISTENCE_ERROR, "Applications list error" + e.getMessage()));
    }

    @Override
    public Mono<Long> countForAdvisor(ListApplicationsQueryCommand cmd) {
        ApplicationQuerySpec spc = DefaultApplicationQuerySpec.from(cmd, null);
        var sql = SqlFragments.countSql(spc);
        return db.sql(sql)
                .bindValues(spc.parameters())
                .map((row, meta) -> row.get(0, Long.class))
                .one();
    }
}
