package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.common.gateways.CatalogResolverPort;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.valueobjects.Amount;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.loan.valueobjects.TermInMonths;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationEntityMapper {
    private final CatalogResolverPort catalog;

    public LoanApplication toDomain(ApplicationEntity entity) {
        if (entity == null) return null;

        ApplicationStatus status = catalog.toStatus(entity.getStatusId().toString());
        LoanType type = catalog.toLoanType(entity.getLoanTypeId().toString());
        if (status == null) {
            throw new ValidationException(ErrorCode.PERSISTENCE_ERROR, "Unknown status_id=" + entity.getStatusId());
        }
        if (type == null) {
            throw new ValidationException(ErrorCode.PERSISTENCE_ERROR, "Unknown loan_type_id=" + entity.getLoanTypeId());
        }

        return new LoanApplication(
                entity.getApplicationId().toString(),
                new Email(entity.getEmail()),
                entity.getName(),
                new Document(entity.getIdentityDocument()),
                new Amount(entity.getAmount()),
                new TermInMonths(entity.getTerm()),
                type,
                entity.getBaseSalary(),
                status,
                entity.getCreatedAt()
        );
    }

    public DecisionEvent toDomainDecision(ApplicationEntity entity) {
        if (entity == null) return null;

        ApplicationStatus status = catalog.toStatus(entity.getStatusId().toString());
        if (status == null) {
            throw new ValidationException(ErrorCode.PERSISTENCE_ERROR, "Unknown status_id=" + entity.getStatusId());
        }

        return DecisionEvent.register(new DecisionEventCommand(
                        entity.getApplicationId().toString(),
                        status,
                        entity.getReason(),
                        new Email(entity.getAdvisorEmail()),
                        new Email(entity.getEmail()),
                        entity.getUpdatedAt()
                )
        );
    }

    public ApplicationEntity toEntity(LoanApplication domain) {
        if (domain == null) return null;

        UUID statusId = UUID.fromString(catalog.toStatusId(domain.status()));
        UUID loanTypeId = UUID.fromString(catalog.toLoanTypeId(domain.loanType()));

        return ApplicationEntity.builder()
                .amount(domain.amount().value().setScale(2, RoundingMode.HALF_UP))
                .term(domain.term().value())
                .email(domain.email().value())
                .name(domain.name())
                .baseSalary(domain.baseSalary())
                .identityDocument(domain.identityDocument().toString())
                .statusId(statusId)
                .loanTypeId(loanTypeId)
                .createdAt(domain.createdAt())
                .updatedAt(domain.createdAt())
                .build();
    }

    public ApplicationEntity toEntity(LoanApplication domain, DecisionEvent event) {
        if (domain == null || event == null) return null;

        return ApplicationEntity.builder()
                .applicationId(UUID.fromString(domain.id()))
                .amount(domain.amount().value().setScale(2, RoundingMode.HALF_UP))
                .term(domain.term().value())
                .email(domain.email().value())
                .name(domain.name())
                .baseSalary(domain.baseSalary())
                .identityDocument(domain.identityDocument().toString())
                .statusId(UUID.fromString(catalog.toStatusId(event.decision())))
                .loanTypeId(UUID.fromString(catalog.toLoanTypeId(domain.loanType())))
                .createdAt(domain.createdAt())
                .updatedAt(event.createdAt())
                .reason(event.reason())
                .advisorEmail(event.advisorEmail().value())
                .build();
    }
}
