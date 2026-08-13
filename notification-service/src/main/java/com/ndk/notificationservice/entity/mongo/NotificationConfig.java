package com.ndk.notificationservice.entity.mongo;

import com.ndk.notificationservice.enums.NotificationChannel;
import com.ndk.notificationservice.enums.NotificationProvider;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationConfig {
  @Id
  private String id;

  private NotificationChannel channel;
  private NotificationProvider provider;
  private Map<String, String> config;
  private RateLimitConfig rateLimit;
  private RetryConfig retryConfig;

  @Builder.Default
  private Boolean isActive = true;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RateLimitConfig {
    private Integer maxPerSecond;
    private Integer maxPerMinute;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RetryConfig {
    private Integer maxRetries;
    private Long initialDelayMs;
    private Long maxDelayMs;
    private Double multiplier;
  }
}
