package com.ndk.service.credit.repository;

import com.ndk.service.credit.entity.WithdrawalRequest;
import com.ndk.service.credit.entity.WithdrawalStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

  Page<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Page<WithdrawalRequest> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status, Pageable pageable);

  List<WithdrawalRequest> findByUserIdAndStatus(Long userId, WithdrawalStatus status);
}
