package com.crediya.loan.model.report;

import java.math.BigDecimal;

public record ReportEvent(
        String eventName, Integer eventVersion,
        BigDecimal amount, String loanId, String approvedAt
) {
    public static ReportEvent register(BigDecimal amount, String loanId, String approvedAt) {
        return new ReportEvent("ReportRegistered", 1, amount, loanId, approvedAt);
    }
}
