package com.ndk.purchase.saga;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseSagaData {
  private Long contentId;
  private String buyerId;
  private String creatorId;
  private String purchaseCode;
  private BigDecimal contentPrice;
  private BigDecimal platformFee;
  private BigDecimal platformFeeRate;
  private BigDecimal creatorCommission;
  private BigDecimal creatorCommissionRate;
  private String contentTitle;
  private String buyerEmail;
  private String creatorEmail;
  private String creatorName;
  private String buyerName;
  private String deductCreditTransactionId;
  private String addCommissionTransactionId;
}
