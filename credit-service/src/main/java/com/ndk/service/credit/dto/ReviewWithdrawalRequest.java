package com.ndk.service.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWithdrawalRequest {
  @NotNull(message = "Approval status is required")
  private Boolean approved;

  @NotBlank(message = "Admin note is required")
  private String adminNote;
}
