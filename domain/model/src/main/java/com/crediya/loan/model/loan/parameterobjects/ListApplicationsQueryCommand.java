package com.crediya.loan.model.loan.parameterobjects;

import com.crediya.loan.model.loan.ApplicationStatus;

import java.util.List;

/**
 * Query for listing applications.
 * @param page required
 * @param size required
 * @param sort optional (default: createdAt)
 * @param email optional
 * @param document optional
 * @param loanTypeCode optional
 */
public record ListApplicationsQueryCommand(
        int page, int size,
        String sort,
        List<ApplicationStatus> statuses,
        String email,
        String document,
        String loanTypeCode
) {
}
