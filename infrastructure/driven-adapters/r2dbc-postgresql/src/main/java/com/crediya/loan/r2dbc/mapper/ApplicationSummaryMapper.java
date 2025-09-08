package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.loan.ApplicationSummary;
import com.crediya.loan.model.loan.policy.Finance;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Component
public class ApplicationSummaryMapper {
    public ApplicationSummary toSummary(Row row) {
        var amount = row.get("amount", java.math.BigDecimal.class);
        var term   = row.get("term", Integer.class);
        var rate   = row.get("termInMonths", java.math.BigDecimal.class); // anual %

        return new ApplicationSummary(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                row.get("email", String.class),
                row.get("document", String.class),
                null, // Get from validation Services - name
                row.get("loan_type", String.class),
                row.get("status", String.class),
                amount,
                term,
                rate,
                null, // Get from validation Services - baseSalary
                Finance.calculateMonthlyInterestRate(amount, term, rate),
                row.get("createdAt", OffsetDateTime.class)
        );
    }
}
