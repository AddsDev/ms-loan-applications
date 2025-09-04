package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.loan.LoanType;
import com.crediya.loan.model.loan.policy.AmountRange;
import com.crediya.loan.model.loan.policy.LoanPolicies;
import com.crediya.loan.model.loan.policy.TermRange;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class LoanPoliciesMapper {
    private final LoanTypeEntityMapper typeMapper;

    public LoanPoliciesMapper(LoanTypeEntityMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public LoanPolicies fromEntities(List<LoanTypeEntity> types, Map<LoanType, TermRange> termRanges) {
        Map<LoanType, AmountRange> amounts = new EnumMap<>(LoanType.class);

        for (LoanTypeEntity e : types) {
            LoanType t = typeMapper.toEnum(e);
            amounts.put(t, typeMapper.toAmountRange(e));
        }

        return new LoanPolicies(amounts, termRanges);
    }
}
