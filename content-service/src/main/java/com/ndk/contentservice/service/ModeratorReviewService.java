package com.ndk.contentservice.service;

import com.ndk.contentservice.dto.request.ReviewContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentReviewDto;
import java.util.List;

public interface ModeratorReviewService {
  
  /**
   * Moderator reviews content (approve/reject/request changes)
   * This is different from user review/rating which is in rating-service
   */
  ContentDto reviewContent(Long contentId, ReviewContentRequest request, Long moderatorId);
  
  /**
   * Get all review history for a content
   */
  List<ContentReviewDto> getContentReviewHistory(Long contentId);
  
  /**
   * Get all contents pending review
   */
  List<ContentDto> getPendingReviewContents();
}
