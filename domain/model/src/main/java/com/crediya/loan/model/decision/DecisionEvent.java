package com.crediya.loan.model.decision;

import com.crediya.loan.model.common.rules.DomainRuleValidator;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.decision.rules.DecisionEventHasFinalStatusRule;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.valueobjects.Email;

import java.time.OffsetDateTime;
import java.util.Objects;

public record DecisionEvent(
        String loanId,
        ApplicationStatus decision,
        String reason,
        Email advisorEmail,
        Email email,
        String eventName,
        Integer eventVersion,
        OffsetDateTime createdAt
) {
    public static DecisionEvent register(DecisionEventCommand command) {
        Objects.requireNonNull(command, "cmd is required");
        var decision = new DecisionEvent(
                command.loanId(),
                command.decision(),
                command.reason(),
                command.advisorEmail(),
                command.email(),
                "DecisionEvent.register",
                1,
                command.createdAt() == null ? OffsetDateTime.now() : command.createdAt()
        );
        DomainRuleValidator.validate(decision, DecisionEventHasFinalStatusRule.INSTANCE);
        return decision;
    }
}
