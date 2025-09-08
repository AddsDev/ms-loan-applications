package com.crediya.loan.model.loan.valueobjects;

import java.util.List;

public record SortSpec(
        String property, boolean descending
) {

    private static final List<String> whiteList = List.of("createdAt", "amount", "termInMonths", "status", "email");

    public static SortSpec from(String raw) {
        var defaultField = whiteList.get(0);
        if(raw == null || raw.isBlank()) return new SortSpec(defaultField, true);
        var parts = raw.split(",");
        var prop = whiteList.contains(parts[0]) ? parts[0] : defaultField;
        var desc = !"asc".equalsIgnoreCase(parts.length > 1 ? parts[1] : "desc");
        return new SortSpec(prop, desc);
    }
}
