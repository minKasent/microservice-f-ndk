package com.ndk.service.credit.service.impl;

import com.ndk.service.credit.dto.AddCreditRequest;
import com.ndk.service.credit.dto.CreditBalanceDto;
import com.ndk.service.credit.dto.CreditTransactionDto;
import com.ndk.service.credit.dto.DeductCreditRequest;
import com.ndk.service.credit.dto.WalletDto;
import com.ndk.service.credit.entity.TransactionType;
import com.ndk.service.credit.service.CreditApiService;
import com.ndk.service.credit.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Credit API Service
 * Handles business logic for inter-service credit transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditApiServiceImpl implements CreditApiService {
  
  private final WalletService walletService;
  
  @Override
  @Transactional(readOnly = true)
  public CreditBalanceDto getBalance(String userId) {
    log.info("Getting balance for user: {}", userId);
    
    Long userIdLong = Long.parseLong(userId);
    WalletDto wallet = walletService.getOrCreateWallet(userIdLong);
    
    return CreditBalanceDto.builder()
        .userId(userId)
        .balance(wallet.getBalance())
        .build();
  }
  
  @Override
  @Transactional
  public CreditTransactionDto deductCredit(DeductCreditRequest request) {
    log.info("Deducting {} credits from user: {}", request.getAmount(), request.getUserId());
    
    Long userIdLong = Long.parseLong(request.getUserId());
    
    // Get wallet before deduction
    WalletDto walletBefore = walletService.getWallet(userIdLong);
    
    // Deduct credit
    walletService.deductCredit(
        userIdLong, 
        request.getAmount(), 
        TransactionType.PURCHASE,
        request.getReason(),
        request.getReferenceId()
    );
    
    // Get wallet after deduction
    WalletDto walletAfter = walletService.getWallet(userIdLong);
    
    CreditTransactionDto transaction = CreditTransactionDto.builder()
        .userId(request.getUserId())
        .type(TransactionType.PURCHASE.name())
        .amount(request.getAmount())
        .balanceBefore(walletBefore.getBalance())
        .balanceAfter(walletAfter.getBalance())
        .description(request.getReason())
        .referenceId(request.getReferenceId())
        .build();
    
    log.info("Credit deducted successfully for user: {}", request.getUserId());
    return transaction;
  }
  
  @Override
  @Transactional
  public CreditTransactionDto addCredit(AddCreditRequest request) {
    log.info("Adding {} credits to user: {}", request.getAmount(), request.getUserId());
    
    Long userIdLong = Long.parseLong(request.getUserId());
    
    // Get wallet before addition
    WalletDto walletBefore = walletService.getWallet(userIdLong);
    
    // Add credit
    walletService.addCredit(
        userIdLong, 
        request.getAmount(), 
        TransactionType.EARNING,
        request.getReason(),
        request.getReferenceId()
    );
    
    // Get wallet after addition
    WalletDto walletAfter = walletService.getWallet(userIdLong);
    
    CreditTransactionDto transaction = CreditTransactionDto.builder()
        .userId(request.getUserId())
        .type(TransactionType.EARNING.name())
        .amount(request.getAmount())
        .balanceBefore(walletBefore.getBalance())
        .balanceAfter(walletAfter.getBalance())
        .description(request.getReason())
        .referenceId(request.getReferenceId())
        .build();
    
    log.info("Credit added successfully for user: {}", request.getUserId());
    return transaction;
  }
}
