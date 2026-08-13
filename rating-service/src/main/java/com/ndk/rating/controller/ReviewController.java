package com.ndk.rating.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.rating.dto.request.CreateReviewRequest;
import com.ndk.rating.dto.request.ModerateReviewRequest;
import com.ndk.rating.dto.response.ReviewDto;
import com.ndk.rating.enums.ReviewStatus;
import com.ndk.rating.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;
  
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<ReviewDto>> createOrUpdateReview(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody CreateReviewRequest request
  ) {
    String userId = jwt.getSubject();
    ReviewDto review = reviewService.createOrUpdateReview(userId, request);
    return ResponseEntity.ok(ApiResponse.success(review));
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ReviewDto>> getReviewById(@PathVariable Long id) {
    ReviewDto review = reviewService.getReviewById(id);
    return ResponseEntity.ok(ApiResponse.success(review));
  }
  
  @GetMapping("/content/{contentId}")
  public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByContent(
      @PathVariable String contentId,
      @RequestParam(required = false) ReviewStatus status,
      Pageable pageable
  ) {
    Page<ReviewDto> reviews = reviewService.getReviewsByContent(contentId, status, pageable);
    return ResponseEntity.ok(ApiResponse.success(reviews));
  }
  
  @GetMapping("/my-reviews")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Page<ReviewDto>>> getMyReviews(
      @AuthenticationPrincipal Jwt jwt,
      Pageable pageable
  ) {
    String userId = jwt.getSubject();
    Page<ReviewDto> reviews = reviewService.getReviewsByUser(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(reviews));
  }
  
  @PostMapping("/{id}/helpful")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> markAsHelpful(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id
  ) {
    String userId = jwt.getSubject();
    reviewService.markAsHelpful(userId, id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
  
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> deleteReview(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id
  ) {
    String userId = jwt.getSubject();
    reviewService.deleteReview(userId, id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
  
  @PatchMapping("/{id}/moderate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> moderateReview(
      @PathVariable Long id,
      @Valid @RequestBody ModerateReviewRequest request
  ) {
    reviewService.moderateReview(id, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
