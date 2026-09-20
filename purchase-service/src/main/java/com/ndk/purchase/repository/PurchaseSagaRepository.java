package com.ndk.purchase.repository;

import com.ndk.purchase.entity.PurchaseSaga;
import com.ndk.purchase.enums.SagaStep;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseSagaRepository extends JpaRepository<PurchaseSaga, Long> {
  Optional<PurchaseSaga> findBySagaId(String sagaId);

  Optional<PurchaseSaga> findByPurchaseId(Long purchaseId);

  List<PurchaseSaga> findByCurrentStepNotInAndUpdatedAtBefore(Collection<SagaStep> terminalSteps, Instant cutoff);
}
