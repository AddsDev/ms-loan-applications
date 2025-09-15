package com.crediya.loan.usecase.decision;

import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.gateways.TransactionPort;
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

import java.util.function.Supplier;

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
    private TransactionPort tx;
    @Mock
    private OwnershipValidatorService ownershipValidator;

    private DecisionLoanRequestUseCase useCase;
    private DecisionEventCommand cmd;

    @BeforeEach
    void setUp() {
        useCase = new DecisionLoanRequestUseCase(loanRepository, decisionPublisher, logger, tx, ownershipValidator);

        cmd = new DecisionEventCommand(
                "LN-001",
                ApplicationStatus.APPROVED,
                "meets policy",
                new Email("advisor@bank.com"),
                new Email("client@mail.com"),
                null
        );

        lenient().when(tx.transactional(any())).thenAnswer(inv -> {
            Supplier<Mono<?>> supplier = inv.getArgument(0);
            return supplier.get();
        });

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
                s != null && s.contains("ROLE_ADMINISTRADOR") && s.size() == 1));

        verify(tx).transactional(any());
        verify(loanRepository).changeStatusIfPending(any(DecisionEvent.class));
        verify(decisionPublisher).publish(any(DecisionEvent.class));

        verify(logger).trace("tx[DecisionLoanRequestUseCase] start");
        verify(logger).trace("usecase=DecisionLoanRequest start loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verify(logger).trace("usecase=DecisionLoanRequest success loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verify(logger, never()).error(startsWith("tx[DecisionLoanRequestUseCase] fail"), any(), any());
    }

    @Test
    void fails_when_command_is_null_and_does_not_call_downstream() {
        StepVerifier.create(useCase.execute(null))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(ValidationException.class);
                    assertThat(err).hasMessageContaining("Command is null");
                })
                .verify();

        verifyNoInteractions(ownershipValidator, tx, loanRepository, decisionPublisher);
        verify(logger, never()).trace(anyString(), any(), any());
        verify(logger, never()).trace("tx[DecisionLoanRequestUseCase] start");
    }

    @Test
    void swallows_publisher_error_and_still_returns_event() {
        when(decisionPublisher.publish(any())).thenReturn(Mono.error(new RuntimeException("bus down")));

        StepVerifier.create(useCase.execute(cmd))
                .assertNext(ev -> assertThat(ev.loanId()).isEqualTo("LN-001"))
                .verifyComplete();

        verify(decisionPublisher).publish(any());
        verify(logger).trace("usecase=DecisionLoanRequest success loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
    }

    @Test
    void logs_in_order_on_success() {
        StepVerifier.create(useCase.execute(cmd)).expectNextCount(1).verifyComplete();

        verify(logger).trace("tx[DecisionLoanRequestUseCase] start");
        verify(logger).trace("usecase=DecisionLoanRequest start loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verify(logger).trace("usecase=DecisionLoanRequest success loanId={} decision={}",
                "LN-001", ApplicationStatus.APPROVED);
        verifyNoMoreInteractions(logger);
    }
}