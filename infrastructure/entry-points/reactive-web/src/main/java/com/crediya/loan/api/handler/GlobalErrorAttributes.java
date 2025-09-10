package com.crediya.loan.api.handler;

import com.crediya.loan.model.common.exceptions.AuthenticationException;
import com.crediya.loan.model.common.exceptions.DomainException;
import com.crediya.loan.model.common.exceptions.ExternalServiceException;
import com.crediya.loan.model.common.exceptions.ValidationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebInputException;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    private static final String KEY_STATUS = "status";
    private static final String KEY_CODE = "code";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_PATH = "path";

    private static final String CODE_INTERNAL_ERROR = "INTERNAL_SERVER_ERROR";
    private static final String CODE_INVALID_FORMAT = "INVALID_FORMAT";
    private static final String CODE_BAD_REQUEST = "BAD_REQUEST";
    private static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";

    private static final Map<Class<? extends Throwable>, Function<Throwable, Tuple2<HttpStatus, String>>> EXCEPTION_HANDLERS = new LinkedHashMap<>();

    static {
        // Manejo de errores personalizados y de dominio
        EXCEPTION_HANDLERS.put(ValidationException.class, ex -> Tuples.of(HttpStatus.UNPROCESSABLE_ENTITY, ((DomainException) ex).getCode().name()));
        EXCEPTION_HANDLERS.put(DomainException.class, ex -> Tuples.of(HttpStatus.BAD_REQUEST, ((DomainException) ex).getCode().name()));
        EXCEPTION_HANDLERS.put(ExternalServiceException.class, ex -> Tuples.of(HttpStatus.FORBIDDEN, ((DomainException) ex).getCode().name()));
        EXCEPTION_HANDLERS.put(AuthenticationException.class, ex -> Tuples.of(HttpStatus.UNAUTHORIZED, ((DomainException) ex).getCode().name()));

        // Manejo de errores estándar de Spring
        EXCEPTION_HANDLERS.put(WebExchangeBindException.class, ex -> Tuples.of(HttpStatus.BAD_REQUEST, CODE_BAD_REQUEST));
        EXCEPTION_HANDLERS.put(IllegalArgumentException.class, ex -> Tuples.of(HttpStatus.BAD_REQUEST, CODE_INVALID_FORMAT));
        EXCEPTION_HANDLERS.put(ConstraintViolationException.class, ex -> Tuples.of(HttpStatus.UNPROCESSABLE_ENTITY, CODE_INVALID_FORMAT));
        EXCEPTION_HANDLERS.put(NoResourceFoundException.class, ex -> Tuples.of(HttpStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED));
        EXCEPTION_HANDLERS.put(ServerWebInputException.class, ex -> {
            Throwable cause = ex.getCause();
            if (cause instanceof DecodingException de && de.getCause() instanceof InvalidFormatException) {
                return Tuples.of(HttpStatus.BAD_REQUEST, CODE_INVALID_FORMAT);
            }
            return Tuples.of(HttpStatus.BAD_REQUEST, CODE_INVALID_FORMAT);
        });
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable ex = getError(request);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TIMESTAMP, OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        map.put(KEY_PATH, request.path());

        Tuple2<HttpStatus, String> statusAndCode = getStatusAndCode(ex);
        String message = getErrorMessage(ex);

        map.put(KEY_STATUS, statusAndCode.getT1().value());
        map.put(KEY_CODE, statusAndCode.getT2());
        map.put(KEY_MESSAGE, message);

        return map;
    }

    private Tuple2<HttpStatus, String> getStatusAndCode(Throwable ex) {
        return EXCEPTION_HANDLERS.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(ex))
                .findFirst()
                .map(entry -> entry.getValue().apply(ex))
                .orElse(Tuples.of(HttpStatus.INTERNAL_SERVER_ERROR, CODE_INTERNAL_ERROR));
    }

    private String getErrorMessage(Throwable ex) {
        if (ex instanceof WebExchangeBindException bind) {
            List<String> errors = bind.getBindingResult().getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return "errors: " + String.join(", ", errors);
        } else if (ex instanceof ServerWebInputException swi) {
            if (swi.getCause() instanceof DecodingException de && de.getCause() instanceof InvalidFormatException ife) {
                String fieldName = ife.getPathReference().split("\"")[1];
                String invalidValue = ife.getValue().toString();
                String targetType = ife.getTargetType().getSimpleName();
                return String.format("The value '%s' for field '%s' is not a valid format for data type '%s'.", invalidValue, fieldName, targetType);
            }
            return "Format error in the request body.";
        } else if (ex instanceof NoResourceFoundException) {
            return "The requested resource was not found.";
        } else {
            return ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred on the server.";
        }
    }
}
