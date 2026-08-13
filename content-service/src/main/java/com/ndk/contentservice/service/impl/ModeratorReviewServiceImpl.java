package com.ndk.contentservice.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.contentservice.dto.request.ReviewContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentReviewDto;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentReview;
import com.ndk.contentservice.entity.ContentStatus;
import com.ndk.contentservice.entity.ReviewAction;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.ContentMapper;
import com.ndk.contentservice.mapper.ContentReviewMapper;
import com.ndk.contentservice.repository.ContentRepository;
import com.ndk.contentservice.repository.ContentReviewRepository;
import com.ndk.contentservice.service.ModeratorReviewService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModeratorReviewServiceImpl implements ModeratorReviewService {

  private final ContentRepository contentRepository;
  private final ContentReviewRepository contentReviewRepository;
  private final ContentMapper contentMapper;
  private final ContentReviewMapper contentReviewMapper;

  @Override
  @Transactional
  public ContentDto reviewContent(Long contentId, ReviewContentRequest request, Long moderatorId) {
    log.info("Moderator {} reviewing content {}", moderatorId, contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Check if content is pending review
    if (content.getStatus() != ContentStatus.PENDING_REVIEW) {
      throw new DevSharingException(ExceptionEnum.CONTENT_NOT_PENDING_REVIEW, null);
    }

    // Create review record
    ContentReview review = ContentReview.builder()
        .content(content)
        .moderatorId(moderatorId)
        .action(request.getAction())
        .feedback(request.getFeedback())
        .reviewedAt(Instant.now())
        .build();

    contentReviewRepository.save(review);

    // Update content status based on review action
    switch (request.getAction()) {
      case APPROVE:
        content.setStatus(ContentStatus.APPROVED);
        log.info("Content {} approved by moderator {}", contentId, moderatorId);
        break;
      case REJECT:
        content.setStatus(ContentStatus.REJECTED);
        log.info("Content {} rejected by moderator {}", contentId, moderatorId);
        break;
      case REQUEST_CHANGES:
        content.setStatus(ContentStatus.DRAFT);
        log.info("Content {} sent back for changes by moderator {}", contentId, moderatorId);
        break;
      default:
        throw new DevSharingException(ExceptionEnum.INVALID_REVIEW_ACTION, null);
    }

    content.setUpdatedAt(Instant.now());
    content = contentRepository.save(content);

    return contentMapper.toDto(content);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContentReviewDto> getContentReviewHistory(Long contentId) {
    log.info("Getting review history for content {}", contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    List<ContentReview> reviews = contentReviewRepository.findByContentOrderByReviewedAtDesc(content);
    return reviews.stream()
        .map(contentReviewMapper::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContentDto> getPendingReviewContents() {
    log.info("Getting all contents pending review");

    List<Content> contents = contentRepository.findByStatusOrderByUpdatedAtAsc(ContentStatus.PENDING_REVIEW);
    return contents.stream()
        .map(contentMapper::toDto)
        .toList();
  }
}
