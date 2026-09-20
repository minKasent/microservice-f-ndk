package com.ndk.contentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.contentservice.client.PurchaseServiceClient;
import com.ndk.contentservice.dto.request.ContentSearchRequest;
import com.ndk.contentservice.dto.request.CreateContentRequest;
import com.ndk.contentservice.dto.request.UpdateContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentSummaryDto;
import com.ndk.contentservice.entity.Category;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentLevel;
import com.ndk.contentservice.entity.ContentStatus;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.ContentMapper;
import com.ndk.contentservice.repository.CategoryRepository;
import com.ndk.contentservice.repository.ContentRepository;
import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ContentMapper contentMapper;

  @Mock
  private PurchaseServiceClient purchaseServiceClient;

  @InjectMocks
  private ContentServiceImpl contentService;

  @Nested
  @DisplayName("createContent tests")
  class CreateContentTests {

    @Test
    @DisplayName("Should create content in DRAFT status when category exists")
    void createContent_Success() {
      Long creatorId = 10L;
      CreateContentRequest request = CreateContentRequest.builder()
          .title("Spring Cloud Microservices")
          .description("Mastering Spring Cloud")
          .categoryId(1L)
          .level(ContentLevel.INTERMEDIATE)
          .price(new BigDecimal("99.00"))
          .thumbnail("thumb.png")
          .build();

      Category category = Category.builder().id(1L).name("Cloud").build();
      Content savedContent = Content.builder()
          .id(100L)
          .title("Spring Cloud Microservices")
          .creatorId(creatorId)
          .status(ContentStatus.DRAFT)
          .build();

      ContentDto expectedDto = ContentDto.builder()
          .id(100L)
          .title("Spring Cloud Microservices")
          .status(ContentStatus.DRAFT)
          .build();

      when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
      when(contentRepository.save(any(Content.class))).thenReturn(savedContent);
      when(contentMapper.toDto(savedContent)).thenReturn(expectedDto);

      ContentDto result = contentService.createContent(request, creatorId);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(100L);
      assertThat(result.getStatus()).isEqualTo(ContentStatus.DRAFT);
      verify(contentRepository).save(any(Content.class));
    }

    @Test
    @DisplayName("Should throw CATEGORY_NOT_FOUND when category does not exist")
    void createContent_ThrowsException_WhenCategoryNotFound() {
      CreateContentRequest request = CreateContentRequest.builder().categoryId(999L).build();
      when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> contentService.createContent(request, 10L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CATEGORY_NOT_FOUND.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("updateContent tests")
  class UpdateContentTests {

    @Test
    @DisplayName("Should update content fields successfully when called by owner")
    void updateContent_Success() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(creatorId)
          .title("Old Title")
          .price(new BigDecimal("10.00"))
          .build();

      UpdateContentRequest request = UpdateContentRequest.builder()
          .title("New Title")
          .price(new BigDecimal("20.00"))
          .build();

      ContentDto expectedDto = ContentDto.builder()
          .id(contentId)
          .title("New Title")
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(content)).thenReturn(content);
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.updateContent(contentId, request, creatorId);

      assertThat(result.getTitle()).isEqualTo("New Title");
      assertThat(content.getTitle()).isEqualTo("New Title");
      assertThat(content.getPrice()).isEqualTo(new BigDecimal("20.00"));
      verify(contentRepository).save(content);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when updater is not the creator")
    void updateContent_ThrowsUnauthorized_WhenNotCreator() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).creatorId(10L).build();
      UpdateContentRequest request = UpdateContentRequest.builder().title("Hack").build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> contentService.updateContent(contentId, request, 999L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.UNAUTHORIZED.getErrorCode());
          });
    }

    @Test
    @DisplayName("Should throw CONTENT_NOT_FOUND when updating nonexistent content")
    void updateContent_ThrowsNotFound_WhenContentDoesNotExist() {
      Long contentId = 999L;
      UpdateContentRequest request = UpdateContentRequest.builder().title("New").build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> contentService.updateContent(contentId, request, 10L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CONTENT_NOT_FOUND.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("getContentById tests")
  class GetContentByIdTests {

    @Test
    @DisplayName("Creator can view their own content even when not published")
    void getContentById_CreatorCanViewOwnUnpublishedContent() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(creatorId)
          .status(ContentStatus.DRAFT)
          .build();

      ContentDto expectedDto = ContentDto.builder().id(contentId).status(ContentStatus.DRAFT).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.getContentById(contentId, creatorId);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(contentId);
    }

    @Test
    @DisplayName("Non-creator can view published content")
    void getContentById_NonCreatorCanViewPublishedContent() {
      Long contentId = 1L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(10L)
          .status(ContentStatus.PUBLISHED)
          .build();

      ContentDto expectedDto = ContentDto.builder().id(contentId).status(ContentStatus.PUBLISHED).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.getContentById(contentId, 999L);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(contentId);
    }

    @Test
    @DisplayName("Non-creator cannot view unpublished content and gets UNAUTHORIZED")
    void getContentById_NonCreatorCannotViewUnpublishedContent() {
      Long contentId = 1L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(10L)
          .status(ContentStatus.DRAFT)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> contentService.getContentById(contentId, 999L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.UNAUTHORIZED.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("deleteContent tests")
  class DeleteContentTests {

    @Test
    @DisplayName("Owner should successfully delete content")
    void deleteContent_Success() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder().id(contentId).creatorId(creatorId).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      contentService.deleteContent(contentId, creatorId);

      verify(contentRepository).delete(content);
    }

    @Test
    @DisplayName("Non-owner cannot delete content")
    void deleteContent_ThrowsUnauthorized_WhenNotCreator() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).creatorId(10L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> contentService.deleteContent(contentId, 999L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.UNAUTHORIZED.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("publishContent tests")
  class PublishContentTests {

    @Test
    @DisplayName("Should publish content when status is APPROVED")
    void publishContent_Success_FromApproved() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(creatorId)
          .status(ContentStatus.APPROVED)
          .build();

      ContentDto expectedDto = ContentDto.builder().id(contentId).status(ContentStatus.PUBLISHED).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(content)).thenReturn(content);
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.publishContent(contentId, creatorId);

      assertThat(result.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
      assertThat(content.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should publish content when status is DRAFT")
    void publishContent_Success_FromDraft() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(creatorId)
          .status(ContentStatus.DRAFT)
          .build();

      ContentDto expectedDto = ContentDto.builder().id(contentId).status(ContentStatus.PUBLISHED).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(content)).thenReturn(content);
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.publishContent(contentId, creatorId);

      assertThat(result.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should throw CONTENT_CANNOT_BE_PUBLISHED when status is REJECTED")
    void publishContent_ThrowsException_WhenStatusInvalid() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder()
          .id(contentId)
          .creatorId(creatorId)
          .status(ContentStatus.REJECTED)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> contentService.publishContent(contentId, creatorId))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CONTENT_CANNOT_BE_PUBLISHED.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("archiveContent & submitForReview tests")
  class LifecycleTests {

    @Test
    @DisplayName("Should archive content successfully")
    void archiveContent_Success() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder().id(contentId).creatorId(creatorId).status(ContentStatus.PUBLISHED).build();
      ContentDto expectedDto = ContentDto.builder().id(contentId).status(ContentStatus.ARCHIVED).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(content)).thenReturn(content);
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.archiveContent(contentId, creatorId);

      assertThat(result.getStatus()).isEqualTo(ContentStatus.ARCHIVED);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.ARCHIVED);
    }

    @Test
    @DisplayName("Should submit draft content for review")
    void submitForReview_Success() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder().id(contentId).creatorId(creatorId).status(ContentStatus.DRAFT).build();
      ContentDto expectedDto = ContentDto.builder().id(contentId).status(ContentStatus.PENDING_REVIEW).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(contentRepository.save(content)).thenReturn(content);
      when(contentMapper.toDto(content)).thenReturn(expectedDto);

      ContentDto result = contentService.submitForReview(contentId, creatorId);

      assertThat(result.getStatus()).isEqualTo(ContentStatus.PENDING_REVIEW);
      assertThat(content.getStatus()).isEqualTo(ContentStatus.PENDING_REVIEW);
    }

    @Test
    @DisplayName("Should throw CONTENT_CANNOT_BE_SUBMITTED when already in PENDING_REVIEW")
    void submitForReview_ThrowsException_WhenAlreadyPending() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder().id(contentId).creatorId(creatorId).status(ContentStatus.PENDING_REVIEW).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> contentService.submitForReview(contentId, creatorId))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CONTENT_CANNOT_BE_SUBMITTED.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("increment counters tests")
  class CounterTests {

    @Test
    @DisplayName("Should increment view count")
    void incrementViewCount_Success() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).viewCount(5L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      contentService.incrementViewCount(contentId);

      assertThat(content.getViewCount()).isEqualTo(6L);
      verify(contentRepository).save(content);
    }

    @Test
    @DisplayName("Should increment purchase count")
    void incrementPurchaseCount_Success() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).purchaseCount(2L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      contentService.incrementPurchaseCount(contentId);

      assertThat(content.getPurchaseCount()).isEqualTo(3L);
      verify(contentRepository).save(content);
    }
  }

  @Nested
  @DisplayName("search and list tests")
  class SearchTests {

    @Test
    @DisplayName("searchContents should return mapped summary page")
    void searchContents_Success() {
      ContentSearchRequest request = new ContentSearchRequest();
      request.setPage(0);
      request.setSize(10);

      Content content = Content.builder().id(1L).title("Test").build();
      ContentSummaryDto summaryDto = ContentSummaryDto.builder().id(1L).title("Test").build();

      Page<Content> page = new PageImpl<>(List.of(content));
      when(contentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(contentMapper.toSummaryDto(content)).thenReturn(summaryDto);

      Page<ContentSummaryDto> result = contentService.searchContents(request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test");
    }

    @Test
    @DisplayName("getPublishedContents should set status to PUBLISHED and return page")
    void getPublishedContents_Success() {
      ContentSearchRequest request = new ContentSearchRequest();
      Content content = Content.builder().id(1L).title("Published").status(ContentStatus.PUBLISHED).build();
      ContentSummaryDto summaryDto = ContentSummaryDto.builder().id(1L).title("Published").build();

      Page<Content> page = new PageImpl<>(List.of(content));
      when(contentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(contentMapper.toSummaryDto(content)).thenReturn(summaryDto);

      Page<ContentSummaryDto> result = contentService.getPublishedContents(request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(request.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
    }

    @Test
    @DisplayName("getContentsByCreator should filter by creatorId and return page")
    void getContentsByCreator_Success() {
      ContentSearchRequest request = new ContentSearchRequest();
      Content content = Content.builder().id(1L).creatorId(10L).build();
      ContentSummaryDto summaryDto = ContentSummaryDto.builder().id(1L).build();

      Page<Content> page = new PageImpl<>(List.of(content));
      when(contentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(contentMapper.toSummaryDto(content)).thenReturn(summaryDto);

      Page<ContentSummaryDto> result = contentService.getContentsByCreator(10L, request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(request.getCreatorId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getContentsByCategory should filter by categoryId and return page")
    void getContentsByCategory_Success() {
      ContentSearchRequest request = new ContentSearchRequest();
      Content content = Content.builder().id(1L).build();
      ContentSummaryDto summaryDto = ContentSummaryDto.builder().id(1L).build();

      Page<Content> page = new PageImpl<>(List.of(content));
      when(contentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(contentMapper.toSummaryDto(content)).thenReturn(summaryDto);

      Page<ContentSummaryDto> result = contentService.getContentsByCategory(5L, request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(request.getCategoryId()).isEqualTo(5L);
      assertThat(request.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
    }
  }
}
