package com.ndk.service.credit.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.service.credit.dto.AddCreditRequest;
import com.ndk.service.credit.dto.CreditBalanceDto;
import com.ndk.service.credit.dto.CreditTransactionDto;
import com.ndk.service.credit.dto.DeductCreditRequest;
import com.ndk.service.credit.service.CreditApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Credit API Controller for inter-service communication
 * 
 * These APIs are called by other services (e.g., purchase-service)
 * to manage credit transactions
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Credit API", description = "APIs for credit management (internal service calls)")
public class CreditApiController {
  
  private final CreditApiService creditApiService;
  
  @GetMapping("/wallets/balance/{userId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Get user balance", 
      description = "Get credit balance by user ID (for internal service calls)"
  )
  public ResponseEntity<ApiResponse<CreditBalanceDto>> getBalance(@PathVariable String userId) {
    CreditBalanceDto balance = creditApiService.getBalance(userId);
    return ResponseEntity.ok(ApiResponse.success(balance));
  }
  
  @PostMapping("/transactions/deduct")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Deduct credit", 
      description = "Deduct credit from user wallet (called by purchase-service)"
  )
  public ResponseEntity<ApiResponse<CreditTransactionDto>> deduct(
      @Valid @RequestBody DeductCreditRequest request) {
    CreditTransactionDto transaction = creditApiService.deductCredit(request);
    return ResponseEntity.ok(ApiResponse.success(transaction));
  }
  
  @PostMapping("/transactions/add")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Add credit", 
      description = "Add credit to user wallet (called by purchase-service for creator commission)"
  )
  public ResponseEntity<ApiResponse<CreditTransactionDto>> add(
      @Valid @RequestBody AddCreditRequest request) {
    CreditTransactionDto transaction = creditApiService.addCredit(request);
    return ResponseEntity.ok(ApiResponse.success(transaction));
  }
}
