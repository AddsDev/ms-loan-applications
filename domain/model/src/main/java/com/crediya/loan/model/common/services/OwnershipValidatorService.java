package com.crediya.loan.model.common.services;

import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.gateways.AuthContextPort;
import com.crediya.loan.model.common.ownership.OwnableCommand;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class OwnershipValidatorService {
    private final AuthContextPort auth;

    public Mono<Void> assertOwner(OwnableCommand cmd, Set<String> bypassAuthorities) {
        return auth.currentUser().flatMap(user -> {
            if (bypassAuthorities != null) {
                for (String a : bypassAuthorities) {
                    if (user.has(a) || user.hasRole(a.replace("ROLE_",""))) {
                        return Mono.empty(); // bypass por rol/permisos
                    }
                }
            }
            String cmdEmail = nullSafeLower(cmd.ownerEmail());
            String tokenEmail = nullSafeLower(user.email());

            boolean matchEmail = cmdEmail != null && cmdEmail.equals(tokenEmail);

            if (matchEmail) return Mono.empty();

            return Mono.error(new DomainException(
                    ErrorCode.EMAIL_MISMATCH,
                    "The provided identity does not match the authenticated user's token"
            ));
        });
    }

    private static String nullSafeLower(String s) { return s == null ? null : s.toLowerCase(); }
}
