package com.ndk.rating.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.rating.dto.request.CreateRatingRequest;
import com.ndk.rating.dto.response.RatingDto;
import com.ndk.rating.entity.Rating;
import com.ndk.rating.entity.Review;
import com.ndk.rating.enums.ReviewStatus;
import com.ndk.rating.exception.ExceptionEnum;
import com.ndk.rating.mapper.RatingMapper;
import com.ndk.rating.repository.RatingRepository;
import com.ndk.rating.repository.ReviewRepository;
import com.ndk.rating.service.RatingService;
import com.ndk.rating.service.StatisticsService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RatingServiceImpl implements RatingService {
  private final RatingRepository ratingRepository;
  private final ReviewRepository reviewRepository;
  private final RatingMapper ratingMapper;
  private final StatisticsService statisticsService;
  
  @Override
  public RatingDto createOrUpdateRating(String userId, CreateRatingRequest request) {
    Rating rating = ratingRepository.findByContentIdAndUserId(request.getContentId(), userId)
        .map(existing -> {
          existing.setRatingValue(request.getRatingValue());
          existing.setUpdatedAt(Instant.now());
          return existing;
        })
        .orElseGet(() -> Rating.builder()
            .contentId(request.getContentId())
            .userId(userId)
            .ratingValue(request.getRatingValue())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());
    
    rating = ratingRepository.save(rating);
    
    if (request.getReviewContent() != null && !request.getReviewContent().isBlank()) {
      Rating finalRating = rating;
      Review review = reviewRepository.findByRatingId(rating.getId())
          .map(existing -> {
            existing.setTitle(request.getReviewTitle());
            existing.setContent(request.getReviewContent());
            existing.setUpdatedAt(Instant.now());
            return existing;
          })
          .orElseGet(() -> Review.builder()
              .ratingId(finalRating.getId())
              .contentId(request.getContentId())
              .userId(userId)
              .title(request.getReviewTitle())
              .content(request.getReviewContent())
              .status(ReviewStatus.ACTIVE)
              .isVerifiedPurchase(false)
              .helpfulCount(0)
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build());
      
      reviewRepository.save(review);
    }
    
    statisticsService.updateStatistics(request.getContentId());
    
    return ratingMapper.toDto(rating);
  }
  
  @Override
  public RatingDto getUserRatingForContent(String userId, String contentId) {
    Rating rating = ratingRepository.findByContentIdAndUserId(contentId, userId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.RATING_NOT_FOUND, null));
    return ratingMapper.toDto(rating);
  }
  
  @Override
  public void deleteRating(String userId, String contentId) {
    if (!ratingRepository.existsByContentIdAndUserId(contentId, userId)) {
      throw new DevSharingException(ExceptionEnum.RATING_NOT_FOUND, null);
    }
    
    ratingRepository.deleteByContentIdAndUserId(contentId, userId);
    statisticsService.updateStatistics(contentId);
  }
  
  @Override
  public Page<RatingDto> getUserRatings(String userId, Pageable pageable) {
    return ratingRepository.findByUserId(userId, pageable)
        .map(ratingMapper::toDto);
  }
}
