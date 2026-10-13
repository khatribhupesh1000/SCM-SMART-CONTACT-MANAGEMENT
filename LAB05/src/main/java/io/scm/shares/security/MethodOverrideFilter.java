package io.scm.shares.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodOverrideFilter implements Filter {
    private final boolean enabled;
    public MethodOverrideFilter(@Value("${scm.method-override.enabled:false}") boolean enabled) { this.enabled = enabled; }
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        String override = http.getHeader("X-HTTP-Method-Override");
        if (enabled && "POST".equalsIgnoreCase(http.getMethod()) && override != null
                && ("PATCH".equalsIgnoreCase(override) || "DELETE".equalsIgnoreCase(override))) {
            HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(http) {
                @Override public String getMethod() { return override.toUpperCase(); }
            };
            chain.doFilter(wrapped, response);
        } else chain.doFilter(request, response);
    }
}
