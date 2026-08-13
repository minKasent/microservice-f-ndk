package com.ndk.rating.dto.response;

import com.ndk.rating.enums.ReviewStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
  private Long id;
  private String contentId;
  private String userId;
  private String userDisplayName;
  private Integer ratingValue;
  private String title;
  private String content;
  private ReviewStatus status;
  private Boolean isVerifiedPurchase;
  private Integer helpfulCount;
  private Instant createdAt;
  private Instant updatedAt;
}
