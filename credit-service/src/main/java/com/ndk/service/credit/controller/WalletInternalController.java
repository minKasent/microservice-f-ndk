package com.ndk.service.credit.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.service.credit.dto.WalletDto;
import com.ndk.service.credit.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API Controller for Wallet Management
 * Used for inter-service communication
 * No authentication required - should be protected at API Gateway level
 */
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet Internal API", description = "Internal APIs for wallet management (inter-service calls)")
public class WalletInternalController {

  private final WalletService walletService;

  /**
   * Get or create wallet for a user (Internal API)
   * This endpoint is called by other services (e.g., identity-service)
   * to automatically create wallet for new users
   * 
   * @param userId The user ID
   * @return Wallet information
   */
  @GetMapping("/{userId}")
  @Operation(
      summary = "Get or create wallet (Internal)", 
      description = "Get existing wallet or create new one if not exists. For inter-service calls."
  )
  public ResponseEntity<ApiResponse<WalletDto>> getOrCreateWallet(@PathVariable String userId) {
    log.info("Internal API: Getting or creating wallet for user: {}", userId);
    
    Long userIdLong = Long.parseLong(userId);
    WalletDto wallet = walletService.getOrCreateWallet(userIdLong);
    
    log.info("Internal API: Wallet retrieved/created successfully for user: {}", userId);
    return ResponseEntity.ok(ApiResponse.success(wallet));
  }
}
