package com.ndk.notificationservice.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.notificationservice.dto.request.SendBulkNotificationRequest;
import com.ndk.notificationservice.dto.request.SendNotificationRequest;
import com.ndk.notificationservice.dto.response.NotificationDto;
import com.ndk.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@LogExecutionTime
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @PostMapping("/send")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<NotificationDto>> send(
      @Valid @RequestBody SendNotificationRequest request
  ) {
    NotificationDto notification = notificationService.send(request);
    return ResponseEntity.ok(ApiResponse.success(notification));
  }

  @PostMapping("/send-bulk")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> sendBulk(
      @Valid @RequestBody SendBulkNotificationRequest request
  ) {
    notificationService.sendBulk(request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/history")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Page<NotificationDto>>> getHistory(
      @RequestParam String recipient,
      Pageable pageable
  ) {
    Page<NotificationDto> history = notificationService.getHistory(recipient, pageable);
    return ResponseEntity.ok(ApiResponse.success(history));
  }
}
