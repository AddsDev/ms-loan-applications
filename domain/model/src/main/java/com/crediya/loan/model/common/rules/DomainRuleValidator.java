package com.crediya.loan.model.common.rules;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;

/**
 * The DomainRuleValidator class provides utility methods for validating domain rules.
 * It ensures that business rules encapsulated by the {@code DomainRule} interface
 * are adhered to during the validation process.
 */
public class DomainRuleValidator {
    private DomainRuleValidator() {
    }

    public static <T> void validate(T domain, DomainRule<T> rule) {
        if (!rule.isSatisfiedBy(domain)) {
            throw new ValidationException(ErrorCode.BUSINESS_RULE_VIOLATION, rule.getErrorMessage(domain));
        }
    }
}
