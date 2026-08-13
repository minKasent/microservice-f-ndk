package com.ndk.common.api.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to log method execution time using AOP.
 * Apply to controller or service methods to automatically log duration.
 *
 * Usage:
 * - On method: @LogExecutionTime
 * - On class: @LogExecutionTime (applies to all public methods)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {

  /**
   * Threshold in milliseconds. Log as WARN if execution exceeds this value.
   * Default: 1000ms (1 second)
   */
  long warnThreshold() default 1000;

  /**
   * Whether to include method arguments in log (be careful with sensitive data).
   * Default: false
   */
  boolean includeArgs() default false;
}
