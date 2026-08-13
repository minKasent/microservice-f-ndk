package com.ndk.notificationservice.dto.request;

import com.ndk.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendBulkNotificationRequest {
  @NotNull(message = "{notification.channel.notNull}")
  private NotificationChannel channel;

  @NotEmpty(message = "{notification.recipients.notEmpty}")
  @Size(max = 10000, message = "{notification.recipients.maxSize}")
  private List<String> recipients;

  private String subject;
  private String content;
  private String templateId;
  private Map<String, Object> templateData;
}
