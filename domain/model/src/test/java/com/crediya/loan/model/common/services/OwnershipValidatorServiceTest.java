package com.crediya.loan.model.common.services;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.AuthContextPort;
import com.crediya.loan.model.common.ownership.OwnableCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class OwnershipValidatorServiceTest {

    @Mock
    AuthContextPort port;

    OwnershipValidatorService service;

    @BeforeEach
    void setUp() {
        service = new OwnershipValidatorService(port);
    }

    private static AuthContextPort.AuthUser user(String email, List<String> roles, List<String> permissions) {
        return new AuthContextPort.AuthUser("sub", email, roles, permissions);
    }

    private static OwnableCommand ownableCommand(String email) {
        return (OwnableCommand) () -> email;
    }

    @Test
    void passesWhenOwnerEmailMatchesTokenEmailCaseInsensitive() {
        when(port.currentUser()).thenReturn(Mono.just(user("User@Test.com", List.of(), List.of())));

        Mono<Void> mono = service.assertOwner(ownableCommand("user@test.com"), Set.of());

        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void bypassesWhenBypassAuthorityIsPermission() {
        when(port.currentUser()).thenReturn(Mono.just(user("x@y.com", List.of(), List.of("loan:override"))));

        var mono = service.assertOwner(ownableCommand("other@y.com"), Set.of("loan:override"));

        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void bypassesWhenBypassAuthorityIsRoleWithoutPrefix() {
        when(port.currentUser()).thenReturn(Mono.just(user("x@y.com", List.of("ADMIN"), List.of())));
        var mono = service.assertOwner(ownableCommand("other@y.com"), Set.of("ADMIN"));

        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void failWhenEmailsDoNotMatchAndNoBypass() {
        when(port.currentUser()).thenReturn(Mono.just(user("x@y.com", List.of("ADMIN"), List.of())));
        var mono = service.assertOwner(ownableCommand("other@y.com"), Set.of("ROLE_ADMIN"));

        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void failsWhenEmailsDoNotMatchAndNoBypass() {
        when(port.currentUser()).thenReturn(Mono.just(user("owner@a.com", List.of(), List.of())));
        var mono = service.assertOwner(ownableCommand("other@a.com"), null);

        StepVerifier.create(mono)
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(DomainException.class);
                    var de = (DomainException) err;
                    assertThat(de.getCode()).isEqualTo(ErrorCode.EMAIL_MISMATCH);
                    assertThat(de).hasMessageContaining("does not match");
                })
                .verify();
    }

    @Test
    void failsWhenAnyEmailIsNull() {
        when(port.currentUser()).thenReturn(Mono.just(user(null, List.of(), List.of())));
        var mono = service.assertOwner(ownableCommand("x@y.com"), Set.of());

        StepVerifier.create(mono)
                .expectError(DomainException.class)
                .verify();

        when(port.currentUser()).thenReturn(Mono.just(user("x@y.com", List.of(), List.of())));
        var mono2 = service.assertOwner(ownableCommand(null), Set.of());

        StepVerifier.create(mono2)
                .expectError(DomainException.class)
                .verify();
    }

}