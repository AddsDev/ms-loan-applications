package com.crediya.loan.api.loan.mapper;

import com.crediya.loan.api.loan.dto.DecisionEventRequest;
import com.crediya.loan.api.loan.dto.DecisionEventResponse;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface DecisionMapper {

    @Mapping(target = "decision", source = "decision")
    @Mapping(target = "advisorEmail", source = "request.advisorEmail", qualifiedByName = "toEmail")
    @Mapping(target = "loanId", source = "id")
    DecisionEventCommand toCommand(DecisionEventRequest request);

    @Named("toEmail")
    default Email toEmail(String email) {
        return email == null ? null : new Email(email);
    }


    @Named("toDateString")
    default String toDateString(OffsetDateTime date) {
        return date.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @Mapping(target = "status", source = "decision")
    @Mapping(target = "id", source = "loanId")
    @Mapping(target = "updatedAt", source = "createdAt", qualifiedByName = "toDateString")
    DecisionEventResponse toResponse(DecisionEvent command);
}
