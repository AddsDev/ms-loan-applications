package com.crediya.loan.config;

import com.crediya.loan.model.common.gateways.AuthContextPort;
import com.crediya.loan.model.common.services.OwnershipValidatorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OwnershipConfig {
    @Bean
    OwnershipValidatorService ownershipValidator(AuthContextPort auth) {
        return new OwnershipValidatorService(auth);
    }
}
