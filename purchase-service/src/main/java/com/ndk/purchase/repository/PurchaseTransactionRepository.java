package com.ndk.purchase.repository;

import com.ndk.purchase.entity.PurchaseTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
  List<PurchaseTransaction> findByPurchaseId(Long purchaseId);
}
