package com.ndk.identityservice.infra.trace;

import java.util.function.Supplier;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

public class SpanHelper {
  public static <T> T runInSpan(Span span, Supplier<T> action){
    try(Scope scope = span.makeCurrent()) {
      return action.get();
    } catch (Exception e) {
      span.recordException(e);
      span.setStatus(StatusCode.ERROR,e.getMessage());
      throw e;
    } finally {
      span.end();
    }
  }
}
