package com.ndk.notificationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Identity-service compatible payload for POST /internal/api/v1/notifications.
 * Fields: content, to, channel (string enum name, e.g. EMAIL).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InternalSendNotificationRequest {
  private String content;
  private String to;
  private String channel;
}
