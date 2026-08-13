package com.ndk.service.credit.service;

import com.ndk.service.credit.dto.ReviewWithdrawalRequest;
import com.ndk.service.credit.dto.WithdrawalRequestDto;
import com.ndk.service.credit.entity.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WithdrawalService {

  WithdrawalRequestDto createWithdrawalRequest(Long userId, WithdrawalRequestDto request);

  WithdrawalRequestDto reviewWithdrawalRequest(Long requestId, Long adminUserId, ReviewWithdrawalRequest review);

  WithdrawalRequestDto getWithdrawalRequest(Long requestId);

  Page<WithdrawalRequestDto> getUserWithdrawalRequests(Long userId, Pageable pageable);

  Page<WithdrawalRequestDto> getWithdrawalRequestsByStatus(WithdrawalStatus status, Pageable pageable);
}
