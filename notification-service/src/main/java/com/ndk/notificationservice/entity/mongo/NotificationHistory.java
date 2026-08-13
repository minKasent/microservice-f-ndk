package com.ndk.notificationservice.entity.mongo;

import com.ndk.notificationservice.enums.NotificationChannel;
import com.ndk.notificationservice.enums.NotificationProvider;
import com.ndk.notificationservice.enums.NotificationStatus;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {
  @Id
  private String id;

  @Indexed(unique = true)
  private String notificationId;

  @Indexed
  private NotificationChannel channel;

  @Indexed
  private String recipient;

  private String subject;
  private String content;
  private String templateId;
  private Map<String, Object> templateData;

  @Indexed
  private NotificationStatus status;

  private NotificationProvider provider;
  private Map<String, Object> metadata;

  private Date sentAt;
  private Date failedAt;
  private String errorMessage;

  @Builder.Default
  private Integer retryCount = 0;

  @Builder.Default
  private Integer maxRetries = 3;

  @Indexed
  private Date nextRetryAt;

  @Indexed
  private Date createdAt;
  private Date updatedAt;
}
