package com.ndk.purchase.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.config.FeignClientConfig;
import com.ndk.purchase.dto.feign.NotificationDto;
import com.ndk.purchase.dto.feign.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "notification-service", 
    configuration = FeignClientConfig.class
)
public interface NotificationServiceClient {
  
  @PostMapping("/api/v1/notifications/send")
  ApiResponse<NotificationDto> send(@RequestBody SendNotificationRequest request);
}
