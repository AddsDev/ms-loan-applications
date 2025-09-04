package com.crediya.loan.model.loan;

import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class LoanApplicationTest {
    @Test
    void registerCreatesPendingReviewAndValidatesRules() {
        ApplyForLoanCommand cmd = TestHelper.anyCmd();
        LoanPolicies policies = TestHelper.policiesForConsumer();

        LoanApplication la = LoanApplication.register(cmd, policies);

        assertThat(la.id()).isNotBlank();
        assertThat(la.status()).isEqualTo(ApplicationStatus.PENDING_REVIEW);
        assertThat(la.loanType()).isEqualTo(LoanType.CONSUMER);
        assertThat(la.createdAt()).isNotNull();
    }

    @Test
    void registerFailsWhenRulesNotSatisfied() {
        var badPolicies = TestHelper.policiesForConsumer();
        var badCmd = new ApplyForLoanCommand(
                TestHelper.anyCmd().document(),
                TestHelper.anyCmd().email(),
                TestHelper.anyCmd().amount(),
                3,
                LoanType.CONSUMER
        );

        assertThatThrownBy(() -> LoanApplication.register(badCmd, badPolicies))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Term must be between");
    }

    @Test
     void markApprovedAndRejectedReturnNewInstances() {
        var la = LoanApplication.register(TestHelper.anyCmd(), TestHelper.policiesForConsumer());
        var approved = la.markApproved();
        var rejected = la.markRejected();

        assertThat(approved).isNotSameAs(la);
        assertThat(approved.status()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(rejected.status()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(approved.createdAt()).isEqualTo(la.createdAt()); // invariante conservada
    }
}