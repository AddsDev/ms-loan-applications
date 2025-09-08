package com.crediya.loan.usecase.listapplications;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.ApplicationSummary;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.model.loan.valueobjects.SortSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ListApplicationUseCaseTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private TraceLoggerPort logger;

    private ListApplicationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListApplicationUseCase(loanRepository, logger);
    }

    private ApplicationSummary app(String id, String email, String loanType, BigDecimal amount, int term, BigDecimal rate) {
        return new ApplicationSummary(
                id,
                email,
                "CC123",
                "John Doe",
                loanType,
                "PENDING",
                amount,
                term,
                rate,
                new BigDecimal("3500000"),
                new BigDecimal("512345.67"),
                OffsetDateTime.now()
        );
    }

    private ListApplicationsQueryCommand cmd(int page, int size, String sort) {
        return new ListApplicationsQueryCommand(
                page,
                size,
                sort,
                List.of(ApplicationStatus.PENDING),
                null,
                null,
                null
        );
    }

    @Test
    void execute_successful_flow_returns_paged_result_and_logs() {
        var command = cmd(1, 2, "createdAt,desc");

        when(loanRepository.findForAdvisor(eq(command), any(SortSpec.class)))
                .thenReturn(Flux.just(
                        app("a-1", "a1@x.com", "CONSUMER", new BigDecimal("10000000"), 48, new BigDecimal("24.5")),
                        app("a-2", "a2@x.com", "BUSINESS", new BigDecimal("15000000"), 60, new BigDecimal("22.0"))
                ));
        when(loanRepository.countForAdvisor(command)).thenReturn(Mono.just(10L));

        StepVerifier.create(useCase.execute(command))
                .assertNext(page -> {
                    assertThat(page.page()).isEqualTo(1);
                    assertThat(page.size()).isEqualTo(2);
                    assertThat(page.totalItems()).isEqualTo(10L);
                    assertThat(page.data()).hasSize(2);
                    assertThat(page.data().get(0).email()).isEqualTo("a1@x.com");
                })
                .verifyComplete();

        verify(loanRepository).findForAdvisor(eq(command), any(SortSpec.class));
        verify(loanRepository).countForAdvisor(command);
        verify(logger).trace(startsWith("ListApplications start page="), any(), any(), any(), any(), any(), any());
        verify(logger).trace("ListApplications query started");
        verify(logger).trace(startsWith("ListApplications query success total="), any());
        verifyNoMoreInteractions(logger);
    }

    @Test
    void execute_sanitizes_sort_and_defaults_when_malicious_value() {
        var command = cmd(0, 10, "amount;drop table applications");

        when(loanRepository.countForAdvisor(command)).thenReturn(Mono.just(0L));
        when(loanRepository.findForAdvisor(eq(command), any(SortSpec.class))).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(command))
                .assertNext(page -> {
                    assertThat(page.page()).isZero();
                    assertThat(page.size()).isEqualTo(10);
                    assertThat(page.totalItems()).isZero();
                    assertThat(page.data()).isEmpty();
                })
                .verifyComplete();

        // Capturamos el SortSpec para verificar que se limpió a la whitelist
        ArgumentCaptor<SortSpec> sortCaptor = ArgumentCaptor.forClass(SortSpec.class);
        verify(loanRepository).findForAdvisor(eq(command), sortCaptor.capture());
        SortSpec sortUsed = sortCaptor.getValue();
        assertThat(sortUsed.property()).isEqualTo("createdAt");
        assertThat(sortUsed.descending()).isTrue();
    }

    @Test
    void execute_when_count_errors_propagates_and_logs() {
        var command = cmd(0, 5, "createdAt,asc");

        when(loanRepository.findForAdvisor(eq(command), any(SortSpec.class)))
                .thenReturn(Flux.just(app("a-1", "a1@x.com", "CONSUMER",
                        new BigDecimal("10000000"), 48, new BigDecimal("24.5"))));
        when(loanRepository.countForAdvisor(command))
                .thenReturn(Mono.error(new IllegalStateException("count failed")));

        StepVerifier.create(useCase.execute(command))
                .expectErrorMatches(e -> e instanceof IllegalStateException && e.getMessage().contains("count failed"))
                .verify();

        verify(logger).trace(startsWith("ListApplications start page="), any(), any(), any(), any(), any(), any());
        verify(logger).trace("ListApplications query started");
        verify(logger).error(eq("ListApplications query fail"), any(Throwable.class));
        verifyNoMoreInteractions(logger);
    }
}