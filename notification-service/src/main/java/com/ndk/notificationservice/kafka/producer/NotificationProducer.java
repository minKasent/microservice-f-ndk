package com.ndk.notificationservice.kafka.producer;

import com.ndk.notificationservice.kafka.message.NotificationMessage;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
  private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
  private static final String TOPIC = "notification.request";

  public CompletableFuture<SendResult<String, NotificationMessage>> send(NotificationMessage message) {
    return kafkaTemplate.send(TOPIC, message.getRecipient(), message)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Failed to send notification to Kafka: {}", message.getNotificationId(), ex);
          } else {
            log.debug("Notification sent to Kafka: {} to partition {}",
                message.getNotificationId(), result.getRecordMetadata().partition());
          }
        });
  }
}
