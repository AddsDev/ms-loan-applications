package com.crediya.loan.model.decision.rules;

import com.crediya.loan.model.common.rules.DomainRule;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.loan.ApplicationStatus;

import java.util.Set;

public class DecisionEventHasFinalStatusRule implements DomainRule<DecisionEvent> {
    private static final Set<ApplicationStatus> ALLOWED_FINAL_STATUSES = Set.of(ApplicationStatus.APPROVED, ApplicationStatus.REJECTED);

    public static final DecisionEventHasFinalStatusRule INSTANCE = new DecisionEventHasFinalStatusRule();

    private DecisionEventHasFinalStatusRule() {
    }

    @Override
    public boolean isSatisfiedBy(DecisionEvent domain) {
        if (domain == null) return false;
        final ApplicationStatus status = domain.decision();
        return status != null && ALLOWED_FINAL_STATUSES.contains(status);
    }

    @Override
    public String getErrorMessage(DecisionEvent domain) {
        final String currentStatus = domain.decision() == null ? "null" : domain.decision().name();
        return "The only valid statuses for DecisionEvent.decision are APPROVED or REJECTED; received:" + currentStatus;
    }
}
