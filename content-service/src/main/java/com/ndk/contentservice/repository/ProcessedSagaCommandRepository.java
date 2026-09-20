package com.ndk.contentservice.repository;

import com.ndk.contentservice.entity.ProcessedSagaCommand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSagaCommandRepository extends JpaRepository<ProcessedSagaCommand, Long> {
  Optional<ProcessedSagaCommand> findByIdempotencyKey(String idempotencyKey);
}
