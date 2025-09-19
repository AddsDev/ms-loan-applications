package com.crediya.loan.r2dbc.query;

import com.crediya.loan.model.loan.parameterobjects.ListApplicationsQueryCommand;
import com.crediya.loan.model.loan.valueobjects.SortSpec;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultApplicationQuerySpec implements ApplicationQuerySpec{
    private final String whereClause;
    private final Map<String, Object> params;
    private final OptionalInt pageSize;
    private final OptionalLong offset;
    private final Optional<OrderClause> orderByClause;

    public static DefaultApplicationQuerySpec from(ListApplicationsQueryCommand cmd, SortSpec sort) {
        StringBuilder sql = new StringBuilder("1=1"); // para poder encadenar AND
        Map<String, Object> params = new HashMap<>();
        if (cmd.statuses() != null && !cmd.statuses().isEmpty()) {
            sql.append(" AND s.name = ANY(:statuses) ");
            params.put("statuses", cmd.statuses().stream().map(Enum::name).toArray(String[]::new));
        } else {
            sql.append(" AND s.name = ANY(:statuses) ");
            params.put("statuses", new String[]{"PENDING", "REJECTED", "REVIEW_MANUAL"});
        }

        if (cmd.email() != null && !cmd.email().isBlank()) {
            sql.append(" AND a.email ILIKE :email ");
            params.put("email", "%" + cmd.email().trim() + "%");
        }

        if (cmd.document() != null && !cmd.document().isBlank()) {
            sql.append(" AND a.identity_document ILIKE :document ");
            params.put("document", "%" + cmd.document().trim() + "%");
        }

        if (cmd.loanTypeCode() != null && !cmd.loanTypeCode().isBlank()) {
            sql.append(" AND lt.code = :loanTypeCode ");
            params.put("loanTypeCode", cmd.loanTypeCode().trim());
        }

        Optional<OrderClause> order = mapSortSpec(sort);

        OptionalInt size = OptionalInt.of(cmd.size());
        OptionalLong off = OptionalLong.of(Math.max(cmd.page(), 0L) * cmd.size());

        return new DefaultApplicationQuerySpec(sql.toString(), params, size, off, order);
    }

    private static Optional<OrderClause> mapSortSpec(SortSpec sort) {
        if (sort == null || sort.property() == null) { return Optional.empty(); }
        String column = switch (sort.property()) {
            case "id" -> "a.id";
            case "email" -> "a.email";
            case "document" -> "a.identity_document";
            case "amount" -> "a.amount";
            case "term" -> "a.term";
            case "status" -> "s.name";
            case "createdAt" -> "a.created_at";
            case "loanType"  -> "lt.code";
            default -> null;
        };
        if (column == null) return Optional.empty();
        return Optional.of(new OrderClause(column, sort.descending()));
    }

    @Override
    public String whereClause() {
        return whereClause;
    }

    @Override
    public Optional<OrderClause> orderByClause() {
        return orderByClause;
    }

    @Override
    public Map<String, Object> parameters() {
        return Collections.unmodifiableMap(params);
    }

    @Override
    public OptionalInt pageSize() {
        return pageSize;
    }

    @Override
    public OptionalLong offset() {
        return offset;
    }
}
