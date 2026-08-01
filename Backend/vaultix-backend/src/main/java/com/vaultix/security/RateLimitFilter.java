package com.vaultix.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> attempts =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = extractClientIp(request);

        if ("/api/auth/login".equals(path)
                && "POST".equalsIgnoreCase(request.getMethod())) {

            long now = System.currentTimeMillis();

            ConcurrentLinkedDeque<Long> deque =
                    attempts.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

            synchronized (deque) {

                while (!deque.isEmpty()
                        && now - deque.peekFirst() > WINDOW_SECONDS * 1000) {
                    deque.pollFirst();
                }

                if (deque.size() >= MAX_LOGIN_ATTEMPTS) {
                    response.setStatus(429);
                    response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("Too many login attempts. Please try again later.");
                    return;
                }

                deque.addLast(now);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {

        String xff = request.getHeader("X-Forwarded-For");

        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}