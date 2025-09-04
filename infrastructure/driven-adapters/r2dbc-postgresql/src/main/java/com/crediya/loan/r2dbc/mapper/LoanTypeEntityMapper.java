package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.common.exceptions.ErrorCode;
import com.crediya.loan.model.common.exceptions.ValidationException;
import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.TermRange;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class LoanTypeEntityMapper {
    public LoanType toEnum(LoanTypeEntity entity) {
        if (entity == null || entity.getCode() == null)
            throw new ValidationException(ErrorCode.PERSISTENCE_ERROR, "LoanTypeEntity code is null");
        return LoanType.valueOf(entity.getCode());
    }

    public TermRange toTermRange(LoanTypeEntity e) {
        if (e == null || e.getMinTerm() == null || e.getMaxTerm() == null)
            throw new ValidationException(ErrorCode.PERSISTENCE_ERROR, "LoanTypeEntity term range is null");
        return new TermRange(e.getMinTerm(), e.getMaxTerm());
    }

    public AmountRange toAmountRange(LoanTypeEntity entity) {
        if (entity == null)
            throw new ValidationException(ErrorCode.PERSISTENCE_ERROR, "LoanTypeEntity is null");
        return new AmountRange(
                entity.getMinAmount(),
                entity.getMaxAmount()
        );
    }
}
