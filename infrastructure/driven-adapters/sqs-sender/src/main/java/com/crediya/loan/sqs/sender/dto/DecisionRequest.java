package com.crediya.loan.sqs.sender.dto;


public record DecisionRequest
        (
                String eventName,
                Integer eventVersion,
                String loanId,
                String decision,
                String email,
                String reason,
                String createdAt
        ) {
}
