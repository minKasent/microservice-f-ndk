package com.ndk.service.credit.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.service.credit.dto.ReviewWithdrawalRequest;
import com.ndk.service.credit.dto.WithdrawalRequestDto;
import com.ndk.service.credit.entity.Transaction;
import com.ndk.service.credit.entity.TransactionType;
import com.ndk.service.credit.entity.Wallet;
import com.ndk.service.credit.entity.WithdrawalRequest;
import com.ndk.service.credit.entity.WithdrawalStatus;
import com.ndk.service.credit.exception.ExceptionEnum;
import com.ndk.service.credit.mapper.WithdrawalRequestMapper;
import com.ndk.service.credit.repository.TransactionRepository;
import com.ndk.service.credit.repository.WalletRepository;
import com.ndk.service.credit.repository.WithdrawalRequestRepository;
import com.ndk.service.credit.service.WalletService;
import com.ndk.service.credit.service.WithdrawalService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

  private final WithdrawalRequestRepository withdrawalRequestRepository;
  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final WalletService walletService;
  private final WithdrawalRequestMapper withdrawalRequestMapper;

  private static final BigDecimal MINIMUM_WITHDRAWAL = new BigDecimal("10"); // 10 credits minimum

  @Override
  @Transactional
  public WithdrawalRequestDto createWithdrawalRequest(Long userId, WithdrawalRequestDto request) {
    // Validate minimum withdrawal
    if (request.getAmount().compareTo(MINIMUM_WITHDRAWAL) < 0) {
      throw new DevSharingException(ExceptionEnum.MINIMUM_WITHDRAWAL_NOT_MET,
          new Object[]{MINIMUM_WITHDRAWAL});
    }

    // Check if user has sufficient balance with pessimistic write lock
    Wallet wallet = walletRepository.findByUserIdWithLock(userId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.WALLET_NOT_FOUND, new Object[]{userId}));

    if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
      throw new DevSharingException(ExceptionEnum.INSUFFICIENT_BALANCE, new Object[]{});
    }

    // Check if user already has pending withdrawal
    List<WithdrawalRequest> pendingRequests = withdrawalRequestRepository
        .findByUserIdAndStatus(userId, WithdrawalStatus.PENDING);
    if (!pendingRequests.isEmpty()) {
      throw new DevSharingException(ExceptionEnum.PENDING_WITHDRAWAL_EXISTS, new Object[]{});
    }

    // Freeze balance immediately to prevent double-spending or subsequent overdraft
    BigDecimal balanceBefore = wallet.getBalance();
    BigDecimal balanceAfter = balanceBefore.subtract(request.getAmount());
    BigDecimal currentFrozen = wallet.getFrozenBalance() != null ? wallet.getFrozenBalance() : BigDecimal.ZERO;
    wallet.setBalance(balanceAfter);
    wallet.setFrozenBalance(currentFrozen.add(request.getAmount()));
    wallet.setUpdatedAt(Instant.now());
    walletRepository.save(wallet);

    WithdrawalRequest withdrawalRequest = WithdrawalRequest.builder()
        .userId(userId)
        .amount(request.getAmount())
        .status(WithdrawalStatus.PENDING)
        .bankAccountNumber(request.getBankAccountNumber())
        .bankName(request.getBankName())
        .accountHolderName(request.getAccountHolderName())
        .note(request.getNote())
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    withdrawalRequest = withdrawalRequestRepository.save(withdrawalRequest);

    log.info("(createWithdrawalRequest)User [{}] created withdrawal request for {} credits. Balance: {} -> {}, Frozen: {}",
        userId, request.getAmount(), balanceBefore, balanceAfter, wallet.getFrozenBalance());

    return withdrawalRequestMapper.toDto(withdrawalRequest);
  }

  @Override
  @Transactional
  public WithdrawalRequestDto reviewWithdrawalRequest(Long requestId, Long adminUserId, ReviewWithdrawalRequest review) {
    WithdrawalRequest withdrawalRequest = withdrawalRequestRepository.findById(requestId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.WITHDRAWAL_REQUEST_NOT_FOUND,
            new Object[]{requestId}));

    if (withdrawalRequest.getStatus() != WithdrawalStatus.PENDING) {
      throw new DevSharingException(ExceptionEnum.WITHDRAWAL_ALREADY_PROCESSED, new Object[]{});
    }

    Instant now = Instant.now();
    Long requestUserId = withdrawalRequest.getUserId();
    Wallet wallet = walletRepository.findByUserIdWithLock(requestUserId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.WALLET_NOT_FOUND,
            new Object[]{requestUserId}));
    BigDecimal currentFrozen = wallet.getFrozenBalance() != null ? wallet.getFrozenBalance() : BigDecimal.ZERO;

    if (review.getApproved()) {
      // Approve: permanently clear frozen balance and record withdrawal transaction
      BigDecimal updatedFrozen = currentFrozen.subtract(withdrawalRequest.getAmount()).max(BigDecimal.ZERO);
      wallet.setFrozenBalance(updatedFrozen);
      wallet.setUpdatedAt(now);
      walletRepository.save(wallet);

      Transaction transaction = Transaction.builder()
          .userId(withdrawalRequest.getUserId())
          .type(TransactionType.WITHDRAWAL)
          .amount(withdrawalRequest.getAmount().negate())
          .balanceBefore(wallet.getBalance().add(withdrawalRequest.getAmount()))
          .balanceAfter(wallet.getBalance())
          .description("Withdrawal approved: " + review.getAdminNote())
          .referenceId(String.valueOf(requestId))
          .createdAt(now)
          .build();
      transactionRepository.save(transaction);

      withdrawalRequest.setStatus(WithdrawalStatus.APPROVED);
      log.info("(reviewWithdrawalRequest)Withdrawal request [{}] approved by admin [{}]",
          requestId, adminUserId);
    } else {
      // Reject: refund frozen balance back to active balance
      BigDecimal updatedFrozen = currentFrozen.subtract(withdrawalRequest.getAmount()).max(BigDecimal.ZERO);
      wallet.setFrozenBalance(updatedFrozen);
      wallet.setBalance(wallet.getBalance().add(withdrawalRequest.getAmount()));
      wallet.setUpdatedAt(now);
      walletRepository.save(wallet);

      withdrawalRequest.setStatus(WithdrawalStatus.REJECTED);
      log.info("(reviewWithdrawalRequest)Withdrawal request [{}] rejected by admin [{}]",
          requestId, adminUserId);
    }

    withdrawalRequest.setAdminNote(review.getAdminNote());
    withdrawalRequest.setReviewedBy(adminUserId);
    withdrawalRequest.setReviewedAt(now);
    withdrawalRequest.setUpdatedAt(now);

    withdrawalRequest = withdrawalRequestRepository.save(withdrawalRequest);

    return withdrawalRequestMapper.toDto(withdrawalRequest);
  }

  @Override
  public WithdrawalRequestDto getWithdrawalRequest(Long requestId) {
    WithdrawalRequest withdrawalRequest = withdrawalRequestRepository.findById(requestId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.WITHDRAWAL_REQUEST_NOT_FOUND,
            new Object[]{requestId}));
    return withdrawalRequestMapper.toDto(withdrawalRequest);
  }

  @Override
  public Page<WithdrawalRequestDto> getUserWithdrawalRequests(Long userId, Pageable pageable) {
    return withdrawalRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(withdrawalRequestMapper::toDto);
  }

  @Override
  public Page<WithdrawalRequestDto> getWithdrawalRequestsByStatus(WithdrawalStatus status, Pageable pageable) {
    return withdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
        .map(withdrawalRequestMapper::toDto);
  }

}
