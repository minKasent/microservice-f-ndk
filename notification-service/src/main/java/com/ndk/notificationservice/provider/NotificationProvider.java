package com.ndk.notificationservice.provider;

import com.ndk.notificationservice.kafka.message.NotificationMessage;

public interface NotificationProvider {
  void send(NotificationMessage message);

  boolean isRetryable(Exception exception);
}
