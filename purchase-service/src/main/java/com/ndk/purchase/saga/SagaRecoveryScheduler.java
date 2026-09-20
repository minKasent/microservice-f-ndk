package com.ndk.purchase.saga;

import com.ndk.common.saga.message.SagaCommand;
import com.ndk.purchase.entity.PurchaseSaga;
import com.ndk.purchase.enums.PurchaseStatus;
import com.ndk.purchase.enums.SagaCommandType;
import com.ndk.purchase.enums.SagaStep;
import com.ndk.purchase.repository.PurchaseRepository;
import com.ndk.purchase.repository.PurchaseSagaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "purchase.saga.recovery.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SagaRecoveryScheduler {

  private final PurchaseSagaRepository sagaRepository;
  private final PurchaseRepository purchaseRepository;
  private final SagaCommandProducer commandProducer;
  private final ObjectMapper objectMapper;

  @Value("${purchase.saga.timeout-seconds:120}")
  private int timeoutSeconds;

  @Value("${purchase.saga.max-retries:3}")
  private int maxRetries;

  @Value("${purchase.saga.reply-topic:purchase.saga-replies}")
  private String replyTopic;

  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void recoverStuckSagas() {
    Instant cutoff = Instant.now().minusSeconds(timeoutSeconds);

    List<SagaStep> terminalSteps = List.of(SagaStep.COMPLETED, SagaStep.COMPENSATION_COMPLETED, SagaStep.FAILED);
    List<PurchaseSaga> stuckSagas = sagaRepository.findByCurrentStepNotInAndUpdatedAtBefore(terminalSteps, cutoff);

    if (stuckSagas.isEmpty()) {
      return;
    }

    log.info("Found stuck sagas: {}", stuckSagas.size());

    for (PurchaseSaga saga : stuckSagas) {
      recoverSaga(saga);
    }
  }

  private void recoverSaga(PurchaseSaga saga) {
    saga.setRetryCount(saga.getRetryCount() + 1);

    if (saga.getRetryCount() > maxRetries) {
      log.error("Saga exceeded max retries - SagaId: {}, Step: {}, RetryCount: {}. Marking as FAILED.",
          saga.getSagaId(), saga.getCurrentStep(), saga.getRetryCount());
      saga.setCurrentStep(SagaStep.FAILED);
      saga.setLastError("Exceeded max retries (" + maxRetries + ")");
      saga.setCompletedAt(Instant.now());
      sagaRepository.save(saga);

      // Release semantic lock on Purchase
      purchaseRepository.findById(saga.getPurchaseId()).ifPresent(purchase -> {
        purchase.setStatus(PurchaseStatus.FAILED);
        purchaseRepository.save(purchase);
      });

      // Trigger compensation if credit was already deducted
      try {
        PurchaseSagaData sagaData = objectMapper.readValue(saga.getSagaData(), PurchaseSagaData.class);
        triggerCompensationOnExhaustion(saga, sagaData);
      } catch (Exception e) {
        log.error("Failed to trigger compensation on retry exhaustion - SagaId: {}, Error: {}",
            saga.getSagaId(), e.getMessage());
      }
      return;
    }

    log.info("Retrying stuck saga - SagaId: {}, Step: {}, RetryCount: {}/{}",
        saga.getSagaId(), saga.getCurrentStep(), saga.getRetryCount(), maxRetries);

    try {
      PurchaseSagaData sagaData = objectMapper.readValue(saga.getSagaData(), PurchaseSagaData.class);
      SagaCommand command = buildRetryCommand(saga, sagaData);

      if (command == null) {
        log.warn("Cannot build retry command for saga step - SagaId: {}, Step: {}",
            saga.getSagaId(), saga.getCurrentStep());
        return;
      }

      String commandType = command.getCommandType();
      if (SagaCommandType.VERIFY_CONTENT.name().equals(commandType)
          || SagaCommandType.INCREMENT_PURCHASE_COUNT.name().equals(commandType)) {
        commandProducer.sendContentCommand(command);
      } else {
        commandProducer.sendCreditCommand(command);
      }

      sagaRepository.save(saga);
      log.info("Retry command sent - SagaId: {}, CommandType: {}", saga.getSagaId(), commandType);

    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize saga data during recovery - SagaId: {}, Error: {}",
          saga.getSagaId(), e.getMessage());
      saga.setLastError("Recovery failed: " + e.getMessage());
      saga.setCompletedAt(Instant.now());
      sagaRepository.save(saga);
    }
  }

  private SagaCommand buildRetryCommand(PurchaseSaga saga, PurchaseSagaData sagaData) {
    Map<String, Object> payload = new HashMap<>();
    String commandType;

    switch (saga.getCurrentStep()) {
      case VERIFYING_CONTENT -> {
        commandType = SagaCommandType.VERIFY_CONTENT.name();
        payload.put("contentId", sagaData.getContentId());
        payload.put("buyerId", sagaData.getBuyerId());
      }
      case DEDUCTING_BUYER -> {
        commandType = SagaCommandType.DEDUCT_CREDIT.name();
        payload.put("userId", sagaData.getBuyerId());
        payload.put("amount", sagaData.getContentPrice());
        payload.put("reason", "Purchase content: " + sagaData.getContentId());
        payload.put("referenceId", sagaData.getPurchaseCode());
      }
      case ADDING_COMMISSION -> {
        commandType = SagaCommandType.ADD_COMMISSION.name();
        payload.put("userId", sagaData.getCreatorId());
        payload.put("amount", sagaData.getCreatorCommission());
        payload.put("reason", "Sale commission: " + sagaData.getContentId());
        payload.put("referenceId", sagaData.getPurchaseCode());
      }
      case INCREMENTING_COUNT -> {
        commandType = SagaCommandType.INCREMENT_PURCHASE_COUNT.name();
        payload.put("contentId", sagaData.getContentId());
      }
      case COMPENSATING_REVERSE_COMMISSION -> {
        commandType = SagaCommandType.REVERSE_COMMISSION.name();
        payload.put("userId", sagaData.getCreatorId());
        payload.put("amount", sagaData.getCreatorCommission());
        payload.put("reason", "Reverse commission: saga recovery");
        payload.put("referenceId", sagaData.getPurchaseCode());
      }
      case COMPENSATING_REFUND_BUYER -> {
        commandType = SagaCommandType.REFUND_CREDIT.name();
        payload.put("userId", sagaData.getBuyerId());
        payload.put("amount", sagaData.getContentPrice());
        payload.put("reason", "Refund: purchase failed");
        payload.put("referenceId", sagaData.getPurchaseCode());
      }
      default -> {
        return null;
      }
    }

    return SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(commandType)
        .idempotencyKey(saga.getSagaId() + "-" + commandType)
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(payload)
        .build();
  }

  private void triggerCompensationOnExhaustion(PurchaseSaga saga, PurchaseSagaData sagaData) {
    SagaStep step = saga.getCurrentStep();
    if (step == SagaStep.INCREMENTING_COUNT || step == SagaStep.COMPENSATING_REVERSE_COMMISSION) {
      triggerReverseCommission(saga, sagaData);
      triggerRefundBuyer(saga, sagaData);
    } else if (step == SagaStep.ADDING_COMMISSION || step == SagaStep.COMPENSATING_REFUND_BUYER) {
      triggerRefundBuyer(saga, sagaData);
    }
  }

  private void triggerReverseCommission(PurchaseSaga saga, PurchaseSagaData sagaData) {
    if (sagaData.getCreatorCommission() == null || sagaData.getCreatorCommission().compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("userId", sagaData.getCreatorId());
    payload.put("amount", sagaData.getCreatorCommission());
    payload.put("reason", "Reverse commission: recovery retry exhausted");
    payload.put("referenceId", sagaData.getPurchaseCode() + "-RECOVERY");

    SagaCommand command = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.REVERSE_COMMISSION.name())
        .idempotencyKey(saga.getSagaId() + "-RECOVERY-REVERSE")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(payload)
        .build();

    commandProducer.sendCreditCommand(command);
    log.info("Sent recovery reverse commission command - SagaId: {}, CreatorId: {}",
        saga.getSagaId(), sagaData.getCreatorId());
  }

  private void triggerRefundBuyer(PurchaseSaga saga, PurchaseSagaData sagaData) {
    if (sagaData.getContentPrice() == null || sagaData.getContentPrice().compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("userId", sagaData.getBuyerId());
    payload.put("amount", sagaData.getContentPrice());
    payload.put("reason", "Refund credit: recovery retry exhausted");
    payload.put("referenceId", sagaData.getPurchaseCode() + "-RECOVERY");

    SagaCommand command = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.REFUND_CREDIT.name())
        .idempotencyKey(saga.getSagaId() + "-RECOVERY-REFUND")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(payload)
        .build();

    commandProducer.sendCreditCommand(command);
    log.info("Sent recovery refund command - SagaId: {}, BuyerId: {}",
        saga.getSagaId(), sagaData.getBuyerId());
  }
}
