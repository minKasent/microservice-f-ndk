package com.ndk.purchase.repository;

import com.ndk.purchase.entity.Purchase;
import com.ndk.purchase.enums.PurchaseStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
  Optional<Purchase> findByPurchaseCode(String purchaseCode);
  
  Page<Purchase> findByBuyerId(String buyerId, Pageable pageable);
  
  Page<Purchase> findByCreatorId(String creatorId, Pageable pageable);
  
  Page<Purchase> findByBuyerIdAndStatus(String buyerId, PurchaseStatus status, Pageable pageable);
  
  long countByBuyerId(String buyerId);
  
  long countByCreatorId(String creatorId);
}
