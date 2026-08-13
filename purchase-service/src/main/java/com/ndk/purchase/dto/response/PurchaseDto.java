package com.ndk.purchase.dto.response;

import com.ndk.purchase.enums.PurchaseStatus;
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
public class PurchaseDto {
  private Long id;
  private String purchaseCode;
  private String buyerId;
  private String contentId;
  private String contentTitle;
  private String creatorId;
  private String creatorName;
  private BigDecimal contentPrice;
  private BigDecimal platformFee;
  private BigDecimal creatorCommission;
  private PurchaseStatus status;
  private Instant purchasedAt;
  private Instant completedAt;
}
