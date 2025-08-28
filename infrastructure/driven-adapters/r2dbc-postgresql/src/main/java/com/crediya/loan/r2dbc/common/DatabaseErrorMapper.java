package com.crediya.loan.r2dbc.common;

import io.r2dbc.postgresql.api.PostgresqlException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.BadSqlGrammarException;

import java.util.function.Supplier;

public class DatabaseErrorMapper {

    private DatabaseErrorMapper() {}

    /**
     * Maps uniqueness violations (constraint UNIQUE) to a specific domain exception.
     *
     * @param error the original exception
     * @param duplicateEx a Supplier that constructs the domain exception (ex: () -> new DuplicateEmailException(..))
     * @return domain exception if it is a uniqueness violation, or the original otherwise
     */
    public static Throwable mapUniqueViolation(Throwable error, Supplier<? extends RuntimeException> duplicateEx) {
        Throwable root = unwrap(error);

        if (root instanceof DataIntegrityViolationException) {
            return duplicateEx.get();
        }

        if (root instanceof PostgresqlException pe && "23505".equals(pe.getErrorDetails().getCode())) {
            return duplicateEx.get();
        }

        if (root instanceof BadSqlGrammarException bge) {
            Throwable cause = unwrap(bge.getCause());
            if (cause instanceof PostgresqlException pe2 && "23505".equals(pe2.getErrorDetails().getCode())) {
                return duplicateEx.get();
            }
        }
        return error;
    }

    /**
     * Go through the chain of causes until you reach the "root cause."
     */
    public static Throwable unwrap(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }
}
