package com.ndk.identityservice.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.identityservice.dto.request.ApplicationReviewRequestDto;
import com.ndk.identityservice.dto.request.CreatorApplicationRequestDto;
import com.ndk.identityservice.dto.response.CreatorApplicationResponseDto;
import com.ndk.identityservice.service.CreatorApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecutionTime
@RestController
@RequestMapping("/api/v1/creator-applications")
@RequiredArgsConstructor
@Slf4j
public class CreatorApplicationController {

  private final CreatorApplicationService creatorApplicationService;

  @PostMapping
  public ResponseEntity<ApiResponse<CreatorApplicationResponseDto>> submitApplication(
      @Valid @RequestBody CreatorApplicationRequestDto requestDto,
      Authentication authentication
  ) {
    log.info("(submitApplication)Received creator application submission");
    Long userId = Long.parseLong(authentication.getName());

    CreatorApplicationResponseDto response =
        creatorApplicationService.submitApplication(userId, requestDto);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/pending")
  public ResponseEntity<ApiResponse<Page<CreatorApplicationResponseDto>>> getPendingApplications(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
      Pageable pageable
  ) {
    log.info("(getPendingApplications)Fetching pending applications");
    Page<CreatorApplicationResponseDto> response =
        creatorApplicationService.getPendingApplications(pageable);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/my-applications")
  public ResponseEntity<ApiResponse<Page<CreatorApplicationResponseDto>>> getMyApplications(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
      Pageable pageable,
      Authentication authentication
  ) {
    log.info("(getMyApplications)Fetching user's applications");
    Long userId = Long.parseLong(authentication.getName());

    Page<CreatorApplicationResponseDto> response =
        creatorApplicationService.getUserApplications(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{applicationId}")
  public ResponseEntity<ApiResponse<CreatorApplicationResponseDto>> getApplicationById(
      @PathVariable Long applicationId
  ) {
    log.info("(getApplicationById)Fetching application [{}]", applicationId);
    CreatorApplicationResponseDto response =
        creatorApplicationService.getApplicationById(applicationId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/{applicationId}/review")
  public ResponseEntity<ApiResponse<CreatorApplicationResponseDto>> reviewApplication(
      @PathVariable Long applicationId,
      @Valid @RequestBody ApplicationReviewRequestDto reviewRequestDto,
      Authentication authentication
  ) {
    log.info("(reviewApplication)Reviewing application [{}]", applicationId);
    Long reviewerId = Long.parseLong(authentication.getName());

    CreatorApplicationResponseDto response = creatorApplicationService.reviewApplication(
        applicationId,
        reviewRequestDto,
        reviewerId
    );
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
