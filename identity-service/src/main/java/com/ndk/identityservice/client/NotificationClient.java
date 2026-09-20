package com.ndk.identityservice.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.identityservice.client.config.NotificationClientConfig;
import com.ndk.identityservice.client.payload.request.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service",
            url = "${services.notification-service.url:}",
    configuration = NotificationClientConfig.class)

public interface NotificationClient {

  @PostMapping("/internal/api/v1/notifications")
  ResponseEntity<ApiResponse<?>> sendNotification(
      @RequestBody SendNotificationRequest requestDto);
}