package com.ndk.service.credit.dto;

import com.ndk.service.credit.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
  private Long id;
  private Long userId;
  private TransactionType type;
  private BigDecimal amount;
  private BigDecimal balanceBefore;
  private BigDecimal balanceAfter;
  private String description;
  private String referenceId;
  private Instant createdAt;
}
