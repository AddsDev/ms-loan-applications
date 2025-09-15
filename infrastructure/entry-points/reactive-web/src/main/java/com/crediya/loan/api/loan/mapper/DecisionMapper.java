package com.crediya.loan.api.loan.mapper;

import com.crediya.loan.api.loan.dto.DecisionEventRequest;
import com.crediya.loan.api.loan.dto.DecisionEventResponse;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

    @Mapping(target = "status", source = "decision")
    DecisionEventResponse toResponse(DecisionEvent command);
}
