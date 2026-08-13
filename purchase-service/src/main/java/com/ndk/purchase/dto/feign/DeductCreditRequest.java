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
public class DeductCreditRequest {
  private String userId;
  private BigDecimal amount;
  private String reason;
  private String referenceId;
}
