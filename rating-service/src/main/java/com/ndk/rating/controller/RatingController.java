package com.ndk.rating.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.rating.dto.request.CreateRatingRequest;
import com.ndk.rating.dto.response.RatingDto;
import com.ndk.rating.service.RatingService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
  private final RatingService ratingService;
  
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<RatingDto>> createOrUpdateRating(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody CreateRatingRequest request
  ) {
    String userId = jwt.getSubject();
    RatingDto rating = ratingService.createOrUpdateRating(userId, request);
    return ResponseEntity.ok(ApiResponse.success(rating));
  }
  
  @GetMapping("/content/{contentId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<RatingDto>> getUserRatingForContent(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String contentId
  ) {
    String userId = jwt.getSubject();
    RatingDto rating = ratingService.getUserRatingForContent(userId, contentId);
    return ResponseEntity.ok(ApiResponse.success(rating));
  }
  
  @DeleteMapping("/content/{contentId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> deleteRating(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String contentId
  ) {
    String userId = jwt.getSubject();
    ratingService.deleteRating(userId, contentId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
  
  @GetMapping("/my-ratings")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Page<RatingDto>>> getMyRatings(
      @AuthenticationPrincipal Jwt jwt,
      Pageable pageable
  ) {
    String userId = jwt.getSubject();
    Page<RatingDto> ratings = ratingService.getUserRatings(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(ratings));
  }
}
