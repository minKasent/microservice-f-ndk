package com.ndk.contentservice.saga;

import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentStatus;
import com.ndk.contentservice.entity.ProcessedSagaCommand;
import com.ndk.contentservice.repository.ContentRepository;
import com.ndk.contentservice.repository.ProcessedSagaCommandRepository;
import com.ndk.contentservice.service.ContentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSagaProcessor {

  private final ContentRepository contentRepository;
  private final ContentService contentService;
  private final ProcessedSagaCommandRepository processedSagaCommandRepository;
  private final ObjectMapper objectMapper;

  public SagaReply processVerifyContent(SagaCommand command) {
    Long contentId = ((Number) command.getPayload().get("contentId")).longValue();
    String buyerId = String.valueOf(command.getPayload().get("buyerId"));

    SagaReply reply;
    Optional<Content> contentOpt = contentRepository.findById(contentId);

    if (contentOpt.isEmpty()) {
      reply = buildErrorReply(command, "CONTENT_NOT_FOUND", "Content not found with id: " + contentId);
    } else {
      Content content = contentOpt.get();
      if (content.getStatus() != ContentStatus.PUBLISHED) {
        reply = buildErrorReply(command, "CONTENT_NOT_PUBLISHED",
            "Content is not published. Current status: " + content.getStatus());
      } else if (content.getCreatorId() != null && content.getCreatorId().toString().equals(buyerId)) {
        reply = buildErrorReply(command, "CANNOT_BUY_OWN_CONTENT", "Cannot purchase your own content");
      } else {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contentId", content.getId());
        payload.put("title", content.getTitle());
        payload.put("price", content.getPrice() != null ? content.getPrice().toString() : "0.00");
        payload.put("creatorId", content.getCreatorId() != null ? content.getCreatorId().toString() : "0");
        payload.put("status", content.getStatus().name());
        reply = buildSuccessReply(command, payload);
      }
    }

    saveProcessedCommand(command, reply);
    return reply;
  }

  public SagaReply processIncrementPurchaseCount(SagaCommand command) {
    Long contentId = ((Number) command.getPayload().get("contentId")).longValue();

    SagaReply reply;
    try {
      contentService.incrementPurchaseCount(contentId);
      reply = buildSuccessReply(command, null);
    } catch (Exception e) {
      log.error("Error incrementing purchase count - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage());
      reply = buildErrorReply(command, "INCREMENT_PURCHASE_COUNT_ERROR", e.getMessage());
    }

    saveProcessedCommand(command, reply);
    return reply;
  }

  private void saveProcessedCommand(SagaCommand command, SagaReply reply) {
    String resultPayload = null;
    if (reply.getPayload() != null) {
      try {
        resultPayload = objectMapper.writeValueAsString(reply.getPayload());
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize reply payload - SagaId: {}", command.getSagaId());
      }
    }
    processedSagaCommandRepository.save(ProcessedSagaCommand.builder()
        .idempotencyKey(command.getIdempotencyKey())
        .sagaId(command.getSagaId())
        .commandType(command.getCommandType())
        .resultSuccess(reply.isSuccess())
        .resultPayload(resultPayload)
        .build());
  }

  private SagaReply buildSuccessReply(SagaCommand command, Map<String, Object> payload) {
    return SagaReply.builder()
        .sagaId(command.getSagaId())
        .commandType(command.getCommandType())
        .success(true)
        .timestamp(Instant.now())
        .payload(payload)
        .build();
  }

  private SagaReply buildErrorReply(SagaCommand command, String errorCode, String errorMessage) {
    return SagaReply.builder()
        .sagaId(command.getSagaId())
        .commandType(command.getCommandType())
        .success(false)
        .errorCode(errorCode)
        .errorMessage(errorMessage)
        .timestamp(Instant.now())
        .build();
  }
}
