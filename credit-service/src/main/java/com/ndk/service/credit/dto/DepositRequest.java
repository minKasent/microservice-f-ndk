package com.ndk.service.credit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
  @NotNull(message = "{deposit.amountVnd.positive}")
  @Min(value = 1000, message = "{deposit.amountVnd.min}")
  private BigDecimal amountVnd; // Số tiền VNĐ

  private String description;
}
