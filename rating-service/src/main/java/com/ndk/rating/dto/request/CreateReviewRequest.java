package com.ndk.rating.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class CreateReviewRequest {
  @NotBlank(message = "{rating.contentId.notBlank}")
  private String contentId;
  
  @NotNull
  @Min(value = 1, message = "{rating.ratingValue.min}")
  @Max(value = 5, message = "{rating.ratingValue.max}")
  private Integer ratingValue;
  
  @Size(max = 255, message = "{review.title.size}")
  private String title;
  
  @NotBlank(message = "{review.content.notBlank}")
  @Size(max = 5000, message = "{review.content.size}")
  private String content;
}
