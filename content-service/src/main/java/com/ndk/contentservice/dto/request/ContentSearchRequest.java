package com.ndk.contentservice.dto.request;

import com.ndk.contentservice.entity.ContentLevel;
import com.ndk.contentservice.entity.ContentStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSearchRequest {
  private String keyword; // Search in title and description
  private Long categoryId;
  private ContentStatus status;
  private ContentLevel level;
  private Long creatorId;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private Long minViewCount;
  private Long maxViewCount;
  private Long minPurchaseCount;
  private Long maxPurchaseCount;
  
  // Sorting
  private String sortBy; // title, createdAt, updatedAt, publishedAt, viewCount, purchaseCount, price
  private String sortDirection; // asc, desc
  
  // Pagination
  private Integer page;
  private Integer size;
}
