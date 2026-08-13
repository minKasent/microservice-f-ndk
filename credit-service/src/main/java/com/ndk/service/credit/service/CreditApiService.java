package com.ndk.service.credit.service;

import com.ndk.service.credit.dto.AddCreditRequest;
import com.ndk.service.credit.dto.CreditBalanceDto;
import com.ndk.service.credit.dto.CreditTransactionDto;
import com.ndk.service.credit.dto.DeductCreditRequest;

/**
 * Service for Credit API operations
 * Handles inter-service credit transactions
 */
public interface CreditApiService {
  
  /**
   * Get user credit balance
   * @param userId User ID
   * @return Credit balance information
   */
  CreditBalanceDto getBalance(String userId);
  
  /**
   * Deduct credit from user wallet
   * @param request Deduct credit request
   * @return Transaction details
   */
  CreditTransactionDto deductCredit(DeductCreditRequest request);
  
  /**
   * Add credit to user wallet
   * @param request Add credit request
   * @return Transaction details
   */
  CreditTransactionDto addCredit(AddCreditRequest request);
}
