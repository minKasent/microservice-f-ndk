package com.ndk.contentservice.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.client.PurchaseServiceClient;
import com.ndk.contentservice.dto.request.ContentSearchRequest;
import com.ndk.contentservice.dto.request.CreateContentRequest;
import com.ndk.contentservice.dto.request.UpdateContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentSummaryDto;
import com.ndk.contentservice.entity.Category;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentStatus;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.ContentMapper;
import com.ndk.contentservice.repository.CategoryRepository;
import com.ndk.contentservice.repository.ContentRepository;
import com.ndk.contentservice.service.ContentService;
import com.ndk.contentservice.specification.ContentSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@LogExecutionTime
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentServiceImpl implements ContentService {

  private final ContentRepository contentRepository;
  private final CategoryRepository categoryRepository;
  private final ContentMapper contentMapper;
  private final PurchaseServiceClient purchaseServiceClient;

  @Override
  @Transactional
  public ContentDto createContent(CreateContentRequest request, Long creatorId) {
    log.info("Creating content for creator: {}", creatorId);

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CATEGORY_NOT_FOUND, null));

    Content content = Content.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .creatorId(creatorId)
        .category(category)
        .status(ContentStatus.DRAFT)
        .level(request.getLevel())
        .price(request.getPrice())
        .thumbnail(request.getThumbnail())
        .viewCount(0L)
        .purchaseCount(0L)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    content = contentRepository.save(content);
    log.info("Content created successfully with id: {}", content.getId());

    return contentMapper.toDto(content);
  }

  @Override
  @Transactional
  public ContentDto updateContent(Long contentId, UpdateContentRequest request, Long userId) {
    log.info("Updating content {} by user {}", contentId, userId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check ownership
    if (!content.getCreatorId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }

    // Update fields
    if (request.getTitle() != null) {
      content.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      content.setDescription(request.getDescription());
    }
    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new DevSharingException(ExceptionEnum.CATEGORY_NOT_FOUND, null));
      content.setCategory(category);
    }
    if (request.getLevel() != null) {
      content.setLevel(request.getLevel());
    }
    if (request.getPrice() != null) {
      content.setPrice(request.getPrice());
    }
    if (request.getThumbnail() != null) {
      content.setThumbnail(request.getThumbnail());
    }

    content.setUpdatedAt(Instant.now());
    content = contentRepository.save(content);

    log.info("Content {} updated successfully", contentId);
    return contentMapper.toDto(content);
  }

  @Override
  @Transactional(readOnly = true)
  public ContentDto getContentById(Long contentId, Long userId) {
    log.info("Getting content {} for user {}", contentId, userId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check access permission
    // Creator can always see their own content
    if (content.getCreatorId().equals(userId)) {
      return contentMapper.toDto(content);
    }
    
    // For non-creators, content must be published
    if (content.getStatus() != ContentStatus.PUBLISHED) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }
    
    // Published content can be viewed by anyone (they'll see free blocks or need to purchase for full access)
    return contentMapper.toDto(content);
  }

  @Override
  @Transactional
  public void deleteContent(Long contentId, Long userId) {
    log.info("Deleting content {} by user {}", contentId, userId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check ownership
    if (!content.getCreatorId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }

    contentRepository.delete(content);
    log.info("Content {} deleted successfully", contentId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ContentSummaryDto> searchContents(ContentSearchRequest request) {
    log.info("Searching contents with filters: {}", request);

    Specification<Content> spec = ContentSpecification.withFilters(request);
    Pageable pageable = createPageable(request);

    Page<Content> contentPage = contentRepository.findAll(spec, pageable);
    return contentPage.map(contentMapper::toSummaryDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ContentSummaryDto> getPublishedContents(ContentSearchRequest request) {
    log.info("Getting published contents with filters: {}", request);

    // Force status to PUBLISHED
    request.setStatus(ContentStatus.PUBLISHED);
    
    Specification<Content> spec = ContentSpecification.withFilters(request);
    Pageable pageable = createPageable(request);

    Page<Content> contentPage = contentRepository.findAll(spec, pageable);
    return contentPage.map(contentMapper::toSummaryDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ContentSummaryDto> getContentsByCreator(Long creatorId, ContentSearchRequest request) {
    log.info("Getting contents by creator {} with filters: {}", creatorId, request);

    request.setCreatorId(creatorId);
    Specification<Content> spec = ContentSpecification.withFilters(request);
    Pageable pageable = createPageable(request);

    Page<Content> contentPage = contentRepository.findAll(spec, pageable);
    return contentPage.map(contentMapper::toSummaryDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ContentSummaryDto> getContentsByCategory(Long categoryId, ContentSearchRequest request) {
    log.info("Getting contents by category {} with filters: {}", categoryId, request);

    request.setCategoryId(categoryId);
    request.setStatus(ContentStatus.PUBLISHED); // Only published contents for category view
    
    Specification<Content> spec = ContentSpecification.withFilters(request);
    Pageable pageable = createPageable(request);

    Page<Content> contentPage = contentRepository.findAll(spec, pageable);
    return contentPage.map(contentMapper::toSummaryDto);
  }

  @Override
  @Transactional
  public ContentDto publishContent(Long contentId, Long userId) {
    log.info("Publishing content {} by user {}", contentId, userId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check ownership
    if (!content.getCreatorId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }

    // Check if content can be published
    if (content.getStatus() != ContentStatus.APPROVED && content.getStatus() != ContentStatus.DRAFT) {
      throw new DevSharingException(ExceptionEnum.CONTENT_CANNOT_BE_PUBLISHED, null);
    }

    content.setStatus(ContentStatus.PUBLISHED);
    content.setPublishedAt(Instant.now());
    content.setUpdatedAt(Instant.now());
    content = contentRepository.save(content);

    log.info("Content {} published successfully", contentId);
    return contentMapper.toDto(content);
  }

  @Override
  @Transactional
  public ContentDto archiveContent(Long contentId, Long userId) {
    log.info("Archiving content {} by user {}", contentId, userId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check ownership
    if (!content.getCreatorId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }

    content.setStatus(ContentStatus.ARCHIVED);
    content.setUpdatedAt(Instant.now());
    content = contentRepository.save(content);

    log.info("Content {} archived successfully", contentId);
    return contentMapper.toDto(content);
  }

  @Override
  @Transactional
  public ContentDto submitForReview(Long contentId, Long userId) {
    log.info("Submitting content {} for review by user {}", contentId, userId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check ownership
    if (!content.getCreatorId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }

    // Check if content can be submitted for review
    if (content.getStatus() != ContentStatus.DRAFT && content.getStatus() != ContentStatus.REJECTED) {
      throw new DevSharingException(ExceptionEnum.CONTENT_CANNOT_BE_SUBMITTED, null);
    }

    content.setStatus(ContentStatus.PENDING_REVIEW);
    content.setUpdatedAt(Instant.now());
    content = contentRepository.save(content);

    log.info("Content {} submitted for review successfully", contentId);
    return contentMapper.toDto(content);
  }

  @Override
  @Transactional
  public void incrementViewCount(Long contentId) {
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    long currentViews = content.getViewCount() != null ? content.getViewCount() : 0L;
    content.setViewCount(currentViews + 1L);
    contentRepository.save(content);
  }

  @Override
  @Transactional
  public void incrementPurchaseCount(Long contentId) {
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    long currentPurchases = content.getPurchaseCount() != null ? content.getPurchaseCount() : 0L;
    content.setPurchaseCount(currentPurchases + 1L);
    contentRepository.save(content);
  }

  private Pageable createPageable(ContentSearchRequest request) {
    int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
    int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
    
    // Default sort by createdAt desc
    String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
    String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";
    
    Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, sortBy);
    
    return PageRequest.of(page, size, sort);
  }

  /**
   * Check if current user owns the content (has purchased it)
   * Calls purchase-service to verify ownership
   */
  private boolean checkUserOwnership(Long contentId) {
    try {
      ApiResponse<Boolean> response = purchaseServiceClient.checkOwnership(contentId);
      return response.getData() != null && response.getData();
    } catch (Exception e) {
      log.error("Error checking content ownership for contentId: {}", contentId, e);
      // If purchase-service is down or error occurs, deny access for safety
      return false;
    }
  }
}
