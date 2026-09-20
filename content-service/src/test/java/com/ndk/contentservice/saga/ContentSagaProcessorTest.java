package com.ndk.contentservice.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentStatus;
import com.ndk.contentservice.entity.ProcessedSagaCommand;
import com.ndk.contentservice.repository.ContentRepository;
import com.ndk.contentservice.repository.ProcessedSagaCommandRepository;
import com.ndk.contentservice.service.ContentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentSagaProcessorTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentService contentService;

  @Mock
  private ProcessedSagaCommandRepository processedSagaCommandRepository;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private ContentSagaProcessor contentSagaProcessor;

  @Nested
  @DisplayName("processVerifyContent tests")
  class ProcessVerifyContentTests {

    @Test
    @DisplayName("Should return success reply when content is published and buyer is not creator")
    void processVerifyContent_Success() {
      Long contentId = 1L;
      String buyerId = "100";
      Content content = Content.builder()
          .id(contentId)
          .title("Saga Pattern")
          .price(new BigDecimal("50.00"))
          .creatorId(200L)
          .status(ContentStatus.PUBLISHED)
          .build();

      SagaCommand command = SagaCommand.builder()
          .sagaId("saga-123")
          .commandType("VERIFY_CONTENT")
          .idempotencyKey("saga-123-VERIFY_CONTENT")
          .replyTopic("purchase.saga-replies")
          .timestamp(Instant.now())
          .payload(Map.of("contentId", contentId, "buyerId", buyerId))
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      SagaReply reply = contentSagaProcessor.processVerifyContent(command);

      assertThat(reply).isNotNull();
      assertThat(reply.isSuccess()).isTrue();
      assertThat(reply.getPayload().get("contentId")).isEqualTo(1L);
      assertThat(reply.getPayload().get("price")).isEqualTo("50.00");
      assertThat(reply.getPayload().get("creatorId")).isEqualTo("200");
      verify(processedSagaCommandRepository).save(any(ProcessedSagaCommand.class));
    }

    @Test
    @DisplayName("Should return error reply when content not found")
    void processVerifyContent_ContentNotFound() {
      Long contentId = 999L;
      SagaCommand command = SagaCommand.builder()
          .sagaId("saga-123")
          .commandType("VERIFY_CONTENT")
          .idempotencyKey("saga-123-VERIFY_CONTENT")
          .payload(Map.of("contentId", contentId, "buyerId", "100"))
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      SagaReply reply = contentSagaProcessor.processVerifyContent(command);

      assertThat(reply.isSuccess()).isFalse();
      assertThat(reply.getErrorCode()).isEqualTo("CONTENT_NOT_FOUND");
      verify(processedSagaCommandRepository).save(any(ProcessedSagaCommand.class));
    }

    @Test
    @DisplayName("Should return error reply when content is not published")
    void processVerifyContent_ContentNotPublished() {
      Long contentId = 1L;
      Content content = Content.builder()
          .id(contentId)
          .status(ContentStatus.DRAFT)
          .creatorId(200L)
          .build();

      SagaCommand command = SagaCommand.builder()
          .sagaId("saga-123")
          .commandType("VERIFY_CONTENT")
          .idempotencyKey("saga-123-VERIFY_CONTENT")
          .payload(Map.of("contentId", contentId, "buyerId", "100"))
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      SagaReply reply = contentSagaProcessor.processVerifyContent(command);

      assertThat(reply.isSuccess()).isFalse();
      assertThat(reply.getErrorCode()).isEqualTo("CONTENT_NOT_PUBLISHED");
      verify(processedSagaCommandRepository).save(any(ProcessedSagaCommand.class));
    }

    @Test
    @DisplayName("Should return error reply when buyer is the creator")
    void processVerifyContent_BuyerIsCreator() {
      Long contentId = 1L;
      String buyerId = "200";
      Content content = Content.builder()
          .id(contentId)
          .status(ContentStatus.PUBLISHED)
          .creatorId(200L)
          .build();

      SagaCommand command = SagaCommand.builder()
          .sagaId("saga-123")
          .commandType("VERIFY_CONTENT")
          .idempotencyKey("saga-123-VERIFY_CONTENT")
          .payload(Map.of("contentId", contentId, "buyerId", buyerId))
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      SagaReply reply = contentSagaProcessor.processVerifyContent(command);

      assertThat(reply.isSuccess()).isFalse();
      assertThat(reply.getErrorCode()).isEqualTo("CANNOT_BUY_OWN_CONTENT");
      verify(processedSagaCommandRepository).save(any(ProcessedSagaCommand.class));
    }
  }

  @Nested
  @DisplayName("processIncrementPurchaseCount tests")
  class ProcessIncrementPurchaseCountTests {

    @Test
    @DisplayName("Should increment purchase count successfully")
    void processIncrementPurchaseCount_Success() {
      Long contentId = 1L;
      SagaCommand command = SagaCommand.builder()
          .sagaId("saga-123")
          .commandType("INCREMENT_PURCHASE_COUNT")
          .idempotencyKey("saga-123-INCREMENT_PURCHASE_COUNT")
          .payload(Map.of("contentId", contentId))
          .build();

      SagaReply reply = contentSagaProcessor.processIncrementPurchaseCount(command);

      assertThat(reply.isSuccess()).isTrue();
      verify(contentService).incrementPurchaseCount(contentId);
      verify(processedSagaCommandRepository).save(any(ProcessedSagaCommand.class));
    }

    @Test
    @DisplayName("Should return error reply when contentService throws exception")
    void processIncrementPurchaseCount_ThrowsException() {
      Long contentId = 1L;
      SagaCommand command = SagaCommand.builder()
          .sagaId("saga-123")
          .commandType("INCREMENT_PURCHASE_COUNT")
          .idempotencyKey("saga-123-INCREMENT_PURCHASE_COUNT")
          .payload(Map.of("contentId", contentId))
          .build();

      doThrow(new RuntimeException("DB Connection Timeout"))
          .when(contentService).incrementPurchaseCount(contentId);

      SagaReply reply = contentSagaProcessor.processIncrementPurchaseCount(command);

      assertThat(reply.isSuccess()).isFalse();
      assertThat(reply.getErrorCode()).isEqualTo("INCREMENT_PURCHASE_COUNT_ERROR");
      verify(processedSagaCommandRepository).save(any(ProcessedSagaCommand.class));
    }
  }
}
