package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.capacity.gateways.CapacityRepository;
import com.crediya.loan.model.capacity.policy.Policies;
import com.crediya.loan.model.capacity.valueobjects.ActiveLoan;
import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import com.crediya.loan.r2dbc.entity.StatusEntity;
import com.crediya.loan.r2dbc.repository.ApplicationReactiveRepository;
import com.crediya.loan.r2dbc.repository.LoanTypeReactiveRepository;
import com.crediya.loan.r2dbc.repository.StatusReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoanCapacityRepositoryAdapter implements CapacityRepository {
    private final ApplicationReactiveRepository applicationRepo;
    private final LoanTypeReactiveRepository loanTypeRepo;
    private final StatusReactiveRepository statusRepo;
    private final TraceLoggerPort logger;

    private static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new DomainException(ErrorCode.INVALID_FORMAT, "Invalid loanId: " + id);
        }
    }

    private static double toRateDecimal(BigDecimal annualPercent) {
        if (annualPercent == null) return 0.0d;
        return annualPercent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP).doubleValue();
    }

    private Mono<StatusEntity> statusByName(String name) {
        var probe = StatusEntity.builder().name(name).build();
        return statusRepo.findOne(Example.of(probe))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Status not found: " + name)));
    }

    private Mono<LoanTypeEntity> loanTypeById(UUID loanTypeId) {
        return loanTypeRepo.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "LoanType not found: " + loanTypeId)));
    }

    @Override
    public Mono<ApplicantProjection> findApplicantByLoanId(String loanId) {
        final UUID id = parseUuid(loanId);
        return applicationRepo.findById(id)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Application not found")))
                .map(app -> {
                    logger.trace("capacityRepo applicant loanId={} email={} baseSalary={}", loanId, app.getEmail(), app.getBaseSalary());
                    return new ApplicantProjection(app.getEmail(), app.getBaseSalary());
                });
    }

    @Override
    public Mono<NewLoanProjection> findNewLoanByLoanId(String loanId) {
        final UUID id = parseUuid(loanId);
        return applicationRepo.findById(id)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Application not found")))
                .flatMap(app ->
                        loanTypeById(app.getLoanTypeId())
                                .map(lt -> {
                                    double rate = toRateDecimal(lt.getInterestRate());
                                    logger.trace("capacityRepo newLoan loanId={} amount={} term={} rate={} auto={}",
                                            loanId, app.getAmount(), app.getTerm(), rate, lt.isAutomaticValidation());
                                    return new NewLoanProjection(
                                            app.getAmount() != null ? app.getAmount().longValue() : 0L,
                                            rate,
                                            app.getTerm() != null ? app.getTerm() : 0,
                                            lt.isAutomaticValidation()
                                    );
                                })
                );
    }

    @Override
    public Mono<List<ActiveLoan>> findActiveLoansByApplicantEmail(String email) {
        return statusByName(ApplicationStatus.APPROVED.name())
                .flatMapMany(st -> {
                    var probe = ApplicationEntity.builder()
                            .email(email)
                            .statusId(st.getStatusId())
                            .build();
                    return applicationRepo.findAll(Example.of(probe));
                })
                .flatMap(app ->
                        loanTypeById(app.getLoanTypeId())
                                .map(lt -> new ActiveLoan(
                                        app.getAmount() != null ? app.getAmount() : BigDecimal.ZERO,
                                        toRateDecimal(lt.getInterestRate()),
                                        app.getTerm() != null ? app.getTerm() : 0
                                ))
                )
                .collectList()
                .doOnSuccess(list -> logger.trace("capacityRepo activeLoans email={} count={}", email, list.size()));
    }

    @Override
    public Mono<Policies> loadPolicies() {
        return Mono.fromSupplier(() -> new Policies(0.35, 5));
    }

    @Override
    public Mono<Void> updateLoanStatus(String loanId, ApplicationStatus decision, long lockVersion) {
        final UUID id = parseUuid(loanId);
        final String target = decision.name(); // Se asume enum alineado con la tabla (APPROVED/REJECTED/MANUAL_REVIEW/PENDING)

        return Mono.zip(
                        applicationRepo.findById(id)
                                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PERSISTENCE_ERROR, "Application not found"))),
                        statusByName(target)
                )
                .flatMap(tuple -> {
                    ApplicationEntity app = tuple.getT1();
                    StatusEntity status = tuple.getT2();

                    app.setStatusId(status.getStatusId());
                    app.setUpdatedAt(OffsetDateTime.now());

                    logger.trace("capacityRepo updateStatus loanId={} newStatus={}", loanId, target);
                    return applicationRepo.save(app).then();
                });
    }
}
