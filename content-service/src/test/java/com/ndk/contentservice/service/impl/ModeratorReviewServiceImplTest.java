package com.ndk.contentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModeratorReviewServiceImplTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentReviewRepository contentReviewRepository;

  @Mock
  private ContentMapper contentMapper;

  @Mock
  private ContentReviewMapper contentReviewMapper;

  @InjectMocks
  private ModeratorReviewServiceImpl moderatorReviewService;

  @Nested
  @DisplayName("reviewContent tests")
  class ReviewContentTests {

    @Test
    @DisplayName("Should approve content successfully when status is PENDING_REVIEW")
    void reviewContent_Approve_Success() {
      Long contentId = 1L;
      Long moderatorId = 99L;
      Content content = Content.builder()
          .id(contentId)
          .status(ContentStatus.PENDING_REVIEW)
          .title("Test Course")
          .creatorId(10L)
          .build();

      ReviewContentRequest request = ReviewContentRequest.builder()
          .action(ReviewAction.APPROVE)
          .feedback("Looks great!")
          .build();

      ContentDto expectedDto = ContentDto.builder()
          .id(contentId)
          .status(ContentStatus.APPROVED)
          .title("Test Course")
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(any(Content.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(contentMapper.toDto(any(Content.class))).thenReturn(expectedDto);

      ContentDto result = moderatorReviewService.reviewContent(contentId, request, moderatorId);

      assertThat(result).isNotNull();
      assertThat(result.getStatus()).isEqualTo(ContentStatus.APPROVED);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.APPROVED);
      verify(contentReviewRepository).save(any(ContentReview.class));
      verify(contentRepository).save(content);
    }

    @Test
    @DisplayName("Should reject content successfully when action is REJECT")
    void reviewContent_Reject_Success() {
      Long contentId = 1L;
      Long moderatorId = 99L;
      Content content = Content.builder()
          .id(contentId)
          .status(ContentStatus.PENDING_REVIEW)
          .build();

      ReviewContentRequest request = ReviewContentRequest.builder()
          .action(ReviewAction.REJECT)
          .feedback("Violates quality guidelines")
          .build();

      ContentDto expectedDto = ContentDto.builder()
          .id(contentId)
          .status(ContentStatus.REJECTED)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(any(Content.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(contentMapper.toDto(any(Content.class))).thenReturn(expectedDto);

      ContentDto result = moderatorReviewService.reviewContent(contentId, request, moderatorId);

      assertThat(result.getStatus()).isEqualTo(ContentStatus.REJECTED);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.REJECTED);
    }

    @Test
    @DisplayName("Should set content to DRAFT when action is REQUEST_CHANGES")
    void reviewContent_RequestChanges_Success() {
      Long contentId = 1L;
      Long moderatorId = 99L;
      Content content = Content.builder()
          .id(contentId)
          .status(ContentStatus.PENDING_REVIEW)
          .build();

      ReviewContentRequest request = ReviewContentRequest.builder()
          .action(ReviewAction.REQUEST_CHANGES)
          .feedback("Please update the thumbnail")
          .build();

      ContentDto expectedDto = ContentDto.builder()
          .id(contentId)
          .status(ContentStatus.DRAFT)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(any(Content.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(contentMapper.toDto(any(Content.class))).thenReturn(expectedDto);

      ContentDto result = moderatorReviewService.reviewContent(contentId, request, moderatorId);

      assertThat(result.getStatus()).isEqualTo(ContentStatus.DRAFT);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.DRAFT);
    }

    @Test
    @DisplayName("Should throw CONTENT_NOT_FOUND when content does not exist")
    void reviewContent_ThrowsException_WhenContentNotFound() {
      Long contentId = 999L;
      ReviewContentRequest request = ReviewContentRequest.builder()
          .action(ReviewAction.APPROVE)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> moderatorReviewService.reviewContent(contentId, request, 1L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CONTENT_NOT_FOUND.getErrorCode());
          });
    }

    @Test
    @DisplayName("Should throw CONTENT_NOT_PENDING_REVIEW when content status is not PENDING_REVIEW")
    void reviewContent_ThrowsException_WhenNotPendingReview() {
      Long contentId = 1L;
      Content content = Content.builder()
          .id(contentId)
          .status(ContentStatus.DRAFT)
          .build();

      ReviewContentRequest request = ReviewContentRequest.builder()
          .action(ReviewAction.APPROVE)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> moderatorReviewService.reviewContent(contentId, request, 1L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CONTENT_NOT_PENDING_REVIEW.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("getContentReviewHistory tests")
  class ReviewHistoryTests {

    @Test
    @DisplayName("Should return review history when content exists")
    void getContentReviewHistory_Success() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).build();
      ContentReview review1 = ContentReview.builder()
          .id(101L)
          .content(content)
          .moderatorId(99L)
          .action(ReviewAction.APPROVE)
          .reviewedAt(Instant.now())
          .build();

      ContentReviewDto reviewDto = ContentReviewDto.builder()
          .id(101L)
          .action(ReviewAction.APPROVE)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentReviewRepository.findByContentOrderByReviewedAtDesc(content)).thenReturn(List.of(review1));
      when(contentReviewMapper.toDto(review1)).thenReturn(reviewDto);

      List<ContentReviewDto> result = moderatorReviewService.getContentReviewHistory(contentId);

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("Should throw CONTENT_NOT_FOUND when getting review history for nonexistent content")
    void getContentReviewHistory_ThrowsException_WhenContentNotFound() {
      Long contentId = 999L;
      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> moderatorReviewService.getContentReviewHistory(contentId))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CONTENT_NOT_FOUND.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("getPendingReviewContents tests")
  class PendingReviewContentsTests {

    @Test
    @DisplayName("Should return list of contents pending review")
    void getPendingReviewContents_Success() {
      Content content1 = Content.builder().id(1L).status(ContentStatus.PENDING_REVIEW).build();
      Content content2 = Content.builder().id(2L).status(ContentStatus.PENDING_REVIEW).build();
      ContentDto dto1 = ContentDto.builder().id(1L).build();
      ContentDto dto2 = ContentDto.builder().id(2L).build();

      when(contentRepository.findByStatusOrderByUpdatedAtAsc(ContentStatus.PENDING_REVIEW))
          .thenReturn(List.of(content1, content2));
      when(contentMapper.toDto(content1)).thenReturn(dto1);
      when(contentMapper.toDto(content2)).thenReturn(dto2);

      List<ContentDto> result = moderatorReviewService.getPendingReviewContents();

      assertThat(result).hasSize(2);
      assertThat(result.get(0).getId()).isEqualTo(1L);
      assertThat(result.get(1).getId()).isEqualTo(2L);
    }
  }
}
