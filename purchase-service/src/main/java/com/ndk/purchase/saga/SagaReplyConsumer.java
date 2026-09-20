package com.ndk.purchase.saga;

import com.ndk.common.saga.message.SagaReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaReplyConsumer {

  private final PurchaseSagaOrchestrator orchestrator;

  @KafkaListener(
      topics = "${purchase.saga.reply-topic:purchase.saga-replies}",
      groupId = "purchase-saga-group",
      concurrency = "3"
  )
  public void onReply(SagaReply reply, Acknowledgment acknowledgment) {
    log.info("Saga reply received - SagaId: {}, CommandType: {}, Success: {}",
        reply.getSagaId(), reply.getCommandType(), reply.isSuccess());
    try {
      orchestrator.handleReply(reply);
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Error handling saga reply - SagaId: {}, CommandType: {}, Error: {}",
          reply.getSagaId(), reply.getCommandType(), e.getMessage(), e);
      throw e;
    }
  }
}
