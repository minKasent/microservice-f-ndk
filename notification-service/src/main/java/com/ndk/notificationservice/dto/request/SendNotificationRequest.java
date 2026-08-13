package com.ndk.notificationservice.dto.request;

import com.ndk.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
  @NotNull(message = "{notification.channel.notNull}")
  private NotificationChannel channel;

  @NotBlank(message = "{notification.recipient.notBlank}")
  private String recipient;

  private String subject;
  private String content;
  private String templateId;
  private Map<String, Object> templateData;
  private Map<String, Object> metadata;

  @Min(value = 0, message = "{notification.priority.min}")
  @Max(value = 10, message = "{notification.priority.max}")
  @Builder.Default
  private Integer priority = 0;
}
