package com.ndk.service.credit.saga;

import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.service.credit.entity.ProcessedSagaCommand;
import com.ndk.service.credit.repository.ProcessedSagaCommandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditSagaCommandHandler {

  private final CreditSagaProcessor processor;
  private final ProcessedSagaCommandRepository processedSagaCommandRepository;
  private final CreditSagaReplyProducer replyProducer;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${purchase.saga.credit-command-topic:purchase.credit-command}",
      groupId = "credit-saga-group",
      concurrency = "3"
  )
  public void handleCommand(SagaCommand command, Acknowledgment ack) {
    try {
      log.info("Received saga command: type={}, sagaId={}", command.getCommandType(), command.getSagaId());

      Optional<ProcessedSagaCommand> existing =
          processedSagaCommandRepository.findByIdempotencyKey(command.getIdempotencyKey());

      if (existing.isPresent()) {
        log.info("Duplicate command - re-sending cached reply: idempotencyKey={}", command.getIdempotencyKey());
        replyProducer.sendReply(command.getReplyTopic(), deserializeReply(existing.get().getResultPayload()));
        ack.acknowledge();
        return;
      }

      SagaReply reply = switch (command.getCommandType()) {
        case "DEDUCT_CREDIT" -> processor.processDeductCredit(command);
        case "ADD_COMMISSION" -> processor.processAddCommission(command);
        case "REFUND_CREDIT" -> processor.processRefundCredit(command);
        case "REVERSE_COMMISSION" -> processor.processReverseCommission(command);
        default -> {
          log.warn("Unknown command type: {}, sagaId={}", command.getCommandType(), command.getSagaId());
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
      log.error("Unexpected error processing saga command: sagaId={}, type={}",
          command.getSagaId(), command.getCommandType(), e);
      ack.acknowledge();
    }
  }

  private SagaReply deserializeReply(String json) {
    try {
      return objectMapper.readValue(json, SagaReply.class);
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize cached SagaReply", e);
      throw new RuntimeException("Failed to deserialize cached saga reply", e);
    }
  }
}
