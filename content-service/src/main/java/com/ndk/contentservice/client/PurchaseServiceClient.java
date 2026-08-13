package com.ndk.contentservice.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for Purchase Service
 * 
 * Note: Bearer token is automatically added by FeignClientConfig interceptor
 * No need to manually pass @RequestHeader("Authorization")
 */
@FeignClient(
    name = "purchase-service",
    configuration = FeignClientConfig.class
)
public interface PurchaseServiceClient {
  
  /**
   * Check if user owns a content (has purchased it)
   * @param contentId The content ID to check
   * @return true if user owns the content, false otherwise
   */
  @GetMapping("/api/v1/library/{contentId}/check")
  ApiResponse<Boolean> checkOwnership(@PathVariable("contentId") Long contentId);
}
