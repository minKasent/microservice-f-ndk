package com.ndk.contentservice.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.ReviewContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentReviewDto;
import com.ndk.contentservice.service.ModeratorReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moderator/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Moderator Review", description = "APIs for moderators to review content (NOT user review/rating)")
public class ModeratorReviewController {

  private final ModeratorReviewService moderatorReviewService;

  @PostMapping("/content/{contentId}")
  @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
  @Operation(
      summary = "Review content (Moderator only)", 
      description = "Moderator reviews content: approve, reject, or request changes. This is NOT user review/rating."
  )
  public ResponseEntity<ApiResponse<ContentDto>> reviewContent(
      @PathVariable Long contentId,
      @Valid @RequestBody ReviewContentRequest request,
      Authentication authentication) {
    Long moderatorId = getUserIdFromAuth(authentication);
    ContentDto content = moderatorReviewService.reviewContent(contentId, request, moderatorId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @GetMapping("/content/{contentId}/history")
  @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('CREATOR')")
  @Operation(
      summary = "Get review history", 
      description = "Get all moderator review history for a content"
  )
  public ResponseEntity<ApiResponse<List<ContentReviewDto>>> getReviewHistory(
      @PathVariable Long contentId) {
    List<ContentReviewDto> reviews = moderatorReviewService.getContentReviewHistory(contentId);
    return ResponseEntity.ok(ApiResponse.success(reviews));
  }

  @GetMapping("/pending")
  @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
  @Operation(
      summary = "Get pending review contents", 
      description = "Get all contents waiting for moderator review"
  )
  public ResponseEntity<ApiResponse<List<ContentDto>>> getPendingReviews() {
    List<ContentDto> contents = moderatorReviewService.getPendingReviewContents();
    return ResponseEntity.ok(ApiResponse.success(contents));
  }

  private Long getUserIdFromAuth(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return Long.parseLong(jwt.getClaimAsString("userId"));
  }
}
