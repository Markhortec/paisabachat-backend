package com.hamza.paisabachat.backend.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int AUTH_MAX_REQUESTS = 10;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        int maxRequests = path.contains("/auth/")
                ? AUTH_MAX_REQUESTS
                : MAX_REQUESTS_PER_MINUTE;

        String key = RATE_LIMIT_PREFIX + clientIp + ":" +
                (path.contains("/auth/") ? "auth" : "api");

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }

            if (count != null && count > maxRequests) {
                log.warn("Rate limit exceeded for IP: {} on path: {}",
                        clientIp, path);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"success\":false," +
                                "\"message\":\"Too many requests. Please slow down.\"," +
                                "\"errorCode\":\"RATE_LIMIT_EXCEEDED\"}");
                return;
            }
        } catch (Exception e) {
            log.error("Rate limit check failed — allowing request: {}",
                    e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}