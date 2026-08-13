package com.ndk.purchase.dto.feign;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {
  private String id;
  private String title;
  private String description;
  private String thumbnail;
  private BigDecimal price;
  private String creatorId;
  private String status;
}
