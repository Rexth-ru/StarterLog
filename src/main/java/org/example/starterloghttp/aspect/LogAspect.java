package org.example.starterloghttp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.starterloghttp.exception.AdviceException;
import org.example.starterloghttp.properties.HttpLoggingProperties;

import java.util.List;

@Slf4j
@Aspect
public class LogAspect {

    private final HttpLoggingProperties properties;

    public LogAspect(final HttpLoggingProperties properties) {
        this.properties = properties;
    }

    @Before("@annotation(org.example.starterloghttp.aspect.annotation.LogExecution)")
    public void logBefore(JoinPoint joinPoint) {
        if (properties.isInfoLevel()) {
            log.info("Before calling method: " + joinPoint.getSignature().getName());
        }
    }

    @AfterThrowing(pointcut = "@annotation(org.example.starterloghttp.aspect.annotation.LogException)")
    public void logAfterThrowing(JoinPoint joinPoint) {
        if (properties.isErrorLevel() || properties.isInfoLevel()) {
            log.error("Exception throwing in method: {}", joinPoint.getSignature().getName());
        }
    }

    @AfterReturning(pointcut = "@annotation(org.example.starterloghttp.aspect.annotation.LogResult)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (properties.isInfoLevel()) {
            log.info("After returning method: {}", joinPoint.getSignature().getName());
            if (result instanceof List) {
                log.info("Result: {}", (List<?>) result);
            } else {
                log.info("Result: {}", result);
            }
        }
    }

    @Around(value = "@annotation(org.example.starterloghttp.aspect.annotation.LogTracking)")
    public Object logAround(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        Object object;
        try {
            object = joinPoint.proceed();
        } catch (Throwable e) {
            if (properties.isErrorLevel() || properties.isInfoLevel()) {
                log.error("Exception in method: {}", joinPoint.getSignature().getName(), e);
            }
            throw new AdviceException("Around exception", e);
        }
        long endTime = System.currentTimeMillis();
        if (properties.isInfoLevel()) {
            log.info("Method {} working {} ms", joinPoint.getSignature().getName(), endTime - startTime);
        }
        return object;
    }
}