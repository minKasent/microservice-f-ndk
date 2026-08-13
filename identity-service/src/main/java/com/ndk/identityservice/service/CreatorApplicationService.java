package com.ndk.identityservice.service;

import com.ndk.identityservice.dto.request.ApplicationReviewRequestDto;
import com.ndk.identityservice.dto.request.CreatorApplicationRequestDto;
import com.ndk.identityservice.dto.response.CreatorApplicationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CreatorApplicationService {

  CreatorApplicationResponseDto submitApplication(Long userId,
      CreatorApplicationRequestDto requestDto);

  CreatorApplicationResponseDto reviewApplication(Long applicationId,
      ApplicationReviewRequestDto reviewRequestDto, Long reviewerId);

  Page<CreatorApplicationResponseDto> getPendingApplications(Pageable pageable);

  Page<CreatorApplicationResponseDto> getUserApplications(Long userId, Pageable pageable);

  CreatorApplicationResponseDto getApplicationById(Long applicationId);
}
