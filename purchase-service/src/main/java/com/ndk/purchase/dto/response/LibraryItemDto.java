package com.ndk.purchase.dto.response;

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
public class LibraryItemDto {
  private Long id;
  private String contentId;
  private String contentTitle;
  private String contentThumbnail;
  private String creatorName;
  private BigDecimal purchasePrice;
  private Instant purchasedAt;
  private Instant lastAccessedAt;
  private Integer accessCount;
  private Boolean isActive;
}
