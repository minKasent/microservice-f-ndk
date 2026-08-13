package com.ndk.notificationservice.dto.response;

import com.ndk.notificationservice.enums.NotificationChannel;
import com.ndk.notificationservice.enums.NotificationProvider;
import com.ndk.notificationservice.enums.NotificationStatus;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
  private String notificationId;
  private NotificationChannel channel;
  private String recipient;
  private String subject;
  private String content;
  private NotificationStatus status;
  private NotificationProvider provider;
  private Integer retryCount;
  private Date sentAt;
  private Date createdAt;
  private Map<String, Object> metadata;
}
