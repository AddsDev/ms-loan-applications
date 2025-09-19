package com.crediya.loan.usecase.applyforloan;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.EmailValidationPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.ownership.OwnableCommand;
import com.crediya.loan.model.common.services.OwnershipValidatorService;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.gateways.LoanPolicyRepository;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.validation.EmailValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyForLoanUseCaseTest {
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private EmailValidationPort emailValidationPort;
    @Mock
    private LoanPolicyRepository policyRepository;
    @Mock
    private TraceLoggerPort logger;
    @Mock
    private OwnershipValidatorService ownershipValidator;

    private ApplyForLoanUseCase useCase;

    private ApplyForLoanCommand cmd;

    private static LoanPolicies policiesForConsumer(BigDecimal minAmt, BigDecimal maxAmt) {
        Map<LoanType, AmountRange> amounts = new EnumMap<>(LoanType.class);
        Map<LoanType, TermRange> terms = new EnumMap<>(LoanType.class);
        amounts.put(LoanType.CONSUMER, new AmountRange(minAmt, maxAmt));
        terms.put(LoanType.CONSUMER, new TermRange(6, 84));
        return new LoanPolicies(amounts, terms);
    }

    @BeforeEach
    void setUp() {
        useCase = new ApplyForLoanUseCase(loanRepository, emailValidationPort, policyRepository, logger, ownershipValidator);

        cmd = new ApplyForLoanCommand(
                new Document("123456"), // 6 dígitos para probar máscara "****3456"
                new Email("user@test.com"),
                new BigDecimal("1000000.00"),
                24,
                LoanType.CONSUMER
        );

        LoanPolicies policies = policiesForConsumer(
                new BigDecimal("500000.00"), new BigDecimal("30000000.00")
        );

        lenient().when(ownershipValidator.assertOwner(any(OwnableCommand.class), any())).thenReturn(Mono.empty());
        lenient().when(policyRepository.loadAllPolicies()).thenReturn(Mono.just(policies));
        lenient().when(policyRepository.loanTypeExists(LoanType.CONSUMER)).thenReturn(Mono.just(true));
        lenient().when(emailValidationPort.checkEmail(any(Email.class))).thenAnswer(inv ->
                Mono.just(new EmailValidationResult(inv.getArgument(0), "User Name",new BigDecimal("510000.00"),true))
        );

        lenient().when(loanRepository.save(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    }


    @Test
    void execute_successful_flow_saves_inside_transaction_and_logs_with_masked_doc() {
        var result = useCase.execute(cmd);

        StepVerifier.create(result)
                .assertNext(app -> {
                    assertThat(app.id()).isNotBlank();
                    assertThat(app.status()).isEqualTo(ApplicationStatus.PENDING);
                    assertThat(app.loanType()).isEqualTo(LoanType.CONSUMER);
                    assertThat(app.email()).isEqualTo(cmd.email());
                })
                .verifyComplete();

        verify(loanRepository, times(1)).save(any(LoanApplication.class));

        verify(logger).trace("ApplyForLoan start, doc={} email={}", "****3456", cmd.email());
        verify(logger).trace("Email validation started");
        verify(logger).trace(eq("Email validation result: {}"), any());
        verify(logger).info(startsWith("tx[ApplyForLoanUseCase] success id="), any(), any());

        verify(logger, never()).error(startsWith("tx[ApplyForLoanUseCase] fail"), any(Throwable.class));
    }

    @Test
    void execute_fails_when_policies_not_configured() {
        when(policyRepository.loadAllPolicies()).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(cmd))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(DomainException.class);
                    DomainException de = (DomainException) err;
                    assertThat(de.getCode()).isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION);
                    assertThat(de).hasMessageContaining("Loan policies not configured");
                })
                .verify();

        verify(loanRepository, never()).save(any());
        verify(logger, atLeast(0)).error(anyString(), any(Throwable.class));
    }

    @Test
    void execute_fails_when_loan_type_not_in_catalog() {
        when(policyRepository.loanTypeExists(LoanType.CONSUMER)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.execute(cmd))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(DomainException.class);
                    DomainException de = (DomainException) err;
                    assertThat(de.getCode()).isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION);
                    assertThat(de).hasMessageContaining("Loan type is not configured in catalog");
                })
                .verify();

        verify(loanRepository, never()).save(any());
        // El error se produce antes del email y del save
        verify(logger, atLeastOnce()).trace(eq("ApplyForLoan start, doc={} email={}"), any(), any());
    }

    @Test
    void execute_fails_when_email_not_registered_logs_error_in_validation_stage() {
        when(emailValidationPort.checkEmail(any())).thenReturn(
                Mono.error(new DomainException(ErrorCode.BUSINESS_RULE_VIOLATION, "email address is not registered"))
        );

        StepVerifier.create(useCase.execute(cmd))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(DomainException.class);
                    DomainException de = (DomainException) err;
                    assertThat(de.getCode()).isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION);
                    assertThat(de).hasMessageContaining("email address is not registered");
                })
                .verify();

        verify(loanRepository, never()).save(any());
        // Se loguea error en la etapa de validación de email y en el tx final al propagarse
        verify(logger).error(eq("Email validation failed"), any(Throwable.class));
        verify(logger).error(eq("tx[ApplyForLoanUseCase] fail"), any(Throwable.class));
    }

    @Test
    void execute_propagates_error_when_repository_save_fails_and_logs_tx_fail() {
        when(loanRepository.save(any())).thenReturn(Mono.error(new RuntimeException("DB down")));

        StepVerifier.create(useCase.execute(cmd))
                .expectErrorSatisfies(err -> assertThat(err)
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("DB down"))
                .verify();

        verify(loanRepository, times(1)).save(any());
        verify(logger).error(eq("tx[ApplyForLoanUseCase] fail"), any(Throwable.class));
    }

    @Test
    void execute_logs_in_reasonable_order_on_success() {
        var result = useCase.execute(cmd);
        StepVerifier.create(result).expectNextCount(1).verifyComplete();

        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).trace("ApplyForLoan start, doc={} email={}", "****3456", cmd.email());
        inOrder.verify(logger).trace("Email validation started");
        inOrder.verify(logger).trace(eq("Email validation result: {}"), any());
        inOrder.verify(logger).info(startsWith("tx[ApplyForLoanUseCase] success id="), any(), any());
    }

}