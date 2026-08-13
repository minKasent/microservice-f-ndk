package com.ndk.identityservice.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.identityservice.client.config.CreditClientConfig;
import com.ndk.identityservice.client.payload.response.WalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for credit-service — creates wallet for newly verified users.
 */
@FeignClient(
    name = "credit-service",
    configuration = CreditClientConfig.class
)
public interface CreditServiceClient {

  /**
   * Get or create wallet for a user.
   * This endpoint automatically creates the wallet if it does not exist.
   */
  @GetMapping("/api/v1/wallets/{userId}")
  ApiResponse<WalletDto> getOrCreateWallet(@PathVariable("userId") String userId);
}
