package com.ndk.notificationservice.service;

import com.ndk.notificationservice.dto.request.SendBulkNotificationRequest;
import com.ndk.notificationservice.dto.request.SendNotificationRequest;
import com.ndk.notificationservice.dto.response.NotificationDto;
import com.ndk.notificationservice.entity.mongo.NotificationHistory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
  NotificationDto send(SendNotificationRequest request);

  void sendBulk(SendBulkNotificationRequest request);

  Page<NotificationDto> getHistory(String recipient, Pageable pageable);

  void processRetry(List<NotificationHistory> notificationHistories);
}
