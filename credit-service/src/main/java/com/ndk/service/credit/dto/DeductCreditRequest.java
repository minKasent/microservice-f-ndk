package com.ndk.service.credit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class DeductCreditRequest {
  
  @NotBlank(message = "User ID is required")
  private String userId;
  
  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;
  
  @NotBlank(message = "Reason is required")
  private String reason;
  
  private String referenceId;  // e.g., purchase code
}
