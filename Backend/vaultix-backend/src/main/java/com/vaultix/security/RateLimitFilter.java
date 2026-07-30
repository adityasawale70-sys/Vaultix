package com.vaultix.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.concurrent.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    // Per-IP deque of timestamps (ms) for login attempts
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> attempts = new ConcurrentHashMap<>();

    public RateLimitFilter() {}

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = extractClientIp(request);

        // Apply rate limiting only to the login endpoint (POST /api/auth/login)
        if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            long now = System.currentTimeMillis();
            ConcurrentLinkedDeque<Long> deque = attempts.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

            synchronized (deque) {
                // Remove timestamps outside the window from the front
                while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_SECONDS * 1000) {
                    deque.pollFirst();
                }

                if (deque.size() >= MAX_LOGIN_ATTEMPTS) {
                    response.setStatus(429);
                    response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("Too many login attempts. Please try again later.");
                    return;
                }

                // Record this attempt
                deque.addLast(now);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
