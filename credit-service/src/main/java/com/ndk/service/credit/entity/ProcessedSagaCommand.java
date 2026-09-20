package com.ndk.service.credit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_saga_commands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedSagaCommand {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "BIGINT UNSIGNED")
  private Long id;

  @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
  private String idempotencyKey;

  @Column(name = "saga_id", length = 36)
  private String sagaId;

  @Column(name = "command_type", length = 50)
  private String commandType;

  @Column(name = "result_success")
  private Boolean resultSuccess;

  @Column(name = "result_payload", columnDefinition = "JSON")
  private String resultPayload;

  @Column(name = "processed_at")
  private Instant processedAt;

  @PrePersist
  public void prePersist() {
    this.processedAt = Instant.now();
  }
}
