package com.ndk.service.credit.service;

import com.ndk.service.credit.dto.DepositRequest;
import com.ndk.service.credit.dto.TransactionDto;
import com.ndk.service.credit.dto.WalletDto;
import com.ndk.service.credit.entity.TransactionType;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletService {

  WalletDto getOrCreateWallet(Long userId);

  WalletDto getWallet(Long userId);

  WalletDto deposit(Long userId, DepositRequest request);

  void deductCredit(Long userId, BigDecimal amount, TransactionType type, String description, String referenceId);

  void addCredit(Long userId, BigDecimal amount, TransactionType type, String description, String referenceId);

  Page<TransactionDto> getTransactionHistory(Long userId, Pageable pageable);

  List<TransactionDto> getAllTransactions(Long userId);
}
