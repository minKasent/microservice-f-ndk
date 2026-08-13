package com.ndk.purchase.dto.response;

import com.ndk.purchase.enums.TransactionStatus;
import com.ndk.purchase.enums.TransactionType;
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
public class PurchaseTransactionDto {
  private Long id;
  private TransactionType transactionType;
  private String userId;
  private BigDecimal amount;
  private String creditTransactionId;
  private TransactionStatus status;
  private String errorMessage;
  private Instant createdAt;
  private Instant completedAt;
}
