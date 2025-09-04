package com.crediya.loan.model.common.gateways;

import reactor.core.publisher.Mono;

import java.util.List;

public interface AuthContextPort {
    Mono<AuthUser> currentUser();

    record AuthUser(String subject, String email, List<String> roles, List<String> permissions) {
        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }
        public boolean has(String authority) {
            return hasRole(authority) || (permissions != null && permissions.contains(authority));
        }
    }
}
