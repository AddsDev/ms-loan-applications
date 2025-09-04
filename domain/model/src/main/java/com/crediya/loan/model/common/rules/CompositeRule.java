package com.crediya.loan.model.common.rules;

import java.util.List;

public class CompositeRule<T> implements DomainRule<T> {

    private final List<DomainRule<T>> rules;
    private final CompositeType type;

    public enum CompositeType {
        AND, OR
    }

    @SafeVarargs
    public CompositeRule(CompositeType type, DomainRule<T>... rules) {
        this.type = type;
        this.rules = List.of(rules);
    }

    @Override
    public boolean isSatisfiedBy(T domain) {
        return switch (type) {
            case AND -> rules.stream().allMatch(rule -> rule.isSatisfiedBy(domain));
            case OR -> rules.stream().anyMatch(rule -> rule.isSatisfiedBy(domain));
        };
    }

    @Override
    public String getErrorMessage(T domain) {
        return rules.stream()
                .filter(rule -> !rule.isSatisfiedBy(domain))
                .map(rule -> rule.getErrorMessage(domain))
                .reduce((ms1, ms2) -> String.format("%s; %s", ms1, ms2))
                .orElse("Composite rule is not satisfied");
    }
}
