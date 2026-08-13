package com.ndk.contentservice.dto.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionRequest {
  private Long userId;
  private BigDecimal amount;
  private String type; // PURCHASE, EARNING
  private String description;
  private String referenceId; // content_id
}
