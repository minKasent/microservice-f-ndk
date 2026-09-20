package com.ndk.purchase.saga;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.purchase.dto.request.PurchaseContentRequest;
import com.ndk.purchase.dto.response.PurchaseDto;
import com.ndk.purchase.entity.Library;
import com.ndk.purchase.entity.Purchase;
import com.ndk.purchase.entity.PurchaseSaga;
import com.ndk.purchase.entity.PurchaseTransaction;
import com.ndk.purchase.enums.PurchaseStatus;
import com.ndk.purchase.enums.SagaCommandType;
import com.ndk.purchase.enums.SagaStep;
import com.ndk.purchase.enums.TransactionStatus;
import com.ndk.purchase.enums.TransactionType;
import com.ndk.purchase.exception.ExceptionEnum;
import com.ndk.purchase.mapper.PurchaseMapper;
import com.ndk.purchase.repository.LibraryRepository;
import com.ndk.purchase.repository.PurchaseRepository;
import com.ndk.purchase.repository.PurchaseSagaRepository;
import com.ndk.purchase.repository.PurchaseTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class PurchaseSagaOrchestrator {

  private final PurchaseSagaRepository sagaRepository;
  private final PurchaseRepository purchaseRepository;
  private final LibraryRepository libraryRepository;
  private final PurchaseTransactionRepository transactionRepository;
  private final PurchaseMapper purchaseMapper;
  private final SagaCommandProducer commandProducer;
  private final ObjectMapper objectMapper;

  private final Counter sagaStartedCounter;
  private final Counter sagaCompletedCounter;
  private final Counter sagaFailedCounter;
  private final Counter sagaCompensatedCounter;
  private final Timer sagaDurationTimer;

  @Value("${purchase.platform-fee-rate:0.30}")
  private BigDecimal platformFeeRate;

  @Value("${purchase.saga.reply-topic:purchase.saga-replies}")
  private String replyTopic;

  public PurchaseSagaOrchestrator(
      PurchaseSagaRepository sagaRepository,
      PurchaseRepository purchaseRepository,
      LibraryRepository libraryRepository,
      PurchaseTransactionRepository transactionRepository,
      PurchaseMapper purchaseMapper,
      SagaCommandProducer commandProducer,
      ObjectMapper objectMapper,
      MeterRegistry meterRegistry
  ) {
    this.sagaRepository = sagaRepository;
    this.purchaseRepository = purchaseRepository;
    this.libraryRepository = libraryRepository;
    this.transactionRepository = transactionRepository;
    this.purchaseMapper = purchaseMapper;
    this.commandProducer = commandProducer;
    this.objectMapper = objectMapper;

    this.sagaStartedCounter = Counter.builder("purchase.saga.started")
        .description("Number of purchase sagas started")
        .register(meterRegistry);
    this.sagaCompletedCounter = Counter.builder("purchase.saga.completed")
        .description("Number of purchase sagas completed successfully")
        .register(meterRegistry);
    this.sagaFailedCounter = Counter.builder("purchase.saga.failed")
        .description("Number of purchase sagas failed")
        .register(meterRegistry);
    this.sagaCompensatedCounter = Counter.builder("purchase.saga.compensated")
        .description("Number of purchase sagas that required compensation")
        .register(meterRegistry);
    this.sagaDurationTimer = Timer.builder("purchase.saga.duration")
        .description("Duration of purchase sagas")
        .register(meterRegistry);
  }

  @Transactional
  public PurchaseDto initiateSaga(String buyerId, PurchaseContentRequest request) {
    String sagaId = UUID.randomUUID().toString();
    Long contentId = request.getContentId();
    log.info("Initiating purchase saga - BuyerId: {}, ContentId: {}", buyerId, contentId);

    // Business validation: reject if content already owned
    if (libraryRepository.existsByUserIdAndContentId(buyerId, contentId)) {
      log.warn("Content already owned - BuyerId: {}, ContentId: {}", buyerId, contentId);
      throw new DevSharingException(ExceptionEnum.CONTENT_ALREADY_OWNED, null);
    }

    // Semantic Lock — reject if another saga already holds the lock
    if (purchaseRepository.existsByBuyerIdAndContentIdAndStatus(buyerId, contentId, PurchaseStatus.PENDING)) {
      log.warn("Purchase already in progress (semantic lock) - BuyerId: {}, ContentId: {}", buyerId, contentId);
      throw new DevSharingException(ExceptionEnum.PURCHASE_ALREADY_IN_PROGRESS, null);
    }

    // Create purchase record with PENDING status
    String purchaseCode = generatePurchaseCode();
    Purchase purchase;
    try {
      purchase = purchaseRepository.save(Purchase.builder()
          .purchaseCode(purchaseCode)
          .buyerId(buyerId)
          .contentId(contentId)
          .creatorId("PENDING")
          .contentPrice(BigDecimal.ZERO)
          .platformFeeRate(platformFeeRate)
          .platformFee(BigDecimal.ZERO)
          .creatorCommissionRate(BigDecimal.ONE.subtract(platformFeeRate))
          .creatorCommission(BigDecimal.ZERO)
          .status(PurchaseStatus.PENDING)
          .purchasedAt(Instant.now())
          .build());
    } catch (DataIntegrityViolationException e) {
      log.warn("Concurrent purchase detected via unique constraint - BuyerId: {}, ContentId: {}", buyerId, contentId);
      throw new DevSharingException(ExceptionEnum.PURCHASE_ALREADY_IN_PROGRESS, null);
    }

    log.info("Purchase record created - PurchaseId: {}, PurchaseCode: {}", purchase.getId(), purchaseCode);

    // Build saga data
    PurchaseSagaData sagaData = PurchaseSagaData.builder()
        .contentId(contentId)
        .buyerId(buyerId)
        .purchaseCode(purchaseCode)
        .platformFeeRate(platformFeeRate)
        .creatorCommissionRate(BigDecimal.ONE.subtract(platformFeeRate))
        .build();

    String sagaDataJson = serializeSagaData(sagaData);

    // Build VERIFY_CONTENT command (Step 1)
    Map<String, Object> payload = new HashMap<>();
    payload.put("contentId", contentId);
    payload.put("buyerId", buyerId);

    SagaCommand command = SagaCommand.builder()
        .sagaId(sagaId)
        .commandType(SagaCommandType.VERIFY_CONTENT.name())
        .idempotencyKey(sagaId + "-VERIFY_CONTENT")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(payload)
        .build();

    PurchaseSaga saga = PurchaseSaga.builder()
        .sagaId(sagaId)
        .purchaseId(purchase.getId())
        .currentStep(SagaStep.VERIFYING_CONTENT)
        .sagaData(sagaDataJson)
        .retryCount(0)
        .build();
    sagaRepository.save(saga);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        commandProducer.sendContentCommand(command);
        sagaStartedCounter.increment();
        log.info("Purchase saga initiated - SagaId: {}, PurchaseId: {}, Step: VERIFYING_CONTENT",
            sagaId, purchase.getId());
      }
    });

    PurchaseDto dto = purchaseMapper.toDto(purchase);
    dto.setContentTitle("Pending verification");
    return dto;
  }

  @Transactional
  public void handleReply(SagaReply reply) {
    String sagaId = reply.getSagaId();
    log.info("Handling saga reply - CommandType: {}, Success: {}", reply.getCommandType(), reply.isSuccess());

    Optional<PurchaseSaga> sagaOpt = sagaRepository.findBySagaId(sagaId);
    if (sagaOpt.isEmpty()) {
      log.error("Saga not found - SagaId: {}", sagaId);
      return;
    }

    PurchaseSaga saga = sagaOpt.get();
    PurchaseSagaData sagaData = deserializeSagaData(saga.getSagaData());
    SagaStep currentStep = saga.getCurrentStep();

    log.debug("Current saga state - Step: {}", currentStep);

    SagaCommandType commandType;
    try {
      commandType = SagaCommandType.valueOf(reply.getCommandType());
    } catch (IllegalArgumentException e) {
      log.warn("Unknown commandType in reply - SagaId: {}, CommandType: {}", sagaId, reply.getCommandType());
      return;
    }

    switch (commandType) {
      case VERIFY_CONTENT           -> handleVerifyContentReply(saga, sagaData, reply);
      case DEDUCT_CREDIT            -> handleDeductBuyerReply(saga, sagaData, reply);
      case ADD_COMMISSION           -> handleAddCommissionReply(saga, sagaData, reply);
      case INCREMENT_PURCHASE_COUNT -> handleIncrementCountReply(saga, sagaData, reply);
      case REVERSE_COMMISSION       -> handleReverseCommissionReply(saga, sagaData, reply);
      case REFUND_CREDIT            -> handleCompensationRefundReply(saga, sagaData, reply);
    }
  }

  private void handleVerifyContentReply(PurchaseSaga saga, PurchaseSagaData sagaData, SagaReply reply) {
    if (saga.getCurrentStep() != SagaStep.VERIFYING_CONTENT) {
      log.warn("Stale VERIFY_CONTENT reply ignored - SagaId: {}, CurrentStep: {}", saga.getSagaId(), saga.getCurrentStep());
      return;
    }
    if (!reply.isSuccess()) {
      log.error("Content verification failed - SagaId: {}, Error: {}", saga.getSagaId(), reply.getErrorMessage());
      failSaga(saga, "Content verification failed: " + reply.getErrorMessage());
      return;
    }

    Map<String, Object> payload = reply.getPayload();
    String title = (String) payload.get("title");
    String creatorId = (String) payload.get("creatorId");
    String status = (String) payload.get("status");
    BigDecimal price = new BigDecimal(payload.get("price").toString());

    if (!"PUBLISHED".equals(status)) {
      log.error("Content not published - SagaId: {}, ContentStatus: {}", saga.getSagaId(), status);
      failSaga(saga, "Content is not published, current status: " + status);
      return;
    }

    if (sagaData.getBuyerId().equals(creatorId)) {
      log.error("Buyer is creator - SagaId: {}, UserId: {}", saga.getSagaId(), sagaData.getBuyerId());
      failSaga(saga, "Buyer cannot purchase their own content");
      return;
    }

    sagaData.setContentTitle(title);
    sagaData.setCreatorId(creatorId);
    sagaData.setContentPrice(price);

    BigDecimal platformFee = price.multiply(sagaData.getPlatformFeeRate())
        .setScale(2, RoundingMode.HALF_UP);
    BigDecimal creatorCommission = price.multiply(sagaData.getCreatorCommissionRate())
        .setScale(2, RoundingMode.HALF_UP);

    sagaData.setPlatformFee(platformFee);
    sagaData.setCreatorCommission(creatorCommission);

    if (payload.containsKey("buyerEmail")) {
      sagaData.setBuyerEmail((String) payload.get("buyerEmail"));
    }
    if (payload.containsKey("buyerName")) {
      sagaData.setBuyerName((String) payload.get("buyerName"));
    }
    if (payload.containsKey("creatorEmail")) {
      sagaData.setCreatorEmail((String) payload.get("creatorEmail"));
    }
    if (payload.containsKey("creatorName")) {
      sagaData.setCreatorName((String) payload.get("creatorName"));
    }

    Purchase purchase = purchaseRepository.findById(saga.getPurchaseId())
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.PURCHASE_NOT_FOUND,
            new Object[]{saga.getPurchaseId().toString()}));

    purchase.setCreatorId(creatorId);
    purchase.setContentPrice(price);
    purchase.setPlatformFee(platformFee);
    purchase.setCreatorCommission(creatorCommission);
    purchaseRepository.save(purchase);

    // Free content: skip wallet deduction and proceed directly to incrementing purchase count
    if (price.compareTo(BigDecimal.ZERO) == 0) {
      log.info("Free content detected, skipping credit deduction - SagaId: {}, ContentId: {}",
          saga.getSagaId(), sagaData.getContentId());

      Map<String, Object> incrementPayload = new HashMap<>();
      incrementPayload.put("contentId", sagaData.getContentId());

      SagaCommand incrementCommand = SagaCommand.builder()
          .sagaId(saga.getSagaId())
          .commandType(SagaCommandType.INCREMENT_PURCHASE_COUNT.name())
          .idempotencyKey(saga.getSagaId() + "-INCREMENT_PURCHASE_COUNT")
          .replyTopic(replyTopic)
          .timestamp(Instant.now())
          .payload(incrementPayload)
          .build();

      saga.setCurrentStep(SagaStep.INCREMENTING_COUNT);
      saga.setSagaData(serializeSagaData(sagaData));
      sagaRepository.save(saga);

      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          commandProducer.sendContentCommand(incrementCommand);
        }
      });
      return;
    }

    // Build DEDUCT_CREDIT command (Step 3)
    Map<String, Object> deductPayload = new HashMap<>();
    deductPayload.put("userId", sagaData.getBuyerId());
    deductPayload.put("amount", price);
    deductPayload.put("reason", "Purchase content: " + sagaData.getContentId());
    deductPayload.put("referenceId", sagaData.getPurchaseCode());

    SagaCommand deductCommand = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.DEDUCT_CREDIT.name())
        .idempotencyKey(saga.getSagaId() + "-DEDUCT_CREDIT")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(deductPayload)
        .build();

    saga.setCurrentStep(SagaStep.DEDUCTING_BUYER);
    saga.setSagaData(serializeSagaData(sagaData));
    sagaRepository.save(saga);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        commandProducer.sendCreditCommand(deductCommand);
      }
    });

    log.info("Content verified, deducting buyer credit - SagaId: {}, Price: {}, CreatorId: {}",
        saga.getSagaId(), price, creatorId);
  }

  private void handleDeductBuyerReply(PurchaseSaga saga, PurchaseSagaData sagaData, SagaReply reply) {
    if (saga.getCurrentStep() != SagaStep.DEDUCTING_BUYER) {
      log.warn("Stale DEDUCT_CREDIT reply ignored - SagaId: {}, CurrentStep: {}", saga.getSagaId(), saga.getCurrentStep());
      return;
    }
    if (!reply.isSuccess()) {
      log.error("Buyer credit deduction failed - SagaId: {}, Error: {}", saga.getSagaId(), reply.getErrorMessage());
      failSaga(saga, "Credit deduction failed: " + reply.getErrorMessage());
      return;
    }

    String creditTransactionId = null;
    if (reply.getPayload() != null && reply.getPayload().containsKey("creditTransactionId")) {
      creditTransactionId = reply.getPayload().get("creditTransactionId").toString();
    }
    sagaData.setDeductCreditTransactionId(creditTransactionId);

    PurchaseTransaction transaction = PurchaseTransaction.builder()
        .purchaseId(saga.getPurchaseId())
        .transactionType(TransactionType.DEDUCT_BUYER)
        .userId(sagaData.getBuyerId())
        .amount(sagaData.getContentPrice())
        .creditTransactionId(creditTransactionId)
        .status(TransactionStatus.COMPLETED)
        .createdAt(Instant.now())
        .completedAt(Instant.now())
        .build();
    transactionRepository.save(transaction);

    // Build ADD_COMMISSION command (Step 4)
    Map<String, Object> commissionPayload = new HashMap<>();
    commissionPayload.put("userId", sagaData.getCreatorId());
    commissionPayload.put("amount", sagaData.getCreatorCommission());
    commissionPayload.put("reason", "Sale commission: " + sagaData.getContentId());
    commissionPayload.put("referenceId", sagaData.getPurchaseCode());

    SagaCommand commissionCommand = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.ADD_COMMISSION.name())
        .idempotencyKey(saga.getSagaId() + "-ADD_COMMISSION")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(commissionPayload)
        .build();

    saga.setCurrentStep(SagaStep.ADDING_COMMISSION);
    saga.setSagaData(serializeSagaData(sagaData));
    sagaRepository.save(saga);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        commandProducer.sendCreditCommand(commissionCommand);
      }
    });

    log.info("Buyer deducted, adding creator commission - SagaId: {}, Commission: {}",
        saga.getSagaId(), sagaData.getCreatorCommission());
  }

  private void handleAddCommissionReply(PurchaseSaga saga, PurchaseSagaData sagaData, SagaReply reply) {
    if (saga.getCurrentStep() != SagaStep.ADDING_COMMISSION) {
      log.warn("Stale ADD_COMMISSION reply ignored - SagaId: {}, CurrentStep: {}", saga.getSagaId(), saga.getCurrentStep());
      return;
    }
    if (!reply.isSuccess()) {
      log.error("Creator commission failed - SagaId: {}, Error: {}. Starting compensation.",
          saga.getSagaId(), reply.getErrorMessage());

      sagaCompensatedCounter.increment();

      // START COMPENSATION - refund buyer
      Map<String, Object> refundPayload = new HashMap<>();
      refundPayload.put("userId", sagaData.getBuyerId());
      refundPayload.put("amount", sagaData.getContentPrice());
      refundPayload.put("reason", "Refund: purchase failed");
      refundPayload.put("referenceId", sagaData.getPurchaseCode());

      SagaCommand refundCommand = SagaCommand.builder()
          .sagaId(saga.getSagaId())
          .commandType(SagaCommandType.REFUND_CREDIT.name())
          .idempotencyKey(saga.getSagaId() + "-REFUND_CREDIT")
          .replyTopic(replyTopic)
          .timestamp(Instant.now())
          .payload(refundPayload)
          .build();

      saga.setCurrentStep(SagaStep.COMPENSATING_REFUND_BUYER);
      saga.setLastError("Creator commission failed: " + reply.getErrorMessage());
      saga.setSagaData(serializeSagaData(sagaData));
      sagaRepository.save(saga);

      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          commandProducer.sendCreditCommand(refundCommand);
        }
      });

      log.info("Compensation started - refunding buyer - SagaId: {}, Amount: {}",
          saga.getSagaId(), sagaData.getContentPrice());
      return;
    }

    String creditTransactionId = null;
    if (reply.getPayload() != null && reply.getPayload().containsKey("creditTransactionId")) {
      creditTransactionId = reply.getPayload().get("creditTransactionId").toString();
    }
    sagaData.setAddCommissionTransactionId(creditTransactionId);

    PurchaseTransaction transaction = PurchaseTransaction.builder()
        .purchaseId(saga.getPurchaseId())
        .transactionType(TransactionType.ADD_CREATOR)
        .userId(sagaData.getCreatorId())
        .amount(sagaData.getCreatorCommission())
        .creditTransactionId(creditTransactionId)
        .status(TransactionStatus.COMPLETED)
        .createdAt(Instant.now())
        .completedAt(Instant.now())
        .build();
    transactionRepository.save(transaction);

    // Build INCREMENT_PURCHASE_COUNT command
    Map<String, Object> incrementPayload = new HashMap<>();
    incrementPayload.put("contentId", sagaData.getContentId());

    SagaCommand incrementCommand = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.INCREMENT_PURCHASE_COUNT.name())
        .idempotencyKey(saga.getSagaId() + "-INCREMENT_PURCHASE_COUNT")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(incrementPayload)
        .build();

    saga.setCurrentStep(SagaStep.INCREMENTING_COUNT);
    saga.setSagaData(serializeSagaData(sagaData));
    sagaRepository.save(saga);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        commandProducer.sendContentCommand(incrementCommand);
      }
    });

    log.info("Commission added, incrementing purchase count - SagaId: {}, ContentId: {}",
        saga.getSagaId(), sagaData.getContentId());
  }

  private void handleIncrementCountReply(PurchaseSaga saga, PurchaseSagaData sagaData, SagaReply reply) {
    if (saga.getCurrentStep() != SagaStep.INCREMENTING_COUNT) {
      log.warn("Stale INCREMENT_PURCHASE_COUNT reply ignored - SagaId: {}, CurrentStep: {}", saga.getSagaId(), saga.getCurrentStep());
      return;
    }
    if (!reply.isSuccess()) {
      log.warn("Increment purchase count failed (non-critical) - SagaId: {}, Error: {}",
          saga.getSagaId(), reply.getErrorMessage());
    } else {
      log.info("Purchase count incremented - SagaId: {}", saga.getSagaId());
    }

    completeSaga(saga, sagaData);
  }

  private void handleCompensationRefundReply(PurchaseSaga saga, PurchaseSagaData sagaData, SagaReply reply) {
    if (saga.getCurrentStep() != SagaStep.COMPENSATING_REFUND_BUYER) {
      log.warn("Stale REFUND_CREDIT reply ignored - SagaId: {}, CurrentStep: {}", saga.getSagaId(), saga.getCurrentStep());
      return;
    }
    Purchase purchase = purchaseRepository.findById(saga.getPurchaseId())
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.PURCHASE_NOT_FOUND,
            new Object[]{saga.getPurchaseId().toString()}));

    if (reply.isSuccess()) {
      String creditTransactionId = null;
      if (reply.getPayload() != null && reply.getPayload().containsKey("creditTransactionId")) {
        creditTransactionId = reply.getPayload().get("creditTransactionId").toString();
      }

      PurchaseTransaction transaction = PurchaseTransaction.builder()
          .purchaseId(saga.getPurchaseId())
          .transactionType(TransactionType.REFUND_BUYER)
          .userId(sagaData.getBuyerId())
          .amount(sagaData.getContentPrice())
          .creditTransactionId(creditTransactionId)
          .status(TransactionStatus.COMPLETED)
          .createdAt(Instant.now())
          .completedAt(Instant.now())
          .build();
      transactionRepository.save(transaction);

      purchase.setStatus(PurchaseStatus.REFUNDED);
      purchaseRepository.save(purchase);

      saga.setCurrentStep(SagaStep.COMPENSATION_COMPLETED);
      saga.setCompletedAt(Instant.now());
      saga.setSagaData(serializeSagaData(sagaData));
      sagaRepository.save(saga);

      sagaFailedCounter.increment();
      log.info("Compensation completed - buyer refunded - SagaId: {}, PurchaseStatus: REFUNDED", saga.getSagaId());

    } else {
      log.error("CRITICAL: Compensation refund failed - SagaId: {}, BuyerId: {}, Amount: {}, Error: {}. Manual intervention required.",
          saga.getSagaId(), sagaData.getBuyerId(), sagaData.getContentPrice(), reply.getErrorMessage());

      purchase.setStatus(PurchaseStatus.FAILED);
      purchaseRepository.save(purchase);

      saga.setCurrentStep(SagaStep.FAILED);
      saga.setLastError("CRITICAL: Compensation refund failed: " + reply.getErrorMessage());
      saga.setCompletedAt(Instant.now());
      saga.setSagaData(serializeSagaData(sagaData));
      sagaRepository.save(saga);

      sagaFailedCounter.increment();
    }
  }

  private void completeSaga(PurchaseSaga saga, PurchaseSagaData sagaData) {
    Purchase purchase = purchaseRepository.findById(saga.getPurchaseId())
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.PURCHASE_NOT_FOUND,
            new Object[]{saga.getPurchaseId().toString()}));

    if (purchase.getStatus() != PurchaseStatus.PENDING) {
      log.error("Purchase status changed during saga - SagaId: {}, Expected: PENDING, Actual: {}",
          saga.getSagaId(), purchase.getStatus());
      startFullCompensation(saga, sagaData, "Purchase status changed during saga execution: " + purchase.getStatus());
      return;
    }

    if (libraryRepository.existsByUserIdAndContentId(sagaData.getBuyerId(), sagaData.getContentId())) {
      log.warn("Duplicate library entry detected - SagaId: {}, BuyerId: {}, ContentId: {}",
          saga.getSagaId(), sagaData.getBuyerId(), sagaData.getContentId());
      startFullCompensation(saga, sagaData, "Duplicate purchase detected: content already in library");
      return;
    }

    Library library = Library.builder()
        .userId(sagaData.getBuyerId())
        .contentId(sagaData.getContentId())
        .purchaseId(saga.getPurchaseId())
        .accessGrantedAt(Instant.now())
        .accessCount(0)
        .isActive(true)
        .build();
    libraryRepository.save(library);

    purchase.setStatus(PurchaseStatus.COMPLETED);
    purchase.setCompletedAt(Instant.now());
    purchaseRepository.save(purchase);

    saga.setCurrentStep(SagaStep.COMPLETED);
    saga.setCompletedAt(Instant.now());
    saga.setSagaData(serializeSagaData(sagaData));
    sagaRepository.save(saga);

    sagaCompletedCounter.increment();
    sagaDurationTimer.record(java.time.Duration.between(saga.getCreatedAt(), Instant.now()));

    log.info("Purchase saga completed - SagaId: {}, PurchaseId: {}, BuyerId: {}, ContentId: {}",
        saga.getSagaId(), saga.getPurchaseId(), sagaData.getBuyerId(), sagaData.getContentId());

    try {
      sendPurchaseNotification(sagaData);
    } catch (Exception e) {
      log.warn("Failed to send purchase notification - SagaId: {}, Error: {}", saga.getSagaId(), e.getMessage());
    }
  }

  private void startFullCompensation(PurchaseSaga saga, PurchaseSagaData sagaData, String reason) {
    log.warn("Starting full compensation - SagaId: {}, Reason: {}", saga.getSagaId(), reason);

    sagaCompensatedCounter.increment();
    saga.setLastError(reason);

    // Free content: no financial deduction occurred, skip reverse/refund commands
    if (sagaData.getContentPrice() != null && sagaData.getContentPrice().compareTo(BigDecimal.ZERO) == 0) {
      log.info("Free content detected, skipping reverse commission and refund - SagaId: {}", saga.getSagaId());
      failSaga(saga, reason);
      return;
    }

    Map<String, Object> reversePayload = new HashMap<>();
    reversePayload.put("userId", sagaData.getCreatorId());
    reversePayload.put("amount", sagaData.getCreatorCommission());
    reversePayload.put("reason", "Reverse commission: " + reason);
    reversePayload.put("referenceId", sagaData.getPurchaseCode());

    SagaCommand reverseCommand = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.REVERSE_COMMISSION.name())
        .idempotencyKey(saga.getSagaId() + "-REVERSE_COMMISSION")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(reversePayload)
        .build();

    saga.setCurrentStep(SagaStep.COMPENSATING_REVERSE_COMMISSION);
    saga.setSagaData(serializeSagaData(sagaData));
    sagaRepository.save(saga);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        commandProducer.sendCreditCommand(reverseCommand);
      }
    });
  }

  private void handleReverseCommissionReply(PurchaseSaga saga, PurchaseSagaData sagaData, SagaReply reply) {
    if (saga.getCurrentStep() != SagaStep.COMPENSATING_REVERSE_COMMISSION) {
      log.warn("Stale REVERSE_COMMISSION reply ignored - SagaId: {}, CurrentStep: {}", saga.getSagaId(), saga.getCurrentStep());
      return;
    }
    if (!reply.isSuccess()) {
      log.error("CRITICAL: Commission reversal failed - SagaId: {}, CreatorId: {}, Amount: {}, Error: {}. Proceeding to refund buyer.",
          saga.getSagaId(), sagaData.getCreatorId(), sagaData.getCreatorCommission(), reply.getErrorMessage());
    } else {
      log.info("Commission reversed successfully - SagaId: {}, CreatorId: {}, Amount: {}",
          saga.getSagaId(), sagaData.getCreatorId(), sagaData.getCreatorCommission());
    }

    Map<String, Object> refundPayload = new HashMap<>();
    refundPayload.put("userId", sagaData.getBuyerId());
    refundPayload.put("amount", sagaData.getContentPrice());
    refundPayload.put("reason", "Refund: purchase compensated");
    refundPayload.put("referenceId", sagaData.getPurchaseCode());

    SagaCommand refundCommand = SagaCommand.builder()
        .sagaId(saga.getSagaId())
        .commandType(SagaCommandType.REFUND_CREDIT.name())
        .idempotencyKey(saga.getSagaId() + "-REFUND_CREDIT")
        .replyTopic(replyTopic)
        .timestamp(Instant.now())
        .payload(refundPayload)
        .build();

    saga.setCurrentStep(SagaStep.COMPENSATING_REFUND_BUYER);
    saga.setSagaData(serializeSagaData(sagaData));
    sagaRepository.save(saga);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        commandProducer.sendCreditCommand(refundCommand);
      }
    });

    log.info("Commission reversed, now refunding buyer - SagaId: {}, Amount: {}",
        saga.getSagaId(), sagaData.getContentPrice());
  }

  private void sendPurchaseNotification(PurchaseSagaData sagaData) {
    log.info("Purchase notification queued - BuyerId: {}, ContentId: {}, ContentTitle: {}",
        sagaData.getBuyerId(), sagaData.getContentId(), sagaData.getContentTitle());
  }

  private void failSaga(PurchaseSaga saga, String error) {
    Purchase purchase = purchaseRepository.findById(saga.getPurchaseId())
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.PURCHASE_NOT_FOUND,
            new Object[]{saga.getPurchaseId().toString()}));

    purchase.setStatus(PurchaseStatus.FAILED);
    purchaseRepository.save(purchase);

    saga.setCurrentStep(SagaStep.FAILED);
    saga.setLastError(error);
    saga.setCompletedAt(Instant.now());
    sagaRepository.save(saga);

    sagaFailedCounter.increment();
    log.error("Purchase saga failed - SagaId: {}, PurchaseId: {}, Error: {}",
        saga.getSagaId(), saga.getPurchaseId(), error);
  }

  private String generatePurchaseCode() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    return "PUR-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private String serializeSagaData(PurchaseSagaData sagaData) {
    try {
      return objectMapper.writeValueAsString(sagaData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize saga data", e);
    }
  }

  private PurchaseSagaData deserializeSagaData(String json) {
    try {
      return objectMapper.readValue(json, PurchaseSagaData.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize saga data", e);
    }
  }
}
