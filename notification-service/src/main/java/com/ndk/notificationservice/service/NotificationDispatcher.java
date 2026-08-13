package com.ndk.notificationservice.service;

import com.ndk.notificationservice.kafka.message.NotificationMessage;
import com.ndk.notificationservice.provider.EmailProvider;
import com.ndk.notificationservice.provider.SmsProvider;
import com.ndk.notificationservice.provider.WebAppProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {
  private final SmsProvider smsProvider;
  private final EmailProvider emailProvider;
  private final WebAppProvider webAppProvider;

  public void dispatch(NotificationMessage message) {
    log.debug("Dispatching notification: {} via {}",
        message.getNotificationId(), message.getChannel());

    switch (message.getChannel()) {
      case SMS -> smsProvider.send(message);
      case EMAIL -> emailProvider.send(message);
      case WEB_APP -> webAppProvider.send(message);
      default -> log.error("Unknown channel: {}", message.getChannel());
    }
  }
}
