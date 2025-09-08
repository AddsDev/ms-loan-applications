package com.crediya.loan.model.loan.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Finance {
    private Finance() {}

    public static BigDecimal calculateMonthlyInterestRate(BigDecimal principal, Integer termInMonths, BigDecimal rateAnnualPercent) {
        if(principal == null || termInMonths <= 0 || rateAnnualPercent == null) return null;

        var monthlyRate = rateAnnualPercent
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10,RoundingMode.HALF_UP); // i
        if(monthlyRate.compareTo(BigDecimal.ZERO) == 0) return principal.divide(BigDecimal.valueOf(termInMonths), 2, RoundingMode.HALF_UP);

        // i = monthlyRate
        // laon = P * i / (1 - (1+i)^-n)

        var one = BigDecimal.ONE;
        var denominator = one.subtract(pow(one.add(monthlyRate), -termInMonths));

        return principal.multiply(monthlyRate).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal pow(BigDecimal base, int exponent) {
        // 1/(base^abs(exp))
        if(exponent == 0) return BigDecimal.ONE;
        boolean negative = exponent < 0;

        var e = Math.abs(exponent);
        var result = BigDecimal.ONE;

        for(int i = 0; i < e; i++) result = result.multiply(base).setScale(10, RoundingMode.HALF_UP);
        return negative ? BigDecimal.ONE.divide(result, 10, RoundingMode.HALF_UP) : result;
    }
}
