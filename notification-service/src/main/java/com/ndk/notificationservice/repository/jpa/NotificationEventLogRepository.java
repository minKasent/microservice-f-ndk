package com.ndk.notificationservice.repository.jpa;

import com.ndk.notificationservice.entity.jpa.NotificationEventLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventLogRepository extends JpaRepository<NotificationEventLog, Long> {
  List<NotificationEventLog> findByNotificationIdOrderByCreatedAtAsc(String notificationId);
}
