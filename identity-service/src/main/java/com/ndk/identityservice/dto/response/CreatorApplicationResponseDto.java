package com.ndk.identityservice.dto.response;

import com.ndk.identityservice.entity.ApplicationStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatorApplicationResponseDto {
  private Long id;
  private Long userId;
  private String username;
  private String email;
  private String reason;
  private String portfolioUrl;
  private Integer experienceYears;
  private String specialization;
  private ApplicationStatus status;
  private Long reviewedBy;
  private Instant reviewedAt;
  private String reviewNote;
  private Instant createdAt;
  private Instant updatedAt;
}
