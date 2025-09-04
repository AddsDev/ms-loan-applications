package com.crediya.loan.config;

import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.common.gateways.TransactionPort;
import com.crediya.loan.model.loan.gateways.LoanPolicyRepository;
import com.crediya.loan.model.loan.gateways.LoanRepository;
import com.crediya.loan.usecase.applyforloan.ApplyForLoanUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(this.getClass())) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Bean
    public ApplyForLoanUseCase applyForLoanUseCase() {
        return Mockito.mock(ApplyForLoanUseCase.class);
    }
    @Bean
    public LoanPolicyRepository loanPolicyRepository() {
        return Mockito.mock(LoanPolicyRepository.class);
    }

    @Bean
    public LoanRepository loanRepository() {
        return Mockito.mock(LoanRepository.class);
    }

    @Bean
    public TransactionPort transactionPort() {
        return Mockito.mock(TransactionPort.class);
    }

    @Bean
    public TraceLoggerPort traceLoggerPort() {
        return Mockito.mock(TraceLoggerPort.class);
    }
}