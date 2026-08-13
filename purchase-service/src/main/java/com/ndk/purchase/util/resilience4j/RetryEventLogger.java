package com.ndk.purchase.util.resilience4j;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryEventLogger {

  private final RetryRegistry retryRegistry;

  public RetryEventLogger(RetryRegistry retryRegistry) {
    this.retryRegistry = retryRegistry;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void registerEventLogger() {
    retryRegistry.getAllRetries().forEach(retry -> retry.getEventPublisher()
        .onRetry(event -> {
          assert event.getLastThrowable() != null;
          log.warn("Retry '{}' - Attempt #{} after {}ms. Reason: {}",
              event.getName(),
              event.getNumberOfRetryAttempts(),
              event.getWaitInterval().toMillis(),
              event.getLastThrowable().getClass().getSimpleName() + ": "
                  + event.getLastThrowable().getMessage()
          );
        })
        .onSuccess(event -> {
          if (event.getNumberOfRetryAttempts() > 0) {
            log.info("Retry '{}' succeeded after {} attempt(s)",
                event.getName(),
                event.getNumberOfRetryAttempts()
            );
          }
        })
        .onError(event -> {
          assert event.getLastThrowable() != null;
          log.error("Retry '{}' FAILED after {} attempt(s). Final error: {}",
              event.getName(),
              event.getNumberOfRetryAttempts(),
              event.getLastThrowable().getMessage()
          );
        }));
  }
}
