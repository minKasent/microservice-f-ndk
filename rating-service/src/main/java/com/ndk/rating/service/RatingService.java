package com.ndk.rating.service;

import com.ndk.rating.dto.request.CreateRatingRequest;
import com.ndk.rating.dto.response.RatingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatingService {
  RatingDto createOrUpdateRating(String userId, CreateRatingRequest request);
  
  RatingDto getUserRatingForContent(String userId, String contentId);
  
  void deleteRating(String userId, String contentId);
  
  Page<RatingDto> getUserRatings(String userId, Pageable pageable);
}
