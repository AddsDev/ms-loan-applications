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
@Table("ms_applications.loan_types")
public class LoanTypeEntity {
    @Id
    @Column("loan_type_id")
    private UUID loanTypeId;

    @Column("code") // CONSUMER, BUSINESS, MORTGAGE
    private String code;

    @Column("name")
    private String name;

    @Column("min_amount")
    private BigDecimal minAmount;

    @Column("max_amount")
    private BigDecimal maxAmount;

    @Column("interest_rate")
    private BigDecimal interestRate; // NUMERIC(5,2) - porcentaje anual

    @Column("min_term")
    private Integer minTerm;

    @Column("max_term")
    private Integer maxTerm;

    @Column("automatic_validation")
    private boolean automaticValidation;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
