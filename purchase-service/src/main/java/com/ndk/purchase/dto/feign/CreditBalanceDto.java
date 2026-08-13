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
public class CreditBalanceDto {
  private String userId;
  private BigDecimal balance;
}
