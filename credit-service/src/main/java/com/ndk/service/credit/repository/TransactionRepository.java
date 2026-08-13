package com.ndk.service.credit.repository;

import com.ndk.service.credit.entity.Transaction;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
