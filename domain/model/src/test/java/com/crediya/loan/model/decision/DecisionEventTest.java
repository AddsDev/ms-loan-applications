package com.crediya.loan.model.decision;

import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DecisionEventTest {
    @Test
    void register_builds_event_filling_defaults_and_validates_rules() {
        var cmd = new DecisionEventCommand(
                "L-123",
                ApplicationStatus.APPROVED,
                "meets criteria",
                new Email("advisor@bank.com"),
                new Email("client@mail.com"),
                null
        );

        var ev = DecisionEvent.register(cmd);

        assertThat(ev.loanId()).isEqualTo("L-123");
        assertThat(ev.decision()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(ev.reason()).isEqualTo("meets criteria");
        assertThat(ev.advisorEmail().toString()).hasToString("advisor@bank.com");
        assertThat(ev.email().toString()).hasToString("client@mail.com");
        assertThat(ev.eventName()).isEqualTo("DecisionEvent.register");
        assertThat(ev.eventVersion()).isEqualTo(1);
        assertThat(ev.createdAt()).isNotNull();
    }

    @Test
    void register_uses_provided_createdAt_when_present() {
        var ts = OffsetDateTime.now().minusDays(1);
        var ev = DecisionEvent.register(new DecisionEventCommand(
                "L-1", ApplicationStatus.REJECTED, "risk",
                new Email("a@b.com"), null, ts
        ));
        assertThat(ev.createdAt()).isEqualTo(ts);
    }

    @Test
    void register_fails_when_status_not_final() {
        var cmd = new DecisionEventCommand(
                "L-2", ApplicationStatus.PENDING, "pending",
                new Email("a@b.com"), null, null
        );
        assertThatThrownBy(() -> DecisionEvent.register(cmd))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("APPROVED").hasMessageContaining("REJECTED");
    }
}