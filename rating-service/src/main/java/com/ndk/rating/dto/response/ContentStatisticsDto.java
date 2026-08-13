package com.ndk.rating.dto.response;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatisticsDto {
  private String contentId;
  private Integer totalRatings;
  private BigDecimal averageRating;
  private Map<Integer, Integer> ratingDistribution;
  private Integer totalReviews;
}
