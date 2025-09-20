package com.crediya.loan.r2dbc.query;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public interface ApplicationQuerySpec {
    String whereClause();
    Optional<OrderClause> orderByClause();
    Map<String, Object> parameters();
    default OptionalInt pageSize() { return OptionalInt.empty(); }
    default OptionalLong offset() { return OptionalLong.empty(); }

    record OrderClause(String column, boolean descending) { }
}
