package io.scm.shares.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class ApiPolicyFilter extends OncePerRequestFilter {
    private final String token;
    private final String allowedOrigin;
    private final int limit;
    private final Map<String, ConcurrentLinkedDeque<Long>> hits = new ConcurrentHashMap<>();

    public ApiPolicyFilter(@Value("${scm.demo.bearer-token:scm-assignment5-demo-token}") String token,
            @Value("${scm.cors.allowed-origin:*}") String allowedOrigin,
            @Value("${scm.rate-limit-per-minute:100}") int limit) {
        this.token = token; this.allowedOrigin = allowedOrigin; this.limit = limit;
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/v1/");
    }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        res.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, Idempotency-Key, If-Match, If-None-Match, X-Client-ID, X-HTTP-Method-Override");
        res.setHeader("Vary", "Origin, Accept, Accept-Encoding");
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) { chain.doFilter(req, res); return; }
        String accept = req.getHeader("Accept");
        if (accept != null && !accept.contains("*/*") && !accept.contains("application/json") && !accept.contains("application/*")) {
            sendProblem(res, 406, "Not Acceptable", "This API returns application/json."); return;
        }
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ") || auth.substring(7).trim().isEmpty() || !token.equals(auth.substring(7).trim())) {
            sendProblem(res, 401, "Unauthorized", "A valid Authorization: Bearer token is required."); return;
        }
        String client = req.getHeader("X-Client-ID");
        if (client == null || client.isBlank()) client = req.getRemoteAddr();
        long now = Instant.now().toEpochMilli();
        ConcurrentLinkedDeque<Long> queue = hits.computeIfAbsent(client, k -> new ConcurrentLinkedDeque<>());
        while (!queue.isEmpty() && queue.peekFirst() < now - 60_000) queue.pollFirst();
        if (queue.size() >= limit) {
            res.setHeader("Retry-After", "30");
            res.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            res.setHeader("X-RateLimit-Remaining", "0");
            sendProblem(res, 429, "Too Many Requests", "Per-client request budget exceeded."); return;
        }
        queue.addLast(now);
        res.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - queue.size())));
        chain.doFilter(req, res);
    }
    private void sendProblem(HttpServletResponse res, int status, String title, String detail) throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/problem+json;charset=UTF-8");
        if (status == 429 || status == 503) res.setHeader("Retry-After", "30");
        String type = status == 401 ? "authentication-required"
                : status == 406 ? "not-acceptable"
                : status == 429 ? "rate-limit-exceeded" : "request-rejected";
        String safeDetail = detail.replace("\\", "\\\\").replace("\"", "'")
                .replace("\\r", " ").replace("\\n", " ");
        res.getWriter().write("{\"type\":\"" + type + "\",\"title\":\"" + title
                + "\",\"status\":" + status + ",\"detail\":\"" + safeDetail + "\",\"errors\":[]}");
    }
}
