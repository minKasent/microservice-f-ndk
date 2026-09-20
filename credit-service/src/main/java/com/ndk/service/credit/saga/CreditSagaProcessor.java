package com.ndk.service.credit.saga;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.service.credit.entity.ProcessedSagaCommand;
import com.ndk.service.credit.entity.TransactionType;
import com.ndk.service.credit.repository.ProcessedSagaCommandRepository;
import com.ndk.service.credit.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditSagaProcessor {

  private final WalletService walletService;
  private final ProcessedSagaCommandRepository processedSagaCommandRepository;
  private final ObjectMapper objectMapper;

  public SagaReply processDeductCredit(SagaCommand command) {
    Map<String, Object> payload = command.getPayload();
    Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
    BigDecimal amount = new BigDecimal(String.valueOf(payload.get("amount")));
    String reason = (String) payload.get("reason");
    String referenceId = (String) payload.get("referenceId");

    SagaReply reply;
    try {
      walletService.deductCredit(userId, amount, TransactionType.PURCHASE, reason, referenceId);
      reply = buildSuccessReply(command, Map.of("creditTransactionId", referenceId));
    } catch (DevSharingException e) {
      log.warn("Deduct credit failed - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage());
      reply = buildErrorReply(command, e.getExceptionInfo().getErrorCode(), e.getExceptionInfo().getErrorMsg());
    } catch (Exception e) {
      log.error("Unexpected error deducting credit - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage(), e);
      reply = buildErrorReply(command, "INTERNAL_ERROR", "Unexpected error: " + e.getMessage());
    }
    saveProcessedCommand(command, reply);
    return reply;
  }

  public SagaReply processAddCommission(SagaCommand command) {
    Map<String, Object> payload = command.getPayload();
    Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
    BigDecimal amount = new BigDecimal(String.valueOf(payload.get("amount")));
    String reason = (String) payload.get("reason");
    String referenceId = (String) payload.get("referenceId");

    SagaReply reply;
    try {
      walletService.addCredit(userId, amount, TransactionType.EARNING, reason, referenceId);
      reply = buildSuccessReply(command, Map.of("creditTransactionId", referenceId));
    } catch (DevSharingException e) {
      log.warn("Add commission failed - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage());
      reply = buildErrorReply(command, e.getExceptionInfo().getErrorCode(), e.getExceptionInfo().getErrorMsg());
    } catch (Exception e) {
      log.error("Unexpected error adding commission - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage(), e);
      reply = buildErrorReply(command, "INTERNAL_ERROR", "Unexpected error: " + e.getMessage());
    }
    saveProcessedCommand(command, reply);
    return reply;
  }

  public SagaReply processRefundCredit(SagaCommand command) {
    Map<String, Object> payload = command.getPayload();
    Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
    BigDecimal amount = new BigDecimal(String.valueOf(payload.get("amount")));
    String reason = (String) payload.get("reason");
    String referenceId = (String) payload.get("referenceId");

    SagaReply reply;
    try {
      walletService.addCredit(userId, amount, TransactionType.REFUND, reason, referenceId);
      reply = buildSuccessReply(command, Map.of("creditTransactionId", referenceId));
    } catch (DevSharingException e) {
      log.warn("Refund credit failed - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage());
      reply = buildErrorReply(command, e.getExceptionInfo().getErrorCode(), e.getExceptionInfo().getErrorMsg());
    } catch (Exception e) {
      log.error("Unexpected error refunding credit - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage(), e);
      reply = buildErrorReply(command, "INTERNAL_ERROR", "Unexpected error: " + e.getMessage());
    }
    saveProcessedCommand(command, reply);
    return reply;
  }

  public SagaReply processReverseCommission(SagaCommand command) {
    Map<String, Object> payload = command.getPayload();
    Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
    BigDecimal amount = new BigDecimal(String.valueOf(payload.get("amount")));
    String reason = (String) payload.get("reason");
    String referenceId = (String) payload.get("referenceId");

    SagaReply reply;
    try {
      walletService.deductCredit(userId, amount, TransactionType.PURCHASE, reason, referenceId);
      reply = buildSuccessReply(command, Map.of("creditTransactionId", referenceId));
    } catch (DevSharingException e) {
      log.warn("Reverse commission failed - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage());
      reply = buildErrorReply(command, e.getExceptionInfo().getErrorCode(), e.getExceptionInfo().getErrorMsg());
    } catch (Exception e) {
      log.error("Unexpected error reversing commission - SagaId: {}, Error: {}", command.getSagaId(), e.getMessage(), e);
      reply = buildErrorReply(command, "INTERNAL_ERROR", "Unexpected error: " + e.getMessage());
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
