package com.ndk.service.credit.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.service.credit.dto.DepositRequest;
import com.ndk.service.credit.dto.TransactionDto;
import com.ndk.service.credit.dto.WalletDto;
import com.ndk.service.credit.entity.Transaction;
import com.ndk.service.credit.entity.TransactionType;
import com.ndk.service.credit.entity.Wallet;
import com.ndk.service.credit.exception.ExceptionEnum;
import com.ndk.service.credit.mapper.TransactionMapper;
import com.ndk.service.credit.mapper.WalletMapper;
import com.ndk.service.credit.repository.TransactionRepository;
import com.ndk.service.credit.repository.WalletRepository;
import com.ndk.service.credit.service.WalletService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final WalletMapper walletMapper;
  private final TransactionMapper transactionMapper;

  private static final BigDecimal VND_TO_CREDIT_RATE = new BigDecimal("1000");

  @Override
  public WalletDto getOrCreateWallet(Long userId) {
    Wallet wallet = walletRepository.findByUserId(userId)
        .orElseGet(() -> createWallet(userId));
    return walletMapper.toDto(wallet);
  }

  @Override
  public WalletDto getWallet(Long userId) {
    Wallet wallet = walletRepository.findByUserId(userId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.WALLET_NOT_FOUND, new Object[]{userId}));
    return walletMapper.toDto(wallet);
  }

  @Override
  @Transactional
  public WalletDto deposit(Long userId, DepositRequest request) {
    if (request.getAmountVnd().compareTo(BigDecimal.ZERO) <= 0) {
      throw new DevSharingException(ExceptionEnum.INVALID_AMOUNT, new Object[]{});
    }

    // Convert VND to credit: 1000 VND = 1 credit
    BigDecimal creditAmount = request.getAmountVnd()
        .divide(VND_TO_CREDIT_RATE, 2, RoundingMode.DOWN);

    Wallet wallet = walletRepository.findByUserIdWithLock(userId)
        .orElseGet(() -> createWallet(userId));

    BigDecimal balanceBefore = wallet.getBalance();
    BigDecimal balanceAfter = balanceBefore.add(creditAmount);

    wallet.setBalance(balanceAfter);
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    recordTransaction(userId, TransactionType.DEPOSIT, creditAmount, balanceBefore, balanceAfter,
        request.getDescription(), null);

    log.info("(deposit)User [{}] deposited {} VND ({} credits). Balance: {} -> {}",
        userId, request.getAmountVnd(), creditAmount, balanceBefore, balanceAfter);

    return walletMapper.toDto(wallet);
  }

  @Override
  @Transactional
  public void deductCredit(Long userId, BigDecimal amount, TransactionType type, String description, String referenceId) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DevSharingException(ExceptionEnum.INVALID_AMOUNT, new Object[]{});
    }

    Wallet wallet = walletRepository.findByUserIdWithLock(userId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.WALLET_NOT_FOUND, new Object[]{userId}));

    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new DevSharingException(ExceptionEnum.INSUFFICIENT_BALANCE, new Object[]{});
    }

    BigDecimal balanceBefore = wallet.getBalance();
    BigDecimal balanceAfter = balanceBefore.subtract(amount);

    wallet.setBalance(balanceAfter);
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    Transaction transaction = Transaction.builder()
        .userId(userId)
        .type(type)
        .amount(amount.negate()) // Negative for deduction
        .balanceBefore(balanceBefore)
        .balanceAfter(balanceAfter)
        .description(description)
        .referenceId(referenceId)
        .createdAt(Instant.now())
        .build();
    transactionRepository.save(transaction);

    log.info("(deductCredit)User [{}] deducted {} credits for {}. Balance: {} -> {}",
        userId, amount, type, balanceBefore, balanceAfter);
  }

  @Override
  @Transactional
  public void addCredit(Long userId, BigDecimal amount, TransactionType type, String description, String referenceId) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DevSharingException(ExceptionEnum.INVALID_AMOUNT, new Object[]{});
    }

    Wallet wallet = walletRepository.findByUserIdWithLock(userId)
        .orElseGet(() -> createWallet(userId));

    BigDecimal balanceBefore = wallet.getBalance();
    BigDecimal balanceAfter = balanceBefore.add(amount);

    wallet.setBalance(balanceAfter);
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    Transaction transaction = Transaction.builder()
        .userId(userId)
        .type(type)
        .amount(amount)
        .balanceBefore(balanceBefore)
        .balanceAfter(balanceAfter)
        .description(description)
        .referenceId(referenceId)
        .createdAt(Instant.now())
        .build();
    transactionRepository.save(transaction);

    log.info("(addCredit)User [{}] received {} credits for {}. Balance: {} -> {}",
        userId, amount, type, balanceBefore, balanceAfter);
  }

  @Override
  public Page<TransactionDto> getTransactionHistory(Long userId, Pageable pageable) {
    return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(transactionMapper::toDto);
  }

  @Override
  public List<TransactionDto> getAllTransactions(Long userId) {
    return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(transactionMapper::toDto)
        .collect(Collectors.toList());
  }

  private Wallet createWallet(Long userId) {
    Wallet wallet = Wallet.builder()
        .userId(userId)
        .balance(BigDecimal.ZERO)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
    return walletRepository.save(wallet);
  }

  private void recordTransaction(Long userId, TransactionType type, BigDecimal amount,
      BigDecimal balanceBefore, BigDecimal balanceAfter, String description, String referenceId) {
    Transaction transaction = Transaction.builder()
        .userId(userId)
        .type(type)
        .amount(amount)
        .balanceBefore(balanceBefore)
        .balanceAfter(balanceAfter)
        .description(description)
        .referenceId(referenceId)
        .createdAt(Instant.now())
        .build();
    transactionRepository.save(transaction);
  }

}
