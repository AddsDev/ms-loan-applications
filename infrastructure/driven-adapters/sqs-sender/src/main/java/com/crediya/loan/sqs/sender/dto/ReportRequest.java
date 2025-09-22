package com.crediya.loan.sqs.sender.dto;

import java.math.BigDecimal;

public record ReportRequest(
        BigDecimal amount, String loanId, String approvedAt
) {
}
