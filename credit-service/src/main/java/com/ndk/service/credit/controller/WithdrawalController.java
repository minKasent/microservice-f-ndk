package com.ndk.service.credit.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.service.credit.dto.ReviewWithdrawalRequest;
import com.ndk.service.credit.dto.WithdrawalRequestDto;
import com.ndk.service.credit.entity.WithdrawalStatus;
import com.ndk.service.credit.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawal")
@RequiredArgsConstructor
public class WithdrawalController {

  private final WithdrawalService withdrawalService;

  @PostMapping
  @PreAuthorize("hasRole('CREATOR')")
  public ResponseEntity<ApiResponse<WithdrawalRequestDto>> createWithdrawalRequest(
      Authentication authentication,
      @Valid @RequestBody WithdrawalRequestDto request
  ) {
    Long userId = Long.parseLong(authentication.getName());
    WithdrawalRequestDto result = withdrawalService.createWithdrawalRequest(userId, request);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/my-requests")
  @PreAuthorize("hasRole('CREATOR')")
  public ResponseEntity<ApiResponse<Page<WithdrawalRequestDto>>> getMyWithdrawalRequests(
      Authentication authentication,
      Pageable pageable
  ) {
    Long userId = Long.parseLong(authentication.getName());
    Page<WithdrawalRequestDto> requests = withdrawalService.getUserWithdrawalRequests(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(requests));
  }

  @GetMapping("/{requestId}")
  @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
  public ResponseEntity<ApiResponse<WithdrawalRequestDto>> getWithdrawalRequest(
      @PathVariable Long requestId
  ) {
    WithdrawalRequestDto request = withdrawalService.getWithdrawalRequest(requestId);
    return ResponseEntity.ok(ApiResponse.success(request));
  }

  @GetMapping("/admin/pending")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<WithdrawalRequestDto>>> getPendingRequests(
      Pageable pageable
  ) {
    Page<WithdrawalRequestDto> requests = withdrawalService
        .getWithdrawalRequestsByStatus(WithdrawalStatus.PENDING, pageable);
    return ResponseEntity.ok(ApiResponse.success(requests));
  }

  @PutMapping("/admin/{requestId}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<WithdrawalRequestDto>> reviewWithdrawalRequest(
      Authentication authentication,
      @PathVariable Long requestId,
      @Valid @RequestBody ReviewWithdrawalRequest review
  ) {
    Long adminUserId = Long.parseLong(authentication.getName());
    WithdrawalRequestDto result = withdrawalService.reviewWithdrawalRequest(requestId, adminUserId, review);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
