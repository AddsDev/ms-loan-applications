package com.crediya.loan.usecase.decision;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.ownership.Authorities;
import com.crediya.loan.model.common.services.OwnershipValidatorService;
import com.crediya.loan.model.decision.DecisionEvent;
import com.crediya.loan.model.decision.gateways.DecisionPublisherPort;
import com.crediya.loan.model.decision.parameterobjects.DecisionEventCommand;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.valueobjects.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DecisionLoanRequestUseCaseTest {
    @Mock
    private  LoanRepository loanRepository;
    @Mock
    private DecisionPublisherPort decisionPublisher;
    @Mock
    private TraceLoggerPort logger;
    @Mock
    private OwnershipValidatorService ownershipValidator;

    private DecisionLoanRequestUseCase useCase;
    private DecisionEventCommand cmd;

    @BeforeEach
    void setUp() {
        useCase = new DecisionLoanRequestUseCase(loanRepository, decisionPublisher, logger, ownershipValidator);

        cmd = new DecisionEventCommand(
                "LN-001",
                ApplicationStatus.APPROVED,
                "meets policy",
                new Email("advisor@bank.com"),
                new Email("client@mail.com"),
                null
        );

        lenient().when(ownershipValidator.assertOwner(any(), any())).thenReturn(Mono.empty());

        lenient().when(loanRepository.changeStatusIfPending(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        lenient().when(decisionPublisher.publish(any())).thenReturn(Mono.empty());
    }

    @Test
    void publishes_and_returns_event_logs_and_uses_tx() {
        var result = useCase.execute(cmd);

        StepVerifier.create(result)
                .assertNext(ev -> {
                    assertThat(ev.loanId()).isEqualTo("LN-001");
                    assertThat(ev.decision()).isEqualTo(ApplicationStatus.APPROVED);
                })
                .verifyComplete();

        verify(ownershipValidator).assertOwner(eq(cmd), argThat(s ->
                s != null && s.contains(Authorities.ROLE_ADMINISTRADOR) && s.size() == 1));

        verify(loanRepository).changeStatusIfPending(any(DecisionEvent.class));
        verify(decisionPublisher).publish(any(DecisionEvent.class));

        verify(logger).trace("tx[DecisionLoanRequestUseCase] start");
        verify(logger).trace("useCase=DecisionLoanRequest start loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verify(logger).trace("useCase=DecisionLoanRequest success loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verify(logger, never()).error(startsWith("tx[DecisionLoanRequestUseCase] fail"), any(), any());
    }

    @Test
    void swallows_publisher_error_and_still_returns_event() {
        when(decisionPublisher.publish(any())).thenReturn(Mono.error(new RuntimeException("bus down")));

        StepVerifier.create(useCase.execute(cmd))
                .assertNext(ev -> assertThat(ev.loanId()).isEqualTo("LN-001"))
                .verifyComplete();

        verify(decisionPublisher).publish(any());
        verify(logger).trace("useCase=DecisionLoanRequest success loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
    }

    @Test
    void logs_in_order_on_success() {
        StepVerifier.create(useCase.execute(cmd)).expectNextCount(1).verifyComplete();

        verify(logger).trace("tx[DecisionLoanRequestUseCase] start");
        verify(logger).trace("useCase=DecisionLoanRequest start loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verify(logger).trace("useCase=DecisionLoanRequest success loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verifyNoMoreInteractions(logger);
    }
}