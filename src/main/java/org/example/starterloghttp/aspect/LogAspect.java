package org.example.starterloghttp.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.starterloghttp.config.LoggingConfig;
import org.example.starterloghttp.exception.AdviceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.List;
import java.util.Objects;

@Slf4j
@Aspect
public class LogAspect {

    private final LoggingConfig loggingConfig;

    public LogAspect(final LoggingConfig loggingConfig) {
        this.loggingConfig = loggingConfig;
    }

    @Before("@annotation(org.example.starterloghttp.aspect.annotation.LogExecution)")
    public void logBefore(JoinPoint joinPoint) {
        String message = "Before calling method: " + joinPoint.getSignature().getName();
        loggingConfig.logInfo(message);
    }

    @AfterThrowing(pointcut = "@annotation(org.example.starterloghttp.aspect.annotation.LogException)", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        String message = "Exception throwing in method: " + joinPoint.getSignature().getName() + " - " + e.getMessage();
        loggingConfig.logError(message);
    }

    @AfterReturning(pointcut = "@annotation(org.example.starterloghttp.aspect.annotation.LogResult)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String message = "After returning method: " + joinPoint.getSignature().getName() + " Result: " + (result instanceof List ? result : result.toString());
        loggingConfig.logInfo(message);
    }

    @Around(value = "@annotation(org.example.starterloghttp.aspect.annotation.LogTracking)")
    public Object logAround(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            String message = "Exception throwing in method: " + joinPoint.getSignature().getName() + " - " + e.getMessage();
            loggingConfig.logError(message);
            throw new AdviceException("Around exception", e);
        }
        long endTime = System.currentTimeMillis();
        String message = "Method " + joinPoint.getSignature().getName() + " working " + (endTime - startTime) + " ms";
        loggingConfig.logInfo(message);
        return result;
    }

    @Around(value = "@annotation(org.example.starterloghttp.aspect.annotation.LogHttp)")
    public Object logHttpAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Object result;
        try {
            loggingConfig.logHttpRequest(request);

            result = joinPoint.proceed();

            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(Objects.requireNonNull(response));

            if (result instanceof ResponseEntity) {
                loggingConfig.logHttpResponse(joinPoint, responseWrapper, response);
            } else {
                loggingConfig.logInfo("Response: " + result + " Http status: " + response.getStatus());
            }
            responseWrapper.copyBodyToResponse();

        } catch (Throwable throwable) {
            loggingConfig.logError("HTTP Error: " + throwable.getMessage());
            throw throwable;
        }
        return result;
    }
}
