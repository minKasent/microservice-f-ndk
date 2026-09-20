package com.ndk.contentservice.saga;

import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.contentservice.entity.ProcessedSagaCommand;
import com.ndk.contentservice.repository.ProcessedSagaCommandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSagaCommandHandler {

  private final ContentSagaProcessor processor;
  private final ProcessedSagaCommandRepository processedSagaCommandRepository;
  private final ContentSagaReplyProducer replyProducer;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${purchase.saga.content-command-topic:purchase.content-command}",
      groupId = "content-saga-group",
      concurrency = "3"
  )
  public void handleCommand(SagaCommand command, Acknowledgment ack) {
    try {
      log.info("Received saga command - CommandType: {}, SagaId: {}",
          command.getCommandType(), command.getSagaId());

      Optional<ProcessedSagaCommand> existing =
          processedSagaCommandRepository.findByIdempotencyKey(command.getIdempotencyKey());

      if (existing.isPresent()) {
        log.info("Duplicate command - re-sending cached reply: idempotencyKey={}", command.getIdempotencyKey());
        replyProducer.sendReply(command.getReplyTopic(), buildReplyFromCached(existing.get()));
        ack.acknowledge();
        return;
      }

      SagaReply reply = switch (command.getCommandType()) {
        case "VERIFY_CONTENT" -> processor.processVerifyContent(command);
        case "INCREMENT_PURCHASE_COUNT" -> processor.processIncrementPurchaseCount(command);
        default -> {
          log.warn("Unknown command type - CommandType: {}, SagaId: {}",
              command.getCommandType(), command.getSagaId());
          yield SagaReply.builder()
              .sagaId(command.getSagaId())
              .commandType(command.getCommandType())
              .success(false)
              .errorCode("UNKNOWN_COMMAND")
              .errorMessage("Unknown command type: " + command.getCommandType())
              .timestamp(Instant.now())
              .build();
        }
      };

      replyProducer.sendReply(command.getReplyTopic(), reply);
      ack.acknowledge();

    } catch (Exception e) {
      log.error("Unexpected error handling saga command - CommandType: {}, SagaId: {}, Error: {}",
          command.getCommandType(), command.getSagaId(), e.getMessage(), e);
      ack.acknowledge();
    }
  }

  private SagaReply buildReplyFromCached(ProcessedSagaCommand cached) {
    Map<String, Object> payload = null;
    if (cached.getResultPayload() != null) {
      try {
        payload = objectMapper.readValue(cached.getResultPayload(),
            objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
      } catch (JsonProcessingException e) {
        log.error("Failed to deserialize cached reply payload - SagaId: {}", cached.getSagaId());
      }
    }
    return SagaReply.builder()
        .sagaId(cached.getSagaId())
        .commandType(cached.getCommandType())
        .success(cached.getResultSuccess() != null && cached.getResultSuccess())
        .timestamp(Instant.now())
        .payload(payload)
        .build();
  }
}
