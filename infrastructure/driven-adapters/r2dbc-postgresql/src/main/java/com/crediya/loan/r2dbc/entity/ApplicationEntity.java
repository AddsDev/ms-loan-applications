package com.crediya.loan.r2dbc.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ms_applications.applications")
public class ApplicationEntity {
    @Id
    @Column("application_id")
    private UUID applicationId;

    @Column("amount")
    private BigDecimal amount;

    @Column("term")
    private Integer term; // en meses > 0

    @Column("email")
    private String email;

    @Column("advisor_email")
    private String advisorEmail;

    @Column("reason")
    private String reason;

    @Column("name")
    private String name;

    @Column("base_salary")
    private BigDecimal baseSalary;

    @Column("identity_document")
    private String identityDocument;

    @Column("status_id")
    private UUID statusId;

    @Column("loan_type_id")
    private UUID loanTypeId;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

}
