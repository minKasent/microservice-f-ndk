package com.ndk.notificationservice.kafka.consumer;

import com.ndk.notificationservice.kafka.message.NotificationMessage;
import com.ndk.notificationservice.service.NotificationDispatcher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
  private final NotificationDispatcher dispatcher;
  
  @KafkaListener(
      topics = "notification.request",
      groupId = "notification-consumer-group",
      concurrency = "3"
  )
  public void consume(NotificationMessage messages, Acknowledgment ack) {
    
    try {
      dispatcher.dispatch(messages);
      ack.acknowledge();
      log.info("Notification dispatched successfully : [{}], Channel: [{}]", messages.getNotificationId(), messages.getChannel());

    } catch (Exception e) {
      log.error("Error processing notification : [{}], Error: [{}]", messages.getNotificationId(), e.getMessage());
    }
  }
}
