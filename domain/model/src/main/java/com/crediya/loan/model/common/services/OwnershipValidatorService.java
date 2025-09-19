package com.crediya.loan.model.common.services;

import com.crediya.loan.model.common.exceptions.AuthenticationException;
import com.crediya.loan.model.common.gateways.AuthContextPort;
import com.crediya.loan.model.common.ownership.Authorities;
import com.crediya.loan.model.common.ownership.OwnableCommand;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class OwnershipValidatorService {
    private final AuthContextPort auth;

    public Mono<Void> assertOwner(OwnableCommand cmd, Set<Authorities> bypassAuthorities) {
        return auth.currentUser().flatMap(user -> {
            if (bypassAuthorities != null) {
                for (Authorities a : bypassAuthorities) {
                    if (user.has(a.name()) || user.hasRole(a.name().replace("ROLE_", ""))) {
                        return Mono.empty(); // bypass authorities
                    }
                }
            }
            String cmdEmail = nullSafeLower(cmd.ownerEmail());
            String tokenEmail = nullSafeLower(user.email());

            boolean matchEmail = cmdEmail != null && cmdEmail.equals(tokenEmail);

            if (matchEmail) return Mono.empty();

            return Mono.error(new AuthenticationException(
                    "The provided identity does not match the authenticated user's token"
            ));
        });
    }

    private static String nullSafeLower(String s) {
        return s == null ? null : s.toLowerCase();
    }
}
