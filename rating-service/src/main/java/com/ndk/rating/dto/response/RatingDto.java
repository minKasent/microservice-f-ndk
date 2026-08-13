package com.ndk.rating.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {
  private Long id;
  private String contentId;
  private String userId;
  private Integer ratingValue;
  private Instant createdAt;
  private Instant updatedAt;
  private ReviewDto review;
}
