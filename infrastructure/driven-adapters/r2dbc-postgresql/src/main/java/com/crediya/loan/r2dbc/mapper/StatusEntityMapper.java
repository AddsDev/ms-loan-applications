package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.r2dbc.entity.StatusEntity;
import org.springframework.stereotype.Component;

@Component
public class StatusEntityMapper {
    public ApplicationStatus toEnum(StatusEntity entity) {
        if (entity == null || entity.getName() == null) return null;
        return ApplicationStatus.valueOf(entity.getName()); // PENDING_REVIEW/APPROVED/REJECTED
    }

    public StatusEntity toEntity(ApplicationStatus status) {
        return StatusEntity.builder()
                .name(status.name())
                .description(status.name())
                .build();
    }
}
