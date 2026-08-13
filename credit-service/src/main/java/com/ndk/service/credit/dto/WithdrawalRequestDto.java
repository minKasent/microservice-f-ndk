package com.ndk.service.credit.dto;

import com.ndk.service.credit.entity.WithdrawalStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class WithdrawalRequestDto {
  private Long id;
  private Long userId;

  @NotNull(message = "{withdrawal.amount.positive}")
  @Min(value = 10, message = "{withdrawal.amount.min}")
  private BigDecimal amount;

  private WithdrawalStatus status;

  @NotBlank(message = "{withdrawal.bankAccountNumber.notBlank}")
  private String bankAccountNumber;

  @NotBlank(message = "{withdrawal.bankName.notBlank}")
  private String bankName;

  @NotBlank(message = "{withdrawal.accountHolderName.notBlank}")
  private String accountHolderName;

  private String note;
  private String adminNote;
  private Long reviewedBy;
  private Instant reviewedAt;
  private Instant createdAt;
  private Instant updatedAt;
}
