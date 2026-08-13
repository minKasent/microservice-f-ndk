package com.ndk.rating.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.rating.dto.request.CreateReviewRequest;
import com.ndk.rating.dto.request.ModerateReviewRequest;
import com.ndk.rating.dto.response.ReviewDto;
import com.ndk.rating.entity.Rating;
import com.ndk.rating.entity.Review;
import com.ndk.rating.entity.ReviewHelpful;
import com.ndk.rating.enums.ReviewStatus;
import com.ndk.rating.exception.ExceptionEnum;
import com.ndk.rating.mapper.ReviewMapper;
import com.ndk.rating.repository.RatingRepository;
import com.ndk.rating.repository.ReviewHelpfulRepository;
import com.ndk.rating.repository.ReviewRepository;
import com.ndk.rating.service.ReviewService;
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
public class ReviewServiceImpl implements ReviewService {
  private final ReviewRepository reviewRepository;
  private final RatingRepository ratingRepository;
  private final ReviewHelpfulRepository reviewHelpfulRepository;
  private final ReviewMapper reviewMapper;
  private final StatisticsService statisticsService;
  
  @Override
  public ReviewDto createOrUpdateReview(String userId, CreateReviewRequest request) {
    Rating rating = ratingRepository.findByContentIdAndUserId(request.getContentId(), userId)
        .map(existing -> {
          existing.setRatingValue(request.getRatingValue());
          existing.setUpdatedAt(Instant.now());
          return ratingRepository.save(existing);
        })
        .orElseGet(() -> {
          Rating newRating = Rating.builder()
              .contentId(request.getContentId())
              .userId(userId)
              .ratingValue(request.getRatingValue())
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();
          return ratingRepository.save(newRating);
        });
    
    Review review = reviewRepository.findByRatingId(rating.getId())
        .map(existing -> {
          existing.setTitle(request.getTitle());
          existing.setContent(request.getContent());
          existing.setUpdatedAt(Instant.now());
          return existing;
        })
        .orElseGet(() -> {
          Review newReview = Review.builder()
              .ratingId(rating.getId())
              .contentId(request.getContentId())
              .userId(userId)
              .title(request.getTitle())
              .content(request.getContent())
              .status(ReviewStatus.ACTIVE)
              .isVerifiedPurchase(false)
              .helpfulCount(0)
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();
          return newReview;
        });
    
    review = reviewRepository.save(review);
    statisticsService.updateStatistics(request.getContentId());
    
    return reviewMapper.toDto(review);
  }
  
  @Override
  public ReviewDto getReviewById(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.REVIEW_NOT_FOUND, new Object[]{reviewId.toString()}));
    return reviewMapper.toDto(review);
  }
  
  @Override
  public Page<ReviewDto> getReviewsByContent(String contentId, ReviewStatus status, Pageable pageable) {
    ReviewStatus filterStatus = status != null ? status : ReviewStatus.ACTIVE;
    return reviewRepository.findByContentIdAndStatus(contentId, filterStatus, pageable)
        .map(reviewMapper::toDto);
  }
  
  @Override
  public Page<ReviewDto> getReviewsByUser(String userId, Pageable pageable) {
    return reviewRepository.findByUserId(userId, pageable)
        .map(reviewMapper::toDto);
  }
  
  @Override
  public void markAsHelpful(String userId, Long reviewId) {
    if (!reviewRepository.existsById(reviewId)) {
      throw new DevSharingException(ExceptionEnum.REVIEW_NOT_FOUND, new  Object[]{reviewId.toString()});
    }
    
    if (reviewHelpfulRepository.existsByReviewIdAndUserId(reviewId, userId)) {
      throw new DevSharingException(ExceptionEnum.REVIEW_ALREADY_MARKED_HELPFUL, null);
    }
    
    ReviewHelpful helpful = ReviewHelpful.builder()
        .reviewId(reviewId)
        .userId(userId)
        .createdAt(Instant.now())
        .build();
    reviewHelpfulRepository.save(helpful);
    
    Review review = reviewRepository.findById(reviewId).orElseThrow();
    review.setHelpfulCount(review.getHelpfulCount() + 1);
    reviewRepository.save(review);
  }
  
  @Override
  public void deleteReview(String userId, Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.REVIEW_NOT_FOUND, new Object[]{reviewId.toString()}));
    
    if (!review.getUserId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED_ACTION, null);
    }
    
    review.setStatus(ReviewStatus.DELETED);
    reviewRepository.save(review);
    statisticsService.updateStatistics(review.getContentId());
  }
  
  @Override
  public void moderateReview(Long reviewId, ModerateReviewRequest request) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.REVIEW_NOT_FOUND, new  Object[]{reviewId.toString()}));
    
    review.setStatus(request.getStatus());
    review.setUpdatedAt(Instant.now());
    reviewRepository.save(review);
    statisticsService.updateStatistics(review.getContentId());
  }
}
