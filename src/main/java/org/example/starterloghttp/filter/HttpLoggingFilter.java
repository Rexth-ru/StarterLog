package org.example.starterloghttp.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.example.starterloghttp.properties.HttpLoggingProperties;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpLoggingFilter extends OncePerRequestFilter {
    private final HttpLoggingProperties properties;

    public HttpLoggingFilter(HttpLoggingProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        stopWatch.stop();

        if (properties.isInfoLevel()) {
            logRequestDetails(requestWrapper, responseWrapper);
        }

        responseWrapper.copyBodyToResponse();

        logHttpDetails(request, response, stopWatch.getTotalTimeMillis());
    }

    private void logRequestDetails(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper) {
        if (properties.isLogRequestHeaders()) {
            logRequestHeaders(requestWrapper);
        }

        if (properties.isLogResponseHeaders()) {
            logResponseHeaders(responseWrapper);
        }

        logRequestBody(requestWrapper);
        logResponseBody(responseWrapper);
    }

    private void logHttpDetails(HttpServletRequest request, HttpServletResponse response, long duration) {
        if (properties.isInfoLevel() && response.getStatus() < 400) {
            log.info("HTTP {} {} - {} - {}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        } else if (response.getStatus() >= 400) {
            log.error("HTTP {} {} - {} - {}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        }
    }

    private void logRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        log.info("Request headers: {}", headers);
    }

    private void logResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        log.info("Response headers: {}", headers);
    }

    private void logRequestBody(ContentCachingRequestWrapper requestWrapper) {
        if (requestWrapper.getContentAsByteArray().length > 0) {
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.info("Request body: {}", requestBody);
        }
    }

    private void logResponseBody(ContentCachingResponseWrapper responseWrapper) {
        if (responseWrapper.getContentAsByteArray().length > 0) {
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.info("Response body: {}", responseBody);
        }
    }
}
