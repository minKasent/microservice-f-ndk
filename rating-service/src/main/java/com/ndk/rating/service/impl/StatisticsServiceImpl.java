package com.ndk.rating.service.impl;

import com.ndk.rating.dto.response.ContentStatisticsDto;
import com.ndk.rating.entity.ContentStatistics;
import com.ndk.rating.entity.Rating;
import com.ndk.rating.enums.ReviewStatus;
import com.ndk.rating.mapper.ContentStatisticsMapper;
import com.ndk.rating.repository.ContentStatisticsRepository;
import com.ndk.rating.repository.RatingRepository;
import com.ndk.rating.repository.ReviewRepository;
import com.ndk.rating.service.StatisticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
  private final ContentStatisticsRepository statisticsRepository;
  private final RatingRepository ratingRepository;
  private final ReviewRepository reviewRepository;
  private final ContentStatisticsMapper statisticsMapper;
  
  @Override
  public ContentStatisticsDto getContentStatistics(String contentId) {
    ContentStatistics stats = statisticsRepository.findByContentId(contentId)
        .orElseGet(() -> {
          ContentStatistics newStats = ContentStatistics.builder()
              .contentId(contentId)
              .totalRatings(0)
              .averageRating(BigDecimal.ZERO)
              .rating1Count(0)
              .rating2Count(0)
              .rating3Count(0)
              .rating4Count(0)
              .rating5Count(0)
              .totalReviews(0)
              .updatedAt(Instant.now())
              .build();
          return statisticsRepository.save(newStats);
        });
    
    return statisticsMapper.toDto(stats);
  }
  
  @Override
  public void updateStatistics(String contentId) {
    List<Rating> ratings = ratingRepository.findByContentId(contentId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    
    int totalRatings = ratings.size();
    int rating1Count = 0;
    int rating2Count = 0;
    int rating3Count = 0;
    int rating4Count = 0;
    int rating5Count = 0;
    int totalRatingValue = 0;
    
    for (Rating rating : ratings) {
      totalRatingValue += rating.getRatingValue();
      switch (rating.getRatingValue()) {
        case 1 -> rating1Count++;
        case 2 -> rating2Count++;
        case 3 -> rating3Count++;
        case 4 -> rating4Count++;
        case 5 -> rating5Count++;
      }
    }
    
    BigDecimal averageRating = totalRatings > 0
        ? BigDecimal.valueOf(totalRatingValue)
            .divide(BigDecimal.valueOf(totalRatings), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;
    
    long totalReviews = reviewRepository.countByContentIdAndStatus(contentId, ReviewStatus.ACTIVE);
    
    ContentStatistics stats = statisticsRepository.findByContentId(contentId)
        .orElseGet(() -> ContentStatistics.builder()
            .contentId(contentId)
            .build());
    
    stats.setTotalRatings(totalRatings);
    stats.setAverageRating(averageRating);
    stats.setRating1Count(rating1Count);
    stats.setRating2Count(rating2Count);
    stats.setRating3Count(rating3Count);
    stats.setRating4Count(rating4Count);
    stats.setRating5Count(rating5Count);
    stats.setTotalReviews((int) totalReviews);
    stats.setUpdatedAt(Instant.now());
    
    statisticsRepository.save(stats);
  }
}
