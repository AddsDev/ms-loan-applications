package com.crediya.loan.model.common.gateways;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthContextPortTest {

    @Test
    void hasRoleTrueWhenRolePresent() {
        var u = new AuthContextPort.AuthUser("sub", "a@b.com", List.of("ADMIN", "USER"), List.of("loan:read"));
        assertTrue(u.hasRole("ADMIN"));
        assertTrue(u.hasRole("USER"));
        assertFalse(u.hasRole("INVALID"));
    }

    @Test
    void hasRoleFalseWhenRoleNotPresent() {
        var u = new AuthContextPort.AuthUser("sub", "t@d.com", List.of("ADMIN", "USER"), List.of("loan:read"));
        assertFalse(u.hasRole("INVALID"));
    }

    @Test
    void hasRoleHandlesNullList() {
        var u = new AuthContextPort.AuthUser("sub", "x@y.com", null, List.of());
        assertFalse(u.hasRole("INVALID"));
    }

    @Test
    void hasTrueWhenRoleMatchesOrPermissionsMatches() {
        var u = new AuthContextPort.AuthUser("sub", "x@y.com", List.of("ADMIN"), List.of("loan:read"));
        assertTrue(u.has("loan:read"));
        assertTrue(u.has("ADMIN"));
        assertFalse(u.has("INVALID"));
    }

    @Test
    void hasHandlesNullPermissionsAndRoles() {
        var u = new AuthContextPort.AuthUser("sub", "x@y.com", null, null);
        assertFalse(u.has("INVALID"));
    }

}