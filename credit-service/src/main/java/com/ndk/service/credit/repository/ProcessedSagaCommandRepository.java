package com.ndk.service.credit.repository;

import com.ndk.service.credit.entity.ProcessedSagaCommand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSagaCommandRepository extends JpaRepository<ProcessedSagaCommand, Long> {
  Optional<ProcessedSagaCommand> findByIdempotencyKey(String idempotencyKey);
}
