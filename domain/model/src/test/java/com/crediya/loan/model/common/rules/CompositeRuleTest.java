package com.crediya.loan.model.common.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CompositeRuleTest {
    static class TrueRule<T> implements DomainRule<T> {
        public boolean isSatisfiedBy(T t) { return true; }
        public String getErrorMessage(T t) { return "ok"; }
    }
    static class FalseRule<T> implements DomainRule<T> {
        private final String msg;
        FalseRule(String msg){ this.msg = msg; }
        public boolean isSatisfiedBy(T t) { return false; }
        public String getErrorMessage(T t) { return msg; }
    }

    @Test
    void andAggregatesAll() {
        var rule = new CompositeRule<>(CompositeRule.CompositeType.AND,
                new TrueRule<>(), new FalseRule<>("e1"), new FalseRule<>("e2"));
        assertThat(rule.isSatisfiedBy(new Object())).isFalse();
        assertThat(rule.getErrorMessage(new Object())).contains("e1").contains("e2");
    }

    @Test
    void orAggregatesAny() {
        var rule = new CompositeRule<>(CompositeRule.CompositeType.OR,
                new FalseRule<>("e1"), new TrueRule<>());
        assertThat(rule.isSatisfiedBy(new Object())).isTrue();
    }
}