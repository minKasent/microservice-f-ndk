package com.ndk.identityservice.client.payload.response;

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
public class WalletDto {
  private Long id;
  private Long userId;
  private BigDecimal balance;
  private Instant createdAt;
  private Instant updatedAt;
}
