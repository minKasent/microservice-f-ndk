package com.ndk.contentservice.service;

import com.ndk.contentservice.dto.request.ContentSearchRequest;
import com.ndk.contentservice.dto.request.CreateContentRequest;
import com.ndk.contentservice.dto.request.UpdateContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentSummaryDto;
import com.ndk.contentservice.entity.ContentStatus;
import org.springframework.data.domain.Page;

public interface ContentService {
  
  // CRUD operations
  ContentDto createContent(CreateContentRequest request, Long creatorId);
  
  ContentDto updateContent(Long contentId, UpdateContentRequest request, Long userId);
  
  ContentDto getContentById(Long contentId, Long userId);
  
  void deleteContent(Long contentId, Long userId);
  
  // Search and filter with pagination
  Page<ContentSummaryDto> searchContents(ContentSearchRequest request);
  
  Page<ContentSummaryDto> getPublishedContents(ContentSearchRequest request);
  
  Page<ContentSummaryDto> getContentsByCreator(Long creatorId, ContentSearchRequest request);
  
  Page<ContentSummaryDto> getContentsByCategory(Long categoryId, ContentSearchRequest request);
  
  // Status management
  ContentDto publishContent(Long contentId, Long userId);
  
  ContentDto archiveContent(Long contentId, Long userId);
  
  ContentDto submitForReview(Long contentId, Long userId);
  
  // Tracking (called by other services)
  void incrementViewCount(Long contentId);
  
  void incrementPurchaseCount(Long contentId); // Called by purchase-service
}
