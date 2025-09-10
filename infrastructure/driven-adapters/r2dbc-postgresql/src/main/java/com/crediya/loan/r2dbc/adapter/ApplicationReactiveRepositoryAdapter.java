package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.ApplicationSummary;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.model.loan.valueobjects.SortSpec;
import com.crediya.loan.r2dbc.common.DatabaseErrorMapper;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.ApplicationEntityMapper;
import com.crediya.loan.r2dbc.mapper.ApplicationSummaryMapper;
import com.crediya.loan.r2dbc.repository.ApplicationReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.UUID;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<LoanApplication, ApplicationEntity, UUID, ApplicationReactiveRepository> implements LoanRepository {
    private final DatabaseClient db;
    private final TraceLoggerPort logger;
    private final ApplicationEntityMapper entityMapper;
    private final ApplicationSummaryMapper summaryMapper;

    protected ApplicationReactiveRepositoryAdapter(DatabaseClient db, ApplicationReactiveRepository repository, ObjectMapper mapper, ApplicationEntityMapper entityMapper, ApplicationSummaryMapper summaryMapper,TraceLoggerPort logger) {
        super(repository, mapper, entityMapper::toDomain);
        this.db = db;
        this.entityMapper = entityMapper;
        this.summaryMapper = summaryMapper;
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

    @Override
    public Flux<ApplicationSummary> findForAdvisor(ListApplicationsQueryCommand cmd, SortSpec sort) {
        var sql = new StringBuilder("""
                SELECT a.application_id as id, a.email as email, a.identity_document as document, a.name as applicantName, a.base_salary as baseSalary,
                    lt.code as loan_type,
                    a.amount as amount, a.term as term,
                    lt.interest_rate as termInMonths,
                    s.name as status,
                    a.created_at as createdAt
                FROM applications a
                JOIN statuses s ON s.status_id = a.status_id
                JOIN loan_types lt ON lt.loan_type_id = a.loan_type_id
                WHERE 1=1
                """);
        var params = new HashMap<String, Object>();

        attachStatuses(cmd, sql, params);
        attachEmail(cmd, sql, params);
        attachDocument(cmd, sql, params);
        attachLoanType(cmd, sql, params);

        if (sort != null) {
            sql.append(" ORDER BY ").append(sort.property()).append(sort.descending() ? " DESC" : " ASC");
            sql.append(" LIMIT :limit OFFSET :offset");
            params.put("limit", cmd.size());
            params.put("offset", Math.max(cmd.page() , 0) * cmd.size());
        }

        return db.sql(sql.toString())
                .bindValues(params)
                .map((row, meta) -> summaryMapper.toSummary(row))
                .all().onErrorMap( e -> new DomainException(ErrorCode.PERSISTENCE_ERROR, "Applications list error"));
    }

    @Override
    public Mono<Long> countForAdvisor(ListApplicationsQueryCommand cmd) {
        var sql = new StringBuilder("""
                SELECT COUNT(1) as count
                FROM applications a
                JOIN statuses s ON s.status_id = a.status_id
                JOIN loan_types lt ON lt.loan_type_id = a.loan_type_id
                WHERE 1=1
                """);
        var params = new HashMap<String, Object>();

        attachStatuses(cmd, sql, params);
        attachEmail(cmd, sql, params);
        attachDocument(cmd, sql, params);
        attachLoanType(cmd, sql, params);

        return db.sql(sql.toString())
                .bindValues(params)
                .map((row, meta) -> row.get(0, Long.class))
                .one();
    }

    private static void attachLoanType(ListApplicationsQueryCommand cmd, StringBuilder sql, HashMap<String, Object> params) {
        if (cmd.loanTypeCode() != null && !cmd.loanTypeCode().isBlank()) {
            sql.append(" AND lt.code = :loanTypeCode ");
            params.put("loanTypeCode", cmd.loanTypeCode().trim());
        }
    }

    private static void attachStatuses(ListApplicationsQueryCommand cmd, StringBuilder sql, HashMap<String, Object> params) {
        if (cmd.statuses() != null && !cmd.statuses().isEmpty()) {
            sql.append(" AND s.name = ANY(:statuses) ");
            params.put("statuses", cmd.statuses().stream().map(Enum::name).toArray(String[]::new));
        } else {
            sql.append(" AND s.name = ANY(:statuses) ");
            params.put("statuses", new String[]{"PENDING_REVIEW", "REJECTED", "MANUAL_REVIEW"});
        }
    }

    private static void attachEmail(ListApplicationsQueryCommand cmd, StringBuilder sql, HashMap<String, Object> params) {
        if (cmd.email() != null && !cmd.email().isBlank()) {
            sql.append(" AND a.email ILIKE :email ");
            params.put("email", "%" + cmd.email().trim() + "%");
        }
    }

    private static void attachDocument(ListApplicationsQueryCommand cmd, StringBuilder sql, HashMap<String, Object> params) {
        if (cmd.document() != null && !cmd.document().isBlank()) {
            sql.append(" AND a.identity_document ILIKE :document ");
            params.put("document", "%" + cmd.document().trim() + "%");
        }
    }
}
