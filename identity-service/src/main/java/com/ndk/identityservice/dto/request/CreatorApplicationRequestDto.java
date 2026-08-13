package com.ndk.identityservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatorApplicationRequestDto {

  @NotBlank(message = "Reason is required")
  @Size(min = 50, max = 2000, message = "Reason must be between 50 and 2000 characters")
  private String reason;

  private String portfolioUrl;

  @Min(value = 0, message = "Experience years must be non-negative")
  private Integer experienceYears;

  @Size(max = 255, message = "Specialization must not exceed 255 characters")
  private String specialization;
}
