package com.ndk.contentservice.saga;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.saga.message.SagaReply;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class ContentSagaReplyProducerTest {

  @Mock
  private KafkaTemplate<String, SagaReply> kafkaTemplate;

  @InjectMocks
  private ContentSagaReplyProducer replyProducer;

  @Test
  @DisplayName("sendReply should send message via kafka template")
  @SuppressWarnings("unchecked")
  void sendReply_Success() {
    String topic = "purchase.saga-replies";
    SagaReply reply = SagaReply.builder()
        .sagaId("saga-123")
        .commandType("VERIFY_CONTENT")
        .success(true)
        .build();

    CompletableFuture<SendResult<String, SagaReply>> future = CompletableFuture.completedFuture(mock(SendResult.class));
    when(kafkaTemplate.send(eq(topic), eq("saga-123"), eq(reply))).thenReturn(future);

    replyProducer.sendReply(topic, reply);

    verify(kafkaTemplate).send(topic, "saga-123", reply);
  }

  @Test
  @DisplayName("sendReply should catch and log exception without rethrowing")
  @SuppressWarnings("unchecked")
  void sendReply_HandlesException() {
    String topic = "purchase.saga-replies";
    SagaReply reply = SagaReply.builder()
        .sagaId("saga-123")
        .build();

    CompletableFuture<SendResult<String, SagaReply>> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new RuntimeException("Kafka down"));
    when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

    // Should not throw
    replyProducer.sendReply(topic, reply);

    verify(kafkaTemplate).send(topic, "saga-123", reply);
  }
}
