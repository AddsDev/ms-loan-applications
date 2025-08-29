package com.crediya.loan.api.handler;

import com.crediya.loan.api.model.HttpError;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Order(-2)
@Component
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {
    public GlobalExceptionHandler(GlobalErrorAttributes errorAttributes, ApplicationContext applicationContext) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(ServerCodecConfigurer.create().getWriters());
        super.setMessageReaders(ServerCodecConfigurer.create().getReaders());
    }


    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> attrs = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        int status = (int) attrs.getOrDefault("status", 500);
        HttpError body = new HttpError(
                status,
                String.valueOf(attrs.get("code")),
                String.valueOf(attrs.get("message")),
                String.valueOf(attrs.get("timestamp")),
                request.path()
        );
        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON).bodyValue(body);
    }
}
