package com.aryanhagat.authenticator.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // ConcurrentHashMap is thread-safe — multiple requests can hit this simultaneously
    // Key: "IP:endpoint" e.g. "192.168.1.1:/auth/login"
    // Value: the Bucket for that IP + endpoint combination
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!isRateLimitedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String bucketKey = clientIp + ":" + path;
        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createBucket(path));

        // tryConsume returns a ConsumptionProbe with details about the attempt
        io.github.bucket4j.ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add remaining requests header — tells client how many they have left
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));

            filterChain.doFilter(request, response);
        } else {
            // Add retry-after header — tells client when to try again (in seconds)
            long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("Retry-After", String.valueOf(waitForRefillSeconds));
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Too many requests. Please try again in "
                            + waitForRefillSeconds + " seconds.\"}"
            );
        }
    }

    // Define rate limit rules per endpoint
    private Bucket createBucket(String path) {
        Bandwidth limit;

        if (path.equals("/auth/login") || path.equals("/auth/login/2fa")) {
            // Strict: 5 requests per minute
            limit = Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(1))
                    .build();

        } else if (path.equals("/auth/signup")) {
            // Moderate: 3 requests per minute
            limit = Bandwidth.builder()
                    .capacity(3)
                    .refillIntervally(3, Duration.ofMinutes(1))
                    .build();

        } else if (path.equals("/2fa/verify")) {
            // Strict: 5 requests per minute
            limit = Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(1))
                    .build();

        } else {
            // Default for any other rate-limited path: 10 per minute
            limit = Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(1))
                    .build();
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // Which paths should be rate limited
    private boolean isRateLimitedPath(String path) {
        return path.startsWith("/auth/") || path.startsWith("/2fa/");
    }

    // Extract real client IP — handles proxies
    private String getClientIp(HttpServletRequest request) {

        // X-Forwarded-For header is set by proxies/load balancers
        // It contains the original client IP
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            // The first one is always the real client
            return xForwardedFor.split(",")[0].trim();
        }

        // No proxy — get IP directly from the connection
        return request.getRemoteAddr();
    }
}