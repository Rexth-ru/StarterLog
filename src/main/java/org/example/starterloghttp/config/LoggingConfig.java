package org.example.starterloghttp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.example.starterloghttp.enums.LogLevel;
import org.example.starterloghttp.properties.HttpLoggingProperties;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.Function;

@Slf4j
public class LoggingConfig {

    private final HttpLoggingProperties properties;

    public LoggingConfig(final HttpLoggingProperties properties) {
        this.properties = properties;
    }

    public void logHttpRequest(HttpServletRequest request) throws IOException {

        logInfo("HTTP Request Method: " + request.getMethod());

        logInfo("HTTP Request URI: " + request.getRequestURI());

        if (properties.isLogRequestHeaders()) {
            logHttpRequestHeader(request);
        }
        if (properties.isLogRequestBody()) {
            logHttpRequestBody(request);
        }
    }

    public void logHttpResponse(final ProceedingJoinPoint joinPoint, final ContentCachingResponseWrapper responseWrapper, final HttpServletResponse response) {
        logInfo("After returning method: " + joinPoint.getSignature().getName());
        logInfo("Http status: " + response.getStatus());

        if (properties.isLogResponseHeaders()) {
            logResponseHeader(responseWrapper);
        }

        if (properties.isLogResponseBody()) {
            logResponseBody(responseWrapper);
        }
    }

    public void logInfo(String message) {
        if (!properties.getLevel().equals(LogLevel.ERROR)) {
            LoggingEventBuilder eventBuilder = log.makeLoggingEventBuilder(LogLevel.toSlf4jLevel(properties.getLevel()));
            eventBuilder.log(message);
        }
    }

    public void logError(String message) {
        if (properties.getLevel().equals(LogLevel.ERROR)) {
            LoggingEventBuilder eventBuilder = log.makeLoggingEventBuilder(LogLevel.toSlf4jLevel(properties.getLevel()));
            eventBuilder.log(message);
        }
        if (!properties.getLevel().equals(LogLevel.ERROR)) {
            log.error(message);
        }
    }

    private void logHttpRequestHeader(final HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logInfo("HTTP Request Header - " + headerName + ": " + request.getHeader(headerName));
        }
    }

    private void logHttpRequestBody(final HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        logInfo("HTTP Request Body: " + requestBody);
    }

    private void logResponseBody(ContentCachingResponseWrapper response) {

        logInfo("Response: " + captureResponseBody(response));
    }

    private void logResponseHeader(ContentCachingResponseWrapper response) {

        logInfo("Response new: " + headersToString(response.getHeaderNames(), response::getHeader));
    }

    private String headersToString(final Collection<String> headerNames, final Function<String, String> headerValueResolver) {
        StringBuilder builder = new StringBuilder();
        for (String headerName : headerNames) {
            String header = headerValueResolver.apply(headerName);
            builder.append("%s=%s".formatted(headerName, header)).append("\n");
        }
        return builder.toString();
    }

    private String captureResponseBody(final ContentCachingResponseWrapper responseWrapper) {
        byte[] responseBodyBytes = responseWrapper.getContentAsByteArray();
        return new String(responseBodyBytes, StandardCharsets.UTF_8);
    }
}
