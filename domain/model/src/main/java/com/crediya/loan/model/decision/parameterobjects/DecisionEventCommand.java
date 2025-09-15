package com.crediya.loan.model.decision.parameterobjects;

import com.crediya.loan.model.common.ownership.OwnableCommand;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.valueobjects.Email;
import reactor.util.annotation.Nullable;

import java.time.OffsetDateTime;

public record DecisionEventCommand(
        String loanId,
        ApplicationStatus decision,
        String reason,
        Email advisorEmail,
        @Nullable
        Email email,
        @Nullable
        OffsetDateTime createdAt
) implements OwnableCommand {
    @Override
    public String ownerEmail() {
        return advisorEmail.value();
    }
}
