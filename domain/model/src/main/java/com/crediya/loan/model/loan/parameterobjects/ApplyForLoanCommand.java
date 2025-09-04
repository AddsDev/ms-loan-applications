package com.crediya.loan.model.loan.parameterobjects;

import com.crediya.loan.model.common.ownership.OwnableCommand;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;

import java.math.BigDecimal;

public record ApplyForLoanCommand(Document document, Email email, BigDecimal amount, int termInMonths, LoanType type) implements OwnableCommand {
    @Override
    public String ownerEmail() {
        return email.value();
    }
}
