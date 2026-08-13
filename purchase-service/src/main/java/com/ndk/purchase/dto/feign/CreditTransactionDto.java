package com.ndk.purchase.dto.feign;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionDto {
  private String id;
  private String userId;
  private BigDecimal amount;
  private String type;
  private String reason;
}
