package com.ndk.contentservice.saga;

import com.ndk.common.saga.message.SagaReply;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSagaReplyProducer {

  private final KafkaTemplate<String, SagaReply> kafkaTemplate;
  private static final int KAFKA_SEND_TIMEOUT_SECONDS = 10;

  public void sendReply(String replyTopic, SagaReply reply) {
    try {
      kafkaTemplate.send(replyTopic, reply.getSagaId(), reply)
          .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("send reply error", e);
    }
  }
}
