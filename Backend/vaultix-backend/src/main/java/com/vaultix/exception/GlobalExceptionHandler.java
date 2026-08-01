package com.vaultix.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, Object> buildBody(HttpStatus status, String error,
                                          String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status",    status.value());
        body.put("error",     error);
        body.put("message",   message != null ? message : "");
        body.put("path",      path    != null ? path    : "");
        return body;
    }

    // ─── 400 Bad Request ─────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {

        log.debug("Business validation error [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(
                buildBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("field",   fieldError.getField());
                    m.put("message", fieldError.getDefaultMessage());
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> body = buildBody(
                HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more fields failed validation", req.getRequestURI());
        body.put("fieldErrors", errors);
        return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    // ─── 401 Unauthorized ────────────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {

        log.warn("Failed login attempt [{}]", req.getRequestURI());
        return new ResponseEntity<>(
                buildBody(HttpStatus.UNAUTHORIZED, "Unauthorized",
                        "Invalid email or password", req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest req) {

        log.warn("Unauthorized [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(
                buildBody(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.UNAUTHORIZED
        );
    }

    // ─── 403 Forbidden ───────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        log.warn("Access denied [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(
                buildBody(HttpStatus.FORBIDDEN, "Forbidden",
                        "You do not have permission to access this resource", req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(
            LockedException ex, HttpServletRequest req) {

        log.warn("Locked account login attempt [{}]", req.getRequestURI());
        return new ResponseEntity<>(
                buildBody(HttpStatus.FORBIDDEN, "Account Locked", ex.getMessage(), req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(
            DisabledException ex, HttpServletRequest req) {

        return new ResponseEntity<>(
                buildBody(HttpStatus.FORBIDDEN, "Account Disabled",
                        "Your account is not active. Please verify your email.", req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.FORBIDDEN
        );
    }

    // ─── 404 Not Found ───────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {

        log.debug("Resource not found [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(
                buildBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.NOT_FOUND
        );
    }

    // ─── 500 Internal Server Error ───────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(
            Exception ex, HttpServletRequest req) {

        // Log full stack trace INTERNALLY — NEVER expose it to the client
        log.error("Unhandled exception processing [{}]", req.getRequestURI(), ex);

        return new ResponseEntity<>(
                buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                        "An unexpected error occurred. Please try again later.",
                        req.getRequestURI()),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
