package com.ndk.purchase.dto.feign;

import com.ndk.purchase.enums.NotificationChannel;
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
  private String status;
  private String provider;
  private Integer retryCount;
  private Date sentAt;
  private Date createdAt;
  private Map<String, Object> metadata;
}
