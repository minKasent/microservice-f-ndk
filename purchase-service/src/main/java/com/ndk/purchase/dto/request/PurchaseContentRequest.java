package com.ndk.purchase.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseContentRequest {
  @NotNull(message = "Content ID is required")
  private Long contentId;  // Changed from String to Long
}
