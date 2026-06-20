package com.dmt.backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final int MAX_PAYLOAD_LENGTH = 4_000;
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return EXCLUDED_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = resolveRequestId(request);

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(
                        request,
                        MAX_PAYLOAD_LENGTH);
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        log.info(
                "Incoming request requestId={} method={} uri={} query={} clientIp={}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr()
        );

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception exception) {
            log.error(
                    "Request failed requestId={} method={} uri={} error={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    exception.getMessage(),
                    exception
            );

            throw exception;
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;

            log.info(
                    "Outgoing response requestId={} method={} uri={} status={} durationMs={} requestBody={} responseBody={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    wrappedResponse.getStatus(),
                    durationMs,
                    getPayload(wrappedRequest.getContentAsByteArray()),
                    getPayload(wrappedResponse.getContentAsByteArray())
            );

            wrappedResponse.copyBodyToResponse();
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {

        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return requestId;
    }

    private String getPayload(byte[] content) {

        if (content == null || content.length == 0) {
            return "";
        }

        String payload = new String(content, StandardCharsets.UTF_8);
        String sanitizedPayload = maskSensitiveValues(payload);

        if (sanitizedPayload.length() <= MAX_PAYLOAD_LENGTH) {
            return sanitizedPayload;
        }

        return sanitizedPayload.substring(0, MAX_PAYLOAD_LENGTH)
                + "...[truncated]";
    }

    private String maskSensitiveValues(String payload) {

        return payload
                .replaceAll(
                        "(?i)(\"password\"\\s*:\\s*\")([^\"]+)(\")",
                        "$1****$3")
                .replaceAll(
                        "(?i)(\"token\"\\s*:\\s*\")([^\"]+)(\")",
                        "$1****$3")
                .replaceAll(
                        "(?i)(\"secret\"\\s*:\\s*\")([^\"]+)(\")",
                        "$1****$3")
                .replaceAll(
                        "(?i)(Bearer\\s+)[A-Za-z0-9._\\-]+",
                        "$1****");
    }
}
