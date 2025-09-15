package com.crediya.loan.model.decision.rules;


import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DecisionEventHasFinalStatusRuleTest {
    @Test
    void isSatisfiedBy_returns_true_only_for_final_statuses() {
        var okApproved = new DecisionEvent("L1",
                ApplicationStatus.APPROVED,
                "ok",
                new Email("adv@x.com"),
                new Email("c@x.com"),
                "ev",
                1,
                OffsetDateTime.now()
        );

        var okRejected = new DecisionEvent("L2",
                ApplicationStatus.REJECTED,
                "no",
                new Email("adv@x.com"),
                null,
                "ev",
                1,
                OffsetDateTime.now()
        );

        var notPending = new DecisionEvent("L3",
                ApplicationStatus.PENDING,
                "pend",
                new Email("adv@x.com"),
                null,
                "ev",
                1,
                OffsetDateTime.now()
        );

        assertThat(DecisionEventHasFinalStatusRule.INSTANCE.isSatisfiedBy(okApproved)).isTrue();
        assertThat(DecisionEventHasFinalStatusRule.INSTANCE.isSatisfiedBy(okRejected)).isTrue();
        assertThat(DecisionEventHasFinalStatusRule.INSTANCE.isSatisfiedBy(notPending)).isFalse();
        assertThat(DecisionEventHasFinalStatusRule.INSTANCE.isSatisfiedBy(null)).isFalse();
    }

    @Test
    void getErrorMessage_mentions_current_status_or_null() {
        var withNull = new DecisionEvent("L1",
                null,
                "r",
                new Email("a@x.com"),
                null,
                "e",
                1,
                OffsetDateTime.now()
        );
        var msg1 = DecisionEventHasFinalStatusRule.INSTANCE.getErrorMessage(withNull);
        assertThat(msg1).contains("APPROVED").contains("REJECTED").contains("received:null");

        var withOther = new DecisionEvent("L2", ApplicationStatus.MANUAL_REVIEW, "r", new Email("a@x.com"), null, "e", 1, OffsetDateTime.now());
        var msg2 = DecisionEventHasFinalStatusRule.INSTANCE.getErrorMessage(withOther);
        assertThat(msg2).contains("received:IN_REVIEW");
    }
}