package com.manifactory.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.manifactory.backend.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final class WindowCounter {
        private volatile long windowStartEpochSec;
        private final AtomicInteger count = new AtomicInteger(0);
    }

    private final RateLimitProperties properties;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public RateLimitingFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = buildKey(request);
        int limit = resolveLimit(request);
        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }
        long now = Instant.now().getEpochSecond();
        long windowSeconds = properties.getWindowSeconds();

        WindowCounter counter = counters.computeIfAbsent(key, ignored -> {
            WindowCounter c = new WindowCounter();
            c.windowStartEpochSec = now;
            return c;
        });

        synchronized (counter) {
            if (now - counter.windowStartEpochSec >= windowSeconds) {
                counter.windowStartEpochSec = now;
                counter.count.set(0);
            }
            int current = counter.count.incrementAndGet();
            if (current > limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ApiError error = new ApiError(
                        java.time.OffsetDateTime.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "RateLimitExceeded",
                        "Too many requests. Please retry later.",
                        request.getRequestURI());
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String buildKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        return ip + "|" + path;
    }

    private int resolveLimit(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("/api/v1/auth/login".equals(path) && HttpMethod.POST.matches(request.getMethod())) {
            return properties.getLoginMaxRequests();
        }
        return properties.getMaxRequests();
    }
}
