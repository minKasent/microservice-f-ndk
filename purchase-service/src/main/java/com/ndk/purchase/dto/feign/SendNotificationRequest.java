package com.ndk.purchase.dto.feign;

import com.ndk.purchase.enums.NotificationChannel;
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
  private NotificationChannel channel;
  private String recipient;
  private String subject;
  private String content;
  private String templateId;
  private Map<String, Object> templateData;
  private Map<String, Object> metadata;
  
  @Builder.Default
  private Integer priority = 0;
}
