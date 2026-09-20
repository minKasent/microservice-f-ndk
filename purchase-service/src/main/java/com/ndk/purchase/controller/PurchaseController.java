package com.ndk.purchase.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.dto.request.PurchaseContentRequest;
import com.ndk.purchase.dto.response.PurchaseDto;
import com.ndk.purchase.dto.response.PurchaseTransactionDto;
import com.ndk.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecutionTime
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {
  private final PurchaseService purchaseService;

  @Value("${purchase.saga.enabled:true}")
  private boolean sagaEnabled;
  
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PurchaseDto>> purchaseContent(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody PurchaseContentRequest request
  ) {
    String userId = jwt.getSubject();
    log.info("Purchase request - BuyerId: {}, ContentId: {}, SagaEnabled: {}", userId, request.getContentId(), sagaEnabled);

    PurchaseDto purchase = purchaseService.purchaseContent(userId, request);

    log.info("Purchase processed - BuyerId: {}, ContentId: {}, PurchaseId: {}, Status: {}",
        userId, request.getContentId(), purchase.getId(), purchase.getStatus());

    if (sagaEnabled) {
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(purchase));
    }
    return ResponseEntity.ok(ApiResponse.success(purchase));
  }
  
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PurchaseDto>> getPurchaseById(@PathVariable Long id) {
    PurchaseDto purchase = purchaseService.getPurchaseById(id);
    return ResponseEntity.ok(ApiResponse.success(purchase));
  }
  
  @GetMapping("/my-purchases")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Page<PurchaseDto>>> getMyPurchases(
      @AuthenticationPrincipal Jwt jwt,
      Pageable pageable
  ) {
    String userId = jwt.getSubject();
    Page<PurchaseDto> purchases = purchaseService.getMyPurchases(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(purchases));
  }
  
  @GetMapping("/{id}/transactions")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<PurchaseTransactionDto>>> getPurchaseTransactions(
      @PathVariable Long id
  ) {
    List<PurchaseTransactionDto> transactions = purchaseService.getPurchaseTransactions(id);
    return ResponseEntity.ok(ApiResponse.success(transactions));
  }
}
