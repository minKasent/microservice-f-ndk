package com.ndk.identityservice.dto.request;

import com.ndk.identityservice.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationReviewRequestDto {

  @NotNull(message = "Status is required")
  private ApplicationStatus status;

  @Size(max = 1000, message = "Review note must not exceed 1000 characters")
  private String reviewNote;
}
