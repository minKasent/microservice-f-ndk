package com.ndk.service.credit.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.service.credit.dto.DepositRequest;
import com.ndk.service.credit.dto.TransactionDto;
import com.ndk.service.credit.dto.WalletDto;
import com.ndk.service.credit.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;

  @GetMapping
  @PreAuthorize("hasAnyRole('CONSUMER', 'CREATOR', 'ADMIN')")
  public ResponseEntity<ApiResponse<WalletDto>> getWallet(Authentication authentication) {
    Long userId = Long.parseLong(authentication.getName());
    WalletDto wallet = walletService.getOrCreateWallet(userId);
    return ResponseEntity.ok(ApiResponse.success(wallet));
  }

  @PostMapping("/deposit")
  @PreAuthorize("hasAnyRole('CONSUMER', 'CREATOR')")
  public ResponseEntity<ApiResponse<WalletDto>> deposit(
      Authentication authentication,
      @Valid @RequestBody DepositRequest request
  ) {
    Long userId = Long.parseLong(authentication.getName());
    WalletDto wallet = walletService.deposit(userId, request);
    return ResponseEntity.ok(ApiResponse.success(wallet));
  }

  @GetMapping("/transactions")
  @PreAuthorize("hasAnyRole('CONSUMER', 'CREATOR', 'ADMIN')")
  public ResponseEntity<ApiResponse<Page<TransactionDto>>> getTransactions(
      Authentication authentication,
      Pageable pageable
  ) {
    Long userId = Long.parseLong(authentication.getName());
    Page<TransactionDto> transactions = walletService.getTransactionHistory(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(transactions));
  }

  @GetMapping("/transactions/all")
  @PreAuthorize("hasAnyRole('CONSUMER', 'CREATOR', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<TransactionDto>>> getAllTransactions(
      Authentication authentication
  ) {
    Long userId = Long.parseLong(authentication.getName());
    List<TransactionDto> transactions = walletService.getAllTransactions(userId);
    return ResponseEntity.ok(ApiResponse.success(transactions));
  }
}
