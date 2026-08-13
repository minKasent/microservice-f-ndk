package com.ndk.contentservice.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.config.FeignClientConfig;
import com.ndk.contentservice.dto.request.CreditTransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Credit Service
 * 
 * Note: Bearer token is automatically added by FeignClientConfig interceptor
 * No need to manually pass @RequestHeader("Authorization")
 */
@FeignClient(
    name = "credit-service",
    configuration = FeignClientConfig.class
)
public interface CreditServiceClient {
  
  @PostMapping("/api/v1/transactions/deduct")
  ApiResponse<Void> deductCredit(@RequestBody CreditTransactionRequest request);
  
  @PostMapping("/api/v1/transactions/add")
  ApiResponse<Void> addCredit(@RequestBody CreditTransactionRequest request);
}
