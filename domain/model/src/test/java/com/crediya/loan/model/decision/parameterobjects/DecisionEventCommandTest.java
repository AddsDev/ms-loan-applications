package com.crediya.loan.model.decision.parameterobjects;

import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DecisionEventCommandTest {
    @Test
    void ownerEmail_returns_advisorEmail_value() {
        var cmd = new DecisionEventCommand(
                "L-1", ApplicationStatus.APPROVED, "ok",
                new Email("Advisor@Bank.com"), null, null
        );
        assertThat(cmd.ownerEmail()).isEqualTo("Advisor@Bank.com");
    }
}