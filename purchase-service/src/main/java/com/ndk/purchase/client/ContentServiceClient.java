package com.ndk.purchase.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.config.FeignClientConfig;
import com.ndk.purchase.dto.feign.ContentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
    name = "content-service", 
    configuration = FeignClientConfig.class
)
public interface ContentServiceClient {
  
  @GetMapping("/contents/{id}")
  ApiResponse<ContentDto> getContentById(@PathVariable("id") Long id);  // Changed to Long
  
  /**
   * Called after successful purchase to increment purchase count
   */
  @PostMapping("/contents/{id}/increment-purchase-count")
  ApiResponse<Void> incrementPurchaseCount(@PathVariable("id") Long id);  // Changed to Long
}
