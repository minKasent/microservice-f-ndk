package com.ndk.contentservice.saga;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndk.common.saga.message.SagaCommand;
import com.ndk.common.saga.message.SagaReply;
import com.ndk.contentservice.entity.ProcessedSagaCommand;
import com.ndk.contentservice.repository.ProcessedSagaCommandRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class ContentSagaCommandHandlerTest {

  @Mock
  private ContentSagaProcessor processor;

  @Mock
  private ProcessedSagaCommandRepository processedSagaCommandRepository;

  @Mock
  private ContentSagaReplyProducer replyProducer;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private Acknowledgment acknowledgment;

  @InjectMocks
  private ContentSagaCommandHandler commandHandler;

  @Test
  @DisplayName("Should re-send cached reply and ack when command is duplicate")
  void handleCommand_DuplicateCommand_ResendsCachedReply() {
    SagaCommand command = SagaCommand.builder()
        .sagaId("saga-1")
        .commandType("VERIFY_CONTENT")
        .idempotencyKey("idem-1")
        .replyTopic("purchase.saga-replies")
        .build();

    ProcessedSagaCommand cached = ProcessedSagaCommand.builder()
        .sagaId("saga-1")
        .commandType("VERIFY_CONTENT")
        .resultSuccess(true)
        .resultPayload("{\"price\":\"50.00\"}")
        .processedAt(Instant.now())
        .build();

    when(processedSagaCommandRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(cached));

    commandHandler.handleCommand(command, acknowledgment);

    verify(replyProducer).sendReply(eq("purchase.saga-replies"), any(SagaReply.class));
    verify(processor, never()).processVerifyContent(any());
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should process VERIFY_CONTENT command and send reply")
  void handleCommand_VerifyContent_Success() {
    SagaCommand command = SagaCommand.builder()
        .sagaId("saga-2")
        .commandType("VERIFY_CONTENT")
        .idempotencyKey("idem-2")
        .replyTopic("purchase.saga-replies")
        .payload(Map.of("contentId", 1L, "buyerId", "100"))
        .build();

    SagaReply reply = SagaReply.builder()
        .sagaId("saga-2")
        .commandType("VERIFY_CONTENT")
        .success(true)
        .build();

    when(processedSagaCommandRepository.findByIdempotencyKey("idem-2")).thenReturn(Optional.empty());
    when(processor.processVerifyContent(command)).thenReturn(reply);

    commandHandler.handleCommand(command, acknowledgment);

    verify(processor).processVerifyContent(command);
    verify(replyProducer).sendReply("purchase.saga-replies", reply);
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should process INCREMENT_PURCHASE_COUNT command and send reply")
  void handleCommand_IncrementPurchaseCount_Success() {
    SagaCommand command = SagaCommand.builder()
        .sagaId("saga-3")
        .commandType("INCREMENT_PURCHASE_COUNT")
        .idempotencyKey("idem-3")
        .replyTopic("purchase.saga-replies")
        .payload(Map.of("contentId", 1L))
        .build();

    SagaReply reply = SagaReply.builder()
        .sagaId("saga-3")
        .commandType("INCREMENT_PURCHASE_COUNT")
        .success(true)
        .build();

    when(processedSagaCommandRepository.findByIdempotencyKey("idem-3")).thenReturn(Optional.empty());
    when(processor.processIncrementPurchaseCount(command)).thenReturn(reply);

    commandHandler.handleCommand(command, acknowledgment);

    verify(processor).processIncrementPurchaseCount(command);
    verify(replyProducer).sendReply("purchase.saga-replies", reply);
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle unknown command type by sending error reply")
  void handleCommand_UnknownCommand_SendsErrorReply() {
    SagaCommand command = SagaCommand.builder()
        .sagaId("saga-4")
        .commandType("UNKNOWN_ACTION")
        .idempotencyKey("idem-4")
        .replyTopic("purchase.saga-replies")
        .build();

    when(processedSagaCommandRepository.findByIdempotencyKey("idem-4")).thenReturn(Optional.empty());

    commandHandler.handleCommand(command, acknowledgment);

    verify(replyProducer).sendReply(eq("purchase.saga-replies"), any(SagaReply.class));
    verify(acknowledgment).acknowledge();
  }
}
