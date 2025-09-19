package com.crediya.loan.r2dbc.query;

public class SqlFragments {
    private SqlFragments() {
    }

    public static final String SELECT_COLUMNS = """
            a.application_id as id, a.email as email, a.identity_document as document, a.name as applicantName, a.base_salary as baseSalary,
            lt.code as loan_type,
            a.amount as amount, a.term as term,
            lt.interest_rate as termInMonths,
            s.name as status,
            a.created_at as createdAt
            """;
    public static final String SELECT_FROM = """
            FROM applications a
            JOIN statuses s ON s.status_id = a.status_id
            JOIN loan_types lt ON lt.loan_type_id = a.loan_type_id
            """;

    public static String queryFilterSql(ApplicationQuerySpec spc) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT ")
                .append(SELECT_COLUMNS).append(" ")
                .append(SELECT_FROM).append(" ")
                .append("WHERE ")
                .append(spc.whereClause()).append(" ");
        spc.orderByClause().ifPresent(order -> sql.append(" ORDER BY ")
                .append(order.column())
                .append(order.descending() ? " DESC " : " ASC ")
        );
        if (spc.pageSize().isPresent() && spc.offset().isPresent()) {
            sql.append("LIMIT :pageSize OFFSET :offset");
        }
        return sql.toString();
    }

    public static String countSql(ApplicationQuerySpec spc) {
        return "SELECT COUNT(1) as count " + SELECT_FROM +
                " WHERE " + spc.whereClause();
    }
}
