package com.ndk.notificationservice.scheduler;

import com.ndk.notificationservice.entity.mongo.NotificationHistory;
import com.ndk.notificationservice.enums.NotificationStatus;
import com.ndk.notificationservice.repository.mongo.NotificationHistoryRepository;
import com.ndk.notificationservice.service.NotificationService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class RetryScheduler {
  private final NotificationHistoryRepository historyRepository;
  private final NotificationService notificationService;
  private final Executor customExecutor;

  @Scheduled(fixedDelay = 10000)
  public void processRetryQueue() {
    List<NotificationHistory> retryable = historyRepository
        .findRetryable(NotificationStatus.RETRY);
    log.info("Found {} retryable notifications", retryable.size());
    if (retryable.isEmpty()) {
      return;
    }
    CompletableFuture.runAsync(
        () -> notificationService.processRetry(retryable),
        customExecutor
    );
  }
}