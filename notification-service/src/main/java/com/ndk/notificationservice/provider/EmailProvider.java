package com.ndk.notificationservice.provider;

import com.ndk.notificationservice.entity.mongo.NotificationHistory;
import com.ndk.notificationservice.enums.NotificationStatus;
import com.ndk.notificationservice.kafka.message.NotificationMessage;
import com.ndk.notificationservice.repository.mongo.NotificationHistoryRepository;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProvider implements NotificationProvider {
  private final WebClient webClient;
  private final NotificationHistoryRepository historyRepository;
  private final RateLimiterRegistry rateLimiterRegistry;

  @Override
  public void send(NotificationMessage message) {
    RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("email");

    rateLimiter.executeRunnable(() -> {
      try {
        NotificationHistory history = historyRepository
            .findByNotificationId(message.getNotificationId())
            .orElseGet(() -> saveHistory(message, NotificationStatus.PENDING));

        sendViaSendGrid(message);

        history.setStatus(NotificationStatus.SENT);
        history.setSentAt(new Date());
        history.setUpdatedAt(new Date());
        historyRepository.save(history);

        log.info("Email sent successfully: {}", message.getNotificationId());
      } catch (Exception e) {
        handleFailure(message, e);
      }
    });
  }

  private void sendViaSendGrid(NotificationMessage message) {
    log.info("================================================================================");
    log.info(">>>>> [EMAIL VERIFICATION DISPATCHED]");
    log.info(">>>>> To: {}", message.getRecipient());
    log.info(">>>>> Subject: {}", message.getSubject());
    log.info(">>>>> Verification Code / Content: {}", message.getContent());
    log.info("================================================================================");
    // TODO: Implement actual SendGrid / SMTP API integration
  }

  private NotificationHistory saveHistory(NotificationMessage message, NotificationStatus status) {
    NotificationHistory history = NotificationHistory.builder()
        .notificationId(message.getNotificationId() != null
            ? message.getNotificationId() : UUID.randomUUID().toString())
        .channel(message.getChannel())
        .recipient(message.getRecipient())
        .subject(message.getSubject())
        .content(message.getContent())
        .templateId(message.getTemplateId())
        .templateData(message.getTemplateData())
        .status(status)
        .provider(com.ndk.notificationservice.enums.NotificationProvider.SENDGRID)
        .metadata(message.getMetadata())
        .retryCount(0)
        .maxRetries(3)
        .createdAt(new Date())
        .updatedAt(new Date())
        .build();

    return historyRepository.save(history);
  }

  private void handleFailure(NotificationMessage message, Exception e) {
    log.error("Failed to send Email: {}", message.getNotificationId(), e);

    NotificationHistory history = historyRepository
        .findByNotificationId(message.getNotificationId())
        .orElseGet(() -> saveHistory(message, NotificationStatus.FAILED));

    history.setStatus(isRetryable(e) ? NotificationStatus.RETRY : NotificationStatus.FAILED);
    history.setFailedAt(new Date());
    history.setErrorMessage(e.getMessage());
    history.setUpdatedAt(new Date());

    if (isRetryable(e)) {
      long delayMs = (long) (1000 * Math.pow(2, history.getRetryCount()));
      history.setNextRetryAt(new Date(System.currentTimeMillis() + Math.min(delayMs, 60000)));
    }

    historyRepository.save(history);
  }

  @Override
  public boolean isRetryable(Exception exception) {
    String message = exception.getMessage();
    return message != null
        && (message.contains("timeout")
        || message.contains("429")
        || message.contains("503"));
  }
}
