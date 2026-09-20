package com.ndk.service.credit.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {
  private Long userId;
  private BigDecimal balance;
  private BigDecimal balanceInVnd; // balance * 1000
  private BigDecimal frozenBalance;
}
