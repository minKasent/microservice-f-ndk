package com.ndk.notificationservice.controller;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.api.response.ApiResponse;
import com.ndk.notificationservice.dto.request.InternalSendNotificationRequest;
import com.ndk.notificationservice.dto.request.SendNotificationRequest;
import com.ndk.notificationservice.enums.NotificationChannel;
import com.ndk.notificationservice.exception.ExceptionEnum;
import com.ndk.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Keeps identity-service Feign contract: POST /internal/api/v1/notifications
 * with body { content, to, channel }.
 */
@RestController
@RequestMapping("/internal/api/v1/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

  private final NotificationService notificationService;

  @PostMapping
  public ResponseEntity<ApiResponse<?>> sendNotification(
      @RequestBody InternalSendNotificationRequest requestDto) {
    NotificationChannel channel = resolveChannel(requestDto.getChannel());

    SendNotificationRequest request = SendNotificationRequest.builder()
        .channel(channel)
        .recipient(requestDto.getTo())
        .content(requestDto.getContent())
        .build();

    notificationService.send(request);
    return ResponseEntity.ok(ApiResponse.success("Notification sent successfully"));
  }

  private NotificationChannel resolveChannel(String channel) {
    if (!StringUtils.hasText(channel)) {
      throw new DevSharingException(ExceptionEnum.INVALID_CHANNEL, new Object[]{channel});
    }
    try {
      return NotificationChannel.valueOf(channel.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new DevSharingException(ExceptionEnum.INVALID_CHANNEL, new Object[]{channel});
    }
  }
}
