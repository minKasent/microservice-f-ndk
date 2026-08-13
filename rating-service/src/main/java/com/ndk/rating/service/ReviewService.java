package com.ndk.rating.service;

import com.ndk.rating.dto.request.CreateReviewRequest;
import com.ndk.rating.dto.request.ModerateReviewRequest;
import com.ndk.rating.dto.response.ReviewDto;
import com.ndk.rating.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
  ReviewDto createOrUpdateReview(String userId, CreateReviewRequest request);
  
  ReviewDto getReviewById(Long reviewId);
  
  Page<ReviewDto> getReviewsByContent(String contentId, ReviewStatus status, Pageable pageable);
  
  Page<ReviewDto> getReviewsByUser(String userId, Pageable pageable);
  
  void markAsHelpful(String userId, Long reviewId);
  
  void deleteReview(String userId, Long reviewId);
  
  void moderateReview(Long reviewId, ModerateReviewRequest request);
}
