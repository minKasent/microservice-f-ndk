package com.ndk.contentservice.dto.response;

import com.ndk.contentservice.entity.ReviewAction;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReviewDto {
  private Long id;
  private Long contentId;
  private Long moderatorId;
  private ReviewAction action;
  private String feedback;
  private Instant reviewedAt;
}
