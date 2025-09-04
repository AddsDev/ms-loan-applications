package com.crediya.loan.model.common.rules;

public interface DomainRule<T> {
    boolean isSatisfiedBy(T domain);
    String getErrorMessage(T domain);
}
