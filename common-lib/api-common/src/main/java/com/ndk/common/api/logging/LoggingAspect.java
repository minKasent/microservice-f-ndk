package com.ndk.common.api.logging;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  @Around("@annotation(LogExecutionTime) || @within(LogExecutionTime)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Class<?> targetClass = joinPoint.getTarget().getClass();

    Logger log = LoggerFactory.getLogger(targetClass);

    LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
    if (annotation == null) {
      annotation = targetClass.getAnnotation(LogExecutionTime.class);
    }

    long warnThreshold = annotation != null ? annotation.warnThreshold() : 1000;
    boolean includeArgs = annotation != null && annotation.includeArgs();

    String methodName = method.getName();
    String className = targetClass.getSimpleName();

    long startTime = System.currentTimeMillis();

    try {
      Object result = joinPoint.proceed();

      long duration = System.currentTimeMillis() - startTime;

      if (duration > warnThreshold) {
        if (includeArgs) {
          log.warn("Slow execution - {}.{} completed in {}ms (threshold: {}ms), Args: {}",
              className, methodName, duration, warnThreshold,
              Arrays.toString(joinPoint.getArgs()));
        } else {
          log.warn("Slow execution - {}.{} completed in {}ms (threshold: {}ms)",
              className, methodName, duration, warnThreshold);
        }
      } else {
        log.debug("Method execution - {}.{} completed in {}ms",
            className, methodName, duration);
      }

      return result;

    } catch (Throwable e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method execution failed - {}.{} failed after {}ms, Error: {}",
          className, methodName, duration, e.getMessage());
      throw e;
    }
  }
}
