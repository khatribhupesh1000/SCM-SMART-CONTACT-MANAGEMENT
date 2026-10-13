package io.scm.shares.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Ensures JSON and Problem Details responses explicitly advertise UTF-8. */
@ControllerAdvice
public class ApiContentTypeAdvice implements ResponseBodyAdvice<Object> {
    private static final MediaType JSON_UTF8 = MediaType.parseMediaType("application/json;charset=UTF-8");
    private static final MediaType PROBLEM_UTF8 = MediaType.parseMediaType("application/problem+json;charset=UTF-8");

    @Override public boolean supports(MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        if (body != null) {
            if (selectedContentType != null && "application".equals(selectedContentType.getType())
                    && "problem+json".equals(selectedContentType.getSubtype())) {
                response.getHeaders().setContentType(PROBLEM_UTF8);
            } else if (selectedContentType != null && "application".equals(selectedContentType.getType())
                    && "json".equals(selectedContentType.getSubtype())) {
                response.getHeaders().setContentType(JSON_UTF8);
            }
        }
        return body;
    }
}
