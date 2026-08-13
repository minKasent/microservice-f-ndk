package com.ndk.contentservice.dto.request;

import com.ndk.contentservice.entity.ReviewAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewContentRequest {
  @NotNull(message = "{review.action.notNull}")
  private ReviewAction action;

  @Size(max = 2000, message = "{review.feedback.size}")
  private String feedback;
}
